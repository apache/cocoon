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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.CategoryNode;
import org.apache.cocoon.components.treeprocessor.CategoryNodeBuilder;
import org.apache.cocoon.components.treeprocessor.LinkedProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: RedirectToNodeBuilder.java,v 1.6 2004/03/05 13:02:52 bdelacretaz Exp $
 */

public class RedirectToNodeBuilder extends AbstractProcessingNodeBuilder
  implements LinkedProcessingNodeBuilder {

    private CallNode callNode;
    private String resourceName;

    /** This builder has no parameters -- return <code>false</code> */
    protected boolean hasParameters() {
        return false;
    }

    public ProcessingNode buildNode(Configuration config) throws Exception {
        
		if (((SitemapLanguage)this.treeBuilder).isBuildingErrorHandler()) {
			throw new ConfigurationException("'map:redirect' is forbidden inside a 'map:handle-errors', at "
			+ config.getLocation());
		}
		
        // Is it a redirect to resource ?
        this.resourceName = config.getAttribute("resource", null);
        if (this.resourceName != null) {
            
            getLogger().warn("Redirect to resource is deprecated. Use map:call instead at " +
                config.getLocation());

            this.callNode = new CallNode();
            this.treeBuilder.setupNode(this.callNode, config);

            String target = config.getAttribute("target", null);
            if (target != null) {
                Map params = new HashMap(1);
                params.put("target", VariableResolverFactory.getResolver(target, this.manager));
                this.callNode.setParameters(params);
            }
            return this.callNode;
            
        } else {
            ProcessingNode URINode = new RedirectToURINode(
                VariableResolverFactory.getResolver(config.getAttribute("uri"), this.manager),
                config.getAttributeAsBoolean("session", false),
                config.getAttributeAsBoolean("global", false),
                config.getAttributeAsBoolean("permanent", false)
            );
            return this.treeBuilder.setupNode(URINode, config);

        }
    }

    public void linkNode() throws Exception {

        if (this.callNode != null) {
            CategoryNode resources = CategoryNodeBuilder.getCategoryNode(this.treeBuilder, "resources");

            if (resources == null) {
                String msg = "This sitemap contains no resources. Cannot redirect at " +
                    this.callNode.getLocation();
                throw new ConfigurationException(msg);
            }

            this.callNode.setResource(
                resources,
                this.resourceName
            );
        }
    }
}
