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
package org.apache.cocoon.components.flow.javascript.fom;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.flow.ContinuationsManager;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.flow.Interpreter.Argument;
import org.apache.cocoon.components.treeprocessor.sitemap.PipelinesNode;
import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.http.HttpResponse;
import org.apache.cocoon.util.ClassUtils;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.continuations.Continuation;

/**
 * Implementation of FOM (Flow Object Model).
 *
 * @since 2.1
 * @author <a href="mailto:coliver.at.apache.org">Christopher Oliver</a>
 * @author <a href="mailto:reinhard.at.apache.org">Reinhard P�tz</a>
 * @version CVS $Id: FOM_Cocoon.java,v 1.28 2004/01/28 16:07:17 vgritsenko Exp $
 */
public class FOM_Cocoon extends ScriptableObject {

    class CallContext {
        CallContext caller;
        Context avalonContext;
        ServiceManager serviceManager;
        FOM_JavaScriptInterpreter interpreter;
        Environment environment;
        ComponentManager componentManager;
        Logger logger;
        FOM_Request request;
        FOM_Response response;
        FOM_Session session;
        FOM_Context context;
        Scriptable parameters;
        FOM_Log log;
        WebContinuation lastContinuation;
        FOM_WebContinuation fwk;
        PageLocalScopeImpl currentPageLocal;

        public CallContext(CallContext caller,
                           FOM_JavaScriptInterpreter interp,
                           Environment env,
                           ComponentManager manager,
                           ServiceManager serviceManager,
                           Context avalonContext,
                           Logger logger,
                           WebContinuation lastContinuation) {
            this.caller = caller;
            this.interpreter = interp;
            this.environment = env;
            this.componentManager = manager;
            this.serviceManager = serviceManager;
            this.avalonContext = avalonContext;
            this.logger = logger;
            this.lastContinuation = lastContinuation;
            if (lastContinuation != null) {
                fwk = new FOM_WebContinuation(lastContinuation);
                Scriptable scope = FOM_Cocoon.this.getParentScope();
                fwk.setParentScope(scope);
                fwk.setPrototype(getClassPrototype(scope, fwk.getClassName()));
                this.currentPageLocal = fwk.getPageLocal();
            }
            if (this.currentPageLocal != null) {
                // "clone" the page local scope
                this.currentPageLocal = this.currentPageLocal.duplicate();
            } else {
                this.currentPageLocal = new PageLocalScopeImpl(getTopLevelScope(FOM_Cocoon.this));
            }
            pageLocal.setDelegate(this.currentPageLocal);
        }

        public FOM_WebContinuation getLastContinuation() {
            return fwk;
        }

        public void setLastContinuation(FOM_WebContinuation fwk) {
            this.fwk = fwk;
            if (fwk != null) {
                pageLocal.setDelegate(fwk.getPageLocal());
                this.lastContinuation = fwk.getWebContinuation();
            } else {
                this.lastContinuation = null;
            }
        }

        public FOM_Session getSession() {
            if (session != null) {
                return session;
            }
            Map objectModel = environment.getObjectModel();
            session =
                new FOM_Session(ObjectModelHelper.getRequest(objectModel).getSession(true));
            session.setParentScope(getParentScope());
            session.setPrototype(getClassPrototype(getParentScope(),
                                                   "FOM_Session"));
            return session;
        }

        public FOM_Request getRequest() {
            if (request != null) {
                return request;
            }
            Map objectModel = environment.getObjectModel();
            request = new FOM_Request(ObjectModelHelper.getRequest(objectModel));
            request.setParentScope(getParentScope());
            request.setPrototype(getClassPrototype(getParentScope(),
                                                   "FOM_Request"));
            return request;
        }

        public FOM_Context getContext() {
            if (context != null) {
                return context;
            }
            Map objectModel = getEnvironment().getObjectModel();
            context =
                new FOM_Context(ObjectModelHelper.getContext(objectModel));
            context.setParentScope(getParentScope());
            context.setPrototype(getClassPrototype(getParentScope(),
                                                   "FOM_Context"));
            return context;
        }

        public FOM_Response getResponse() {
            if (response != null) {
                return response;
            }
            Map objectModel = environment.getObjectModel();
            response =
                new FOM_Response(ObjectModelHelper.getResponse(objectModel));
            response.setParentScope(getParentScope());
            response.setPrototype(getClassPrototype(getParentScope(),
                                                    "FOM_Response"));
            return response;
        }

        public FOM_Log getLog() {
            if (log != null) {
                return log;
            }
            log = new FOM_Log(logger);
            log.setParentScope(getParentScope());
            log.setPrototype(getClassPrototype(getParentScope(), "FOM_Log"));
            return log;
        }

