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
package org.apache.cocoon.components.flow.javascript.fom;

import java.io.OutputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.ContinuationsManager;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.flow.Interpreter.Argument;
import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.Session;
import org.mozilla.javascript.JavaScriptException;
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
 * @author <a href="mailto:reinhard.at.apache.org">Reinhard Pötz</a>
 * @version CVS $Id: AO_FOM_Cocoon.java,v 1.6 2004/03/05 10:07:25 bdelacretaz Exp $
 */

public class AO_FOM_Cocoon extends ScriptableObject {

    private AO_FOM_JavaScriptInterpreter interpreter;

    private Redirector redirector;
    private ServiceManager componentManager;
    private Logger logger;
    private Context avalonContext;

    private FOM_Request request;
    private FOM_Response response;
    private FOM_Session session;
    private FOM_Context context;
    private Scriptable parameters;
    private FOM_Log log;

    private WebContinuation lastContinuation;

    public String getClassName() {
        return "AO_FOM_Cocoon";
    }

    // Called by FOM_JavaScriptInterpreter
    static void init(Scriptable scope) throws Exception {
        defineClass(scope, AO_FOM_Cocoon.class);
        defineClass(scope, FOM_Request.class);
        defineClass(scope, FOM_Response.class);
        defineClass(scope, FOM_Cookie.class);
        defineClass(scope, FOM_Session.class);
        defineClass(scope, FOM_Context.class);
        defineClass(scope, FOM_Log.class);
        defineClass(scope, FOM_WebContinuation.class);
    }

    void setup(AO_FOM_JavaScriptInterpreter interp,
                      Redirector redirector, 
                      Context avalonContext,
                      ServiceManager manager,
                      Logger logger) {
        this.interpreter = interp;
        this.redirector = redirector;
        this.avalonContext = avalonContext;
        this.componentManager = manager;
        this.logger = logger;
    }

    void invalidate() {
        this.request = null;
        this.response = null;
        this.session = null;
        this.context = null;
        this.log = null;
        this.logger = null;
        this.componentManager = null;
        this.redirector = null;
        this.interpreter = null;
    }


    private FOM_WebContinuation forwardTo(String uri, Object bizData,
                                          Continuation continuation) 
        throws Exception {
        WebContinuation wk = null;
        if (continuation != null) {
            ContinuationsManager contMgr = (ContinuationsManager)
                componentManager.lookup(ContinuationsManager.ROLE);
            wk = lastContinuation = 
                contMgr.createWebContinuation(continuation,
                                              lastContinuation,
                                              0,
                                              null);
        }
        
        String redUri = uri;
        
        FOM_WebContinuation fom_wk =
            new FOM_WebContinuation(wk);
        fom_wk.setParentScope(getParentScope());
        fom_wk.setPrototype(getClassPrototype(getParentScope(), 
                                              fom_wk.getClassName()));
        interpreter.forwardTo(getParentScope(), this, redUri,
                              bizData, fom_wk, redirector);

        FOM_WebContinuation result = null;
        if (wk != null) {
            result = new FOM_WebContinuation(wk);
            result.setParentScope(getParentScope());
            result.setPrototype(getClassPrototype(getParentScope(),
                                                  result.getClassName()));
        }
        return result;
    }

    public FOM_WebContinuation jsFunction_sendPage(String uri, 
                                                   Object obj, 
                                                   Object continuation) 
        throws Exception {
        return forwardTo(uri, obj, (Continuation)unwrap(continuation)); 
    }
                                    

    public void jsFunction_processPipelineTo(String uri,
                                             Object map,
                                             Object outputStream) 
        throws Exception {
        if (!(unwrap(outputStream) instanceof OutputStream)) {
            throw new JavaScriptException("expected a java.io.OutputStream instead of " + outputStream);
        }
        interpreter.process(getParentScope(), this, uri, map, 
                            (OutputStream)unwrap(outputStream));
    }

    public void jsFunction_redirectTo(String uri) throws Exception {
        redirector.redirect(false, uri);
    }

    /*
    NOTE (SM): These are the hooks to the future FOM Event Model that will be
    designed in the future. It has been postponed because we think
    there are more important things to do at the moment, but these
    are left here to indicate that they are planned.
 
    public void jsFunction_addEventListener(String eventName, 
                                            Object function) {
    }
    
    public void jsFunction_removeEventListener(String eventName,
                                               Object function) {
    }
    */

    /**
     * Access components.
     * 
     * @param id - role name of the component
     */  
    public Object jsFunction_getComponent(String id) 
        throws Exception { 
        return componentManager.lookup(id);
    }
    
