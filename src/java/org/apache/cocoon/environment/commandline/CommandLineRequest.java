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
package org.apache.cocoon.environment.commandline;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;

/**
 * Creates a specific servlet request simulation from command line usage.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CommandLineRequest.java,v 1.8 2004/03/01 03:50:58 antonio Exp $
 */

/*
 * NOTE: method with a non-compliant implementation are marked with FIXME
 * and should be fixed in the future if required
 */
public class CommandLineRequest implements Request {

    private class IteratorWrapper implements Enumeration {
        private Iterator iterator;
        public IteratorWrapper(Iterator i) {
            this.iterator = i;
        }
        public boolean hasMoreElements() {
            return iterator.hasNext();
        }
        public Object nextElement() {
            return iterator.next();
        }
    }

    private class EmptyEnumeration implements Enumeration {
        public boolean hasMoreElements() {
            return false;
        }
        public Object nextElement() {
            return null;
        }
    }

    private Environment env;
    private String contextPath;
    private String servletPath;
    private String pathInfo;
    private Map attributes;
    private Map parameters;
    private Map headers;
    private String characterEncoding = null;

    public CommandLineRequest(Environment env,
                              String contextPath,
                              String servletPath,
                              String pathInfo) {
        this(env, contextPath, servletPath, pathInfo, null, null, null);
    }

    public CommandLineRequest(Environment env,
                              String contextPath,
                              String servletPath,
                              String pathInfo,
                              Map attributes) {
        this(env, contextPath, servletPath, pathInfo, attributes, null, null);
    }

    public CommandLineRequest(Environment env,
                              String contextPath,
                              String servletPath,
                              String pathInfo,
                              Map attributes,
                              Map parameters) {
        this(env, contextPath, servletPath, pathInfo, attributes, parameters, null);
    }

    public CommandLineRequest(Environment env,
                              String contextPath,
                              String servletPath,
                              String pathInfo,
                              Map attributes,
                              Map parameters,
                              Map headers) {
        this.env = env;
        this.contextPath = contextPath;
        this.servletPath = servletPath;
        this.pathInfo = pathInfo;
        this.attributes = (attributes == null ? new HashMap() : attributes);
        this.parameters = parameters;
        this.headers = headers;
    }

    public Object get(String name) { return getAttribute(name); }

    public String getContextPath() { return contextPath; }
    public String getServletPath() { return servletPath; }
    public String getPathInfo() { return pathInfo; }
    public String getRequestURI() {
        StringBuffer buffer = new StringBuffer();
        if (servletPath != null) buffer.append(servletPath);
        if (contextPath != null) buffer.append(contextPath);
        if (pathInfo != null) buffer.append(pathInfo);
        return buffer.toString();
    }
    // FIXME
    public String getSitemapURI() {
        return this.env.getURI();
    }
    
