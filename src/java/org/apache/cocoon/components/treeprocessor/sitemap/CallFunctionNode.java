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
package org.apache.cocoon.components.treeprocessor.sitemap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.PatternException;

/**
 * Node handler for calling functions and resuming continuations in
 * the control flow layer.
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @since March 13, 2002
 * @version CVS $Id: CallFunctionNode.java,v 1.10 2004/05/25 13:48:12 cziegeler Exp $
 */
public class CallFunctionNode extends AbstractProcessingNode implements Configurable, Composable {
    protected List parameters;
    protected VariableResolver functionName;
    protected VariableResolver continuationId;
    protected ComponentManager manager;
    protected Interpreter interpreter;

    public static List resolveList(List expressions, ComponentManager manager, InvokeContext context, Map objectModel)
        throws PatternException {
        int size;
        if (expressions == null || (size = expressions.size()) == 0)
            return Collections.EMPTY_LIST;

        List result = new ArrayList(size);

        for (int i = 0; i < size; i++) {
            Interpreter.Argument arg = (Interpreter.Argument)expressions.get(i);
            String value = VariableResolverFactory.getResolver(arg.value, manager).resolve(context, objectModel);
            result.add(new Interpreter.Argument(arg.name, value));
        }

        return result;
    }

    public CallFunctionNode(VariableResolver functionName, VariableResolver continuationId) {
        this.functionName = functionName;
        this.continuationId = continuationId;
    }

    public void setInterpreter(Interpreter interp) throws Exception {
        this.interpreter = interp;
    }

    /**
     * Obtain the configuration specific to this node type and update
     * the internal state.
     *
     * @param config a <code>Configuration</code> value
     * @exception ConfigurationException if an error occurs
     */
    public void configure(Configuration config) throws ConfigurationException {
        //TODO (SW): Deprecate this in the future.
        // It has be found to be bad practice to pass sitemap parameters
        // as function arguments, as these are name-value pairs in the sitemap
        // and positional arguments in the flowscript. If the user doesn't respect
        // the argument order, this leads to difficult to solve bugs.
        parameters = new ArrayList();

        Configuration[] params = config.getChildren("parameter");
        for (int i = 0; i < params.length; i++) {
            Configuration param = params[i];
            String name = param.getAttribute("name", null);
            String value = param.getAttribute("value", null);
            parameters.add(new Interpreter.Argument(name, value));
        }
    }

    public void compose(ComponentManager manager) {
        this.manager = manager;
    }

    public boolean invoke(Environment env, InvokeContext context) throws Exception {
        List params = null;

        // Resolve parameters
        if (parameters != null) {
            params = resolveList(parameters, manager, context, env.getObjectModel());
        }

        String continuation = continuationId.resolve(context, env.getObjectModel());

        // If the continuation id is not null, it takes precedence over
        // the function call, so we invoke it here.
        if (continuation != null && continuation.length() > 0) {
            interpreter.handleContinuation(continuation, params, context.getRedirector());
            return true;
        }

        // We don't have a continuation id passed in <map:call>, so invoke
        // the specified function

        String name = functionName.resolve(context, env.getObjectModel());

        if (name != null && name.length() > 0) {
            interpreter.callFunction(name, params, context.getRedirector());
            return true;
        }
        
        // Found neither continuation nor function to call
        throw new ProcessingException("No function nor continuation given in <map:call function> at " + getLocation());
    }
}