    /**
     * Release pooled components.
     * 
     * @param component - an <code>Object</code> that is an instance 
     * of <code>org.apache.avalon.framework.component.Component</code>
     */
    public void jsFunction_releaseComponent( Object component ) throws Exception {
        this.componentManager.release( unwrap(component) );
    }

    // (RPO) added by interception layer    
    /**
     * Dummy function for apply methods which does nothing - only necessary
     * to make it easy to switch between the interception aware interpreter
     * (intercepted-javascript) and the other (javascript). See cocoon.xconf
     * for details.
     */
    public void jsFunction_apply( String script ) {
        if( logger.isWarnEnabled() ) {
            logger.warn( "In order to apply interceptions to your Javascript functions " + 
                         "you have to use the interceptions aware Javascript interpreter. " +
                         "Check your settings in cocoon.xconf!" );
        }
    }
    // --end

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
        Script script = interpreter.compileScript( cx, filename );
        return script.exec( cx, scope );
    }    
        
    public static class FOM_Request extends ScriptableObject {

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
            FOM_Cookie[] FOM_cookies = new FOM_Cookie[cookies.length];
            for (int i = 0 ; i < cookies.length ; ++i) {
                FOM_cookies[i] = new FOM_Cookie(cookies[i]);
            }
            return FOM_cookies;
        }
        
        public String jsFunction_getHeader(String name) {
            return request.getHeader(name);
        }
        
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
    }

    public static class FOM_Cookie extends ScriptableObject {

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
    }

    public static class FOM_Response extends ScriptableObject {

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
            result.setPrototype(getClassPrototype(this, "FOM_Cookie"));
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
    }

    public static class FOM_Session extends ScriptableObject {

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
            session.setAttribute(name, value);
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
    }

    public static class FOM_Context extends ScriptableObject {

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
        if (request != null) {
            return request;
        }
        if (redirector == null) {
            // context has been invalidated
            return null;
        }
        request = new FOM_Request(ContextHelper.getRequest(avalonContext));
        request.setParentScope(getParentScope());
        request.setPrototype(getClassPrototype(getParentScope(), "FOM_Request"));
        return request;
    }

    public FOM_Response jsGet_response() {
        if (response != null) {
            return response;
        }
        if (redirector == null) {
            // context has been invalidated
            return null;
        }
        response = new FOM_Response(ContextHelper.getResponse(avalonContext));
        response.setParentScope(getParentScope());
        response.setPrototype(getClassPrototype(this, "FOM_Response"));
        return response;
    }

    public FOM_Log jsGet_log() {
        if (log != null) {
            return log;
        }
        log = new FOM_Log(logger);
        log.setParentScope(getParentScope());
        log.setPrototype(getClassPrototype(this, "FOM_Log"));
        return log;
    }

    public FOM_Context jsGet_context() {
        if (context != null) {
            return context;
        }
        Map objectModel = ContextHelper.getObjectModel(avalonContext);
        context = 
            new FOM_Context(ObjectModelHelper.getContext(objectModel));
        context.setParentScope(getParentScope());
        context.setPrototype(getClassPrototype(this, "FOM_Context"));
        return context;
    }

    public FOM_Session jsGet_session() {
        if (session != null) {
            return session;
        }
        if (redirector == null) {
            // session has been invalidated
            return null;
        }
        session = new FOM_Session(ContextHelper.getRequest(avalonContext).getSession(true));
        session.setParentScope(getParentScope());
        session.setPrototype(getClassPrototype(this, "FOM_Session"));
        return session;
    }

    /**
     * Get Sitemap parameters
     *
     * @return a <code>Scriptable</code> value whose properties represent 
     * the Sitemap parameters from <map:call>
     */
    public Scriptable jsGet_parameters() {
        return parameters;
    }

    void setParameters(Scriptable value) {
        parameters = value;
    }

    // unwrap Wrapper's and convert undefined to null
    protected static Object unwrap(Object obj) {
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
        return jsGet_request().request;
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
        return ContextHelper.getObjectModel(avalonContext);
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
        interpreter.forwardTo(getTopLevelScope(this),
                              this, 
                              uri,
                              bean,
                              fom_wk,
                              redirector);
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
            parameters = this.parameters;
        }
        Object[] ids = parameters.getIds();
        list = new ArrayList();
        for (int i = 0; i < ids.length; i++) {
            String name = ids[i].toString();
            Argument arg = new Argument(name,
                                        org.mozilla.javascript.Context.toString(getProperty(parameters, name)));
            list.add(arg);
        }
        interpreter.handleContinuation(kontId, list, redirector);
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
        if (k == null) return null;
        WebContinuation wk;
        ContinuationsManager contMgr;
        contMgr = (ContinuationsManager)
            componentManager.lookup(ContinuationsManager.ROLE);
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
