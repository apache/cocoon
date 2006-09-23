/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.wsrp.consumer;

import oasis.names.tc.wsrp.v1.intf.WSRP_v1_Markup_PortType;
import oasis.names.tc.wsrp.v1.intf.WSRP_v1_PortletManagement_PortType;
import oasis.names.tc.wsrp.v1.types.BlockingInteractionResponse;
import oasis.names.tc.wsrp.v1.types.ClientData;
import oasis.names.tc.wsrp.v1.types.CookieProtocol;
import oasis.names.tc.wsrp.v1.types.DestroyPortletsResponse;
import oasis.names.tc.wsrp.v1.types.Extension;
import oasis.names.tc.wsrp.v1.types.InteractionParams;
import oasis.names.tc.wsrp.v1.types.InvalidCookieFault;
import oasis.names.tc.wsrp.v1.types.MarkupContext;
import oasis.names.tc.wsrp.v1.types.MarkupParams;
import oasis.names.tc.wsrp.v1.types.MarkupResponse;
import oasis.names.tc.wsrp.v1.types.MarkupType;
import oasis.names.tc.wsrp.v1.types.PortletContext;
import oasis.names.tc.wsrp.v1.types.PortletDescription;
import oasis.names.tc.wsrp.v1.types.PortletDescriptionResponse;
import oasis.names.tc.wsrp.v1.types.PortletPropertyDescriptionResponse;
import oasis.names.tc.wsrp.v1.types.PropertyList;
import oasis.names.tc.wsrp.v1.types.RegistrationContext;
import oasis.names.tc.wsrp.v1.types.ReturnAny;
import oasis.names.tc.wsrp.v1.types.RuntimeContext;
import oasis.names.tc.wsrp.v1.types.ServiceDescription;
import oasis.names.tc.wsrp.v1.types.StateChange;
import oasis.names.tc.wsrp.v1.types.Templates;
import oasis.names.tc.wsrp.v1.types.UserContext;
import oasis.names.tc.wsrp.v1.types.ClonePortlet;
import oasis.names.tc.wsrp.v1.types.DestroyPortlets;
import oasis.names.tc.wsrp.v1.types.GetMarkup;
import oasis.names.tc.wsrp.v1.types.GetPortletDescription;
import oasis.names.tc.wsrp.v1.types.GetPortletProperties;
import oasis.names.tc.wsrp.v1.types.GetPortletPropertyDescription;
import oasis.names.tc.wsrp.v1.types.InitCookie;
import oasis.names.tc.wsrp.v1.types.PerformBlockingInteraction;
import oasis.names.tc.wsrp.v1.types.ReleaseSessions;
import oasis.names.tc.wsrp.v1.types.SetPortletProperties;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.wsrp.logging.WSRPLogger;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.commons.lang.ArrayUtils;
import org.apache.wsrp4j.consumer.ConsumerEnvironment;
import org.apache.wsrp4j.consumer.GroupSessionMgr;
import org.apache.wsrp4j.consumer.InteractionRequest;
import org.apache.wsrp4j.consumer.MarkupRequest;
import org.apache.wsrp4j.consumer.PortletDriver;
import org.apache.wsrp4j.consumer.Producer;
import org.apache.wsrp4j.consumer.URLRewriter;
import org.apache.wsrp4j.consumer.URLTemplateComposer;
import org.apache.wsrp4j.consumer.User;
import org.apache.wsrp4j.consumer.UserSessionMgr;
import org.apache.wsrp4j.consumer.WSRPBaseRequest;
import org.apache.wsrp4j.consumer.WSRPPortlet;
import org.apache.wsrp4j.exception.WSRPException;
import org.apache.wsrp4j.exception.WSRPXHelper;
import org.apache.wsrp4j.log.Logger;
import org.apache.wsrp4j.util.Constants;
import org.apache.wsrp4j.util.ParameterChecker;

/**
 * This class implements a portlet driver.
 *
 * @version $Id$
 */
