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
import org.apache.cocoon.reading.Reader;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id$
 */

public class ReadNodeBuilder extends AbstractProcessingNodeBuilder implements ThreadSafe {

    public ProcessingNode buildNode(Configuration config) throws Exception {

        String type = this.treeBuilder.getTypeForStatement(config, Reader.ROLE + "Selector");

        ReadNode node = new ReadNode(
            type,
            VariableResolverFactory.getResolver(config.getAttribute("src", null), this.manager),
            VariableResolverFactory.getResolver(config.getAttribute("mime-type", null), this.manager),
            config.getAttributeAsInteger("status-code", -1)
        );

        return this.treeBuilder.setupNode(node, config);
    }
}
