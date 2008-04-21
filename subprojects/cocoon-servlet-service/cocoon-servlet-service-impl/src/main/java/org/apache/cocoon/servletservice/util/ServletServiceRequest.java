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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.apache.cocoon.callstack.CallFrame;
import org.apache.cocoon.callstack.CallStack;
import org.apache.cocoon.callstack.environment.CallFrameHelper;
import org.apache.cocoon.servletservice.ServletServiceContext;
import org.apache.commons.collections.iterators.IteratorEnumeration;

/**
 * <p>
 * Create a {@link HttpServletRequest} from an URL, that is used while calling e.g. a
 * servlet service. The current implementation forwards headers, attributes and
 * parameters.
 * </p>
 *
 * @version $Id: BlockCallHttpServletRequest.java 577519 2007-09-20 03:05:26Z
 *          vgritsenko $
 * @since 1.0.0
 */
public class ServletServiceRequest implements HttpServletRequest {

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

    private ServletServiceContext context;

    /**
     * @param uri
     *            points to the called servlet
     * @param parentRequest
     *            reference to the request object that makes a servlet call
     */
    public ServletServiceRequest(URI uri, HttpServletRequest parentRequest) {
        this.parentRequest = parentRequest;
        this.uri = uri;
        this.headers = new Headers();
        this.method = "GET";
        this.contentLength = -1;
        this.content = NullServletInputStream.INSTANCE;
        this.attributes = new Attributes();
        this.parameters = new Parameters();
    }

