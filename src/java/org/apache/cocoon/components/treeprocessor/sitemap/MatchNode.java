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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.SimpleSelectorProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.sitemap.PatternException;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: MatchNode.java,v 1.3 2004/03/05 13:02:52 bdelacretaz Exp $
 */

public class MatchNode extends SimpleSelectorProcessingNode
        implements ParameterizableProcessingNode, Composable, Disposable {

    /** The 'pattern' attribute */
    private VariableResolver pattern;

    /** The 'name' for the variable anchor */
    private String name;

    /** The matcher, if it's ThreadSafe */
    private Matcher threadSafeMatcher;

    private Map parameters;

    private ComponentManager manager;

    public MatchNode(String type, VariableResolver pattern, String name) throws PatternException {
        super(type);
        this.pattern = pattern;
        this.name = name;
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
        this.setSelector((ComponentSelector)manager.lookup(Matcher.ROLE + "Selector"));

        // Get matcher if it's ThreadSafe
        this.threadSafeMatcher = (Matcher)this.getThreadSafeComponent();
    }

    public final boolean invoke(Environment env, InvokeContext context)
      throws Exception {
	
        // Perform any common invoke functionality 
        super.invoke(env, context);

        Map objectModel = env.getObjectModel();

        String resolvedPattern = pattern.resolve(context, objectModel);
        Parameters resolvedParams = VariableResolver.buildParameters(this.parameters, context, objectModel);

        Map result = null;

        if (this.threadSafeMatcher != null) {
            // Avoid select() and try/catch block (faster !)
            result = this.threadSafeMatcher.match(resolvedPattern, objectModel, resolvedParams);
        } else {
            // Get matcher from selector
            Matcher matcher = (Matcher)this.selector.select(this.componentName);
            try {
                result = matcher.match(resolvedPattern, objectModel, resolvedParams);
            } finally {
                this.selector.release(matcher);
            }
        }

        if (result != null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Matcher '" + this.componentName + "' matched pattern '" + this.pattern +
                    "' at " + this.getLocation());
            }

            // Invoke children with the matcher results
            return this.invokeNodes(children, env, context, name, result);
        } else {
            // Matcher failed
            return false;
        }
    }

    /**
     * Disposable Interface
     */
    public void dispose() {
        this.manager.release(this.selector);
    }
}
