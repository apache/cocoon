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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.SimpleSelectorProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.matching.PreparableMatcher;
import org.apache.cocoon.sitemap.PatternException;

import java.util.Map;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: PreparableMatchNode.java,v 1.7 2004/06/11 20:03:35 vgritsenko Exp $
 */
public class PreparableMatchNode extends SimpleSelectorProcessingNode
                                 implements ParameterizableProcessingNode, Composable, Disposable {

    /** The 'pattern' attribute */
    private String pattern;

    /** The 'name' for the variable anchor */
    private String name;

    private Object preparedPattern;

    private Map parameters;

    /** The matcher, if it's ThreadSafe */
    private PreparableMatcher threadSafeMatcher;

    protected ComponentManager manager;

    public PreparableMatchNode(String type, String pattern, String name) {
        super(type);
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
     * @see org.apache.avalon.framework.component.Composable#compose(org.apache.avalon.framework.component.ComponentManager)
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
        setSelector((ComponentSelector)manager.lookup(Matcher.ROLE + "Selector"));

        // Prepare the pattern, and keep matcher if ThreadSafe
        PreparableMatcher matcher = (PreparableMatcher)selector.select(componentName);

        if (matcher instanceof ThreadSafe) {
            this.threadSafeMatcher = matcher;
        }

        try {
            this.preparedPattern = matcher.preparePattern(this.pattern);

        } catch(PatternException pe) {
            String msg = "Invalid pattern '" + this.pattern + "' for matcher at " + this.getLocation();
            throw new ComponentException(null, msg, pe);

        } finally {
            if (this.threadSafeMatcher == null) {
                selector.release(matcher);
            }
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

        if (this.threadSafeMatcher != null) {
            // Avoid select() and try/catch block (faster !)
            result = this.threadSafeMatcher.preparedMatch(preparedPattern, objectModel, resolvedParams);

        } else {
            // Get matcher from selector
            PreparableMatcher matcher = (PreparableMatcher)this.selector.select(this.componentName);
            try {
                result = matcher.preparedMatch(preparedPattern, objectModel, resolvedParams);

            } finally {
                this.selector.release(matcher);
            }
        }

        if (result != null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Matcher '" + this.componentName + "' matched prepared pattern '" +
                                  this.pattern + "' at " + this.getLocation());
            }

            // Invoke children with the matcher results
            return this.invokeNodes(children, env, context, name, result);

        } else {
            // Matcher failed
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.threadSafeMatcher != null) {
            selector.release(this.threadSafeMatcher);
            this.threadSafeMatcher = null;
        }
        if (this.selector != null) {
            this.manager.release(this.selector);
            this.selector = null;
        }
        this.manager = null;
    }
}
