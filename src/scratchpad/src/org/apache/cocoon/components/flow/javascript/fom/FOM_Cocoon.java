/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-

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

*/
package org.apache.cocoon.components.flow.javascript.fom;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.continuations.Continuation;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.flow.ContinuationsManager;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.logger.Logger;
import java.util.Map;
import java.io.OutputStream;
/**
 * Implementation of FOM (Flow Object Model)
 */

public class FOM_Cocoon extends ScriptableObject {

    private FOM_JavaScriptInterpreter interpreter;

    private Environment environment;
    private ComponentManager componentManager;

    private FOM_Request request;
    private FOM_Response response;
    private FOM_Session session;
    private FOM_Context context;
    private FOM_Log log;

    private WebContinuation lastContinuation;

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
    }

    public void setup(FOM_JavaScriptInterpreter interp,
                      Environment env, 
                      ComponentManager manager) {
        this.interpreter = interp;
        this.environment = env;
        this.componentManager = manager;
    }

    public void invalidate() {
        this.request = null;
        this.response = null;
        this.session = null;
        this.context = null;
        this.componentManager = null;
        this.environment = null;
        this.interpreter = null;
    }

    private void forwardTo(String uri, Object bizData,
                           Continuation continuation) 
        throws Exception {
        WebContinuation wk = null;
        if (continuation != null) {
            ContinuationsManager contMgr = (ContinuationsManager)
                componentManager.lookup(ContinuationsManager.ROLE);
            wk = lastContinuation = 
                contMgr.createWebContinuation(continuation,
                                              lastContinuation,
                                              0);
        }
        interpreter.forwardTo("cocoon://"+
                              environment.getURIPrefix() + uri,
                              bizData, wk, environment);
    }

    public void jsFunction_sendPage(String uri, 
                                    Object obj, 
                                    Object continuation) 
        throws Exception {
        forwardTo(uri, obj, (Continuation)unwrap(continuation)); 
    }
                                    

    public void jsFunction_processPipelineTo(String uri,
                                             Object map,
                                             Object outputStream) 
        throws Exception {
        if (!(unwrap(outputStream) instanceof OutputStream)) {
            throw new JavaScriptException("expected a java.io.OutputStream instead of " + outputStream);
        }
        interpreter.process(uri, map, 
                            (OutputStream)unwrap(outputStream), 
                            environment);
    }

    public void jsFunction_redirectTo(String uri) throws Exception {
        environment.redirect(false, uri);
    }

    public void jsFunction_addEventListener(String eventName, 
                                            Object function) {
        // what is this?
    }
    
    public void jsFunction_removeEventListener(String eventName,
                                               Object function) {
        // what is this?
    }

    public Object jsFunction_getComponent(String id) {
        // what is this?
        return null;
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
                throw new JavaScriptException("expected a Cookie instead of "+cookie);
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
    }

    public static class FOM_Log extends ScriptableObject {

        private Logger logger;

        public FOM_Log() {
        }

        public String getClassName() {
            return "FOM_Log";
        }
        
        public void enableLogging(Logger logger) {
            this.logger = logger;
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
        if (environment == null) {
            // context has been invalidated
            return null;
        }
        Map objectModel = environment.getObjectModel();
        request = 
            new FOM_Request(ObjectModelHelper.getRequest(objectModel));
        request.setParentScope(getParentScope());
        request.setPrototype(getClassPrototype(getParentScope(),
                                               "FOM_Request"));
        return request;
    }

    public FOM_Response jsGet_response() {
        if (response != null) {
            return response;
        }
        if (environment == null) {
            // context has been invalidated
            return null;
        }
        Map objectModel = environment.getObjectModel();
        response = 
            new FOM_Response(ObjectModelHelper.getResponse(objectModel));
        response.setParentScope(getParentScope());
        response.setPrototype(getClassPrototype(this,
                                               "FOM_Response"));
        return response;
    }

    public FOM_Log jsGet_log() {
        if (log != null) {
            return log;
        }
        log = new FOM_Log();
        log.setParentScope(getParentScope());
        log.setPrototype(getClassPrototype(this, "FOM_Log"));
        return log;
    }

    public FOM_Context jsGet_context() {
        if (context != null) {
            return context;
        }
        Map objectModel = environment.getObjectModel();
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
        if (environment == null) {
            // session has been invalidated
            return null;
        }
        Map objectModel = environment.getObjectModel();
        session = 
            new FOM_Session(ObjectModelHelper.getRequest(objectModel).getSession(true));
        session.setParentScope(getParentScope());
        session.setPrototype(getClassPrototype(this,
                                               "FOM_Session"));
        return session;
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

}
