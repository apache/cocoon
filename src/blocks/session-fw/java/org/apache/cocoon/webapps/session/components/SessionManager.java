/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.webapps.session.components;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.Recomposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.RequestLifecycleComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.webapps.session.SessionConstants;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.apache.cocoon.webapps.session.context.SessionContextProvider;
import org.apache.cocoon.webapps.session.context.SimpleSessionContext;
import org.apache.cocoon.webapps.session.context.StandardSessionContextProvider;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *  This is the basic session component.
 *
 *  The main purpose of this component is session handling, maintaining contexts
 *  and providing system management functions.
 *  The session information is divided into session contexts.
 *
 *  Transaction management<p>
 *  </p>
 *  Transactions are a series of get/set calls to a sessuib context which must
 *  be seen as atomic (single modification).
 *  We distingish between reading and writing. Usually parallel reading is
 *  allowed but if one thread wants to write, no other can read or write.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: SessionManager.java,v 1.1 2003/03/09 00:06:08 pier Exp $
*/
public final class SessionManager
extends AbstractLogEnabled
implements Composable, Component, Recomposable, Recyclable, RequestLifecycleComponent {

    /** Avalon role */
    public static final String ROLE = SessionManager.class.getName();;

    /** This session attribute is used to store the information for the inputxml tags */
    private static final String ATTRIBUTE_INPUTXML_STORAGE= "org.apache.cocoon.webapps.session.InputXMLStorage";

    /** The <code>ComponentManager</code> */
    private ComponentManager manager;

    /** The request */
    private Request    request;
    /** The response */
    private Response   response;
    /** The object model */
    private Map        objectModel;
    /** The resolver */
    private SourceResolver resolver;

    /** SessionContexts */
    private Map  contexts;
    /** TransactionStates of the session contexts */
    private Map  transactionStates;

    /** Session contexts delivered by a provider */
    private Map  deliveredContexts;

    /** Registered context provider */
    private static Map contextProvider = new HashMap();

    /* The list of reserved contexts */
    static private String[] reservedContextNames = {"session",
                                                    "context"};
    /** Init the class,
     *  add the provider for the temp context
     */
    static {
        // add the provider for the portal context
        SessionContextProvider provider = new StandardSessionContextProvider();
        try {
            SessionManager.addSessionContextProvider(provider, SessionConstants.TEMPORARY_CONTEXT);
            SessionManager.addSessionContextProvider(provider, SessionConstants.REQUEST_CONTEXT);
            SessionManager.addSessionContextProvider(provider, SessionConstants.RESPONSE_CONTEXT);
        } catch (ProcessingException local) {
            throw new CascadingRuntimeException("Unable to register provider for standard contexts.", local);
        }
    }

    /**
     * Avalon Composer Interface
     */
    public void compose(ComponentManager manager) {
        this.manager = manager;
    }

    /**
     * Recomposable
     */
    public void recompose( ComponentManager componentManager )
    throws ComponentException {
        this.manager = componentManager;
    }

    /**
     * Set the <code>SourceResolver</code>, objectModel <code>Map</code>,
     * used to process the request.
     *  Set up the SessionManager.
     *  This method is automatically called for each request. Do not invoke
     *  this method by hand.
     */
    public void setup(SourceResolver resolver, Map objectModel)
    throws ProcessingException, SAXException, IOException {
        // no sync required
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN setup objectModel=" + objectModel);
        }
        this.objectModel = objectModel;
        this.request = ObjectModelHelper.getRequest(objectModel);
        this.response = ObjectModelHelper.getResponse(objectModel);
        this.resolver = resolver;

        this.processInputFields();

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END setup");
        }
    }

    /**
     *  Recycling the SessionManager.
     *  This method is automatically called after each request. Do not invoke
     *  this method by hand.
     */
    public void recycle() {
        this.objectModel = null;
        this.request = null;
        this.response = null;
        this.contexts = null;
        if (this.deliveredContexts != null) {
            this.deliveredContexts.clear();
        }
        this.transactionStates = null;
        this.resolver = null;
    }

    /**
     * Get the list of contexts
     */
    protected Map getSessionContexts() {
        if (this.contexts == null) {
            Session session = this.getSession(true);
            this.contexts = (Map)session.getAttribute("org.apache.cocoon.webapps.session.context.SessionContext");
            if (this.contexts == null) {
                this.contexts = new HashMap(5, 3);
                session.setAttribute("org.apache.cocoon.webapps.session.context.SessionContext", this.contexts);
            }
        }
        return this.contexts;
    }

    /**
     * Get the list of contexts
     */
    protected Map getSessionContextsTransactionStates() {
        if (this.transactionStates == null) {
            Session session = this.getSession(true);
            this.transactionStates = (Map)session.getAttribute("org.apache.cocoon.webapps.session.context.TransactionState");
            if (this.transactionStates == null) {
                this.transactionStates = new HashMap(5, 3);
                session.setAttribute("org.apache.cocoon.webapps.session.context.TransactionState", this.transactionStates);
            }
        }
        return this.transactionStates;
    }

    /**
     * Checks if the context name is a reserved context.
     */
    static boolean isReservedContextName(String name) {
        // synchronized (not needed)
        int     i, l;
        boolean found;
        found = false;
        i = 0;
        l = reservedContextNames.length;
        while (i < l && found == false) {
            found = reservedContextNames[i].equals(name);
            i++;
        }
        if (found == false) {
            found = (contextProvider.get(name) != null);
        }
        return found;
    }

    /**
     * Add a context provider.
     */
    public static synchronized void addSessionContextProvider(SessionContextProvider provider,
                                                              String                 contextName)
    throws ProcessingException {
        if (contextName != null && provider != null) {
            if (isReservedContextName(contextName) == true) {
                throw new ProcessingException("Unable to register context '"+contextName+"' : Already registered.");
            }
            contextProvider.put(contextName, provider);
        } else {
            throw new ProcessingException("Unable to add new provider: Name or provider info missing.");
        }
    }

    /**
     * Get a reserved context
     */
    protected boolean existsReservedContext(String name) {
        // synchronized (not needed)
        SessionContext context = null;
        SessionContextProvider provider = (SessionContextProvider)contextProvider.get(name);
        if (provider != null) {
            if ( null != this.deliveredContexts ) {
                context = (SessionContext)this.deliveredContexts.get(name);
            }
        }

        return (context != null);
    }

    /**
     * Get a reserved context
     */
    protected SessionContext getReservedContext(String name)
    throws ProcessingException {
        // synchronized (not needed)
        SessionContext context = null;
        SessionContextProvider provider = (SessionContextProvider)contextProvider.get(name);
        if (provider != null) {
            if ( null != this.deliveredContexts ) {
                context = (SessionContext)this.deliveredContexts.get(name);
            }
            if (context == null) {
                if ( null == this.deliveredContexts ) {
                    this.deliveredContexts = new HashMap(5);
                }
                context = provider.getSessionContext(name,
                                                     this.objectModel,
                                                     this.resolver,
                                                     this.manager);
                if (context != null) this.deliveredContexts.put(name, context);
            }
        }

        return context;
    }

    /**
     *  Create a new session for the user.
     *  A new session is created for this user. If the user has already a session,
     *  no new session is created and the old one is returned.
     */
    public Session createSession() {
        // synchronized
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN createSession");
        }
        Session session = this.getSession(true);

        if (this.getLogger().isDebugEnabled() == true) {
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
        // synchronized
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN getSession create=" + createFlag);
        }
        Session session = this.request.getSession(createFlag);

        if (this.getLogger().isDebugEnabled() == true) {
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
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN terminateSession force="+force);
        }

        Session session = request.getSession(false);
        if (session != null) {
            if (force == true
                || this.hasSessionContext() == false) {
                synchronized(session) {
                    session.invalidate();
                }
            }
        }
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END terminateSession");
        }
    }

    /**
     *  Create a new public context in the session.
     *  Create a new public session context for this user. If this context
     *  already exists no new context is created and the old one will be used
     *  instead.
     */
    public SessionContext createContext(String name, String loadURI, String saveURI)
    throws IOException, SAXException, ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN createContext name=" + name +
                                   "load=" + loadURI +
                                   "save=" + saveURI);
        }
        // test arguments
        Session session = this.getSession(false);
        if (session == null) {
            throw new ProcessingException("CreateContext: Session is required");
        }
        if (name == null) {
            throw new ProcessingException("CreateContext: Name is required");
        }

        SessionContext context;
        synchronized(session) {
            // test for reserved context
            if (SessionManager.isReservedContextName(name) == true) {
                throw new ProcessingException("SessionContext with name " + name + " is reserved and cannot be created manually.");
            }

            Map contexts = this.getSessionContexts();
            if (this.existsContext(name) == true) {
                context = this.getContext(name);
            } else {
                context = new SimpleSessionContext();
                context.setup(name, loadURI, saveURI);
                contexts.put(name, context);
                this.getSessionContextsTransactionStates().put(context, new TransactionState());
            }
        }
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END createContext context="+context);
        }

        return context;
    }

    /**
     *  Delete a public context in the session.
     *  If the context exists for this user, it and all of its information
     *  is deleted.
     */
    public void deleteContext(String name)
    throws ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN deleteContext name=" + name);
        }


        // test arguments
        if (name == null) {
            throw new ProcessingException("SessionManager.deleteContext: Name is required");
        }
        if (SessionManager.isReservedContextName(name) == true) {
            throw new ProcessingException("SessionContext with name " + name + " is reserved and cannot be deleted manually.");
        }
        Session session = this.getSession(false);
        if (session == null) {
            throw new ProcessingException("SessionManager.deleteContext: Session is required");
        }

        synchronized(session) {
            final Map contexts = this.getSessionContexts();
            if (contexts.containsKey(name) == true) {
                SessionContext context = (SessionContext)contexts.get(name);
                contexts.remove(name);
                this.getSessionContextsTransactionStates().remove(context);
            }
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END deleteContext");
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
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN getContextFragment name=" + contextName + ", path=" + path);
        }

        // test arguments
        if (contextName == null) {
            throw new ProcessingException("SessionManager.getContextFragment: Name is required");
        }
        if (path == null) {
            throw new ProcessingException("SessionManager.getContextFragment: Path is required");
        }

        SessionContext context;
        if (SessionManager.isReservedContextName(contextName) == true) {
            context = this.getReservedContext(contextName);
        } else {
            Session session = this.getSession(false);
            if (session == null) {
                throw new ProcessingException("SessionManager.getContextFragment: Session is required for context " + contextName);
            }
            context = this.getContext(contextName);
        }
        if (context == null) {
            throw new ProcessingException("SessionManager.getContextFragment: Context '" + contextName + "' not found.");
        }

        DocumentFragment frag;
        frag = context.getXML(path);

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END getContextFragment documentFragment=" + (frag == null ? "null" : XMLUtils.serializeNodeToXML(frag)));
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
        if (this.getLogger().isDebugEnabled() == true) {
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

        SessionContext context = this.getContext(contextName);
        if (context == null) {
            throw new ProcessingException("SessionManager.streamContextFragment: Context '" + contextName + "' not found.");
        }

        streamed = context.streamXML(path, consumer, consumer);

        if (this.getLogger().isDebugEnabled() == true) {
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

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN setContextFragment name=" + contextName + ", path=" + path +
               ", fragment=" + (fragment == null ? "null" : XMLUtils.serializeNodeToXML(fragment)));
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
        SessionContext context = this.getContext(contextName);

        // check context
        if (context == null) {
            throw new ProcessingException("SessionManager.setContextFragment: Context '" + contextName + "' not found.");
        }

        context.setXML(path, fragment);

        if (this.getLogger().isDebugEnabled() == true) {
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
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN appendContextFragment name=" + contextName +
                              ", path=" + path +
                              ", fragment=" + (fragment == null ? "null" : XMLUtils.serializeNodeToXML(fragment)));
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
        SessionContext context = this.getContext(contextName);

        // check context
        if (context == null) {
            throw new ProcessingException("SessionManager.appendContextFragment: Context '" + contextName + "' not found.");
        }

        context.appendXML(path, fragment);

        if (this.getLogger().isDebugEnabled() == true) {
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
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN mergeContextFragment name=" + contextName + ", path=" + path +
                ", fragment=" + (fragment == null ? "null" : XMLUtils.serializeNodeToXML(fragment)));
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
        SessionContext context = this.getContext(contextName);

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

        if (this.getLogger().isDebugEnabled() == true) {
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
        if (this.getLogger().isDebugEnabled() == true) {
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
        SessionContext context = this.getContext(contextName);

        // check context
        if (context == null) {
            throw new ProcessingException("SessionManager.removeContextFragment: Context '" + contextName + "' not found.");
        }

        context.removeXML(path);

        if (this.getLogger().isDebugEnabled() == true) {
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
                            this.importNode((Element)currentProfile, (Element)currentDelta, preserve);
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

    /**
     * Get the url for the request.
     */
    public static String getURL(Request req) {
        // no need for synchronized
        boolean isDefaultPort = "http".equalsIgnoreCase(req.getScheme())
                                    && 80 == req.getServerPort();
        return req.getScheme() + "://" +
               req.getServerName() +
               (isDefaultPort ? "" : ":" + req.getServerPort()) +
               req.getContextPath() +
               req.getServletPath() +
               (req.getPathInfo() == null ? "" : req.getPathInfo());
    }

    /**
     * Register input field and return the current value of the field.
     * This is a private method and should not be invoked directly.
     */
    public DocumentFragment registerInputField(String contextName,
                                               String path,
                                               String name,
                                               String formName)
    throws ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN registerInputField context="+contextName+", path="+path+", name="+name+", formName="+formName);
        }

        // test arguments
        if (contextName == null) {
            throw new ProcessingException("SessionManager.registerInputField: Context Name is required");
        }
        if (path == null) {
            throw new ProcessingException("SessionManager.registerInputField: Path is required");
        }
        if (name == null) {
            throw new ProcessingException("SessionManager.registerInputField: Name is required");
        }
        if (formName == null) {
            throw new ProcessingException("SessionManager.registerInputField: Form is required");
        }

        DocumentFragment value = null;
        SessionContext context = this.getContext(contextName);
        if (context == null) {
            throw new ProcessingException("SessionManager.registerInputField: Context not found " + contextName);
        }
        Session session = this.getSession(false);
        if (session == null) {
            throw new ProcessingException("SessionManager.registerInputField: Session is required for context " + contextName);
        }

        synchronized(session) {
            Map inputFields = (Map)session.getAttribute(SessionManager.ATTRIBUTE_INPUTXML_STORAGE);
            if (inputFields == null) {
                inputFields = new HashMap(10);
                session.setAttribute(SessionManager.ATTRIBUTE_INPUTXML_STORAGE, inputFields);
            }
            inputFields.put(name, new Object[] {context, path, formName});
            value = context.getXML(path);
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END registerInputField value="+value);
        }
        return value;
    }

    /**
     * Process all input fields.
     * The fields are removed even if the request did not contain
     * any values.
     * This is a private method and should not be invoked directly.
     */
    public void processInputFields()
    throws ProcessingException {
        // synchronized
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN processInputFields");
        }

        final String formName = this.request.getParameter(SessionConstants.SESSION_FORM_PARAMETER);
        if ( null != formName ) {
            final Session session = this.getSession(false);
            if (session != null) {
                synchronized(session) {
                    final Map inputFields = (Map)session.getAttribute(SessionManager.ATTRIBUTE_INPUTXML_STORAGE);
                    if (inputFields != null) {
                        final Enumeration keys = this.request.getParameterNames();
                        String   currentKey;
                        Object[] contextAndPath;

                        while (keys.hasMoreElements() == true) {
                            currentKey = (String)keys.nextElement();
                            if (inputFields.containsKey(currentKey) == true) {
                                contextAndPath = (Object[])inputFields.get(currentKey);
                                inputFields.remove(currentKey);

                                SessionContext context = (SessionContext)contextAndPath[0];
                                String path            = (String)contextAndPath[1];

                                if (formName.equals(contextAndPath[2]) == true) {
                                    context.setXML(path,
                                                 this.getContextFragment(SessionConstants.REQUEST_CONTEXT, "/parameter/"+currentKey));
                                }
                            }
                        }
                    }
                    // inputFields.clear();
                }
            }
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END processInputFields");
        }
    }

    /**
     *  Get a public context.
     *  The session context with the given name is returned. If the context does
     *  not exist <CODE>null</CODE> is returned.
     */
    public synchronized SessionContext getContext(String name)
    throws ProcessingException {
        SessionContext context;
        if (SessionManager.isReservedContextName(name) == true) {
            context = this.getReservedContext(name);
        } else {
            Session session = this.getSession(false);
            if (session == null) {
                throw new ProcessingException("SessionManager.getContext: Session is required.");
            }
            synchronized (session) {
                final Map contexts = this.getSessionContexts();
                context = (SessionContext)contexts.get(name);
            }
        }

        return context;
    }

    /**
     * Check if a context exists
     */
    public synchronized boolean hasSessionContext() 
    throws ProcessingException {
        Session session = this.getSession(false);
        if (session == null) {
            throw new ProcessingException("SessionManager.hasSessionContext: Session is required.");
        }
        synchronized (session) {
            final Map contexts = this.getSessionContexts();
            return !(contexts.isEmpty());
        }
    }

    /**
     *  Check if a public context exists.
     *  If the session context with the given name exists, <CODE>true</CODE> is
     *  returned.
     */
    public synchronized boolean existsContext(String name) 
    throws ProcessingException {
        Session session = this.getSession(false);
        if (session == null) {
            throw new ProcessingException("SessionManager.existsContext: Session is required.");
        }
        synchronized (session) {
            final Map contexts = this.getSessionContexts();
            boolean result = contexts.containsKey(name);
            if (result == false && SessionManager.isReservedContextName(name) == true) {
                result = this.existsReservedContext(name);
            }
            return result;
        }
    }

    private class TransactionState {
        /** number readers reading*/
        public int nr=0;
        /** number of readers total (reading or waiting to read)*/
        public int nrtotal=0;
        /** number writers writing, 0 or 1 */
        public int nw=0;
        /** number of writers total (writing or waiting to write)*/
        public int nwtotal=0;
    }

    /**
     *  Reset the transaction management state.
     */
    public void resetTransactions(SessionContext context) {
         TransactionState ts = (TransactionState)this.getSessionContextsTransactionStates().get(context);
        ts.nr=0;
        ts.nrtotal=0;
        ts.nw=0;
        ts.nwtotal=0;
    }

    /**
     *  Start a reading transaction.
     *  This call must always be matched with a stopReadingTransaction().
     *  Otherwise the session context is blocked.
     */
    public synchronized void startReadingTransaction(SessionContext context)
    throws ProcessingException {
         TransactionState ts = (TransactionState)this.getSessionContextsTransactionStates().get(context);
        ts.nrtotal++;
        while (ts.nw!=0) {
            try {
                wait();
            } catch (InterruptedException local) {
                throw new ProcessingException("Interrupted", local);
            }
        }
        ts.nr++;
    }

    /**
     *  Stop a reading transaction.
     *  This call must always be done for each startReadingTransaction().
     *  Otherwise the session context is blocked.
     */
    public synchronized void stopReadingTransaction(SessionContext context) {
        TransactionState ts = (TransactionState)this.getSessionContextsTransactionStates().get(context);
        ts.nr--;
        ts.nrtotal--;
        if (ts.nrtotal==0) notify();
    }

    /**
     *  Start a writing transaction.
     *  This call must always be matched with a stopWritingTransaction().
     *  Otherwise the session context is blocked.
     */
     public synchronized void startWritingTransaction(SessionContext context)
     throws ProcessingException {
         TransactionState ts = (TransactionState)this.getSessionContextsTransactionStates().get(context);
         ts.nwtotal++;
         while (ts.nrtotal+ts.nw != 0) {
            try {
                wait();
            } catch (InterruptedException local) {
                throw new ProcessingException("Interrupted", local);
            }
        }
        ts.nw=1;
     }

    /**
     *  Stop a writing transaction.
     *  This call must always be done for each startWritingTransaction().
     *  Otherwise the session context is blocked.
     */
    public synchronized void stopWritingTransaction(SessionContext context) {
        TransactionState ts = (TransactionState)this.getSessionContextsTransactionStates().get(context);
        ts.nw=0;
        ts.nwtotal--;
        notifyAll();
    }

}
