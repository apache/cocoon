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
package org.apache.cocoon.components.treeprocessor.sitemap;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;
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
 * @version $Id$
 */
public class ActTypeNode extends SimpleSelectorProcessingNode
                         implements ParameterizableProcessingNode {

    /** The parameters of this node */
    private Map parameters;

    /** The 'src' attribute */
    protected VariableResolver source;

    /** The 'name' for the variable anchor */
    protected String name;

    protected boolean inActionSet;


    public ActTypeNode(String type,
                       VariableResolver source,
                       String name,
                       boolean inActionSet)  {
        super(Action.ROLE + "Selector", type);
        this.source = source;
        this.name = name;
        this.inActionSet = inActionSet;
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {

        // Perform any common invoke functionality
        super.invoke(env, context);

        // Prepare data needed by the action
        Map objectModel = env.getObjectModel();
        String resolvedSource = source.resolve(context, objectModel);
        Parameters resolvedParams =
            VariableResolver.buildParameters(this.parameters, context, objectModel);

        // If in action set, merge parameters
        if (inActionSet) {
            Parameters callerParams =
                (Parameters) env.getAttribute(ActionSetNode.CALLER_PARAMETERS);
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

        Redirector redirector = context.getRedirector();
        SourceResolver resolver = EnvironmentHelper.getCurrentProcessor().getSourceResolver();

        try {
            Action action = (Action) getComponent();
            Map actionResult;
            try {
                actionResult = this.executor.invokeAction(this,
                                                          objectModel,
                                                          action,
                                                          redirector,
                                                          resolver,
                                                          resolvedSource,
                                                          resolvedParams);
            } finally {
                releaseComponent(action);
            }

            if (redirector.hasRedirected()) {
                return true;
            }

            if (actionResult != null) {
                // Action succeeded : process children if there are some, with the action result
                if (this.children != null) {
                    boolean result = invokeNodes(this.children, env, context, name, actionResult);

                    if (inActionSet) {
                        // Merge child action results, if any
                        Map childMap = (Map) env.getAttribute(ActionSetNode.ACTION_RESULTS);
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
                }
            }
        } catch (Exception e) {
            throw ProcessingException.throwLocated("Sitemap: error invoking action", e, getLocation());
        }

        return false;   // Action failed
    }
}