        public Scriptable getParameters() {
            return parameters;
        }

        public void setParameters(Scriptable parameters) {
            this.parameters = parameters;
        }
    }

    private CallContext currentCall;
    private PageLocalScopeHolder pageLocal;

    public String getClassName() {
        return "FOM_Cocoon";
    }


    // Called by FOM_JavaScriptInterpreter
    static void init(Scriptable scope) throws Exception {
        defineClass(scope, FOM_Cocoon.class);
        defineClass(scope, FOM_Request.class);
        defineClass(scope, FOM_Response.class);
        defineClass(scope, FOM_Cookie.class);
        defineClass(scope, FOM_Session.class);
        defineClass(scope, FOM_Context.class);
        defineClass(scope, FOM_Log.class);
        defineClass(scope, FOM_WebContinuation.class);
        defineClass(scope, PageLocalImpl.class);
    }

    void pushCallContext(FOM_JavaScriptInterpreter interp,
                         Environment env,
                         ComponentManager manager,
                         ServiceManager serviceManager,
                         Context avalonContext,
                         Logger logger,
                         WebContinuation lastContinuation) {
        if (pageLocal == null) {
            pageLocal = new PageLocalScopeHolder(getTopLevelScope(this));
        }
        this.currentCall = new CallContext(currentCall, interp, env, manager,
                                           serviceManager, avalonContext,
                                           logger, lastContinuation);
    }

    void popCallContext() {
        // Clear the scope attribute
        Request request = this.getRequest();
        if (request != null) {
            request.removeAttribute(FOM_JavaScriptFlowHelper.FOM_SCOPE);
        }
        this.currentCall = this.currentCall.caller;
        // reset current page locals
        if (this.currentCall != null) {
            pageLocal.setDelegate(this.currentCall.currentPageLocal);
        } else {
            pageLocal.setDelegate(null);
        }
    }


    public FOM_WebContinuation jsGet_continuation() {
        return currentCall.getLastContinuation();
    }

    public void jsSet_continuation(Object obj) {
        FOM_WebContinuation fwk = (FOM_WebContinuation)unwrap(obj);
        currentCall.setLastContinuation(fwk);
    }

    public FOM_WebContinuation jsFunction_sendPage(String uri,
                                                   Object obj,
                                                   Object wk)
        throws Exception {
        FOM_WebContinuation fom_wk = (FOM_WebContinuation)unwrap(wk);
        if (fom_wk != null) {
            // save page locals
            fom_wk.setPageLocal(pageLocal.getDelegate());
        }
        forwardTo(uri, unwrap(obj), fom_wk);
        return fom_wk;
    }

    public Scriptable jsFunction_createPageLocal() {
        return pageLocal.createPageLocal();
    }

    public void jsFunction_processPipelineTo(String uri,
                                             Object map,
                                             Object outputStream)
        throws Exception {
        if (!(unwrap(outputStream) instanceof OutputStream)) {
            throw new JavaScriptException("expected a java.io.OutputStream instead of " + outputStream);
        }
        getInterpreter().process(getParentScope(), this, uri, map,
                                 (OutputStream)unwrap(outputStream),
                                 getEnvironment());
    }

    public void jsFunction_redirectTo(String uri) throws Exception {
        // Cannot use environment directly as TreeProcessor uses own version of redirector
        // environment.redirect(false, uri);
        PipelinesNode.getRedirector(getEnvironment()).redirect(false, uri);
    }

    public void jsFunction_sendStatus(int sc) {
        PipelinesNode.getRedirector(getEnvironment()).sendStatus(sc);
    }

/*

 NOTE (SM): These are the hooks to the future FOM Event Model that will be
 designed in the future. It has been postponed because we think
 there are more important things to do at the moment, but these
 are left here to indicate that they are planned.

    public void jsFunction_addEventListener(String eventName,
                                            Object function) {
        // what is this?
    }

    public void jsFunction_removeEventListener(String eventName,
                                               Object function) {
        // what is this?
    }

*/

    /**
     * Access components.
     *
     * TODO: Do we want to restrict the access of sitemap components? (RP)
     * TODO: Do we want to raise an error or return null? (RP)
     */
    public Object jsFunction_getComponent(String id)
        throws Exception {
        return getComponentManager().lookup(id);
    }

    /**
     * Release pooled components.
     *
     * @param component - an <code>Object</code> that is an instance
     * of <code>org.apache.avalon.framework.component.Component</code>
     */
    public void jsFunction_releaseComponent( Object component ) throws Exception {
        try {
            this.getComponentManager().release( (Component) unwrap(component) );
        } catch( ClassCastException cce ) {
            throw new JavaScriptException( "Only components can be released!" );
        }
    }

