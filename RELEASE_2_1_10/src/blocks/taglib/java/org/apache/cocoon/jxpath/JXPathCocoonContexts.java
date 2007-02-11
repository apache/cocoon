/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.jxpath;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.servlet.Constants;
import org.apache.commons.jxpath.servlet.KeywordVariables;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * Component that allocate and cache JXPathContexts bound to VariableContext,
 * Cocoon Request, Cocoon Session and Cocoon Context.
 *
 * <p>
 * If you need to limit the attibute lookup to just one scope, you can use the
 * pre-definded variables "request", "session" and "application".
 * For example, the expression "$session/foo" extracts the value of the
 * session attribute named "foo".</p>
 *
 * <p>
 * Following are some implementation details.
 * There is a separate JXPathContext for each of the four scopes. These contexts are chained
 * according to the nesting of the scopes.  So, the parent of the "variable"
 * JXPathContext is a "request" JXPathContext, whose parent is a "session"
 * JXPathContext (that is if there is a session), whose parent is an "application"
 * context.</p>
 *
 * <p>
 * Since JXPath chains lookups for variables and extension functions, variables
 * and extension function declared in the outer scopes are also available in
 * the inner scopes.</p>
 *
 * <p>
 * The "session" variable will be undefined if there is no session for this servlet.
 * JXPath does not automatically create sessions.</p>
 *
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version $Id$
 */
public final class JXPathCocoonContexts implements Component, Contextualizable, ThreadSafe {

    public static final String ROLE = JXPathCocoonContexts.class.getName();

    private static final String VARCONTEXT = Constants.JXPATH_CONTEXT + "/VAR";

    private static JXPathContextFactory factory;
    private org.apache.avalon.framework.context.Context context;

    static {
        factory = JXPathContextFactory.newInstance();
        JXPathIntrospector.registerDynamicClass(RequestProxy.class, CocoonRequestHandler.class);
        JXPathIntrospector.registerDynamicClass(SessionProxy.class, CocoonSessionHandler.class);
        JXPathIntrospector.registerDynamicClass(ContextProxy.class, CocoonContextHandler.class);
    }

    public void contextualize(org.apache.avalon.framework.context.Context context) {
        this.context = context;
    }

    public final JXPathContext getVariableContext() {
        final Map objectModel = ContextHelper.getObjectModel(this.context);

        Request request = ObjectModelHelper.getRequest(objectModel);
        JXPathContext context = (JXPathContext) request.getAttribute(VARCONTEXT);
        if (context == null) {
            context = factory.newContext(getRequestContext(), null);
            request.setAttribute(VARCONTEXT, context);
        }
        return context;
    }

    /**
     * Returns a JXPathContext bound to the "request" scope.
     * Caches that context within the request itself.
     */
    public final JXPathContext getRequestContext() {
        final Map objectModel = ContextHelper.getObjectModel(this.context);

        Request request = ObjectModelHelper.getRequest(objectModel);
        JXPathContext context = (JXPathContext) request.getAttribute(Constants.JXPATH_CONTEXT);
        if (context == null) {
            Context envContext = ObjectModelHelper.getContext(objectModel);
            JXPathContext parentContext = null;

            Session session = request.getSession(false);
            if (session != null) {
                parentContext = getSessionContext(session, envContext);
            } else {
                parentContext = getApplicationContext(envContext);
            }

            request = new RequestProxy(request);
            context = factory.newContext(parentContext, request);
            context.setVariables(new KeywordVariables(Constants.REQUEST_SCOPE, request));
            request.setAttribute(Constants.JXPATH_CONTEXT, context);
        }
        return context;
    }

    /**
     * Returns a JXPathContext bound to the "session" scope.
     * Caches that context within the session itself.
     */
    public final JXPathContext getSessionContext(Session session, Context envContext) {
        JXPathContext context = (JXPathContext) session.getAttribute(Constants.JXPATH_CONTEXT);
        if (context == null) {
            JXPathContext parentContext = getApplicationContext(envContext);
            session = new SessionProxy(session);
            context = factory.newContext(parentContext, session);
            context.setVariables(new KeywordVariables(Constants.SESSION_SCOPE, session));
            session.setAttribute(Constants.JXPATH_CONTEXT, context);
        }
        return context;
    }

    /**
     * Returns a JXPathContext bound to the "application" scope. Caches that context
     * within the servlet context itself.
     */
    public final JXPathContext getApplicationContext(Context envContext) {
        JXPathContext context = (JXPathContext) envContext.getAttribute(Constants.JXPATH_CONTEXT);
        if (context == null) {
            envContext = new ContextProxy(envContext);
            context = factory.newContext(null, envContext);
            context.setVariables(new KeywordVariables(Constants.APPLICATION_SCOPE, envContext));
            envContext.setAttribute(Constants.JXPATH_CONTEXT, context);
        }
        return context;
    }

    public static final class RequestProxy implements Request {
        private Request delegate;

        public RequestProxy (Request delegate) {
            this.delegate = delegate;
        }

        public Object get(String name) {
            return this.delegate.get(name);
        }

        public Object getAttribute(String name) {
            return this.delegate.getAttribute(name);
        }

        public Enumeration getAttributeNames() {
            return this.delegate.getAttributeNames();
        }

        public String getCharacterEncoding() {
            return this.delegate.getCharacterEncoding();
        }

        public void setCharacterEncoding(String enc)
        throws java.io.UnsupportedEncodingException {
            this.delegate.setCharacterEncoding(enc);
        }

        public int getContentLength() {
            return this.delegate.getContentLength();
        }

