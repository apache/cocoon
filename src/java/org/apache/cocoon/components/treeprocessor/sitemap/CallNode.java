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

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.CategoryNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.environment.Environment;

import java.util.Map;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: CallNode.java,v 1.3 2004/06/09 11:59:23 cziegeler Exp $
 */

public class CallNode extends AbstractProcessingNode
    implements Initializable, ParameterizableProcessingNode, Composable {

    private ComponentManager manager;

    /** The parameters of this node */
    private Map parameters;

    /** The 'resource' attribute */
    private String resourceName;

    private VariableResolver resourceResolver;

    /** The category node */
    private CategoryNode resources;

    private ProcessingNode resourceNode;

    public CallNode() {
        super(null);
    }
    
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

    public void setResource(CategoryNode resources, String resourceName) throws Exception {
        this.resourceName = resourceName;
        this.resources = resources;
    }

    public void initialize() throws Exception {
        if (VariableResolverFactory.needsResolve(this.resourceName)) {
            // Will always be resolved at invoke time
            this.resourceResolver = VariableResolverFactory.getResolver(this.resourceName, this.manager);
        } else {
            // Static name : get it now
            this.resourceNode = this.resources.getNodeByName(VariableResolverFactory.unescape(this.resourceName));
        }
    }

    public final boolean invoke(Environment env, InvokeContext context)
      throws Exception {

        Map objectModel = env.getObjectModel();
        // Resolve parameters, but push them only once the resource name has been
        // resolved, otherwise it adds an unwanted nesting level
        Map params = VariableResolver.buildMap(this.parameters, context, objectModel);

        if (this.resourceNode != null) {
            // Static resource name
            context.pushMap(null,params);
            
            try {
                return this.resourceNode.invoke(env, context);
            } finally {
                context.popMap();
            }
    
        } else {
            // Resolved resource name
            String name = this.resourceResolver.resolve(context, objectModel);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Calling resource " + name);
            }
            
            // and only now push the parameters
            context.pushMap(null,params);
            
            try {
                return this.resources.invokeByName(name, env, context);
            } finally {
                context.popMap();
            }
        }
    }
}
