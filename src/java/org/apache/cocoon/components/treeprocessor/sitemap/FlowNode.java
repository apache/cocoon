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
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.Environment;

/**
 * Handler for &lt;map:flow&gt; element in the sitemap.
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @since September 13, 2002
 * @version CVS $Id: FlowNode.java,v 1.5 2004/06/08 13:09:27 cziegeler Exp $
 */
public class FlowNode extends AbstractProcessingNode
        implements Composable, Contextualizable, Disposable {

    ComponentManager manager;
    String language;
    Context context;
    Interpreter interpreter;
    ComponentSelector interpreterSelector;

    public FlowNode(String language) {
        this.language = language;
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

    public void contextualize(org.apache.avalon.framework.context.Context context)
        throws ContextException {
        this.context = (Context)context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }

    /**
     *
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
            this.interpreterSelector = (ComponentSelector)manager.lookup(Interpreter.ROLE);
            // Obtain the Interpreter instance for this language
            this.interpreter = (Interpreter)this.interpreterSelector.select(language);
        } catch (ComponentException ce) {
            throw ce;
        } catch (Exception ex) {
            throw new ComponentException(language,
                "ScriptNode: Couldn't obtain a flow interpreter for " + language + ": " + ex);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            if ( this.interpreterSelector != null ) {
                this.interpreterSelector.release( (Component)this.interpreter );
                this.interpreter = null;
                this.manager.release( this.interpreterSelector );
                this.interpreterSelector = null;
            }
            this.manager = null;
        }

    }

    public Interpreter getInterpreter() {
        return interpreter;
    }
}
