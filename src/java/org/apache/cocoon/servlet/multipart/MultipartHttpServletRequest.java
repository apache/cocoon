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
package org.apache.cocoon.servlet.multipart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Servlet request wrapper for multipart parser.
 *
 * @author <a href="mailto:j.tervoorde@home.nl">Jeroen ter Voorde</a>
 * @author Stefano Mazzocchi
 * @version CVS $Id: MultipartHttpServletRequest.java,v 1.7 2004/03/08 14:03:30 cziegeler Exp $
 */
public class MultipartHttpServletRequest implements HttpServletRequest {

    /** The wrapped request */
    private HttpServletRequest request = null;

    /** The submitted parts */
    private Hashtable values = null;

    /**
     * Create this wrapper around the given request and including the given
     * parts.
     */
    public MultipartHttpServletRequest(HttpServletRequest request, Hashtable values) {
        this.request = request;
        this.values = values;
    }

    /**
     * Cleanup eventually uploaded parts that were saved on disk
     */
    public void cleanup() throws IOException {
        Enumeration e = getParameterNames();
        while (e.hasMoreElements()) {
            Object o = get( (String)e.nextElement() );
            if (o instanceof Part) {
                Part part = (Part)o;
                if (part.disposeWithRequest()) {
                    part.dispose();
                }
            }
        }
    }

