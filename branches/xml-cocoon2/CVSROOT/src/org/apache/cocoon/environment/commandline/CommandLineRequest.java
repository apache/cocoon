/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.environment.commandline;

import org.apache.cocoon.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Iterator;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;

/**
 * Creates a specific servlet request simulation from command line usage.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.12 $ $Date: 2001-04-18 12:05:56 $
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

    private String contextPath;
    private String servletPath;
    private String pathInfo;
    private Map attributes;
    private Map parameters;
    private Map headers;
    private String characterEncoding = null;

    public CommandLineRequest(String contextPath, String servletPath, String pathInfo) {
        this(contextPath, servletPath, pathInfo, null, null, null);
    }

    public CommandLineRequest(String contextPath, String servletPath, String pathInfo, Map attributes) {
        this(contextPath, servletPath, pathInfo, attributes, null, null);
    }

    public CommandLineRequest(String contextPath, String servletPath, String pathInfo, Map attributes, Map parameters) {
        this(contextPath, servletPath, pathInfo, attributes, parameters, null);
    }

    public CommandLineRequest(String contextPath, String servletPath, String pathInfo, Map attributes, Map parameters, Map headers) {
        this.contextPath = contextPath;
        this.servletPath = servletPath;
        this.pathInfo = pathInfo;
        this.attributes = attributes;
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
    public String getQueryString() { return null; } // use parameters instead
    public String getPathTranslated() { return null; } // FIXME (SM) this is legal but should we do something more?

    public Object getAttribute(String name) {
        return (attributes != null) ? attributes.get(name) : null;
    }
    public Enumeration getAttributeNames() {
        return (attributes != null) ? new IteratorWrapper(attributes.keySet().iterator()) : null;
    }
    public void setAttribute(String name, Object value) {
        if (attributes != null) attributes.put(name, value);
    }
    public void removeAttribute(String name) {
        if (attributes != null) attributes.remove(name);
    }

    public String getParameter(String name) {
        return (parameters != null) ? (String) parameters.get(name) : null;
    }
    public Enumeration getParameterNames() {
        return (parameters != null) ? new IteratorWrapper(parameters.keySet().iterator()) : null;
    }
    public String[] getParameterValues(String name) {
        Object [] obj = parameters.values().toArray();
        String [] str = new String [obj.length];
        for (int i = 0; i < obj.length; i++)
            str[i] = (String)obj[i];
        return str;
    }

    public String getHeader(String name) {
        return (headers != null) ? (String) headers.get(name) : null;
    }
    public int getIntHeader(String name) {
        String header = (headers != null) ? (String) headers.get(name) : null;
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
        if (headers != null)
            return new IteratorWrapper(headers.keySet().iterator());
        else
            return new EmptyEnumeration();
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
    public Session getSession() { return null; }
    public Session getSession(boolean create) { return null; }
    public String getRequestedSessionId() { return null; }
    public boolean isRequestedSessionIdValid() { return false; }
    public boolean isRequestedSessionIdFromCookie() { return false; }
    public boolean isRequestedSessionIdFromURL() { return false; }

    public Locale getLocale() { return Locale.getDefault(); }
    public Enumeration getLocales() {
        throw new RuntimeException (this.getClass().getName() + ".getLocales() method not yet implemented!");
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