    /**
     * Load the script file specified as argument.
     *
     * @param filename a <code>String</code> value
     * @return an <code>Object</code> value
     * @exception JavaScriptException if an error occurs
     */
    public Object jsFunction_load( String filename )
        throws Exception {
        org.mozilla.javascript.Context cx =
            org.mozilla.javascript.Context.getCurrentContext();
        Scriptable scope = getParentScope();
        Script script = getInterpreter().compileScript( cx,
                                                        getEnvironment(),
                                                        filename );
        return script.exec( cx, scope );
    }

    /**
     * Setup an object so that it can access the information provided to regular components.
     * This is done by calling the various Avalon lifecycle interfaces implemented by the object, which
     * are <code>LogEnabled</code>, <code>Contextualizable</code>, <code>ServiceManageable</code>,
     * <code>Composable</code> (even if deprecated) and <code>Initializable</code>.
     * <p>
     * <code>Contextualizable</code> is of primary importance as it gives access to the whole object model
     * (request, response, etc.) through the {@link org.apache.cocoon.components.ContextHelper} class.
     * <p>
     * Note that <code>Configurable</code> is ignored, as no configuration exists in a flowscript that
     * can be passed to the object.
     *
     * @param obj the object to setup
     * @return the same object (convenience that allows to write <code>var foo = cocoon.setupObject(new Foo());</code>).
     * @throws Exception if something goes wrong during setup.
     */
    public Object jsFunction_setupObject(Object obj) throws Exception {
        LifecycleHelper.setupComponent(
             unwrap(obj),
             this.getLogger(),
             this.getAvalonContext(),
             this.getServiceManager(),
             this.getComponentManager(),
             null,// roleManager
             null,// configuration
             true);
         return obj;
    }

    /**
     * Create and setup an object so that it can access the information provided to regular components.
     * This is done by calling the various Avalon lifecycle interfaces implemented by the object, which
     * are <code>LogEnabled</code>, <code>Contextualizable</code>, <code>ServiceManageable</code>,
     * <code>Composable</code> (even if deprecated) and <code>Initializable</code>.
     * <p>
     * <code>Contextualizable</code> is of primary importance as it gives access to the whole object model
     * (request, response, etc.) through the {@link org.apache.cocoon.components.ContextHelper} class.
     * <p>
     * Note that <code>Configurable</code> is ignored, as no configuration exists in a flowscript that
     * can be passed to the object.
     *
     * @param classObj the class to instantiate, either as a String or a Rhino NativeJavaClass object
     * @return an set up instance of <code>clazz</code>
     * @throws Exception if something goes wrong either during instantiation or setup.
     */
    public Object jsFunction_createObject(Object classObj) throws Exception {
        Object result;

        if (classObj instanceof String) {
            result = ClassUtils.newInstance((String)classObj);

        } else if (classObj instanceof NativeJavaClass) {
            Class clazz = ((NativeJavaClass)classObj).getClassObject();
            result = clazz.newInstance();

        } else {
            throw new IllegalArgumentException("cocoon.createObject expects either a String or Class argument, but got "
                + classObj.getClass());
        }

        return jsFunction_setupObject(result);
    }

    /**
     * Dispose an object that has been created using {@link #jsFunction_createObject(Object)}.
     *
     * @param obj
     * @throws Exception
     */
    public void jsFunction_disposeObject(Object obj) throws Exception {
        LifecycleHelper.decommission(obj);
    }

