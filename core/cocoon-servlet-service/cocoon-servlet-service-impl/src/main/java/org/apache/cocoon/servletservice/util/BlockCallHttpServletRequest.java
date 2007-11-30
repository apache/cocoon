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

import org.apache.cocoon.callstack.CallFrame;
import org.apache.cocoon.callstack.CallStack;
import org.apache.cocoon.callstack.environment.CallFrameHelper;
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
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * <p>
 * Create a HttpServletRequest from an URL, that is used while calling e.g. a
 * servlet service. The current implementation forwards headers, attributes and
 * parameters.
 * </p>
 * <p>
 * Note: Session handling and HTTP authentication information hasn't been
 * implemented yet.
 * </p>
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
     * The <code>parentRequest</code> holds reference to the request object
     * that makes a servlet call.
     */
    private HttpServletRequest parentRequest;

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
    private final Headers headers;

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
    private final Attributes attributes;

    private Parameters parameters;

    /**
     * @param uri
     *            points to the called servlet
     * @param parentRequest
     *            reference to the request object that makes a servlet call
     */
    public BlockCallHttpServletRequest(URI uri, HttpServletRequest parentRequest) {
        this.parentRequest = parentRequest;
        this.uri = uri;
        this.headers = new Headers();
        this.method = "GET";
        this.contentLength = -1;
        this.content = NullServletInputStream.INSTANCE;
        this.attributes = new Attributes();
        this.parameters = new Parameters();
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
        return this.parentRequest.getServerName();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getServerPort()
     */
    public int getServerPort() {
        return this.parentRequest.getServerPort();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getContextPath()
     */
    public String getContextPath() {
        return parentRequest.getContextPath();
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
        return (String) this.headers.getValue(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
     */
    public Enumeration getHeaders(String name) {
        return this.headers.getNames();
    }

    public void setHeader(String name, String value) {
        this.headers.setValue(name, value);
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
        return this.headers.getNames();
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
        return (String) this.parameters.getValue(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String name) {
        return this.parameters.getValues(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    public Enumeration getParameterNames() {
        return this.parameters.getNames();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {
        return this.parameters.getValues();
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
        return this.content;
    }

    public void setInputStream(final InputStream inputStream) {
        try {
            this.contentLength = inputStream.available();
        } catch (IOException e) {
            this.contentLength = -1;
        }

        this.content = new ServletInputStream() {
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
        return this.attributes.getValue(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return this.attributes.getNames();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#setAttribute(java.lang.String,
     *      java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        if (value != null) {
            this.attributes.setValue(name, value);
        } else {
            this.removeAttribute(name);
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
        return this.parentRequest.getCookies();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getLocale()
     */
    public Locale getLocale() {
        return this.parentRequest.getLocale();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getLocales()
     */
    public Enumeration getLocales() {
        return this.parentRequest.getLocales();
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
        return this.parentRequest.getRemoteAddr();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletRequest#getRemoteHost()
     */
    public String getRemoteHost() {
        return this.parentRequest.getRemoteHost();
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
        return this.parentRequest.isSecure();
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
        return this.parentRequest.getLocalAddr();
    }

    public String getLocalName() {
        return this.parentRequest.getLocalName();
    }

    public int getLocalPort() {
        return this.parentRequest.getLocalPort();
    }

    public int getRemotePort() {
        return this.parentRequest.getRemotePort();
    }

    private abstract static class Values implements Serializable {

        /** The parameter names are the keys and the value is a List object */
        Map values = new HashMap();

        /**
         * Construct a new object from a queryString
         */
        public Values() {
        }

        protected void setValue(String name, Object value) {
            List list;
            if (this.values.containsKey(name)) {
                list = (List) values.get(name);
            } else {
                list = new ArrayList();
                this.values.put(name, list);
            }
            list.add(value);
        }

        public Object getValue(String name) {
            if (this.values.containsKey(name)) {
                return ((List) values.get(name)).get(0);
            }

            return getValueOfCaller(name);
        }

        protected abstract Object getValueOfCaller(String name);

        public Enumeration getNames() {
            Set names = new HashSet();
            for (int i = 0; i < CallStack.size(); i++) {
                CallFrame frame = CallStack.frameAt(i);
                HttpServletRequest request = (HttpServletRequest) frame.getAttribute(CallFrameHelper.REQUEST_OBJECT);
                if (request instanceof BlockCallHttpServletRequest) {
                    names.addAll(this.values.keySet());
                } else {
                    for (Enumeration enumeration = request.getParameterNames(); enumeration.hasMoreElements();) {
                        names.add(enumeration.nextElement());
                    }
                }
                if (request.equals(this.getRequest())) {
                    break;
                }
            }

            return new EnumerationFromIterator(names.iterator());
        }

        public Map getValues() {
            Map result = new HashMap();
            for (int i = 0; i < CallStack.size(); i++) {
                CallFrame frame = CallStack.frameAt(i);
                HttpServletRequest request = (HttpServletRequest) frame.getAttribute(CallFrameHelper.REQUEST_OBJECT);
                if (request instanceof BlockCallHttpServletRequest)
                    result.putAll(this.values);
                else {
                    result.putAll(request.getParameterMap());
                }
                if (request.equals(this.getRequest()))
                    break;
            }

            return result;
        }

        protected abstract BlockCallHttpServletRequest getRequest();

        // protected abstract Map getValues(HttpServletRequest request);

        final class EnumerationFromIterator implements Enumeration {

            private Iterator iterator;

            EnumerationFromIterator(Iterator iter) {
                this.iterator = iter;
            }

            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            public Object nextElement() {
                return iterator.next();
            }
        }

    }

    private class Parameters extends Values {

        public Parameters() {
            if (this.getRequest().uri.getQuery() != null) {
                StringTokenizer st = new StringTokenizer(this.getRequest().uri.getQuery(), "&");
                while (st.hasMoreTokens()) {
                    String pair = st.nextToken();
                    int pos = pair.indexOf('=');
                    if (pos != -1) {
                        this.setValue(this.parseName(pair.substring(0, pos)), this.parseName(pair.substring(pos + 1,
                                        pair.length())));
                    }
                }
            }
        }

        private String parseName(String s) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                case '+':
                    sb.append(' ');
                    break;
                case '%':
                    try {
                        if (s.charAt(i + 1) == 'u') {
                            // working with multi-byte symbols in format %uXXXX
                            sb.append((char) Integer.parseInt(s.substring(i + 2, i + 6), 16));
                            i += 5; // 4 digits and 1 symbol u
                        } else {
                            // working with single-byte symbols in format %YY
                            sb.append((char) Integer.parseInt(s.substring(i + 1, i + 3), 16));
                            i += 2;
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException();
                    } catch (StringIndexOutOfBoundsException e) {
                        String rest = s.substring(i);
                        sb.append(rest);
                        if (rest.length() == 2)
                            i++;
                    }

                    break;
                default:
                    sb.append(c);
                    break;
                }
            }
            return sb.toString();
        }

        public String[] getValues(String name) {
            List list = (List) this.values.get(name);
            if (list == null)
                return null;

            String[] result = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                result[i] = (String) list.get(i);
            }
            return result;
        }

        protected BlockCallHttpServletRequest getRequest() {
            return BlockCallHttpServletRequest.this;
        }

        protected Object getValueOfCaller(String name) {
            return this.getRequest().parentRequest.getParameter(name);
        }
    }

    private class Headers extends Values {
        public Object getValueOfCaller(String name) {
            return this.getRequest().parentRequest.getHeader(name);
        }

        public Enumeration getValues(String name) {
            List list = (List) this.values.get(name);
            if (list == null) {
                return new Enumeration() {

                    public boolean hasMoreElements() {
                        return false;
                    }

                    public Object nextElement() {
                        throw new NoSuchElementException();
                    }

                };
            }

            return new IteratorEnumeration(list.iterator());
        }

        protected BlockCallHttpServletRequest getRequest() {
            return BlockCallHttpServletRequest.this;
        }
    }

    private class Attributes extends Values {

        public Object getValue(String name) {
            if (values.containsKey(name)) {
                return values.get(name);
            }

            return getValueOfCaller(name);
        }

        public Object getValueOfCaller(String name) {
            return this.getRequest().parentRequest.getAttribute(name);
        }

        protected void setValue(String name, Object value) {
            this.values.put(name, value);
        }

        public void remove(String name) {
            if (this.values.containsKey(name)) {
                this.values.remove(name);
            } else {
                this.getRequest().parentRequest.removeAttribute(name);
            }
        }

        protected BlockCallHttpServletRequest getRequest() {
            return BlockCallHttpServletRequest.this;
        }

    }

}
