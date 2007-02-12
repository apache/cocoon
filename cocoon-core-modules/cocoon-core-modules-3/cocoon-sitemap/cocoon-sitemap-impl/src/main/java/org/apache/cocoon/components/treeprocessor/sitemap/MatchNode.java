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

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.SimpleSelectorProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.matching.Matcher;

/**
 * @version $Id$
 */
public class MatchNode extends SimpleSelectorProcessingNode
                       implements ParameterizableProcessingNode {

    /** The 'pattern' attribute */
    private VariableResolver pattern;

    /** The 'name' for the variable anchor */
    private String name;

    private Map parameters;


    public MatchNode(String type, VariableResolver pattern, String name) {
        super(Matcher.ROLE + "Selector", type);
        this.pattern = pattern;
        this.name = name;
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {

        // Perform any common invoke functionality
        super.invoke(env, context);

        Map objectModel = env.getObjectModel();

        String resolvedPattern = pattern.resolve(context, objectModel);
        Parameters resolvedParams = VariableResolver.buildParameters(this.parameters, context, objectModel);

        try {
            Map result = null;
            Matcher matcher = (Matcher) getComponent();
            try {
                result = this.executor.invokeMatcher(this,
                                                     objectModel,
                                                     matcher,
                                                     resolvedPattern,
                                                     resolvedParams);
            } finally {
                releaseComponent(matcher);
            }

            if (result != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Matcher '" + this.componentName + "' matched pattern '" + this.pattern +
                                      "' at " + getLocation());
                }

                // Invoke children with the matcher results
                return invokeNodes(children, env, context, name, result);
            }
        } catch (Exception e) {
            throw ProcessingException.throwLocated("Sitemap: error invoking matcher", e, getLocation());
        }

        // Matcher failed
        return false;
    }
}
