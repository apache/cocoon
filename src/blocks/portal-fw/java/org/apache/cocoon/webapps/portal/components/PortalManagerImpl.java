/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.webapps.portal.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.Recomposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.webapps.authentication.AuthenticationManager;
import org.apache.cocoon.webapps.authentication.user.RequestState;
import org.apache.cocoon.webapps.portal.PortalConstants;
import org.apache.cocoon.webapps.session.ContextManager;
import org.apache.cocoon.webapps.session.MediaManager;
import org.apache.cocoon.webapps.session.SessionManager;
import org.apache.cocoon.webapps.session.TransactionManager;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.apache.cocoon.webapps.session.xml.XMLUtil;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *  This is the basis portal component
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: PortalManagerImpl.java,v 1.5 2004/03/17 12:09:51 cziegeler Exp $
*/
public final class PortalManagerImpl
extends AbstractLogEnabled
implements Disposable, Composable, Recomposable, Recyclable, Contextualizable, Component, PortalManager {

    /** The cache (store) for the profiles */
    private Store   profileStore;

    /** The authenticationManager */
    private AuthenticationManager authenticationManager;

    /** The media manager */
    private MediaManager mediaManager;
    
    /** The XPath Processor */
    private XPathProcessor xpathProcessor;

    /** The session manager */
    private SessionManager     sessionManager;
    
    /** The Context manager */
    private ContextManager     contextManager;
    
    /** The transaction manager */
    private TransactionManager transactionManager;

    /** The component manager */
    protected ComponentManager manager;

    /** The current source resolver */
    protected SourceResolver resolver;

    /** The context */
    protected Context componentContext;
    
    /** Are we already setup for this request? */
    protected boolean initialized = false;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        if (this.manager != null) {
            this.manager.release(this.profileStore);
            this.manager.release( (Component)this.authenticationManager);
            this.manager.release( (Component)this.mediaManager);
            this.manager.release( (Component)this.sessionManager);
            this.manager.release( (Component)this.contextManager);
            this.manager.release( (Component)this.transactionManager);
            this.profileStore = null;
            this.authenticationManager = null;
            this.mediaManager = null;
            this.transactionManager = null;
            this.sessionManager = null;
            this.contextManager = null;
        }
        this.initialized = false;
    }

    /**
     * Get the current authentication state
     */
    protected RequestState getRequestState() {
        AuthenticationManager authManager = null;
        try {
            authManager = (AuthenticationManager)this.manager.lookup(AuthenticationManager.ROLE);
            return authManager.getState();    
        } catch (ComponentException ce) {
            // ignore this here
            return null;
        } finally {
            this.manager.release( (Component)authManager );
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.component.Composable#compose(org.apache.avalon.framework.component.ComponentManager)
     */
    public void compose(ComponentManager manager) 
    throws ComponentException {
        this.manager = manager;
        this.resolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);
        this.xpathProcessor = (XPathProcessor)this.manager.lookup(XPathProcessor.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.component.Recomposable#recompose(org.apache.avalon.framework.component.ComponentManager)
     */
    public void recompose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( (Component)this.xpathProcessor );
            this.xpathProcessor = null;
            this.manager.release( this.resolver );
            this.resolver = null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.componentContext = context;
    }

    /**
     * Get the profile store
     */
    protected Store getProfileStore()
    throws ProcessingException {
        if (this.profileStore == null) {
            try {
                this.profileStore = (Store)this.manager.lookup(Store.ROLE);
            } catch (ComponentException ce) {
                throw new ProcessingException("Error during lookup of store component.", ce);
            }
        }
        return this.profileStore;
    }

    /**
     * Get the authentication manager
     */
    protected AuthenticationManager getAuthenticationManager()
    throws ProcessingException {
        if (this.authenticationManager == null) {
            try {
                this.authenticationManager = (AuthenticationManager)this.manager.lookup(AuthenticationManager.ROLE);
            } catch (ComponentException ce) {
                throw new ProcessingException("Error during lookup of AuthenticationManager.", ce);
            }
        }
        return this.authenticationManager;
    }

    /**
     * Get the media manager
     */
    protected MediaManager getMediaManager()
    throws ProcessingException {
        if (this.mediaManager == null) {
            try {
                this.mediaManager = (MediaManager)this.manager.lookup(MediaManager.ROLE);
            } catch (ComponentException ce) {
                throw new ProcessingException("Error during lookup of MediaManager.", ce);
            }
        }
        return this.mediaManager;
    }

    /**
     * Setup this component
     */
    protected void setup()
    throws ProcessingException {
        if ( !this.initialized ) {
            
            final Request request = ContextHelper.getRequest(this.componentContext);
            
            if ( request.getAttribute(PortalManager.ROLE) == null ) {
                
                request.setAttribute(PortalManager.ROLE, Boolean.TRUE);
                
                // Get and ignore the configuration
                this.getConfiguration();
        
                try {
                    this.changeProfile();
                } catch (SAXException se) {
                    throw new ProcessingException(se);
                } catch (IOException ioe) {
                    throw new ProcessingException(ioe);
                }
            }
            
            this.initialized = true;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.portal.components.PortalManager#configurationTest()
     */
    public void configurationTest()
    throws ProcessingException, IOException, SAXException {        
        // no sync required
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN configurationTest");
        }

        this.setup();
        
        // Ignore result
        this.getConfiguration();

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END configurationTest");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.portal.components.PortalManager#getContext(boolean)
     */
    public SessionContext getContext(boolean create)
    throws ProcessingException, IOException, SAXException {
        // synchronized
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN getContext create="+create);
        }
        this.setup();
        SessionContext context = null;

        final Session session = this.getSessionManager().getSession(false);
        if (session != null) {
            synchronized(session) {
                String appName = this.getRequestState().getApplicationName();
                String attrName = PortalConstants.PRIVATE_SESSION_CONTEXT_NAME;
                if (appName != null) {
                    attrName = attrName + ':' + appName;
                }
                context = this.getContextManager().getContext(attrName);
                if (context == null && create) {

                    // create new context
                    
                    context = this.getAuthenticationManager().createApplicationContext(attrName, null, null);

                }
            } // end synchronized
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END getContext context="+context);
        }
        return context;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.portal.components.PortalManager#streamConfiguration(org.apache.cocoon.xml.XMLConsumer, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void streamConfiguration(XMLConsumer consumer,
                                    String      requestURI,
                                    String      profileID,
                                    String      media,
                                    String      contextID)
    throws IOException, SAXException, ProcessingException {
        // synchronized not req.
        this.setup();
        Response response = ContextHelper.getResponse(this.componentContext);
        
        XMLUtils.startElement(consumer, PortalConstants.ELEMENT_CONFIGURATION);

        // set the portal-page uri:
        StringBuffer buffer = new StringBuffer(requestURI);
        buffer.append((requestURI.indexOf('?') == -1 ? '?' : '&'))
            .append(PortalManagerImpl.REQ_PARAMETER_PROFILE)
            .append('=')
            .append(profileID);
        String uri = response.encodeURL(buffer.toString());
        XMLUtils.startElement(consumer, "uri");
        XMLUtils.data(consumer, uri);
        XMLUtils.endElement(consumer, "uri");

        Map config = this.getConfiguration();
        String portalURI = response.encodeURL((String)config.get(PortalConstants.CONF_PORTAL_URI));

        XMLUtils.startElement(consumer, "portal");
        XMLUtils.data(consumer, portalURI);
        XMLUtils.endElement(consumer, "portal");

        XMLUtils.startElement(consumer, PortalConstants.ELEMENT_PROFILE);
        XMLUtils.data(consumer, profileID);
        XMLUtils.endElement(consumer, PortalConstants.ELEMENT_PROFILE);

        if (media != null) {
            XMLUtils.startElement(consumer, "media");
            XMLUtils.data(consumer, media);
            XMLUtils.endElement(consumer, "media");
        }

        if (contextID != null) {
            XMLUtils.startElement(consumer, "context");
            XMLUtils.data(consumer, contextID);
            XMLUtils.endElement(consumer, "context");
        }

        XMLUtils.endElement(consumer, PortalConstants.ELEMENT_CONFIGURATION);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.portal.components.PortalManager#showAdminConf(org.apache.cocoon.xml.XMLConsumer)
     */
    public void showAdminConf(XMLConsumer consumer)
    throws SAXException, ProcessingException, IOException {
        // synchronized
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN showAdminConf consumer=" + consumer);
        }
        this.setup();
        Request request = ContextHelper.getRequest(this.componentContext);
        try {
            String profileID = "global";
            String copletID = request.getParameter(PortalManagerImpl.REQ_PARAMETER_COPLET);

            SessionContext context = this.getContext(true);

            Map configuration = this.getConfiguration();

            DocumentFragment copletsFragment = (DocumentFragment)context.getAttribute(ATTRIBUTE_ADMIN_COPLETS);
            String command = request.getParameter(PortalManagerImpl.REQ_PARAMETER_ADMIN_COPLETS);
            if (command != null && copletsFragment != null) {
                try {
                    this.getTransactionManager().startWritingTransaction(context);
                    // save : save coplets base
                    // new  : new coplet
                    // delete : use id to delete coplet
                    // change : change the coplet
                    //        cache : cleans the cache
                    if (command.equals("delete") && copletID != null) {
                        Node coplet = DOMUtil.getSingleNode(copletsFragment, "coplets-profile/coplets/coplet[@id='"+copletID+"']", this.xpathProcessor);
                        if (coplet != null) {
                            coplet.getParentNode().removeChild(coplet);
                        }
                    } else if (command.equals("change") && copletID != null) {
                        Node coplet = DOMUtil.getSingleNode(copletsFragment, "coplets-profile/coplets/coplet[@id='"+copletID+"']", this.xpathProcessor);
                        if (coplet != null) {
                            // now get the information
                            String value;

                            value = request.getParameter("portaladmin_title");
                            if (value != null) DOMUtil.setValueOfNode(DOMUtil.getSingleNode(coplet, "title", this.xpathProcessor), value);

                            value = request.getParameter("portaladmin_mand");
                            if (value != null) DOMUtil.setValueOfNode(DOMUtil.getSingleNode(coplet, "configuration/mandatory", this.xpathProcessor), value);

                            value = request.getParameter("portaladmin_sizable");
                            if (value != null) DOMUtil.setValueOfNode(DOMUtil.getSingleNode(coplet, "configuration/sizable", this.xpathProcessor), value);

                            value = request.getParameter("portaladmin_active");
                            if (value != null) DOMUtil.setValueOfNode(DOMUtil.getSingleNode(coplet, "configuration/active", this.xpathProcessor), value);

                            value = request.getParameter("portaladmin_handsize");
                            if (value != null) DOMUtil.setValueOfNode(DOMUtil.selectSingleNode(coplet, "configuration/handlesSizable", this.xpathProcessor), value);

                            value = request.getParameter("portaladmin_handpar");
                            if (value != null) DOMUtil.setValueOfNode(DOMUtil.selectSingleNode(coplet, "configuration/handlesParameters", this.xpathProcessor), value);

                            value = request.getParameter("portaladmin_timeout");
                            if (value != null) DOMUtil.setValueOfNode(DOMUtil.selectSingleNode(coplet, "configuration/timeout", this.xpathProcessor), value);

                            value = request.getParameter("portaladmin_customizable");
                            if (value != null) DOMUtil.setValueOfNode(DOMUtil.selectSingleNode(coplet, "configuration/customizable", this.xpathProcessor), value);

                            value = request.getParameter("portaladmin_persistent");
                            if (value != null) DOMUtil.setValueOfNode(DOMUtil.selectSingleNode(coplet, "configuration/persistent", this.xpathProcessor), value);

                            String resource = request.getParameter("portaladmin_resource");
                            if (resource != null) {
                                Element resourceNode = (Element)DOMUtil.getSingleNode(coplet, "resource", this.xpathProcessor);
                                resourceNode.getParentNode().removeChild(resourceNode);
                                resourceNode = coplet.getOwnerDocument().createElementNS(null, "resource");
                                resourceNode.setAttributeNS(null, "uri", resource);
                                coplet.appendChild(resourceNode);
                            }
                            resource = request.getParameter("portaladmin_cust");
                            boolean isCustom = DOMUtil.getValueAsBooleanOf(coplet, "configuration/customizable", false, this.xpathProcessor);
                            if (resource != null && isCustom ) {
                                Element resourceNode = (Element)DOMUtil.getSingleNode(coplet, "customization", this.xpathProcessor);
                                if (resourceNode != null) resourceNode.getParentNode().removeChild(resourceNode);
                                resourceNode = coplet.getOwnerDocument().createElementNS(null, "customization");
                                resourceNode.setAttributeNS(null, "uri", resource);
                                coplet.appendChild(resourceNode);
                            }
                            if (!isCustom) {
                                Element resourceNode = (Element)DOMUtil.getSingleNode(coplet, "customization", this.xpathProcessor);
                                if (resourceNode != null) resourceNode.getParentNode().removeChild(resourceNode);
                            }

                            // transformations
                            value = request.getParameter("portaladmin_newxsl");
                            if (value != null) {
                                Element tNode = (Element)DOMUtil.selectSingleNode(coplet, "transformation", this.xpathProcessor);
                                Element sNode = tNode.getOwnerDocument().createElementNS(null, "stylesheet");
                                tNode.appendChild(sNode);
                                sNode.appendChild(sNode.getOwnerDocument().createTextNode(value));
                            }

                            // now get all transformation stylesheets, mark
                            // all stylesheets which should be deleted with
                            // an attribute delete
                            Enumeration keys = request.getParameterNames();
                            Element sNode;
                            String key;
                            while (keys.hasMoreElements() ) {
                                key = (String)keys.nextElement();
                                if (key.startsWith("portaladmin_xsl_") ) {
                                    value = key.substring(key.lastIndexOf('_')+ 1);
                                    sNode = (Element)DOMUtil.getSingleNode(coplet, "transformation/stylesheet[position()="+value+"]", this.xpathProcessor);
                                    if (sNode != null) {
                                        String xslName = request.getParameter(key);
                                        if (xslName.equals("true") ) xslName = "**STYLESHEET**";
                                        DOMUtil.setValueOfNode(sNode, xslName);
                                    }
                                } else if (key.startsWith("portaladmin_delxsl_") ) {
                                    value = key.substring(key.lastIndexOf('_')+ 1);
                                    sNode = (Element)DOMUtil.getSingleNode(coplet, "transformation/stylesheet[position()="+value+"]", this.xpathProcessor);
                                    if (sNode != null) {
                                        sNode.setAttributeNS(null, "delete", "true");
                                    }
                                }
                            }
                            NodeList delete = DOMUtil.selectNodeList(coplet, "transformation/stylesheet[@delete]", this.xpathProcessor);
                            if (delete != null) {
                                for(int i=0; i < delete.getLength(); i++) {
                                    delete.item(i).getParentNode().removeChild(delete.item(i));
                                }
                            }
                        }
                    } else if (command.equals("new") ) {
                        // first we have to invent a new coplet id!
                        int index = 0;
                        boolean found = false;
                        Element coplet;
                        Element subNode;

                        while (!found) {
                            copletID = "S"+index;
                            coplet = (Element)DOMUtil.getSingleNode(copletsFragment, "coplets-profile/coplets/coplet[@id='"+copletID+"']", this.xpathProcessor);
                            if (coplet == null) {
                                found = true;
                            } else {
                                index++;
                            }
                        }
                        coplet = copletsFragment.getOwnerDocument().createElementNS(null, "coplet");
                        coplet.setAttributeNS(null, "id", copletID);
                        subNode = coplet.getOwnerDocument().createElementNS(null, "resource");
                        coplet.appendChild(subNode);
                        subNode.setAttributeNS(null, "uri", "uri_in_sitemap");

                        String title = request.getParameter("portaladmin_title");
                        if (title == null || title.trim().length() == 0) title = "**NEW COPLET**";
                        DOMUtil.setValueOfNode(DOMUtil.selectSingleNode(coplet, "configuration/mandatory", this.xpathProcessor), "false");
                        DOMUtil.setValueOfNode(DOMUtil.selectSingleNode(coplet, "configuration/sizable", this.xpathProcessor), "true");
                        DOMUtil.setValueOfNode(DOMUtil.selectSingleNode(coplet, "configuration/active", this.xpathProcessor), "false");
                        DOMUtil.setValueOfNode(DOMUtil.selectSingleNode(coplet, "configuration/handlesParameters", this.xpathProcessor), "true");
                        DOMUtil.setValueOfNode(DOMUtil.selectSingleNode(coplet, "configuration/handlesSizable", this.xpathProcessor), "false");
                        DOMUtil.setValueOfNode(DOMUtil.selectSingleNode(coplet, "title", this.xpathProcessor), title);
                        DOMUtil.setValueOfNode(DOMUtil.selectSingleNode(coplet, "status/visible", this.xpathProcessor), "true");
                        DOMUtil.setValueOfNode(DOMUtil.selectSingleNode(coplet, "status/size", this.xpathProcessor), "max");
                        DOMUtil.getSingleNode(copletsFragment, "coplets-profile/coplets", this.xpathProcessor).appendChild(coplet);
                    } else if (command.equals("save") ) {

                        SourceParameters pars = new SourceParameters();
                        pars.setSingleParameterValue("profile", "coplet-base");
                        RequestState state = this.getRequestState();
                        pars.setSingleParameterValue("application", state.getApplicationName());
                        pars.setSingleParameterValue("handler", state.getHandlerName());

                        String saveResource = (String)configuration.get(PortalConstants.CONF_COPLETBASE_SAVE_RESOURCE);

                        if (saveResource == null) {
                            throw new ProcessingException("portal: No save resource defined for type coplet-base.");
                        } else {
                            
                            SourceUtil.writeDOM(saveResource, 
                                                null, 
                                                pars, 
                                                copletsFragment, 
                                                this.resolver, 
                                                "xml");

                            // now the hardest part, clean up the whole cache
                            this.cleanUpCache(null, null, configuration);
                        }
                    }
                } finally {
                    this.getTransactionManager().stopWritingTransaction(context);
                }
            }

            // general commands
            if (command != null && command.equals("cleancache") ) {
                this.cleanUpCache(null, null, configuration);
            }

            String state = request.getParameter(PortalManagerImpl.REQ_PARAMETER_STATE);
            if (state == null) {
                state = (String)context.getAttribute(ATTRIBUTE_ADMIN_STATE, PortalConstants.STATE_MAIN);
            }

            // now start producing xml:
            AttributesImpl attr = new AttributesImpl();
            consumer.startElement("", PortalConstants.ELEMENT_ADMINCONF, PortalConstants.ELEMENT_ADMINCONF, attr);

            context.setAttribute(ATTRIBUTE_ADMIN_STATE, state);
            consumer.startElement("", PortalConstants.ELEMENT_STATE, PortalConstants.ELEMENT_STATE, attr);
            consumer.characters(state.toCharArray(), 0, state.length());
            consumer.endElement("", PortalConstants.ELEMENT_STATE, PortalConstants.ELEMENT_STATE);

            if (state.equals(PortalConstants.STATE_MAIN) ) {

                Document rolesDF = this.getRoles();
                Node     roles   = null;
                if (rolesDF != null) roles = DOMUtil.getSingleNode(rolesDF, "roles", this.xpathProcessor);
                IncludeXMLConsumer.includeNode(roles, consumer, consumer);
            }

            if (state.equals(PortalConstants.STATE_MAIN_ROLE) ) {

                Document rolesDF = this.getRoles();
                Node     roles   = null;
                if (rolesDF != null) roles = DOMUtil.getSingleNode(rolesDF, "roles", this.xpathProcessor);
                IncludeXMLConsumer.includeNode(roles, consumer, consumer);

                String role = request.getParameter(PortalManagerImpl.REQ_PARAMETER_ROLE);
                if (role == null) {
                    role = (String)context.getAttribute(ATTRIBUTE_ADMIN_ROLE);
                }
                context.setAttribute(ATTRIBUTE_ADMIN_ROLE, role);
                if (role != null) {
                    XMLUtils.startElement(consumer, "roleusers");
                    XMLUtils.startElement(consumer, "name");
                    XMLUtils.data(consumer, role);
                    XMLUtils.endElement(consumer, "name");
                    Document userDF = this.getUsers(role, null);
                    Node     users = null;
                    if (userDF != null) users = DOMUtil.getSingleNode(userDF, "users", this.xpathProcessor);
                    IncludeXMLConsumer.includeNode(users, consumer, consumer);
                    XMLUtils.endElement(consumer, "roleusers");
                }
            }

            if (state.equals(PortalConstants.STATE_GLOBAL)) {
                profileID = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_GLOBAL, null, null, true);
                Map profile = this.retrieveProfile(profileID);
                if (profile == null) {
                    this.createProfile(context, PortalManagerImpl.BUILDTYPE_VALUE_GLOBAL, null, null, true);
                    profile = this.retrieveProfile(profileID);
                }
                this.showPortal(consumer, true, context, profile, profileID);
            }

            if (state.equals(PortalConstants.STATE_ROLE) ) {
                String role = request.getParameter(PortalManagerImpl.REQ_PARAMETER_ROLE);
                if (role == null) {
                    role = (String)context.getAttribute(ATTRIBUTE_ADMIN_ROLE);
                }
                context.setAttribute(ATTRIBUTE_ADMIN_ROLE, role);
                if (role != null) {
                    consumer.startElement("", PortalConstants.ELEMENT_ROLE, PortalConstants.ELEMENT_ROLE, attr);
                    consumer.characters(role.toCharArray(), 0, role.length());
                    consumer.endElement("", PortalConstants.ELEMENT_ROLE, PortalConstants.ELEMENT_ROLE);
                    profileID = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_ROLE, role, null, true);
                    Map profile = this.retrieveProfile(profileID);
                    if (profile == null) {
                        this.createProfile(context, PortalManagerImpl.BUILDTYPE_VALUE_ROLE, role, null, true);
                        profile = this.retrieveProfile(profileID);
                    }
                    this.showPortal(consumer, true, context, profile, profileID);
                }
            }
            if (state.equals(PortalConstants.STATE_USER) ) {
                String role = request.getParameter(PortalManagerImpl.REQ_PARAMETER_ROLE);
                String id   = request.getParameter(PortalManagerImpl.REQ_PARAMETER_ID);
                if (role == null) {
                    role = (String)context.getAttribute(ATTRIBUTE_ADMIN_ROLE);
                }
                if (id == null) {
                    id = (String)context.getAttribute(ATTRIBUTE_ADMIN_ID);
                }
                context.setAttribute(ATTRIBUTE_ADMIN_ID, id);
                context.setAttribute(ATTRIBUTE_ADMIN_ROLE, role);
                if (role != null && id != null) {
                    consumer.startElement("", PortalConstants.ELEMENT_ROLE, PortalConstants.ELEMENT_ROLE, attr);
                    consumer.characters(role.toCharArray(), 0, role.length());
                    consumer.endElement("", PortalConstants.ELEMENT_ROLE, PortalConstants.ELEMENT_ROLE);
                    consumer.startElement("", PortalConstants.ELEMENT_ID, PortalConstants.ELEMENT_ID, attr);
                    consumer.characters(id.toCharArray(), 0, id.length());
                    consumer.endElement("", PortalConstants.ELEMENT_ID, PortalConstants.ELEMENT_ID);

                    profileID = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_ID, role, id, true);
                    Map profile = this.retrieveProfile(profileID);
                    if (profile == null) {
                        this.createProfile(context, PortalManagerImpl.BUILDTYPE_VALUE_ID, role, id, true);
                        profile = this.retrieveProfile(profileID);
                    }
                    this.showPortal(consumer, true, context, profile, profileID);
                }
            }
            // one coplet
            if (state.equals(PortalConstants.STATE_COPLET) ) {
                if (copletsFragment != null && copletID != null) {
                    Node coplet = DOMUtil.getSingleNode(copletsFragment, "coplets-profile/coplets/coplet[@id='"+copletID+"']", this.xpathProcessor);
                    if (coplet != null) {
                        IncludeXMLConsumer.includeNode(coplet, consumer, consumer);
                    }
                } else {
                    state = PortalConstants.STATE_COPLETS;
                }
            }
            if (state.equals(PortalConstants.STATE_COPLETS) ) {
                consumer.startElement("", PortalConstants.ELEMENT_COPLETS, PortalConstants.ELEMENT_COPLETS, attr);

                // load the base coplets profile
                if (copletsFragment == null) {
                    SourceParameters pars = new SourceParameters();
                    RequestState reqstate = this.getRequestState();
                    pars.setSingleParameterValue("application", reqstate.getApplicationName());
                    String res = (String)configuration.get(PortalConstants.CONF_COPLETBASE_RESOURCE);
                    if (res == null) {
                        throw new ProcessingException("No configuration for portal-coplet base profile found.");
                    }
                    copletsFragment = SourceUtil.readDOM(res, 
                                                         null, 
                                                         pars, 
                                                         this.resolver);
                    context.setAttribute(ATTRIBUTE_ADMIN_COPLETS, copletsFragment);
                }
                IncludeXMLConsumer.includeNode(DOMUtil.selectSingleNode(copletsFragment,
                                   "coplets-profile", this.xpathProcessor), consumer, consumer);
                consumer.endElement("", PortalConstants.ELEMENT_COPLETS, PortalConstants.ELEMENT_COPLETS);
            }

            // configuration
            this.streamConfiguration(consumer, request.getRequestURI(), profileID, null, null);

            consumer.endElement("", PortalConstants.ELEMENT_ADMINCONF, PortalConstants.ELEMENT_ADMINCONF);
        } catch (javax.xml.transform.TransformerException local) {
            throw new ProcessingException("TransformerException: " + local, local);
        }

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END showAdminConf");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.portal.components.PortalManager#getStatusProfile()
     */
    public Element getStatusProfile()
    throws SAXException, IOException, ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN getStatusProfile");
        }
        this.setup();
        SessionContext context = this.getContext(true);
        String profileID = null;
        Map storedProfile = null;
        Element statusProfile = null;

        if (context.getAttribute(PortalManagerImpl.ATTRIBUTE_PORTAL_ROLE) != null) {
            profileID = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_ID,
                  (String)context.getAttribute(PortalManagerImpl.ATTRIBUTE_PORTAL_ROLE),
                  (String)context.getAttribute(PortalManagerImpl.ATTRIBUTE_PORTAL_ID), false);
            storedProfile = this.retrieveProfile(profileID);
        }

        if (storedProfile != null) {
            DocumentFragment profile = (DocumentFragment)storedProfile.get(PortalConstants.PROFILE_PROFILE);
            try {
                statusProfile = (Element)DOMUtil.getSingleNode(profile, "profile/status-profile", this.xpathProcessor);
            } catch (javax.xml.transform.TransformerException ignore) {
            }
        }

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END getStatusProfile statusProfile="+(statusProfile == null ? "null" : XMLUtils.serializeNode(statusProfile, XMLUtils.createPropertiesForXML(false))));
        }
        return statusProfile;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.portal.components.PortalManager#showPortal(org.apache.cocoon.xml.XMLConsumer, boolean, boolean)
     */
    public void showPortal(XMLConsumer consumer,
                           boolean configMode,
                           boolean adminProfile)
    throws SAXException, ProcessingException, IOException {
        // synchronized
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN showPortal consumer=" + consumer+", configMode="+
                             configMode+", adminProfile="+adminProfile);
        }
        this.setup();
        
        SessionContext context = this.getContext(true);
        String profileID = null;
        Map storedProfile = null;

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("start portal generation");
        }
        if (context.getAttribute(PortalManagerImpl.ATTRIBUTE_PORTAL_ROLE) != null) {
            profileID = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_ID,
                  (String)context.getAttribute(PortalManagerImpl.ATTRIBUTE_PORTAL_ROLE),
                  (String)context.getAttribute(PortalManagerImpl.ATTRIBUTE_PORTAL_ID), adminProfile);
            storedProfile = this.retrieveProfile(profileID);
        }
        if (storedProfile == null) {

            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("start building profile");
            }
            this.createProfile(context, PortalManagerImpl.BUILDTYPE_VALUE_ID, null, null, adminProfile);
            // get the profileID
            profileID = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_ID,
                    (String)context.getAttribute(PortalManagerImpl.ATTRIBUTE_PORTAL_ROLE),
                    (String)context.getAttribute(PortalManagerImpl.ATTRIBUTE_PORTAL_ID), adminProfile);
            storedProfile = this.retrieveProfile(profileID);
            if (storedProfile == null) {
                throw new ProcessingException("portal: No portal profile found.");
            }
            if (this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug("end building profile");
            }
        }

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("start showing profile");
        }
        this.showPortal(consumer,
                        configMode,
                        context,
                        storedProfile,
                        profileID);
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("end showing profile");
            this.getLogger().debug("end portal generation");
        }
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END showPortal");
        }
    }

    /**
     * Stream all layout information for the current portal
     * to the consumer.
     * The resulting XML:
     * <layout>
     *     <portal>
     *         ...
     *     </portal>
     *     <coplets>
     *         ...
     *     </coplets>
     * </layout>
     */
    public static void streamLayoutProfile(XMLConsumer consumer,
                                           Map         portalLayouts,
                                           Map         copletLayouts,
                                           String      mediaType)
    throws SAXException {
        Element  element;
        NodeList childs;
        Attributes attr = new AttributesImpl();

        consumer.startElement("", PortalConstants.ELEMENT_LAYOUT, PortalConstants.ELEMENT_LAYOUT, attr);

        // first: layout of portal
        consumer.startElement("", PortalConstants.ELEMENT_PORTAL, PortalConstants.ELEMENT_PORTAL, attr);

        element = (Element)portalLayouts.get(mediaType);
        childs = element.getChildNodes();
        for(int ci = 0; ci < childs.getLength(); ci++) {
            IncludeXMLConsumer.includeNode(childs.item(ci),
                                      consumer,
                                      consumer);
        }
        consumer.endElement("", PortalConstants.ELEMENT_PORTAL, PortalConstants.ELEMENT_PORTAL);

        // second: layout of coplets
        consumer.startElement("", PortalConstants.ELEMENT_COPLETS, PortalConstants.ELEMENT_COPLETS, attr);
        element = (Element)copletLayouts.get(mediaType);
        childs = element.getChildNodes();
        for(int ci = 0; ci < childs.getLength(); ci++) {
            IncludeXMLConsumer.includeNode(childs.item(ci),
                                      consumer,
                                      consumer);
        }
        consumer.endElement("", PortalConstants.ELEMENT_COPLETS, PortalConstants.ELEMENT_COPLETS);

        consumer.endElement("", PortalConstants.ELEMENT_LAYOUT, PortalConstants.ELEMENT_LAYOUT);
    }

    /**
     * Show the portal.
     * The portal is included in the current stream.
     */
    private void showPortal(XMLConsumer consumer,
                           boolean      configMode,
                           SessionContext context,
                           Map          storedProfile,
                           String       profileID)
    throws SAXException, ProcessingException, IOException {
        // synchronized
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN showPortal consumer=" + consumer+", configMode="+configMode+", context="+context+
                ", profile="+storedProfile);
        }
        Request request = ContextHelper.getRequest(this.componentContext);
        try {
            this.getTransactionManager().startReadingTransaction(context);

            DocumentFragment profile;
            Map              defaultCoplets;
            Map              mediaCoplets;
            Map              portalLayouts;
            Map              copleyLayouts;
            Node[]           miscNodes;
            String           mediaType = this.getMediaManager().getMediaType();

            profile = (DocumentFragment)storedProfile.get(PortalConstants.PROFILE_PROFILE);
            portalLayouts = (Map)storedProfile.get(PortalConstants.PROFILE_PORTAL_LAYOUTS);
            copleyLayouts = (Map)storedProfile.get(PortalConstants.PROFILE_COPLET_LAYOUTS);
            miscNodes = (Node[])storedProfile.get(PortalConstants.PROFILE_MISC_POINTER);
            defaultCoplets = (Map)storedProfile.get(PortalConstants.PROFILE_DEFAULT_COPLETS);
            mediaCoplets = (Map)storedProfile.get(PortalConstants.PROFILE_MEDIA_COPLETS);
            if (profile == null ||
                defaultCoplets == null ||
                mediaCoplets == null ||
                portalLayouts == null ||
                copleyLayouts == null ||
                miscNodes == null) {
                throw new ProcessingException("portal: No portal profile found.");
            }

            // get the configuration
            Map config = this.getConfiguration();
            if (config == null) {
                throw new ProcessingException("No configuration for portal found.");
            }
            boolean processCopletsParallel = false;
            long    defaultCopletTimeout   = 600000;

            Boolean boolValue = (Boolean)config.get(PortalConstants.CONF_PARALLEL_COPLETS);
            if (boolValue != null) processCopletsParallel = boolValue.booleanValue();
            Long longValue = (Long)config.get(PortalConstants.CONF_COPLET_TIMEOUT);
            if (longValue != null) defaultCopletTimeout = longValue.longValue();

            Element element;

            // now start producing xml:
            AttributesImpl attr = new AttributesImpl();
            if (configMode) {
                XMLUtils.startElement(consumer, PortalConstants.ELEMENT_PORTALCONF);
            } else {
                XMLUtils.startElement(consumer, PortalConstants.ELEMENT_PORTAL);
            }

            // configuration
            this.streamConfiguration(consumer, request.getRequestURI(), profileID, mediaType, null);

            // LAYOUT:
            if (configMode) {
                IncludeXMLConsumer.includeNode(DOMUtil.getFirstNodeFromPath(profile, new String[] {"profile","layout-profile"}, false),
                     consumer, consumer);
                // copletsConfiguration (only for configMode)
                IncludeXMLConsumer.includeNode(DOMUtil.getFirstNodeFromPath(profile, new String[] {"profile","coplets-profile"}, false),
                     consumer, consumer);
                IncludeXMLConsumer.includeNode(DOMUtil.getFirstNodeFromPath(profile, new String[] {"profile","type-profile","typedefs"}, false),
                     consumer, consumer);
                IncludeXMLConsumer.includeNode(DOMUtil.getFirstNodeFromPath(profile, new String[] {"profile","portal-profile"}, false),
                     consumer, consumer);
                IncludeXMLConsumer.includeNode(DOMUtil.getFirstNodeFromPath(profile, new String[] {"profile","personal-profile"}, false),
                     consumer, consumer);
                IncludeXMLConsumer.includeNode(DOMUtil.getFirstNodeFromPath(profile, new String[] {"profile","status-profile"}, false),
                     consumer, consumer);
            } else {
                PortalManagerImpl.streamLayoutProfile(consumer, portalLayouts, copleyLayouts, mediaType);
            }
            // END LAYOUT

            if (!configMode) {
                Element statusProfile = (Element)DOMUtil.getFirstNodeFromPath(profile, new String[] {"profile","status-profile"}, false);

                String copletNotAvailableMessage = "The coplet is currently not available.";
                Node messages = miscNodes[PortalConstants.PROFILE_MISC_MESSAGES_NODE];
                if (messages != null) {
                    messages = DOMUtil.getFirstNodeFromPath(messages, new String[] {"coplet_not_available"}, false);
                    if (messages != null) copletNotAvailableMessage = DOMUtil.getValueOfNode(messages,
                         copletNotAvailableMessage);
                }

                // LOAD COPLETS
                List[] copletContents;

                List[] temp = (List[])context.getAttribute(PortalConstants.ATTRIBUTE_COPLET_REPOSITORY);
                if (temp != null) {
                    copletContents = new List[temp.length];
                    for (int i = 0; i < temp.length; i++) {
                        if (temp[i] == null) {
                            copletContents[i] = null;
                        } else {
                            copletContents[i] = new ArrayList(temp[i]);
                        }
                    }
                } else {
                    copletContents = new List[PortalConstants.MAX_COLUMNS+2];
                    context.setAttribute(PortalConstants.ATTRIBUTE_COPLET_REPOSITORY, copletContents);
                }

                if (copletContents[0] == null) {
                    copletContents[0] = new ArrayList(1);
                } else {
                    copletContents[0].clear();
                }
                if (copletContents[1] == null) {
                    copletContents[1] = new ArrayList(1);
                } else {
                    copletContents[1].clear();
                }

                // test for header
                String value;
                value = DOMUtil.getValueOfNode(miscNodes[PortalConstants.PROFILE_MISC_HEADER_NODE]);
                if (value != null && new Boolean(value).booleanValue()) {
                    element = (Element)miscNodes[PortalConstants.PROFILE_MISC_HEADER_CONTENT_NODE];
                    if (element != null) {
                        this.loadCoplets(element,
                                         defaultCoplets,
                                         mediaCoplets,
                                         copletContents[0],
                                         processCopletsParallel,
                                         defaultCopletTimeout,
                                         statusProfile);
                    }
                }

                // content
                value = DOMUtil.getValueOfNode(miscNodes[PortalConstants.PROFILE_MISC_COLUMNS_NODE]);

                // for a simpler XSL-Stylesheet: The columns must be inserted in the
                // correct order!!!
                if (value != null && new Integer(value).intValue() > 0) {

                    Element columnElement;
                    int columns = new Integer(value).intValue();
                    if (columns > PortalConstants.MAX_COLUMNS) {
                        throw new ProcessingException("portal: Maximum number of columns supported is: "+PortalConstants.MAX_COLUMNS);
                    }

                    for(int colindex = 1; colindex <= columns; colindex++) {
                        if (copletContents[colindex+1] == null) {
                            copletContents[colindex+1] = new ArrayList(10);
                        } else {
                            copletContents[colindex+1].clear();
                        }
                        columnElement = (Element)miscNodes[7 + colindex];
                        element = (Element)DOMUtil.getFirstNodeFromPath(columnElement, new String[] {"coplets"}, false);
                        if (element != null) {
                            this.loadCoplets(element,
                                             defaultCoplets,
                                             mediaCoplets,
                                             copletContents[colindex+1],
                                             processCopletsParallel,
                                             defaultCopletTimeout,
                                             statusProfile);
                        }

                    }
                    for(int colindex = columns+2; colindex <= PortalConstants.MAX_COLUMNS+1; colindex++) {
                        if (copletContents[colindex] != null) {
                            copletContents[colindex] = null;
                        }
                    }

                } else {
                    for(int colindex = 1; colindex <= PortalConstants.MAX_COLUMNS; colindex++) {
                        if (copletContents[colindex+1] != null) {
                            copletContents[colindex+1] = null;
                        }
                    }
                }

                // test for footer
                value = DOMUtil.getValueOfNode(miscNodes[PortalConstants.PROFILE_MISC_FOOTER_NODE]);
                if (value != null && new Boolean(value).booleanValue()) {
                    element = (Element)miscNodes[PortalConstants.PROFILE_MISC_FOOTER_CONTENT_NODE];
                    if (element != null) {
                        this.loadCoplets(element,
                                         defaultCoplets,
                                         mediaCoplets,
                                         copletContents[1],
                                         processCopletsParallel,
                                         defaultCopletTimeout,
                                         statusProfile);
                    }
                }
                // END LOAD COPLETS

                // DESIGN
                // test for header
                if (copletContents[0].size() > 0) {
                    consumer.startElement("", "header", "header", attr);
                    this.processCopletList(copletContents[0], consumer, copletNotAvailableMessage, defaultCopletTimeout);
                    consumer.endElement("", "header", "header");
                }

                // content
                value = DOMUtil.getValueOfNode(miscNodes[PortalConstants.PROFILE_MISC_COLUMNS_NODE]);

                // for a simpler XSL-Stylesheet: The columns must be inserted in the
                // correct order!!!
                if (value != null && new Integer(value).intValue() > 0) {
                    attr.addAttribute("", "number", "number", "CDATA", value);
                    XMLUtils.startElement(consumer, "columns", attr);
                    attr.clear();

                    int columns = new Integer(value).intValue();
                    if (columns > PortalConstants.MAX_COLUMNS) {
                        throw new ProcessingException("portal: Maximum number of columns supported is: "+PortalConstants.MAX_COLUMNS);
                    }

                    // determine the width of the columns
                    String[] width = new String[columns];
                    int normalWidth = 100 / columns;
                    Element columnElement;

                    for(int colindex = 1; colindex <= columns; colindex++) {
                        columnElement = (Element)miscNodes[7 + colindex];
                        value = DOMUtil.getValueOf(columnElement, "width", this.xpathProcessor);
                        if (value == null) {
                            width[colindex-1] = "" + normalWidth + "%";
                        } else {
                            width[colindex-1] = value;
                        }
                    }

                    for(int colindex = 1; colindex <= columns; colindex++) {
                        attr.addAttribute("", "position", "position", "CDATA", "" + colindex);
                        attr.addAttribute("", "width", "width", "CDATA", width[colindex-1]);
                        XMLUtils.startElement(consumer, "column", attr);
                        attr.clear();

                        this.processCopletList(copletContents[colindex+1], consumer, copletNotAvailableMessage, defaultCopletTimeout);

                        XMLUtils.endElement(consumer, "column");
                    }
                    XMLUtils.endElement(consumer, "columns");
                } else {
                    attr.addAttribute("", "number", "number", "CDATA", "0");
                    XMLUtils.startElement(consumer, "columns", attr);
                    XMLUtils.endElement(consumer, "columns");
                    attr.clear();
                }

                // test for footer
                if (copletContents[1].size() > 0) {
                    XMLUtils.startElement(consumer, "footer");
                    this.processCopletList(copletContents[1], consumer, copletNotAvailableMessage, defaultCopletTimeout);
                    XMLUtils.endElement(consumer, "footer");
                }
                // END DESIGN

                for(int i=0; i<copletContents.length;i++) {
                    if (copletContents[i]!=null) copletContents[i].clear();
                }

                // Personal information and status information
                this.sendEvents(consumer, DOMUtil.getFirstNodeFromPath(profile, new String[] {"profile","personal-profile"}, false));
                this.sendEvents(consumer, statusProfile);
            }

            if (configMode) {
                XMLUtils.endElement(consumer, PortalConstants.ELEMENT_PORTALCONF);
            } else {
                XMLUtils.endElement(consumer, PortalConstants.ELEMENT_PORTAL);
            }

        } catch (javax.xml.transform.TransformerException local) { // end synchronized
            throw new ProcessingException("TransformerException: " + local, local);
        } finally {
            this.getTransactionManager().stopReadingTransaction(context);
        }
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END showPortal");
        }
    }


    /**
     * Building the profile.
     * This includes several steps which are declared in detail inside this method...
     */
    protected void buildProfile(String type,
                             String role,
                             String id,
                             boolean adminProfile)
    throws ProcessingException, IOException, SAXException {
        // synchronized
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN buildProfile type=" + type + ", role=" + role + ", id=" +id+", adminProfile="+adminProfile);
        }
        try {
            // check parameter
            if (type == null) {
                throw new ProcessingException("buildProfile: Type is required");
            }
            if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_GLOBAL) ||
                type.equals(PortalManagerImpl.BUILDTYPE_VALUE_BASIC)) {
                // nothing to do here
            } else if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ROLE)) {
                if (role == null) {
                    throw new ProcessingException("buildProfile: Role is required");
                }
            } else if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ID)) {
                if (role == null) {
                    throw new ProcessingException("buildProfile: Role is required");
                }
                if (id == null) {
                    throw new ProcessingException("buildProfile: ID is required");
                }
            } else {
                throw new ProcessingException("buildProfile: Type unknown: " + type);
            }

            SessionContext context = this.getContext(true);
            try {
                this.getTransactionManager().startWritingTransaction(context);

                String profileID = this.getProfileID(type, role, id, adminProfile);
                Map theProfile = null;

                // get the configuration
                Map config = this.getConfiguration();
                if (config == null) {
                    throw new ProcessingException("No configuration for portal found.");
                }

                // is the ID profile cached?
                if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ID) ) {
                    theProfile = this.getCachedProfile(profileID, config);
                }

                if (theProfile == null) {

                    boolean doBase = false;
                    boolean doGlobal = false;
                    boolean doRole = false;
                    boolean doID = false;
                    String previousID;

                    if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ID)) {
                        doID = true;
                        previousID = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_ROLE, role, null, adminProfile);
                        theProfile = this.getCachedProfile(previousID, config);
                        if (theProfile == null) {
                            doRole = true;
                            previousID = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_GLOBAL, null, null, adminProfile);
                            theProfile = this.getCachedProfile(previousID, config);
                            if (theProfile == null) {
                                doGlobal = true;
                                previousID = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_BASIC, null, null, adminProfile);
                                theProfile = this.getCachedProfile(previousID, config);
                            }
                        }
                    } else if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ROLE)) {
                        theProfile = this.getCachedProfile(profileID, config);
                        if (theProfile == null) {
                            doRole = true;
                            previousID = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_GLOBAL, null, null, adminProfile);
                            theProfile = this.getCachedProfile(previousID, config);
                            if (theProfile == null) {
                                doGlobal = true;
                                previousID = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_BASIC, null, null, adminProfile);
                                theProfile = this.getCachedProfile(previousID, config);
                            }
                        }
                    } else if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_GLOBAL)) {
                        theProfile = this.getCachedProfile(profileID, config);
                        if (theProfile == null) {
                            doGlobal = true;
                            previousID = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_BASIC, null, null, adminProfile);
                            theProfile = this.getCachedProfile(previousID, config);
                        }
                    } else { // basic profile
                        theProfile = this.getCachedProfile(profileID, config);
                    }

                    // build the profile
                    if (theProfile == null) {
                        theProfile = new HashMap(8,2);
                        doBase = true;
                    }

                    Element          profileRoot;
                    DocumentFragment profile;

                    if (doBase) {
                        // build the base level
                        profile = this.buildBaseProfile(config, adminProfile);
                        profileRoot = (Element)profile.getFirstChild();
                        theProfile.put(PortalConstants.PROFILE_PROFILE, profile);
                        this.cacheProfile(this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_BASIC, null, null, adminProfile), theProfile, config);
                    } else {
                        profile = (DocumentFragment)theProfile.get(PortalConstants.PROFILE_PROFILE);
                        profileRoot = (Element)profile.getFirstChild();
                    }

                    // load the global delta if type is global, role or user (but not basic!)
                    if (doGlobal) {
                        this.buildGlobalProfile(profileRoot, config, adminProfile);
                        this.cacheProfile(this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_GLOBAL, null, null, adminProfile), theProfile, config);
                    }

                    // load the role delta if type is role or user
                    if (doRole) {
                        this.buildRoleProfile(profileRoot, config, role, adminProfile);
                        this.cacheProfile(this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_ROLE, role, null, adminProfile), theProfile, config);
                    }

                    // load the user delta if type is user
                    if (doID) {
                        this.buildUserProfile(profileRoot, config, role, id, adminProfile);
                    }

                    // load the status profile when type is user
                    if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ID)) {
                        this.buildUserStatusProfile(profileRoot, config, role, id, adminProfile);
                    }

                    if (!type.equals(PortalManagerImpl.BUILDTYPE_VALUE_BASIC)) {
                        this.buildRunProfile(theProfile, context, profile);

                        theProfile.put(PortalConstants.PROFILE_PORTAL_LAYOUTS,
                               this.buildPortalLayouts(context, profile));
                        theProfile.put(PortalConstants.PROFILE_COPLET_LAYOUTS,
                               this.buildcopleyLayouts(context, profile));

                        this.buildTypeProfile(theProfile, context, profile);
                    }

                    // cache the profile, if user
                    if (doID) {
                        this.cacheProfile(profileID, theProfile, config);
                    }
                } else {
                    // load the status profile when type is user
                    if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ID)) {
                        DocumentFragment profile = (DocumentFragment)theProfile.get(PortalConstants.PROFILE_PROFILE);
                        Element profileRoot = (Element)profile.getFirstChild();
                        this.buildUserStatusProfile(profileRoot, config, role, id, adminProfile);
                    }
                }

                // store the whole profile
                this.storeProfile(profileID, theProfile);

                // now put role and id into the context if type is ID
                if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ID)
                    && !adminProfile) {
                    context.setAttribute(PortalManagerImpl.ATTRIBUTE_PORTAL_ROLE, role);
                    context.setAttribute(PortalManagerImpl.ATTRIBUTE_PORTAL_ID, id);
                }
            } finally {
                this.getTransactionManager().stopWritingTransaction(context);
            }// end synchronized
        } catch (javax.xml.transform.TransformerException local) {
            throw new ProcessingException("TransformerException: " + local, local);
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END buildProfile");
        }
    }

    /**
     * Build the profile delta
     */
    private DocumentFragment buildProfileDelta(String type,
                                               String role,
                                               String id,
                                               boolean adminProfile)
    throws SAXException, ProcessingException, IOException, javax.xml.transform.TransformerException {
        // calling method must be synchronized
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END buildProfileDeltaN type="+type+", role="+role+", id="+id);
        }

        Map    originalProfile;
        Map    baseProfile;
        String baseType, baseRole, baseID, rootElementName;
        DocumentFragment originalFragment;
        DocumentFragment delta;
        SessionContext context = this.getContext(true);

        originalProfile = this.retrieveProfile(this.getProfileID(type, role, id, adminProfile));
        if (originalProfile == null) {
            throw new ProcessingException("buildProfileDelta: no profile found for " +
                   type + " - " + role + " - " + id + ".");
        }

        if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ID)) {
            baseType = PortalManagerImpl.BUILDTYPE_VALUE_ROLE;
            baseRole = role;
            baseID   = null;
            rootElementName = "user-delta";
        } else if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ROLE)) {
            baseType = PortalManagerImpl.BUILDTYPE_VALUE_GLOBAL;
            baseRole = null;
            baseID   = null;
            rootElementName = "role-delta";
        } else if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_GLOBAL)) {
            baseType = PortalManagerImpl.BUILDTYPE_VALUE_BASIC;
            baseRole = null;
            baseID   = null;
            rootElementName = "global-delta";
        } else {
            throw new ProcessingException("buildProfileDelta: type '"+type+"' not allowed.");
        }

        // the profile is created as we dont want to use any memory representation!
        this.createProfile(context, baseType, baseRole, baseID, adminProfile);
        baseProfile = this.retrieveProfile(this.getProfileID(baseType, baseRole, baseID, adminProfile));
        if (baseProfile == null) {
            throw new ProcessingException("buildProfileDelta: no baseProfile found.");
        }

        originalFragment = (DocumentFragment)originalProfile.get(PortalConstants.PROFILE_PROFILE);
        delta = originalFragment.getOwnerDocument().createDocumentFragment();
        delta.appendChild(delta.getOwnerDocument().createElementNS(null, rootElementName));

        // Copy portal content
        Node profileDelta = DOMUtil.getFirstNodeFromPath(originalFragment, new String[] {"profile","portal-profile"}, false).cloneNode(true);
        delta.getFirstChild().appendChild(profileDelta);

        // Diff layout profile, coplet profile, personal profile but not status profile!
        this.diff(originalFragment,
                 (DocumentFragment)baseProfile.get(PortalConstants.PROFILE_PROFILE),
                  "profile/layout-profile",
                  (Element)delta.getFirstChild());
        this.diff(originalFragment,
                  (DocumentFragment)baseProfile.get(PortalConstants.PROFILE_PROFILE),
                  "profile/coplets-profile",
                  (Element)delta.getFirstChild());
        if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_GLOBAL)) {
            profileDelta = DOMUtil.getFirstNodeFromPath(originalFragment, new String[] {"profile","personal-profile"}, false).cloneNode(true);
            delta.getFirstChild().appendChild(profileDelta);
        } else {
            this.diff(originalFragment,
                  (DocumentFragment)baseProfile.get(PortalConstants.PROFILE_PROFILE),
                  "profile/personal-profile",
                  (Element)delta.getFirstChild());
        }

        // check for the highes coplet number
        Node[] miscNodes = (Node[])originalProfile.get(PortalConstants.PROFILE_MISC_POINTER);
        Element lastCoplet = (Element)miscNodes[PortalConstants.PROFILE_MISC_LAST_COPLET_NODE];
        if (lastCoplet != null) {
            String lastNumber = lastCoplet.getAttributeNS(null, "number");
            if (lastNumber != null) {
                int value = new Integer(lastNumber).intValue();
                if (value > 1000000) {
                    NodeList coplets = DOMUtil.selectNodeList(delta, "profile/portal-profile/descendant::coplet[@id and @number]", this.xpathProcessor);
                    if (coplets != null) {
                        Element copletNode;
                        String oldNumber;
                        String copletId;
                        Element statusNode;
                        boolean copletsChanged = false;
                        for(int i=0; i <coplets.getLength(); i++) {
                            copletNode = (Element)coplets.item(i);
                            oldNumber = copletNode.getAttributeNS(null, "number");
                            copletId = copletNode.getAttributeNS(null, "id");
                            statusNode = (Element)DOMUtil.getSingleNode(delta, "status-profile/customization/coplet[@id='"+copletId+"' and @number='"+oldNumber+"']", this.xpathProcessor);
                            copletNode.setAttributeNS(null, "number", ""+(i+1));
                            if (statusNode != null) {
                                statusNode.setAttributeNS(null, "number", ""+(i+1));
                                copletsChanged = true;
                            }
                        }
                        if (copletsChanged) {
                            this.saveUserStatusProfile(originalProfile,
                                   this.getConfiguration(), role, id, adminProfile);
                        }
                    }
                }
            }
        }

        // Last part: strip type information
        NodeList typeElements = DOMUtil.selectNodeList(delta, "descendant::*[@formpath and @formdescription and @formtype]", this.xpathProcessor);
        if (typeElements != null) {
            for(int i = 0; i < typeElements.getLength(); i++) {
                ((Element)typeElements.item(i)).removeAttributeNS(null, "formpath");
                ((Element)typeElements.item(i)).removeAttributeNS(null, "formdescription");
                ((Element)typeElements.item(i)).removeAttributeNS(null, "formtype");
            }
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END buildProfileDelta delta="+delta);
        }
        return delta;
    }

    /**
     * Make the difference :-)
     */
    private void diff(DocumentFragment original,
                      DocumentFragment base,
                      String           path,
                      Element          deltaElement)
    throws SAXException, javax.xml.transform.TransformerException {
        // calling method is already synchronized
        Element originalRoot = (Element)DOMUtil.getSingleNode(original, path, this.xpathProcessor);
        Element baseRoot = (Element)DOMUtil.getSingleNode(base, path, this.xpathProcessor);
        if (originalRoot != null && baseRoot != null) {
            List nodeStack = new ArrayList();
            String name = baseRoot.getNodeName();
            name = name.substring(0, name.indexOf("-profile")) + "-delta";
            nodeStack.add(originalRoot.getOwnerDocument().createElementNS(null, name));

            this.diffNode(baseRoot, originalRoot, nodeStack, deltaElement);
        }
    }

    /**
     * Diff one node
     */
    private void diffNode(Element baseNode,
                          Element originalNode,
                          List nodeStack,
                          Element deltaElement)
    throws SAXException, javax.xml.transform.TransformerException {
        // calling method is already synchronized
        NodeList baseChilds;
        NodeList originalChilds;
        int      i, len;
        int      m, l;
        boolean  found;
        Node     currentOrigNode = null;
        Node     currentBaseNode = null;

        originalChilds = originalNode.getChildNodes();
        len = originalChilds.getLength();
        baseChilds = baseNode.getChildNodes();
        l = baseChilds.getLength();

        for(i = 0; i < len; i++) {
            currentOrigNode = originalChilds.item(i);
            if (currentOrigNode.getNodeType() == Node.ELEMENT_NODE) {

                // search the delta node in the profile
                m = 0;
                found = false;
                while (!found && m < l) {
                    currentBaseNode = baseChilds.item(m);
                    if (currentBaseNode.getNodeType() == Node.ELEMENT_NODE
                        && currentBaseNode.getNodeName().equals(currentOrigNode.getNodeName()) ) {

                        // now we have found a node with the same name
                        // next: the attributes must match also
                        found = this.compareAttributes(currentBaseNode, currentOrigNode);
                    }
                    if (!found) m++;
                }

                if (found) {
                    // do we have elements as children or text?
                    currentOrigNode.normalize();
                    if (currentOrigNode.hasChildNodes()) {

                        // do a recursive call for sub elements
                        nodeStack.add(currentOrigNode);
                        this.diffNode((Element)currentBaseNode,
                                      (Element)currentOrigNode,
                                      nodeStack,
                                      deltaElement);

                        // and now compare the text nodes
                        String baseString = DOMUtil.getValueOfNode(currentBaseNode, "").trim();
                        String originalString = DOMUtil.getValueOfNode(currentOrigNode, "").trim();

                        if (!baseString.equals(originalString)) {
                            // this is the tricky part:
                            // we have to process all nodes on the stack
                            // and insert them in the deltaElement
                            Element currentElement;
                            Element contextElement = deltaElement;
                            NodeList possibleChilds;
                            boolean  foundChild;
                            int      cIndex;

                            for(int p = 0; p < nodeStack.size(); p++) {
                                currentElement = (Element)nodeStack.get(p);
                                possibleChilds = DOMUtil.getNodeListFromPath(contextElement, new String[] {currentElement.getNodeName()});
                                foundChild = false;
                                cIndex = 0;
                                if (possibleChilds != null) {
                                    while (!foundChild && cIndex < possibleChilds.getLength()) {
                                        foundChild = this.compareAttributes(currentElement, possibleChilds.item(cIndex));
                                        if (!foundChild) cIndex++;
                                    }
                                }
                                if (foundChild) {
                                    contextElement = (Element)possibleChilds.item(cIndex);
                                } else {
                                    currentElement = (Element)currentElement.cloneNode(false);
                                    contextElement.appendChild(currentElement);
                                    contextElement = currentElement;
                                }
                            }
                            // now add the text
                            contextElement.appendChild(contextElement.getOwnerDocument().createTextNode(originalString));
                        }

                        nodeStack.remove(nodeStack.size()-1);
                    }
                }
            }

        }

    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.portal.components.PortalManager#getProfileID(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public String getProfileID(String type,
                                String role,
                                String id,
                                boolean adminProfile) 
    throws ProcessingException {
        // No sync required
        this.setup();
        StringBuffer key = new StringBuffer((adminProfile ? "aprofile:" : "uprofile:"));
        RequestState reqstate = this.getRequestState();
        key.append(reqstate.getHandlerName())
           .append('|')
           .append(reqstate.getApplicationName())
           .append(':')
           .append(type);

        if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ROLE)
            || type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ID)) {
            role = XMLUtil.encode(role);
            key.append('_').append(role.length()).append('_').append(role);
        }
        if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ID)) {
            id = XMLUtil.encode(id);
            key.append('_').append(id);
        }
        return key.toString();
    }

    /**
     * Get the profile role from the key
     */
    private boolean getIsAdminProfile(String profileID) {
        // No sync required
        return profileID.startsWith("a");
    }

    /**
     * Get the profile role from the key
     */
    private String getRole(String profileID) {
        // No sync required
        profileID = XMLUtil.decode(profileID);
        int pos = profileID.indexOf('_');
        if (pos == -1) {
            return null;
        } else {
            String lastPart = profileID.substring(pos+1);
            pos = lastPart.indexOf('_');
            if (pos == -1) return null;
            int len = new Integer(lastPart.substring(0, pos)).intValue();
            lastPart = lastPart.substring(pos+1, pos+1+len);
            return lastPart;
        }
    }

    /**
     * Get the profile ID from the key
     */
    private String getID(String profileID) {
        // No sync required
        profileID = XMLUtil.decode(profileID);
        int pos = profileID.indexOf('_');
        if (pos == -1) {
            return null;
        } else {
            String lastPart = profileID.substring(pos+1);
            pos = lastPart.indexOf('_');
            if (pos == -1) {
                return null;
            } else {
                lastPart = lastPart.substring(pos+1);
                pos = lastPart.indexOf('_');
                if (pos == -1) {
                    return null;
                } else {
                    return lastPart.substring(pos+1);
                }
            }
        }
    }

    /**
     * Get the profile type from the key
     */
    private String getType(String profileID) {
        // No sync required
        profileID = XMLUtil.decode(profileID);
        int endPos = profileID.indexOf('_');
        if (endPos == -1) {
            int startPos = profileID.lastIndexOf(':');
            return profileID.substring(startPos+1);
        } else {
            int startPos = profileID.lastIndexOf(':', endPos);
            return profileID.substring(startPos+1, endPos);
        }
    }

    /**
     * Store the profil
     */
    private void storeProfile(String profileID,
                              Map profile)
    throws ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN storeProfile id="+profileID+", profile="+profile);
        }

        Session session = this.getSessionManager().getSession(true);
        synchronized(session) {
            session.setAttribute(profileID, profile);
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END storeProfile");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.portal.components.PortalManager#retrieveProfile(java.lang.String)
     */
    public Map retrieveProfile(String profileID)
    throws ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN retrieveProfile id="+profileID);
        }
        this.setup();
        Session session = this.getSessionManager().getSession(true);
        Map result;
        synchronized(session) {
            result = (Map)session.getAttribute(profileID);
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END retrieveProfile profile="+(result != null ? "**PROFILE**" : "null"));
        }

        return result;
    }

    /**
     * Cache the profile (if cache is turned on)
     */
    private void cacheProfile(String profileID,
                              Map profile,
                              Map configuration) {
        // synchronized
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN cacheProfile id="+profileID+", profile="+profile);
        }
        try {
            if (configuration != null && !this.getIsAdminProfile(profileID)) {
                String storePrefix = (String)configuration.get(PortalConstants.CONF_PROFILE_CACHE);
                if (storePrefix != null) {
                    String key = profileID.substring(1);
                    this.getProfileStore().store(key, profile);
                }
            }
        } catch (Exception local) {
            this.getLogger().warn("Caching Profile failed.", local);
            // local exceptions are ignored
            // we dont want to get an exception response due to cache problems
        }
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END cacheProfile");
        }
    }

    /**
     * Retrieve the cached profil if available
     */
    private Map getCachedProfile(String profileID, Map configuration) {
        // synchronized
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN getCachedProfile id="+profileID);
        }

        Map result = null;

        try {
            if (configuration != null && !this.getIsAdminProfile(profileID)) {
                final String storePrefix = (String)configuration.get(PortalConstants.CONF_PROFILE_CACHE);
                if (storePrefix != null) {
                    final String key = profileID.substring(1);
                    final Store store = this.getProfileStore();
                    if (store.containsKey(key)) {
                        result = (Map)store.get(key);
                    }
                }
            }
        } catch (Exception local) {
            // local exceptions are ignored
            // we dont want to get an exception response due to cache problems
            this.getLogger().warn("Getting cached Profile failed.", local);
            result = null;
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END getCachedProfile profile="+(result != null ? "**PROFILE**" : "null"));
        }
        return result;
    }

    /**
     * Clean up the cache, if the global profile was saved, delete all role and user profiles.
     * If a role profile was saved delete all user profiles. If the basic profile was
     * saved delete all profiles.
     */
    private void cleanUpCache(String type, String role, Map configuration)
    throws ProcessingException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN cleanUpCache type="+type+", role="+role+", config="+configuration);
        }
        if (configuration != null
            && type != null
            && !type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ID)) {
            String storePrefix = (String)configuration.get(PortalConstants.CONF_PROFILE_CACHE);
            if (storePrefix != null) {
                Store store = this.getProfileStore();
                Enumeration keys = store.keys();
                String   currentKey;
                String  deleteGlobal = null;
                String  deleteRole = null;
                String  deleteUser = null;

                if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_BASIC) ||
                    type.equals(PortalManagerImpl.BUILDTYPE_VALUE_GLOBAL)) {
                    if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_BASIC)) {
                        deleteGlobal = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_GLOBAL, null, null, false).substring(1);
                    }
                    deleteRole = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_GLOBAL, null, null, false);
                    deleteRole = deleteRole.substring(1, deleteRole.lastIndexOf(':')+1) + PortalManagerImpl.BUILDTYPE_VALUE_ROLE;
                    deleteUser = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_GLOBAL, null, null, false);
                    deleteUser = deleteUser.substring(1, deleteUser.lastIndexOf(':')+1) + PortalManagerImpl.BUILDTYPE_VALUE_ID;
                } else { // role
                    deleteGlobal = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_ROLE, role, null, false).substring(1);
                    deleteUser = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_ID, role, "a", false);
                    deleteUser = deleteUser.substring(1, deleteUser.length()-1);
                }

                while (keys.hasMoreElements()) {
                    Object k = keys.nextElement();
                    if ( k instanceof String ) {
                        currentKey = (String)k;
                        if (deleteGlobal != null && currentKey.equals(deleteGlobal)) {
                            store.remove(currentKey);
                        } else if (deleteRole != null && currentKey.startsWith(deleteRole)) {
                            store.remove(currentKey);
                        } else if (deleteUser != null && currentKey.startsWith(deleteUser)) {
                            store.remove(currentKey);
                        }
                    }
                }
            }
        } else if (configuration != null && type == null) {
            // clean whole cache
            String storePrefix = (String)configuration.get(PortalConstants.CONF_PROFILE_CACHE);
            if (storePrefix != null) {
                Store store = this.getProfileStore();
                Enumeration keys = store.keys();
                String currentKey;
                String delete;

                delete = this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_GLOBAL, null, null, false);
                delete = delete.substring(1, delete.lastIndexOf(':') + 1);
                while (keys.hasMoreElements()) {
                    Object k = keys.nextElement();
                    if ( k instanceof String ) {
                        currentKey = (String)k;
                        if (currentKey.startsWith(delete)) {
                            store.remove(currentKey);
                        }
                    }
                }
            }
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END cleanUpCache");
        }
    }

    /**
     * Build the run profil and store it in the <code>profileMap</code>.
     */
    private void buildRunProfile(Map              profileMap,
                                 SessionContext  context,
                                 DocumentFragment baseProfile)
    throws ProcessingException, javax.xml.transform.TransformerException {
        // calling method is synced
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN buildRunProfile context="+context+", profile="+baseProfile);
        }

        // The map containing the coplets which appear on each medium
        Map defaultCoplets = new HashMap(20, 5);
        // The map containing for each media type a map with coplets which
        // appear only for the given media
        Map mediaCoplets   = new HashMap(5, 2);

        profileMap.put(PortalConstants.PROFILE_DEFAULT_COPLETS, defaultCoplets);
        profileMap.put(PortalConstants.PROFILE_MEDIA_COPLETS, mediaCoplets);

        // get AuthenticationManager instance
        String[] types = this.getMediaManager().getMediaTypes();
        Map      mediaMap;
        for(int i = 0; i < types.length; i++) {
            mediaCoplets.put(types[i], new HashMap(5, 3));
        }

        // build misc nodes
        Node[] miscNodes = new Node[13];
        miscNodes[PortalConstants.PROFILE_MISC_HEADER_NODE] = DOMUtil.getFirstNodeFromPath(baseProfile, new String[] {"profile","layout-profile","portal","header","exists"}, false);
        miscNodes[PortalConstants.PROFILE_MISC_FOOTER_NODE] = DOMUtil.getFirstNodeFromPath(baseProfile, new String[] {"profile","layout-profile","portal","footer","exists"}, false);
        miscNodes[PortalConstants.PROFILE_MISC_HEADER_CONTENT_NODE] = DOMUtil.getFirstNodeFromPath(baseProfile, new String[] {"profile","portal-profile","content","header"}, false);
        miscNodes[PortalConstants.PROFILE_MISC_FOOTER_CONTENT_NODE] = DOMUtil.getFirstNodeFromPath(baseProfile, new String[] {"profile","portal-profile","content","footer"}, false);
        miscNodes[PortalConstants.PROFILE_MISC_COLUMNS_NODE]= DOMUtil.getFirstNodeFromPath(baseProfile, new String[] {"profile","layout-profile","portal","columns","number"}, false);
        miscNodes[PortalConstants.PROFILE_MISC_MESSAGES_NODE]= DOMUtil.getFirstNodeFromPath(baseProfile, new String[] {"profile","personal-profile","messages"}, false);
        for(int i = 1; i <= PortalConstants.MAX_COLUMNS; i++) {
            miscNodes[7 + i] = DOMUtil.getSingleNode(baseProfile,
                "profile/portal-profile/content/column[@position='"+i+"']", this.xpathProcessor);
        }

        profileMap.put(PortalConstants.PROFILE_MISC_POINTER, miscNodes);

        // build coplet configs
        NodeList coplets;
        int      i, l;
        Element  configElement;
        String   copletID;
        String   copletMedia;

        coplets = DOMUtil.getNodeListFromPath(baseProfile, new String[] {"profile","coplets-profile","coplets","coplet"});

        if (coplets != null) {

            l = coplets.getLength();
            for(i = 0; i < l; i++) {
                configElement = (Element)coplets.item(i);
                if (DOMUtil.getValueAsBooleanOf(configElement, "configuration/active", this.xpathProcessor)) {

                    copletID = configElement.getAttributeNS(null, "id");
                    if (configElement.hasAttributeNS(null, "media")) {
                        copletMedia = configElement.getAttributeNS(null, "media");
                        mediaMap = (Map)mediaCoplets.get(copletMedia);
                        if (mediaMap != null) {
                            mediaMap.put(copletID, configElement);
                        }
                    } else {
                        copletMedia = null;
                        defaultCoplets.put(copletID, configElement);
                    }

                    // Now: add the coplet if mandatory and missing
                    if (DOMUtil.getValueAsBooleanOf(configElement, "configuration/mandatory", this.xpathProcessor)) {
                        // get all coplet instances
                        NodeList copletElements;

                        // the next is crap, but it works....
                        // search all coplets (columns, header, footer)
                        if (copletMedia == null) {
                            copletElements = DOMUtil.selectNodeList(baseProfile,
                                "profile/portal-profile/content/column/coplets/coplet[@id='"+copletID+"' and not(@media)]", this.xpathProcessor);
                        } else {
                            copletElements = DOMUtil.selectNodeList(baseProfile,
                                "profile/portal-profile/content/column/coplets/coplet[@id='"+copletID+"' and media='"+copletMedia+"']", this.xpathProcessor);
                        }

                        if (copletElements == null || copletElements.getLength() == 0) {
                            if (copletMedia == null) {
                                copletElements = DOMUtil.selectNodeList(baseProfile,
                                   "profile/portal-profile/content/header/coplet[@id='"+copletID+"' and not(@media)]", this.xpathProcessor);
                            } else {
                                copletElements = DOMUtil.selectNodeList(baseProfile,
                                   "profile/portal-profile/content/header/coplet[@id='"+copletID+"' and media='"+copletMedia+"']", this.xpathProcessor);
                            }
                        }

                        if (copletElements == null || copletElements.getLength() == 0) {
                            if (copletMedia == null) {
                                copletElements = DOMUtil.selectNodeList(baseProfile,
                                   "profile/portal-profile/content/footer/coplet[@id='"+copletID+"' and not(@media)]", this.xpathProcessor);
                            } else {
                                copletElements = DOMUtil.selectNodeList(baseProfile,
                                   "profile/portal-profile/content/footer/coplet[@id='"+copletID+"' and media='"+copletMedia+"']", this.xpathProcessor);
                            }
                        }

                        if (copletElements == null || copletElements.getLength() == 0) {
                            // mandatory coplet is not configured, so add it to the first column
                            Node content = DOMUtil.getSingleNode(baseProfile,
                                  "profile/portal-profile/content/column[@position='1']/coplets", this.xpathProcessor);
                            if (content == null)
                                throw new ProcessingException("Element not found: portal-profile/content/column/coplets");
                            Element el = content.getOwnerDocument().createElementNS(null, "coplet");
                            el.setAttributeNS(null, "id", copletID);
                            if (copletMedia != null) {
                                el.setAttributeNS(null, "media", copletMedia);
                            }
                            // Set position attribute
                            NodeList childs = DOMUtil.getNodeListFromPath(content, new String[] {"coplet"});
                            int      childsCount = (childs == null ? 0 : childs.getLength());
                            el.setAttributeNS(null, "position", ""+(childsCount+1));
                            Text    t;
                            content.appendChild(el);
                            content = el;
                            el = content.getOwnerDocument().createElementNS(null, "status");
                            content.appendChild(el);
                            content = el;
                            el = content.getOwnerDocument().createElementNS(null, "visible");
                            content.appendChild(el);
                            content = el;
                            t = content.getOwnerDocument().createTextNode("true");
                            content.appendChild(t);
                        } else {
                            // is any of them visible?
                            boolean found;
                            boolean origVisible = DOMUtil.getValueAsBooleanOf(configElement, "status/visible", this.xpathProcessor);
                            int si, sl;
                            sl = copletElements.getLength();
                            si = 0;
                            found = false;
                            while (si < sl && !found) {
                                found = DOMUtil.getValueAsBooleanOf(copletElements.item(si),
                                           "status/visible", origVisible, this.xpathProcessor);
                                si++;
                            }
                            if (!found) {
                                // set first to visible
                                // first: is status node available
                                Node statusElem = DOMUtil.getFirstNodeFromPath(copletElements.item(0), new String[] {"status"}, false);
                                if (statusElem == null) {
                                    statusElem = copletElements.item(0).getOwnerDocument().createElementNS(null, "status");
                                    copletElements.item(0).appendChild(statusElem);
                                }
                                // second: is visible node available
                                Node visibleElem = DOMUtil.getFirstNodeFromPath(statusElem, new String[] {"visible"}, false);
                                if (visibleElem == null) {
                                    visibleElem = statusElem.getOwnerDocument().createElementNS(null, "visible");
                                    statusElem.appendChild(visibleElem);
                                }
                                // remove old childs
                                while (visibleElem.hasChildNodes()) {
                                    visibleElem.removeChild(visibleElem.getFirstChild());
                                }
                                visibleElem.appendChild(statusElem.getOwnerDocument().createTextNode("true"));
                            }
                        }
                    }
                }
            }
        }

        // Numerate all coplets by adding an attribute number with a unique value
        // and put them into the corresponding maps.
        // update the status section of the coplet: Only the values of the coplet
        // configuration are allowed. Not less and not more!
        // All coplets are required to have
        // the number attribute! So this is only a compatibility function
        // which adds the first time the number to the coplets
        // If the number attribute is available, the node with the highest
        // number is searched
        NodeList copletElements;
        int     number = 0;
        Element  content = (Element)DOMUtil.getFirstNodeFromPath(baseProfile,
                       new String[] {"profile","portal-profile","content"}, false);
        Element  currentCoplet;
        NodeList statusConfigList;
        NodeList statusCopletList;
        Element  statusCopletElement;
        int      list_index, list_length;
        Node     currentStatus;
        int      highestCopletNumber = -1;

        for(i = 0; i < 7; i++) {
            if (i == 0) {
                copletElements = DOMUtil.getNodeListFromPath(content,
                   new String[] {"header","coplet"});
            } else if (i == 1) {
                copletElements = DOMUtil.getNodeListFromPath(content,
                   new String[] {"footer","coplet"});
            } else {
                copletElements = DOMUtil.selectNodeList(content,
                   "column[@position='"+(i-1)+"']/coplets/coplet", this.xpathProcessor);
            }
            if (copletElements != null && copletElements.getLength() > 0) {
                Element[] list = new Element[copletElements.getLength()];
                for(int index = 0; index < copletElements.getLength(); index++) {
                    list[index] = (Element)copletElements.item(index);
                }

                for(int index = 0; index < list.length; index++) {
                    // get coplet element
                    currentCoplet = list[index];

                    String numberValue = currentCoplet.getAttributeNS(null, "number");
                    if (numberValue == null || numberValue.equals("")) {
                        // create unique number attribute
                        currentCoplet.setAttributeNS(null, "number", ""+number);
                        miscNodes[PortalConstants.PROFILE_MISC_LAST_COPLET_NODE] = currentCoplet;
                        number++;
                    } else {
                        int currentNumber = new Integer(numberValue).intValue();
                        if (currentNumber > highestCopletNumber) {
                            highestCopletNumber = currentNumber;
                            number = highestCopletNumber+1;
                            miscNodes[PortalConstants.PROFILE_MISC_LAST_COPLET_NODE] = currentCoplet;
                        }
                    }
                    // update status
                    configElement = this.getCopletConfiguration(currentCoplet.getAttributeNS(null, "id"),
                                                      defaultCoplets,
                                                      mediaCoplets);
                    if (configElement != null) {
                        statusCopletElement = (Element)DOMUtil.selectSingleNode(configElement, "status", this.xpathProcessor);
                        statusConfigList = DOMUtil.selectNodeList(statusCopletElement, "*", this.xpathProcessor);
                        statusCopletList = DOMUtil.selectNodeList(currentCoplet, "status/*", this.xpathProcessor);
                        // first test if each status is included in the config
                        if (statusCopletList != null) {
                            list_length = statusCopletList.getLength();
                            for(list_index = list_length-1; list_index >= 0; list_index--) {
                                currentStatus = statusCopletList.item(list_index);
                                if (currentStatus.getNodeType() == Node.ELEMENT_NODE) {
                                    if (DOMUtil.getFirstNodeFromPath(configElement, new String[] {"status", currentStatus.getNodeName()}, false) == null) {
                                        currentStatus.getParentNode().removeChild(currentStatus);
                                    }
                                }
                            }
                        }
                        // second, test if each status attribute of the config is included
                        if (statusConfigList != null) {
                            list_length = statusConfigList.getLength();
                            for(list_index = 0; list_index < list_length; list_index++) {
                                currentStatus = statusConfigList.item(list_index);
                                if (currentStatus.getNodeType() == Node.ELEMENT_NODE) {
                                    if (DOMUtil.getFirstNodeFromPath(statusCopletElement, new String[] {currentStatus.getNodeName()}, false) == null) {
                                        // create a new element
                                        statusCopletElement.appendChild(statusCopletElement.getOwnerDocument().importNode(currentStatus, true));
                                    }
                                }
                            }
                        }
                    } else {
                        // coplet not in configuration
                        // adopt position of following coplets and then remove
                        String posAttr = currentCoplet.getAttributeNS(null, "position");
                        NodeList followUps = DOMUtil.selectNodeList(currentCoplet.getParentNode(), "coplet[@position > '"+posAttr+"']", this.xpathProcessor);
                        if (followUps != null) {
                            int value;
                            for(int iq = 0; iq < followUps.getLength(); iq++) {
                                value = new Integer(((Element)followUps.item(iq)).getAttributeNS(null, "position")).intValue();
                                value -= 1;
                                ((Element)followUps.item(iq)).setAttributeNS(null, "position", "" + value);
                            }
                        }
                        currentCoplet.getParentNode().removeChild(currentCoplet);
                    }
                }
            }
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END buildRunProfile");
        }
    }

    /**
     *  Add the type information to the profile and do some type checkings
     */
    private void buildTypeProfile(Map              theProfile,
                                  SessionContext  context,
                                  DocumentFragment baseProfile)
    throws javax.xml.transform.TransformerException {
        // calling method is synced
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN buildTypeProfile context="+context+", profile="+baseProfile);
        }
        List list = new ArrayList(25);
        List confList = new ArrayList(25);

        theProfile.put(PortalConstants.PROFILE_TYPE_PATHS, list);
        theProfile.put(PortalConstants.PROFILE_TYPE_CONF_PATHS, confList);

        Element typeElement;

        typeElement = (Element)DOMUtil.getFirstNodeFromPath(baseProfile, new String[] {"profile","type-profile","elements"}, false);
        if (typeElement != null) {
            if (typeElement.hasChildNodes())
                this.addTypePath(list, typeElement.getChildNodes(), "profile");

            // now we have the list with the xpaths
            this.setTypeInfo(baseProfile, list, null);

            // build the conf paths
            int i, l, pos;
            String current;

            l = list.size();
            for(i = 0; i < l; i++) {
                current = (String)list.get(i);

                // now the path has to be changed: the new attributes must be included
                pos = current.lastIndexOf('/');
                current = current.substring(0, pos);

                pos = current.lastIndexOf('[');
                if (current.substring(pos+1).equals("not(@*)]")) {
                    current = current.substring(0, pos+1);
                } else {
                    current = current.substring(0, current.length()-1) + " and ";
                }
                current += "@formtype and @formpath and @formdescription]";
                confList.add(current);
            }

        }

        // and now the type checking part:
        //
        // If the default layout has changed the number of columns and the current
        // user (or role) is not allowed to change this, we have to adjust the
        // profile. Otherwise the current number of columns has to be stored
        // into the profile layout part.
        Element layoutColumnsNode = (Element)DOMUtil.getFirstNodeFromPath(baseProfile, new String[] {"profile","layout-profile","portal","columns","number"}, false);
        String layoutValue = DOMUtil.getValueOfNode(layoutColumnsNode);
        int layoutColumns = 0;
        if (layoutValue != null && new Integer(layoutValue).intValue() > 0) {
            layoutColumns = new Integer(layoutValue).intValue();
        }
        NodeList columnNodes = DOMUtil.selectNodeList(baseProfile, "profile/portal-profile/content/column[@position]", this.xpathProcessor);
        int columns = columnNodes.getLength();
        if (columns != layoutColumns) {
            if (layoutColumnsNode.hasAttributeNS(null, "formtype")) {
                DOMUtil.setValueOfNode(layoutColumnsNode, ""+columns);
            } else {
                this.changeColumns(baseProfile,
                                   columns,
                                   layoutColumns,
                                   (Node[])theProfile.get(PortalConstants.PROFILE_MISC_POINTER));
                this.setTypeInfo(baseProfile,
                                 (List)theProfile.get(PortalConstants.PROFILE_TYPE_PATHS),
                                 (List)theProfile.get(PortalConstants.PROFILE_TYPE_CONF_PATHS));
            }
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END buildTypeProfile");
        }
    }

    /**
     * Set the tpe information
     */
    private void setTypeInfo(DocumentFragment baseProfile, List paths, List confPaths)
    throws javax.xml.transform.TransformerException {
        // calling method is synced
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN setTypeInfo profile="+baseProfile+", paths="+paths);
        }
        if (baseProfile != null && paths != null) {
            int pos;
            String currentPath;
            String value;
            String description;
            NodeList nodes;
            int nodes_count;
            int path_count = paths.size();
            Node currentNode;

            for(int i = 0; i < path_count; i++) {
                currentPath = (String)paths.get(i);
                pos = currentPath.lastIndexOf('/');
                value = currentPath.substring(pos + 1);
                currentPath = currentPath.substring(0, pos);
                pos = value.indexOf("|");
                if (pos != -1) {
                    description = value.substring(pos + 1);
                    value = value.substring(0, pos);
                } else {
                    description = "UNKNOWN";
                }

                // get all nodes
                boolean changed = false;
                nodes = DOMUtil.selectNodeList(baseProfile, currentPath, this.xpathProcessor);
                if (nodes != null) {
                    nodes_count = nodes.getLength();
                    for(int m = 0; m < nodes_count; m++) {
                        currentNode = nodes.item(m);
                        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                            ((Element)currentNode).setAttributeNS(null, "formtype", value);
                            ((Element)currentNode).setAttributeNS(null, "formpath",
                                     PortalManagerImpl.REQ_PARAMETER_CONF + '.' + i + '.' + m);
                            ((Element)currentNode).setAttributeNS(null, "formdescription", description);
                            changed = true;
                        }
                    }
                }
                if (changed && confPaths != null) {
                    currentPath = (String)confPaths.get(i);
                    nodes = DOMUtil.selectNodeList(baseProfile, currentPath, this.xpathProcessor);
                    if (nodes != null) {
                        nodes_count = nodes.getLength();
                        for(int m = 0; m < nodes_count; m++) {
                            currentNode = nodes.item(m);
                            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                                ((Element)currentNode).setAttributeNS(null, "formpath",
                                     PortalManagerImpl.REQ_PARAMETER_CONF + '.' + i + '.' + m);
                            }
                        }
                    }
                }
            }
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END setTypeInfo");
        }
    }


    /**
     * Add the type info to the xpath. This is done recursevly
     */
    private void addTypePath(List list, NodeList childs, String path) {
        // calling method is synced
        int i, l;
        Element current;
        StringBuffer newPath;

        l = childs.getLength();
        for(i = 0; i < l; i++) {
            if (childs.item(i).getNodeType() == Node.ELEMENT_NODE) {
                current = (Element)childs.item(i);
                newPath = new StringBuffer(path);
                newPath.append('/').append(current.getNodeName());
                if (current.hasAttributes()) {
                    NamedNodeMap nnm = current.getAttributes();
                    int ia, la;
                    boolean first = true;
                    StringBuffer expression = new StringBuffer();
                    la = nnm.getLength();
                    newPath.append('[');
                    for(ia = 0; ia < la; ia++) {
                        if (!nnm.item(ia).getNodeName().equals("type")
                            && !nnm.item(ia).getNodeName().equals("description")) {
                            if (!first) expression.append(" and ");
                            if (!nnm.item(ia).getNodeValue().equals("*")) {
                                expression.append('@')
                                  .append(nnm.item(ia).getNodeName())
                                  .append("='")
                                  .append(nnm.item(ia).getNodeValue())
                                  .append("'");
                            } else {
                                expression.append('@').append(nnm.item(ia).getNodeName());
                            }
                            first = false;
                        }
                    }
                    if (first) {
                        newPath.append("not(@*)");
                    } else {
                        newPath.append(expression);
                    }
                    newPath.append(']');
                } else {
                    newPath.append("[not(@*)]");
                }
                if (current.getAttributeNS(null, "type").length() > 0) {
                    list.add(newPath.toString() + '/' + current.getAttributeNS(null, "type") + '|' + current.getAttributeNS(null, "description"));
                } else {
                    if (current.hasChildNodes()) {
                        this.addTypePath(list, current.getChildNodes(), newPath.toString());
                    }
                }
            }
        }
    }

    /**
     * Build the Map with the portal layouts
     */
    private Map buildPortalLayouts(SessionContext context,
                                 DocumentFragment baseProfile)
    throws ProcessingException, javax.xml.transform.TransformerException {
        // calling method is synced
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN buildPortalLayouts context="+context+", profile="+baseProfile);
        }
        Map layouts = new HashMap(5, 2);
        Element defLayout = (Element)DOMUtil.getSingleNode(baseProfile,
                        "profile/layout-profile/portal/layouts/layout[not(@*)]", this.xpathProcessor);
        Node currentLayout;
        String[] types = this.getMediaManager().getMediaTypes();

        for(int i = 0; i < types.length; i++) {
             currentLayout = DOMUtil.getSingleNode(baseProfile,
               "profile/layout-profile/portal/layouts/layout[media='"+types[i]+"']", this.xpathProcessor);
             layouts.put(types[i], (currentLayout == null ? defLayout : currentLayout));
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END buildPortalLayouts layouts="+layouts);
        }
        return layouts;
    }

    /**
     * Build the Map with the coplet layouts
     */
    private Map buildcopleyLayouts(SessionContext context,
                                 DocumentFragment baseProfile)
    throws ProcessingException, javax.xml.transform.TransformerException {
        // calling method is synced
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN buildcopleyLayouts context="+context+", profile="+baseProfile);
        }
        Map layouts = new HashMap(5, 2);
        Element defLayout = (Element)DOMUtil.getSingleNode(baseProfile,
                        "profile/layout-profile/coplets/layouts/layout[not(@*)]", this.xpathProcessor);
        Node currentLayout;
        String[] types = this.getMediaManager().getMediaTypes();

        for(int i = 0; i < types.length; i++) {
            currentLayout = DOMUtil.getSingleNode(baseProfile,
               "profile/layout-profile/coplets/layouts/layout[media='"+types[i]+"']", this.xpathProcessor);
           layouts.put(types[i], (currentLayout == null ? defLayout : currentLayout));
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END buildcopleyLayouts layouts="+layouts);
        }
        return layouts;
    }

    /**
     * Import a delta into the profile
     */
    private void importProfileDelta(Element          profileRoot,
                                  DocumentFragment delta,
                                  String           deltaRootTagName,
                                  String           deltaTag)
    throws ProcessingException {
        // calling method is synced
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN importProfileDelta root=" + profileRoot + ", delta=" + delta + ", deltaRoot:" + deltaRootTagName + ", delta: " + deltaTag);
        }
        Node     deltaRoot   = null;

        deltaRoot = DOMUtil.getFirstNodeFromPath(delta, new String[] {deltaRootTagName, deltaTag}, false);

        if (deltaRoot != null) {
            // root tag found in delta , now search root tag in profile
            String searchName = deltaRoot.getNodeName().substring(0, deltaRoot.getNodeName().lastIndexOf("-delta"));
            searchName = searchName + "-profile";

            profileRoot = (Element)DOMUtil.getFirstNodeFromPath(profileRoot, new String[] {searchName}, false);
            if (profileRoot == null) {
                throw new ProcessingException("Importing Delta: Tag " + searchName + " not found in profile.");
            }

            // now import it
            this.importNode(profileRoot, (Element)deltaRoot);
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END importProfileDelta");
        }
    }

    /**
     * Add the node to the profile (replace an existing one)
     */
    private void addProfilePart(Element          profileRoot,
                              DocumentFragment delta,
                              String           deltaRootTagName,
                              String           deltaTag)
    {
        // calling method is synced
        if (this.getLogger().isDebugEnabled()) {
           this.getLogger().debug("BEGIN addProfilePart root=" + profileRoot + ", delta=" + delta + ", deltaRoot:" + deltaRootTagName + ", delta: " + deltaTag);
        }
        Node     deltaRoot   = null;
        Node     oldNode     = null;

        if (deltaRootTagName != null) {
            deltaRoot = DOMUtil.getFirstNodeFromPath(delta, new String[] {deltaRootTagName, deltaTag}, false);
        } else {
            deltaRoot = DOMUtil.getFirstNodeFromPath(delta, new String[] {deltaTag}, false);
        }

        if (deltaRoot != null) {
            // root tag found in delta found, now search root tag in profile
            oldNode = DOMUtil.getFirstNodeFromPath(profileRoot, new String[] {deltaTag}, false);
            if (oldNode == null) {
                profileRoot.appendChild(profileRoot.getOwnerDocument().importNode(deltaRoot, true));
            } else {
                profileRoot.replaceChild(profileRoot.getOwnerDocument().importNode(deltaRoot, true), oldNode);
            }
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END addProfilePart");
        }
    }


    /**
     * This is the hardest part. Incorporting a node into the profile.
     * For performance reasons there is now tracing here.
     */
    private void importNode(Element profile, Element delta) {
        // calling method is synced
        NodeList profileChilds = null;
        NodeList deltaChilds   = delta.getChildNodes();
        int      i, len;
        int      m, l;
        boolean  found;
        Node     currentDelta = null;
        Node     currentProfile = null;

        len = deltaChilds.getLength();
        for(i = 0; i < len; i++) {
            currentDelta = deltaChilds.item(i);
            if (currentDelta.getNodeType() == Node.ELEMENT_NODE) {
                // search the delta node in the profile
                profileChilds = profile.getChildNodes();
                l = profileChilds.getLength();
                m = 0;
                found = false;
                while (!found && m < l) {
                    currentProfile = profileChilds.item(m);
                    if (currentProfile.getNodeType() == Node.ELEMENT_NODE
                        && currentProfile.getNodeName().equals(currentDelta.getNodeName())) {

                        // now we have found a node with the same name
                        // next: the attributes must match also
                        found = this.compareAttributes(currentProfile, currentDelta);
                    }
                    if (!found) m++;
                }
                if (found) {
                    // this is not new

                    // do we have elements as children or text?
                    if (currentDelta.hasChildNodes()) {
                        currentDelta.normalize();
                        currentProfile.normalize();
                        // do a recursive call for sub elements
                        this.importNode((Element)currentProfile, (Element)currentDelta);
                        // and now the text nodes: Remove all from the profile and add all
                        // of the delta
                        NodeList childs = currentProfile.getChildNodes();
                        int      index, max;
                        max = childs.getLength();
                        for(index = max - 1; index >= 0; index--) {
                            if (childs.item(index).getNodeType() == Node.TEXT_NODE) {
                                currentProfile.removeChild(childs.item(index));
                            }
                        }
                        childs = currentDelta.getChildNodes();
                        max = childs.getLength();
                        for(index = 0; index < max; index++) {
                            if (childs.item(index).getNodeType() == Node.TEXT_NODE) {
                                currentProfile.appendChild(currentProfile.getOwnerDocument()
                                     .createTextNode(childs.item(index).getNodeValue()));
                            }
                        }
                    }
                } else {
                    // this is a new node, so it is considered as an old information
                    // No inserting: profile.appendChild(profile.getOwnerDocument().importNode(currentDelta, true));
                }
            }

        }

    }

    /**
     * Compare Attributes of two nodes. This method returns true only if both
     * nodes have the same number of attributes and the same attributes with equal
     * values.
     * Namespacedefinition nodes are ignored
     * BUT: For type handling the attributes <code>formtype</code>,
     *      <code>formdescription</code> and <code>formpath</code> are ignored!
     */
    private boolean compareAttributes(Node first, Node second) {
        // calling method is synced
        NamedNodeMap attr1 = first.getAttributes();
        NamedNodeMap attr2 = second.getAttributes();
        String value;

        if (attr1 == null && attr2 == null) return true;
        if (attr1 == null || attr2 == null) return false;
        int attr1Len = (attr1 == null ? 0 : attr1.getLength());
        int attr2Len = (attr2 == null ? 0 : attr2.getLength());
        if (attr1Len > 0) {
            if (attr1.getNamedItemNS(null, "formtype") != null) attr1Len--;
            if (attr1.getNamedItemNS(null, "formpath") != null) attr1Len--;
            if (attr1.getNamedItemNS(null, "formdescription") != null) attr1Len--;
            int l = attr1.getLength();
            for(int i=0;i<l;i++) {
                if (attr1.item(i).getNodeName().startsWith("xmlns:"))
                    attr1Len--;
            }
        }
        if (attr2Len > 0) {
            if (attr2.getNamedItemNS(null, "formtype") != null) attr2Len--;
            if (attr2.getNamedItemNS(null, "formpath") != null) attr2Len--;
            if (attr2.getNamedItemNS(null, "formdescription") != null) attr2Len--;
            int l = attr2.getLength();
            for(int i=0;i<l;i++) {
                if (attr2.item(i).getNodeName().startsWith("xmlns:"))
                    attr2Len--;
            }
        }
        if (attr1Len != attr2Len) return false;
        int i, l;
        int m, l2;
        i = 0;
        l = attr1.getLength();
        l2 = attr2.getLength();
        boolean ok = true;
        // each attribute of first must be in second with the same value
        while (i < l && ok) {
            value = attr1.item(i).getNodeName();
            if (!value.equals("formtype") 
                && !value.equals("formpath") 
                && !value.equals("formdescription") 
                && !value.startsWith("xmlns:")) {
                ok = false;
                m = 0;
                while (m < l2 && !ok) {
                    if (attr2.item(m).getNodeName().equals(value)) {
                        // same name, same value?
                        ok = attr1.item(i).getNodeValue().equals(attr2.item(m).getNodeValue());
                    }
                    m++;
                }

            }
            i++;
        }
        return ok;
    }


    /**
     * Parse the fragment(tree denoted by the element)
     * and include the processed xml in the output
     */
    private void processCopletList(List        copletList,
                                   XMLConsumer consumer,
                                   String      copletNotAvailableMessage,
                                   long        defaultCopletTimeout)
    throws ProcessingException, SAXException, javax.xml.transform.TransformerException {
        // calling method is synced
        for(int i = 0; i < copletList.size(); i++) {
            this.processCoplet((Object[])copletList.get(i),
                         consumer, copletNotAvailableMessage, defaultCopletTimeout);
        }
    }

    /**
     * Parse the fragment(tree denoted by the element)
     * and include the processed xml in the output
     */
    private void loadCoplets(Element element,
                             Map     defaultCoplets,
                             Map     mediaCoplets,
                             List    copletList,
                             boolean parallelCoplets,
                             long    defaultCopletTimeout,
                             Element statusProfile)
    throws ProcessingException, javax.xml.transform.TransformerException {
        // calling method is synced
        // All children, which are coplets are processed, all other tags
        // are ignored
        if (element.hasChildNodes()) {
            NodeList childs = element.getChildNodes();
            Node     current = null;
            int i, l;
            l = childs.getLength();
            for(i = 0; i < l; i++) {
                current = childs.item(i);
                if (current.getNodeType() == Node.ELEMENT_NODE
                    && current.getNodeName().equals("coplet")) {

                    // now we have a coplet
                    this.loadCoplet((Element)current,
                             defaultCoplets,
                             mediaCoplets,
                             copletList,
                             parallelCoplets,
                             defaultCopletTimeout,
                             statusProfile);
                }
            }
        }
    }

    /**
     * Load a coplet and store the binary output in the list
     */
    private void loadCoplet(Element         element,
                            Map             defaultCoplets,
                            Map             mediaCoplets,
                            List            copletList,
                            boolean         parallelCoplets,
                            long            defaultCopletTimeout,
                            Element         statusProfile)
    throws ProcessingException, javax.xml.transform.TransformerException {
        // calling method is synced
        String copletID = element.getAttributeNS(null, "id");

        Element copletConf = this.getCopletConfiguration(copletID, defaultCoplets, mediaCoplets);
        if (copletConf != null) {

             // first: check visibility
            boolean visible = DOMUtil.getValueAsBooleanOf(element,
                "status/visible", this.xpathProcessor);
            // second: check media
            String media = this.getMediaManager().getMediaType();
            if (visible && copletConf.hasAttributeNS(null, "media")) {
                String copletMedia = copletConf.getAttributeNS(null, "media");
                visible = media.equals(copletMedia);
            }

            if (visible) {

                Object[] loadedCoplet = new Object[8];
                copletList.add(loadedCoplet);

                boolean isCustomizable = DOMUtil.getValueAsBooleanOf(copletConf, "configuration/customizable", false, this.xpathProcessor);
                if (isCustomizable) {
                    boolean showCustomizePage = DOMUtil.getValueAsBooleanOf(element, "status/customize", false, this.xpathProcessor);
                    boolean hasConfig = false;
                    if (statusProfile != null) {
                        Element customInfo = (Element)DOMUtil.getSingleNode(statusProfile,
                              "customization/coplet[@id='"+copletID+"' and @number='"+element.getAttributeNS(null, "number")+"']", this.xpathProcessor);
                        hasConfig = (customInfo != null);
                    }
                    if (showCustomizePage || !hasConfig ) {
                        Node node = DOMUtil.selectSingleNode(element, "status/customize", this.xpathProcessor);
                        DOMUtil.setValueOfNode(node, "true");
                    } else {
                        Node node = DOMUtil.selectSingleNode(element, "status/customize", this.xpathProcessor);
                        DOMUtil.setValueOfNode(node, "false");
                    }
                } else {
                    Node node = DOMUtil.selectSingleNode(element, "status/customize", this.xpathProcessor);
                    DOMUtil.setValueOfNode(node, "false");
                }

                // Create the parameters for the coplet:
                //   The <status> part is mapped to parameters
                //   id, number and media are added
                SourceParameters p = DOMUtil.createParameters(DOMUtil.getFirstNodeFromPath(element, new String[] {"status"}, false), null);
                p.setSingleParameterValue(PortalConstants.PARAMETER_ID, copletID);
                p.setSingleParameterValue(PortalConstants.PARAMETER_NUMBER, element.getAttributeNS(null, "number"));
                p.setSingleParameterValue(PortalConstants.PARAMETER_MEDIA, media);
                String isPersistent = DOMUtil.getValueOf(copletConf, "configuration/persistent", "false", this.xpathProcessor);
                p.setSingleParameterValue(PortalConstants.PARAMETER_PERSISTENT, isPersistent);

                // the coplet loading is a tricky part:
                // we create an object array containing all information
                // for later processing of the coplet
                // so the processCoplet() method needs no lookup for information
                // again
                loadedCoplet[0] = null;
                loadedCoplet[1] = copletConf;
                loadedCoplet[2] = p;
                loadedCoplet[3] = element;
                loadedCoplet[4] = new Long(System.currentTimeMillis());
                loadedCoplet[5] = new Long(DOMUtil.getValueOf(copletConf, "configuration/timeout", "-1", this.xpathProcessor));
                loadedCoplet[7] = statusProfile;

                CopletThread copletThread = new CopletThread();
                Thread theThread = new Thread(copletThread);
                loadedCoplet[6] = copletThread;
                copletThread.init(copletID,
                                  ContextHelper.getObjectModel(this.componentContext),
                                  this.getLogger(),
                                  ContextHelper.getResponse(this.componentContext),
                                  loadedCoplet,
                                  this.manager,
                                  this.resolver,
                                  this.xpathProcessor);
                theThread.start();
                Thread.yield();

                if (!parallelCoplets) {
                    copletThread = (CopletThread)loadedCoplet[6];
                    if (copletThread != null) {
                        long startTime = System.currentTimeMillis() - ((Long)loadedCoplet[4]).longValue();
                        long timeout = ((Long)loadedCoplet[5]).longValue();
                        long waitTime;
                        if (timeout == -1) {
                            waitTime = defaultCopletTimeout;
                        } else {
                            waitTime = timeout - startTime;
                        }

                        while (copletThread != null && waitTime > 2) {
                            try {
                                Thread.sleep(15);
                                waitTime -= 15;
                            } catch(InterruptedException local) {
                                // ignore
                            }
                            copletThread = (CopletThread)loadedCoplet[6];
                        }
                        loadedCoplet[6] = null; // mark as loaded
                    }
                }

            }

        }
    }

    /**
     * Process a coplet which is previously loaded
     */
    private void processCoplet(Object[]    loadedCoplet,
                               XMLConsumer consumer,
                               String      notAvailableMessage,
                               long        defaultCopletTimeout)
    throws ProcessingException,
           SAXException,
           javax.xml.transform.TransformerException  {
        // calling method is synced

        Element copletConf = (Element)loadedCoplet[1];
        Element element    = (Element)loadedCoplet[3];

        String copletID = element.getAttributeNS(null, "id");
        if (copletConf != null) {
            AttributesImpl attr = new AttributesImpl();
            attr.addAttribute("", "id", "id", "CDATA", copletID);
            attr.addAttribute("", "number", "number", "CDATA", element.getAttributeNS(null, "number"));
            attr.addAttribute("", "position", "position", "CDATA", element.getAttributeNS(null, "position"));
            consumer.startElement("", "coplet", "coplet", attr);
            attr.clear();

            // now include all children of the coplet element except status
            NodeList children = copletConf.getChildNodes();
            if (children != null && children.getLength() > 0) {
                int l = children.getLength();
                for(int i = 0; i < l; i++) {
                    if (!children.item(i).getNodeName().equals("status") 
                        && children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        IncludeXMLConsumer.includeNode(children.item(i), consumer, consumer);
                    }
                }
            }

            // now the status parameter
            // SourceParameters p = DOMUtil.createParameters(DOMUtil.getFirstNodeFromPath(element, new String[] {"status"}, false), null);
            consumer.startElement("", "status", "status", attr);
            children = DOMUtil.selectNodeList(element, "status/*", this.xpathProcessor);
            if (children != null && children.getLength() > 0) {
                int l = children.getLength();
                for(int i = 0; i < l; i++) {
                    if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        IncludeXMLConsumer.includeNode(children.item(i), consumer, consumer);
                    }
                }
            }
            consumer.endElement("", "status", "status");

            // now the content:
            consumer.startElement("", "content", "content", attr);

            CopletThread thread = (CopletThread)loadedCoplet[6];
            if (thread != null) {
                long startTime = System.currentTimeMillis() - ((Long)loadedCoplet[4]).longValue();
                long timeout = ((Long)loadedCoplet[5]).longValue();
                long waitTime;
                if (timeout == -1) {
                    waitTime = defaultCopletTimeout;
                } else {
                    waitTime = timeout - startTime;
                }

                while (thread != null && waitTime > 2) {
                    try {
                        Thread.sleep(15);
                        waitTime -= 15;
                    } catch(InterruptedException local) {
                        // ignore
                    }
                    thread = (CopletThread)loadedCoplet[6];
                }
            }
            byte[] content = (byte[])loadedCoplet[0];
            if (content != null) {
                if (content.length > 0) {
                    XMLDeserializer interpreter = null;
                    try {
                        interpreter = (XMLDeserializer)this.manager.lookup(XMLDeserializer.ROLE);
                        interpreter.setConsumer(new IncludeXMLConsumer(consumer, consumer));
                        interpreter.deserialize(content);
                    } catch (ComponentException e) {
                        throw new ProcessingException("Component for XMLDeserializer not found." + e, e);
                    } finally {
                        if (interpreter != null) this.manager.release(interpreter);
                    }
                }
            } else {
                notAvailableMessage = DOMUtil.getValueOf(copletConf,
                         "configuration/messages/coplet_not_available", notAvailableMessage, this.xpathProcessor);
                consumer.characters(notAvailableMessage.toCharArray(), 0, notAvailableMessage.length());
            }
            consumer.endElement("", "content", "content");
            consumer.endElement("", "coplet", "coplet");

        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.portal.components.PortalManager#getMediaType()
     */
    public String getMediaType() 
    throws ProcessingException {
        this.setup();
        return this.getMediaManager().getMediaType();
    }
    
    /**
     * Get the coplet with the id
     */
    private Element getCopletConfiguration(String copletID,
                                           Map    defaultCoplets,
                                           Map    mediaCoplets) 
    throws ProcessingException {
        // calling method is synced
        String media = this.getMediaManager().getMediaType();
        Map    coplets = (Map)mediaCoplets.get(media);
        Element coplet = null;
        if (coplets != null) coplet = (Element)coplets.get(copletID);
        if (coplet == null)  coplet = (Element)defaultCoplets.get(copletID);
        return coplet;
    }

    /**
     * Get the coplet Element
     */
    private Element getCopletElement(DocumentFragment profile,
                                     String copletID,
                                     String copletNr,
                                     Node[] miscNodes)
    throws javax.xml.transform.TransformerException {
        // calling method is synced
        Element node = null;

        // first test content, then header and then footer
        int colindex = 8;
        while (node == null && colindex < 13) {
            if (miscNodes[colindex] != null) {
                node = (Element)DOMUtil.getSingleNode(miscNodes[colindex],
                        "coplets/coplet[@id='"+copletID+"' and @number='"+copletNr+"']", this.xpathProcessor);
                colindex++;
            } else {
                colindex = 13;
            }
        }
        if (node == null && miscNodes[PortalConstants.PROFILE_MISC_HEADER_CONTENT_NODE] != null) {
            node = (Element)DOMUtil.getSingleNode(miscNodes[PortalConstants.PROFILE_MISC_HEADER_CONTENT_NODE],
                      "coplet[@id='"+copletID+"' and @number='"+copletNr+"']", this.xpathProcessor);
        }
        if (node == null && miscNodes[PortalConstants.PROFILE_MISC_FOOTER_CONTENT_NODE] != null) {
            node = (Element)DOMUtil.getSingleNode(miscNodes[PortalConstants.PROFILE_MISC_FOOTER_CONTENT_NODE],
                      "coplet[@id='"+copletID+"' and @number='"+copletNr+"']", this.xpathProcessor);
        }
        return node;
    }

    /**
     * Modify the coplet.
     * This method returns true if the type informations must be recalculated
     */
    private boolean modifyCoplet(String requestString,
                                 SessionContext context,
                                 Map             theProfile,
                                 DocumentFragment profile)
    throws ProcessingException, javax.xml.transform.TransformerException {
        // synchronized as the caller is synced
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN modifyCoplet request=" + requestString);
        }
        boolean result = false;


        int pos, pos2;
        pos  = requestString.indexOf('_');
        pos2 = requestString.indexOf('_', pos+1);
        if (pos != -1 && pos2 != -1) {
            Element coplet = null;

            String copletID;
            String copletNr;
            String argument = null;

            copletID = requestString.substring(pos+1,pos2);
            copletNr = requestString.substring(pos2+1);
            pos = copletNr.indexOf('_');
            if (pos != -1) {
                argument = copletNr.substring(pos+1);
                copletNr = copletNr.substring(0, pos);
            }

            // create a new coplet: in the given column, header or footer
            if (requestString.startsWith(PortalManagerImpl.REQ_CMD_NEW)
                && this.isCopletAvailable(context, copletID,
                                 (Map)theProfile.get(PortalConstants.PROFILE_DEFAULT_COPLETS),
                                 (Map)theProfile.get(PortalConstants.PROFILE_MEDIA_COPLETS))) {
                Node[] miscNodes = (Node[])theProfile.get(PortalConstants.PROFILE_MISC_POINTER);
                // determine the coplet number
                Node   lastCoplet = miscNodes[PortalConstants.PROFILE_MISC_LAST_COPLET_NODE];
                String lastNumber = null;
                if (lastCoplet != null) {
                    lastNumber = ((Element)lastCoplet).getAttributeNS(null, "number");
                    if (lastNumber != null) {
                        int value = new Integer(lastNumber).intValue();
                        value++;
                        lastNumber = ""+value;
                    }
                }
                if (lastNumber == null) lastNumber = "0";

                Node copletsNode;
                if (copletNr.equals("header")) {
                    copletsNode = miscNodes[PortalConstants.PROFILE_MISC_HEADER_CONTENT_NODE];
                    if (copletsNode == null) {
                        copletsNode = DOMUtil.selectSingleNode(profile, "profile/portal-profile/content/header", this.xpathProcessor);
                        miscNodes[PortalConstants.PROFILE_MISC_HEADER_CONTENT_NODE] = copletsNode;
                    } else { // remove old coplet
                        Node oldCoplet = DOMUtil.getFirstNodeFromPath(copletsNode, new String[] {"coplet"}, false);
                        if (oldCoplet != null) copletsNode.removeChild(oldCoplet);
                    }
                } else if (copletNr.equals("footer")) {
                    copletsNode = miscNodes[PortalConstants.PROFILE_MISC_FOOTER_CONTENT_NODE];
                    if (copletsNode == null) {
                        copletsNode = DOMUtil.selectSingleNode(profile, "profile/portal-profile/content/footer", this.xpathProcessor);
                        miscNodes[PortalConstants.PROFILE_MISC_FOOTER_CONTENT_NODE] = copletsNode;
                    } else { // remove old coplet
                        Node oldCoplet = DOMUtil.getFirstNodeFromPath(copletsNode, new String[] {"coplet"}, false);
                        if (oldCoplet != null) copletsNode.removeChild(oldCoplet);
                    }
                } else {
                    Node columnNode = miscNodes[7+new Integer(copletNr).intValue()];
                    copletsNode = DOMUtil.getFirstNodeFromPath(columnNode, new String[] {"coplets"}, false);
                }
                Element copletNode;
                Document doc = copletsNode.getOwnerDocument();
                copletNode = doc.createElementNS(null, "coplet");
                copletsNode.appendChild(copletNode);
                copletNode.setAttributeNS(null, "id", copletID);
                copletNode.setAttributeNS(null, "number", lastNumber);
                // set position
                NodeList childs = DOMUtil.getNodeListFromPath(copletsNode, new String[] {"coplet"});
                int childsCount = (childs == null ? 0 : childs.getLength());
                copletNode.setAttributeNS(null, "position", ""+(childsCount));
                miscNodes[PortalConstants.PROFILE_MISC_LAST_COPLET_NODE] = copletNode;

                // copy status
                Element configElement = this.getCopletConfiguration(copletID,
                                                      (Map)theProfile.get(PortalConstants.PROFILE_DEFAULT_COPLETS),
                                                      (Map)theProfile.get(PortalConstants.PROFILE_MEDIA_COPLETS));
                Element configStatus = (Element)DOMUtil.getFirstNodeFromPath(configElement, new String[] {"status"}, false);
                copletNode.appendChild(configStatus.cloneNode(true));

                // clear type information for each status
                Element status = (Element)copletNode.getElementsByTagName("status").item(0);
                NodeList parameters = status.getChildNodes();
                Node    current;
                Element statusNode;
                if (parameters != null) {
                    for(int i = 0; i < parameters.getLength(); i++) {
                        current = parameters.item(i);
                        if (current.getNodeType() == Node.ELEMENT_NODE) {
                            statusNode = (Element)current;
                            if (statusNode.hasAttributeNS(null, "formpath"))
                                statusNode.removeAttributeNS(null, "formpath");
                            if (statusNode.hasAttributeNS(null, "formtype"))
                                statusNode.removeAttributeNS(null, "formtype");
                            if (statusNode.hasAttributeNS(null, "formdescription"))
                                statusNode.removeAttributeNS(null, "formdescription");
                        }
                    }
                }
                result = true;

           } else {
                coplet = this.getCopletElement(profile,
                                   copletID,
                                    copletNr,
                                    (Node[])theProfile.get(PortalConstants.PROFILE_MISC_POINTER));
                if (coplet != null) {
                    if (requestString.startsWith(PortalManagerImpl.REQ_CMD_CLOSE) ||
                        requestString.startsWith(PortalManagerImpl.REQ_CMD_HIDE)) {
                         Node node = DOMUtil.selectSingleNode(coplet, "status/visible", this.xpathProcessor);
                         DOMUtil.setValueOfNode(node, "false");
                    } else if (requestString.startsWith(PortalManagerImpl.REQ_CMD_OPEN) ||
                        requestString.startsWith(PortalManagerImpl.REQ_CMD_SHOW)) {
                         Node node = DOMUtil.selectSingleNode(coplet, "status/visible", this.xpathProcessor);
                         DOMUtil.setValueOfNode(node, "true");
                    } else if (requestString.startsWith(PortalManagerImpl.REQ_CMD_MINIMIZE)) {
                         Node node = DOMUtil.selectSingleNode(coplet, "status/size", this.xpathProcessor);
                         DOMUtil.setValueOfNode(node, "min");
                    } else if (requestString.startsWith(PortalManagerImpl.REQ_CMD_MAXIMIZE)) {
                         Node node = DOMUtil.selectSingleNode(coplet, "status/size", this.xpathProcessor);
                         DOMUtil.setValueOfNode(node, "max");
                    } else if (requestString.startsWith(PortalManagerImpl.REQ_CMD_CUSTOMIZE)) {
                         Node node = DOMUtil.selectSingleNode(coplet, "status/customize", this.xpathProcessor);
                         DOMUtil.setValueOfNode(node, "true");
                    } else if (requestString.startsWith(PortalManagerImpl.REQ_CMD_UPDATE)) {
                         Node node = DOMUtil.selectSingleNode(coplet, "status/customize", this.xpathProcessor);
                         DOMUtil.setValueOfNode(node, "false");
                    } else if (requestString.startsWith(PortalManagerImpl.REQ_CMD_DELETE)) {
                        // delete the status of the coplet
                        Node statusNode = DOMUtil.getSingleNode(profile,
                             "profile/status-profile/customization/coplet[@id='"+copletID+"' and @number='"+copletNr+"']", this.xpathProcessor);
                        if (statusNode != null) {
                            statusNode.getParentNode().removeChild(statusNode);
                            Element configElement = this.getCopletConfiguration(copletID,
                                                      (Map)theProfile.get(PortalConstants.PROFILE_DEFAULT_COPLETS),
                                                      (Map)theProfile.get(PortalConstants.PROFILE_MEDIA_COPLETS));
                            boolean isPersistent = DOMUtil.getValueAsBooleanOf(configElement, "configuration/persistent", false, this.xpathProcessor);
                            if (isPersistent) {
                                // mark the status profile to be saved
                                theProfile.put(PortalConstants.PROFILE_SAVE_STATUS_FLAG, "true");
                            }
                        }
                        String posAttr = coplet.getAttributeNS(null, "position");
                        NodeList followUps = DOMUtil.selectNodeList(coplet.getParentNode(), "coplet[@position > '"+posAttr+"']", this.xpathProcessor);
                        coplet.getParentNode().removeChild(coplet);
                        coplet = null;
                        if (followUps != null) {
                            int value;
                            for(int i = 0; i < followUps.getLength(); i++) {
                                value = new Integer(((Element)followUps.item(i)).getAttributeNS(null, "position")).intValue();
                                value -= 1;
                                ((Element)followUps.item(i)).setAttributeNS(null, "position", "" + value);
                           }
                        }
                    } else if (requestString.startsWith(PortalManagerImpl.REQ_CMD_MOVE)) {
                        if (argument != null) {
                            Element  copletsElement = (Element)DOMUtil.getSingleNode(profile,
                                  "profile/portal-profile/content/column[@position='"+argument+"']/coplets", this.xpathProcessor);
                            if (copletsElement != null) {
                                if (!coplet.getParentNode().equals(copletsElement)) {
                                     String posAttr = coplet.getAttributeNS(null, "position");
                                     NodeList followUps = DOMUtil.selectNodeList(coplet.getParentNode(), "coplet[@position > '"+posAttr+"']", this.xpathProcessor);
                                     coplet.getParentNode().removeChild(coplet);
                                     // set position attribute
                                     NodeList childs = DOMUtil.getNodeListFromPath(copletsElement, new String[] {"coplet"});
                                     int childsCount = (childs == null ? 0 : childs.getLength());
                                     coplet.setAttributeNS(null, "position", "" + (childsCount + 1));
                                     copletsElement.appendChild(coplet);
                                     if (followUps != null) {
                                         int value;
                                         for(int i = 0; i < followUps.getLength(); i++) {
                                             value = new Integer(((Element)followUps.item(i)).getAttributeNS(null, "position")).intValue();
                                             value -= 1;
                                             ((Element)followUps.item(i)).setAttributeNS(null, "position", "" + value);
                                         }
                                     }
                                 }
                            }
                        }
                    } else if (requestString.startsWith(PortalManagerImpl.REQ_CMD_MOVEROW)) {
                        if (argument != null) {
                            Element newCoplet = (Element)DOMUtil.getSingleNode(coplet.getParentNode(),
                                                 "coplet[@position='"+argument+"']", this.xpathProcessor);
                            if (newCoplet != null) {
                                String position = coplet.getAttributeNS(null, "position");
                                coplet.removeAttributeNS(null, "position");
                                coplet.setAttributeNS(null, "position", argument);
                                newCoplet.removeAttributeNS(null, "position");
                                newCoplet.setAttributeNS(null, "position", position);
                            }
                        }
                    }
                }
            }
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END modifyCoplet calculate="+result);
        }
        return result;
    }

    /**
     * Check if the coplet is available for the current logged in user
     * If the user is not logged in, this returns false.
     * First the default coplets are searched. If none is found then
     * the coplets for each media are searched.
     */
    private boolean isCopletAvailable(SessionContext context,
                                    String copletID,
                                    Map defaultCoplets,
                                    Map mediaCoplets)
    {
        // no sync required
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN isCopletAvailable coplet="+copletID);
        }
        boolean result = false;

        if (context != null) {
            result = defaultCoplets.containsKey(copletID);
            if (!result) {
                Iterator iter = mediaCoplets.values().iterator();
                while (!result && iter.hasNext()) {
                    result = ((Map)iter.next()).containsKey(copletID);
                }
            }
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END isCopletAvailable result=" + result);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.portal.components.PortalManager#checkAuthentication(org.apache.cocoon.environment.Redirector, java.lang.String)
     */
    public boolean checkAuthentication(Redirector redirector, String copletID)
    throws SAXException, IOException, ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN checkAuthentication coplet="+copletID);
        }
        this.setup();
        boolean result = false;
        SessionContext context = this.getContext(false);
        if (context != null
            && (String)context.getAttribute(PortalManagerImpl.ATTRIBUTE_PORTAL_ROLE) != null) {

            try {
                this.getTransactionManager().startReadingTransaction(context);
                Map theProfile = this.retrieveProfile(this.getProfileID(PortalManagerImpl.BUILDTYPE_VALUE_ID,
                     (String)context.getAttribute(PortalManagerImpl.ATTRIBUTE_PORTAL_ROLE),
                     (String)context.getAttribute(PortalManagerImpl.ATTRIBUTE_PORTAL_ID), false));

                if (theProfile != null) {
                    if (copletID == null || copletID.trim().length() == 0) {
                        result = true;
                    } else {
                        result = this.isCopletAvailable(context,
                                      copletID,
                                      (Map)theProfile.get(PortalConstants.PROFILE_DEFAULT_COPLETS),
                                      (Map)theProfile.get(PortalConstants.PROFILE_MEDIA_COPLETS));
                    }
                }
            } finally {
                this.getTransactionManager().stopReadingTransaction(context);
            } // end synced
        }


        if (!result) {
            Map config = this.getConfiguration();
            if (config != null) {
                String redirectURI = (String)config.get(PortalConstants.CONF_AUTH_REDIRECT);
                if (redirectURI == null) {
                    redirectURI = (String)config.get(PortalConstants.CONF_PORTAL_URI);
                }
                if (redirectURI != null) {
                    redirector.globalRedirect( false, redirectURI );
                }
            }
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END checkAuthentication result=" + result);
        }
        return result;
    }

    /**
     * Get the configuration. This configuration is an authentication application configuration
     * for the current application with the name "portal".
     * The first time this configuration is build it is stored in the session
     * so later requests get the cached result.
     */
    private Map getConfiguration()
    throws ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN getConfiguration");
        }
        Map result = null;
        RequestState reqstate = this.getRequestState();
        String appName = reqstate.getApplicationName();
        String handlerName = reqstate.getHandlerName();
        Session session = this.getSessionManager().getSession(false);
        if (session != null && appName != null && handlerName != null) {

            synchronized (session) {
                result = (Map)session.getAttribute(PortalConstants.ATTRIBUTE_CONFIGURATION + handlerName + ':' + appName);
                if (result == null) {

                    try {
                        Configuration config;

                        Configuration conf = reqstate.getModuleConfiguration(PortalConstants.AUTHENTICATION_MODULE_NAME);
                        if (conf == null) {
                            throw new ProcessingException("portal: Configuration for application '" + appName + "' not found.");
                        }
                        result = new HashMap(10, 2);
                        // auth-redirect (optional)
                        config = conf.getChild("auth-redirect", false);
                        if (config != null) {
                            result.put(PortalConstants.CONF_AUTH_REDIRECT, config.getValue());
                        }

                        // portal-uri (required)
                        config = conf.getChild("portal-uri", false);
                        if (config == null) {
                            throw new ProcessingException("portal: portal-uri required for application '"+appName+"'");
                        }
                        result.put(PortalConstants.CONF_PORTAL_URI, config.getValue());

                        // profile-cache (optional)
                        config = conf.getChild("profile-cache", false);
                        if (config != null && config.getValueAsBoolean()) {
                            result.put(PortalConstants.CONF_PROFILE_CACHE, appName);
                        }

                        // parallel coplets
                        config = conf.getChild("process-coplets-parallel", false);
                        if (config != null) {
                            result.put(PortalConstants.CONF_PARALLEL_COPLETS, new Boolean(config.getValueAsBoolean(false)));
                        } else {
                            result.put(PortalConstants.CONF_PARALLEL_COPLETS, Boolean.FALSE);
                        }

                        // timeout
                        config = conf.getChild("default-coplet-timeout", false);
                        if (config != null) {
                            result.put(PortalConstants.CONF_COPLET_TIMEOUT, new Long(config.getValueAsLong(600000)));
                        } else {
                            result.put(PortalConstants.CONF_COPLET_TIMEOUT, new Long(600000));
                        }

                        // and now the profile
                        config = conf.getChild("profile", false);
                        if (config == null) throw new ProcessingException("portal: profile configuration required for application '" + appName + "'");
                        Configuration child;

                        // build resource (optional)
                        child = config.getChild("buildprofile", false);
                        if (child != null) {
                            result.put(PortalConstants.CONF_BUILD_RESOURCE, child.getAttribute("uri"));
                        }

                        // base resource, type is optional
                        child = config.getChild("layout-base", false);
                        if (child == null) {
                            throw new ProcessingException("portal: layout-base required for application '" + appName + "'");
                        }
                        result.put(PortalConstants.CONF_LAYOUTBASE_RESOURCE, child.getAttribute("uri"));
                        child = config.getChild("coplet-base", false);
                        if (child == null) {
                            throw new ProcessingException("portal: coplet-base required for application '" + appName + "'");
                        }
                        result.put(PortalConstants.CONF_COPLETBASE_RESOURCE, child.getAttribute("uri"));
                        child = config.getChild("type-base", false);
                        if (child != null) {
                            result.put(PortalConstants.CONF_TYPEBASE_RESOURCE, child.getAttribute("uri"));
                        }

                        // coplet base save (is optional)
                        child = config.getChild("coplet-base-save", false);
                        if (child != null) {
                            result.put(PortalConstants.CONF_COPLETBASE_SAVE_RESOURCE, child.getAttribute("uri"));
                        }

                        // global delta (load required)
                        child = config.getChild("global-delta-load", false);
                        if (child == null) {
                            throw new ProcessingException("portal: global-delta-load required for application '" + appName + "'");
                        }
                        result.put(PortalConstants.CONF_GLOBALDELTA_LOADRESOURCE, child.getAttribute("uri"));
                        child = config.getChild("global-delta-save", false);
                        if (child != null) {
                            result.put(PortalConstants.CONF_GLOBALDELTA_SAVERESOURCE, child.getAttribute("uri"));
                        }
                        child = config.getChild("global-type-delta", false);
                        if (child != null) {
                            result.put(PortalConstants.CONF_GLOBALDELTA_TYPERESOURCE, child.getAttribute("uri"));
                        }

                        // role delta (optional)
                        child = config.getChild("role-delta-load", false);
                        if (child != null) {
                            result.put(PortalConstants.CONF_ROLEDELTA_LOADRESOURCE, child.getAttribute("uri"));
                        }
                        child = config.getChild("role-delta-save", false);
                        if (child != null) {
                            result.put(PortalConstants.CONF_ROLEDELTA_SAVERESOURCE, child.getAttribute("uri"));
                        }
                        child = config.getChild("role-type-delta", false);
                        if (child != null) {
                            result.put(PortalConstants.CONF_ROLEDELTA_TYPERESOURCE, child.getAttribute("uri"));
                        }

                        // User delta
                        child = config.getChild("user-delta-load", false);
                        if (child != null) {
                            result.put(PortalConstants.CONF_USERDELTA_LOADRESOURCE, child.getAttribute("uri"));
                        }
                        child = config.getChild("user-delta-save", false);
                        if (child != null) {
                            result.put(PortalConstants.CONF_USERDELTA_SAVERESOURCE, child.getAttribute("uri"));
                        }
                        child = config.getChild("user-type-delta", false);
                        if (child != null) {
                            result.put(PortalConstants.CONF_USERDELTA_TYPERESOURCE, child.getAttribute("uri"));
                        }

                        // Personal information
                        child = config.getChild("user-status-load", false);
                        if (child != null) {
                            result.put(PortalConstants.CONF_STATUS_LOADRESOURCE, child.getAttribute("uri"));
                        }
                        child = config.getChild("user-status-save", false);
                        if (child != null) {
                            result.put(PortalConstants.CONF_STATUS_SAVERESOURCE, child.getAttribute("uri"));
                        }

                        // Admin Type profil
                        child = config.getChild("admin-type-base", false);
                        if (child != null) {
                            result.put(PortalConstants.CONF_ADMIN_TYPE_BASE, child.getAttribute("uri"));
                        }

                        // store the config in the session
                        session.setAttribute(PortalConstants.ATTRIBUTE_CONFIGURATION + handlerName + ':' + appName, result);
                    } catch (ConfigurationException conf) {
                        throw new ProcessingException("ConfigurationException: " + conf, conf);
                    }
                }
            }

        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END getConfiguration conf="+result);
        }
        return result;
    }

    /**
     * Build the profile for the required level if not already done
     */
    private void createProfile(SessionContext context,
                              String type,
                              String role,
                              String id,
                              boolean adminProfile)
    throws SAXException, IOException, ProcessingException {
        // no sync required
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN createProfile context="+context+
                                   ", type="+type+
                                   ", role="+role+
                                   ", id="+id);
        }

        RequestState reqstate = this.getRequestState();
        SourceParameters pars = reqstate.getHandler().getContext().getContextInfoAsParameters();
        pars.setSingleParameterValue("type", type);
        pars.setSingleParameterValue("admin", (adminProfile ? "true" : "false"));

        if (!type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ID) || role != null) {
            pars.setSingleParameterValue("ID", id);
            pars.setSingleParameterValue("role", role);
        } else {
            id = pars.getParameter("ID", null);
            role = pars.getParameter("role", null);
        }

        Map map = this.getConfiguration();
        if (map == null) {
            throw new ProcessingException("portal Configuration not found.");
        }

        // is the configuration build by using a own resource?
        String resource = (String)map.get(PortalConstants.CONF_BUILD_RESOURCE);
        if (resource != null) {
            if (this.getLogger().isInfoEnabled()) {
                this.getLogger().info("Building portal profile: " + resource);
            }
            SourceUtil.readDOM(resource, 
                               null, 
                               pars, 
                               this.resolver);
        } else {
            this.buildProfile(type, role, id, adminProfile);
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END createProfile");
        }

    }

    /**
     * Get the base profile for the current application.
     * The base profile consists of the layout and the coplet profile
     * and optional the type profile
     */
    private DocumentFragment buildBaseProfile(Map config, boolean adminProfile)
    throws ProcessingException {
        // calling method is synced
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN buildBaseProfile config="+config+", adminProfile="+adminProfile);
        }
        DocumentFragment copletsFragment;
        DocumentFragment layoutFragment;
        DocumentFragment typeFragment;
        DocumentFragment profile;
        Document         profileDoc;
        Element          profileRoot;
        String           res;

        SourceParameters pars = new SourceParameters();
        RequestState reqstate = this.getRequestState();
        pars.setSingleParameterValue("application", reqstate.getApplicationName());
        pars.setSingleParameterValue("handler", reqstate.getHandlerName());
        pars.setSingleParameterValue("profile", "coplet-base");

        // First load the base profiles: copletProfile + layoutProfile
        res = (String)config.get(PortalConstants.CONF_COPLETBASE_RESOURCE);
        if (res == null) {
            throw new ProcessingException("No configuration for portal-coplet base profile found.");
        }
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Loading coplet base profile");
        }
        copletsFragment = SourceUtil.readDOM(res, 
                               null, 
                               pars, 
                               this.resolver);

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("coplet base profile loaded");
        }
        res = (String)config.get(PortalConstants.CONF_LAYOUTBASE_RESOURCE);
        if (res == null) {
            throw new ProcessingException("No configuration for portal-layout base profile found.");
        }
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("loading layout base profile");
        }
        pars.setSingleParameterValue("profile", "layout-base");
        layoutFragment = SourceUtil.readDOM(res, 
                               null, 
                               pars, 
                               this.resolver);

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("layout base profile loaded");
        }
        // now create the base profile containing the above profiles
        profileDoc = DOMUtil.createDocument();
        profile = profileDoc.createDocumentFragment();
        profileRoot = profileDoc.createElementNS(null, "profile");
        profile.appendChild(profileRoot);
        profileRoot.appendChild(profileDoc.importNode(DOMUtil.selectSingleNode(layoutFragment,
                                                                  "layout-profile", this.xpathProcessor), true));
        profileRoot.appendChild(profileDoc.importNode(DOMUtil.selectSingleNode(copletsFragment,
                                                                  "coplets-profile", this.xpathProcessor), true));

        // if avalailable append the type profile
        if (adminProfile) {
            res = (String)config.get(PortalConstants.CONF_ADMIN_TYPE_BASE);
            pars.setSingleParameterValue("profile", "admin-type-base");
        } else {
            res = (String)config.get(PortalConstants.CONF_TYPEBASE_RESOURCE);
            pars.setSingleParameterValue("profile", "type-base");
        }
        if (res != null) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("loading type base profile");
            }
            typeFragment = SourceUtil.readDOM(res, 
                                   null, 
                                   pars, 
                                   this.resolver);
            profileRoot.appendChild(profileDoc.importNode(DOMUtil.selectSingleNode(typeFragment,
                              "type-profile", this.xpathProcessor), true));

            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("type base profile loaded");
            }
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END buildBaseProfile profile=" + profile);
        }
        return profile;
    }

    /**
     * Build the global profile.
     */
    private void buildGlobalProfile(Element profileRoot,
                                    Map config,
                                    boolean adminProfile)
    throws ProcessingException {
        // calling method is synced
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN buildGlobalProfile profileRoot="+profileRoot+", config="+config+", adminProfile="+adminProfile);
        }
        DocumentFragment globalFragment;
        String res = (String)config.get(PortalConstants.CONF_GLOBALDELTA_LOADRESOURCE);
        if (res == null) {
            throw new ProcessingException("No configuration for portal-role delta profile found.");
        }
        SourceParameters pars = new SourceParameters();
        RequestState reqstate = this.getRequestState();
        pars.setSingleParameterValue("application", reqstate.getApplicationName());
        pars.setSingleParameterValue("handler", reqstate.getHandlerName());
        pars.setSingleParameterValue("profile", "global-delta");

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("loading global profile");
        }
        globalFragment = SourceUtil.readDOM(res, 
                                   null, 
                                   pars, 
                                   this.resolver);
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("global profile loaded");
        }
        this.importProfileDelta(profileRoot, globalFragment, "global-delta", "layout-delta");
        this.importProfileDelta(profileRoot, globalFragment, "global-delta", "coplets-delta");
        this.addProfilePart(profileRoot, globalFragment, "global-delta", "portal-profile");
        this.addProfilePart(profileRoot, globalFragment, "global-delta", "personal-profile");

        // types
        res = (String)config.get(PortalConstants.CONF_GLOBALDELTA_TYPERESOURCE);
        if (!adminProfile && res != null) {
            pars.setSingleParameterValue("profile", "global-type-delta");
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("loading global type profile");
            }
            globalFragment = SourceUtil.readDOM(res, 
                                       null, 
                                       pars, 
                                       this.resolver);
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("global type profile loaded");
            }
            this.addProfilePart(profileRoot, globalFragment, "global-delta", "type-profile");
        }
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END buildGlobalProfile");
        }
    }

    /**
     * Build the role profile
     */
    private void buildRoleProfile(Element profileRoot,
                                            Map config,
                                            String role,
                                            boolean adminProfile)
    throws ProcessingException {
        // calling method is synced

        DocumentFragment roleFragment;
        RequestState reqstate = this.getRequestState();
        SourceParameters pars;
        pars = new SourceParameters();
        pars.setSingleParameterValue("role", role);
        pars.setSingleParameterValue("application", reqstate.getApplicationName());
        pars.setSingleParameterValue("handler", reqstate.getHandlerName());
        pars.setSingleParameterValue("profile", "role-delta");

        String res = (String)config.get(PortalConstants.CONF_ROLEDELTA_LOADRESOURCE);
        if (res != null) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("loading role profile");
            }
            roleFragment = SourceUtil.readDOM(res, 
                                       null, 
                                       pars, 
                                       this.resolver);
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("role profile loaded");
            }
            this.importProfileDelta(profileRoot, roleFragment, "role-delta", "layout-delta");
            this.importProfileDelta(profileRoot, roleFragment, "role-delta", "coplets-delta");
            this.addProfilePart(profileRoot, roleFragment, "role-delta", "portal-profile");
            this.importProfileDelta(profileRoot, roleFragment, "role-delta", "personal-delta");
        }

        // types
        res = (String)config.get(PortalConstants.CONF_ROLEDELTA_TYPERESOURCE);
        if (!adminProfile && res != null) {
            pars.setSingleParameterValue("profile", "role-type-delta");
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("loading role type profile");
            }
            roleFragment = SourceUtil.readDOM(res, 
                                   null, 
                                   pars, 
                                   this.resolver);
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("role type profile loaded");
            }
            this.addProfilePart(profileRoot, roleFragment, "role-delta", "type-profile");
        }
    }

    /**
     * Build the user profile
     */
    private void buildUserProfile(Element profileRoot,
                                Map config,
                                String role,
                                String id,
                                boolean adminProfile)
    throws ProcessingException {
        // calling method is synced
        DocumentFragment userFragment;
        RequestState reqstate = this.getRequestState();
        SourceParameters pars;
        pars = new SourceParameters();
        pars.setSingleParameterValue("ID", id);
        pars.setSingleParameterValue("role", role);
        pars.setSingleParameterValue("application", reqstate.getApplicationName());
        pars.setSingleParameterValue("handler", reqstate.getHandlerName());
        pars.setSingleParameterValue("profile", "user-delta");

        String res = (String)config.get(PortalConstants.CONF_USERDELTA_LOADRESOURCE);
        if (res != null) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("loading user profile");
            }
            userFragment = SourceUtil.readDOM(res, 
                                   null, 
                                   pars, 
                                   this.resolver);
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("user profile loaded");
            }
            this.importProfileDelta(profileRoot, userFragment, "user-delta", "layout-delta");
            this.importProfileDelta(profileRoot, userFragment, "user-delta", "coplets-delta");
            this.addProfilePart(profileRoot, userFragment, "user-delta", "portal-profile");
            this.importProfileDelta(profileRoot, userFragment, "user-delta", "personal-delta");
        }

        // types
        res = (String)config.get(PortalConstants.CONF_USERDELTA_TYPERESOURCE);
        if (!adminProfile && res != null) {
            pars.setSingleParameterValue("profile", "user-type-delta");
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("loading user type profile");
            }
            userFragment = SourceUtil.readDOM(res, 
                                   null, 
                                   pars, 
                                   this.resolver);
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("user type profile loaded");
            }
            this.addProfilePart(profileRoot, userFragment, "user-delta", "type-profile");
        }
    }

    /**
     * Load the user status profile (if available)
     */
    private void buildUserStatusProfile(Element profileRoot,
                                        Map config,
                                        String role,
                                        String id,
                                        boolean adminProfile)
    throws ProcessingException {
        // calling method is synced
        String res = (String)config.get(PortalConstants.CONF_STATUS_LOADRESOURCE);

        // remove the old status profile
        Node statusProfile = DOMUtil.getFirstNodeFromPath(profileRoot, new String[] {"status-profile"}, false);
        if (statusProfile != null) {
            profileRoot.removeChild(statusProfile);
        }

        if (res != null) {
            DocumentFragment userFragment;
            SourceParameters pars;
            RequestState reqstate = this.getRequestState();
            pars = new SourceParameters();
            pars.setSingleParameterValue("ID", id);
            pars.setSingleParameterValue("role", role);
            pars.setSingleParameterValue("application", reqstate.getApplicationName());
            pars.setSingleParameterValue("handler", reqstate.getHandlerName());
            pars.setSingleParameterValue("profile", "user-status");
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("loading user status profile");
            }
            userFragment = SourceUtil.readDOM(res, 
                                   null, 
                                   pars, 
                                   this.resolver);
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("user status profile loaded");
            }
            this.addProfilePart(profileRoot, userFragment, null, "status-profile");
        }
        // test if the status-profile node is available
        // if not create one
        if (DOMUtil.getFirstNodeFromPath(profileRoot, new String[] {"status-profile"}, false) == null) {
            statusProfile = profileRoot.getOwnerDocument().createElementNS(null, "status-profile");
            profileRoot.appendChild(statusProfile);
        }
    }

    /**
     * Save the user status profile (if available)
     */
    private void saveUserStatusProfile(Map profile,
                                       Map config,
                                       String role,
                                       String id,
                                       boolean adminProfile)
    throws ProcessingException {
        // calling method is synced
        String res = (String)config.get(PortalConstants.CONF_STATUS_SAVERESOURCE);
        Element statusProfile = (Element)DOMUtil.getFirstNodeFromPath((DocumentFragment)profile.get(PortalConstants.PROFILE_PROFILE),
                         new String[] {"profile","status-profile"}, false);
        if (res != null && statusProfile != null) {
            DocumentFragment userFragment = statusProfile.getOwnerDocument().createDocumentFragment();
            Element saveStatus = (Element)statusProfile.cloneNode(true);
            userFragment.appendChild(saveStatus);
            // now filter all not persistent coplets!
            NodeList list = DOMUtil.getNodeListFromPath(saveStatus, new String[] {"customization","coplet"});
            String copletID;
            Element coplet;
            Element copletConfig;
            Map copletConfigs = (Map)profile.get(PortalConstants.PROFILE_DEFAULT_COPLETS);
            Map mediaCopletConfigs = (Map)profile.get(PortalConstants.PROFILE_MEDIA_COPLETS);
            boolean isPersistent;
            for(int i = 0; i < list.getLength(); i++) {
                coplet = (Element)list.item(i);
                copletID = coplet.getAttributeNS(null, "id");
                copletConfig = this.getCopletConfiguration(copletID, copletConfigs, mediaCopletConfigs);
                isPersistent = DOMUtil.getValueAsBooleanOf(copletConfig, "configuration/persistent", false, this.xpathProcessor);
                if (!isPersistent) {
                    coplet.getParentNode().removeChild(coplet);
                }
            }

            try {

                RequestState reqstate = this.getRequestState();
                SourceParameters pars;
                pars = new SourceParameters();
                pars.setSingleParameterValue("ID", id);
                pars.setSingleParameterValue("role", role);
                pars.setSingleParameterValue("application", reqstate.getApplicationName());
                pars.setSingleParameterValue("handler", reqstate.getHandlerName());
                pars.setSingleParameterValue("profile", "user-status");

                SourceUtil.writeDOM(res, 
                                   null, 
                                   pars,
                                   userFragment, 
                                   this.resolver, 
                                   "xml");

            } finally {
                userFragment.removeChild(saveStatus);
            }
        }
    }

    /**
     * Change the profile according to the request parameter
     */
    private void changeProfile()
    throws ProcessingException, SAXException, IOException {
        // synchronized
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN changeProfile");
        }
        Request request = ContextHelper.getRequest(this.componentContext);
        SessionContext context = this.getContext(false);

        if (context != null) {
            try {
                Map theProfile = null;
                String profileID = request.getParameter(PortalManagerImpl.REQ_PARAMETER_PROFILE);
                if (profileID != null) {
                    theProfile = this.retrieveProfile(profileID);
                }

                if (theProfile != null) {
                    synchronized (context) {
                        DocumentFragment profile = (DocumentFragment)theProfile.get(PortalConstants.PROFILE_PROFILE);
                        Node[]           miscNodes = (Node[])theProfile.get(PortalConstants.PROFILE_MISC_POINTER);
                        Element          columns = (Element)miscNodes[PortalConstants.PROFILE_MISC_COLUMNS_NODE];
                        Enumeration      enum = request.getParameterNames();
                        String           current;
                        boolean          saveProfile = false;

                        // first iteration: all changing commands
                        while (enum.hasMoreElements()) {
                            current = (String)enum.nextElement();
                            if (current.startsWith(PortalManagerImpl.REQ_PARAMETER_CONF)) {
                                int pos1, pos2;
                                pos1 = current.indexOf('.');
                                pos2 = current.indexOf('.', pos1+1);
                                if (pos1 != -1 && pos2 != -1) {
                                    int pathIndex = new Integer(current.substring(pos1+1, pos2)).intValue();
                                    int place= new Integer(current.substring(pos2+1)).intValue();
                                    List typePaths = (List)theProfile.get(PortalConstants.PROFILE_TYPE_CONF_PATHS);
                                    String path = (String)typePaths.get(pathIndex);
                                    if (path != null) {
                                        NodeList nodes = DOMUtil.selectNodeList(profile, path, this.xpathProcessor);
                                        if (nodes != null) {
                                            Node node = nodes.item(place);
                                            if (node != null) {
                                                if (!node.equals(columns)) {
                                                        DOMUtil.setValueOfNode(node, request.getParameter(current));
                                                }
                                            }
                                        }

                                    }
                                }
                            }
                        }

                        // second: all new
                        boolean     calculate = false;
                        enum = request.getParameterNames();
                        while (enum.hasMoreElements()) {

                            current = (String)enum.nextElement();
                            if (current.startsWith(PortalManagerImpl.REQ_PARAMETER_CONF)) {
                                int pos1, pos2;
                                pos1 = current.indexOf('.');
                                pos2 = current.indexOf('.', pos1+1);
                                if (pos1 != -1 && pos2 != -1) {
                                    int pathIndex = new Integer(current.substring(pos1+1, pos2)).intValue();
                                    int place= new Integer(current.substring(pos2+1)).intValue();
                                    List typePaths = (List)theProfile.get(PortalConstants.PROFILE_TYPE_CONF_PATHS);
                                    String path = (String)typePaths.get(pathIndex);
                                    if (path != null) {
                                        NodeList nodes = DOMUtil.selectNodeList(profile, path, this.xpathProcessor);
                                        if (nodes != null) {
                                            Node node = nodes.item(place);
                                            if (node != null) {
                                                if (node.equals(columns)) {
                                                    int columnNumber = new Integer(request.getParameter(current)).intValue();
                                                    int oldNumber = new Integer(DOMUtil.getValueOfNode(columns)).intValue();
                                                    if (columnNumber > 0 && columnNumber != oldNumber && columnNumber <= PortalConstants.MAX_COLUMNS) {
                                                        this.changeColumns(profile,
                                                               oldNumber,
                                                               columnNumber,
                                                               miscNodes);
                                                        calculate = true;
                                                        DOMUtil.setValueOfNode(node, request.getParameter(current));
                                                    }
                                                }
                                            }
                                        }

                                    }
                                }

                            } else if (current.equals(PortalManagerImpl.REQ_PARAMETER_CMD)) {
                                String[] cmds = request.getParameterValues(current);
                                if (cmds != null && cmds.length > 0) {
                                    for(int i = 0; i < cmds.length; i++) {
                                        if (cmds[i].equals(PortalManagerImpl.REQ_CMD_SAVEPROFILE)) {
                                            saveProfile = true;
                                        } else {
                                            if (this.modifyCoplet(cmds[i], context, theProfile, profile)) {
                                                calculate = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // set type infos
                        if (calculate) {
                            this.setTypeInfo(profile,
                                 (List)theProfile.get(PortalConstants.PROFILE_TYPE_PATHS),
                                 (List)theProfile.get(PortalConstants.PROFILE_TYPE_CONF_PATHS));
                        }

                        // test if the status profile changed
                        Object statusChanged = theProfile.get(PortalConstants.PROFILE_SAVE_STATUS_FLAG);
                        if (statusChanged != null) {
                            theProfile.remove(PortalConstants.PROFILE_SAVE_STATUS_FLAG);
                            this.saveUserStatusProfile(theProfile,
                                                       this.getConfiguration(),
                                                       this.getRole(profileID),
                                                       this.getID(profileID),
                                                       this.getIsAdminProfile(profileID));
                        }

                        // save the profile
                        if (saveProfile) {
                            Map      conf = this.getConfiguration();
                            String   role = this.getRole(profileID);
                            String   id   = this.getID(profileID);
                            String   type = this.getType(profileID);
                            String   saveResource;
                            String   profileType;

                            if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_GLOBAL)) {
                                saveResource = (String)conf.get(PortalConstants.CONF_GLOBALDELTA_SAVERESOURCE);
                                profileType = "global-delta";
                            } else if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ROLE)) {
                                saveResource = (String)conf.get(PortalConstants.CONF_ROLEDELTA_SAVERESOURCE);
                                profileType = "role-delta";
                            } else if (type.equals(PortalManagerImpl.BUILDTYPE_VALUE_ID)) {
                                saveResource = (String)conf.get(PortalConstants.CONF_USERDELTA_SAVERESOURCE);
                                profileType = "user-delta";
                            } else {
                                throw new ProcessingException("portal: No save resource defined for type '"+type+"'.");
                            }

                            // patch
                            // search for all "status/customize" nodes and set them
                            // to false
                            NodeList statusNodes = DOMUtil.selectNodeList(profile,
                                    "profile/portal-profile/content/descendant::status/customize", this.xpathProcessor);
                            if (statusNodes != null) {
                                String value;
                                for(int l=0; l < statusNodes.getLength(); l++) {
                                    value = DOMUtil.getValueOfNode(statusNodes.item(l));
                                    if (value.equals("true")) {
                                        DOMUtil.setValueOfNode(statusNodes.item(l), "false");
                                    }
                                }
                            }

                            // build delta
                            RequestState reqstate = this.getRequestState();
                            DocumentFragment delta;
                            delta = this.buildProfileDelta(type, role, id, this.getIsAdminProfile(profileID));
                            SourceParameters pars = new SourceParameters();
                            pars.setSingleParameterValue("type", profileType);
                            if (id != null) pars.setSingleParameterValue("ID", id);
                            if (role != null) pars.setSingleParameterValue("role", role);
                            pars.setSingleParameterValue("application", reqstate.getApplicationName());
                            pars.setSingleParameterValue("handler", reqstate.getHandlerName());
                            SourceUtil.writeDOM(saveResource, 
                                               null, 
                                               pars,
                                               delta, 
                                               this.resolver, 
                                               "xml");

                            if (delta.getParentNode() != null) delta.getParentNode().removeChild(delta);
                            delta = null;

                            // cache the profile
                            // The profile is only cached if it is already in the cache!
                            // Why? During login the profile is build and cached, so it is in the cache.
                            // But: If a user logs in, the profile is cached.
                            // Now the admin logs in, changes the global profile and saves it.
                            // The cache is invalidated, including the user profile.
                            // Now the user changes his profile and saves it.
                            // If it now would be cached, it would be invalid as it would
                            // not reflect the changes by the admin.
                            // But if the old profile is still in the cache, nobody
                            // has changed a profile above.
                            // Note CZ: The above is correct, but for building the delta
                            // the "previous" profile is build and cached ! Thus we can
                            // easily cache the new profile.
//                            if (this.isProfileCached(profileID, conf)) {
                            this.cacheProfile(profileID, theProfile, conf); // cache it
                            // now the hardest part, clean up the cache
                            this.cleanUpCache(type, role, conf);
//                          }

                        }

                    } // end synchronized
                }
            } catch (javax.xml.transform.TransformerException local) {
                throw new ProcessingException("TransformerException: " + local, local);
            }
        }
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END changeProfile");
        }
    }

    /**
     * Change the number of the columns
     */
    private void changeColumns(DocumentFragment profile,
                               int oldNumber,
                               int columnNumber,
                               Node[] miscNodes)
    throws javax.xml.transform.TransformerException {
        // calling method is (hopefully) synced
        if (columnNumber < oldNumber) {
            // remove columns and all coplets to the first one
            Node columnNode;
            Node firstColumn = DOMUtil.getSingleNode(profile,
                        "profile/portal-profile/content/column[@position='1']/coplets", this.xpathProcessor);
            NodeList firstColumnCoplets = DOMUtil.getNodeListFromPath(firstColumn, new String[] {"coplet"});
            int copletsCount = (firstColumnCoplets == null ? 0 : firstColumnCoplets.getLength());
            for(int i = columnNumber + 1; i <= oldNumber; i++) {
                columnNode = miscNodes[7+i];
                if (columnNode != null) {
                    NodeList coplets = DOMUtil.getNodeListFromPath(columnNode, new String[] {"coplets","coplet"});
                    Node coplet;
                    if (coplets != null && coplets.getLength() > 0) {
                        for(int m = 0; m < coplets.getLength(); m++) {
                            coplet = coplets.item(m);
                            coplet.getParentNode().removeChild(coplet);
                            copletsCount++;
                            ((Element)coplet).setAttributeNS(null, "position", "" + copletsCount);
                            firstColumn.appendChild(coplet);
                        }
                    }
                    columnNode.getParentNode().removeChild(columnNode);
                    miscNodes[7+i] = null;
                }
            }
        } else if (columnNumber <= PortalConstants.MAX_COLUMNS) {
            // add new columns
            Node contentNode = DOMUtil.getFirstNodeFromPath(profile,
                        new String[] {"profile","portal-profile","content"}, false);
            Document doc = contentNode.getOwnerDocument();
            Element newColumn;
            Element el;
            for(int i = oldNumber + 1; i <= columnNumber; i++) {
                newColumn = doc.createElementNS(null, "column");
                newColumn.setAttributeNS(null, "position", ""+i);
                miscNodes[7+i] = newColumn;
                contentNode.appendChild(newColumn);
                el = doc.createElementNS(null, "width");
                el.appendChild(doc.createTextNode("5%"));
                newColumn.appendChild(el);
                el = doc.createElementNS(null, "coplets");
                newColumn.appendChild(el);
            }
        }
    }

    /**
     * Send SAX events to the next pipeline component.
     * The node is parsed and the events are send to
     * the next component in the pipeline.
     * @param node The tree to be included.
     */
    protected void sendEvents(XMLConsumer consumer, Node node)
    throws SAXException {
        IncludeXMLConsumer.includeNode(node, consumer, consumer);
    }

    /**
     * Get all users in a document fragment with the following children:
     * <users>
     *     <user>
     *         <ID>...</ID>
     *         <role>...</role> <!-- optional -->
     *         <data>
     *         ...
     *         </data>
     *     </user>
     *     ....
     * </users>
     * The document fragment might contain further nodes at the root!
     * If <code>role</code> is <code>null</code> all users are fetched,
     * otherwise only the users for this role.
     * If also ID is not null only the single user is fetched.
     */
    private Document getUsers(String role, String ID)
    throws IOException, ProcessingException, SAXException {
        // calling method is syned
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN getUsers role="+role+", ID="+ID);
        }
        RequestState reqstate = this.getRequestState();
        Document frag = null;
        Configuration conf = reqstate.getModuleConfiguration("single-role-user-management");
        if (conf != null) {

            // get load-users resource (optional)
            Configuration child = conf.getChild("load-users", false);
            if (child != null) {
                String loadUsersResource = child.getAttribute("uri", null);
                SourceParameters loadUsersResourceParameters = SourceParameters.create(child);
    
                if (loadUsersResource != null) {
                    SourceParameters parameters = (loadUsersResourceParameters == null) ? new SourceParameters()

                                                                             : loadUsersResourceParameters;
                    if (reqstate.getApplicationName() != null)
                        parameters.setSingleParameterValue("application", reqstate.getApplicationName());
                    if (ID != null) {
                        parameters.setSingleParameterValue("type", "user");
                        parameters.setSingleParameterValue("ID", ID);
                    } else {
                        parameters.setSingleParameterValue("type", "users");
                    }
                    if (role != null) parameters.setSingleParameterValue("role", role);
                    frag = this.loadResource(loadUsersResource, parameters);
        
                }
            }
        }
            
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END getUsers fragment="+(frag == null ? "null" : XMLUtils.serializeNode(frag, XMLUtils.createPropertiesForXML(false))));
        }
        return frag;
    }

    /**
     * Get all roles in a document fragment with the following children:
     * <roles>
     *     <role>...</role>
     *     ....
     * </roles>
     * The document fragment might contain further nodes at the root!
     */
    private Document getRoles()
    throws IOException, ProcessingException, SAXException {
        // calling method is syned
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN getRoles");
        }
        Document frag = null;

        RequestState reqstate = this.getRequestState();

        Configuration conf = reqstate.getModuleConfiguration("single-role-user-management");
        if (conf != null) {

            // get load-roles resource (optional)
            Configuration child = conf.getChild("load-roles", false);
            if (child != null) {
                String loadRolesResource = child.getAttribute("uri", null);
                SourceParameters loadRolesResourceParameters = SourceParameters.create(child);
                if (loadRolesResource != null) {
                    SourceParameters parameters = (loadRolesResourceParameters == null) ? new SourceParameters()
                                                                           : loadRolesResourceParameters;
                    if (reqstate.getApplicationName() != null)
                        parameters.setSingleParameterValue("application", reqstate.getApplicationName());
                    parameters.setSingleParameterValue("type", "roles");
                    frag = this.loadResource(loadRolesResource, parameters);
                }
            }
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END getRoles fragment="+frag);
        }
        return frag;
    }

    /**
     * Load XML resource
     */
    private Document loadResource(String resource,
                               SourceParameters parameters)
    throws IOException, ProcessingException, SAXException {
        Source source = null;
        try {
            source = SourceUtil.getSource(resource, 
                                          null, 
                                          parameters, 
                                          this.resolver);
            return SourceUtil.toDOM(source);
        } catch (SourceException se) {
            throw SourceUtil.handle(se);
        } finally {
            this.resolver.release(source);
        }
    }

    /**
     * Get the SessionManager component
     */
    protected SessionManager getSessionManager()
    throws ProcessingException {
        if (this.sessionManager == null) {
            try {
                this.sessionManager = (SessionManager)this.manager.lookup(SessionManager.ROLE);
            } catch (ComponentException ce) {
                throw new ProcessingException("Error during lookup of SessionManager component.", ce);
            }
        }
        return this.sessionManager;
    }

    /**
     * Get the ContextManager component
     */
    protected ContextManager getContextManager()
    throws ProcessingException {
        if (this.contextManager == null) {
            try {
                this.contextManager = (ContextManager)this.manager.lookup(ContextManager.ROLE);
            } catch (ComponentException ce) {
                throw new ProcessingException("Error during lookup of ContextManager component.", ce);
            }
        }
        return this.contextManager;
    }

    /**
     * Get the ContextManager component
     */
    protected TransactionManager getTransactionManager()
    throws ProcessingException {
        if (this.transactionManager == null) {
            try {
                this.transactionManager = (TransactionManager)this.manager.lookup(TransactionManager.ROLE);
            } catch (ComponentException ce) {
                throw new ProcessingException("Error during lookup of TransactionManager component.", ce);
            }
        }
        return this.transactionManager;
    }

}