    public static class FOM_Request
        extends ScriptableObject implements Request {

        Request request;

        public FOM_Request() {
            // prototype ctor
        }

        public FOM_Request(Object request) {
            this.request = (Request)unwrap(request);
        }

        public String getClassName() {
            return "FOM_Request";
        }

        public Object jsFunction_get(String name) {
            return request.get(name);
        }

        public Object jsFunction_getAttribute(String name) {
            return request.getAttribute(name);
        }

        public String jsFunction_getRemoteUser() {
            return request.getRemoteUser();
        }


        public void jsFunction_removeAttribute(String name) {
            request.removeAttribute(name);
        }

        public void jsFunction_setAttribute(String name,
                                            Object value) {
            request.setAttribute(name, unwrap(value));
        }

        public Object get(String name, Scriptable start) {
            Object result = super.get(name, start);
            if (result == NOT_FOUND && request != null) {
                result = request.getParameter(name);
                if (result == null) {
                    result = NOT_FOUND;
                }
            }
            return result;
        }

        public Object[] getIds() {
            if (request != null) {
                List list = new LinkedList();
                Enumeration e = request.getAttributeNames();
                while (e.hasMoreElements()) {
                    list.add(e.nextElement());
                }
                Object[] result = new Object[list.size()];
                list.toArray(result);
                return result;
            }
            return super.getIds();
        }

        public String jsFunction_getCharacterEncoding() {
            return request.getCharacterEncoding();
        }

        public void jsFunction_setCharacterEncoding(String value)
            throws Exception {
            request.setCharacterEncoding(value);
        }

        public int jsFunction_getContentLength() {
            return request.getContentLength();
        }

        public String jsFunction_getContentType() {
            return request.getContentType();
        }

        public String jsFunction_getParameter(String name) {
            return request.getParameter(name);
        }

        public Object jsFunction_getParameterValues(String name) {
            return request.getParameterValues(name);
        }

        public Object jsFunction_getParameterNames() {
            return request.getParameterNames();
        }

        public String jsFunction_getAuthType() {
            return request.getAuthType();
        }

        public String jsFunction_getProtocol() {
            return request.getProtocol();
        }

        public String jsFunction_getServerName() {
            return request.getServerName();
        }

        public String jsFunction_getRemoteAddr() {
            return request.getRemoteAddr();
        }

        public String jsFunction_getRemoteHost() {
            return request.getRemoteHost();
        }

        public int jsFunction_getServerPort() {
            return request.getServerPort();
        }

        public String jsFunction_getScheme() {
            return request.getScheme();
        }

        public String jsFunction_getMethod() {
            return request.getMethod();
        }

        public boolean jsFunction_isSecure() {
            return request.isSecure();
        }

        public Locale jsFunction_getLocale() {
            return request.getLocale();
        }

        public Enumeration jsFunction_getLocales() {
            return request.getLocales();
        }

        public FOM_Cookie[] jsFunction_getCookies() {
            Cookie[] cookies = request.getCookies();
            FOM_Cookie[] FOM_cookies = new FOM_Cookie[cookies!=null ? cookies.length : 0];
            for (int i = 0 ; i < FOM_cookies.length ; ++i) {
                FOM_Cookie FOM_cookie = new FOM_Cookie(cookies[i]);
                FOM_cookie.setParentScope(getParentScope());
                FOM_cookie.setPrototype(getClassPrototype(this, FOM_cookie.getClassName()));
                FOM_cookies[i] = FOM_cookie;
            }
            return FOM_cookies;
        }

        public Scriptable jsGet_cookies() {
            return org.mozilla.javascript.Context.getCurrentContext().newArray(getParentScope(), jsFunction_getCookies());
        }

        public FOM_Cookie jsFunction_getCookie(String name) {
            Object     cookie  = request.getCookieMap().get(name);
            FOM_Cookie fcookie = null;
            if ( cookie!=null ) {
                fcookie = new FOM_Cookie(cookie);
                fcookie.setParentScope(getParentScope());
                fcookie.setPrototype(getClassPrototype(this, fcookie.getClassName()));
            }
            return fcookie;
        }

        public String jsFunction_getHeader(String name) {
            return request.getHeader(name);
        }

        // TODO: FOM_Header

        public Enumeration jsFunction_getHeaders(String name) {
            return request.getHeaders(name);
        }

        public Enumeration jsFunction_getHeaderNames() {
            return request.getHeaderNames();
        }

        public Principal jsFunction_getUserPrincipal() {
            return request.getUserPrincipal();
        }

        public boolean jsFunction_isUserInRole(String role) {
            return request.isUserInRole(role);
        }

        // Request interface

        public Object get(String name) {
            return request.get(name);
        }

        public Object getAttribute(String name) {
            return request.getAttribute(name);
        }

        public Enumeration getAttributeNames() {
            return request.getAttributeNames();
        }

        public void setAttribute(String name, Object o) {
            request.setAttribute(name, o);
        }

        public void removeAttribute(String name) {
            request.removeAttribute(name);
        }

        public String getAuthType() {
            return request.getAuthType();
        }

        public String getCharacterEncoding() {
            return request.getCharacterEncoding();
        }

        public void setCharacterEncoding(String enc)
            throws java.io.UnsupportedEncodingException {
            request.setCharacterEncoding(enc);
        }

        public int getContentLength() {
            return request.getContentLength();
        }

        public String getContentType() {
            return request.getContentType();
        }

        public String getParameter(String name) {
            return request.getParameter(name);
        }

        public Enumeration getParameterNames() {
            return request.getParameterNames();
        }

        public String[] getParameterValues(String name) {
            return request.getParameterValues(name);
        }

        public String getProtocol() {
            return request.getProtocol();
        }

        public String getScheme() {
            return request.getScheme();
        }

        public String getServerName() {
            return request.getServerName();
        }

        public int getServerPort() {
            return request.getServerPort();
        }

        public String getRemoteAddr() {
            return request.getRemoteAddr();
        }

        public String getRemoteHost() {
            return request.getRemoteHost();
        }

        public Locale getLocale() {
            return request.getLocale();
        }

        public Enumeration getLocales() {
            return request.getLocales();
        }

        public boolean isSecure() {
            return request.isSecure();
        }

        public Cookie[] getCookies() {
            return request.getCookies();
        }

        public Map getCookieMap() {
            return request.getCookieMap();
        }

        public long getDateHeader(String name) {
            return request.getDateHeader(name);
        }

        public String getHeader(String name) {
            return request.getHeader(name);
        }

        public Enumeration getHeaders(String name) {
            return request.getHeaders(name);
        }

        public Enumeration getHeaderNames() {
            return request.getHeaderNames();
        }

        public String getMethod() {
            return request.getMethod();
        }

        public String getPathInfo() {
            return request.getPathInfo();
        }

        public String getPathTranslated() {
            return request.getPathTranslated();
        }

        public String getContextPath() {
            return request.getContextPath();
        }

        public String getQueryString() {
            return request.getQueryString();
        }

        public String getRemoteUser() {
            return request.getRemoteUser();
        }

        public Principal getUserPrincipal() {
            return request.getUserPrincipal();
        }

        public boolean isUserInRole(String role) {
            return request.isUserInRole(role);
        }

        public String getRequestedSessionId() {
            return request.getRequestedSessionId();
        }

        public String getRequestURI() {
            return request.getRequestURI();
        }

        public String getSitemapURI() {
            return request.getSitemapURI();
        }

        public String getServletPath() {
            return request.getServletPath();
        }

        public Session getSession(boolean create) {
            return request.getSession(create);
        }

        public Session getSession() {
            return request.getSession();
        }

        public boolean isRequestedSessionIdValid() {
            return request.isRequestedSessionIdValid();
        }

        public boolean isRequestedSessionIdFromCookie() {
            return request.isRequestedSessionIdFromCookie();
        }

        public boolean isRequestedSessionIdFromURL() {
            return request.isRequestedSessionIdFromURL();
        }

    }

