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
package org.apache.cocoon.environment.portlet;

import org.apache.cocoon.environment.Session;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Provides access to the JSR-168 (Portlet) environment session.
 *
 * <p>Portlet scope and application scope session attributes are differentiated
 * using attribute name prefix, {@link PortletEnvironment#SESSION_APPLICATION_SCOPE}.
 *
 * @see javax.portlet.PortletSession
 * @author <a href="mailto:alex.rudnev@dc.gov">Alex Rudnev</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: PortletSession.java,v 1.4 2004/06/18 16:45:57 vgritsenko Exp $
 */
public final class PortletSession implements Session {

    private static final String APP_SCOPE = PortletEnvironment.SESSION_APPLICATION_SCOPE;
    private static final String PORTLET_SCOPE = PortletEnvironment.SESSION_PORTLET_SCOPE;

    javax.portlet.PortletSession session;

    /**
     * Default session scope. One of
     * {@link javax.portlet.PortletSession.APPLICATION_SCOPE},
     * {@link javax.portlet.PortletSession.PORTLET_SCOPE}.
     */
    private int scope;

    /**
     * Construct a new session from an PortletSession
     */
    public PortletSession(javax.portlet.PortletSession session, int scope) {
        this.scope = scope; 
        this.session = session;
    }

    /**
     * Returns the time when this session was created, measured
     * in milliseconds since midnight January 1, 1970 GMT.
     *
     * @return                            a <code>long</code> specifying
     *                                    when this session was created,
     *                                    expressed in
     *                                    milliseconds since 1/1/1970 GMT
     *
     * @exception IllegalStateException   if this method is called on an
     *                                    invalidated session
     */
    public long getCreationTime() {
        return this.session.getCreationTime();
    }

    /**
     * Returns a string containing the unique identifier assigned
     * to this session. The identifier is assigned
     * by the context container and is implementation dependent.
     *
     * @return                            a string specifying the identifier
     *                                    assigned to this session
     *
     * @exception IllegalStateException   if this method is called on an
     *                                    invalidated session
     */
    public String getId() {
        return this.session.getId();
    }

    /**
     * Returns the last time the client sent a request associated with
     * this session, as the number of milliseconds since midnight
     * January 1, 1970 GMT.
     *
     * <p>Actions that your application takes, such as getting or setting
     * a value associated with the session, do not affect the access
     * time.
     *
     * @return                            a <code>long</code>
     *                                    representing the last time
     *                                    the client sent a request associated
     *                                    with this session, expressed in
     *                                    milliseconds since 1/1/1970 GMT
     *
     * @exception IllegalStateException   if this method is called on an
     *                                    invalidated session
     */
    public long getLastAccessedTime() {
        return this.session.getLastAccessedTime();
    }

    /**
     * Specifies the time, in seconds, between client requests before the
     * contextcontainer will invalidate this session.  A negative time
     * indicates the session should never timeout.
     *
     * @param interval                An integer specifying the number
     *                                of seconds
     */
    public void setMaxInactiveInterval(int interval) {
        this.session.setMaxInactiveInterval(interval);
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
     * @see                   #setMaxInactiveInterval(int)
     */
    public int getMaxInactiveInterval() {
        return this.session.getMaxInactiveInterval();
    }

    /**
     * Returns the object bound with the specified name in this session, or
     * <code>null</code> if no object is bound under the name.
     *
     * @param name                a string specifying the name of the object
     *
     * @return                    the object with the specified name
     *
     * @exception IllegalStateException   if this method is called on an
     *                                    invalidated session
     */
    public Object getAttribute(String name) {
        if (name.startsWith(APP_SCOPE)) {
            return this.session.getAttribute(name.substring(APP_SCOPE.length()),
                                             javax.portlet.PortletSession.APPLICATION_SCOPE);
        } else if (name.startsWith(PORTLET_SCOPE)) {
            return this.session.getAttribute(name.substring(PORTLET_SCOPE.length()),
                                             javax.portlet.PortletSession.PORTLET_SCOPE);
        } else {
            return this.session.getAttribute(name, this.scope);
        }
    }

    /**
     * Returns an <code>Enumeration</code> of <code>String</code> objects
     * containing the names of all the objects bound to this session.
     *
     * <p>Objects' names in portlet session scope will be prefixed with
     * {@link #PORTLET_SCOPE}, and names in application scope will be prefixed
     * with {@link #APP_SCOPE}.
     *
     * @return                        an <code>Enumeration</code> of
     *                                <code>String</code> objects specifying the
     *                                names of all the objects bound to
     *                                this session
     *
     * @exception IllegalStateException   if this method is called on an
     *                                    invalidated session
     */
    public Enumeration getAttributeNames() {
        final Enumeration names1 = this.session.getAttributeNames(javax.portlet.PortletSession.PORTLET_SCOPE);
        final Enumeration names2 = this.session.getAttributeNames(javax.portlet.PortletSession.APPLICATION_SCOPE);

        return new Enumeration() {
            public boolean hasMoreElements() {
                return names1.hasMoreElements() || names2.hasMoreElements();
            }

            public Object nextElement() throws NoSuchElementException {
                if (names1.hasMoreElements()) {
                    return PORTLET_SCOPE + names1.nextElement();
                } else if (names2.hasMoreElements()) {
                    return APP_SCOPE + names2.nextElement();
                }

                throw new NoSuchElementException();
            }
        };
    }

    /**
     * Binds an object to this session, using the name specified.
     * If an object of the same name is already bound to the session,
     * the object is replaced.
     *
     *
     * @param name                        the name to which the object is bound;
     *                                    cannot be null
     *
     * @param value                       the object to be bound; cannot be null
     *
     * @exception IllegalStateException   if this method is called on an
     *                                    invalidated session
     */
    public void setAttribute(String name, Object value) {
        if (name.startsWith(APP_SCOPE)) {
            this.session.setAttribute(name.substring(APP_SCOPE.length()),
                                      value,
                                      javax.portlet.PortletSession.APPLICATION_SCOPE);
        } else if (name.startsWith(PORTLET_SCOPE)) {
            this.session.setAttribute(name.substring(PORTLET_SCOPE.length()),
                                      value,
                                      javax.portlet.PortletSession.PORTLET_SCOPE);
        } else {
            this.session.setAttribute(name, value, this.scope);
        }
    }

    /**
     * Removes the object bound with the specified name from
     * this session. If the session does not have an object
     * bound with the specified name, this method does nothing.
     *
     *
     * @param name                        the name of the object to
     *                                    remove from this session
     *
     * @exception IllegalStateException   if this method is called on an
     *                                    invalidated session
     */
    public void removeAttribute(String name) {
        if (name.startsWith(APP_SCOPE)) {
            this.session.removeAttribute(name.substring(APP_SCOPE.length()),
                                         javax.portlet.PortletSession.APPLICATION_SCOPE);
        } else if (name.startsWith(PORTLET_SCOPE)) {
            this.session.removeAttribute(name.substring(PORTLET_SCOPE.length()),
                                         javax.portlet.PortletSession.PORTLET_SCOPE);
        } else {
            this.session.removeAttribute(name, this.scope);
        }
    }

    /**
     * Invalidates this session to it.
     *
     * @exception IllegalStateException   if this method is called on an
     *                                    already invalidated session
     */
    public void invalidate() {
        this.session.invalidate();
    }

    /**
     * Returns <code>true</code> if the client does not yet know about the
     * session or if the client chooses not to join the session.  For
     * example, if the server used only cookie-based sessions, and
     * the client had disabled the use of cookies, then a session would
     * be new on each request.
     *
     * @return                            <code>true</code> if the
     *                                    server has created a session,
     *                                    but the client has not yet joined
     *
     * @exception IllegalStateException   if this method is called on an
     *                                    already invalidated session
     */
    public boolean isNew() {
        return this.session.isNew();
    }
}
