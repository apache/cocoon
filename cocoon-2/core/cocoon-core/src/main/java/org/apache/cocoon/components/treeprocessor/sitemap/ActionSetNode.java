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
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.NamedProcessingNode;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.environment.Environment;

/**
 * @version $Id$
 */
public class ActionSetNode extends AbstractProcessingNode
                           implements NamedProcessingNode {

    public static final String CALLER_PARAMETERS = ActionSetNode.class.getName() + "/CallerParameters";
    public static final String ACTION_RESULTS    = ActionSetNode.class.getName() + "/ActionResults";

    /** The action nodes */
    private ProcessingNode[] nodes;

    /** The 'action' attribute for each action */
    private String[] actionNames;


    public ActionSetNode(String name, ProcessingNode[] nodes, String[] actionNames) {
        super(name);
        this.nodes = nodes;
        this.actionNames = actionNames;
    }

    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {
        throw new ProcessingException("An action-set cannot be invoked", getLocation());
    }

    /**
     * Call the actions composing the action-set and return the combined result of
     * these actions.
     */
    public final Map call(Environment env, InvokeContext context, Parameters params)
    throws Exception {

        String cocoonAction = env.getAction();

        // Store the parameters from the caller into the environment so that they can be merged with
        // each action's parameters.
        env.setAttribute(CALLER_PARAMETERS, params);

        Map result = null;

        try {
            // Call each action that either has no cocoonAction, or whose cocoonAction equals
            // the one from the environment.
            for (int i = 0; i < nodes.length; i++) {

                String actionName = actionNames[i];
                if (actionName == null || actionName.equals(cocoonAction)) {

                    this.nodes[i].invoke(env, context);

                    // Get action results. They're passed back through the environment since action-sets
                    // "violate" the tree hierarchy (the returned Map is visible outside of the node)
                    Map actionResult = (Map) env.getAttribute(ACTION_RESULTS);
                    // Don't forget to clear it
                    env.removeAttribute(ACTION_RESULTS);

                    if (actionResult != null) {
                        // Merge the result in the global result, creating it if necessary.
                        if (result == null) {
                            result = new HashMap(actionResult);
                        } else {
                            result.putAll(actionResult);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw ProcessingException.throwLocated("Sitemap: error invoking action set", e, getLocation());
        }

        return result;
    }

    /**
     * Implementation of <code>NamedProcessingNode</code>.
     */
    public String getName() {
        return this.componentName;
    }
}
