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

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.SimpleSelectorProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.internal.EnvironmentHelper;

/**
 * Handles &lt;map:act type="..."&gt; (action-sets calls are handled by {@link ActSetNode}).
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ActTypeNode.java,v 1.9 2004/06/11 08:51:56 cziegeler Exp $
 */

public class ActTypeNode extends SimpleSelectorProcessingNode
  implements ParameterizableProcessingNode, Disposable, Composable {

    /** The parameters of this node */
    private Map parameters;

    /** The 'src' attribute */
    protected VariableResolver source;

    /** The 'name' for the variable anchor */
    protected String name;

    /** Pre-selected action, if it's ThreadSafe */
    protected Action threadSafeAction;

    protected ComponentManager manager;

    protected boolean inActionSet;

    public ActTypeNode(String type, 
                       VariableResolver source, 
                       String name,
                       boolean inActionSet)  {
        super(type);
        this.source = source;
        this.name = name;
        this.inActionSet = inActionSet;
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
        setSelector((ComponentSelector)manager.lookup(Action.ROLE + "Selector"));

        // Get the action, if it's thread safe
        this.threadSafeAction = (Action)this.getThreadSafeComponent();
    }

    public final boolean invoke(Environment env, InvokeContext context)
          throws Exception {

        // Perform any common invoke functionality 
        super.invoke(env, context);

        // Prepare data needed by the action
        Map objectModel = env.getObjectModel();
        Redirector redirector = context.getRedirector();
        SourceResolver resolver = EnvironmentHelper.getCurrentProcessor().getSourceResolver();
        String resolvedSource = source.resolve(context, objectModel);
        Parameters resolvedParams =
            VariableResolver.buildParameters(this.parameters,
                    context, objectModel);

        Map actionResult;

        // If in action set, merge parameters
        if (inActionSet) {
            Parameters callerParams =
                (Parameters)env.getAttribute(ActionSetNode.CALLER_PARAMETERS);
            if (resolvedParams == Parameters.EMPTY_PARAMETERS) {
                // Just swap
                resolvedParams = callerParams;
            } else if (callerParams != Parameters.EMPTY_PARAMETERS) {
                // Build new Parameters object, the both we hare are read-only!
                Parameters newParams = new Parameters();
                // And merge both
                newParams.merge(resolvedParams);
                newParams.merge(callerParams);
                resolvedParams = newParams;
            }
        }

        // If action is ThreadSafe, avoid select() and try/catch block (faster !)
        if (this.threadSafeAction != null) {
            actionResult = this.executor.invokeAction(this, 
                                             objectModel, 
                                             this.threadSafeAction, 
                                             redirector, 
                                             resolver, 
                                             resolvedSource, 
                                             resolvedParams);
        } else {
            Action action = (Action)this.selector.select(this.componentName);
            try {
                actionResult = this.executor.invokeAction(this,
                                                 objectModel, 
                                                 action, 
                                                 redirector, 
                                                 resolver, 
                                                 resolvedSource, 
                                                 resolvedParams);
            } finally {
                this.selector.release(action);
            }
        }

        if (redirector.hasRedirected()) {
            return true;
        }

        if (actionResult != null) {
            // Action succeeded : process children if there are some, with the action result
            if (this.children != null) {
                boolean result = this.invokeNodes(this.children, env, context, name, actionResult);

                if (inActionSet) {
                    // Merge child action results, if any
                    Map childMap = (Map)env.getAttribute(ActionSetNode.ACTION_RESULTS);
                    if (childMap != null) {
                        Map newResults = new HashMap(childMap);
                        newResults.putAll(actionResult);
                        env.setAttribute(ActionSetNode.ACTION_RESULTS, newResults);
                    } else {
                        // No previous results
                        env.setAttribute(ActionSetNode.ACTION_RESULTS, actionResult);
                    }
                }
                return result;
            }// else {
               // return false; // Return false to continue sitemap invocation
            //}
        }// else {
            return false;   // Action failed
        //}
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.threadSafeAction != null) {
            this.selector.release(this.threadSafeAction);
        }
        this.manager.release(this.selector);
    }

}
