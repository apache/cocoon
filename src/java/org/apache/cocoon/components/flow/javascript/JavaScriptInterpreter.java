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
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.flow.AbstractInterpreter;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.excalibur.source.Source;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.debugger.ScopeProvider;
import org.mozilla.javascript.tools.ToolErrorReporter;

/**
 * Interface with the JavaScript interpreter.
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @since March 25, 2002
 */
public class JavaScriptInterpreter extends AbstractInterpreter
    implements Configurable, Initializable
{
    public static final String USER_GLOBAL_SCOPE = "JavaScript GLOBAL SCOPE";

    // This is the only optimization level that supports continuations
    // in the Christoper Oliver's Rhino JavaScript implementation
    static int OPTIMIZATION_LEVEL = -2;

    /**
     * List of <code>Source</code> objects that represent files to be
     * read in by the JavaScript interpreter.
     */
    protected List scripts = new ArrayList();

    /**
     * When was the last time we checked for script modifications. Used
     * only if {@link #reloadScripts} is true.
     */
    protected long lastTimeCheck = 0;
    JSGlobal scope;
    List compiledScripts = Collections.synchronizedList(new ArrayList());
    JSErrorReporter errorReporter;
    boolean enableDebugger = false;
    org.mozilla.javascript.tools.debugger.Main debugger;

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

            final org.mozilla.javascript.tools.debugger.Main db
                = new org.mozilla.javascript.tools.debugger.Main("Cocoon Flow Debugger");
            db.pack();
            db.setSize(600,460);
            db.setExitAction(new Runnable() { 
                    public void run() { 
                        db.setVisible(false); 
                    } 
                });
            db.setVisible(true);
            debugger = db;
            Context.addContextListener(debugger);
            debugger.doBreak();
        }

        Context context = Context.enter();
        context.setOptimizationLevel(OPTIMIZATION_LEVEL);
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
                ((ScriptableObject)scope)
                    .defineFunctionProperties(names, JSGlobal.class,
                                              ScriptableObject.DONTENUM);
            }
            catch (PropertyException e) {
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
     * @param createSession a <code>boolean</code> value
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

        scope = (Scriptable)userScopes.get(environment.getURIPrefix());

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

        userScopes.put(environment.getURIPrefix(), scope);
    }

    public void removeSessionScope(Environment environment)
    {
        Map objectModel = environment.getObjectModel();
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession(true);

        HashMap userScopes = (HashMap)session.getAttribute(USER_GLOBAL_SCOPE);
        if (userScopes == null)
            return;

        userScopes.remove(environment.getURIPrefix());
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
     * will be retrieved from at the next invocation of
     * callFunction().</p>
     *
     * @param environment an <code>Environment</code> value
     * @param createNew a <code>boolean</code> value
     * @param sourcesToBeCompiled list of Source's to compile 
     * @return a <code>Scriptable</code> value
     * @exception Exception if an error occurs
     */
    protected Scriptable enterContext(Environment environment, 
                                      boolean needsExec,
                                      List sourcesToBeCompiled)
        throws Exception
    {
        Context context = Context.enter();
        context.setOptimizationLevel(OPTIMIZATION_LEVEL);
        context.setGeneratingDebug(true);
        context.setCompileFunctionsWithDynamicScope(true);
        context.setErrorReporter(errorReporter);
        Scriptable thrScope = null;

        compileScripts(context, environment, sourcesToBeCompiled);

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
        if (thrScope == null) {
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
            cocoon = (JSCocoon)context.newObject(scope, "Cocoon", args);
            ((JSCocoon)cocoon).setInterpreter(this);
            ((JSCocoon)cocoon).setScope(thrScope);
            thrScope.put("cocoon", thrScope, cocoon);
            needsExec = true;
        } else {
            cocoon = (JSCocoon)thrScope.get("cocoon", thrScope);
        }
        // We need to setup the JSCocoon object according to the current
        // request. Everything else remains the same.
        cocoon.setContext(manager, environment);

        if (needsExec) {
            for (int i = 0; i < compiledScripts.size(); i++) {
                Script compiledScript = (Script)compiledScripts.get(i);
                compiledScript.exec(context, thrScope);
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

    private void compileScripts(Context cx, Environment environment, List sources)
        throws Exception
    {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Reading scripts");
        }
        Iterator iter = sources.iterator();
        while (iter.hasNext()) {
            Source src = (Source)iter.next();
            compileScript(cx, src);
        }
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
        Source src = environment.resolveURI(fileName);
        if (src == null) {
            throw new ResourceNotFoundException(fileName + ": not found");
        }
        return compileScript(cx, src);
    }

    private Script compileScript(Context cx, Source src) throws Exception {
        InputStream is = src.getInputStream();
        Reader reader = new BufferedReader(new InputStreamReader(is));
        Script compiledScript = cx.compileReader(scope, reader,
                                                 src.getURI(), 
                                                 1, null);
        compiledScripts.add(compiledScript);
        return compiledScript;
    }

    /**
     * Reloads any modified script files.
     *
     * <p>It checks to see if any of the files already read in (those
     * present in the <code>scripts</code> hash map) have been
     * modified.
     *
     * <p>It also checks to see if any script files have been registered
     * with the interpreter since the last call to
     * <code>checkForModifiedScripts</code>. These files are stored in
     * the temporary array <code>needResolve</code>. If any such files
     * are found, they are read in.
     *
     * @param environment an <code>Environment</code> value
     * @param toBeCompiled output parameter: the list of <code>Source</code> objects to be compiled
     * @return true if any existing Source script has changed
     */
    public boolean checkForModifiedScripts(Environment environment, 
                                           List toBeCompiled)
        throws Exception
    {
        boolean needsRefresh = false;
        if (reloadScripts
            && System.currentTimeMillis() >= lastTimeCheck + checkTime) {
            // FIXME: should we worry about synchronization?
            for (int i = 0, size = scripts.size(); i < size; i++) {
                Source src = (Source)scripts.get(i);
                src.refresh();
                getLogger().debug("Checking " + src.getURI()
                                  + ", source " + src
                                  + ", last modified " + src.getLastModified()
                                  + ", last time check " + lastTimeCheck);
                if (src.getLastModified() > lastTimeCheck) {
                    needsRefresh = true;
                    break;
                }
            }
        }
        
        // FIXME: remove the need for synchronization
        synchronized (this) {
            int size = needResolve.size();
            
            // If there's no need to re-read any file, and no files
            // have been requested to be read since the last time,
            // don't do anything.
            if (!needsRefresh && size == 0) {
                lastTimeCheck = System.currentTimeMillis();
                return false;
            }
            
            for (int i = 0; i < size; i++) {
                String source = (String)needResolve.get(i);
                Source src = environment.resolveURI(source);
                scripts.add(src);
                toBeCompiled.add(src);
            }
            needResolve.clear();
        }
        
        // Update the time of the last check. If an exception occurs, this
        // is not executed, so the next request will force a reparse of
        // the script files because of an old time stamp.
        lastTimeCheck = System.currentTimeMillis();
        return needsRefresh;
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
        List toBeCompiled = new ArrayList(); 
        boolean needsRefresh = checkForModifiedScripts(environment, toBeCompiled);
        try {
            thrScope = enterContext(environment, needsRefresh, toBeCompiled);

            Context context = Context.getCurrentContext();
            JSCocoon cocoon = (JSCocoon)thrScope.get("cocoon", thrScope);
          
            if (enableDebugger) {
                final Scriptable s = thrScope;
                debugger.setScopeProvider(
                                          new ScopeProvider()
                                              { public Scriptable getScope() {return s;} }
                                          );
                if (!debugger.isVisible())
                    debugger.setVisible(true);
            }
          
            Object callFunction = thrScope.get("callFunction", thrScope);
            if (callFunction == Scriptable.NOT_FOUND)
                throw new RuntimeException("Cannot find 'callFunction' "
                                           + "(system.js not loaded?)");
          
            Object fun = thrScope.get(funName, thrScope);
            if (fun == Scriptable.NOT_FOUND)
                throw new RuntimeException("'" + funName + "' is undefined!");
            if (!(fun instanceof Function))
                throw new RuntimeException("'" + funName + "' is not a function!");
          
            int size = (params != null ? params.size() : 0);
            Object[] funArgs = new Object[size];
            NativeArray parameters = new NativeArray(size);
            if (size != 0) {
                for (int i = 0; i < size; i++) {
                    Interpreter.Argument arg = (Interpreter.Argument)params.get(i);
                    funArgs[i] = arg.value;
                    parameters.put(arg.name, parameters, arg.value);
                }
            }
            cocoon.setParameters(parameters);
            NativeArray funArgsArray = new NativeArray(funArgs);
            Object callFunArgs[] = { fun, funArgsArray };
            ((Function) callFunction).call(context, thrScope, thrScope, callFunArgs);
        }
        catch (JavaScriptException ex) {
            Context.reportError(ToolErrorReporter.getMessage("msg.uncaughtJSException",
                                                             ex.getMessage()));
            throw ex;
        }
        finally {
            exitContext(thrScope);
        }
    }

    public void handleContinuation(String id, List params,
                                   Environment environment)
        throws Exception
    {
        WebContinuation wk = continuationsMgr.lookupWebContinuation(id);

        if (wk == null) {
            List p = new ArrayList();
            p.add(new Interpreter.Argument("kontId", id));
            callFunction("handleInvalidContinuation", p, environment);
            return;
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
        final Scriptable kScope = cocoon.getScope();

        if (enableDebugger) {
            debugger.setScopeProvider(
                                      new ScopeProvider()
                                          { public Scriptable getScope() {return kScope;} }
                                      );
            if (!debugger.isVisible())
                debugger.setVisible(true);
        }

        // We can now resume the processing from the state saved by the
        // continuation object. Setup the JavaScript Context object.
        Object handleContFunction = kScope.get("handleContinuation", kScope);
        if (handleContFunction == Scriptable.NOT_FOUND)
            throw new RuntimeException("Cannot find 'handleContinuation' "
                                       + "(system.js not loaded?)");

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
            Context.reportError(ToolErrorReporter.getMessage("msg.uncaughtJSException",
                                                             ex.getMessage()));
            throw ex;
        } finally {
            Context.exit();
        }
    }
}
