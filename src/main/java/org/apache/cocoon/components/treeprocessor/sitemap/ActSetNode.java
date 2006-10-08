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

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.SimpleParentProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.ProcessingException;

import java.util.Map;

/**
 * Handles &lt;map:act type="..."&gt; (action-sets calls are handled by {@link ActSetNode}).
 *
 * @version $Id$
 */
public class ActSetNode extends SimpleParentProcessingNode
                        implements ParameterizableProcessingNode {

    /** The parameters of this node */
    private Map parameters;

    /** The action set to call */
    private ActionSetNode actionSet;


    public ActSetNode() {
        super(null);
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

    public void setActionSet(ActionSetNode actionSet) {
        this.actionSet = actionSet;
    }

    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {

        // Perform any common invoke functionality
        super.invoke(env, context);

        Parameters resolvedParams = VariableResolver.buildParameters(
            this.parameters,
            context,
            env.getObjectModel()
        );

        try {
            Map result = this.actionSet.call(env, context, resolvedParams);

            if (context.getRedirector().hasRedirected()) {
                return true;

            } else if (result == null) {
                return false;

            } else if (this.children == null) {
                return true;

            } else {
                return this.invokeNodes(this.children, env, context, null, result);
            }
        } catch (Exception e) {
            throw ProcessingException.throwLocated("Sitemap: error invoking action set", e, getLocation());
        }
    }
}