    public String getQueryString() { return null; } // use parameters instead
    public String getPathTranslated() { return null; } // FIXME (SM) this is legal but should we do something more?

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }
    public Enumeration getAttributeNames() {
        return new IteratorWrapper(this.attributes.keySet().iterator());
    }
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    public String getParameter(String name) {
        if (this.parameters == null) {
            return null;
        }
        
        final Object value = this.parameters.get(name);
        if (value instanceof String) {
            return (String)value;
        } else if (value == null) {
            return null;
        } else {
            final String[] values = (String[]) value;
            if (values.length == 0) {
                return null;
            }
            return values[0];
        }
    }

    public Enumeration getParameterNames() {
        return (this.parameters != null) ? new IteratorWrapper(this.parameters.keySet().iterator()) : null;
    }

    public String[] getParameterValues(String name) {
        final Object value = this.parameters.get(name);
        if (value instanceof String) {
            return new String[] { (String)value };
        } else {
            return (String[]) value;
        }
    }

    public String getHeader(String name) {
        return (headers != null) ? (String) headers.get(name.toLowerCase()) : null;
    }

    public int getIntHeader(String name) {
        String header = (headers != null) ? (String) headers.get(name.toLowerCase()) : null;
        return (header != null) ? Integer.parseInt(header) : -1;
    }

    public long getDateHeader(String name) {
        //FIXME
        //throw new RuntimeException (this.getClass().getName() + ".getDateHeader(String name) method not yet implemented!");
        return 0;
    }

    public Enumeration getHeaders(String name) {
        //throw new RuntimeException (this.getClass().getName() + ".getHeaders(String name) method not yet implemented!");
        return new EmptyEnumeration();
    } // FIXME

    public Enumeration getHeaderNames() {
        if (headers != null) {
            return new IteratorWrapper(headers.keySet().iterator());
        } else {
            return new EmptyEnumeration();
        }
    }

    public String getCharacterEncoding() { return characterEncoding; }
    public int getContentLength() { return -1; }

    public String getContentType() { return null; }
    public String getProtocol()  { return "cli"; }
    public String getScheme() { return "cli"; }
    public String getServerName() { return Constants.COMPLETE_NAME; }
    public int getServerPort() { return -1; }
    public String getRemoteAddr() { return "127.0.0.1"; }
    public String getRemoteHost() { return "localhost"; }
    public String getMethod() { return "get"; }
    public String getRemoteUser() { return System.getProperty("user.name"); }

    public Cookie[] getCookies() { return null; }
    public Map getCookieMap() {
        return Collections.unmodifiableMap(new HashMap());
    }

    /**
     * Returns the current session associated with this request,
     * or if the request does not have a session, creates one.
     *
     * @return                the <code>Session</code> associated
     *                        with this request
     *
     * @see        #getSession(boolean)
     */
    public Session getSession() {
        return this.getSession(true);
    }

    /**
     * Returns the current <code>Session</code>
     * associated with this request or, if if there is no
     * current session and <code>create</code> is true, returns
     * a new session.
     *
     * <p>If <code>create</code> is <code>false</code>
     * and the request has no valid <code>Session</code>,
     * this method returns <code>null</code>.
     *
     * <p>To make sure the session is properly maintained,
     * you must call this method before
     * the response is committed.
     *
     * @param create  <code>true</code> to create a new session for this request
     *                if necessary;
     *                <code>false</code> to return <code>null</code> if there's
     *                no current session
     *
     * @return  the <code>Session</code> associated with this request or
     *          <code>null</code> if <code>create</code> is <code>false</code>
     *          and the request has no valid session
     *
     * @see  #getSession()
     */
    public Session getSession(boolean create) {
        return CommandLineSession.getSession(create);
    }

    /**
     * Returns the session ID specified by the client. This may
     * not be the same as the ID of the actual session in use.
     * For example, if the request specified an old (expired)
     * session ID and the server has started a new session, this
     * method gets a new session with a new ID. If the request
     * did not specify a session ID, this method returns
     * <code>null</code>.
     *
     *
     * @return                a <code>String</code> specifying the session
     *                        ID, or <code>null</code> if the request did
     *                        not specify a session ID
     *
     * @see                #isRequestedSessionIdValid()
     */
    public String getRequestedSessionId() {
        return (CommandLineSession.getSession(false) != null) ?
                CommandLineSession.getSession(false).getId() : null;
    }

    /**
     * Checks whether the requested session ID is still valid.
     *
     * @return                        <code>true</code> if this
     *                                request has an id for a valid session
     *                                in the current session context;
     *                                <code>false</code> otherwise
     *
     * @see                        #getRequestedSessionId()
     * @see                        #getSession()
     */
    public boolean isRequestedSessionIdValid() {
        return (CommandLineSession.getSession(false) != null);
    }

    /**
     * Checks whether the requested session ID came in as a cookie.
     *
     * @return                        <code>true</code> if the session ID
     *                                came in as a
     *                                cookie; otherwise, <code>false</code>
     *
     *
     * @see                        #getSession()
     */
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    /**
     * Checks whether the requested session ID came in as part of the
     * request URL.
     *
     * @return                        <code>true</code> if the session ID
     *                                came in as part of a URL; otherwise,
     *                                <code>false</code>
     *
     *
     * @see                        #getSession()
     */
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    public Locale getLocale() { return Locale.getDefault(); }
    public Enumeration getLocales() {
        throw new RuntimeException (getClass().getName() + ".getLocales() method not yet implemented!");
    } // FIXME

    public String getAuthType() { return null; }
    public boolean isSecure() { return false; }
    public boolean isUserInRole(String role) { return false; }
    public java.security.Principal getUserPrincipal() { return null; }

    public java.util.Map getParameterMap() { return parameters; }
    public void setCharacterEncoding(java.lang.String env)
                          throws java.io.UnsupportedEncodingException { characterEncoding = env; }
    public StringBuffer getRequestURL() { return null; }
}