public class PortletDriverImpl
    extends AbstractLogEnabled
    implements PortletDriver, RequiresConsumerEnvironment, RequiresPortalService {

    protected WSRPPortlet portlet;
    protected WSRP_v1_Markup_PortType markupPort;
    protected WSRP_v1_PortletManagement_PortType portletPort;
    protected ConsumerEnvironment consumerEnv;
    protected Producer producer;
    protected ParameterChecker parameterChecker;
    protected CookieProtocol initCookie = CookieProtocol.none;
    protected PortletDescription desc;

    protected Logger logger;

    /** The portal service. */
    protected PortalService service;

    /**
     * @see org.apache.cocoon.portal.wsrp.consumer.RequiresConsumerEnvironment#setConsumerEnvironment(org.apache.wsrp4j.consumer.ConsumerEnvironment)
     */
    public void setConsumerEnvironment(ConsumerEnvironment env) {
        this.consumerEnv = env;
    }

    /**
     * @see org.apache.cocoon.portal.wsrp.consumer.RequiresPortalService#setPortalService(org.apache.cocoon.portal.PortalService)
     */
    public void setPortalService(PortalService service) {
       this.service = service;
    }

    /**
     * Set the wsrp portlet and initialize this component.
     * @param portlet
     * @throws WSRPException
     */
    public void init(WSRPPortlet portlet) 
    throws WSRPException {
		this.parameterChecker = new ParameterChecker();

        this.portlet = portlet;
        this.producer = consumerEnv.getProducerRegistry().getProducer(portlet.getPortletKey().getProducerId());
        
        this.portletPort = this.producer.getPortletManagementInterface();
        this.desc = this.producer.getPortletDescription(this.portlet.getPortletKey().getPortletHandle());

        ServiceDescription serviceDescription = this.producer.getServiceDescription(false);
        if (serviceDescription != null) {
            this.initCookie = serviceDescription.getRequiresInitCookie();
            if (initCookie == null) {
                initCookie = CookieProtocol.none; // TODO - get from config
            }
        }
    }

    /**
     * Get the portlet this driver is bind to.
     * 
     * @return The enity
     **/
    public WSRPPortlet getPortlet() {
        return this.portlet;
    }

    protected void resetInitCookie(String userID) throws WSRPException {

        UserSessionMgr userSession =
            this.consumerEnv.getSessionHandler().getUserSession(
                getPortlet().getPortletKey().getProducerId(),
                userID);

        if (initCookie.getValue().equalsIgnoreCase(CookieProtocol._none)) {

            userSession.setInitCookieDone(false);

        } else if (initCookie.getValue().equalsIgnoreCase(CookieProtocol._perGroup)) {

            PortletDescription portletDescription = null;
            try {

                portletDescription = producer.getPortletDescription(getPortlet().getParent());

            } catch (WSRPException e) {

                // do nothing since exception has been logged already

            }

            String groupID = null;
            if (portletDescription != null) {
                groupID = portletDescription.getGroupID();
            }

            if (groupID != null) {
                GroupSessionMgr groupSession = userSession.getGroupSession(groupID);
                groupSession.setInitCookieDone(false);
            }
        }
    }

    protected void checkInitCookie(String userID) throws WSRPException {
        UserSessionMgr userSession =
            this.consumerEnv.getSessionHandler().getUserSession(
                getPortlet().getPortletKey().getProducerId(),
                userID);

        if (initCookie.getValue().equalsIgnoreCase(CookieProtocol._perUser)) {

            this.markupPort = userSession.getWSRPBaseService();

            if (!userSession.isInitCookieDone()) {

                userSession.setInitCookieRequired(true);
                initCookie();
                userSession.setInitCookieDone(true);
            }

        } else if (initCookie.getValue().equalsIgnoreCase(CookieProtocol._perGroup)) {

            PortletDescription portletDescription = producer.getPortletDescription(getPortlet().getParent());
            String groupID = null;
            if (portletDescription != null) {
                groupID = portletDescription.getGroupID();
            }

            if (groupID != null) {
                GroupSessionMgr groupSession = userSession.getGroupSession(groupID);

                this.markupPort = groupSession.getWSRPBaseService();

                if (!groupSession.isInitCookieDone()) {
                    groupSession.setInitCookieRequired(true);
                    initCookie();
                    groupSession.setInitCookieDone(true);
                }

            } else {
                // means either we have no service description from the producer containg the portlet
                // or the producer specified initCookieRequired perGroup but didn't provide
                // a groupID in the portlet description                             
            }
        } else {
            this.markupPort = userSession.getWSRPBaseService();
        }
        
    }

    protected MarkupParams getMarkupParams(WSRPBaseRequest request) {

        MarkupParams markupParams = new MarkupParams();
        ClientData clientData = null;

        // lets just set this to the consumer agent for now
        if (producer.getRegistrationData() != null) {
            clientData = new ClientData();
            clientData.setUserAgent(producer.getRegistrationData().getConsumerAgent());
        }
        markupParams.setClientData(clientData);
        markupParams.setSecureClientCommunication(this.service.getLinkService().isSecure());
        markupParams.setLocales(consumerEnv.getSupportedLocales());

        markupParams.setMimeTypes(consumerEnv.getMimeTypes());

        markupParams.setMode(request.getMode());
        markupParams.setWindowState(request.getWindowState());
        markupParams.setNavigationalState(request.getNavigationalState());
        markupParams.setMarkupCharacterSets(consumerEnv.getCharacterEncodingSet());
        markupParams.setValidateTag(null); // TODO ValidateTag

        // TODO we could cache the modes and window states
        final MarkupType markupType = this.desc.getMarkupTypes(0);
        markupParams.setValidNewModes(this.getValidValues(consumerEnv.getSupportedModes(), markupType.getModes()));
        markupParams.setValidNewWindowStates(this.getValidValues(consumerEnv.getSupportedWindowStates(), markupType.getWindowStates()));

        markupParams.setExtensions(this.getMarkupParamsExtensions());

        return markupParams;
    }

    /**
     * Calculate the valid values for modes and window states.
     * @param supported The values supported by the consumer.
     * @param allowed The values allowed by the producer.
     * @return The valid values.
     */
    protected String[] getValidValues(String[] supported, String[] allowed) {
        String[] validModes = new String[0];
        for(int i=0; i < supported.length; i++ ) {
            if ( ArrayUtils.contains(allowed, supported[i]) ) {
                String[] newValidModes = new String[validModes.length+1];
                System.arraycopy(validModes, 0, newValidModes, 0, validModes.length);
                newValidModes[validModes.length] = supported[i];
                validModes = newValidModes;
            }
        }
        return validModes;
    }

    /**
     * Create the runtime context for the current request.
     */
    protected RuntimeContext getRuntimeContext(WSRPBaseRequest request) {
        RuntimeContext runtimeContext = new RuntimeContext();
        runtimeContext.setUserAuthentication(consumerEnv.getUserAuthentication());
        runtimeContext.setPortletInstanceKey(request.getPortletInstanceKey());

        URLTemplateComposer templateComposer = consumerEnv.getTemplateComposer();
        if (templateComposer != null) {
            runtimeContext.setNamespacePrefix(templateComposer.getNamespacePrefix());
        }

        Boolean doesUrlTemplateProcess = null;
        try {

            PortletDescription desc = producer.getPortletDescription(getPortlet().getParent());

            if (desc != null) {

                doesUrlTemplateProcess = desc.getDoesUrlTemplateProcessing();
            }

        } catch (WSRPException e) {

            // do nothing since exception has been logged already
            // continue with assumption that portlet does not support template processing
        }

        if (doesUrlTemplateProcess != null && templateComposer != null && doesUrlTemplateProcess.booleanValue()) {
            Templates templates = new Templates();
            templates.setBlockingActionTemplate(templateComposer.createBlockingActionTemplate(true, true, true, true));
            templates.setRenderTemplate(templateComposer.createRenderTemplate(true, true, true, true));
            templates.setDefaultTemplate(templateComposer.createDefaultTemplate(true, true, true, true));
            templates.setResourceTemplate(templateComposer.createResourceTemplate(true, true, true, true));
            templates.setSecureBlockingActionTemplate(
                templateComposer.createSecureBlockingActionTemplate(true, true, true, true));
            templates.setSecureRenderTemplate(templateComposer.createSecureRenderTemplate(true, true, true, true));
            templates.setSecureDefaultTemplate(templateComposer.createSecureDefaultTemplate(true, true, true, true));
            templates.setSecureResourceTemplate(templateComposer.createSecureResourceTemplate(true, true, true, true));
            runtimeContext.setTemplates(templates);
        }

        runtimeContext.setSessionID(request.getSessionID());
        runtimeContext.setExtensions(null);

        return runtimeContext;
    }

    protected UserContext getUserContext(String userID) {
        UserContext userContext = null;

        if (userID != null) {
            User user = consumerEnv.getUserRegistry().getUser(userID);

            if (user != null) {
                userContext = user.getUserContext();
            }
        }
        
        // workaround for Oracle bug, always send a userContext with dummy value
        // if none was provided
        
        if (userContext == null) {
        	userContext = new UserContext();
        	userContext.setUserContextKey("dummyUserContextKey");
        }

        return userContext;
    }

    protected InteractionParams getInteractionParams(InteractionRequest actionRequest) {
        InteractionParams interactionParams = new InteractionParams();

        interactionParams.setPortletStateChange(consumerEnv.getPortletStateChange());
        
        // access POPs with cloneBeforeWrite
        // however keep the default behaviour from ConsEnv
        // this means that if readWrite is set and we access a POP then set to cloneBeforeWrite
        if (!portlet.isConsumerConfigured() &&
             interactionParams.getPortletStateChange().toString().equalsIgnoreCase(StateChange._readWrite)) {
             	interactionParams.setPortletStateChange(StateChange.cloneBeforeWrite);
       	}
        
        interactionParams.setInteractionState(actionRequest.getInteractionState());
        interactionParams.setFormParameters(actionRequest.getFormParameters());
        interactionParams.setUploadContexts(null);
        interactionParams.setExtensions(null);

        return interactionParams;
    }

    /**
      * This method is used to retrieve the markup generated by the portlet instance.
      * 
      * @param markupRequest 
      * @param userID
      * @return The markup response generated by the portlet
      **/
    public MarkupResponse getMarkup(MarkupRequest markupRequest,
                                    String userID)
    throws WSRPException {
        checkInitCookie(userID);

        MarkupResponse response = null;

        try {

            MarkupContext markupContext = null;
            if ((markupContext = markupRequest.getCachedMarkup()) == null) {

                // getMarkup request
                GetMarkup request = new GetMarkup();

                request.setPortletContext(getPortlet().getPortletContext());
                request.setMarkupParams(getMarkupParams(markupRequest));
                request.setRuntimeContext(getRuntimeContext(markupRequest));

                RegistrationContext regCtx = producer.getRegistrationContext();
                if (regCtx != null)
                    request.setRegistrationContext(regCtx);

                UserContext userCtx = getUserContext(userID);
                if (userCtx != null) {
                    request.setUserContext(getUserContext(userID));
                }

                response = markupPort.getMarkup(request);

                parameterChecker.check(response);

            } else {

                response = new MarkupResponse();
                response.setMarkupContext(markupContext);
            }            
            
            Boolean requiresRewriting = response.getMarkupContext().getRequiresUrlRewriting();
            requiresRewriting = requiresRewriting == null ? Boolean.FALSE : requiresRewriting;

            if (requiresRewriting.booleanValue()) {
                // rewrite url's

                URLRewriter urlRewriter = consumerEnv.getURLRewriter();
                String rewrittenMarkup = urlRewriter.rewriteURLs(response.getMarkupContext().getMarkupString());

                if (rewrittenMarkup != null) {
                    response.getMarkupContext().setMarkupString(rewrittenMarkup);
                }
            }

        } catch (InvalidCookieFault cookieFault) {

            // lets reset the init cookie settings
            resetInitCookie(userID);

            // and try it again
            response = getMarkup(markupRequest, userID);

        } catch (java.rmi.RemoteException wsrpFault) {

            WSRPXHelper.handleWSRPFault(this.getWsrp4jLogger(), wsrpFault);

        }

        return response;
    }

    protected Logger getWsrp4jLogger() {
        if ( this.logger == null ) {
            this.logger = new WSRPLogger(this.getLogger());
        }
        return this.logger;
    }

    /**
     * This method is used to perform a blocking interaction on the portlet instance.
     * 
     * @param actionRequest 
     **/
    public BlockingInteractionResponse performBlockingInteraction(InteractionRequest actionRequest,
                                                                  String userID)
    throws WSRPException {

        checkInitCookie(userID);

        BlockingInteractionResponse response = null;

        try {
            PerformBlockingInteraction request = new PerformBlockingInteraction();

            request.setPortletContext(getPortlet().getPortletContext());
            request.setInteractionParams(getInteractionParams(actionRequest));
            request.setMarkupParams(getMarkupParams(actionRequest));
            request.setRuntimeContext(getRuntimeContext(actionRequest));

            RegistrationContext regCtx = producer.getRegistrationContext();
            if (regCtx != null)
                request.setRegistrationContext(regCtx);

            UserContext userCtx = getUserContext(userID);
            if (userCtx != null)
                request.setUserContext(userCtx);

            response = markupPort.performBlockingInteraction(request);

            parameterChecker.check(response);

        } catch (InvalidCookieFault cookieFault) {

            // lets reset the init cookie settings
            resetInitCookie(userID);

            // and try it again
            response = performBlockingInteraction(actionRequest, userID);

        } catch (java.rmi.RemoteException wsrpFault) {

            WSRPXHelper.handleWSRPFault(this.getWsrp4jLogger(), wsrpFault);
        }

        return response;
    }

    /**
     * Clone the portlet
     * 
     * @return The new portlet context
     **/
    public PortletContext clonePortlet(String userID) throws WSRPException {
        ClonePortlet request = new ClonePortlet();

        request.setPortletContext(getPortlet().getPortletContext());

        RegistrationContext regCtx = producer.getRegistrationContext();
        if (regCtx != null)
            request.setRegistrationContext(regCtx);

        UserContext userCtx = getUserContext(userID);
        if (userCtx != null)
            request.setUserContext(userCtx);

        PortletContext response = null;

        try {

            response = portletPort.clonePortlet(request);
            parameterChecker.check(response, Constants.NILLABLE_FALSE);

        } catch (java.rmi.RemoteException wsrpFault) {

            WSRPXHelper.handleWSRPFault(this.getWsrp4jLogger(), wsrpFault);
        }

        return response;
    }

    /**
     * Destroy the producer portlets specified in the entiyHandles array.    
     **/
    public DestroyPortletsResponse destroyPortlets(String[] portletHandles,
                                                   String userID)
    throws WSRPException {
        DestroyPortlets request = new DestroyPortlets();

        RegistrationContext regCtx = producer.getRegistrationContext();
        if (regCtx != null)
            request.setRegistrationContext(regCtx);

        request.setPortletHandles(portletHandles);

        DestroyPortletsResponse response = null;
        try {

            response = portletPort.destroyPortlets(request);
            parameterChecker.check(response);

        } catch (java.rmi.RemoteException wsrpFault) {

            WSRPXHelper.handleWSRPFault(this.getWsrp4jLogger(), wsrpFault);
        }

        return response;
    }

    /**
     * Inform the producer that the sessions specified in the sessionIDs array
     * will no longer be used by the consumer and can therefor be released.     
     **/
    public ReturnAny releaseSessions(String[] sessionIDs,
                                     String userID)
    throws WSRPException {

        checkInitCookie(userID);

        ReleaseSessions request = new ReleaseSessions();

        RegistrationContext regCtx = producer.getRegistrationContext();
        if (regCtx != null)
            request.setRegistrationContext(regCtx);

        request.setSessionIDs(sessionIDs);

        ReturnAny response = null;
        try {

            response = markupPort.releaseSessions(request);

        } catch (java.rmi.RemoteException wsrpFault) {

            WSRPXHelper.handleWSRPFault(this.getWsrp4jLogger(), wsrpFault);
        }

        return response;
    }

    /**
     * Actual WSRP initCookie() call
     **/
    public void initCookie() throws WSRPException {
        InitCookie request = new InitCookie();

        RegistrationContext regCtx = producer.getRegistrationContext();
        if (regCtx != null)
            request.setRegistrationContext(regCtx);

        try {

            markupPort.initCookie(request);

        } catch (java.rmi.RemoteException wsrpFault) {
            WSRPXHelper.handleWSRPFault(this.getWsrp4jLogger(), wsrpFault);
        }
    }

    /**
     * Fetches information about the portlet from the producer. 
     * 
     * @param userID is used to get the user context of the user from the user registry
     * @param desiredLocales Array of locales the description should be provided
     * 
     * @return The response to the getPortletDescription call.
     **/
    public PortletDescriptionResponse getPortletDescription(String userID,
                                                            String[] desiredLocales)
    throws WSRPException {
        GetPortletDescription request = new GetPortletDescription();

        RegistrationContext regCtx = producer.getRegistrationContext();
        if (regCtx != null)
            request.setRegistrationContext(regCtx);

        request.setPortletContext(getPortlet().getPortletContext());

        UserContext userCtx = getUserContext(userID);
        if (userCtx != null)
            request.setUserContext(userCtx);

        request.setDesiredLocales(desiredLocales);

        PortletDescriptionResponse response = null;

        try {

            response = portletPort.getPortletDescription(request);
            parameterChecker.check(response);

        } catch (java.rmi.RemoteException wsrpFault) {

            WSRPXHelper.handleWSRPFault(this.getWsrp4jLogger(), wsrpFault);
        }

        return response;
    }

    /**
     * Fetches all published properties of an remote portlet.
     * 
     * @param userID The ID of the user this request is done for
     * 
     * @return The portlet property description response from the producer
     **/
    public PortletPropertyDescriptionResponse getPortletPropertyDescription(String userID)
    throws WSRPException {
        GetPortletPropertyDescription request = new GetPortletPropertyDescription();
        request.setPortletContext(getPortlet().getPortletContext());

        RegistrationContext regCtx = producer.getRegistrationContext();
        if (regCtx != null)
            request.setRegistrationContext(regCtx);

        UserContext userCtx = getUserContext(userID);
        if (userCtx != null)
            request.setUserContext(userCtx);

        request.setDesiredLocales(consumerEnv.getSupportedLocales());

        PortletPropertyDescriptionResponse response = null;

        try {

            response = portletPort.getPortletPropertyDescription(request);
            parameterChecker.check(response);

        } catch (java.rmi.RemoteException wsrpFault) {

            WSRPXHelper.handleWSRPFault(this.getWsrp4jLogger(), wsrpFault);
        }

        return response;
    }

    /**
     * Get the current values of the properties with the given names.
     * 
     * @param names The names of the properties
     * @param userID The ID of the user is used to get the user context
     * 
     * @return A list of properties containing the values and names of the properties.
     **/
    public PropertyList getPortletProperties(String[] names, String userID)
    throws WSRPException {
        GetPortletProperties request = new GetPortletProperties();
        request.setPortletContext(getPortlet().getPortletContext());
        request.setNames(names);

        RegistrationContext regCtx = producer.getRegistrationContext();
        if (regCtx != null)
            request.setRegistrationContext(regCtx);

        UserContext userCtx = getUserContext(userID);
        if (userCtx != null)
            request.setUserContext(userCtx);

        PropertyList response = null;

        try {

            response = portletPort.getPortletProperties(request);
            parameterChecker.check(response, Constants.NILLABLE_FALSE);

        } catch (java.rmi.RemoteException wsrpFault) {

            WSRPXHelper.handleWSRPFault(this.getWsrp4jLogger(), wsrpFault);
        }

        return response;
    }

    /**
     * Set the portlet properties specified in the property list
     * 
     * @param properties List of properties to be set.
     * @param userID The ID of the user is used to get the user context
     **/
    public PortletContext setPortletProperties(PropertyList properties, String userID)
    throws WSRPException {
        SetPortletProperties request = new SetPortletProperties();
        request.setPortletContext(getPortlet().getPortletContext());

        RegistrationContext regCtx = producer.getRegistrationContext();
        if (regCtx != null)
            request.setRegistrationContext(regCtx);

        UserContext userCtx = getUserContext(userID);
        if (userCtx != null)
            request.setUserContext(userCtx);
        request.setPropertyList(properties);

        PortletContext response = null;

        try {

            response = portletPort.setPortletProperties(request);
            parameterChecker.check(response, Constants.NILLABLE_FALSE);

        } catch (java.rmi.RemoteException wsrpFault) {

            WSRPXHelper.handleWSRPFault(this.getWsrp4jLogger(), wsrpFault);
        }

        return response;
    }

    /**
     * Get the extensions for the MarkupContext.
     */
    protected Extension[] getMarkupParamsExtensions() {
        // this can be overwritten in sub classes
        return null;
    }
}
