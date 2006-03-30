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

import java.util.Map;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.SimpleSelectorProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.matching.PreparableMatcher;
import org.apache.cocoon.sitemap.PatternException;

/**
 *
 * @version $Id$
 */
public class PreparableMatchNode extends SimpleSelectorProcessingNode
    implements ParameterizableProcessingNode, Initializable {

    /** The 'pattern' attribute */
    private String pattern;

    /** The 'name' for the variable anchor */
    private String name;

    private Object preparedPattern;

    private Map parameters;

    public PreparableMatchNode(String type, String pattern, String name) {
        super(Matcher.ROLE + "Selector", type);
        this.pattern = pattern;
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode#setParameters(java.util.Map)
     */
    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {

        // Prepare the pattern
        PreparableMatcher matcher = (PreparableMatcher)getComponent();

        try {
            this.preparedPattern = matcher.preparePattern(this.pattern);
        } catch(PatternException pe) {
            String msg = "Invalid pattern '" + this.pattern + "' for matcher at " + this.getLocation();
            throw new ConfigurationException(msg, pe);
        } finally {
            releaseComponent(matcher);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.treeprocessor.ProcessingNode#invoke(org.apache.cocoon.environment.Environment, org.apache.cocoon.components.treeprocessor.InvokeContext)
     */
    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {

      	// Perform any common invoke functionality
      	super.invoke(env, context);

        Map objectModel = env.getObjectModel();
        Parameters resolvedParams = VariableResolver.buildParameters(
            this.parameters, context, objectModel
        );

        Map result = null;

        PreparableMatcher matcher = (PreparableMatcher)getComponent();
        try {
            result = this.executor.invokePreparableMatcher(this,
                                                           objectModel,
                                                           matcher,
                                                           this.pattern,
                                                           preparedPattern,
                                                           resolvedParams);
        } finally {
            releaseComponent(matcher);
        }

        if (result != null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Matcher '" + this.componentName + "' matched prepared pattern '" +
                                  this.pattern + "' at " + this.getLocation());
            }

            // Invoke children with the matcher results
            return this.invokeNodes(children, env, context, name, result);

        }
        // Matcher failed
        return false;
    }
}
