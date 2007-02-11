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
package org.apache.cocoon.environment.wrapper;

import java.security.Principal;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;


/**
 * This is a wrapper class for the <code>Request</code> object.
 * It has the same properties except that the url and the parameters
 * are different.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: RequestWrapper.java,v 1.5 2004/03/01 03:50:59 antonio Exp $
 */
public final class RequestWrapper implements Request {

    /** The real {@link Request} object */
    private final Request req;

    /** The query string */
    private String queryString;

    /** The request parameters */
    private final RequestParameters parameters ;

    /** The environment */
    private final Environment environment;

    /** raw mode? **/
    private final boolean rawMode;

    /**
     * Constructor
     */
    public RequestWrapper(Request request,
                          String  requestURI,
                          String  queryString,
                          Environment env) {
        this(request, requestURI, queryString, env, false);
    }

    /**
     * Constructor
     */
    public RequestWrapper(Request request,
                          String  requestURI,
                          String  queryString,
                          Environment env,
                          boolean rawMode) {
        this.environment = env;
        this.req = request;
        this.queryString = queryString;
        this.parameters = new RequestParameters(queryString);
        this.rawMode = rawMode;
        if (this.req.getQueryString() != null && this.rawMode == false) {
            if (this.queryString == null)
                this.queryString = this.req.getQueryString();
            else
                this.queryString += '&' + this.req.getQueryString();
        }
    }

    public Object get(String name) {
        return this.req.get(name);
    }

    public Object getAttribute(String name) {
        return this.req.getAttribute(name);
    }

    public Enumeration getAttributeNames() {
        return this.req.getAttributeNames();
    }

    public String getCharacterEncoding() {
        return this.req.getCharacterEncoding();
    }

    public void setCharacterEncoding(String enc)
    throws java.io.UnsupportedEncodingException {
        this.req.setCharacterEncoding(enc);
    }

    public int getContentLength() {
        return this.req.getContentLength();
    }

    public String getContentType() {
        return this.req.getContentType();
    }

    public String getParameter(String name) {
        String value = this.parameters.getParameter(name);
        if (value == null && this.rawMode == false)
            return this.req.getParameter(name);
        else
            return value;
    }

    public Enumeration getParameterNames() {
        if ( this.rawMode == false ) {
            // put all parameter names into a set
            Set parameterNames = new HashSet();
            Enumeration names = this.parameters.getParameterNames();
            while (names.hasMoreElements()) {
                parameterNames.add(names.nextElement());
            }
            names = this.req.getParameterNames();
            while (names.hasMoreElements()) {
                parameterNames.add(names.nextElement());
            }
            return new EnumerationFromIterator(parameterNames.iterator());
        } else {
            return this.parameters.getParameterNames();
        }
    }

    final class EnumerationFromIterator implements Enumeration {
        private Iterator iter;
        EnumerationFromIterator(Iterator iter) {
            this.iter = iter;
        }

        public boolean hasMoreElements() {
            return iter.hasNext();
        }
        public Object nextElement() { return iter.next(); }
    }

    public String[] getParameterValues(String name) {
        if ( this.rawMode == false) {
            String[] values = this.parameters.getParameterValues(name);
            String[] inherited = this.req.getParameterValues(name);
            if (inherited == null) return values;
            if (values == null) return inherited;
            String[] allValues = new String[values.length + inherited.length];
            System.arraycopy(values, 0, allValues, 0, values.length);
            System.arraycopy(inherited, 0, allValues, values.length, inherited.length);
            return allValues;
        } else {
            return this.parameters.getParameterValues(name);
        }
    }

    public String getProtocol() {
        return this.req.getProtocol();
    }

    public String getScheme() {
        return this.req.getScheme();
    }

    public String getServerName() {
        return this.req.getServerName();
    }

    public int getServerPort() {
        return this.req.getServerPort();
    }

    public String getRemoteAddr() {
        return this.req.getRemoteAddr();
    }

    public String getRemoteHost() {
        return this.req.getRemoteHost();
    }

    public void setAttribute(String name, Object o) {
        this.req.setAttribute(name, o);
    }

    /**
     * Remove one attriube
     */
    public void removeAttribute(String name) {
        this.req.removeAttribute(name);
    }

    public Locale getLocale() {
        return this.req.getLocale();
    }

    public Enumeration getLocales() {
        return this.req.getLocales();
    }

    public boolean isSecure() {
        return this.req.isSecure();
    }

    public Cookie[] getCookies() {
        return this.req.getCookies();
    }

    public Map getCookieMap() {
        return this.req.getCookieMap();
    }

    public long getDateHeader(String name) {
        return this.req.getDateHeader(name);
    }

    public String getHeader(String name) {
        return this.req.getHeader(name);
    }

    public Enumeration getHeaders(String name) {
        return this.req.getHeaders(name);
    }

    public Enumeration getHeaderNames() {
        return this.req.getHeaderNames();
    }

    public String getMethod() {
        return this.req.getMethod();
    }

    public String getPathInfo() {
        return this.req.getPathInfo();
    }

    public String getPathTranslated() {
        return this.req.getPathTranslated();
    }

    public String getContextPath() {
        return this.req.getContextPath();
    }

    public String getQueryString() {
        return this.queryString;
    }

    public String getRemoteUser() {
        return this.req.getRemoteUser();
    }

    public String getRequestedSessionId() {
        return this.req.getRequestedSessionId();
    }

    public String getRequestURI() {
        return this.req.getRequestURI();
    }

    public String getSitemapURI() {
        return this.environment.getURI();
    }

    public String getServletPath() {
        return this.req.getServletPath();
    }

    public Session getSession(boolean create) {
        return this.req.getSession(create);
    }

    public Session getSession() {
        return this.req.getSession();
    }

    public boolean isRequestedSessionIdValid() {
        return this.req.isRequestedSessionIdValid();
    }

    public boolean isRequestedSessionIdFromCookie()  {
        return this.req.isRequestedSessionIdFromCookie();
    }

    public boolean isRequestedSessionIdFromURL() {
        return this.req.isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdFromUrl() {
        return this.req.isRequestedSessionIdFromURL();
    }

    public Principal getUserPrincipal() {
        return this.req.getUserPrincipal();
    }

    public boolean isUserInRole(String role) {
        return this.req.isUserInRole(role);
    }

    public String getAuthType() {
        return this.req.getAuthType();
    }   
}
