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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.flow.CompilingInterpreter;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.flow.InvalidContinuationException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.excalibur.source.Source;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.PropertyException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.tools.ToolErrorReporter;

/**
 * Interface with the JavaScript interpreter.
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @since March 25, 2002
 * @version CVS $Id: JavaScriptInterpreter.java,v 1.23 2003/12/26 18:43:39 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type=Interpreter
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=javascript-interpreter
 */
public class JavaScriptInterpreter extends CompilingInterpreter
    implements Configurable, Initializable
{

    /**
     * LAST_EXEC_TIME
     * A long value is stored under this key in each top level JavaScript
     * thread scope object. When you enter a context any scripts whose
     * modification time is later than this value will be recompiled and reexecuted,
     * and this value will be updated to the current time.
     */
    private final static String LAST_EXEC_TIME = "__PRIVATE_LAST_EXEC_TIME__";

    /**
     * Key for storing a JavaScript global scope object in the Cocoon session
     */
    public static final String USER_GLOBAL_SCOPE = "JavaScript GLOBAL SCOPE";

    // This is the only optimization level that supports continuations
    // in the Christoper Oliver's Rhino JavaScript implementation
    static int OPTIMIZATION_LEVEL = -2;

    /**
     * When was the last time we checked for script modifications. Used
     * only if {@link #reloadScripts} is true.
     */
    protected long lastTimeCheck = 0;

    /**
     * Shared global scope for scripts and other immutable objects
     */
    JSGlobal scope;

    /**
     * List of <code>String</code> objects that represent files to be
     * read in by the JavaScript interpreter.
     */
    List topLevelScripts = new ArrayList();

    JSErrorReporter errorReporter;
    boolean enableDebugger = false;
    /**
     * JavaScript debugger: there's only one of these: it can debug multiple
     * threads executing JS code.
     */
    static org.mozilla.javascript.tools.debugger.Main debugger;

    static synchronized org.mozilla.javascript.tools.debugger.Main getDebugger() {
        if (debugger == null) {
            final org.mozilla.javascript.tools.debugger.Main db
                = new org.mozilla.javascript.tools.debugger.Main("Cocoon Flow Debugger");
            db.pack();
            java.awt.Dimension size =
                java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            size.width *= 0.75;
            size.height *= 0.75;
            db.setSize(size);
            db.setExitAction(new Runnable() {
                    public void run() {
                        db.setVisible(false);
                    }
                });
            db.setOptimizationLevel(OPTIMIZATION_LEVEL);
            db.setVisible(true);
            debugger = db;
            Context.addContextListener(debugger);
        }
        return debugger;
    }

    public void configure(Configuration config)
        throws ConfigurationException
    {
        super.configure(config);

        String loadOnStartup
            = config.getChild("load-on-startup", true).getValue(null);
        if (loadOnStartup != null) {
            register(loadOnStartup);
        }

        String debugger
            = config.getChild("debugger").getValue(null);
        if ("enabled".equalsIgnoreCase(debugger)) {
            enableDebugger = true;
        }
    }

    public void initialize()
        throws Exception
    {
        if (enableDebugger) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Flow debugger enabled, creating");
            }
            getDebugger().doBreak();
        }
        Context context = Context.enter();
        context.setOptimizationLevel(OPTIMIZATION_LEVEL);
        context.setCompileFunctionsWithDynamicScope(true);
        context.setGeneratingDebug(true);
        // add support for Rhino objects to JXPath
        JXPathIntrospector.registerDynamicClass(org.mozilla.javascript.Scriptable.class,
                                                ScriptablePropertyHandler.class);
        JXPathContextReferenceImpl.addNodePointerFactory(new ScriptablePointerFactory());

        try {
            scope = new JSGlobal(context);

            // Register some handy classes with JavaScript, so we can make
            // use of them from the flow layer.

            // Access to the Cocoon log
            ScriptableObject.defineClass(scope, JSLog.class);

            // Access to Cocoon internal objects
            ScriptableObject.defineClass(scope, JSCocoon.class);

            // Wrapper for WebContinuation
            ScriptableObject.defineClass(scope, JSWebContinuation.class);

            // Define some functions on the top level scope
            String[] names = { "print" };
            try {
                scope.defineFunctionProperties(names, JSGlobal.class,
                                               ScriptableObject.DONTENUM);
            } catch (PropertyException e) {
                throw new Error(e.getMessage());
            }

            // Define some global variables in JavaScript
            Object args[] = {};
            Scriptable log = context.newObject(scope, "Log", args);
            ((JSLog)log).enableLogging(getLogger());
            scope.put("log", scope, log);
            errorReporter = new JSErrorReporter(getLogger());
        }
        catch (Exception e) {
            Context.exit();
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Returns the JavaScript scope, a Scriptable object, from the user
     * session instance. Each URI prefix, as returned by the {@link
     * org.apache.cocoon.environment.Environment#getURIPrefix} method,
     * can have a scope associated with it.
     *
     * @param environment an <code>Environment</code> value
     * @return a <code>Scriptable</code> value
     */
    public Scriptable getSessionScope(Environment environment)
    {
        Map objectModel = environment.getObjectModel();
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession(false);

        if (session == null) {
            return null;
        }
        Scriptable scope;
        HashMap userScopes = (HashMap)session.getAttribute(USER_GLOBAL_SCOPE);

        if (userScopes == null) {
            return null;
        }
        String uriPrefix = environment.getURIPrefix();
        scope = (Scriptable)userScopes.get(uriPrefix);
        return scope;
    }

    /**
     * Associates a JavaScript scope, a Scriptable object, with the URI
     * prefix of the current sitemap, as returned by the {@link
     * org.apache.cocoon.environment.Environment#getURIPrefix} method.
     *
     * @param environment an <code>Environment</code> value
     * @param scope a <code>Scriptable</code> value
     */
    public void setSessionScope(Environment environment, Scriptable scope)
    {
        Map objectModel = environment.getObjectModel();
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession(true);

        HashMap userScopes = (HashMap)session.getAttribute(USER_GLOBAL_SCOPE);
        if (userScopes == null) {
            userScopes = new HashMap();
            session.setAttribute(USER_GLOBAL_SCOPE, userScopes);
        }

        String uriPrefix = environment.getURIPrefix();
        userScopes.put(uriPrefix, scope);
    }

    public void removeSessionScope(Environment environment)
    {
        Map objectModel = environment.getObjectModel();
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession(false);
        if (session != null) {
            HashMap userScopes = (HashMap)session.getAttribute(USER_GLOBAL_SCOPE);
            if (userScopes != null) {
                userScopes.remove(environment.getURIPrefix());
            }
        }
    }

    /**
     * Returns a new Scriptable object to be used as the global scope
     * when running the JavaScript scripts in the context of a request.
     *
     * <p>If you want to maintain the state of global variables across
     * multiple invocations of <code>&lt;map:call
     * function="..."&gt;</code>, you need to invoke from the JavaScript
     * script <code>cocoon.createSession()</code>. This will place the
     * newly create Scriptable object in the user's session, where it
     * will be retrieved from at the next invocation of {@link #callFunction}.</p>
     *
     * @param environment an <code>Environment</code> value
     * @return a <code>Scriptable</code> value
     * @exception Exception if an error occurs
     */
    protected Scriptable enterContext(Environment environment)
        throws Exception
    {
        Context context = Context.enter();
        context.setOptimizationLevel(OPTIMIZATION_LEVEL);
        context.setGeneratingDebug(true);
        context.setCompileFunctionsWithDynamicScope(true);
        context.setErrorReporter(errorReporter);
        Scriptable thrScope = null;


        // Try to retrieve the scope object from the session instance. If
        // no scope is found, we create a new one, but don't place it in
        // the session.
        //
        // When a user script "creates" a session using
        // cocoon.createSession() in JavaScript, the thrScope is placed in
        // the session object, where it's later retrieved from here. This
        // behaviour allows multiple JavaScript functions to share the
        // same global scope.
        thrScope = getSessionScope(environment);

        // The Cocoon object exported to JavaScript needs to be setup here
        JSCocoon cocoon;
        boolean newScope = false;
        long lastExecTime = 0;
        if (thrScope == null) {

            newScope = true;

            thrScope = context.newObject(scope);

            thrScope.setPrototype(scope);
            // We want 'thrScope' to be a new top-level scope, so set its
            // parent scope to null. This means that any variables created
            // by assignments will be properties of "thrScope".
            thrScope.setParentScope(null);

            // Put in the thread scope the Cocoon object, which gives access
            // to the interpreter object, and some Cocoon objects. See
            // JSCocoon for more details.
            Object args[] = {};
            cocoon = (JSCocoon)context.newObject(thrScope, "Cocoon", args);
            cocoon.setInterpreter(this);
            cocoon.setParentScope(thrScope);
            thrScope.put("cocoon", thrScope, cocoon);
            ((ScriptableObject)thrScope).defineProperty(LAST_EXEC_TIME,
                                                        new Long(0),
                                                        ScriptableObject.DONTENUM |
                                                        ScriptableObject.PERMANENT);

        } else {
            cocoon = (JSCocoon)thrScope.get("cocoon", thrScope);
            lastExecTime = ((Long)thrScope.get(LAST_EXEC_TIME,
                                               thrScope)).longValue();

        }
        // We need to setup the JSCocoon object according to the current
        // request. Everything else remains the same.
        cocoon.setContext(manager, environment);

        // Check if we need to compile and/or execute scripts
        synchronized (compiledScripts) {
            List execList = new ArrayList();
            boolean needsRefresh = false;
            if (reloadScripts) {
                long now = System.currentTimeMillis();
                if (now >= lastTimeCheck + checkTime) {
                    needsRefresh = true;
                }
                lastTimeCheck = now;
            }
            // If we've never executed scripts in this scope or
            // if reload-scripts is true and the check interval has expired
            // or if new scripts have been specified in the sitemap,
            // then create a list of scripts to compile/execute
            if (lastExecTime == 0 || needsRefresh || needResolve.size() > 0) {
                topLevelScripts.addAll(needResolve);
                if (!newScope && !needsRefresh) {
                    execList.addAll(needResolve);
                } else {
                    execList.addAll(topLevelScripts);
                }
                needResolve.clear();
            }
            thrScope.put(LAST_EXEC_TIME, thrScope,
                         new Long(System.currentTimeMillis()));
            // Compile all the scripts first. That way you can set breakpoints
            // in the debugger before they execute.
            for (int i = 0, size = execList.size(); i < size; i++) {
                String sourceURI = (String)execList.get(i);
                ScriptSourceEntry entry =
                    (ScriptSourceEntry)compiledScripts.get(sourceURI);
                if (entry == null) {
                    Source src = this.sourceresolver.resolveURI(sourceURI);
                    entry = new ScriptSourceEntry(src);
                    compiledScripts.put(sourceURI, entry);
                }
                // Compile the script if necessary
                entry.getScript(context, this.scope, needsRefresh, this);
            }
            // Execute the scripts if necessary
            for (int i = 0, size = execList.size(); i < size; i++) {
                String sourceURI = (String)execList.get(i);
                ScriptSourceEntry entry =
                    (ScriptSourceEntry)compiledScripts.get(sourceURI);
                long lastMod = entry.getSource().getLastModified();
                Script script = entry.getScript(context, this.scope, false, this);
                if (lastExecTime == 0 || lastMod > lastExecTime) {
                    script.exec(context, thrScope);
                }
            }
        }
        return thrScope;
    }

    /**
     * Remove the Cocoon object from the JavaScript thread scope so it
     * can be garbage collected, together with all the objects it
     * contains.
     */
    protected void exitContext(Scriptable thrScope)
    {
        // thrScope may be null if an exception occurred compiling a script
        if (thrScope != null) {
            JSCocoon cocoon = (JSCocoon)thrScope.get("cocoon", thrScope);
            cocoon.invalidateContext();
        }
        Context.exit();
    }


    /**
     * Compile filename as JavaScript code
     * @param cx Rhino context
     * @param environment source resolver
     * @param fileName resource uri
     * @return compiled script
     */

    public Script compileScript(Context cx,
                                Environment environment,
                                String fileName) throws Exception {
        Source src = this.sourceresolver.resolveURI(fileName);
        if (src == null) {
            throw new ResourceNotFoundException(fileName + ": not found");
        }
        synchronized (compiledScripts) {
            ScriptSourceEntry entry =
                (ScriptSourceEntry)compiledScripts.get(src.getURI());
            Script compiledScript = null;
            if (entry == null) {
                compiledScripts.put(src.getURI(),
                                    entry = new ScriptSourceEntry(src));
            } else {
                this.sourceresolver.release(src);
            }
            compiledScript = entry.getScript(cx, this.scope, false, this);
            return compiledScript;
        }
    }

    protected Script compileScript(Context cx, Scriptable scope,
                                  Source src) throws Exception {
        InputStream is = src.getInputStream();
        if (is == null) {
            throw new ResourceNotFoundException(src.getURI() + ": not found");
        }
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is));
            Script compiledScript = cx.compileReader(scope, reader,
                                                     src.getURI(),
                                                     1, null);
            return compiledScript;
        } finally {
            is.close();
        }
    }

    /**
     * Calls a JavaScript function, passing <code>params</code> as its
     * arguments. In addition to this, it makes available the parameters
     * through the <code>cocoon.parameters</code> JavaScript array
     * (indexed by the parameter names).
     *
     * @param funName a <code>String</code> value
     * @param params a <code>List</code> value
     * @param environment an <code>Environment</code> value
     * @exception Exception if an error occurs
     */
    public void callFunction(String funName, List params,
                             Environment environment)
        throws Exception
    {
        Scriptable thrScope = null;
        try {
            thrScope = enterContext(environment);

            Context context = Context.getCurrentContext();
            JSCocoon cocoon = (JSCocoon)thrScope.get("cocoon", thrScope);
            if (enableDebugger) {
                if (!getDebugger().isVisible()) {
                    // only raise the debugger window if it isn't already visible
                    getDebugger().setVisible(true);
                }
            }
            int size = (params != null ? params.size() : 0);
            Object[] funArgs = new Object[size];
            NativeArray parameters = new NativeArray(size);
            if (size != 0) {
                for (int i = 0; i < size; i++) {
                    Interpreter.Argument arg = (Interpreter.Argument)params.get(i);
                    funArgs[i] = arg.value;
                    if (arg.name == null) arg.name = "";
                    parameters.put(arg.name, parameters, arg.value);
                }
            }
            cocoon.setParameters(parameters);
            NativeArray funArgsArray = new NativeArray(funArgs);
            Object fun = ScriptableObject.getProperty(thrScope, funName);
            if (fun == Scriptable.NOT_FOUND) {
                fun = funName; // this will produce a better error message
            }
            Object callFunArgs[] = { fun, funArgsArray };
            Object callFun = ScriptableObject.getProperty(thrScope, "callFunction");
            if (callFun == Scriptable.NOT_FOUND) {
                callFun = "callFunction"; // this will produce a better error message
            }
            ScriptRuntime.call(context, callFun, thrScope, callFunArgs, thrScope);
        } catch (JavaScriptException ex) {
            EvaluatorException ee =
                Context.reportRuntimeError(ToolErrorReporter.getMessage("msg.uncaughtJSException",
                                                                        ex.getMessage()));
            Throwable unwrapped = unwrap(ex);
            if (unwrapped instanceof ProcessingException) {
                throw (ProcessingException)unwrapped;
            }

            throw new CascadingRuntimeException(ee.getMessage(), unwrapped);
        } catch (EcmaError ee) {
            String msg = ToolErrorReporter.getMessage("msg.uncaughtJSException", ee.toString());
            if (ee.getSourceName() != null) {
                Context.reportRuntimeError(msg,
                                           ee.getSourceName(),
                                           ee.getLineNumber(),
                                           ee.getLineSource(),
                                           ee.getColumnNumber());
            } else {
                Context.reportRuntimeError(msg);
            }
            throw new CascadingRuntimeException(ee.getMessage(), ee);
        } finally {
            exitContext(thrScope);
        }
    }

    public void handleContinuation(String id, List params,
                                   Environment environment)
        throws Exception
    {
        WebContinuation wk = continuationsMgr.lookupWebContinuation(id);

        if (wk == null) {

            /*
             * Throw an InvalidContinuationException to be handled inside the
             * <map:handle-errors> sitemap element.
             */
            throw new InvalidContinuationException("The continuation ID " + id + " is invalid.");
        }

        Context context = Context.enter();
        context.setOptimizationLevel(OPTIMIZATION_LEVEL);
        context.setGeneratingDebug(true);
        context.setCompileFunctionsWithDynamicScope(true);

        // Obtain the JS continuation object from it, and setup the
        // JSCocoon object associated in the dynamic scope of the saved
        // continuation with the environment and context objects.
        JSWebContinuation jswk = (JSWebContinuation)wk.getUserObject();
        JSCocoon cocoon = jswk.getJSCocoon();
        cocoon.setContext(manager, environment);
        final Scriptable kScope = cocoon.getParentScope();
        if (enableDebugger) {
            getDebugger().setVisible(true);
        }

        // We can now resume the processing from the state saved by the
        // continuation object. Setup the JavaScript Context object.
        Object handleContFunction = kScope.get("handleContinuation", kScope);
        if (handleContFunction == Scriptable.NOT_FOUND) {
            throw new RuntimeException("Cannot find 'handleContinuation' "
                                       + "(system.js not loaded?)");
        }

        Object args[] = { jswk };

        int size = (params != null ? params.size() : 0);
        NativeArray parameters = new NativeArray(size);

        if (size != 0) {
            for (int i = 0; i < size; i++) {
                Interpreter.Argument arg = (Interpreter.Argument)params.get(i);
                parameters.put(arg.name, parameters, arg.value);
            }
        }

        cocoon.setParameters(parameters);

        try {
            ((Function)handleContFunction).call(context, kScope, kScope, args);
        } catch (JavaScriptException ex) {
            EvaluatorException ee =
                Context.reportRuntimeError(ToolErrorReporter.getMessage("msg.uncaughtJSException",
                                                                        ex.getMessage()));
            Throwable unwrapped = unwrap(ex);
            if (unwrapped instanceof ProcessingException) {
                throw (ProcessingException)unwrapped;
            }

            throw new CascadingRuntimeException(ee.getMessage(), unwrapped);
        } catch (EcmaError ee) {
            String msg = ToolErrorReporter.getMessage("msg.uncaughtJSException", ee.toString());
            if (ee.getSourceName() != null) {
                Context.reportRuntimeError(msg,
                                           ee.getSourceName(),
                                           ee.getLineNumber(),
                                           ee.getLineSource(),
                                           ee.getColumnNumber());
            } else {
                Context.reportRuntimeError(msg);
            }
            throw new CascadingRuntimeException(ee.getMessage(), ee);
        } finally {
            Context.exit();
        }
    }

    private Throwable unwrap(JavaScriptException e) {
        Object value = e.getValue();
        while (value instanceof Wrapper) {
            value = ((Wrapper)value).unwrap();
        }
        if (value instanceof Throwable) {
            return (Throwable)value;
        }
        return e;
    }

    public void forwardTo(String uri, Object bizData,
                          WebContinuation continuation,
                          Environment environment)
        throws Exception {
        Map objectModel = environment.getObjectModel();
        // Make the live-connect objects available to the view layer
        JavaScriptFlow.setPackages(objectModel,
                                   (Scriptable)ScriptableObject.getProperty(scope,
                                                                            "Packages"));
        JavaScriptFlow.setJavaPackage(objectModel,
                                      (Scriptable)ScriptableObject.getProperty(scope,
                                                                   "java"));
        super.forwardTo(uri, bizData, continuation, environment);
    }
    
}
