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
package org.apache.cocoon.portal.wsrp.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import oasis.names.tc.wsrp.v1.types.BlockingInteractionResponse;
import oasis.names.tc.wsrp.v1.types.LocalizedString;
import oasis.names.tc.wsrp.v1.types.MarkupContext;
import oasis.names.tc.wsrp.v1.types.MarkupResponse;
import oasis.names.tc.wsrp.v1.types.MarkupType;
import oasis.names.tc.wsrp.v1.types.PortletContext;
import oasis.names.tc.wsrp.v1.types.PortletDescription;
import oasis.names.tc.wsrp.v1.types.SessionContext;
import oasis.names.tc.wsrp.v1.types.UpdateResponse;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.serializers.util.EncodingSerializer;
import org.apache.cocoon.environment.wrapper.RequestParameters;
import org.apache.cocoon.portal.coplet.adapter.impl.AbstractCopletAdapter;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.coplet.CopletInstanceSizingEvent;
import org.apache.cocoon.portal.om.CopletDecorationProvider;
import org.apache.cocoon.portal.om.CopletDefinition;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.DecorationAction;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutFeatures;
import org.apache.cocoon.portal.services.aspects.DynamicAspect;
import org.apache.cocoon.portal.services.aspects.RequestProcessorAspect;
import org.apache.cocoon.portal.services.aspects.RequestProcessorAspectContext;
import org.apache.cocoon.portal.util.HtmlSaxParser;
import org.apache.cocoon.portal.wsrp.consumer.ConsumerEnvironmentImpl;
import org.apache.cocoon.portal.wsrp.consumer.ProducerDescription;
import org.apache.cocoon.portal.wsrp.consumer.ProducerRegistryImpl;
import org.apache.cocoon.portal.wsrp.consumer.Request;
import org.apache.cocoon.portal.wsrp.consumer.RequestImpl;
import org.apache.cocoon.portal.wsrp.consumer.RequiresConsumerEnvironment;
import org.apache.cocoon.portal.wsrp.consumer.RequiresPortalService;
import org.apache.cocoon.portal.wsrp.consumer.RequiresWSRPAdapter;
import org.apache.cocoon.portal.wsrp.consumer.SimplePortletWindowSession;
import org.apache.cocoon.portal.wsrp.consumer.UserContextProvider;
import org.apache.cocoon.portal.wsrp.consumer.WSRPRequestImpl;
import org.apache.cocoon.portal.wsrp.logging.WSRPLogManager;
import org.apache.cocoon.portal.wsrp.logging.WSRPLogger;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.wsrp4j.consumer.GroupSession;
import org.apache.wsrp4j.consumer.PortletDriver;
import org.apache.wsrp4j.consumer.PortletKey;
import org.apache.wsrp4j.consumer.PortletSession;
import org.apache.wsrp4j.consumer.Producer;
import org.apache.wsrp4j.consumer.SessionHandler;
import org.apache.wsrp4j.consumer.URLGenerator;
import org.apache.wsrp4j.consumer.User;
import org.apache.wsrp4j.consumer.UserSession;
import org.apache.wsrp4j.consumer.WSRPPortlet;
import org.apache.wsrp4j.consumer.driver.PortletKeyImpl;
import org.apache.wsrp4j.consumer.driver.UserImpl;
import org.apache.wsrp4j.consumer.driver.WSRPPortletImpl;
import org.apache.wsrp4j.exception.ErrorCodes;
import org.apache.wsrp4j.exception.WSRPException;
import org.apache.wsrp4j.log.LogManager;
import org.apache.wsrp4j.util.Constants;
import org.apache.wsrp4j.util.Modes;
import org.apache.wsrp4j.util.WindowStates;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * Adapter to use wsrp-portlets as coplets <br/>
 * It provides the wsrp support by initializing wsrp4j <br/>
 * The aspect/adapter can be configured at the portal manager.<br/>
 *
 * @version $Id$
 */
