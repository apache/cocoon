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

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
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
 * @version $Id$
 */
public class PipelinesNodeBuilder extends ContainerNodeBuilder {

    public ProcessingNode buildNode(Configuration config)
    throws Exception {
        // Check for component configurations
        Configuration child = config.getChild("component-configurations", false);
        if (child != null) {
            checkNamespace(child);
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
