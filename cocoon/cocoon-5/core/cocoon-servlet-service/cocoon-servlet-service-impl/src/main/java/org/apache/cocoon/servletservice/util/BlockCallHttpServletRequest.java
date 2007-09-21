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
 * Create a HttpServletRequest from an URL, that is used while calling e.g. a block.
 * 
 * @version $Id$
 */
public class BlockCallHttpServletRequest implements HttpServletRequest {
    
    /**
     * Protocol of block call requests.
     */
    private static final String PROTOCOL = "HTTP/1.1";

    /**
     * Date header format definied by RFC 822,
     * see http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3
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
     * Request method. If not set via {@link #setMethod(String)},
     * defaults to <code>GET</code>.
     */
    private String method;

    /**
     * Request headers map
     */
    private final Map headers;

    /**
     * Request parameters extracted from {@link #uri}.
     */
    private final RequestParameters parameters;

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
     * @param uri points to the called servlet
     * @param parent reference to the request object that makes a servlet call
     */
    public BlockCallHttpServletRequest(URI uri, HttpServletRequest parent) {
        this.parent = parent;
        this.uri = uri;
        this.headers = new HashMap();
        this.parameters = new RequestParameters(this.uri.getQuery());
        this.method = "GET";
        this.contentLength = -1;
        this.content = NullServletInputStream.INSTANCE;
        this.attributes = new HashMap();
    }

    /* (non-Javadoc)
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

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getScheme()
     */
    public String getScheme() {
        return this.uri.getScheme();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getServerName()
     */
    public String getServerName() {
        // TODO implement this
        return "";
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getServerPort()
     */
    public int getServerPort() {
        // TODO implement this
        return 80;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getContextPath()
     */
    public String getContextPath() {
        return parent.getContextPath();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getServletPath()
     */
    public String getServletPath() {
        // TODO Is this right?
        return "";
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getPathInfo()
     */
    public String getPathInfo() {
        return this.uri.getPath();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
     */
    public String getPathTranslated() {
        // TODO This is legal but more info might be possible
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getQueryString()
     */
    public String getQueryString() {
        return this.uri.getQuery();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    public String getRequestURI() {
        // TODO Is this right?
        return getContextPath() + getServletPath() + getPathInfo();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    public StringBuffer getRequestURL() {
        return new StringBuffer(getScheme()).append(':').append(getRequestURI());
    }
    
    //
    // Request headers
    //

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
     */
    public String getHeader(String name) {
        return (String) headers.get(name);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
     */
    public Enumeration getHeaders(String name) {
        return new IteratorEnumeration(headers.values().iterator());
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    /* (non-Javadoc)
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
        setHeader(name, dateFormat.format(new Date(date)));
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
     */
    public int getIntHeader(String name) {
        String header = getHeader(name);
        if (header == null) {
            return -1;
        }

        return Integer.parseInt(header);
    }

    public void setIntHeader(String name, int value) {
        setHeader(name, String.valueOf(value));
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
     */
    public Enumeration getHeaderNames() {
        return new IteratorEnumeration(headers.keySet().iterator());
    }

    //
    // Request parameters
    //

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */
    public String getParameter(String name) {
        return this.parameters.getParameter(name);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String name) {
        return this.parameters.getParameterValues(name);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    public Enumeration getParameterNames() {
        return this.parameters.getParameterNames();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {
        return this.parameters.getParameterMap();
    }

    //
    // Request body
    //

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getCharacterEncoding()
     */
    public String getCharacterEncoding() {
        return this.encoding;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
     */
    public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
        this.encoding = encoding;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getContentLength()
     */
    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getContentType()
     */
    public String getContentType() {
        // TODO Doesn't handle input streams yet
        return null;
    }

    /* (non-Javadoc)
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

    /* (non-Javadoc)
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

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return new IteratorEnumeration(this.attributes.keySet().iterator());
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        if (value != null) {
            this.attributes.put(name, value);
        } else {
            removeAttribute(name);
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    //
    //
    //

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getAuthType()
     */
    public String getAuthType() {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getCookies()
     */
    public Cookie[] getCookies() {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getLocale()
     */
    public Locale getLocale() {
        // TODO Implement this
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getLocales()
     */
    public Enumeration getLocales() {
        // TODO Implement this
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
     */
    public String getRealPath(String path) {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRemoteAddr()
     */
    public String getRemoteAddr() {
        // TODO The URI of the current block might be an appropriate choice.
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRemoteHost()
     */
    public String getRemoteHost() {
        // TODO Local host might be an appropriate choice
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
     */
    public String getRemoteUser() {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
     */
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
     */
    public String getRequestedSessionId() {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getSession()
     */
    public HttpSession getSession() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
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

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        // TODO No authentication handling between blocks yet
        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
     */
    public boolean isRequestedSessionIdFromCookie() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     */
    public boolean isRequestedSessionIdFromUrl() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
     */
    public boolean isRequestedSessionIdFromURL() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
     */
    public boolean isRequestedSessionIdValid() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#isSecure()
     */
    public boolean isSecure() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
     */
    public boolean isUserInRole(String role) {
        // TODO No authentication handling between blocks yet
        return false;
    }

    public String getLocalAddr() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getLocalName() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getLocalPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getRemotePort() {
        // TODO Auto-generated method stub
        return 0;
    }
}
