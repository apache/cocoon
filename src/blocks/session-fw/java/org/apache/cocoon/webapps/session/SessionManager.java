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
 * @version CVS $Id: SessionManager.java,v 1.1 2003/05/04 20:19:41 cziegeler Exp $
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
