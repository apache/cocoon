/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.servletservice.util;

import org.apache.commons.collections.iterators.IteratorEnumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Create a HttpServletRequest from an URL, that is used while calling e.g. a
 * block.
 *
 * @version $Id: BlockCallHttpServletRequest.java 577519 2007-09-20 03:05:26Z
 *          vgritsenko $
 */
public class BlockCallHttpServletRequest implements HttpServletRequest {

    /**
     * Protocol of block call requests.
     */
    private static final String PROTOCOL = "HTTP/1.1";

    /**
     * Date header format definied by RFC 822, see
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

    /**
     * The <code>parent</code> holds reference to the request object that
     * makes a servlet call.
     */
    private HttpServletRequest parent;

    /**
     * Block call request URI
     */
    private final URI uri;

    /**
     * Request method. If not set via {@link #setMethod(String)}, defaults to
     * <code>GET</code>.
     */
    private String method;

    /**
     * Request headers map
     */
    private final Map headers;

    /**
     * Request character encoding.
     */
    private String encoding;

    /**
     * Length of the request body.
     */
    private int contentLength;

    /**
     * Request body.
     */
    private ServletInputStream content;

    /**
     * Request attributes map.
     */
    private final Map attributes;

    /**
     * @param uri
     *            points to the called servlet
     * @param parentRequest
     *            reference to the request object that makes a servlet call
     */
    public BlockCallHttpServletRequest(URI uri, HttpServletRequest parentRequest) {
        this.parent = parentRequest;
        this.uri = uri;
        this.headers = createRequestHeaderMap(parentRequest);
        this.method = "GET";
        this.contentLength = -1;
        this.content = NullServletInputStream.INSTANCE;
        this.attributes = createRequestAttributesMap(parentRequest);
    }

    /**
     * Create a new {@link Map} that contains all request attributes. A sub
     * request can't pass parameters to the parent request, however, it can
     * modify parameter values.
     *
     * TODO Is there any way to prevent sub request from altering parent
     * attributes?
     */
    private Map createRequestAttributesMap(HttpServletRequest req) {
        Map attributes = new HashMap();
        Enumeration parentAttributes = req.getAttributeNames();
        while (parentAttributes.hasMoreElements()) {
            String attr = (String) parentAttributes.nextElement();
            attributes.put(attr, req.getAttribute(attr));
        }
        return attributes;
    }

