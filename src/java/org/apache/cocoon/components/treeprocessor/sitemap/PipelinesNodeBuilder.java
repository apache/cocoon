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

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.treeprocessor.ContainerNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.ProcessingNodeBuilder;

/**
 * Builds a &lt;map:pipelines&gt;
 *
 * @author <a href="mailto:juergen.seitz@basf-it-services.com">J&uuml;rgen Seitz</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: PipelinesNodeBuilder.java,v 1.5 2004/03/05 13:02:52 bdelacretaz Exp $
 */

public class PipelinesNodeBuilder extends ContainerNodeBuilder implements ThreadSafe {

    public ProcessingNode buildNode(Configuration config)
    throws Exception {
        // check for component configurations
        Configuration child = config.getChild("component-configurations", false);
        if (child != null) {
            this.checkNamespace(child);
            this.treeBuilder.getProcessor().setComponentConfigurations(child);
        }
        PipelinesNode node = new PipelinesNode();
        this.treeBuilder.setupNode(node, config);

		Configuration[] childConfigs = config.getChildren();
		List children = new ArrayList();
		HandleErrorsNode handler = null;
		
		for (int i = 0; i < childConfigs.length; i++) {

			Configuration childConfig = childConfigs[i];
			if (isChild(childConfig)) {

				ProcessingNodeBuilder builder = this.treeBuilder.createNodeBuilder(childConfig);
				if (builder instanceof HandleErrorsNodeBuilder) {
					handler = (HandleErrorsNode)builder.buildNode(childConfig);
				} else {
					// Regular builder
					children.add(builder.buildNode(childConfig));
				}
			}
		}
        if (children.size() == 0) {
            String msg = "There must be at least one pipeline at " + config.getLocation();
            throw new ConfigurationException(msg);
        }

		node.setChildren(toNodeArray(children));
		node.setErrorHandler(handler);

        return node;
    }
}
