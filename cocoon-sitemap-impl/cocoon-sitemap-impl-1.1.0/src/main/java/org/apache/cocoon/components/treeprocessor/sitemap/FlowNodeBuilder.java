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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.treeprocessor.AbstractParentProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.spring.configurator.ResourceUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;

/**
 * Builder of a {@link FlowNode} instance, corresponding to a
 * <code>&lt;map:flow&gt;</code> element in the sitemap.
 *
 * @since September 13, 2002
 * @version $Id$
 */
public class FlowNodeBuilder extends AbstractParentProcessingNodeBuilder {

    protected static String DEFAULT_FLOW_SCRIPT_LOCATION = "flow";

    /**
     * @see org.apache.cocoon.components.treeprocessor.ProcessingNodeBuilder#buildNode(org.apache.avalon.framework.configuration.Configuration)
     */
    public ProcessingNode buildNode(Configuration config)
    throws Exception {
        final String language = config.getAttribute("language", "javascript");
        final FlowNode node = new FlowNode(language);

        if ( !this.treeBuilder.registerNode("flow", node) ) {
            throw new ConfigurationException("Only one <map:flow> is allowed in a sitemap. Another one is declared at " +
                    config.getLocation());
        }
        this.treeBuilder.setupNode(node, config);

        // since 2.2 we add by default all flow scripts located in the ./flow directory
        // The default location can be overwritten by specifying the location attribute.
        final BeanFactory beanFactory = this.treeBuilder.getWebApplicationContext();
        if ( beanFactory instanceof ApplicationContext && node.getInterpreter().getScriptExtension() != null ) {
            final ResourceLoader resourceLoader = (ApplicationContext)beanFactory;
            final String scriptLocation = config.getAttribute("location", DEFAULT_FLOW_SCRIPT_LOCATION);
            if ( resourceLoader.getResource(scriptLocation).exists() ) {
                final ServletContextResourcePatternResolver resolver = new ServletContextResourcePatternResolver(resourceLoader);
                final Resource[] resources = resolver.getResources(scriptLocation + "/*" + node.getInterpreter().getScriptExtension());
                if ( resources != null ) {
                    for(int i=0; i < resources.length; i++) {
                        node.getInterpreter().register(ResourceUtils.getUri(resources[i]));
                    }
                }
            }
        }

        // now process child nodes
        buildChildNodesList(config);

        return node;
    }
}