public class WSRPAdapter
    extends AbstractCopletAdapter
    implements CopletDecorationProvider, Receiver, RequestProcessorAspect, DynamicAspect {

    /** Key to store the consumer map into the coplet instance data object as a temporary attribute. */
    public static final String ATTRIBUTE_NAME_CONSUMER_MAP = "wsrp-consumer-map";

    /** Key to store the portlet instance key into the coplet instance data object as a temporary attribute. */
    public static final String ATTRIBUTE_NAME_PORTLET_INSTANCE_KEY = "wsrp-portlet-instance-key";

    /** Key to store the portlet key into the coplet instance data objectt as a temporary attribute. */
    public static final String ATTRIBUTE_NAME_PORTLET_KEY = "wsrp-portletkey";

    /** Key to store the wsrp user into the coplet instance data object as a temporary attribute. */
    public static final String ATTRIBUTE_NAME_USER = "wsrp-user";

    /** Key to store the wsrp title into the coplet instance data object as a temporary attribute. */
    public static final String ATTRIBUTE_NAME_PORTLET_TITLE = "wsrp-title";

    /** Key to store the wsrp modes into the coplet instance data object as a temporary attribute. */
    public static final String ATTRIBUTE_NAME_PORTLET_MODES = "wsrp-modes";

    /** Key to store the window states into the coplet instance data object as a temporary attribute. */
    public static final String ATTRIBUTE_NAME_PORTLET_WINDOWSTATES = "wsrp-window-states";

    /** Unique name of the consumer. */
    public static final String CONSUMER_URL = "http://cocoon.apache.org/portal/wsrp-consumer";

    /** Name of the service. */
    public static final String consumerAgent = "Apache Cocoon Portal." + org.apache.cocoon.Constants.VERSION;

    public static final String REQUEST_PARAMETER_NAME = "cocoon-wsrpevent";

    /** The consumer environment implementation. */
    protected ConsumerEnvironmentImpl consumerEnvironment;

    /** Stores the current coplet instance data per thread. */
    protected final ThreadLocal copletInstance = new ThreadLocal();

    /** The user context provider. */
    protected UserContextProvider userContextProvider;

    /** Location of the wsrp configuration. */
    protected String wsrpConfigLocation = "config/wsrp-config.xml";

    /** Initialized? */
    protected boolean initialized = false;

    /** The wsrp configuration. */
    protected Configuration wsrpConfiguration;

    protected SourceResolver sourceResolver;

    protected Properties properties;

    protected ServiceManager manager;

    public void setSourceResolver(SourceResolver sourceResolver) {
        this.sourceResolver = sourceResolver;
    }

    public void setUserContextProvider(UserContextProvider userContextProvider) {
        this.userContextProvider = userContextProvider;
    }

    public void setConfigLocation(String location) {
        this.wsrpConfigLocation = location;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setServiceManager(ServiceManager manager) {
        this.manager = manager;
    }

    /**
     * Sets the <tt>WSRPLogger</tt>, the <tt>producerConfig</tt> and the <tt>consumerEnvironment</tt><br/>
     *
     */
    public void init() throws Exception {
        LogManager.setLogManager(new WSRPLogManager(new WSRPLogger(this.getLogger())));
        this.consumerEnvironment = new ConsumerEnvironmentImpl();
        this.consumerEnvironment.init(this.portalService,
                                      this);
        this.consumerEnvironment.setConsumerAgent(consumerAgent);
    }

    /**
     * Removes all portlets, producers and users out of the <tt>consumerEnvironment</tt>-registries<br/>
     *
     */
    public void destroy() {
        if ( this.consumerEnvironment != null ) {
            this.consumerEnvironment.getPortletRegistry().removeAllPortlets();

            this.consumerEnvironment.getProducerRegistry().removeAllProducers();

            this.consumerEnvironment.getUserRegistry().removeAllUsers();
        }
        try {
            ContainerUtil.dispose(this.consumerEnvironment);
            this.consumerEnvironment = null;
        } catch (Throwable t) {
            this.getLogger().error("Destruction failed!", t);
        }
    }

    /**
     * Gets the required information of the producer, user, wsrp-portlet, window-states, window-modes<br/>
     * and stores its into the copletInstanceData<br/>
     * After that it initiates the <tt>getServiceDescription()</tt>-call<br/>
     *
     * @see org.apache.cocoon.portal.coplet.adapter.impl.AbstractCopletAdapter#login(org.apache.cocoon.portal.om.CopletInstance)
     */
    public void login(CopletInstance coplet) {
        super.login(coplet);

        final CopletDefinition copletData = coplet.getCopletDefinition();

        // get the producer
        final String producerId = (String) copletData.getAttribute("producer-id");
        if ( producerId == null ) {
            // if the producer can't be found, we simply return
            this.getLogger().error("Producer not configured in wsrp coplet " + copletData.getId());
            return;
        }
        final Producer producer = consumerEnvironment.getProducerRegistry().getProducer(producerId);
        if ( producer == null ) {
            // if the producer can't be found, we simply return
            this.getLogger().error("Producer with id " + producerId + " not found.");
            return;
        }

        // get the wsrp user and store it as an attribute on the instance
        final String currentUserID = this.portalService.getUserService().getUser().getUserName();
        User user = this.consumerEnvironment.getUserRegistry().getUser(currentUserID);
        if ( user == null ) {
            // create a new user
            user = new UserImpl(currentUserID);
            user.setUserContext(this.userContextProvider.createUserContext(currentUserID));
            this.consumerEnvironment.getUserRegistry().addUser(user);
        }
        coplet.setTemporaryAttribute(ATTRIBUTE_NAME_USER, user);

        // get the portlet handle
        final String portletHandle = (String) copletData.getAttribute("portlet-handle");
        if ( portletHandle == null ) {
            // if the portlet handle can't be found, we simply return
            this.getLogger().error("Portlet handle not configured in wsrp coplet " + copletData.getId());
            return;
        }

        // get the wsrp portlet
        final PortletKey portletKey = new PortletKeyImpl(portletHandle, producerId);
        WSRPPortlet wsrpportlet = this.consumerEnvironment.getPortletRegistry().getPortlet(portletKey);
        if ( wsrpportlet == null ) {
            wsrpportlet = new WSRPPortletImpl(portletKey);
            final PortletContext portletContext = new PortletContext(null, portletKey.getPortletHandle(), null);
            wsrpportlet.setPortletContext(portletContext);
            try {
                consumerEnvironment.getPortletRegistry().addPortlet(wsrpportlet);
            } catch (WSRPException we) {
                this.getLogger().error("Exception adding wsrp portlet.", we);
                return;
            }
        }
        coplet.setTemporaryAttribute(ATTRIBUTE_NAME_PORTLET_KEY, portletKey);
        final HttpSession session = this.portalService.getRequestContext().getRequest().getSession();
        final String portletInstanceKey = this.getPortletInstanceKey(portletKey, coplet, session.getId());
        coplet.setTemporaryAttribute(ATTRIBUTE_NAME_PORTLET_INSTANCE_KEY, portletInstanceKey);

        // create consumer parameters
        final Map addParams = new HashMap();
        addParams.put(Constants.PORTLET_INSTANCE_KEY, portletInstanceKey);
        coplet.setTemporaryAttribute(ATTRIBUTE_NAME_CONSUMER_MAP, addParams);

        // get the window-state and -mode
        wsrpportlet = consumerEnvironment.getPortletRegistry().getPortlet(portletKey);

        try {
            // this call includes the getServiceDescription()-Invocation
            // additionally register() initCookie() and so on will be handled
            // (within ProducerImpl and PortletDriverImpl)
            getSimplePortletWindowSession(wsrpportlet,
                                          portletInstanceKey,
                                          user);
            final PortletDescription desc = producer.getPortletDescription(portletHandle);
            coplet.setTemporaryAttribute(ATTRIBUTE_NAME_PORTLET_TITLE,
                                         desc.getTitle());
            final MarkupType markupType = desc.getMarkupTypes(0);
            coplet.setTemporaryAttribute(ATTRIBUTE_NAME_PORTLET_MODES, markupType.getModes());
            coplet.setTemporaryAttribute(ATTRIBUTE_NAME_PORTLET_WINDOWSTATES, markupType.getWindowStates());

        } catch (WSRPException e) {
            this.getLogger().error("Invoking getServiceDescription()", e);
        }
    }

	/**
     * Checks the values of the <tt>portlet-key</tt> and the <tt>user</tt> for current portlet-instance<br/>
     * After that all passed the <tt>getMarkup()</tt>-call will be initiated<br />
     *
	 * @see org.apache.cocoon.portal.coplet.adapter.impl.AbstractCopletAdapter#streamContent(org.apache.cocoon.portal.om.CopletInstance, org.xml.sax.ContentHandler)
	 */
	protected void streamContent(CopletInstance coplet, ContentHandler contentHandler)
    throws SAXException {
        try {
            // set the coplet in the thread local variable to give other components access to
            // the instance
            this.setCurrentCopletInstanceData(coplet);

            // get the portlet key and the user
            final PortletKey portletKey = (PortletKey)coplet.getTemporaryAttribute(ATTRIBUTE_NAME_PORTLET_KEY);
            if ( portletKey == null ) {
                throw new SAXException("WSRP configuration is missing: portlet key.");
            }
            final User user = (User)coplet.getTemporaryAttribute(ATTRIBUTE_NAME_USER);
            if ( user == null ) {
                throw new SAXException("WSRP configuration is missing: user.");
            }

            final String portletInstanceKey = (String)coplet.getTemporaryAttribute(ATTRIBUTE_NAME_PORTLET_INSTANCE_KEY);

            // getMarkup()
            final WSRPPortlet wsrpportlet = consumerEnvironment.getPortletRegistry().getPortlet(portletKey);

            SimplePortletWindowSession windowSession = getSimplePortletWindowSession(wsrpportlet, portletInstanceKey, user);
            final MarkupContext markupContext = this.getMarkupContext(wsrpportlet, windowSession, user);
            if ( markupContext == null || markupContext.getMarkupString() == null ) {
                throw new SAXException("No markup received from wsrp coplet " + coplet.getId());
            }
            final String content = markupContext.getMarkupString();

            final Boolean usePipeline;
            final boolean usesGet;
            // If the portlet uses the method get we always have to rewrite form elements
            final Producer producer = this.consumerEnvironment.getProducerRegistry().getProducer(portletKey.getProducerId());
            final PortletDescription desc = producer.getPortletDescription(portletKey.getPortletHandle());
            if ( desc.getUsesMethodGet() != null && desc.getUsesMethodGet().booleanValue() ) {
                usePipeline = Boolean.TRUE;
                usesGet = true;
            } else {
                usePipeline = (Boolean)this.getConfiguration(coplet, "use-pipeline", Boolean.FALSE);
                usesGet = false;
            }
            if ( usePipeline.booleanValue() ) {
                ContentHandler handler = contentHandler;
                if ( usesGet ) {
                    handler = new FormRewritingHandler(contentHandler);
                }
                HtmlSaxParser.parseString(content, HtmlSaxParser.getContentFilter(handler));
            } else {
                // stream out the include for the serializer
                EncodingSerializer.include(content, this.portalService.getRequestContext().getRequest(), contentHandler);
            }
        } catch (WSRPException e) {
            throw new SAXException("Exception during getMarkup of wsrp coplet: " + coplet.getId(), e);
        } catch (SAXException se) {
            throw se;
        } finally {
            this.setCurrentCopletInstanceData(null);
        }
	}

    /**
     * Releases all sessions (<tt>userSession, groupSession, portletSession</tt>)<br/>
     *
     * @see org.apache.cocoon.portal.coplet.adapter.impl.AbstractCopletAdapter#logout(org.apache.cocoon.portal.om.CopletInstance)
     */
    public void logout(CopletInstance coplet) {
    	super.logout(coplet);

        PortletKey portletKey = (PortletKey)coplet.getTemporaryAttribute(ATTRIBUTE_NAME_PORTLET_KEY);
        User user = (User)coplet.getTemporaryAttribute(ATTRIBUTE_NAME_USER);
        final String portletInstanceKey = (String)coplet.getTemporaryAttribute(ATTRIBUTE_NAME_PORTLET_INSTANCE_KEY);
        Producer producer = consumerEnvironment.getProducerRegistry().getProducer(portletKey.getProducerId());

        // releaseSession()
        try {
            UserSession userSession = consumerEnvironment.getSessionHandler().getUserSession(portletKey.getProducerId(), user.getUserID());
            PortletDescription portletDescription = producer.getPortletDescription(portletKey.getPortletHandle());
            GroupSession groupSession = userSession.getGroupSession(portletDescription.getGroupID());
            PortletSession portletSession = groupSession.getPortletSession(portletInstanceKey);
            SessionContext sessionContext = portletSession.getSessionContext();
            WSRPPortlet wsrpportlet = consumerEnvironment.getPortletRegistry().getPortlet(portletKey);
            PortletDriver portletDriver = consumerEnvironment.getPortletDriverRegistry().getPortletDriver(wsrpportlet);

            if (sessionContext != null) {
                String[] sessions = new String[1];
                sessions[0] = new String (sessionContext.getSessionID());
                portletDriver.releaseSessions(sessions, user.getUserID());
            }

            userSession.removeGroupSession(portletDescription.getGroupID());
        } catch (WSRPException e) {
            this.getLogger().error("session deregister()", e);
        }
    }

    /**
     * After getting the <tt>userSession</tt> and <tt>groupSession</tt> it performs the <tt>getServiceDescription()</tt>-call<br/>
     *
     * @param portlet
     * @param portletInstanceKey
     * @param user
     * @return SimplePortletWindowSession
     * @throws WSRPException
     */
    public SimplePortletWindowSession getSimplePortletWindowSession(WSRPPortlet portlet,
                                                                    String portletInstanceKey,
                                                                    User user)
    throws WSRPException {
        SimplePortletWindowSession windowSession = null;

        // get the user session
        SessionHandler sessionHandler = consumerEnvironment.getSessionHandler();
        UserSession userSession = sessionHandler.getUserSession(portlet.getPortletKey().getProducerId(), user.getUserID());

        if (userSession != null) {
            // get the group session
            String groupID = null;
            try {
                Producer producer = consumerEnvironment.getProducerRegistry().getProducer(portlet.getPortletKey().getProducerId());
                // *********************************
                // getServiceDescription()
                // *********************************
                PortletDescription portletDescription = producer.getPortletDescription(portlet.getPortletKey().getPortletHandle());

                if (portletDescription != null) {
                    groupID = portletDescription.getGroupID();
                }
            } catch (WSRPException e) {
                groupID = "default_group_id";
                this.getLogger().info("using default-group");
            }

            GroupSession groupSession = userSession.getGroupSession(groupID);

            if (groupSession != null) {
                PortletSession portletSession = groupSession.getPortletSession(portlet.getPortletKey().getPortletHandle());

                if (portletSession != null) {
                    windowSession = (SimplePortletWindowSession) portletSession.getPortletWindowSession(portletInstanceKey);
                } else {
                    this.getLogger().error("WSRP-Errorcode: " + Integer.toString(ErrorCodes.PORTLET_SESSION_NOT_FOUND));
                }
            } else {
                this.getLogger().error("WSRP-Errorcode: " + Integer.toString(ErrorCodes.GROUP_SESSION_NOT_FOUND));
            }
        } else {
            this.getLogger().error("WSRP-Errorcode: " + Integer.toString(ErrorCodes.USER_SESSION_NOT_FOUND));
        }
        return windowSession;
    }

    /**
     * Performs an blocking interaction with the given portlet and session.<br/>
     * If the response to this call is a redirect URL's it won't be followed.<br/>
     *
     * An optionally returned markup context is store in the window session<br/>
     * and should be processed by the portlet driver instead of making a new<br/>
     * getMarkup() call.<br/>
     *
     * @param portlet The portlet on which this action should be performed
     * @param windowSession The window session of the portlet on which the action should
     *        be performed
     * @param user The user on which this action should be performed
     * @param request The request with all required information for the call
     **/
    protected void performBlockingInteraction(WSRPPortlet portlet,
                                              SimplePortletWindowSession windowSession,
                                              User user,
                                              Request request) {
        try {
            PortletDriver portletDriver = consumerEnvironment.getPortletDriverRegistry().getPortletDriver(portlet);
            BlockingInteractionResponse response = portletDriver.performBlockingInteraction(
                    new WSRPRequestImpl(windowSession, request, consumerEnvironment), user.getUserID());

            if (response != null) {
                UpdateResponse update = response.getUpdateResponse();
                if (update != null) {
                    //update the WSRP portlet sessionContext
                    windowSession.getPortletSession().setSessionContext(update.getSessionContext());

                    MarkupContext markupContext = update.getMarkupContext();
                    if (markupContext != null) {
                        windowSession.updateMarkupCache(markupContext);
                    }

                    windowSession.setNavigationalState(update.getNavigationalState());

                    String windowState = null;
                    if ((windowState = update.getNewWindowState()) != null) {
                        windowSession.setWindowState(windowState);
                    }

                    String windowMode = null;
                    if ((windowMode = update.getNewMode()) != null) {
                        windowSession.setMode(windowMode);
                    }
                } else if (response.getRedirectURL() != null) {
                    this.getLogger().debug("response.getRedirectURL() != null");
                }
            }
        } catch (WSRPException e) {
            this.getLogger().error("Error occured during performInteraction!", e);
        }
    }

    /**
     * Retrieves the markup generated by the portlet.
     *
     * @param portlet
     * @param windowSession
     * @param user
     * @return The markup context.
     **/
    protected MarkupContext getMarkupContext(WSRPPortlet portlet,
                                             SimplePortletWindowSession windowSession,
                                             User user)
    throws WSRPException {
        WSRPRequestImpl wsrpRequest = new WSRPRequestImpl(windowSession, null, this.consumerEnvironment);

        PortletDriver portletDriver = consumerEnvironment.getPortletDriverRegistry().getPortletDriver(portlet);
        MarkupResponse response = portletDriver.getMarkup(wsrpRequest, user.getUserID());

        if (response != null) {
            SessionContext sessionContext = response.getSessionContext();
            if (sessionContext != null && windowSession != null) {
                windowSession.getPortletSession().setSessionContext(sessionContext);
            }

            return response.getMarkupContext();
        }
        return null;
    }

    /**
     * Creates a <tt>String</tt> consists of the producer-id_portlet-handle_coplet-id_user-name <br/>
     *
     * @param key includes the essential values
     * @param coplet current <tt>CopletInstanceData</tt>-object
     * @param userName
     * @return the unique string which represents the portlet-instance
     * */
    protected String getPortletInstanceKey(PortletKey key,
                                           CopletInstance coplet,
                                           String userName) {
        final StringBuffer buffer = new StringBuffer(key.getProducerId());
        buffer.append('_').append(key.getPortletHandle()).append('_');
        buffer.append(coplet.getId()).append('_').append(userName);
        return buffer.toString();
    }

    /**
     * Gets all required information like <tt>portletKey, portletInstanceKey, userName, portletModes, windowStates,
     * interactionState</tt> and the <tt>navigationalStat</tt><br/>
     *
     * After that it decides with the <tt>URL_TYPE</tt> to perform the <tt>performBlockingInteraction()</tt>-call or
     * do some render- alternatively some resource-specific things<br/>
     *
     * @see Receiver
     */
    public void inform(WSRPEvent event) {
        final CopletInstance coplet = event.getTarget();
        this.setCurrentCopletInstanceData(coplet);

        try {
            PortletKey portletKey = (PortletKey)coplet.getTemporaryAttribute(ATTRIBUTE_NAME_PORTLET_KEY);
            final String portletInstanceKey = (String)coplet.getTemporaryAttribute(ATTRIBUTE_NAME_PORTLET_INSTANCE_KEY);
            WSRPPortlet wsrpPortlet = consumerEnvironment.getPortletRegistry().getPortlet(portletKey);
            User user = (User) coplet.getTemporaryAttribute(ATTRIBUTE_NAME_USER);

            final HttpServletRequest requestObject = this.portalService.getRequestContext().getRequest();

            Request request = new RequestImpl();
            String portletMode = requestObject.getParameter(Constants.PORTLET_MODE);
            String windowState = requestObject.getParameter(Constants.WINDOW_STATE);

            request.setInteractionState(requestObject.getParameter(Constants.INTERACTION_STATE));
            SimplePortletWindowSession windowSession = getSimplePortletWindowSession(wsrpPortlet, portletInstanceKey, user);
            windowSession.setNavigationalState(requestObject.getParameter(Constants.NAVIGATIONAL_STATE));

            if (portletMode != null) {
                windowSession.setMode(portletMode);
            }
            if (windowState != null) {
                if ( !windowState.equals(windowSession.getWindowState()) ) {

                    final Layout rootLayout = this.portalService.getProfileManager().getLayout(null);
                    final Layout layout = LayoutFeatures.searchLayout(this.portalService, coplet.getId(), rootLayout);
                    final Layout fullScreenLayout = LayoutFeatures.getFullScreenInfo(this.portalService);
                    if ( fullScreenLayout != null
                         && fullScreenLayout.equals( layout )
                         && !windowState.equals(WindowStates._maximized) ) {
                        Event e = new CopletInstanceSizingEvent( coplet, CopletInstance.SIZE_FULLSCREEN );
                        this.portalService.getEventManager().send(e);
                    }
                    if ( windowState.equals(WindowStates._minimized) ) {
                        Event e = new CopletInstanceSizingEvent(coplet, CopletInstance.SIZE_MINIMIZED);
                        this.portalService.getEventManager().send(e);
                    }
                    if ( windowState.equals(WindowStates._normal) ) {
                        Event e = new CopletInstanceSizingEvent(coplet, CopletInstance.SIZE_NORMAL);
                        this.portalService.getEventManager().send(e);
                    }
                    if ( windowState.equals(WindowStates._maximized) ) {
                        Event e = new CopletInstanceSizingEvent( coplet, CopletInstance.SIZE_FULLSCREEN );
                        this.portalService.getEventManager().send(e);
                    }
                    windowSession.setWindowState(windowState);
                }
            }
            if (requestObject.getParameter(Constants.URL_TYPE).equals(Constants.URL_TYPE_BLOCKINGACTION)) {
                // performBlockingInteraction()
                final Enumeration formParameter = requestObject.getParameterNames();
                while (formParameter.hasMoreElements()) {
                    final String parameter = (String) formParameter.nextElement();
                    request.addFormParameter(parameter, requestObject.getParameter(parameter));
                }
                performBlockingInteraction(wsrpPortlet, windowSession, user, request);

            }

        } catch (WSRPException e) {
            this.getLogger().error("Error during processing wsrp event.", e);
        } finally {
            this.setCurrentCopletInstanceData(null);
        }
    }

    /**
     * @return Returns the <tt>consumerEnvironmentImpl</tt>
     */
    public ConsumerEnvironmentImpl getConsumerEnvironment() {
        return consumerEnvironment;
    }

    /**
     * Add a new producer<br/>
     *
     * @param desc The producer description.
     * @return Returns true if the producer could be added.
     */
    public boolean addProducer(ProducerDescription desc) {
        return ((ProducerRegistryImpl)this.consumerEnvironment.getProducerRegistry()).addProducer(desc);
    }

    /**
     * This sets the current coplet instance data for the thread <br/>
     *
     * @param coplet The coplet instance data or null to clear the information.
     */
    public void setCurrentCopletInstanceData(CopletInstance coplet) {
        this.copletInstance.set(coplet);
    }

    /**
     * Return the current coplet instance data<br/>
     *
     * @return Returns the instance or null.
     */
    public CopletInstance getCurrentCopletInstanceData() {
        return (CopletInstance)this.copletInstance.get();
    }

    /**
     * This handler is triggered by a form element. It rewrites the
     * action if the method get is used. In this case all request parameters
     * of the action are added as hidden fields.
     */
    public static final class FormRewritingHandler extends AbstractXMLPipe {

        public FormRewritingHandler(ContentHandler ch) {
            this.setContentHandler(ch);
            if ( ch instanceof LexicalHandler ) {
                this.setLexicalHandler((LexicalHandler)ch);
            }
        }

        /**
         * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException {
            if ( loc.equalsIgnoreCase("form") && a.getValue("method").equalsIgnoreCase("get") ) {
                final String action = a.getValue("action");
                final int pos = action.indexOf('?');
                if ( pos != -1 ) {
                    final AttributesImpl ai = new AttributesImpl(a);
                    final String queryString = action.substring(pos+1);
                    ai.removeAttribute("action");
                    ai.addCDATAAttribute("action", action.substring(0, pos));
                    super.startElement(uri, loc, raw, ai);
                    final RequestParameters rp = new RequestParameters(queryString);

                    final Enumeration e = rp.getParameterNames();
                    while ( e.hasMoreElements() ) {
                        final String key = (String)e.nextElement();
                        final String value = rp.getParameter(key);
                        AttributesImpl attributes = new AttributesImpl();
                        attributes.addCDATAAttribute("type", "hidden");
                        attributes.addCDATAAttribute("name", key);
                        attributes.addCDATAAttribute("value", value);
                        XMLUtils.createElement(this.contentHandler, "input", attributes);
                    }
                    return;
                }
            }
            super.startElement(uri, loc, raw, a);
        }

    }

    /**
     * Check if we have read our configuration already.
     * If not, read the config and invoke the configure method.
     */
    protected void checkInitialized() {
        if ( !this.initialized ) {
            synchronized (this) {
                if (! this.initialized ) {
                    this.initialized = true;
                    Source source = null;
                    try {
                        source = this.sourceResolver.resolveURI(this.wsrpConfigLocation);
                        DefaultConfigurationBuilder dcb = new DefaultConfigurationBuilder();
                        this.wsrpConfiguration = dcb.build(source.getInputStream());
                    } catch (ConfigurationException ce) {
                        this.getLogger().error("Unable to read wsrp configuration: " + this.wsrpConfigLocation, ce);
                    } catch (IOException ioe) {
                        this.getLogger().error("Unable to read wsrp configuration: " + this.wsrpConfigLocation, ioe);
                    } catch (SAXException sae) {
                        this.getLogger().error("Unable to read wsrp configuration: " + this.wsrpConfigLocation, sae);
                    } finally {
                        this.sourceResolver.release(source);
                    }
                }
            }
        }
    }

    /**
     * Get the wsrp configuration.
     */
    public Configuration getWsrpConfiguration() {
        this.checkInitialized();
        return this.wsrpConfiguration;
    }

    /**
     * Get the adapter configuration.
     */
    public Properties getAdapterConfiguration() {
        return this.properties;
    }

    /**
     * Create a component.
     */
    public Object createObject(String className)
    throws Exception {
        final Object component = ClassUtils.newInstance(className);
        if ( component instanceof AbstractLogEnabled ) {
            ((AbstractLogEnabled)component).setLogger(this.getLogger());
        }
        if (component instanceof RequiresConsumerEnvironment) {
            ((RequiresConsumerEnvironment)component).setConsumerEnvironment(this.consumerEnvironment);
        }
        if (component instanceof RequiresWSRPAdapter) {
            ((RequiresWSRPAdapter)component).setWSRPAdapter(this);
        }
        if (component instanceof RequiresPortalService) {
            ((RequiresPortalService)component).setPortalService(this.portalService);
        }
        ContainerUtil.service(component, manager);
        ContainerUtil.initialize(component);

        return component;
    }

    /**
     * @see org.apache.cocoon.portal.om.CopletDecorationProvider#getPossibleCopletModes(CopletInstance)
     */
    public List getPossibleCopletModes(CopletInstance copletInstanceData) {
        final List modes = new ArrayList();
        final PortletKey portletKey = (PortletKey)copletInstanceData.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_PORTLET_KEY);

        if ( portletKey != null ) {
            final String portletInstanceKey = (String)copletInstanceData.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_PORTLET_INSTANCE_KEY);
            final User user = (User)copletInstanceData.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_USER);

            final WSRPPortlet portlet = this.consumerEnvironment.getPortletRegistry().getPortlet(portletKey);
            try {
                SimplePortletWindowSession windowSession = this.getSimplePortletWindowSession(portlet, portletInstanceKey, user);
                if (  windowSession != null ) {
                    this.setCurrentCopletInstanceData(copletInstanceData);
                    final URLGenerator urlGenerator = this.consumerEnvironment.getURLGenerator();

                    final String[] supportedModes = (String[])copletInstanceData.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_PORTLET_MODES);
                    String pm = windowSession.getMode();
                    if ( pm == null ) {
                        pm = Modes._view;
                    }
                    if ( !pm.equals(Modes._edit)
                         && ArrayUtils.contains(supportedModes, Modes._edit) ) {
                        final Map p = new HashMap();
                        p.put(Constants.URL_TYPE, Constants.URL_TYPE_RENDER);
                        p.put(Constants.PORTLET_MODE, Modes._edit);

                        final String link = urlGenerator.getRenderURL(p);
                        modes.add(new DecorationAction("edit-uri", link));
                    }
                    if ( !pm.equals(Modes._help)
                        && ArrayUtils.contains(supportedModes, Modes._help) ) {
                        final Map p = new HashMap();
                        p.put(Constants.URL_TYPE, Constants.URL_TYPE_RENDER);
                        p.put(Constants.PORTLET_MODE, Modes._help);

                        final String link = urlGenerator.getRenderURL(p);
                        modes.add(new DecorationAction("help-uri", link));
                    }
                    if ( !pm.equals(Modes._view)
                        && ArrayUtils.contains(supportedModes, Modes._view) ) {
                        final Map p = new HashMap();
                        p.put(Constants.URL_TYPE, Constants.URL_TYPE_RENDER);
                        p.put(Constants.PORTLET_MODE, Modes._view);

                        final String link = urlGenerator.getRenderURL(p);
                        modes.add(new DecorationAction("view-uri", link));
                    }
                }
            } catch (WSRPException ignore) {
                // we ignore this
            } finally {
                this.setCurrentCopletInstanceData(null);
            }
        }
        return modes;
    }

    /**
     * @see org.apache.cocoon.portal.om.CopletDecorationProvider#getPossibleWindowStates(CopletInstance)
     */
    public List getPossibleWindowStates(CopletInstance copletInstanceData) {
        final List states = new ArrayList();
        final PortletKey portletKey = (PortletKey)copletInstanceData.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_PORTLET_KEY);

        if ( portletKey != null ) {
            final String portletInstanceKey = (String)copletInstanceData.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_PORTLET_INSTANCE_KEY);
            final User user = (User)copletInstanceData.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_USER);

            final WSRPPortlet portlet = this.consumerEnvironment.getPortletRegistry().getPortlet(portletKey);
            try {
                SimplePortletWindowSession windowSession = this.getSimplePortletWindowSession(portlet, portletInstanceKey, user);
                if (  windowSession != null ) {
                    this.setCurrentCopletInstanceData(copletInstanceData);
                    final URLGenerator urlGenerator = this.consumerEnvironment.getURLGenerator();

                    final String[] supportedWindowStates = (String[])copletInstanceData.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_PORTLET_WINDOWSTATES);
                    String ws = windowSession.getWindowState();
                    if ( ws == null ) {
                        ws = WindowStates._normal;
                    }

                    if ( !ws.equals(WindowStates._minimized)
                         && ArrayUtils.contains(supportedWindowStates, WindowStates._minimized)) {
                        final Map p = new HashMap();
                        p.put(Constants.URL_TYPE, Constants.URL_TYPE_RENDER);
                        p.put(Constants.WINDOW_STATE, WindowStates._minimized);

                        final String link = urlGenerator.getRenderURL(p);
                        states.add(new DecorationAction(DecorationAction.WINDOW_STATE_MINIMIZED, link));
                    }
                    if ( !ws.equals(WindowStates._normal)
                          && ArrayUtils.contains(supportedWindowStates, WindowStates._normal)) {
                        final Map p = new HashMap();
                        p.put(Constants.URL_TYPE, Constants.URL_TYPE_RENDER);
                        p.put(Constants.WINDOW_STATE, WindowStates._normal);

                        final String link = urlGenerator.getRenderURL(p);
                        states.add(new DecorationAction(DecorationAction.WINDOW_STATE_NORMAL, link));
                    }
                    if ( !ws.equals(WindowStates._maximized)
                          && ArrayUtils.contains(supportedWindowStates, WindowStates._maximized)) {
                        final Map p = new HashMap();
                        p.put(Constants.URL_TYPE, Constants.URL_TYPE_RENDER);
                        p.put(Constants.WINDOW_STATE, WindowStates._maximized);

                        final String link = urlGenerator.getRenderURL(p);
                        states.add(new DecorationAction(DecorationAction.WINDOW_STATE_MAXIMIZED, link));
                    }
                }
            } catch (WSRPException ignore) {
                // we ignore this
            } finally {
                this.setCurrentCopletInstanceData(null);
            }
        }
        return states;
    }

    /**
     * @see org.apache.cocoon.portal.om.CopletDecorationProvider#getTitle(org.apache.cocoon.portal.om.CopletInstance)
     */
    public String getTitle(CopletInstance copletInstanceData) {
        String title = null;
        final PortletKey portletKey = (PortletKey)copletInstanceData.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_PORTLET_KEY);

        if ( portletKey != null ) {
            LocalizedString localizedTitle = (LocalizedString)copletInstanceData.getTemporaryAttribute(WSRPAdapter.ATTRIBUTE_NAME_PORTLET_TITLE);
            if ( localizedTitle != null ) {
                title = localizedTitle.getValue();
            }
        }
        if ( title == null ) {
            title = copletInstanceData.getTitle();
        }
        return title;
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.RequestProcessorAspect#process(org.apache.cocoon.portal.services.aspects.RequestProcessorAspectContext)
     */
    public void process(RequestProcessorAspectContext context) {
        final HttpServletRequest request = context.getPortalService().getRequestContext().getRequest();
        final String[] values = request.getParameterValues("cocoon-wsrpevent");
        if ( values != null && values.length == 1 ) {
            // create a wsrp event, first build map of parameters
            final Map parameters = new HashMap();
            final Enumeration parameterNames = request.getParameterNames();
            while ( parameterNames.hasMoreElements() ) {
                final String name = (String)parameterNames.nextElement();
                if ( !REQUEST_PARAMETER_NAME.equals(name) ) {
                    final String value = request.getParameter(name);
                    parameters.put(name, value);
                }
            }
            final String copletid = values[0];
            final CopletInstance cid = context.getPortalService().getProfileManager().getCopletInstance(copletid);

            final Event e = new WSRPEvent(cid, parameters);
            context.getPortalService().getEventManager().send(e);
        }
        context.invokeNext();
    }
}