    public static class FOM_Cookie
        extends ScriptableObject implements Cookie {

        Cookie cookie;

        public FOM_Cookie() {
            // prototype ctor
        }

        public FOM_Cookie(Object cookie) {
            this.cookie = (Cookie)unwrap(cookie);
        }

        public String getClassName() {
            return "FOM_Cookie";
        }

        public String jsGet_name() {
            return cookie.getName();
        }

        public int jsGet_version() {
            return cookie.getVersion();
        }

        public void jsSet_version(int value) {
            cookie.setVersion(value);
        }

        public String jsGet_value() {
            return cookie.getValue();
        }

        public void jsSet_value(String value) {
            cookie.setValue(value);
        }

        public void jsSet_comment(String purpose) {
            cookie.setComment(purpose);
        }

        public String jsGet_comment() {
            return cookie.getComment();
        }

        public void jsSet_domain(String pattern) {
            cookie.setDomain(pattern);
        }

        public String jsGet_domain() {
            return cookie.getDomain();
        }

        public void jsSet_maxAge(int value) {
            cookie.setMaxAge(value);
        }

        public int jsGet_maxAge() {
            return cookie.getMaxAge();
        }

        public void jsSet_path(String value) {
            cookie.setPath(value);
        }

        public String jsGet_path() {
            return cookie.getPath();
        }

        public void jsSet_secure(boolean value) {
            cookie.setSecure(value);
        }

        public boolean jsGet_secure() {
            return cookie.getSecure();
        }

        // Cookie interface

        public void setComment(String purpose) {
            cookie.setComment(purpose);
        }

        public String getComment() {
            return cookie.getComment();
        }

        public void setDomain(String pattern) {
            cookie.setDomain(pattern);
        }

        public String getDomain() {
            return cookie.getDomain();
        }

        public void setMaxAge(int expiry) {
            cookie.setMaxAge(expiry);
        }

        public int getMaxAge() {
            return cookie.getMaxAge();
        }

        public void setPath(String uri) {
            cookie.setPath(uri);
        }

        public String getPath() {
            return cookie.getPath();
        }

        public void setSecure(boolean flag) {
            cookie.setSecure(flag);
        }

        public boolean getSecure() {
            return cookie.getSecure();
        }

        public String getName() {
            return cookie.getName();
        }

        public void setValue(String newValue) {
            cookie.setValue(newValue);
        }

        public String getValue() {
            return cookie.getValue();
        }

        public int getVersion() {
            return cookie.getVersion();
        }

        public void setVersion(int v) {
            cookie.setVersion(v);
        }
    }

