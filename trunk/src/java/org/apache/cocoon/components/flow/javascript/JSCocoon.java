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

*/
package org.apache.cocoon.components.flow.javascript;

import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;

import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.JavaScriptException;

import org.apache.cocoon.sitemap.SitemapRedirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.components.modules.output.OutputModule;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.components.cprocessor.sitemap.PipelinesNode;
import org.apache.cocoon.components.flow.ContinuationsManager;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.acting.Action;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;

/**
 * JavaScript interface to various Cocoon abstractions.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @since March 16, 2002
 * @version CVS $Id: JSCocoon.java,v 1.16 2004/01/10 14:38:19 cziegeler Exp $
 */
public class JSCocoon extends ScriptableObject
{
    protected static String OBJECT_SOURCE_RESOLVER = "source-resolver";
    protected JavaScriptInterpreter interpreter;
    protected NativeArray parameters;
    protected Environment environment;
    protected ServiceManager manager;

    public JSCocoon() {}

    public String getClassName()
    {
        return "Cocoon";
    }


    public void setParameters(NativeArray parameters)
    {
        this.parameters = parameters;
    }

    public void setInterpreter(JavaScriptInterpreter interpreter)
    {
        this.interpreter = interpreter;
    }

    public void setContext(ServiceManager manager, Environment environment)
    {
        this.manager = manager;
        this.environment = environment;
    }

    public void invalidateContext()
    {
        manager = null;
        environment = null;
    }

    public NativeArray jsGet_parameters()
    {
        return parameters;
    }

    public JavaScriptInterpreter jsGet_interpreter()
    {
        return interpreter;
    }

    public Environment jsGet_environment()
    {
        return environment;
    }

    public Request jsGet_request()
    {
        if (environment == null) {
            // context has been invalidated
            return null;
        }
        Map objectModel = environment.getObjectModel();
        return ObjectModelHelper.getRequest(objectModel);
    }

    public Response jsGet_response()
    {
        if (environment == null) {
            // context has been invalidated
            return null;
        }
        Map objectModel = environment.getObjectModel();
        return ObjectModelHelper.getResponse(objectModel);
    }

    public Session jsGet_session()
    {
        if (environment == null) {
            // context has been invalidated
            return null;
        }
        return jsGet_request().getSession();
    }

    public Context jsGet_context()
    {
        if (environment == null) {
            // context has been invalidated
            return null;
        }
        Map objectModel = environment.getObjectModel();
        return ObjectModelHelper.getContext(objectModel);
    }

    public ServiceManager jsGet_componentManager()
    {
        return manager;
    }

    /**
     * Load the script file specified as argument.
     *
     * @param filename a <code>String</code> value
     * @return an <code>Object</code> value
     * @exception JavaScriptException if an error occurs
     */
    public Object jsFunction_load(String filename) throws JavaScriptException
    {
        org.mozilla.javascript.Context cx =
            org.mozilla.javascript.Context.getCurrentContext();
        try {
            Scriptable scope = getParentScope();
            Script script = interpreter.compileScript(cx, environment, filename);
            return script.exec(cx, scope);
        } catch (JavaScriptException e) {
            throw e;
        } catch (Exception e) {
            throw new JavaScriptException(e);
        }
    }

    public String jsFunction_toString()
    {
        return "[object " + toString() + "]";
    }

    public void jsFunction_forwardTo(String uri, Object bizData, Object cont)
        throws JavaScriptException
    {
        try {
            bizData = jsobjectToObject(bizData);

            WebContinuation kont = null;

            if (cont != null) {
                kont = ((JSWebContinuation)cont).getWebContinuation();
            }

            interpreter.forwardTo(uri, bizData, kont, environment);
        } catch (JavaScriptException e) {
            throw e;
        } catch (Exception e) {
            throw new JavaScriptException(e);
        }
    }

    /**
     * Call the Cocoon sitemap for the given URI, sending the output of the
     * eventually matched pipeline to the specified outputstream.
     *
     * @param uri The URI for which the request should be generated.
     * @param biz Extra data associated with the subrequest.
     * @param out An OutputStream where the output should be written to.
     * @return Whatever the Cocoon processor returns (????).
     */
    public boolean jsFunction_process(String uri, Object biz, Object out)
        throws JavaScriptException
    {
        try {
            out = jsobjectToObject(out);
            biz = jsobjectToObject(biz);
            return interpreter.process(uri, biz, (OutputStream)out, environment);
        } catch (JavaScriptException e) {
            throw e;
        } catch (Exception e) {
            throw new JavaScriptException(e);
        }
    }

    /**
     * Send a client-side redirect to the browser.
     *
     * @param uri The URI to which the browser should be redirected to.
     */
    public void jsFunction_redirect(String uri)
        throws JavaScriptException
    {
        try {
            // Cannot use environment directly as TreeProcessor uses own version of redirector
            // environment.redirect(false, uri);
            PipelinesNode.getRedirector(environment).redirect(false, uri);
        } catch (Exception e) {
            throw new JavaScriptException(e);
        }
    }

    /**
     * Set the Scope object in the session object of the current
     * user. This effectively means that at the next invocation from the
     * sitemap of a JavaScript function (using the &lt;map:call
     * function="..."&gt;), will obtain the same scope as the current
     * one.
     */
    public void jsFunction_createSession()
    {
        interpreter.setSessionScope(environment, getParentScope());
    }

