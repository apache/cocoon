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
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.flow.java.AbstractContinuable;
import org.apache.cocoon.components.flow.java.Continuable;
import org.apache.cocoon.components.flow.java.ContinuationHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeGlobal;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.NativeJavaPackage;
import org.mozilla.javascript.PropertyException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.mozilla.javascript.tools.shell.Global;

/**
 * Interface with the JavaScript interpreter.
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @since March 25, 2002
 * @version CVS $Id: JavaScriptHelper.java,v 1.3 2004/06/28 08:28:38 stephan Exp $
 */
public class JavaScriptHelper extends AbstractContinuable
    implements ScriptHelper {
	
	/**
     * List of source locations that need to be resolved.
     */
    protected ArrayList needResolve = new ArrayList();
    
    /**
     * Whether reloading of scripts should be done. Specified through
     * the "reload-scripts" attribute in <code>flow.xmap</code>.
     */
    protected boolean reloadScripts;

    /**
     * Interval between two checks for modified script files. Specified
     * through the "check-time" XML attribute in <code>flow.xmap</code>.
     */
    protected long checkTime;
	
	/** A source resolver */
    protected SourceResolver sourceresolver;
    /**
     * Mapping of String objects (source uri's) to ScriptSourceEntry's
     */
    protected Map compiledScripts = new HashMap();

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
    public static final String USER_GLOBAL_SCOPE = "FOM JavaScript GLOBAL SCOPE";

    // This is the only optimization level that supports continuations
    // in the Christoper Oliver's Rhino JavaScript implementation
    public static int OPTIMIZATION_LEVEL = -2;

    /**
     * When was the last time we checked for script modifications. Used
     * only if {@link #reloadScripts} is true.
     */
    protected long lastTimeCheck = 0;

    /**
     * Shared global scope for scripts and other immutable objects
     */
    private Global scope;

    /**
     * List of <code>String</code> objects that represent files to be
     * read in by the JavaScript interpreter.
     */
    private List topLevelScripts = new ArrayList();

    private JSErrorReporter errorReporter;
    private boolean enableDebugger = false;
    
    /**
     * Registers a source file with the interpreter. Using this method
     * an implementation keeps track of all the script files which are
     * compiled. This allows them to reload the script files which get
     * modified on the file system.
     *
     * <p>The parsing/compilation of a script file by an interpreter
     * happens in two phases. In the first phase the file's location is
     * registered in the <code>needResolve</code> array.
     *
     * <p>The second is possible only when a Cocoon
     * <code>Environment</code> is passed to the Interpreter. This
     * allows the file location to be resolved using Cocoon's
     * <code>SourceFactory</code> class.
     *
     * <p>Once a file's location can be resolved, it is removed from the
     * <code>needResolve</code> array and placed in the
     * <code>scripts</code> hash table. The key in this hash table is
     * the file location string, and the value is a
     * DelayedRefreshSourceWrapper instance which keeps track of when
     * the file needs to re-read.
     *
     * @param source the location of the script
     *
     * @see org.apache.cocoon.environment.Environment
     * @see org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper
     */
    public void register(String source)
    {
    	System.out.println("register source "+source);
        synchronized(this) {
            needResolve.add(source);
        }
    }

    public void configure(Configuration config) throws ConfigurationException {
        
        reloadScripts = config.getChild("reload-scripts").getValueAsBoolean(false);
        checkTime = config.getChild("check-time").getValueAsLong(1000L);

        String loadOnStartup
            = config.getChild("load-on-startup", true).getValue(null);
        if (loadOnStartup != null) {
            register(loadOnStartup);
        }

        String debugger = config.getChild("debugger").getValue(null);
        if ("enabled".equalsIgnoreCase(debugger)) {
            enableDebugger = true;
        }
    }

    public void initialize() throws Exception {
        if (enableDebugger) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Flow debugger enabled, creating");
            }
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
            NativeGlobal.init(context, scope, false);
            
            scope = new Global(context);
            // Access to Cocoon internal objects
            errorReporter = new JSErrorReporter(getLogger());
        } catch (Exception e) {
            Context.exit();
            e.printStackTrace();
            throw e;
        }
        
        sourceresolver = (SourceResolver)getServiceManager().lookup(SourceResolver.ROLE);
    }

    /**
     * Returns the JavaScript scope, a Scriptable object, from the user
     * session instance. Each sitemap can have a scope associated with it.
     *
     * @return a <code>Scriptable</code> value
     */
    private ThreadScope getSessionScope() throws Exception {
        Request request = getRequest();
        ThreadScope scope = null;
        Session session = request.getSession(false);
        if (session != null) {
            HashMap userScopes =
                    (HashMap)session.getAttribute(USER_GLOBAL_SCOPE);
            if (userScopes != null) {
                // Get the scope attached to the current context
                scope = (ThreadScope)userScopes.get(getSitemapPath());
            }
        }
        if (scope == null) {
            scope = createThreadScope();
        }
        return scope;
    }

    void updateSession(Scriptable scope) throws Exception {
        ThreadScope thrScope = (ThreadScope)scope;
        if (thrScope.useSession) {
            setSessionScope(scope);
        }
    }

    /**
     * Associates a JavaScript scope, a Scriptable object, with the
     * directory path of the current sitemap, as resolved by the
     * source resolver.
     *
     * @param scope a <code>Scriptable</code> value
     */
    private Scriptable setSessionScope(Scriptable scope) throws Exception {
        Request request = getRequest();

        // FIXME: Where "session scope" should go when session is invalidated?
        try {
            Session session = request.getSession(true);

            HashMap userScopes = (HashMap)session.getAttribute(USER_GLOBAL_SCOPE);
            if (userScopes == null) {
                userScopes = new HashMap();
                session.setAttribute(USER_GLOBAL_SCOPE, userScopes);
            }

            // Attach the scope to the current context
            userScopes.put(getSitemapPath(), scope);
        } catch (IllegalStateException e) {
            // Session might be invalidated already.
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Got '" + e + "' while trying to set session scope.", e);
            }
        }
        return scope;
    }

    private String getSitemapPath() throws Exception {
        Source src = this.sourceresolver.resolveURI(".");
        try {
            return src.getURI();
        } finally {
            this.sourceresolver.release(src);
        }
    }

    public static class ThreadScope extends ScriptableObject {
        static final String[] builtinPackages = {"javax", "org", "com"};

        ClassLoader classLoader;

        /* true if this scope has assigned any global vars */
        public boolean useSession = false;

        public ThreadScope() {
            final String[] names = { "importClass"};

            try {
                this.defineFunctionProperties(names, ThreadScope.class,
                                              ScriptableObject.DONTENUM);
            } catch (PropertyException e) {
                throw new Error();  // should never happen
            }
        }

        public String getClassName() {
            return "ThreadScope";
        }

        public void put(String name, Scriptable start, Object value) {
            useSession = true;
            super.put(name, start, value);
        }

        public void put(int index, Scriptable start, Object value) {
            useSession = true;
            super.put(index, start, value);
        }

        public void reset() {
            useSession = false;
        }

        // Override importClass to allow reloading of classes
        public static void importClass(Context cx, Scriptable thisObj,
                                       Object[] args, Function funObj) {
            for (int i = 0; i < args.length; i++) {
                Object cl = args[i];
                if (!(cl instanceof NativeJavaClass)) {
                    throw Context.reportRuntimeError("Not a Java class: " +
                                                       Context.toString(cl));
                }
                String s = ((NativeJavaClass) cl).getClassObject().getName();
                String n = s.substring(s.lastIndexOf('.')+1);
                thisObj.put(n, thisObj, cl);
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
                for (int i = 0; i < builtinPackages.length; i++) {
                    String pkgName = builtinPackages[i];
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
        //Context context = Context.getCurrentContext();

        ThreadScope thrScope = new ThreadScope();

        thrScope.setPrototype(scope);
        // We want 'thrScope' to be a new top-level scope, so set its
        // parent scope to null. This means that any variables created
        // by assignments will be properties of "thrScope".
        thrScope.setParentScope(null);
        // Put in the thread scope the Cocoon object, which gives access
        // to the interpreter object, and some Cocoon objects. See
        // FOM_Cocoon for more details.
        //Object[] args = {};
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
        //Thread.currentThread().setContextClassLoader(classLoader);
        //thrScope.setupPackages(classLoader);

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
        
        thrScope.put("cocoon", thrScope, new ContinuationHelper());
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
    public void callFunction(String funName, List params) throws Exception {
        Context context = Context.enter();
        context.setOptimizationLevel(OPTIMIZATION_LEVEL);
        context.setGeneratingDebug(true);
        context.setCompileFunctionsWithDynamicScope(true);
        context.setErrorReporter(errorReporter);
        ThreadScope thrScope = getSessionScope();
        synchronized (thrScope) {
            ClassLoader savedClassLoader =
                Thread.currentThread().getContextClassLoader();
            try {
                try {
                    setupContext(getRedirector(), context, thrScope);

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
                    Object fun = ScriptableObject.getProperty(thrScope, funName);
                    if (fun == Scriptable.NOT_FOUND) {
                        throw new ResourceNotFoundException("Function \"javascript:" + funName + "()\" not found");
                    }
                    ScriptRuntime.call(context, fun, thrScope, funArgs, thrScope);
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
                updateSession(thrScope);
                Context.exit();
                Thread.currentThread().setContextClassLoader(savedClassLoader);
            }
        }
    }

    /*public void handleContinuation(String id, List params,
                                   Redirector redirector) throws Exception
    {
        WebContinuation wk = continuationsMgr.lookupWebContinuation(id);

        if (wk == null) {
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
                cocoon.pushCallContext(this, redirector, getServiceManager(),
                                       getAvalonContext(),
                                       getLogger(), wk);

                // Register the current scope for scripts indirectly called from this function
                FOM_JavaScriptFlowHelper.setFOM_FlowScope(cocoon.getObjectModel(), kScope);

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
                updateSession(kScope);
                if (cocoon != null) {
                    cocoon.popCallContext();
                }
                Context.exit();
                Thread.currentThread().setContextClassLoader(savedClassLoader);
            }
        }
    }*/

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

    /*private void setupView(Scriptable scope, FOM_Cocoon cocoon, FOM_WebContinuation kont) {
        Map objectModel = ContextHelper.getObjectModel(getAvalonContext());

        // Make the JS live-connect objects available to the view layer
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
    }*/
    
    protected class ScriptSourceEntry implements Continuable {
        final private Source source;
        private Script script;
        private long compileTime;

        public ScriptSourceEntry(Source source) {
            this.source = source;
        }

        public ScriptSourceEntry(Source source, Script script, long t) {
            this.source = source;
            this.script = script;
            this.compileTime = t;
        }

        public Source getSource() {
            return source;
        }

        public Script getScript(Context context, Scriptable scope,
                                boolean refresh, JavaScriptHelper interpreter)
            throws Exception {
            if (refresh) {
                source.refresh();
            }
            if (script == null || compileTime < source.getLastModified()) {
                script = interpreter.compileScript(context, scope, source);
                compileTime = source.getLastModified();
            }
            return script;
        }
    }
}