    public static class FOM_Response
        extends ScriptableObject implements Response {

        Response response;

        public FOM_Response() {
            // prototype ctor
        }

        public FOM_Response(Object response) {
            this.response = (Response)unwrap(response);
        }

        public String getClassName() {
            return "FOM_Response";
        }

        public Object jsFunction_createCookie(String name, String value) {
            FOM_Cookie result =
                new FOM_Cookie(response.createCookie(name, value));
            result.setParentScope(getParentScope());
            result.setPrototype(getClassPrototype(this, result.getClassName()));
            return result;
        }

        public void jsFunction_addCookie(Object cookie)
            throws JavaScriptException {
            if (!(cookie instanceof FOM_Cookie)) {
                throw new JavaScriptException("expected a Cookie instead of " + cookie);
            }
            FOM_Cookie fom_cookie = (FOM_Cookie)cookie;
            response.addCookie(fom_cookie.cookie);
        }

        public boolean jsFunction_containsHeader(String name) {
            return response.containsHeader(name);
        }

        public void jsFunction_setHeader(String name, String value) {
            response.setHeader(name, value);
        }

        public void jsFunction_addHeader(String name, String value) {
            response.addHeader(name, value);
        }

        public void jsFunction_setStatus(int sc) {
            if (response instanceof HttpResponse) {
                ((HttpResponse) response).setStatus(sc);
            }
        }

        // Response interface

        public String getCharacterEncoding() {
            return response.getCharacterEncoding();
        }

        public void setLocale(Locale loc) {
            response.setLocale(loc);
        }

        public Locale getLocale() {
            return response.getLocale();
        }
        public Cookie createCookie(String name, String value) {
            return response.createCookie(name, value);
        }

        public void addCookie(Cookie cookie) {
            response.addCookie(cookie);
        }

        public boolean containsHeader(String name) {
            return response.containsHeader(name);
        }

        public String encodeURL(String url) {
            return response.encodeURL(url);
        }

        public void setDateHeader(String name, long date) {
            response.setDateHeader(name, date);
        }

        public void addDateHeader(String name, long date) {
            response.addDateHeader(name, date);
        }

        public void setHeader(String name, String value) {
            response.setHeader(name, value);
        }

        public void addHeader(String name, String value) {
            response.addHeader(name, value);
        }

        public void setIntHeader(String name, int value) {
            response.setIntHeader(name, value);
        }

        public void addIntHeader(String name, int value) {
            response.addIntHeader(name, value);
        }
    }

    public static class FOM_Session
        extends ScriptableObject implements Session {

        Session session;

        public FOM_Session() {
            // prototype ctor
        }

        public FOM_Session(Object session) {
            this.session = (Session)unwrap(session);
        }

        public String getClassName() {
            return "FOM_Session";
        }

        public Object[] getIds() {
            if (session != null) {
                List list = new LinkedList();
                Enumeration e = session.getAttributeNames();
                while (e.hasMoreElements()) {
                    list.add(e.nextElement());
                }
                Object[] result = new Object[list.size()];
                list.toArray(result);
                return result;
            }
            return super.getIds();
        }

        public Object get(String name, Scriptable start) {
            Object result = super.get(name, start);
            if (result == NOT_FOUND && session != null) {
                result = session.getAttribute(name);
                if (result == null) {
                    result = NOT_FOUND;
                }
            }
            return result;
        }

        public Object jsFunction_getAttribute(String name) {
            return session.getAttribute(name);
        }

        public void jsFunction_setAttribute(String name, Object value) {
            session.setAttribute(name, unwrap(value));
        }

        public void jsFunction_removeAttribute(String name) {
            session.removeAttribute(name);
        }

        public Object jsFunction_getAttributeNames() {
            return session.getAttributeNames();
        }

        public void jsFunction_invalidate() {
            session.invalidate();
        }

        public boolean jsFunction_isNew() {
            return session.isNew();
        }

        public String jsFunction_getId() {
            return session.getId();
        }

        public long jsFunction_getCreationTime() {
            return session.getCreationTime();
        }

        public long jsFunction_getLastAccessedTime() {
            return session.getLastAccessedTime();
        }

        public void jsFunction_setMaxInactiveInterval(int interval) {
            session.setMaxInactiveInterval(interval);
        }

        public int jsFunction_getMaxInactiveInterval() {
            return session.getMaxInactiveInterval();
        }


        // Session interface

        public long getCreationTime() {
            return session.getCreationTime();
        }

        public String getId() {
            return session.getId();
        }

        public long getLastAccessedTime() {
            return session.getLastAccessedTime();
        }

        public void setMaxInactiveInterval(int interval) {
            session.setMaxInactiveInterval(interval);
        }

        public int getMaxInactiveInterval() {
            return session.getMaxInactiveInterval();
        }

        public Object getAttribute(String name) {
            return session.getAttribute(name);
        }

        public Enumeration getAttributeNames() {
            return session.getAttributeNames();
        }

        public void setAttribute(String name, Object value) {
            session.setAttribute(name, value);
        }

        public void removeAttribute(String name) {
            session.removeAttribute(name);
        }

        public void invalidate() {
            session.invalidate();
        }

        public boolean isNew() {
            return session.isNew();
        }
    }

