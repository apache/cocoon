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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.AbstractInterpreter;
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
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.continuations.Continuation;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.mozilla.javascript.tools.shell.Global;

/**
 * <p>Interface with the JavaScript interpreter.</p>
 * <p>This version of the JavaScript interpreter provides enhanced
 *    functionality and supports interception.</p>
 * 
 * <p>Changes:
 *   <ul>
 *     <li>Use of the AO_FOM_Cocoon object encapsulating the Cocoon object.
 *         All references to the FOM_Cocoon object had to be changed.
 *     </li>
 *     <li>Additional configurations</li>
 *     <li>adding the <code>JavaScriptAspectWeaver</code> to the SourceEntry
 *         object if interceptions are enabled</li>
 *    </ul>
 * </p>
 * 
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @author <a href="mailto:coliver@apache.org">Christopher Oliver</a>  
 * @author <a href="mailto:reinhard@apache.org">Reinhard Pötz</a> 
 * @since 2.1
 * @version CVS $Id: AO_FOM_JavaScriptInterpreter.java,v 1.10 2004/05/26 01:31:06 joerg Exp $
 */
public class AO_FOM_JavaScriptInterpreter extends AbstractInterpreter
    implements Serviceable, Configurable, Initializable
{

    private SourceResolver sourceResolver;

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
    static int OPTIMIZATION_LEVEL = -2;

    // the postfix of the resulting file streamed into the same directory as
    // the basescript
    // (RPO) added/changed by interception layer        
    public static final String INTERCEPTION_POSTFIX = "_intercepted.js";
    // end
         
    /**
     * When was the last time we checked for script modifications. Used
     * only if {@link #reloadScripts} is true.
     */
    protected long lastTimeCheck = 0;

    // (RPO) added/changed by interception layer      
    /**
     * Are interceptions enabled?
     */
    private boolean isInterceptionEnabled;
    // end

    /**
     * Shared global scope for scripts and other immutable objects
     */
    Global scope;


    /**
     * List of <code>String</code> objects that represent files to be
     * read in by the JavaScript interpreter.
     */
    List topLevelScripts = new ArrayList();

    class ScriptSourceEntry {
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
        // (RPO) added/changed by interception layer    
        private JavaScriptAspectWeaver aspectWeaver = null;

        public void setAspectWeaver( JavaScriptAspectWeaver aspectWeaver ) {
            this.aspectWeaver = aspectWeaver;
        }

        public Script getScript(Context context, Scriptable scope,
                                             boolean refresh)
            throws Exception {
            if (refresh) {
                source.refresh();
            }
            if (script == null || compileTime < source.getLastModified()) {           
                script = compileScript(context, scope, source, aspectWeaver);                    
                compileTime = source.getLastModified();
            }
            return script;
        }
        //  -- end        
    }
    
    /**
     * Mapping of String objects (source uri's) to ScriptSourceEntry's
     *
     */
    Map compiledScripts = new HashMap();
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

    // (RPO) added by interception layer       
    Configuration stopExecutionFunctionsConf = null;
    boolean serializeResultScript = false;
    // --end
    
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.sourceResolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);
    }

    public void configure(Configuration config) 
        throws ConfigurationException {
            
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
        
        // (RPO) added by interception layer    
        isInterceptionEnabled = 
            config.getChild( "enable-interception" ).getValueAsBoolean( true );
        stopExecutionFunctionsConf = 
            config.getChild( "cont-creating-functions" );
        serializeResultScript = 
           config.getChild( "serialize-result-script" ).getValueAsBoolean( false );
        // --end
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
            scope = new Global(context);
            // Access to Cocoon internal objects
            AO_FOM_Cocoon.init(scope);
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
     * @return a <code>Scriptable</code> value
     */
    private Scriptable getSessionScope()
        throws Exception {
        Request request = ContextHelper.getRequest(avalonContext);
        Scriptable scope = null;
        Session session = request.getSession(false);
        if (session != null) {
            HashMap userScopes = (HashMap)session.getAttribute(USER_GLOBAL_SCOPE);
            if (userScopes != null) {
                // Get context prefix
                Source src = this.sourceResolver.resolveURI(".");
                String uriPrefix = src.getURI();
                this.sourceResolver.release(src);
                scope = (Scriptable)userScopes.get(uriPrefix);
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
     * Associates a JavaScript scope, a Scriptable object, with the URI
     * prefix of the current sitemap, as returned by the {@link
     * org.apache.cocoon.environment.Environment#getURIPrefix} method.
     *
     * @param scope a <code>Scriptable</code> value
     */
    private Scriptable setSessionScope(Scriptable scope)
        throws Exception {
        Request request = ContextHelper.getRequest(this.avalonContext);
        Session session = request.getSession(true);

        HashMap userScopes = (HashMap)session.getAttribute(USER_GLOBAL_SCOPE);
        if (userScopes == null) {
            userScopes = new HashMap();
            session.setAttribute(USER_GLOBAL_SCOPE, userScopes);
        }
        // Get context prefix
        Source src = this.sourceResolver.resolveURI(".");
        String uriPrefix = src.getURI();
        this.sourceResolver.release(src);
        userScopes.put(uriPrefix, scope);
        return scope;
    }

    public static class ThreadScope extends ScriptableObject {

        /* true if this scope has assigned any global vars */
        boolean useSession = false;

        public ThreadScope() {
        }

        public String getClassName() {
            return "ThreadScope";
        }
        
        public void put(String name, Scriptable start,
                        Object value) {
            useSession = true;
            super.put(name, start, value);
        }
        
        public void put(int index, Scriptable start,
                        Object value) {
            useSession = true;
            super.put(index, start, value);
        }
        
        void reset() {
            useSession = false;
        }
    }

    private Scriptable createThreadScope() 
        throws Exception {
        org.mozilla.javascript.Context context = 
            org.mozilla.javascript.Context.getCurrentContext();

        ThreadScope thrScope = new ThreadScope();

        thrScope.setPrototype(scope);
        // We want 'thrScope' to be a new top-level scope, so set its
        // parent scope to null. This means that any variables created
        // by assignments will be properties of "thrScope".
        thrScope.setParentScope(null);
        
        // Put in the thread scope the Cocoon object, which gives access
        // to the interpreter object, and some Cocoon objects. See
        // FOM_Cocoon for more details.
        Object args[] = {};
        AO_FOM_Cocoon cocoon = (AO_FOM_Cocoon)
            context.newObject(thrScope, "AO_FOM_Cocoon", args);
        cocoon.setParentScope(thrScope);
        thrScope.put("cocoon", thrScope, cocoon);
        ((ScriptableObject)thrScope).defineProperty(LAST_EXEC_TIME,
                                                    new Long(0),
                                                    ScriptableObject.DONTENUM |
                                                    ScriptableObject.PERMANENT);
        
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
    private void setupContext(Redirector redirector,
                              org.mozilla.javascript.Context context,
                              Scriptable thrScope)
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

        AO_FOM_Cocoon cocoon = (AO_FOM_Cocoon)thrScope.get("cocoon", thrScope);
        long lastExecTime = ((Long)thrScope.get(LAST_EXEC_TIME,
                                                thrScope)).longValue();
        // We need to setup the FOM_Cocoon object according to the current
        // request. Everything else remains the same.
        cocoon.setup( this, redirector, avalonContext, manager, getLogger());
        
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
                    Source src = this.sourceResolver.resolveURI(sourceURI);
                    entry = new ScriptSourceEntry(src);
                    compiledScripts.put(sourceURI, entry);
                }
                // (RPO) added/changed by interception layer            
                // add interception support
                if( isInterceptionEnabled ) {
                    JavaScriptAspectWeaver aspectWeaver = new JavaScriptAspectWeaver();
                    aspectWeaver.enableLogging( this.getLogger() );
                    aspectWeaver.service(this.manager);
                    aspectWeaver.setSerializeResultScriptParam( this.serializeResultScript );
                    aspectWeaver.setStopExecutionFunctionsConf( this.stopExecutionFunctionsConf );
                    entry.setAspectWeaver( aspectWeaver );
                }        
                // --end           
                // Compile the script if necessary
                entry.getScript(context, this.scope, needsRefresh);
            }
            // Execute the scripts if necessary
            for (int i = 0, size = execList.size(); i < size; i++) {
                String sourceURI = (String)execList.get(i);
                ScriptSourceEntry entry =
                    (ScriptSourceEntry)compiledScripts.get(sourceURI);
                long lastMod = entry.getSource().getLastModified();
                Script script = entry.getScript(context, this.scope, false);
                if (lastExecTime == 0 || lastMod > lastExecTime) {
                    script.exec(context, thrScope);
                    thrScope.put(LAST_EXEC_TIME, thrScope,
                                 new Long(System.currentTimeMillis()));
                    ((ThreadScope)thrScope).reset();
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
    Script compileScript(Context cx,
                         String fileName) throws Exception {
        Source src = sourceResolver.resolveURI(fileName);
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
            }
            compiledScript = entry.getScript(cx, this.scope, false);
            return compiledScript;
        }
    }
    // (RPO) added/changed by interception layer   
    protected Script compileScript( Context cx, Scriptable scope, 
                                  Source src, JavaScriptAspectWeaver aspectWeaver)
        throws Exception {
        Script compiledScript = null;

        boolean areScriptsApplied = false;

        // test base script if scripts are applied to
        if( aspectWeaver != null ) {
            aspectWeaver.setBaseScript( src );      
            areScriptsApplied = aspectWeaver.areScriptsApplied();      
        }

        // no scripts applied or compileScript was called with null AspectWeaver
        if( aspectWeaver == null || ! areScriptsApplied ) {
            InputStream is = src.getInputStream();
            if (is == null) {
                throw new ResourceNotFoundException(src.getURI() + ": not found");
            }
            Reader reader = new BufferedReader(new InputStreamReader(is));
            compiledScript = cx.compileReader(scope, reader, src.getURI(), 1, null );
        }
        // script applied
        else {
            this.getLogger().info( "Adding interceptions to script " + src.getURI() );
            Reader reader = new BufferedReader( 
                                    aspectWeaver.getInterceptedScriptAsReader() );
            compiledScript = cx.compileReader(scope, reader, src.getURI() + INTERCEPTION_POSTFIX, 1, null );            
        }
        return compiledScript;
    }
    // -- end
    
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
    public void callFunction(String funName, List params,
                             Redirector redirector)
        throws Exception
    {
        Context context = Context.enter();
        context.setOptimizationLevel(OPTIMIZATION_LEVEL);
        context.setGeneratingDebug(true);
        context.setCompileFunctionsWithDynamicScope(true);
        context.setErrorReporter(errorReporter);
        AO_FOM_Cocoon cocoon = null;
        Scriptable thrScope = getSessionScope();
        synchronized (thrScope) {
            try {
                setupContext(redirector, context, thrScope);
                cocoon = (AO_FOM_Cocoon)thrScope.get("cocoon", thrScope);
                if (enableDebugger) {
                    if (!getDebugger().isVisible()) {
                        // only raise the debugger window if it isn't already visible
                        getDebugger().setVisible(true);
                    }
                }
                int size = (params != null ? params.size() : 0);
                Object[] funArgs = new Object[size];
                Scriptable parameters = context.newObject(thrScope);
                if (size != 0) {
                    for (int i = 0; i < size; i++) {
                        Interpreter.Argument arg = (Interpreter.Argument)params.get(i);
                        funArgs[i] = arg.value;
                        if (arg.name == null) arg.name = "";
                        parameters.put(arg.name, parameters, arg.value);
                    }
                }
                cocoon.setParameters(parameters);
                Object fun = ScriptableObject.getProperty(thrScope, funName);
                if (fun == Scriptable.NOT_FOUND) {
                    throw new ResourceNotFoundException("Function \"javascript:"+funName+ "()\" not found");
                }
                ScriptRuntime.call(context, fun, thrScope, 
                                   funArgs, thrScope);
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
                updateSession(thrScope);
                if (cocoon != null) cocoon.invalidate();
                Context.exit();
            }
        }
    }

    public void handleContinuation(String id, List params,
                                   Redirector redirector)
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

        // Obtain the continuation object from it, and setup the
        // FOM_Cocoon object associated in the dynamic scope of the saved
        // continuation with the environment and context objects.
        Continuation k = (Continuation)wk.getContinuation();
        Scriptable kScope = k.getParentScope();
        synchronized (kScope) {
            AO_FOM_Cocoon cocoon = (AO_FOM_Cocoon)kScope.get("cocoon", kScope);
            cocoon.setup(this, redirector, avalonContext, manager, getLogger());
            if (enableDebugger) {
                getDebugger().setVisible(true);
            }
            int size = (params != null ? params.size() : 0);
            Scriptable parameters = context.newObject(kScope);
            if (size != 0) {
                for (int i = 0; i < size; i++) {
                    Interpreter.Argument arg = (Interpreter.Argument)params.get(i);
                    parameters.put(arg.name, parameters, arg.value);
                }
            }
            cocoon.setParameters(parameters);
            FOM_WebContinuation fom_wk = 
                new FOM_WebContinuation(wk);
            fom_wk.setParentScope(kScope);
            fom_wk.setPrototype(ScriptableObject.getClassPrototype(kScope, 
                                                                   fom_wk.getClassName()));
                                  
            Object[] args = new Object[] {k, fom_wk};

            try {
                ScriptableObject.callMethod(cocoon, 
                                            "handleContinuation", 
                                            args);
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
                updateSession(kScope);
                cocoon.invalidate();
                Context.exit();
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

    public void forwardTo(Scriptable scope, AO_FOM_Cocoon cocoon,
                          String uri, Object bizData,
                          FOM_WebContinuation fom_wk,
                          Redirector redirector)
        throws Exception {
        setupView(scope, cocoon, fom_wk);
        super.forwardTo(uri, bizData, 
                        fom_wk == null ? null :
                           fom_wk.getWebContinuation(), 
                        redirector);
    }

    // package access as this is called by FOM_Cocoon
    void process(Scriptable scope, AO_FOM_Cocoon cocoon,
                    String uri, Object bizData, 
                    OutputStream out)
        throws Exception {
        setupView(scope, cocoon, null);
        super.process(uri, bizData, out);
    }
    
    private void setupView(Scriptable scope,
                           AO_FOM_Cocoon cocoon,
                           FOM_WebContinuation kont) {
        Map objectModel = ContextHelper.getObjectModel(this.avalonContext);
        // Make the JS live-connect objects available to the view layer
        FOM_JavaScriptFlowHelper.setPackages(objectModel,
                                   (Scriptable)ScriptableObject.getProperty(scope,
                                                                            "Packages"));
        FOM_JavaScriptFlowHelper.setJavaPackage(objectModel,
                                                (Scriptable)ScriptableObject.getProperty(scope,
                                                                                         "java"));
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
            FOM_JavaScriptFlowHelper.setFOM_WebContinuation(objectModel, 
                                                            kont);
        }
    }

}