        public String getContentType() {
            return this.delegate.getContentType();
        }

        public String getParameter(String name) {
            return this.delegate.getParameter(name);
        }

        public Enumeration getParameterNames() {
            return this.delegate.getParameterNames();
        }

        public String[] getParameterValues(String name) {
            return this.delegate.getParameterValues(name);
        }

        public String getProtocol() {
            return this.delegate.getProtocol();
        }

        public String getScheme() {
            return this.delegate.getScheme();
        }

        public String getServerName() {
            return this.delegate.getServerName();
        }

        public int getServerPort() {
            return this.delegate.getServerPort();
        }

        public String getRemoteAddr() {
            return this.delegate.getRemoteAddr();
        }

        public String getRemoteHost() {
            return this.delegate.getRemoteHost();
        }

        public void setAttribute(String name, Object o) {
            this.delegate.setAttribute(name, o);
        }

        public void removeAttribute(String name) {
            this.delegate.removeAttribute(name);
        }

        public Locale getLocale() {
            return this.delegate.getLocale();
        }

        public Enumeration getLocales() {
            return this.delegate.getLocales();
        }

        public boolean isSecure() {
            return this.delegate.isSecure();
        }

        public Cookie[] getCookies() {
            return this.delegate.getCookies();
        }

        public Map getCookieMap() {
            return this.delegate.getCookieMap();
        }

        public long getDateHeader(String name) {
            return this.delegate.getDateHeader(name);
        }

        public String getHeader(String name) {
            return this.delegate.getHeader(name);
        }

        public Enumeration getHeaders(String name) {
            return this.delegate.getHeaders(name);
        }

        public Enumeration getHeaderNames() {
            return this.delegate.getHeaderNames();
        }

        public String getMethod() {
            return this.delegate.getMethod();
        }

        public String getPathInfo() {
            return this.delegate.getPathInfo();
        }

        public String getPathTranslated() {
            return this.delegate.getPathTranslated();
        }

        public String getContextPath() {
            return this.delegate.getContextPath();
        }

        public String getQueryString() {
            return this.delegate.getQueryString();
        }

        public String getRemoteUser() {
            return this.delegate.getRemoteUser();
        }

        public String getRequestedSessionId() {
            return this.delegate.getRequestedSessionId();
        }

        public String getRequestURI() {
            return this.delegate.getRequestURI();
        }

        public String getSitemapURI() {
            return this.delegate.getSitemapURI();
        }

        public String getSitemapURIPrefix() {
            return this.delegate.getSitemapURIPrefix();
        }

        public String getServletPath() {
            return this.delegate.getServletPath();
        }

        public Session getSession(boolean create) {
            return this.delegate.getSession(create);
        }

        public Session getSession() {
            return this.delegate.getSession();
        }

        public boolean isRequestedSessionIdValid() {
            return this.delegate.isRequestedSessionIdValid();
        }

        public boolean isRequestedSessionIdFromCookie()  {
            return this.delegate.isRequestedSessionIdFromCookie();
        }

        public boolean isRequestedSessionIdFromURL() {
            return this.delegate.isRequestedSessionIdFromURL();
        }

        public Principal getUserPrincipal() {
            return this.delegate.getUserPrincipal();
        }

        public boolean isUserInRole(String role) {
            return this.delegate.isUserInRole(role);
        }

        public String getAuthType() {
            return this.delegate.getAuthType();
        }
    }

    public class SessionProxy implements Session {
        private Session delegate;

        public SessionProxy(Session delegate) {
            this.delegate = delegate;
        }

        public long getCreationTime() {
            return this.delegate.getCreationTime();
        }

        public String getId() {
            return this.delegate.getId();
        }

        public long getLastAccessedTime() {
            return this.delegate.getLastAccessedTime();
        }

        public void setMaxInactiveInterval(int interval) {
            this.delegate.setMaxInactiveInterval(interval);
        }

        public int getMaxInactiveInterval() {
            return this.delegate.getMaxInactiveInterval();
        }

        public Object getAttribute(String name) {
            return this.delegate.getAttribute(name);
        }

        public Enumeration getAttributeNames() {
            return this.delegate.getAttributeNames();
        }

        public void setAttribute(String name, Object value) {
            this.delegate.setAttribute(name, value);
        }

        public void removeAttribute(String name) {
            this.delegate.removeAttribute(name);
        }

        public void invalidate() {
            this.delegate.invalidate();
        }

        public boolean isNew() {
            return this.delegate.isNew();
        }
    }

    public class ContextProxy implements Context {
        private Context delegate;

        public ContextProxy(org.apache.cocoon.environment.Context delegate) {
            this.delegate = delegate;
        }

        public Object getAttribute(String name) {
            return this.delegate.getAttribute(name);
        }

        public void setAttribute(String name, Object value) {
            this.delegate.setAttribute(name, value);
        }

        public void removeAttribute(String name) {
            this.delegate.removeAttribute(name);
        }

        public Enumeration getAttributeNames() {
            return this.delegate.getAttributeNames();
        }

        public URL getResource(String path) throws MalformedURLException {
            return this.delegate.getResource(path);
        }

        public String getRealPath(String path) {
            return this.delegate.getRealPath(path);
        }

        public String getMimeType(String file) {
            return this.delegate.getMimeType(file);
        }

        public String getInitParameter(String name) {
            return this.delegate.getInitParameter(name);
        }

        public InputStream getResourceAsStream(String path) {
            return this.delegate.getResourceAsStream(path);
        }
    }
}