    public static class FOM_Context extends ScriptableObject
        implements org.apache.cocoon.environment.Context {

        org.apache.cocoon.environment.Context context;

        public FOM_Context() {
            // prototype ctor
        }

        public FOM_Context(Object context) {
            this.context = (org.apache.cocoon.environment.Context)unwrap(context);
        }

        public String getClassName() {
            return "FOM_Context";
        }

        public Object jsFunction_getAttribute(String name) {
            return context.getAttribute(name);
        }

        public void jsFunction_setAttribute(String name, Object value) {
            context.setAttribute(name, unwrap(value));
        }

        public void jsFunction_removeAttribute(String name) {
            context.removeAttribute(name);
        }

        public Object jsFunction_getAttributeNames() {
            return context.getAttributeNames();
        }

        public Object jsFunction_getInitParameter(String name) {
            return context.getInitParameter(name);
        }

        public Object[] getIds() {
            if (context != null) {
                List list = new LinkedList();
                Enumeration e = context.getAttributeNames();
                while (e.hasMoreElements()) {
                    list.add(e.nextElement());
                }
                Object[] result = new Object[list.size()];
                list.toArray(result);
                return result;
            }
            return super.getIds();
        }

        public Object get(String name, Scriptable start) {
            Object value = super.get(name, start);
            if (value == NOT_FOUND && context != null) {
                value = context.getAttribute(name);
                if (value == null) {
                    value = NOT_FOUND;
                }
            }
            return value;
        }

        /* TODO: Vote on the inclusion of this method
        public String jsFunction_getRealPath(String path) {
        	return context.getRealPath(path);
        }
        */

        // Context interface

        public Object getAttribute(String name) {
            return context.getAttribute(name);
        }

        public void setAttribute(String name, Object value) {
            context.setAttribute(name, value);
        }

        public void removeAttribute(String name) {
            context.removeAttribute(name);
        }

        public Enumeration getAttributeNames() {
            return context.getAttributeNames();
        }

        public URL getResource(String path)
            throws MalformedURLException {
            return context.getResource(path);
        }

        public String getRealPath(String path) {
            return context.getRealPath(path);
        }

        public String getMimeType(String file) {
            return context.getMimeType(file);
        }

        public String getInitParameter(String name) {
            return context.getInitParameter(name);
        }

        public InputStream getResourceAsStream(String path) {
            return context.getResourceAsStream(path);
        }

    }

    public static class FOM_Log extends ScriptableObject {

        private Logger logger;

        public FOM_Log() {
        }

        public FOM_Log(Object logger) {
            this.logger = (Logger)unwrap(logger);
        }

        public String getClassName() {
            return "FOM_Log";
        }

        public void jsFunction_debug(String message) {
            logger.debug(message);
        }

        public void jsFunction_info(String message) {
            logger.info(message);
        }

        public void jsFunction_warn(String message) {
            logger.warn(message);
        }

        public void jsFunction_error(String message) {
            logger.error(message);
        }

        public boolean jsFunction_isDebugEnabled() {
            return logger.isDebugEnabled();
        }

        public boolean jsFunction_isInfoEnabled() {
            return logger.isInfoEnabled();
        }

        public boolean jsFunction_isWarnEnabled() {
            return logger.isWarnEnabled();
        }

        public boolean jsFunction_isErrorEnabled() {
            return logger.isErrorEnabled();
        }
    }

    public FOM_Request jsGet_request() {
        return currentCall.getRequest();
    }

    public FOM_Response jsGet_response() {
        return currentCall.getResponse();
    }

    public FOM_Log jsGet_log() {
        return currentCall.getLog();
    }

    public FOM_Context jsGet_context() {
        return currentCall.getContext();
    }

    public FOM_Session jsGet_session() {
        return currentCall.getSession();
    }

    /**
     * Get Sitemap parameters
     *
     * @return a <code>Scriptable</code> value whose properties represent
     * the Sitemap parameters from <map:call>
     */
    public Scriptable jsGet_parameters() {
        return getParameters();
    }

    public Scriptable getParameters() {
        return currentCall.getParameters();
    }

    void setParameters(Scriptable value) {
        currentCall.setParameters(value);
    }

    // unwrap Wrapper's and convert undefined to null
    private static Object unwrap(Object obj) {
        if (obj instanceof Wrapper) {
            obj = ((Wrapper)obj).unwrap();
        } else if (obj == Undefined.instance) {
            obj = null;
        }
        return obj;
    }

