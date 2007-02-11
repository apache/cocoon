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
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.SimpleSelectorProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.selection.SwitchSelector;
import org.apache.cocoon.sitemap.PatternException;

import java.util.Map;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: SwitchSelectNode.java,v 1.5 2004/03/05 13:02:52 bdelacretaz Exp $
 */
public class SwitchSelectNode extends SimpleSelectorProcessingNode
                              implements ParameterizableProcessingNode, Composable, Disposable {

    /** The parameters of this node */
    private Map parameters;

    /** Pre-selected selector, if it's ThreadSafe */
    protected SwitchSelector threadSafeSelector;

    private ProcessingNode[][] whenNodes;

    private VariableResolver[] whenTests;

    private ProcessingNode[] otherwhiseNodes;

    private ComponentManager manager;

    public SwitchSelectNode(String name) throws PatternException {
        super(name);
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

    public void setCases(ProcessingNode[][] whenNodes, VariableResolver[] whenTests, ProcessingNode[] otherwhiseNodes) {
        this.whenNodes = whenNodes;
        this.whenTests = whenTests;
        this.otherwhiseNodes = otherwhiseNodes;
    }

    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;

        setSelector((ComponentSelector)manager.lookup(Selector.ROLE + "Selector"));

        // Get the selector, if it's ThreadSafe
        this.threadSafeSelector = (SwitchSelector)this.getThreadSafeComponent();
    }

    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {
	
      	// Perform any common invoke functionality 
        super.invoke(env, context);

        // Prepare data needed by the action
        Map objectModel = env.getObjectModel();
        Parameters resolvedParams = VariableResolver.buildParameters(this.parameters, context, objectModel);

        // If selector is ThreadSafe, avoid select() and try/catch block (faster !)
        if (this.threadSafeSelector != null) {

            Object ctx = this.threadSafeSelector.getSelectorContext(objectModel, resolvedParams);

            for (int i = 0; i < this.whenTests.length; i++) {
                if (this.threadSafeSelector.select(whenTests[i].resolve(context, objectModel), ctx)) {
                    return invokeNodes(this.whenNodes[i], env, context);
                }
            }

            if (this.otherwhiseNodes != null) {
                return invokeNodes(this.otherwhiseNodes, env, context);
            }

            return false;

        } else {
            SwitchSelector selector = (SwitchSelector)this.selector.select(this.componentName);

            Object ctx = selector.getSelectorContext(objectModel, resolvedParams);
           
            try {
                for (int i = 0; i < this.whenTests.length; i++) {
                    if (selector.select(whenTests[i].resolve(context, objectModel), ctx)) {
                        return invokeNodes(this.whenNodes[i], env, context);
                    }
                }

                if (this.otherwhiseNodes != null) {
                    return invokeNodes(this.otherwhiseNodes, env, context);
                }

                return false;
            } finally {
                this.selector.release(selector);
            }
        }
    }

    public void dispose() {
        if (this.threadSafeSelector != null) {
            this.selector.release(this.threadSafeSelector);
            this.threadSafeSelector = null;
        }
        if (this.selector == null) {
            this.manager.release(this.selector);
            this.selector = null;
        }
        this.manager = null;
    }
}
