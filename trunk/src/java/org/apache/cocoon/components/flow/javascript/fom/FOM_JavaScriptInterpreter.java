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
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.flow.AbstractInterpreter;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.flow.InvalidContinuationException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.flow.javascript.JSErrorReporter;
import org.apache.cocoon.components.flow.javascript.ScriptablePointerFactory;
import org.apache.cocoon.components.flow.javascript.ScriptablePropertyHandler;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.excalibur.source.Source;
import org.mozilla.javascript.*;
import org.mozilla.javascript.continuations.Continuation;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.mozilla.javascript.tools.shell.Global;
import org.apache.cocoon.components.flow.javascript.fom.FOM_Cocoon.FOM_WebContinuation;
/**
 * Interface with the JavaScript interpreter.
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @since March 25, 2002
 * @version CVS $Id: FOM_JavaScriptInterpreter.java,v 1.2 2003/07/14 09:54:13 reinhard Exp $
 */
public class FOM_JavaScriptInterpreter extends AbstractInterpreter
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
    public static final String USER_GLOBAL_SCOPE = "FOM JavaScript GLOBAL SCOPE";

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

        public Script getScript(Context context, Scriptable scope,
                                             boolean refresh)
            throws Exception {
            if (refresh) {
                source.refresh();
            }
            if (script == null || compileTime < source.getLastModified()) {
                script = compileScript(context, scope, source);
                compileTime = source.getLastModified();
            }
            return script;
        }
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
            scope = new Global(context);
            // Access to Cocoon internal objects
            FOM_Cocoon.init(scope);
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
    private Scriptable getSessionScope(Environment environment)
        throws Exception {
        Map objectModel = environment.getObjectModel();
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession(true);
        Scriptable scope = null;
        HashMap userScopes = (HashMap)session.getAttribute(USER_GLOBAL_SCOPE);
        if (userScopes != null) {
            String uriPrefix = environment.getURIPrefix();
            scope = (Scriptable)userScopes.get(uriPrefix);
        }
        if (scope == null) {
            return setSessionScope(environment, createThreadScope());
        }
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
    private Scriptable setSessionScope(Environment environment, Scriptable scope)
        throws Exception {
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
        return scope;
    }

    private Scriptable createThreadScope() 
        throws Exception {
        org.mozilla.javascript.Context context = 
            org.mozilla.javascript.Context.getCurrentContext();

        Scriptable thrScope = context.newObject(scope);

        thrScope.setPrototype(scope);
        // We want 'thrScope' to be a new top-level scope, so set its
        // parent scope to null. This means that any variables created
        // by assignments will be properties of "thrScope".
        thrScope.setParentScope(null);
        
        // Put in the thread scope the Cocoon object, which gives access
        // to the interpreter object, and some Cocoon objects. See
        // FOM_Cocoon for more details.
        Object args[] = {};
        FOM_Cocoon cocoon = (FOM_Cocoon)
            context.newObject(thrScope, "FOM_Cocoon", args);
        cocoon.setParentScope(thrScope);
        thrScope.put("cocoon", thrScope, cocoon);
        ((ScriptableObject)thrScope).defineProperty(LAST_EXEC_TIME,
                                                    new Long(0),
                                                    ScriptableObject.DONTENUM |
                                                    ScriptableObject.PERMANENT);
        
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
     * @param environment an <code>Environment</code> value
     * @return a <code>Scriptable</code> value
     * @exception Exception if an error occurs
     */
    private void setupContext(Environment environment,
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

        FOM_Cocoon cocoon = (FOM_Cocoon)thrScope.get("cocoon", thrScope);
        long lastExecTime = ((Long)thrScope.get(LAST_EXEC_TIME,
                                           thrScope)).longValue();
        // We need to setup the FOM_Cocoon object according to the current
        // request. Everything else remains the same.
        cocoon.setup(this, environment, manager, getLogger());

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
                if (!needsRefresh) {
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
                    Source src = environment.resolveURI(sourceURI);
                    entry = new ScriptSourceEntry(src);
                    compiledScripts.put(sourceURI, entry);
                }
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
                }
            }
        }
    }

    /**
     * Compile filename as JavaScript code
     * 
     * @param cx Rhino context
     * @param environment source resolver
     * @param fileName resource uri
     * @return compiled script
     */
    public Script compileScript(Context cx,
                                Environment environment,
                                String fileName) throws Exception {
        Source src = environment.resolveURI(fileName);
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

    private Script compileScript(Context cx, Scriptable scope,
                                 Source src) throws Exception {
        InputStream is = src.getInputStream();
        if (is == null) {
            throw new ResourceNotFoundException(src.getURI() + ": not found");
        }
        Reader reader = new BufferedReader(new InputStreamReader(is));
        Script compiledScript = cx.compileReader(scope, reader,
                                                 src.getURI(),
                                                 1, null);
        return compiledScript;
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
        Context context = Context.enter();
        context.setOptimizationLevel(OPTIMIZATION_LEVEL);
        context.setGeneratingDebug(true);
        context.setCompileFunctionsWithDynamicScope(true);
        context.setErrorReporter(errorReporter);
        FOM_Cocoon cocoon = null;
        Scriptable thrScope = getSessionScope(environment);
        synchronized (thrScope) {
            try {
                setupContext(environment, context, thrScope);
                cocoon = (FOM_Cocoon)thrScope.get("cocoon", thrScope);
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
                    fun = funName; // this will produce a better error message
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
                cocoon.invalidate();
                Context.exit();
            }
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

        // Obtain the continuation object from it, and setup the
        // FOM_Cocoon object associated in the dynamic scope of the saved
        // continuation with the environment and context objects.
        Continuation k = (Continuation)wk.getContinuation();
        Scriptable kScope = k.getParentScope();
        synchronized (kScope) {
            FOM_Cocoon cocoon = (FOM_Cocoon)kScope.get("cocoon", kScope);
            cocoon.setup(this, environment, manager, getLogger());
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
            Object[] args = new Object[] {k, 
                                          cocoon.makeWebContinuation(wk)};
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

    public void forwardTo(Scriptable scope, FOM_Cocoon cocoon,
                          String uri, Object bizData,
                          WebContinuation continuation,
                          Environment environment)
        throws Exception {
        setupView(scope, cocoon , environment, 
                  cocoon.makeWebContinuation(continuation));
        super.forwardTo(uri, bizData, continuation, environment);
    }

    // package access as this is called by FOM_Cocoon
    boolean process(Scriptable scope, FOM_Cocoon cocoon,
                    String uri, Object bizData, 
                    OutputStream out, Environment environment)
        throws Exception {
        setupView(scope, cocoon, environment, null);
        return super.process(uri, bizData, out, environment);
    }
    
    private void setupView(Scriptable scope,
                           FOM_Cocoon cocoon,
                           Environment environment,
                           FOM_WebContinuation kont) {
        Map objectModel = environment.getObjectModel();
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
        FOM_JavaScriptFlowHelper.setFOM_Session(objectModel,
                                            cocoon.jsGet_session());
        FOM_JavaScriptFlowHelper.setFOM_Context(objectModel,
                                                cocoon.jsGet_context());
        if (kont != null) {
            FOM_JavaScriptFlowHelper.setFOM_WebContinuation(objectModel, 
                                                            kont);
        }
    }
}
