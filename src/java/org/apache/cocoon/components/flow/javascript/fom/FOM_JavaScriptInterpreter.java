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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.CompilingInterpreter;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.flow.InvalidContinuationException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.flow.javascript.JSErrorReporter;
import org.apache.cocoon.components.flow.javascript.ScriptablePointerFactory;
import org.apache.cocoon.components.flow.javascript.ScriptablePropertyHandler;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.NativeJavaPackage;
import org.mozilla.javascript.PropertyException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.continuations.Continuation;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.mozilla.javascript.tools.debugger.Main;
import org.mozilla.javascript.tools.shell.Global;

/**
 * Interface with the JavaScript interpreter.
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @since March 25, 2002
 * @version CVS $Id$
 */
public class FOM_JavaScriptInterpreter extends CompilingInterpreter
        implements Configurable, Initializable {

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
    public static final String USER_GLOBAL_SCOPE = "FOM JavaScript GLOBAL SCOPE/";

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
    Global scope;

    CompilingClassLoader classLoader;

    MyClassRepository javaClassRepository = new MyClassRepository();

    String[] javaSourcePath;

    /**
     * List of <code>String</code> objects that represent files to be
     * read in by the JavaScript interpreter.
     */
    List topLevelScripts = new ArrayList();

    JSErrorReporter errorReporter;
    boolean enableDebugger = false;

    /**
     * Needed to get things working with JDK 1.3. Can be removed once we
     * don't support that platform any more.
     */
    protected ServiceManager getServiceManager() {
        return manager;
    }

    class MyClassRepository implements CompilingClassLoader.ClassRepository {

        Map javaSource = new HashMap();
        Map javaClass = new HashMap();
        Map sourceToClass = new HashMap();
        Map classToSource = new HashMap();

        public synchronized void addCompiledClass(String className, Source src,
                    byte[] contents) {
            javaSource.put(src.getURI(), src.getValidity());
            javaClass.put(className, contents);
            String uri = src.getURI();
            Set set = (Set)sourceToClass.get(uri);
            if (set == null) {
                set = new HashSet();
                sourceToClass.put(uri, set);
            }
            set.add(className);
            classToSource.put(className, src.getURI());
        }

        public synchronized byte[] getCompiledClass(String className) {
            return (byte[])javaClass.get(className);
        }

        public synchronized boolean upToDateCheck() throws Exception {
            SourceResolver sourceResolver = (SourceResolver)
                getServiceManager().lookup(SourceResolver.ROLE);
            Iterator iter = javaSource.entrySet().iterator();
            List invalid = new LinkedList();
            while (iter.hasNext()) {
                Map.Entry e = (Map.Entry)iter.next();
                String uri = (String)e.getKey();
                SourceValidity validity =
                    (SourceValidity)e.getValue();
                int valid = validity.isValid();
                if (valid == SourceValidity.UNKNOWN) {
                    Source newSrc = null;
                    try {
                        newSrc = sourceResolver.resolveURI(uri);
                        valid = newSrc.getValidity().isValid(validity);
                    } catch (Exception ignored) {
                    } finally {
                        if (newSrc != null) {
                            sourceResolver.release(newSrc);
                        }
                    }
                }
                if (valid != SourceValidity.VALID) {
                    invalid.add(uri);
                }
            }
            iter = invalid.iterator();
            while (iter.hasNext()) {
                String uri = (String)iter.next();
                Set set = (Set)sourceToClass.get(uri);
                Iterator ii = set.iterator();
                while (ii.hasNext()) {
                    String className = (String)ii.next();
                    sourceToClass.remove(className);
                    javaClass.remove(className);
                    classToSource.remove(className);
                }
                set.clear();
                javaSource.remove(uri);
            }
            return invalid.size() == 0;
        }
    }

    /**
     * JavaScript debugger: there's only one of these: it can debug multiple
     * threads executing JS code.
     */
    static Main debugger;

    static synchronized Main getDebugger() {
        if (debugger == null) {
            final Main db = new Main("Cocoon Flow Debugger");
            db.pack();
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
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

    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);

        String loadOnStartup
            = config.getChild("load-on-startup", true).getValue(null);
        if (loadOnStartup != null) {
            register(loadOnStartup);
        }

        String debugger = config.getChild("debugger").getValue(null);
        if ("enabled".equalsIgnoreCase(debugger)) {
            enableDebugger = true;
        }

        if (reloadScripts) {
            String classPath = config.getChild("classpath").getValue(null);
            synchronized (javaClassRepository) {
                if (classPath != null) {
                    StringTokenizer izer = new StringTokenizer(classPath, ";");
                    int i = 0;
                    javaSourcePath = new String[izer.countTokens() + 1];
                    javaSourcePath[javaSourcePath.length - 1] = "";
                    while (izer.hasMoreTokens()) {
                        javaSourcePath[i++] = izer.nextToken();
                    }
                } else {
                    javaSourcePath = new String[]{""};
                }
                updateSourcePath();
            }
        }
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
            errorReporter = new JSErrorReporter(getLogger());
        } catch (Exception e) {
            Context.exit();
            e.printStackTrace();
            throw e;
        }
    }

    private ClassLoader getClassLoader(boolean needsRefresh) throws Exception {
        if (!reloadScripts) {
            return Thread.currentThread().getContextClassLoader();
        }
        synchronized (javaClassRepository) {
            boolean reload = needsRefresh || classLoader == null;
            if (needsRefresh && classLoader != null) {
                reload = !javaClassRepository.upToDateCheck();
            }
            if (reload) {
                classLoader = new CompilingClassLoader(
                        Thread.currentThread().getContextClassLoader(),
                        (SourceResolver)manager.lookup(SourceResolver.ROLE),
                        javaClassRepository);
                classLoader.addSourceListener(
                        new CompilingClassLoader.SourceListener() {
                            public void sourceCompiled(Source src) {
                                // no action
                            }

                            public void sourceCompilationError(Source src,
                                                               String errMsg) {

                                if (src != null) {
                                    throw Context.reportRuntimeError(errMsg);
                                }
                            }
                        });
                updateSourcePath();
            }
            return classLoader;
        }
    }

    private void updateSourcePath() {
        if (classLoader != null) {
            classLoader.setSourcePath(javaSourcePath);
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
        final Request request = ContextHelper.getRequest(this.avalonContext);

        ThreadScope scope = null;

        // Get/create the scope attached to the current context
        Session session = request.getSession(false);
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
     * {@link #getInterpreterID identifier} of this {@link Interpreter}
     * instance.
     *
     * @param scope a <code>ThreadScope</code> value
     */
    private void setSessionScope(ThreadScope scope) throws Exception {
        if (scope.useSession) {
            final String scopeID = USER_GLOBAL_SCOPE + getInterpreterID();
            final Request request = ContextHelper.getRequest(this.avalonContext);

            // FIXME: Where "session scope" should go when session is invalidated?
            // Attach the scope to the current context
            try {
                Session session = request.getSession(true);
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
        private static final String[] BUILTIN_PACKAGES = {"javax", "org", "com"};

        private ClassLoader classLoader;

        /* true if this scope has assigned any global vars */
        boolean useSession;

        boolean locked = false;

        public ThreadScope() {
            final String[] names = { "importClass" };
            try {
                defineFunctionProperties(names, ThreadScope.class,
                                         ScriptableObject.DONTENUM);
            } catch (PropertyException e) {
                throw new Error();  // should never happen
            }
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
            if (this.locked && !has(name, start) && !(value instanceof NativeJavaClass)) {
                // Need to wrap into a runtime exception as Scriptable.put has no throws clause...
                throw new WrappedException (new JavaScriptException("Implicit declaration of global variable '" + name +
                  "' forbidden. Please ensure all variables are explicitely declared with the 'var' keyword"));
            }
            this.useSession = true;
            super.put(name, start, value);
        }

        public void put(int index, Scriptable start, Object value) {
            // FIXME(SW): do indexed properties have a meaning on the global scope?
            if (this.locked && !has(index, start)) {
                throw new WrappedException(new JavaScriptException("Global scope locked. Cannot set value for index " + index));
            }
            this.useSession = true;
            super.put(index, start, value);
        }

        void reset() {
            this.useSession = false;
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
        Context context = Context.getCurrentContext();

        ThreadScope thrScope = new ThreadScope();

        thrScope.setPrototype(scope);
        // We want 'thrScope' to be a new top-level scope, so set its
        // parent scope to null. This means that any variables created
        // by assignments will be properties of "thrScope".
        thrScope.setParentScope(null);
        // Put in the thread scope the Cocoon object, which gives access
        // to the interpreter object, and some Cocoon objects. See
        // FOM_Cocoon for more details.
        Object[] args = {};
        FOM_Cocoon cocoon = (FOM_Cocoon)
            context.newObject(thrScope, "FOM_Cocoon", args);
        cocoon.setParentScope(thrScope);
        thrScope.put("cocoon", thrScope, cocoon);
        thrScope.defineProperty(LAST_EXEC_TIME,
                                new Long(0),
                                ScriptableObject.DONTENUM | ScriptableObject.PERMANENT);

        thrScope.reset();
        return thrScope;
    }

    /**
     * Returns a new Scriptable object to be used as the global scope
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
        // Try to retrieve the scope object from the session instance. If
        // no scope is found, we create a new one, but don't place it in
        // the session.
        //
        // When a user script "creates" a session using
        // cocoon.createSession() in JavaScript, the thrScope is placed in
        // the session object, where it's later retrieved from here. This
        // behaviour allows multiple JavaScript functions to share the
        // same global scope.

        FOM_Cocoon cocoon = (FOM_Cocoon)thrScope.get("cocoon", thrScope);
        long lastExecTime = ((Long)thrScope.get(LAST_EXEC_TIME,
                                                thrScope)).longValue();
        boolean needsRefresh = false;
        if (reloadScripts) {
            long now = System.currentTimeMillis();
            if (now >= lastTimeCheck + checkTime) {
                needsRefresh = true;
            }
            lastTimeCheck = now;
        }
        // We need to setup the FOM_Cocoon object according to the current
        // request. Everything else remains the same.
        ClassLoader classLoader = getClassLoader(needsRefresh);
        Thread.currentThread().setContextClassLoader(classLoader);
        thrScope.setupPackages(classLoader);
        cocoon.pushCallContext(this, redirector, manager,
                               avalonContext, getLogger(), null);

        // Check if we need to compile and/or execute scripts
        synchronized (compiledScripts) {
            List execList = new ArrayList();
            // If we've never executed scripts in this scope or
            // if reload-scripts is true and the check interval has expired
            // or if new scripts have been specified in the sitemap,
            // then create a list of scripts to compile/execute
            if (lastExecTime == 0 || needsRefresh || needResolve.size() > 0) {
                topLevelScripts.addAll(needResolve);
                if (lastExecTime != 0 && !needsRefresh) {
                    execList.addAll(needResolve);
                } else {
                    execList.addAll(topLevelScripts);
                }
                needResolve.clear();
            }
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
                    thrScope.put(LAST_EXEC_TIME, thrScope,
                                 new Long(System.currentTimeMillis()));
                    thrScope.reset();
                }
            }
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
        if (src != null) {
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
        } else {
            throw new ResourceNotFoundException(fileName + ": not found");
        }
    }

    protected Script compileScript(Context cx, Scriptable scope, Source src)
            throws Exception {
        InputStream is = src.getInputStream();
        if (is != null) {
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is));
                Script compiledScript = cx.compileReader(scope, reader,
                        src.getURI(), 1, null);
                return compiledScript;
            } finally {
                is.close();
            }
        } else {
            throw new ResourceNotFoundException(src.getURI() + ": not found");
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
     * @param redirector
     * @exception Exception if an error occurs
     */
    public void callFunction(String funName, List params, Redirector redirector)
    throws Exception {
        Context context = Context.enter();
        context.setOptimizationLevel(OPTIMIZATION_LEVEL);
        context.setGeneratingDebug(true);
        context.setCompileFunctionsWithDynamicScope(true);
        context.setErrorReporter(errorReporter);

        ThreadScope thrScope = getSessionScope();
        synchronized (thrScope) {
            ClassLoader savedClassLoader =
                Thread.currentThread().getContextClassLoader();
            FOM_Cocoon cocoon = null;
            try {
                try {
                    setupContext(redirector, context, thrScope);
                    cocoon = (FOM_Cocoon)thrScope.get("cocoon", thrScope);

                    // Register the current scope for scripts indirectly called from this function
                    FOM_JavaScriptFlowHelper.setFOM_FlowScope(cocoon.getObjectModel(), thrScope);

                    if (enableDebugger) {
                        if (!getDebugger().isVisible()) {
                            // only raise the debugger window if it isn't already visible
                            getDebugger().setVisible(true);
                        }
                    }

                    int size = (params != null ? params.size() : 0);
                    Object[] funArgs = new Object[size];
                    Scriptable parameters = context.newObject(thrScope);
                    for (int i = 0; i < size; i++) {
                        Interpreter.Argument arg = (Interpreter.Argument)params.get(i);
                        funArgs[i] = arg.value;
                        if (arg.name == null) {
                            arg.name = "";
                        }
                        parameters.put(arg.name, parameters, arg.value);
                    }
                    cocoon.setParameters(parameters);

                    Object fun = ScriptableObject.getProperty(thrScope, funName);
                    if (fun == Scriptable.NOT_FOUND) {
                        throw new ResourceNotFoundException("Function \"javascript:" + funName + "()\" not found");
                    }
                    thrScope.setLock(true);
                    ScriptRuntime.call(context, fun, thrScope, funArgs, thrScope);
                } catch (JavaScriptException ex) {
                    EvaluatorException ee = Context.reportRuntimeError(
                                                                       ToolErrorReporter.getMessage("msg.uncaughtJSException",
                                                                                                    ex.getMessage()));
                    Throwable unwrapped = unwrap(ex);
                    if (unwrapped instanceof ProcessingException) {
                        throw (ProcessingException) unwrapped;
                    }
                    throw new CascadingRuntimeException(ee.getMessage(),
                                                        unwrapped);
                } catch (EcmaError ee) {
                    String msg = ToolErrorReporter.getMessage("msg.uncaughtJSException", ee.toString());
                    if (ee.getSourceName() != null) {
                        Context.reportRuntimeError(msg, ee.getSourceName(),
                                                   ee.getLineNumber(), ee.getLineSource(),
                                                   ee.getColumnNumber());
                    } else {
                        Context.reportRuntimeError(msg);
                    }
                    throw new CascadingRuntimeException(ee.getMessage(), ee);
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

        // Obtain the continuation object from it, and setup the
        // FOM_Cocoon object associated in the dynamic scope of the saved
        // continuation with the environment and context objects.
        Continuation k = (Continuation)wk.getContinuation();
        ThreadScope kScope = (ThreadScope)k.getParentScope();
        synchronized (kScope) {
            ClassLoader savedClassLoader =
                Thread.currentThread().getContextClassLoader();
            FOM_Cocoon cocoon = null;
            try {
                Thread.currentThread().setContextClassLoader(kScope.getClassLoader());
                cocoon = (FOM_Cocoon)kScope.get("cocoon", kScope);
                kScope.setLock(true);
                cocoon.pushCallContext(this, redirector, manager,
                                       avalonContext,
                                       getLogger(), wk);

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
                fom_wk.setParentScope(kScope);
                fom_wk.setPrototype(ScriptableObject.getClassPrototype(kScope,
                                                                       fom_wk.getClassName()));
                Object[] args = new Object[] {k, fom_wk};
                try {
                    ScriptableObject.callMethod(cocoon,
                                                "handleContinuation", args);
                } catch (JavaScriptException ex) {
                    EvaluatorException ee = Context.reportRuntimeError(
                                                                       ToolErrorReporter.getMessage("msg.uncaughtJSException",
                                                                                                    ex.getMessage()));
                    Throwable unwrapped = unwrap(ex);
                    if (unwrapped instanceof ProcessingException) {
                        throw (ProcessingException)unwrapped;
                    }
                    throw new CascadingRuntimeException(ee.getMessage(),
                                                        unwrapped);
                } catch (EcmaError ee) {
                    String msg = ToolErrorReporter.getMessage("msg.uncaughtJSException", ee.toString());
                    if (ee.getSourceName() != null) {
                        Context.reportRuntimeError(msg, ee.getSourceName(),
                                                   ee.getLineNumber(), ee.getLineSource(),
                                                   ee.getColumnNumber());
                    } else {
                        Context.reportRuntimeError(msg);
                    }
                    throw new CascadingRuntimeException(ee.getMessage(), ee);
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

    public void forwardTo(Scriptable scope, FOM_Cocoon cocoon, String uri,
                          Object bizData, FOM_WebContinuation fom_wk,
                          Redirector redirector)
    throws Exception {
        setupView(scope, cocoon, fom_wk);
        super.forwardTo(uri, bizData,
                        fom_wk == null ? null : fom_wk.getWebContinuation(),
                        redirector);
    }

    // package access as this is called by FOM_Cocoon
    void process(Scriptable scope, FOM_Cocoon cocoon, String uri,
                 Object bizData, OutputStream out)
    throws Exception {
        setupView(scope, cocoon, null);
        super.process(uri, bizData, out);
    }

    private void setupView(Scriptable scope, FOM_Cocoon cocoon, FOM_WebContinuation kont) {
        Map objectModel = ContextHelper.getObjectModel(this.avalonContext);

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
}
