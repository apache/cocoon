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

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Redirector;

/**
 * Node handler for calling functions and resuming continuations in
 * the control flow layer.
 *
 * @since March 13, 2002
 * @version $Id$
 */
public class CallFunctionNode extends AbstractProcessingNode implements ParameterizableProcessingNode {

    protected Map parameters;
    protected VariableResolver functionName;
    protected VariableResolver continuationId;
    protected String[] argumentNames;
    protected Interpreter interpreter;

    public CallFunctionNode(VariableResolver functionName, VariableResolver continuationId, String[] argumentNames) {
        super(null);
        this.functionName = functionName;
        this.continuationId = continuationId;
        this.argumentNames = argumentNames;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode#setParameters(java.util.Map)
     */
    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

    public void setInterpreter(Interpreter interp) throws Exception {
        this.interpreter = interp;
    }

    public boolean invoke(Environment env, InvokeContext context) throws Exception {

        Map objectModel = env.getObjectModel();

        // Resolve parameters
        Parameters params = VariableResolver.buildParameters(this.parameters, context, objectModel);

        // Build the list of positional arguments
        //TODO (SW): Deprecate this in the future.
        // It has be found to be bad practice to pass sitemap parameters
        // as function arguments, as these are name-value pairs in the sitemap
        // and positional arguments in the flowscript. If the user doesn't respect
        // the argument order, this leads to difficult to solve bugs.
        List args;
        if (argumentNames.length != 0) {
            args = new ArrayList(argumentNames.length);
            for (int i = 0; i < argumentNames.length; i++) {
                String name = argumentNames[i];
                args.add(new Interpreter.Argument(name, params.getParameter(name)));
            }
        } else {
            args = Collections.EMPTY_LIST;
        }

        // Need redirector in any case
        Redirector redirector = context.getRedirector();

        // If the continuation id is not null, it takes precedence over
        // the function call, so we invoke it here.
        String continuation = continuationId.resolve(context, env.getObjectModel());
        if (continuation != null && continuation.length() > 0) {
            try {
                interpreter.handleContinuation(continuation, args, redirector);
            } catch(Exception e) {
                throw ProcessingException.throwLocated("Sitemap: error calling continuation", e, getLocation());
            }
            if (!redirector.hasRedirected()) {
                throw new ProcessingException("Sitemap: <map:call continuation> did not send a response", getLocation());
            }
            return true;
        }

        // We don't have a continuation id passed in <map:call>, so invoke
        // the specified function
        String name = functionName.resolve(context, objectModel);
        if (name != null && name.length() > 0) {
            try {
                interpreter.callFunction(name, args, redirector);
            } catch(Exception e) {
                throw ProcessingException.throwLocated("Sitemap: error calling function '" + name + "'", e, getLocation());
            }
            if (!redirector.hasRedirected()) {
                throw new ProcessingException("Sitemap: <map:call function> did not send a response", getLocation());
            }
            return true;
        }

        // Found neither continuation nor function to call
        throw new ProcessingException("Sitemap: no function nor continuation given in <map:call function>", getLocation());
    }
}
