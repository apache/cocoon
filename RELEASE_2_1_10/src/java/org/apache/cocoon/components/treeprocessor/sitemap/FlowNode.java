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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;

import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.environment.Environment;

/**
 * Handler for &lt;map:flow&gt; element in the sitemap.
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @since September 13, 2002
 * @version CVS $Id$
 */
public class FlowNode extends AbstractProcessingNode
                      implements Composable, Disposable {

    private ComponentManager manager;
    private String language;
    private Interpreter interpreter;
    private ComponentSelector interpreterSelector;

    public FlowNode(String language) {
        this.language = language;
    }

    /**
     * Lookup an flow {@link org.apache.cocoon.components.flow.Interpreter}
     * instance to hold the scripts defined within the <code>&lt;map:flow&gt;</code>
     * in the sitemap.
     *
     * @param manager a <code>ComponentManager</code> value
     * @exception ComponentException if no flow interpreter could be obtained
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;

        try {
            this.interpreterSelector = (ComponentSelector) manager.lookup(Interpreter.ROLE);
            // Obtain the Interpreter instance for this language
            this.interpreter = (Interpreter) this.interpreterSelector.select(language);
            // Set interpreter ID as URI of the flow node (full sitemap file path)
            this.interpreter.setInterpreterID(this.location.getURI());
        } catch (ComponentException e) {
            throw e;
        } catch (Exception e) {
            throw new ComponentException(language,
                                         "FlowNode: Couldn't obtain a flow interpreter for '" + language +
                                         "' at " + getLocation(), e);
        }
    }

    /**
     * This method should never be called by the TreeProcessor, since a
     * <code>&lt;map:flow&gt;</code> element should not be in an
     * "executable" sitemap node.
     *
     * @param env an <code>Environment</code> value
     * @param context an <code>InvokeContext</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean invoke(Environment env, InvokeContext context) throws Exception {
        return true;
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            if (this.interpreterSelector != null) {
                this.interpreterSelector.release((Component) this.interpreter);
                this.interpreter = null;

                this.manager.release(this.interpreterSelector);
                this.interpreterSelector = null;
            }
            this.manager = null;
        }
    }
}