    // Make everything available to JavaScript objects implemented in Java:

    /**
     * Get the current request
     * @return The request
     */
    public Request getRequest() {
        FOM_Request fom_request = jsGet_request();
        return fom_request != null ? fom_request.request : null;
    }

    /**
     * Get the current session
     * @return The session (may be null)
     */
    public Session getSession() {
        if (getRequest().getSession(false) == null) {
            return null;
        }
        return jsGet_session().session;
    }

    /**
     * Get the current response
     * @return The response
     */
    public Response getResponse() {
        return jsGet_response().response;
    }

    /**
     * Get the current context
     * @return The context
     */
    public org.apache.cocoon.environment.Context getContext() {
        return jsGet_context().context;
    }

    /**
     * Get the current object model
     * @return The object model
     */
    public Map getObjectModel() {
        return getEnvironment().getObjectModel();
    }

    /**
     * Get the current Sitemap's component manager
     * @return The component manager
     */

    public ComponentManager getComponentManager() {
        return currentCall.componentManager;
    }


    private Environment getEnvironment() {
        return currentCall.environment;
    }

    private Context getAvalonContext() {
        return currentCall.avalonContext;
    }

    private Logger getLogger() {
        return currentCall.logger;
    }

    private ServiceManager getServiceManager() {
        return currentCall.serviceManager;
    }

    private FOM_JavaScriptInterpreter getInterpreter() {
        return currentCall.interpreter;
    }

    /**
     * Call the Cocoon Sitemap to process a page
     * @param uri Uri to match
     * @param bean Input to page
     * @param fom_wk Current Web continuation (may be null)
     */

    public void forwardTo(String uri,
                          Object bean,
                          FOM_WebContinuation fom_wk)
        throws Exception {
        getInterpreter().forwardTo(getTopLevelScope(this),
                                   this,
                                   uri,
                                   bean,
                                   fom_wk,
                                   getEnvironment());
    }

    /**
     * Perform the behavior of <map:call continuation="blah">
     * This can be used in cases where the continuation id is not encoded
     * in the request in a form convenient to access in the sitemap.
     * Your script can extract the id from the request and then call
     * this method to process it as normal.
     * @param kontId The continuation id
     * @param parameters Any parameters you want to pass to the continuation (may be null)
     */
    public void handleContinuation(String kontId, Scriptable parameters)
        throws Exception {
        List list = null;
        if (parameters == null || parameters == Undefined.instance) {
            parameters = getParameters();
        }
        Object[] ids = parameters.getIds();
        list = new ArrayList();
        for (int i = 0; i < ids.length; i++) {
            String name = ids[i].toString();
            Argument arg = new Argument(name,
                                        org.mozilla.javascript.Context.toString(getProperty(parameters, name)));
            list.add(arg);
        }
        getInterpreter().handleContinuation(kontId, list, getEnvironment());
    }

    /**
     * Create a Bookmark WebContinuation from a JS Continuation with the last
     * continuation of sendPageAndWait as its parent.
     * PageLocal variables will be shared with the continuation of
     * the next call to sendPageAndWait().
     * @param k The JS continuation
     * @param ttl Lifetime for this continuation (zero means no limit)
     */
    public FOM_WebContinuation jsFunction_makeWebContinuation(Object k,
                                                              Object ttl)
        throws Exception {
        double d = org.mozilla.javascript.Context.toNumber(ttl);
        FOM_WebContinuation result =
            makeWebContinuation((Continuation)unwrap(k),
                                jsGet_continuation(),
                                (int)d);
        result.setPageLocal(pageLocal.getDelegate());
        currentCall.setLastContinuation(result);
        return result;
    }

    /**
     * Create a Web Continuation from a JS Continuation
     * @param k The JS continuation (may be null - null will be returned in that case)
     * @param parent The parent of this continuation (may be null)
     * @param timeToLive Lifetime for this continuation (zero means no limit)
     */
    public FOM_WebContinuation makeWebContinuation(Continuation k,
                                                   FOM_WebContinuation parent,
                                                   int timeToLive)
        throws Exception {
        if (k == null) {
            return null;
        }
        WebContinuation wk;
        ContinuationsManager contMgr;
        contMgr = (ContinuationsManager)
            getComponentManager().lookup(ContinuationsManager.ROLE);
        wk = contMgr.createWebContinuation(unwrap(k),
                                           (parent == null ? null : parent.getWebContinuation()),
                                           timeToLive,
                                           null);
        FOM_WebContinuation result = new FOM_WebContinuation(wk);
        result.setParentScope(getParentScope());
        result.setPrototype(getClassPrototype(getParentScope(),
                                              result.getClassName()));
        return result;
    }
}
