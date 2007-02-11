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
package org.apache.cocoon.webapps.session;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.xml.XMLConsumer;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.SAXException;

/** 
 * 
 *  This is the session manager component.
 *
 *  The main purpose of this component is creating and termination sessions
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SessionManager.java,v 1.2 2004/03/05 13:02:22 bdelacretaz Exp $
*/
public interface SessionManager {

    /** Avalon role */
    String ROLE = SessionManager.class.getName();;

    /**
     *  Create a new session for the user.
     *  A new session is created for this user. If the user has already a session,
     *  no new session is created and the old one is returned.
     */
    Session createSession();

    /**
     * Get the session for the current user.
     * If the user has no session right now, <CODE>null</CODE> is returned.
     * If createFlag is true, the session is created if it does not exist.
     */
    Session getSession(boolean createFlag);

    /**
     *  Terminate the current session.
     *  If the user has a session, this session is terminated and all of its
     *  data is deleted.
     *  @param force If this is set to true the session is terminated, if
     *                   it is set to false, the session is only terminated
     *                   if no session context is available.
     */
    void terminateSession(boolean force) 
    throws ProcessingException;


    /**
     * Get information from the context.
     * A document fragment containg the xml data stored in the session context
     * with the given name is returned. If the information is not available,
     * <CODE>null</CODE> is returned.
     * @param contextName The name of the public context.
     * @param path        XPath expression specifying which data to get.
     * @return A DocumentFragment containing the data or <CODE>null</CODE>
     */
    DocumentFragment getContextFragment(String  contextName,
                                        String  path)
    throws ProcessingException;

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
    boolean streamContextFragment(String  contextName,
                                  String  path,
                                  XMLConsumer consumer)
    throws SAXException, ProcessingException;

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
    void setContextFragment(String  contextName,
                            String  path,
                            DocumentFragment fragment)
    throws ProcessingException;

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
    void appendContextFragment(String  contextName,
                                String  path,
                                DocumentFragment fragment)
    throws ProcessingException;

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
    void mergeContextFragment(String  contextName,
                               String  path,
                               DocumentFragment fragment)
    throws ProcessingException;

    /**
     * Remove data in a public context.
     * The data specified by the path is removed from the public session context.
     *
     * @param contextName The name of the public context.
     * @param path        XPath expression specifying where to merge the data.
     *
     */
    void removeContextFragment(String  contextName,
                                String  path)
    throws ProcessingException;

}