    /**
     * Remove the Scope object from the session object of the current
     * user.
     */
    public void jsFunction_removeSession()
    {
        interpreter.removeSessionScope(environment);
    }

    public void jsFunction_diplayAllContinuations()
        throws ServiceException
    {
        ContinuationsManager continuationsMgr
            = (ContinuationsManager)manager.lookup(ContinuationsManager.ROLE);

        try {
            continuationsMgr.displayAllContinuations();
        } finally {
            manager.release(continuationsMgr);
        }
    }

    // All right, this breaks the encapsulation, but I couldn't find any
    // better way to obtain the ComponentManager for a
    // JSWebContinuation.
    ServiceManager getComponentManager()
    {
        return manager;
    }


    public static Map jsobjectToMap(Scriptable jsobject)
    {
        HashMap hash = new HashMap();
        Object[] ids = jsobject.getIds();
        for (int i = 0; i < ids.length; i++) {
            String key = ScriptRuntime.toString(ids[i]);
            Object value = jsobject.get(key, jsobject);
            if (value == Undefined.instance)
                value = null;
            else
                value = jsobjectToObject(value);
            hash.put(key, value);
        }
        return hash;
    }

    public static Object jsobjectToObject(Object obj)
    {
        // unwrap Scriptable wrappers of real Java objects
        if (obj instanceof Wrapper) {
            obj = ((Wrapper) obj).unwrap();
        } else if (obj == Undefined.instance) {
            obj = null;
        }
        return obj;
    }

    public Scriptable jsFunction_callAction(String type,
                                            String source,
                                            Scriptable parameters)
        throws Exception
    {
        Redirector redirector = new SitemapRedirector(this.environment);
        SourceResolver resolver = (SourceResolver)this.environment.getObjectModel()
            .get(OBJECT_SOURCE_RESOLVER);
        ServiceManager sitemapManager = EnvironmentHelper.getSitemapServiceManager();
        ServiceSelector actionSelector
            = (ServiceSelector)sitemapManager.lookup(Action.ROLE + "Selector");
        Action action = (Action)actionSelector.select(type);
        Map result = null;
        try {
            result = action.act(redirector,
                                resolver,
                                this.environment.getObjectModel(),
                                source,
                                jsobjectToParameters(parameters));
        } finally {
            actionSelector.release(action);
        }

        // what should be done with the redirector ??
        // ignore it or call sendPage with it?
        return (result!=null? new ScriptableMap(result) : null);
    }

    public static Parameters jsobjectToParameters(Scriptable jsobject)
    {
        Parameters params = new Parameters();
        Object[] ids = jsobject.getIds();
        for (int i = 0; i < ids.length; i++) {
            String key = ScriptRuntime.toString(ids[i]);
            Object value = jsobject.get(key, jsobject);
            if (value == Undefined.instance) {
                value = null;
            } else {
                value = ScriptRuntime.toString(value);
            }
            params.setParameter(key, (String) value);
        }
        return params;
    }

    public Object jsFunction_inputModuleGetAttribute(String type, String attribute)
        throws Exception
    {
        // since no new components can be declared on sitemap we could
        // very well use the 'other' one here. Anyway, since it's there...
        ServiceManager sitemapManager = EnvironmentHelper.getSitemapServiceManager();
        ServiceSelector inputSelector = (ServiceSelector)sitemapManager
            .lookup(InputModule.ROLE + "Selector");
        InputModule input = (InputModule) inputSelector.select(type);
        Object result = null;
        try {
            result = input.getAttribute(attribute, null,
                                        this.environment.getObjectModel());
        } finally {
            inputSelector.release(input);
        }
        return result;
    }

    public void jsFunction_outputModuleSetAttribute(String type, String attribute,
                                                    Object value)
        throws Exception
    {
        // since no new components can be declared on sitemap we could
        // very well use the 'other' one here. Anyway, since it's there...
        ServiceManager sitemapManager = EnvironmentHelper.getSitemapServiceManager();
        ServiceSelector outputSelector = (ServiceSelector)sitemapManager
            .lookup(OutputModule.ROLE + "Selector");
        OutputModule output = (OutputModule) outputSelector.select(type);
        try {
            output.setAttribute(null, this.environment.getObjectModel(), attribute,
                                jsobjectToObject(value));
        }
        finally {
            outputSelector.release(output);
        }
    }

    public void jsFunction_outputModuleCommit(String type)
        throws Exception
    {
        // since no new components can be declared on sitemap we could
        // very well use the 'other' one here. Anyway, since it's there...
        ServiceManager sitemapManager = EnvironmentHelper.getSitemapServiceManager();
        ServiceSelector outputSelector = (ServiceSelector)sitemapManager
            .lookup(OutputModule.ROLE + "Selector");
        OutputModule output = (OutputModule) outputSelector.select(type);
        try {
            output.commit(null, this.environment.getObjectModel());
        } finally {
            outputSelector.release(output);
        }
    }

    public void jsFunction_outputModuleRollback(String type)
        throws Exception
    {
        // since no new components can be declared on sitemap we could
        // very well use the 'other' one here. Anyway, since it's there...
        ServiceManager sitemapManager = EnvironmentHelper.getSitemapServiceManager();
        ServiceSelector outputSelector = (ServiceSelector)sitemapManager
            .lookup(OutputModule.ROLE + "Selector");
        OutputModule output = (OutputModule) outputSelector.select(type);
        try {
            output.rollback(null, this.environment.getObjectModel(), null);
        } finally {
            outputSelector.release(output);
        }
    }
}