    /**
     * Method get
     *
     * @param name
     *
     */
    public Object get(String name) {
        Object result = null;

        if (values != null) {
            result = values.get(name);

            if (result instanceof Vector) {
                if (((Vector) result).size() == 1) {
                    return ((Vector) result).elementAt(0);
                } else {
                    return result;
                }
            }
        } else {
            String[] array = request.getParameterValues(name);
            Vector vec = new Vector();

            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    vec.addElement(array[i]);
                }

                if (vec.size() == 1) {
                    result = vec.elementAt(0);
                } else {
                    result = vec;
                }
            }
        }

        return result;
    }

    /**
     * Method getParameterNames
     *
     */
    public Enumeration getParameterNames() {
        if (values != null) {
            return values.keys();
        } else {
            return request.getParameterNames();
        }
    }

    /**
     * Method getParameter
     *
     * @param name
     *
     */
    public String getParameter(String name) {
        Object value = get(name);
        String result = null;

        if (value != null) {
            if (value instanceof Vector) {
                value = ((Vector) value).elementAt(0);
            }

            result = value.toString();
        }

        return result;
    }

    /**
     * Method getParameterValues
     *
     * @param name
     *
     */
    public String[] getParameterValues(String name) {
        if (values != null) {
            Object value = get(name);

            if (value != null) {
                if (value instanceof Vector) {
                    String[] results = new String[((Vector)value).size()];
                    for (int i=0;i<((Vector)value).size();i++) {
                        results[i] = ((Vector)value).elementAt(i).toString();
                    }
                    return results;

                } else {
                    return new String[]{value.toString()};
                }
            }

            return null;
        } else {
            return request.getParameterValues(name);
        }
    }

    /**
     * Method getAttribute
     *
     * @param name
     *
     */
    public Object getAttribute(String name) {
        return request.getAttribute(name);
    }

    /**
     * Method getAttributeNames
     *
     */
    public Enumeration getAttributeNames() {
        return request.getAttributeNames();
    }

    /**
     * Method getCharacterEncoding
     *
     */
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    /**
     * Method getContentLength
     *
     */
    public int getContentLength() {
        return request.getContentLength();
    }

    /**
     * Method getContentType
     *
     */
    public String getContentType() {
        return request.getContentType();
    }

    /**
     * Method getInputStream
     *
     *
     * @throws IOException
     */
    public ServletInputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    /**
     * Method getProtocol
     *
     */
    public String getProtocol() {
        return request.getProtocol();
    }

    /**
     * Method getScheme
     *
     */
    public String getScheme() {
        return request.getScheme();
    }

    /**
     * Method getServerName
     *
     */
    public String getServerName() {
        return request.getServerName();
    }

    /**
     * Method getServerPort
     *
     */
    public int getServerPort() {
        return request.getServerPort();
    }

    /**
     * Method getReader
     *
     *
     * @throws IOException
     */
    public BufferedReader getReader() throws IOException {
        return request.getReader();
    }

    /**
     * Method getRemoteAddr
     *
     */
    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    /**
     * Method getRemoteHost
     *
     */
    public String getRemoteHost() {
        return request.getRemoteHost();
    }

    /**
     * Method setAttribute
     *
     * @param name
     * @param o
     */
    public void setAttribute(String name, Object o) {
        request.setAttribute(name, o);
    }

    /**
     * Method removeAttribute
     *
     * @param name
     */
    public void removeAttribute(String name) {
        request.removeAttribute(name);
    }

    /**
     * Method getLocale
     *
     */
    public Locale getLocale() {
        return request.getLocale();
    }

    /**
     * Method getLocales
     *
     */
    public Enumeration getLocales() {
        return request.getLocales();
    }

    /**
     * Method isSecure
     *
     */
    public boolean isSecure() {
        return request.isSecure();
    }

    /**
     * Method getRequestDispatcher
     *
     * @param path
     *
     */
    public RequestDispatcher getRequestDispatcher(String path) {
        return request.getRequestDispatcher(path);
    }

    /**
     * Method getRealPath
     *
     * @param path
     *
     */
    public String getRealPath(String path) {
        return request.getRealPath(path);
    }

    /**
     * Method getAuthType
     *
     */
    public String getAuthType() {
        return request.getAuthType();
    }

    /**
     * Method getCookies
     *
     */
    public Cookie[] getCookies() {
        return request.getCookies();
    }

    /**
     * Method getDateHeader
     *
     * @param name
     *
     */
    public long getDateHeader(String name) {
        return request.getDateHeader(name);
    }

    /**
     * Method getHeader
     *
     * @param name
     *
     */
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    /**
     * Method getHeaders
     *
     * @param name
     *
     */
    public Enumeration getHeaders(String name) {
        return request.getHeaders(name);
    }

    /**
     * Method getHeaderNames
     *
     */
    public Enumeration getHeaderNames() {
        return request.getHeaderNames();
    }

    /**
     * Method getIntHeader
     *
     * @param name
     *
     */
    public int getIntHeader(String name) {
        return request.getIntHeader(name);
    }

    /**
     * Method getMethod
     *
     */
    public String getMethod() {
        return request.getMethod();
    }

    /**
     * Method getPathInfo
     *
     */
    public String getPathInfo() {
        return request.getPathInfo();
    }

    /**
     * Method getPathTranslated
     *
     */
    public String getPathTranslated() {
        return request.getPathTranslated();
    }

    /**
     * Method getContextPath
     *
     */
    public String getContextPath() {
        return request.getContextPath();
    }

    /**
     * Method getQueryString
     *
     */
    public String getQueryString() {
        return request.getQueryString();
    }

    /**
     * Method getRemoteUser
     *
     */
    public String getRemoteUser() {
        return request.getRemoteUser();
    }

    /**
     * Method isUserInRole
     *
     * @param role
     *
     */
    public boolean isUserInRole(String role) {
        return request.isUserInRole(role);
    }

    /**
     * Method getUserPrincipal
     *
     */
    public Principal getUserPrincipal() {
        return request.getUserPrincipal();
    }

    /**
     * Method getRequestedSessionId
     *
     */
    public String getRequestedSessionId() {
        return request.getRequestedSessionId();
    }

    /**
     * Method getRequestURI
     *
     */
    public String getRequestURI() {
        return request.getRequestURI();
    }

    /**
     * Method getServletPath
     *
     */
    public String getServletPath() {
        return request.getServletPath();
    }

    /**
     * Method getSession
     *
     * @param create
     *
     */
    public HttpSession getSession(boolean create) {
        return request.getSession(create);
    }

    /**
     * Method getSession
     *
     */
    public HttpSession getSession() {
        return request.getSession();
    }

    /**
     * Method isRequestedSessionIdValid
     *
     */
    public boolean isRequestedSessionIdValid() {
        return request.isRequestedSessionIdValid();
    }

    /**
     * Method isRequestedSessionIdFromCookie
     *
     */
    public boolean isRequestedSessionIdFromCookie() {
        return request.isRequestedSessionIdFromCookie();
    }

    /**
     * Method isRequestedSessionIdFromURL
     *
     */
    public boolean isRequestedSessionIdFromURL() {
        return request.isRequestedSessionIdFromURL();
    }

    /**
     * Method isRequestedSessionIdFromUrl
     * @deprecated use {@link #isRequestedSessionIdFromURL()} instead
     */
    public boolean isRequestedSessionIdFromUrl() {
        return request.isRequestedSessionIdFromURL();
    }

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	public Map getParameterMap() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0)
		throws UnsupportedEncodingException {
		// TODO Auto-generated method stub

	}

}
