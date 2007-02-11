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
package org.apache.cocoon.environment.http;

import org.apache.cocoon.environment.Session;

import java.util.Enumeration;

/**
 *
 * Provides a way to identify a user across more than one page
 * request or visit to a Web site and to store information about that user.
 *
 * <p>Cocoon uses this interface to create a session
 * between a client and the "cocoon server". The session persists
 * for a specified time period, across more than one connection or
 * page request from the user. A session usually corresponds to one
 * user, who may visit a site many times. The server can maintain a
 * session in many ways such as using cookies or rewriting URLs.
 *
 * <p>This interface allows Cocoon to
 * <ul>
 * <li>View and manipulate information about a session, such as
 *     the session identifier, creation time, and last accessed time
 * <li>Bind objects to sessions, allowing user information to persist
 *     across multiple user connections
 * </ul>
 *
 * <p>Session information is scoped only to the current context
 * (<code>Context</code>), so information stored in one context
 * will not be directly visible in another.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: HttpSession.java,v 1.2 2003/12/23 15:28:32 joerg Exp $
 *
 */

public final class HttpSession
implements Session {

    javax.servlet.http.HttpSession wrappedSession;

    /**
     * Construct a new session from an HttpSession
     */
    public HttpSession(javax.servlet.http.HttpSession session) {
        this.wrappedSession = session;
    }

    /**
     *
     * Returns the time when this session was created, measured
     * in milliseconds since midnight January 1, 1970 GMT.
     *
     * @return                                a <code>long</code> specifying
     *                                         when this session was created,
     *                                        expressed in
     *                                        milliseconds since 1/1/1970 GMT
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        invalidated session
     *
     */
    public long getCreationTime() {
        return this.wrappedSession.getCreationTime();
    }

    /**
     *
     * Returns a string containing the unique identifier assigned
     * to this session. The identifier is assigned
     * by the context container and is implementation dependent.
     *
     * @return                                a string specifying the identifier
     *                                        assigned to this session
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        invalidated session
     *
     */
    public String getId() {
        return this.wrappedSession.getId();
    }

    /**
     *
     * Returns the last time the client sent a request associated with
     * this session, as the number of milliseconds since midnight
     * January 1, 1970 GMT.
     *
     * <p>Actions that your application takes, such as getting or setting
     * a value associated with the session, do not affect the access
     * time.
     *
     * @return                                a <code>long</code>
     *                                        representing the last time
     *                                        the client sent a request associated
     *                                        with this session, expressed in
     *                                        milliseconds since 1/1/1970 GMT
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        invalidated session
     *
     */

    public long getLastAccessedTime() {
        return this.wrappedSession.getLastAccessedTime();
    }

    /**
     *
     * Specifies the time, in seconds, between client requests before the
     * contextcontainer will invalidate this session.  A negative time
     * indicates the session should never timeout.
     *
     * @param interval                An integer specifying the number
     *                                 of seconds
     *
     */
    public void setMaxInactiveInterval(int interval) {
        this.wrappedSession.setMaxInactiveInterval(interval);
    }

   /**
    * Returns the maximum time interval, in seconds, that
    * the context container will keep this session open between
    * client accesses. After this interval, the context container
    * will invalidate the session.  The maximum time interval can be set
    * with the <code>setMaxInactiveInterval</code> method.
    * A negative time indicates the session should never timeout.
    *
    *
    * @return                an integer specifying the number of
    *                        seconds this session remains open
    *                        between client requests
    *
    * @see                #setMaxInactiveInterval(int)
    *
    *
    */
    public int getMaxInactiveInterval() {
        return this.wrappedSession.getMaxInactiveInterval();
    }

    /**
     *
     * Returns the object bound with the specified name in this session, or
     * <code>null</code> if no object is bound under the name.
     *
     * @param name                a string specifying the name of the object
     *
     * @return                        the object with the specified name
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        invalidated session
     *
     */
    public Object getAttribute(String name) {
        return this.wrappedSession.getAttribute(name);
    }

    /**
     *
     * Returns an <code>Enumeration</code> of <code>String</code> objects
     * containing the names of all the objects bound to this session.
     *
     * @return                        an <code>Enumeration</code> of
     *                                <code>String</code> objects specifying the
     *                                names of all the objects bound to
     *                                this session
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        invalidated session
     *
     */
    public Enumeration getAttributeNames() {
        return this.wrappedSession.getAttributeNames();
    }

    /**
     * Binds an object to this session, using the name specified.
     * If an object of the same name is already bound to the session,
     * the object is replaced.
     *
     *
     * @param name                        the name to which the object is bound;
     *                                        cannot be null
     *
     * @param value                        the object to be bound; cannot be null
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        invalidated session
     *
     */
    public void setAttribute(String name, Object value) {
        this.wrappedSession.setAttribute(name, value);
    }

    /**
     *
     * Removes the object bound with the specified name from
     * this session. If the session does not have an object
     * bound with the specified name, this method does nothing.
     *
     *
     * @param name                                the name of the object to
     *                                                remove from this session
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        invalidated session
     */
    public void removeAttribute(String name) {
        this.wrappedSession.removeAttribute(name);
    }

    /**
     *
     * Invalidates this session
     * to it.
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        already invalidated session
     *
     */
    public void invalidate() {
        this.wrappedSession.invalidate();
    }

    /**
     *
     * Returns <code>true</code> if the client does not yet know about the
     * session or if the client chooses not to join the session.  For
     * example, if the server used only cookie-based sessions, and
     * the client had disabled the use of cookies, then a session would
     * be new on each request.
     *
     * @return                                 <code>true</code> if the
     *                                        server has created a session,
     *                                        but the client has not yet joined
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        already invalidated session
     *
     */
    public boolean isNew() {
        return this.wrappedSession.isNew();
    }

}