    public String getProtocol() {
        return PROTOCOL;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    //
    // Request URI parts
    //

    public String getScheme() {
        return this.uri.getScheme();
    }

    public String getServerName() {
        return this.parentRequest.getServerName();
    }

    public int getServerPort() {
        return this.parentRequest.getServerPort();
    }

    public String getContextPath() {
        return this.parentRequest.getContextPath();
    }

    public String getServletPath() {
        // TODO Is this right?
        return "";
    }

    public String getPathInfo() {
        return this.uri.getPath();
    }

    public String getPathTranslated() {
        // TODO This is legal but more info might be possible
        return null;
    }

    public String getQueryString() {
        return this.uri.getQuery();
    }

    public String getRequestURI() {
        // TODO Is this right?
        return this.getContextPath() + this.getServletPath() + this.getPathInfo();
    }

    public StringBuffer getRequestURL() {
        return new StringBuffer(this.getScheme()).append(':').append(this.getRequestURI());
    }

    //
    // Request headers
    //

    public String getHeader(String name) {
        return (String) this.headers.getValue(name);
    }

    public Enumeration getHeaders(String name) {
        return this.headers.getNames();
    }

    public void setHeader(String name, String value) {
        this.headers.setValue(name, value);
    }

    public long getDateHeader(String name) {
        String header = this.getHeader(name);
        if (header == null) {
            return -1;
        }

        try {
            return this.dateFormat.parse(header).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDateHeader(String name, long date) {
        this.setHeader(name, this.dateFormat.format(new Date(date)));
    }

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

    public Enumeration getHeaderNames() {
        return this.headers.getNames();
    }

    //
    // Request parameters
    //

    public String getParameter(String name) {
        return (String) this.parameters.getValue(name);
    }

    public String[] getParameterValues(String name) {
        return this.parameters.getValues(name);
    }

    public Enumeration getParameterNames() {
        return this.parameters.getNames();
    }

    public Map getParameterMap() {
        return this.parameters.getValues();
    }

    //
    // Request body
    //

    public String getCharacterEncoding() {
        return this.encoding;
    }

    public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
        this.encoding = encoding;
    }

    public int getContentLength() {
        return this.contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    /*
     * TODO Doesn't handle input streams yet
     */
    public String getContentType() {
        return null;
    }

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

    public BufferedReader getReader() throws IOException {
        Reader reader;
        String encoding = this.getCharacterEncoding();
        if (encoding == null) {
            reader = new InputStreamReader(this.getInputStream());
        } else {
            reader = new InputStreamReader(this.getInputStream(), encoding);
        }

        return new BufferedReader(reader);
    }

    //
    // Request attributes
    //

    public Object getAttribute(String name) {
        return this.attributes.getValue(name);
    }

    public Enumeration getAttributeNames() {
        return this.attributes.getNames();
    }

    public void setAttribute(String name, Object value) {
        if (value != null) {
            this.attributes.setValue(name, value);
        } else {
            this.removeAttribute(name);
        }
    }

    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    public String getAuthType() {
        return null;
    }

    public Cookie[] getCookies() {
        return this.parentRequest.getCookies();
    }

    public Locale getLocale() {
        return this.parentRequest.getLocale();
    }

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

    public String getRemoteAddr() {
        return this.parentRequest.getRemoteAddr();
    }

    public String getRemoteHost() {
        return this.parentRequest.getRemoteHost();
    }

    public String getRemoteUser() {
        return this.parentRequest.getRemoteUser();
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return this.context.getRequestDispatcher(path);
    }


    public String getRequestedSessionId() {
        return this.parentRequest.getRequestedSessionId();
    }

    public HttpSession getSession() {
        Session session = (Session) this.getSession(true);
        session.setRequest(this);
        return session;
    }

    public HttpSession getSession(boolean create) {

        HttpServletRequest request = this.parentRequest;
        while (request != null) {
            if (request instanceof ServletServiceRequest) {
                request = ((ServletServiceRequest) request).parentRequest;
            } else {
                break;
            }
        }
        HttpSession outestSession = request.getSession();
        Session session = null;
        // each block's session object is stored in the outest session as an
        // attribute
        if (this != request) {
            session = (Session) outestSession.getAttribute(HttpSession.class + "_" + this.context.getMountPath());
        } else {
            session = (Session) outestSession;
        }

        if (session == null && create) {
            session = new Session(this.context);
            outestSession.setAttribute(HttpSession.class + "_" + this.context.getMountPath(), session);
        }

        if (session != null) {
            session.setRequest(this);
        }

        return session;
    }

    public void setContext(ServletContext context) {
        this.context = (ServletServiceContext) context;
    }

    public Principal getUserPrincipal() {
        return this.parentRequest.getUserPrincipal();
    }

    public boolean isRequestedSessionIdFromCookie() {
        return this.parentRequest.isRequestedSessionIdFromCookie();
    }

    public boolean isRequestedSessionIdFromUrl() {
        return this.isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdFromURL() {
        return this.parentRequest.isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdValid() {
        return this.parentRequest.isRequestedSessionIdValid();
    }

    public boolean isSecure() {
        return this.parentRequest.isSecure();
    }

    public boolean isUserInRole(String role) {
        return this.parentRequest.isUserInRole(role);
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

        public Values() {
        }

        protected void setValue(String name, Object value) {
            List list;
            if (this.values.containsKey(name)) {
                list = (List) this.values.get(name);
            } else {
                list = new ArrayList();
                this.values.put(name, list);
            }
            list.add(value);
        }

        public Object getValue(String name) {
            if (this.values.containsKey(name)) {
                return ((List) this.values.get(name)).get(0);
            }

            return this.getValueOfCaller(name);
        }

        protected abstract Object getValueOfCaller(String name);

        protected abstract Enumeration namesOf(HttpServletRequest request);

        public Enumeration getNames() {
            Set names = new HashSet();
            for (int i = 0; i < CallStack.size(); i++) {
                CallFrame frame = CallStack.frameAt(i);
                HttpServletRequest request = (HttpServletRequest) frame.getAttribute(CallFrameHelper.REQUEST_OBJECT);
                if (request instanceof ServletServiceRequest) {
                    names.addAll(this.values.keySet());
                } else {
                    for (Enumeration enumeration = this.namesOf(request); enumeration.hasMoreElements();) {
                        names.add(enumeration.nextElement());
                    }
                }
                if (request.equals(this.getRequest())) {
                    break;
                }
            }

            return new EnumerationFromIterator(names.iterator());
        }

        protected abstract ServletServiceRequest getRequest();

        final class EnumerationFromIterator implements Enumeration {

            private Iterator iterator;

            EnumerationFromIterator(Iterator iter) {
                this.iterator = iter;
            }

            public boolean hasMoreElements() {
                return this.iterator.hasNext();
            }

            public Object nextElement() {
                return this.iterator.next();
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
                        IllegalArgumentException iae = new IllegalArgumentException();
                        iae.initCause(e);
                        throw iae;
                    } catch (StringIndexOutOfBoundsException e) {
                        String rest = s.substring(i);
                        sb.append(rest);
                        if (rest.length() == 2) {
                            i++;
                        }
                    }

                    break;
                default:
                    sb.append(c);
                    break;
                }
            }
            return sb.toString();
        }

        public Map getValues() {
            Map result = new HashMap();
            for (int i = 0; i < CallStack.size(); i++) {
                CallFrame frame = CallStack.frameAt(i);
                HttpServletRequest request = (HttpServletRequest) frame.getAttribute(CallFrameHelper.REQUEST_OBJECT);
                if (request instanceof ServletServiceRequest) {
                    result.putAll(this.values);
                } else {
                    result.putAll(request.getParameterMap());
                }
                if (request.equals(this.getRequest())) {
                    break;
                }
            }

            return result;
        }

        public String[] getValues(String name) {
            List list = (List) this.values.get(name);
            if (list == null) {
                return null;
            }

            String[] result = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                result[i] = (String) list.get(i);
            }
            return result;
        }

        protected ServletServiceRequest getRequest() {
            return ServletServiceRequest.this;
        }

        protected Object getValueOfCaller(String name) {
            return this.getRequest().parentRequest.getParameter(name);
        }

        protected Enumeration namesOf(HttpServletRequest request) {
            return request.getParameterNames();
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

        protected ServletServiceRequest getRequest() {
            return ServletServiceRequest.this;
        }

        protected Enumeration namesOf(HttpServletRequest request) {
            return request.getHeaderNames();
        }
    }

    private class Attributes extends Values {

        public Object getValue(String name) {
            if (this.values.containsKey(name)) {
                return this.values.get(name);
            }

            return this.getValueOfCaller(name);
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

        protected ServletServiceRequest getRequest() {
            return ServletServiceRequest.this;
        }

        protected Enumeration namesOf(HttpServletRequest request) {
            return request.getAttributeNames();
        }

    }

    private static class Session extends Values implements HttpSession {

        private ServletServiceContext context;

        private transient ServletServiceRequest request;

        public Session(ServletServiceContext context) {
            this.context = context;
        }

        protected Object getValueOfCaller(String name) {
            return this.getRequest().parentRequest.getSession().getAttribute(name);
        }

        protected Enumeration namesOf(HttpServletRequest request) {
            return this.getRequest().parentRequest.getSession().getAttributeNames();
        }

        protected ServletServiceRequest getRequest() {
            return this.request;
        }

        private void setRequest(ServletServiceRequest request) {
            this.request = request;
        }

        public Object getAttribute(String name) {
            if (this.values.containsKey(name)) {
                return this.values.get(name);
            }

            return this.getValueOfCaller(name);
        }

        public Enumeration getAttributeNames() {
            return this.getNames();
        }

        public long getCreationTime() {
            return this.getRequest().parentRequest.getSession().getCreationTime();
        }

        public String getId() {
            return this.getRequest().parentRequest.getSession().getId();
        }

        public long getLastAccessedTime() {
            return this.getRequest().parentRequest.getSession().getLastAccessedTime();
        }

        public int getMaxInactiveInterval() {
            return this.getRequest().parentRequest.getSession().getMaxInactiveInterval();
        }

        public ServletContext getServletContext() {
            return this.context;
        }

        public HttpSessionContext getSessionContext() {
            throw new UnsupportedOperationException();
        }

        public String[] getValueNames() {
            throw new UnsupportedOperationException();
        }

        public void invalidate() {
            this.getRequest().parentRequest.getSession().invalidate();
        }

        public boolean isNew() {
            return this.getRequest().parentRequest.getSession().isNew();
        }

        public void putValue(String name, Object value) {
            this.setValue(name, value);
        }

        public void removeAttribute(String name) {
            this.removeValue(name);
        }

        public void removeValue(String name) {
            this.removeAttribute(name);
        }

        public void setAttribute(String name, Object value) {
            this.setValue(name, value);
        }

        public void setMaxInactiveInterval(int interval) {
            this.getRequest().parentRequest.getSession().setMaxInactiveInterval(interval);
        }

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Override the setValue and getValue methods because session attributes
        // are not arrays
        protected void setValue(String name, Object value) {
            this.values.put(name, value);
        }

        public Object getValue(String name) {
            return this.values.get(name);
        }

    }

}
