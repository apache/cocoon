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
package org.apache.cocoon.webapps.session.components;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.webapps.session.ContextManager;
import org.apache.cocoon.webapps.session.SessionConstants;
import org.apache.cocoon.webapps.session.SessionManager;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *  This is the default implementation of the session manager
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: DefaultSessionManager.java,v 1.6 2004/03/17 12:09:51 cziegeler Exp $
*/
public final class DefaultSessionManager
extends AbstractLogEnabled
implements Serviceable, Component, ThreadSafe, SessionManager, Disposable, Contextualizable {

    /** The context */
    private Context context;
    
    /** The <code>ServiceManager</code> */
    private ServiceManager manager;

    /** The context manager */
    private ContextManager contextManager;
    
    /**
    * Avalon Serviceable Interface
    */
   public void service(ServiceManager manager) 
   throws ServiceException {
       this.manager = manager;
       this.contextManager = (ContextManager)this.manager.lookup(ContextManager.ROLE);
   }

    /**
     * Avalon Disposable Interface
     */
    public void dispose() {
        if (this.manager != null ) {
            this.manager.release(this.contextManager);
            this.manager = null;
            this.contextManager = null;
        }
    }
    /**
     *  Create a new session for the user.
     *  A new session is created for this user. If the user has already a session,
     *  no new session is created and the old one is returned.
     */
    public Session createSession() {
        // synchronized
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN createSession");
        }
        Session session = this.getSession(true);

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END createSession session=" + session);
        }
        return session;
    }

    /**
     * Get the session for the current user.
     * If the user has no session right now, <CODE>null</CODE> is returned.
     * If createFlag is true, the session is created if it does not exist.
     */
    public Session getSession(boolean createFlag) {
        final Request request = ContextHelper.getRequest(this.context);

        // synchronized
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN getSession create=" + createFlag);
        }
        Session session = request.getSession(createFlag);

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END getSession session=" + session);
        }

        return session;
    }

    /**
     *  Terminate the current session.
     *  If the user has a session, this session is terminated and all of its
     *  data is deleted.
     *  @param force If this is set to true the session is terminated, if
     *                   it is set to false, the session is only terminated
     *                   if no session context is available.
     */
    public void terminateSession(boolean force) 
    throws ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN terminateSession force="+force);
        }

        Session session = this.getSession( false );
        if (session != null) {
            if (force || this.contextManager.hasSessionContext() ) {
                synchronized(session) {
                    session.invalidate();
                }
            }
        }
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END terminateSession");
        }
    }

    /**
     * Get information from the context.
     * A document fragment containg the xml data stored in the session context
     * with the given name is returned. If the information is not available,
     * <CODE>null</CODE> is returned.
     * @param contextName The name of the public context.
     * @param path        XPath expression specifying which data to get.
     * @return A DocumentFragment containing the data or <CODE>null</CODE>
     */
    public DocumentFragment getContextFragment(String  contextName,
                                                     String  path)
    throws ProcessingException  {
        // synchronized via context
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN getContextFragment name=" + contextName + ", path=" + path);
        }

        // test arguments
        if (contextName == null) {
            throw new ProcessingException("SessionManager.getContextFragment: Name is required");
        }
        if (path == null) {
            throw new ProcessingException("SessionManager.getContextFragment: Path is required");
        }

        SessionContext context = this.contextManager.getContext( contextName );

        if (context == null) {
            throw new ProcessingException("SessionManager.getContextFragment: Context '" + contextName + "' not found.");
        }

        DocumentFragment frag;
        frag = context.getXML(path);

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END getContextFragment documentFragment=" + (frag == null ? "null" : XMLUtils.serializeNode(frag, XMLUtils.createPropertiesForXML(false))));
        }
        return frag;
    }

    /**
     * Stream public context data.
     * The document fragment containing the data from a path in the
     * given context is streamed to the consumer.
     *
     * @param contextName The name of the public context.
     * @param path        XPath expression specifying which data to get.
     *
     * @return If the data is available <code>true</code> is returned,
     *         otherwise <code>false</code> is returned.
     */
    public boolean streamContextFragment(String  contextName,
                                           String  path,
                                           XMLConsumer consumer)
    throws SAXException, ProcessingException  {
        // synchronized via context
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN streamContextFragment name=" + contextName + ", path=" + path + ", consumer"+consumer);
        }
        boolean streamed = false;

        // test arguments
        if (contextName == null) {
            throw new ProcessingException("SessionManager.streamContextFragment: Name is required");
        }
        if (path == null) {
            throw new ProcessingException("SessionManager.streamContextFragment: Path is required");
        }

        SessionContext context = this.contextManager.getContext( contextName );

        if (context == null) {
            throw new ProcessingException("SessionManager.streamContextFragment: Context '" + contextName + "' not found.");
        }

        streamed = context.streamXML(path, consumer, consumer);

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END streamContextFragment streamed=" + streamed);
        }
        return streamed;
    }

    /**
     * Set data in a public context.
     * The document fragment containing the data is set at the given path in the
     * public session context.
     *
     * @param contextName The name of the public context.
     * @param path        XPath expression specifying where to set the data.
     * @param fragment    The DocumentFragment containing the data.
     *
     */
    public void setContextFragment(String  contextName,
                                    String  path,
                                    DocumentFragment fragment)
    throws ProcessingException  {
        // synchronized via context

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN setContextFragment name=" + contextName + ", path=" + path +
               ", fragment=" + (fragment == null ? "null" : XMLUtils.serializeNode(fragment, XMLUtils.createPropertiesForXML(false))));
        }
        // test arguments
        if (contextName == null) {
            throw new ProcessingException("SessionManager.setContextFragment: Name is required");
        }
        if (path == null) {
            throw new ProcessingException("SessionManager.setContextFragment: Path is required");
        }
        if (fragment == null) {
            throw new ProcessingException("SessionManager.setContextFragment: Fragment is required");
        }

        // get context
        SessionContext context = this.contextManager.getContext( contextName );

        // check context
        if (context == null) {
            throw new ProcessingException("SessionManager.setContextFragment: Context '" + contextName + "' not found.");
        }

        context.setXML(path, fragment);

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END setContextFragment");
        }
    }

    /**
     * Append data in a public context.
     * The document fragment containing the data is appended at the given
     * path in the public session context.
     *
     * @param contextName The name of the public context.
     * @param path        XPath expression specifying where to append the data.
     * @param fragment    The DocumentFragment containing the data.
     *
     */
    public void appendContextFragment(String  contextName,
                                       String  path,
                                       DocumentFragment fragment)
    throws ProcessingException  {
        // synchronized via context
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN appendContextFragment name=" + contextName +
                              ", path=" + path +
                              ", fragment=" + (fragment == null ? "null" : XMLUtils.serializeNode(fragment, XMLUtils.createPropertiesForXML(false))));
        }
        // test arguments
        if (contextName == null) {
            throw new ProcessingException("SessionManager.appendContextFragment: Name is required");
        }
        if (path == null) {
            throw new ProcessingException("SessionManager.appendContextFragment: Path is required");
        }
        if (fragment == null) {
            throw new ProcessingException("SessionManager.appendContextFragment: Fragment is required");
        }

        // get context
        SessionContext context = this.contextManager.getContext( contextName );

        // check context
        if (context == null) {
            throw new ProcessingException("SessionManager.appendContextFragment: Context '" + contextName + "' not found.");
        }

        context.appendXML(path, fragment);

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END appendContextFragment");
        }
    }

    /**
     * Merge data in a public context.
     * The document fragment containing the data is merged at the given
     * path in the public session context.
     *
     * @param contextName The name of the public context.
     * @param path        XPath expression specifying where to merge the data.
     * @param fragment    The DocumentFragment containing the data.
     *
     */
    public void mergeContextFragment(String  contextName,
                                      String  path,
                                      DocumentFragment fragment)
    throws ProcessingException  {
        // synchronized via context
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN mergeContextFragment name=" + contextName + ", path=" + path +
                ", fragment=" + (fragment == null ? "null" : XMLUtils.serializeNode(fragment, XMLUtils.createPropertiesForXML(false))));
        }

        // test arguments
        if (contextName == null) {
            throw new ProcessingException("SessionManager.mergeContextFragment: Name is required");
        }
        if (path == null) {
            throw new ProcessingException("SessionManager.mergeContextFragment: Path is required");
        }
        if (fragment == null) {
            throw new ProcessingException("SessionManager.mergeContextFragment: Fragment is required");
        }

        // get context
        SessionContext context = this.contextManager.getContext( contextName );

        // check context
        if (context == null) {
            throw new ProcessingException("SessionManager.mergeContextFragment: Context '" + contextName + "' not found.");
        }

        Node contextNode = context.getSingleNode(path);
        if (contextNode == null) {
            // no merge required
            context.setXML(path, fragment);
        } else {
            this.importNode(contextNode, fragment, false);
            context.setNode(path, contextNode);
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END mergeContextFragment");
        }
    }

    /**
     * Remove data in a public context.
     * The data specified by the path is removed from the public session context.
     *
     * @param contextName The name of the public context.
     * @param path        XPath expression specifying where to merge the data.
     *
     */
    public void removeContextFragment(String  contextName,
                                        String  path)
    throws ProcessingException  {
        // synchronized via context
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN removeContextFragment name=" + contextName + ", path=" + path);
        }
        // test arguments
        if (contextName == null) {
            throw new ProcessingException("SessionManager.removeContextFragment: Name is required");
        }
        if (path == null) {
            throw new ProcessingException("SessionManager.removeContextFragment: Path is required");
        }

        // get context
        SessionContext context = this.contextManager.getContext( contextName );

        // check context
        if (context == null) {
            throw new ProcessingException("SessionManager.removeContextFragment: Context '" + contextName + "' not found.");
        }

        context.removeXML(path);

        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("END removeContextFragment");
        }
    }

    /**
     * Import nodes. If preserve is set to true, the nodes
     * marked with cocoon:preserve are always imported
     * overwriting others!
     */
    private void importNode(Node profile, Node delta, boolean preserve) {
        // no sync req
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
                while (found == false && m < l) {
                    currentProfile = profileChilds.item(m);
                    if (currentProfile.getNodeType() == Node.ELEMENT_NODE
                        && currentProfile.getNodeName().equals(currentDelta.getNodeName()) == true) {

                        // now we have found a node with the same name
                        // next: the attributes must match also
                        found = DOMUtil.compareAttributes((Element)currentProfile, (Element)currentDelta);
                    }
                    if (found == false) m++;
                }
                if (found == true) {
                    // this is not new

                    if (preserve == true
                        && ((Element)currentDelta).hasAttributeNS(SessionConstants.SESSION_NAMESPACE_URI, "preserve")
                        && ((Element)currentDelta).getAttributeNS(SessionConstants.SESSION_NAMESPACE_URI, "preserve").equalsIgnoreCase("true")) {
                        // replace the original with the delta
                        profile.replaceChild(profile.getOwnerDocument().importNode(currentDelta, true),
                                              currentProfile);
                    } else {
                        // do we have elements as children or text?
                        if (currentDelta.hasChildNodes() == true) {
                            currentDelta.normalize();
                            currentProfile.normalize();
                            // do a recursive call for sub elements
                            this.importNode(currentProfile, currentDelta, preserve);
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
                    }
                } else {
                    profile.appendChild(profile.getOwnerDocument().importNode(currentDelta, true));
                }
            }

        }

    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

}