    /**
     * Create a new {@link Map} that contains all headers.
     */
    private Map createRequestHeaderMap(HttpServletRequest req) {
        Map headers = new HashMap();
        Enumeration parentHeaders = req.getHeaderNames();
        while (parentHeaders.hasMoreElements()) {
            String header = (String) parentHeaders.nextElement();
            headers.put(header, req.getHeader(header));
        }
        return headers;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getProtocol()
     */
    public String getProtocol() {
        return PROTOCOL;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    //
    // Request URI parts
    //

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getScheme()
     */
    public String getScheme() {
        return this.uri.getScheme();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getServerName()
     */
    public String getServerName() {
        // TODO implement this
        return "";
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getServerPort()
     */
    public int getServerPort() {
        // TODO implement this
        return 80;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getContextPath()
     */
    public String getContextPath() {
        return parent.getContextPath();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getServletPath()
     */
    public String getServletPath() {
        // TODO Is this right?
        return "";
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getPathInfo()
     */
    public String getPathInfo() {
        return this.uri.getPath();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
     */
    public String getPathTranslated() {
        // TODO This is legal but more info might be possible
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getQueryString()
     */
    public String getQueryString() {
        return this.uri.getQuery();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    public String getRequestURI() {
        // TODO Is this right?
        return getContextPath() + getServletPath() + getPathInfo();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    public StringBuffer getRequestURL() {
        return new StringBuffer(getScheme()).append(':').append(getRequestURI());
    }

    //
    // Request headers
    //

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
     */
    public String getHeader(String name) {
        return (String) this.headers.get(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
     */
    public Enumeration getHeaders(String name) {
        return new IteratorEnumeration(headers.values().iterator());
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
     */
    public long getDateHeader(String name) {
        String header = getHeader(name);
        if (header == null) {
            return -1;
        }

        try {
            return dateFormat.parse(header).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDateHeader(String name, long date) {
        this.setHeader(name, dateFormat.format(new Date(date)));
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
     */
    public int getIntHeader(String name) {
        String header = this.getHeader(name);
        if (header == null) {
            return -1;
        }

        return Integer.parseInt(header);
    }

    public void setIntHeader(String name, int value) {
        this.setHeader(name, String.valueOf(value));
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
     */
    public Enumeration getHeaderNames() {
        return new IteratorEnumeration(headers.keySet().iterator());
    }

    //
    // Request parameters
    //

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */
    public String getParameter(String name) {
        return this.parent.getParameter(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String name) {
        return this.parent.getParameterValues(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    public Enumeration getParameterNames() {
        return this.parent.getParameterNames();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {
        return this.parent.getParameterMap();
    }

    //
    // Request body
    //

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getCharacterEncoding()
     */
    public String getCharacterEncoding() {
        return this.encoding;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
     */
    public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
        this.encoding = encoding;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getContentLength()
     */
    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getContentType()
     *
     * TODO Doesn't handle input streams yet
     */
    public String getContentType() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getInputStream()
     */
    public ServletInputStream getInputStream() throws IOException {
        return content;
    }

    public void setInputStream(final InputStream inputStream) {
        try {
            contentLength = inputStream.available();
        } catch (IOException e) {
            contentLength = -1;
        }

        content = new ServletInputStream() {
            public int read() throws IOException {
                return inputStream.read();
            }
        };
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getReader()
     */
    public BufferedReader getReader() throws IOException {
        Reader reader;
        String encoding = getCharacterEncoding();
        if (encoding == null) {
            reader = new InputStreamReader(getInputStream());
        } else {
            reader = new InputStreamReader(getInputStream(), encoding);
        }

        return new BufferedReader(reader);
    }

    //
    // Request attributes
    //

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return new IteratorEnumeration(this.attributes.keySet().iterator());
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#setAttribute(java.lang.String,
     *      java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        if (value != null) {
            this.attributes.put(name, value);
        } else {
            removeAttribute(name);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getAuthType()
     */
    public String getAuthType() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getCookies()
     */
    public Cookie[] getCookies() {
        return this.parent.getCookies();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getLocale()
     */
    public Locale getLocale() {
        return this.parent.getLocale();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getLocales()
     */
    public Enumeration getLocales() {
        return this.parent.getLocales();
    }

    /**
     * @deprecated
     * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
     */
    public String getRealPath(String path) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getRemoteAddr()
     */
    public String getRemoteAddr() {
        return this.parent.getRemoteAddr();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getRemoteHost()
     */
    public String getRemoteHost() {
        return this.parent.getRemoteHost();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
     */
    public String getRemoteUser() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
     *
     * TODO delegate to parent?
     */public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
     *
     * TODO What do we do with the session? Make it available in sub requests
     * too?
     */
    public String getRequestedSessionId() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getSession()
     *
     * TODO What do we do with the session? Make it available in sub requests
     * too?
     */
    public HttpSession getSession() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
     *
     * TODO What do we do with the session? Make it available in sub requests
     * too?
     */
    public HttpSession getSession(boolean create) {
        // TODO Auto-generated method stub
        if (create) {
            return new HttpSession() {

                public Object getAttribute(String name) {
                    // TODO Auto-generated method stub
                    return null;
                }

                public Enumeration getAttributeNames() {
                    // TODO Auto-generated method stub
                    return null;
                }

                public long getCreationTime() {
                    // TODO Auto-generated method stub
                    return 0;
                }

                public String getId() {
                    // TODO Auto-generated method stub
                    return null;
                }

                public long getLastAccessedTime() {
                    // TODO Auto-generated method stub
                    return 0;
                }

                public int getMaxInactiveInterval() {
                    // TODO Auto-generated method stub
                    return 0;
                }

                public ServletContext getServletContext() {
                    // TODO Auto-generated method stub
                    return null;
                }

                public HttpSessionContext getSessionContext() {
                    // TODO Auto-generated method stub
                    return null;
                }

                public Object getValue(String name) {
                    // TODO Auto-generated method stub
                    return null;
                }

                public String[] getValueNames() {
                    // TODO Auto-generated method stub
                    return null;
                }

                public void invalidate() {
                    // TODO Auto-generated method stub

                }

                public boolean isNew() {
                    // TODO Auto-generated method stub
                    return false;
                }

                public void putValue(String name, Object value) {
                    // TODO Auto-generated method stub

                }

                public void removeAttribute(String name) {
                    // TODO Auto-generated method stub

                }

                public void removeValue(String name) {
                    // TODO Auto-generated method stub

                }

                public void setAttribute(String name, Object value) {
                    // TODO Auto-generated method stub

                }

                public void setMaxInactiveInterval(int interval) {
                    // TODO Auto-generated method stub

                }
            };
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
     *
     * TODO No authentication handling between blocks yet
     */
    public Principal getUserPrincipal() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
     *
     * TODO What do we do with the session? Make it available in sub requests
     * too?
     */
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     *
     * TODO What do we do with the session? Make it available in sub requests
     * too?
     */
    public boolean isRequestedSessionIdFromUrl() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
     *
     * TODO What do we do with the session? Make it available in sub requests
     * too?
     */
    public boolean isRequestedSessionIdFromURL() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
     *
     * TODO What do we do with the session? Make it available in sub requests
     * too?
     */
    public boolean isRequestedSessionIdValid() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#isSecure()
     */
    public boolean isSecure() {
        // TODO Auto-generated method stub
        return this.parent.isSecure();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
     *
     * TODO No authentication handling between blocks yet
     */
    public boolean isUserInRole(String role) {
        return false;
    }

    public String getLocalAddr() {
        return this.parent.getLocalAddr();
    }

    public String getLocalName() {
        return this.parent.getLocalName();
    }

    /*
     * TODO delegate to parent?
     */
    public int getLocalPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * TODO delegate to parent?
     */
    public int getRemotePort() {
        // TODO Auto-generated method stub
        return 0;
    }
}
