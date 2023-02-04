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
package org.apache.cocoon.components.flow.javascript.fom;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.flow.CompilingInterpreter;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.flow.InvalidContinuationException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.flow.javascript.JSErrorReporter;
import org.apache.cocoon.components.flow.javascript.LocationTrackingDebugger;
import org.apache.cocoon.components.flow.javascript.ScriptablePointerFactory;
import org.apache.cocoon.components.flow.javascript.ScriptablePropertyHandler;
import org.apache.cocoon.components.flow.util.PipelineUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;

import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;

import org.apache.excalibur.source.Source;

import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.NativeJavaPackage;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;
import org.mozilla.javascript.continuations.Continuation;
import org.mozilla.javascript.tools.debugger.Main;
import org.mozilla.javascript.tools.shell.Global;

/**
 * Interface with the JavaScript interpreter.
 *
 * @since March 25, 2002
 * @version $Id$
 */
public class FOM_JavaScriptInterpreter extends CompilingInterpreter
                                       implements Initializable {

    /**
     * A long value is stored under this key in each top level JavaScript
     * thread scope object. When you enter a context any scripts whose
     * modification time is later than this value will be recompiled and reexecuted,
     * and this value will be updated to the current time.
     */
    private final static String LAST_EXEC_TIME = "__PRIVATE_LAST_EXEC_TIME__";

    /**
     * Prefix for session/request attribute storing JavaScript global scope object.
     */
    private static final String USER_GLOBAL_SCOPE = "FOM JavaScript GLOBAL SCOPE/";

    /**
     * Rhino supports Debuggers only in interpreting mode, and we are using
     * LocationTrackerDebugger. Hence need to force rhino into interpreted
     * mode by setting optimization level on a context.
     */
    private static final int OPTIMIZATION_LEVEL = -1;
    
    /**
     * When was the last time we checked for script modifications. Used
     * only if {@link #reloadScripts} is true. Access is synchronized by
     * {@link #compiledScripts}.
     */
    private long lastReloadCheckTime;

    /**
     * Shared global scope for scripts and other immutable objects
     */
    private Global scope;

    /**
     * List of <code>String</code> objects that represent files to be
     * read in by the JavaScript interpreter.
     */
    private List topLevelScripts = new ArrayList();

    private boolean enableDebugger;

    
    /**
     * JavaScript debugger: there's only one of these: it can debug multiple
     * threads executing JS code.
     */
    private static Main debugger;

    static synchronized Main getDebugger() {
        if (debugger == null) {
            final Main db = new Main("Cocoon Flow Debugger");
            db.pack();
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            size.width *= 0.75;
            size.height *= 0.75;
            db.setSize(size.width, size.height);
            db.setExitAction(new Runnable() {
                public void run() {
                    db.setVisible(false);
                }
            });
            db.setVisible(true);
            debugger = db;
            debugger.attachTo(ContextFactory.getGlobal());
        }
        return debugger;
    }


    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);

        String loadOnStartup = config.getChild("load-on-startup").getValue(null);
        if (loadOnStartup != null) {
            register(loadOnStartup);
        }

        String debugger = config.getChild("debugger").getValue(null);
        enableDebugger = "enabled".equalsIgnoreCase(debugger);
    }

    public void initialize() throws Exception {
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
        JXPathIntrospector.registerDynamicClass(Scriptable.class,
                                                ScriptablePropertyHandler.class);
        JXPathContextReferenceImpl.addNodePointerFactory(new ScriptablePointerFactory());

        try {
            scope = new Global(context);
            // Access to Cocoon internal objects
            FOM_Cocoon.init(scope);
        } catch (Exception e) {
            Context.exit();
            throw e;
        }
    }


    /**
     * Returns the JavaScript scope, a Scriptable object, from the user
     * session instance. Each interpreter instance can have a scope
     * associated with it.
     *
     * @return a <code>ThreadScope</code> value
     */
    private ThreadScope getSessionScope() throws Exception {
        final String scopeID = USER_GLOBAL_SCOPE + getInterpreterID();
        final Request request = ObjectModelHelper.getRequest(this.processInfoProvider.getObjectModel());

        ThreadScope scope;

        // Get/create the scope attached to the current context
        HttpSession session = request.getSession(false);
        if (session != null) {
            scope = (ThreadScope) session.getAttribute(scopeID);
        } else {
            scope = (ThreadScope) request.getAttribute(scopeID);
        }

        if (scope == null) {
            scope = createThreadScope();
            // Save scope in the request early to allow recursive Flow calls
            request.setAttribute(scopeID, scope);
        }

        return scope;
    }

    /**
     * Associates a JavaScript scope, a Scriptable object, with
     * {@link #getInterpreterID() identifier} of this {@link Interpreter}
     * instance.
     *
     * @param scope a <code>ThreadScope</code> value
     */
    private void setSessionScope(ThreadScope scope) throws Exception {
        if (scope.useSession) {
            final String scopeID = USER_GLOBAL_SCOPE + getInterpreterID();
            final Request request = ObjectModelHelper.getRequest(this.processInfoProvider.getObjectModel());

            // FIXME: Where "session scope" should go when session is invalidated?
            // Attach the scope to the current context
            try {
                HttpSession session = request.getSession(true);
                session.setAttribute(scopeID, scope);
            } catch (IllegalStateException e) {
                // Session might be invalidated already.
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Got '" + e + "' while trying to set session scope.", e);
                }
            }
        }
    }

    public static class ThreadScope extends ScriptableObject {
        private static final String[] BUILTIN_PACKAGES = { "javax", "org", "com" };
        private static final String[] BUILTIN_FUNCTIONS = { "importClass" };

        private ClassLoader classLoader;

        /** true if this scope has assigned any global vars */
        boolean useSession;

        /** true if this scope is locked for implicit variable declarations */
        boolean locked;

        /**
         * Initializes new top-level scope.
         */
        public ThreadScope(Global scope) throws Exception {
            final Context context = Context.getCurrentContext();

            defineFunctionProperties(BUILTIN_FUNCTIONS,
                                     ThreadScope.class,
                                     ScriptableObject.DONTENUM);

            setPrototype(scope);

            // We want this to be a new top-level scope, so set its
            // parent scope to null. This means that any variables created
            // by assignments will be properties of this.
            setParentScope(null);

            // Put in the thread scope the Cocoon object, which gives access
            // to the interpreter object, and some Cocoon objects. See
            // FOM_Cocoon for more details.
            final Object[] args = {};
            FOM_Cocoon cocoon = (FOM_Cocoon) context.newObject(this,
                                                               "FOM_Cocoon",
                                                               args);
            cocoon.setParentScope(this);
            super.put("cocoon", this, cocoon);

            defineProperty(LAST_EXEC_TIME,
                           new Long(0),
                           ScriptableObject.DONTENUM | ScriptableObject.PERMANENT);
        }

        public String getClassName() {
            return "ThreadScope";
        }

        public void setLock(boolean lock) {
            this.locked = lock;
        }

        public void put(String name, Scriptable start, Object value) {
            //Allow setting values to existing variables, or if this is a
            //java class (used by importClass & importPackage)
            if (this.locked && !has(name, start) && !(value instanceof NativeJavaClass) && !(value instanceof Function)) {
                // Need to wrap into a runtime exception as Scriptable.put has no throws clause...
                throw new WrappedException (new RuntimeException("Implicit declaration of global variable '" + name +
                  "' forbidden. Please ensure all variables are explicitely declared with the 'var' keyword"));
            }
            this.useSession = true;
            super.put(name, start, value);
        }

        public void put(int index, Scriptable start, Object value) {
            // FIXME(SW): do indexed properties have a meaning on the global scope?
            if (this.locked && !has(index, start)) {
                throw new WrappedException(new RuntimeException("Global scope locked. Cannot set value for index " + index));
            }
            this.useSession = true;
            super.put(index, start, value);
        }

        /** Invoked after script execution */
        void onExec() {
            this.useSession = false;
            super.put(LAST_EXEC_TIME, this, new Long(System.currentTimeMillis()));
        }

        /** Override importClass to allow reloading of classes */
        public static void importClass(Context ctx,
                                       Scriptable thisObj,
                                       Object[] args,
                                       Function funObj) {
            for (int i = 0; i < args.length; i++) {
                Object clazz = args[i];
                if (!(clazz instanceof NativeJavaClass)) {
                    throw Context.reportRuntimeError("Not a Java class: " +
                                                     Context.toString(clazz));
                }
                String s = ((NativeJavaClass) clazz).getClassObject().getName();
                String n = s.substring(s.lastIndexOf('.') + 1);
                thisObj.put(n, thisObj, clazz);
            }
        }

        public void setupPackages(ClassLoader cl) throws Exception {
            final String JAVA_PACKAGE = "JavaPackage";
            if (classLoader != cl) {
                classLoader = cl;
                Scriptable newPackages = new NativeJavaPackage("", cl);
                newPackages.setParentScope(this);
                newPackages.setPrototype(ScriptableObject.getClassPrototype(this, JAVA_PACKAGE));
                super.put("Packages", this, newPackages);
                for (int i = 0; i < BUILTIN_PACKAGES.length; i++) {
                    String pkgName = BUILTIN_PACKAGES[i];
                    Scriptable pkg = new NativeJavaPackage(pkgName, cl);
                    pkg.setParentScope(this);
                    pkg.setPrototype(ScriptableObject.getClassPrototype(this, JAVA_PACKAGE));
                    super.put(pkgName, this, pkg);
                }
            }
        }

        public ClassLoader getClassLoader() {
            return classLoader;
        }
    }

    private ThreadScope createThreadScope() throws Exception {
        return new ThreadScope(scope);
    }

    /**
     * Sets up a ThreadScope object to be used as the global scope
     * when running the JavaScript scripts in the context of a request.
     *
     * <p>If you want to maintain the state of global variables across
     * multiple invocations of <code>&lt;map:call
     * function="..."&gt;</code>, you need to instanciate the session
     * object which is a property of the cocoon object
     * <code>var session = cocoon.session</code>. This will place the
     * newly create Scriptable object in the user's session, where it
     * will be retrieved from at the next invocation of {@link #callFunction}.</p>
     *
     * @exception Exception if an error occurs
     */
    private void setupContext(Redirector redirector, Context context,
                              ThreadScope thrScope)
    throws Exception {

        // We need to setup the FOM_Cocoon object according to the current
        // request. Everything else remains the same.
        ClassLoader contextClassloader = Thread.currentThread().getContextClassLoader();
        thrScope.setupPackages(contextClassloader);

        FOM_Cocoon cocoon = (FOM_Cocoon) thrScope.get("cocoon", thrScope);
        cocoon.pushCallContext(this, redirector, avalonContext, null);

        // Time when scripts were last executed, in this thread scope
        final long lastExecuted = ((Long) thrScope.get(LAST_EXEC_TIME,
                                                       thrScope)).longValue();

        // List of scripts (ScriptSourceEntry objects) which might have to be
        // executed in this thread scope.
        List execList = new ArrayList();

        // Check if we need to (re)compile any of the scripts
        synchronized (compiledScripts) {
            // Determine if refresh is needed.
            boolean needsRefresh = false;
            if (reloadScripts) {
                long now = System.currentTimeMillis();
                if (now >= lastReloadCheckTime + checkTime) {
                    needsRefresh = true;
                    lastReloadCheckTime = now;
                }
            }

            // List of script URIs to resolve
            List resolveList = new ArrayList();
            // If reloadScripts is true, recompile all top level scripts
            if (needsRefresh) {
                resolveList.addAll(topLevelScripts);
            }
            // If new scripts has been specified in sitemap, load and compile them
            if (needResolve.size() > 0) {
                topLevelScripts.addAll(needResolve);
                resolveList.addAll(needResolve);
                needResolve.clear();
            }

            // Compile all the scripts first. That way you can set breakpoints
            // in the debugger before they execute.
            for (int i = 0, size = resolveList.size(); i < size; i++) {
                String sourceURI = (String) resolveList.get(i);
                ScriptSourceEntry entry =
                        (ScriptSourceEntry) compiledScripts.get(sourceURI);
                if (entry == null) {
                    Source src = this.sourceresolver.resolveURI(sourceURI);
                    entry = new ScriptSourceEntry(src);
                    compiledScripts.put(sourceURI, entry);
                }
                entry.compile(context, this.scope);
                // If top level scripts were executed in this thread scope,
                // collect only newly added scripts for execution.
                if (lastExecuted != 0) {
                    execList.add(entry);
                }
            }

            // If scripts were never executed in this thread scope yet,
            // then collect all top level scripts for execution.
            if (lastExecuted == 0) {
                for (int i = 0, size = topLevelScripts.size(); i < size; i++) {
                    String sourceURI = (String) topLevelScripts.get(i);
                    ScriptSourceEntry entry =
                            (ScriptSourceEntry) compiledScripts.get(sourceURI);
                    if (entry != null) {
                        execList.add(entry);
                    }
                }
            }
        }

        // Execute the scripts identified above, as necessary
        boolean executed = false;
        for (int i = 0, size = execList.size(); i < size; i++) {
            ScriptSourceEntry entry = (ScriptSourceEntry) execList.get(i);
            if (lastExecuted == 0 || entry.getCompileTime() > lastExecuted) {
                entry.getScript().exec(context, thrScope);
                executed = true;
            }
        }

        // If any of the scripts has been executed, inform ThreadScope,
        // which will update last execution timestamp.
        if (executed) {
            thrScope.onExec();
        }
    }

    /**
     * Compile filename as JavaScript code
     *
     * @param cx Rhino context
     * @param fileName resource uri
     * @return compiled script
     */
    Script compileScript(Context cx, String fileName) throws Exception {
        Source src = this.sourceresolver.resolveURI(fileName);
        synchronized (compiledScripts) {
            ScriptSourceEntry entry =
                    (ScriptSourceEntry) compiledScripts.get(src.getURI());
            if (entry == null) {
                compiledScripts.put(src.getURI(),
                                    entry = new ScriptSourceEntry(src));
            } else {
                this.sourceresolver.release(src);
            }

            long compileTime = entry.getCompileTime();
            if (compileTime == 0 || reloadScripts && (compileTime + checkTime < System.currentTimeMillis())) {
                entry.compile(cx, this.scope);
            }

            return entry.getScript();
        }
    }

    protected Script compileScript(Context cx, Scriptable scope, Source src)
    throws Exception {
        PushbackInputStream is = new PushbackInputStream(src.getInputStream(), ENCODING_BUF_SIZE);
        try {
            String encoding = findEncoding(is);
            Reader reader = encoding == null ? new InputStreamReader(is) : new InputStreamReader(is, encoding);
            reader = new BufferedReader(reader);
            return cx.compileReader(reader, src.getURI(), 1, null);
        } finally {
            is.close();
        }
    }

    // A charset name can be up to 40 characters taken from the printable characters of US-ASCII
    // (see http://www.iana.org/assignments/character-sets). So reading 100 bytes should be more than enough.
    private final static int ENCODING_BUF_SIZE = 100;
    // Match 'encoding = xxxx' on the first line
    private final static REProgram encodingRE = new RECompiler().compile("encoding\\s*=\\s*([^\\s]*)");

    /**
     * Find the encoding of the stream, or null if not specified
     */
    String findEncoding(PushbackInputStream is) throws IOException {
        // Read some bytes
        byte[] buffer = new byte[ENCODING_BUF_SIZE];
        int len = is.read(buffer, 0, buffer.length);
        // and push them back
        is.unread(buffer, 0, len);

        // Interpret them as an ASCII string
        String str = new String(buffer, 0, len, "ASCII");
        RE re = new RE(encodingRE);
        if (re.match(str)) {
            return re.getParen(1);
        }
        return null;
    }

    /**
     * Calls a JavaScript function, passing <code>params</code> as its
     * arguments. In addition to this, it makes available the parameters
     * through the <code>cocoon.parameters</code> JavaScript array
     * (indexed by the parameter names).
     *
     * @param funName a <code>String</code> value
     * @param params a <code>List</code> value
     * @param redirector
     * @exception Exception if an error occurs
     */
    public void callFunction(String funName, List params, Redirector redirector)
    throws Exception {
        Context context = Context.enter();
        context.setOptimizationLevel(OPTIMIZATION_LEVEL); 
        context.setGeneratingDebug(true);
        context.setCompileFunctionsWithDynamicScope(true);
        context.setErrorReporter(new JSErrorReporter());

        LocationTrackingDebugger locationTracker = new LocationTrackingDebugger();
        if (!enableDebugger) {
            //FIXME: add a "tee" debugger that allows both to be used simultaneously
            context.setDebugger(locationTracker, null);
        }

        // Try to retrieve the scope object from the session instance. If
        // no scope is found, we create a new one, but don't place it in
        // the session.
        //
        // When a user script "creates" a session using
        // cocoon.createSession() in JavaScript, the thrScope is placed in
        // the session object, where it's later retrieved from here. This
        // behaviour allows multiple JavaScript functions to share the
        // same global scope.
        ThreadScope thrScope = getSessionScope();

        synchronized (thrScope) {
            ClassLoader savedClassLoader =
                Thread.currentThread().getContextClassLoader();
            FOM_Cocoon cocoon = null;
            try {
                try {
                    setupContext(redirector, context, thrScope);
                    cocoon = (FOM_Cocoon) thrScope.get("cocoon", thrScope);

                    // Register the current scope for scripts indirectly called from this function
                    FOM_JavaScriptFlowHelper.setFOM_FlowScope(cocoon.getObjectModel(), thrScope);

                    if (enableDebugger) {
                        // only raise the debugger window if it isn't already visible
                        if (!getDebugger().isVisible()) {
                            getDebugger().setVisible(true);
                        }
                    }

                    int size = (params != null ? params.size() : 0);
                    Scriptable parameters = context.newObject(thrScope);
                    for (int i = 0; i < size; i++) {
                        Interpreter.Argument arg = (Interpreter.Argument)params.get(i);
                        if (arg.name == null) {
                            arg.name = "";
                        }
                        parameters.put(arg.name, parameters, arg.value);
                    }
                    cocoon.setParameters(parameters);

                    // Resolve function name
                    //
                    Object fun;
                    try {
                        fun = context.compileString(funName, null, 1, null).exec (context, thrScope);
                    } catch (EcmaError ee) {
                        throw new ResourceNotFoundException (
                             "Function \"javascript:" + funName + "()\" not found");
                    }

                    thrScope.setLock(true);
                    ScriptRuntime.call(context, fun, thrScope, new Object[0], thrScope);
                } catch (JavaScriptException e) {
                    throw locationTracker.getException("Error calling flowscript function " + funName, e);
                } catch (EcmaError e) {
                    throw locationTracker.getException("Error calling function " + funName, e);
                } catch (WrappedException e) {
                    throw locationTracker.getException("Error calling function " + funName, e);
                }
            } finally {
                thrScope.setLock(false);
                setSessionScope(thrScope);
                if (cocoon != null) {
                    cocoon.popCallContext();
                }
                Context.exit();
                Thread.currentThread().setContextClassLoader(savedClassLoader);
            }
        }
    }

    public void handleContinuation(String id, List params,
                                   Redirector redirector) throws Exception
    {
        WebContinuation wk = continuationsMgr.lookupWebContinuation(id, getInterpreterID());

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
        LocationTrackingDebugger locationTracker = new LocationTrackingDebugger();
        if (!enableDebugger) {
            //FIXME: add a "tee" debugger that allows both to be used simultaneously
            context.setDebugger(locationTracker, null);
        }

        // Obtain the continuation object from it, and setup the
        // FOM_Cocoon object associated in the dynamic scope of the saved
        // continuation with the environment and context objects.
        Continuation k = (Continuation) wk.getContinuation();
        ThreadScope kScope = (ThreadScope) k.getParentScope();

        synchronized (kScope) {
            ClassLoader savedClassLoader =
                Thread.currentThread().getContextClassLoader();
            FOM_Cocoon cocoon = null;
            try {
                Thread.currentThread().setContextClassLoader(kScope.getClassLoader());
                cocoon = (FOM_Cocoon)kScope.get("cocoon", kScope);
                kScope.setLock(true);
                cocoon.pushCallContext(this, redirector, avalonContext, wk);

                // Register the current scope for scripts indirectly called from this function
                FOM_JavaScriptFlowHelper.setFOM_FlowScope(cocoon.getObjectModel(), kScope);

                if (enableDebugger) {
                    getDebugger().setVisible(true);
                }
                Scriptable parameters = context.newObject(kScope);
                int size = params != null ? params.size() : 0;
                for (int i = 0; i < size; i++) {
                    Interpreter.Argument arg = (Interpreter.Argument)params.get(i);
                    parameters.put(arg.name, parameters, arg.value);
                }
                cocoon.setParameters(parameters);
                FOM_WebContinuation fom_wk = new FOM_WebContinuation(wk);
                fom_wk.setLogger(getLogger());
                fom_wk.setParentScope(kScope);
                fom_wk.setPrototype(ScriptableObject.getClassPrototype(kScope,
                                                                       fom_wk.getClassName()));
                Object[] args = new Object[] {k, fom_wk};
                try {
                    ScriptableObject.callMethod(cocoon,
                                                "handleContinuation", args);
                } catch (JavaScriptException e) {
                    throw locationTracker.getException("Error calling continuation", e);
                } catch (EcmaError e) {
                    throw locationTracker.getException("Error calling continuation", e);
                } catch (WrappedException e) {
                    throw locationTracker.getException("Error calling continuation", e);
                }
            } finally {
                kScope.setLock(false);
                setSessionScope(kScope);
                if (cocoon != null) {
                    cocoon.popCallContext();
                }
                Context.exit();
                Thread.currentThread().setContextClassLoader(savedClassLoader);
            }
        }
    }

    public void forwardTo(Scriptable scope, FOM_Cocoon cocoon, String uri,
                          Object bizData, FOM_WebContinuation fom_wk,
                          Redirector redirector)
    throws Exception {
        setupView(scope, cocoon, fom_wk);
        super.forwardTo(uri, bizData,
                        fom_wk == null ? null : fom_wk.getWebContinuation(),
                        redirector);
    }

    /**
     * Call the Cocoon sitemap for the given URI, sending the output of the
     * eventually matched pipeline to the specified outputstream.
     *
     * @param uri The URI for which the request should be generated.
     * @param bizData Extra data associated with the subrequest.
     * @param out An OutputStream where the output should be written to.
     * @exception Exception If an error occurs.
     */
    // package access as this is called by FOM_Cocoon
    void process(Scriptable scope, FOM_Cocoon cocoon, String uri,
                 Object bizData, OutputStream out)
    throws Exception {
        setupView(scope, cocoon, null);
        // FIXME (SW): should we deprecate this method in favor of PipelineUtil?
        PipelineUtil pipeUtil = new PipelineUtil();
        pipeUtil.processToStream(uri, bizData, out);
    }

    private void setupView(Scriptable scope, FOM_Cocoon cocoon, FOM_WebContinuation kont) {
        final Map objectModel = this.processInfoProvider.getObjectModel();

        // Make the JS live-connect objects available to the view layer
        FOM_JavaScriptFlowHelper.setPackages(objectModel,
               (Scriptable)ScriptableObject.getProperty(scope, "Packages"));
        FOM_JavaScriptFlowHelper.setJavaPackage(objectModel,
               (Scriptable)ScriptableObject.getProperty(scope, "java"));

        // Make the FOM objects available to the view layer
        FOM_JavaScriptFlowHelper.setFOM_Request(objectModel,
                                            cocoon.jsGet_request());
        FOM_JavaScriptFlowHelper.setFOM_Response(objectModel,
                                             cocoon.jsGet_response());
        Request request = ObjectModelHelper.getRequest(objectModel);
        Scriptable session = null;
        if (request.getSession(false) != null) {
            session = cocoon.jsGet_session();
        }
        FOM_JavaScriptFlowHelper.setFOM_Session(objectModel, session);

        FOM_JavaScriptFlowHelper.setFOM_Context(objectModel,
                                                cocoon.jsGet_context());
        if (kont != null) {
            FOM_JavaScriptFlowHelper.setFOM_WebContinuation(objectModel, kont);
        }
    }

    /**
     * @see org.apache.cocoon.components.flow.AbstractInterpreter#getScriptExtension()
     */
    public String getScriptExtension() {
        return ".js";
    }
}
