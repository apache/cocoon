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
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.configuration.Settings;

/**
 *
 * @version $Id$
 */
public class MountNodeBuilder
    extends AbstractProcessingNodeBuilder
    implements ThreadSafe {

    /** This builder has no parameters -- return <code>false</code> */
    protected boolean hasParameters() {
        return false;
    }

    public ProcessingNode buildNode(Configuration config) throws Exception {
        final Settings settings = (Settings)manager.lookup(Settings.ROLE);
        MountNode node = new MountNode(
            VariableResolverFactory.getResolver(config.getAttribute("uri-prefix"), manager),
            VariableResolverFactory.getResolver(config.getAttribute("src"), manager),
            this.treeBuilder.getProcessor().getWrappingProcessor(),
            config.getAttributeAsBoolean("check-reload", settings.isReloadingEnabled("sitemap")),
            config.getAttributeAsBoolean("pass-through", false)
        );
  
        return (this.treeBuilder.setupNode(node, config));
    }
}
