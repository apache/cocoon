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
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.treeprocessor.AbstractParentProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.SimpleSelectorProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.matching.PreparableMatcher;

/**
 *
 * @version $Id$
 */
public class MatchNodeBuilder extends AbstractParentProcessingNodeBuilder
  implements ThreadSafe {

    public ProcessingNode buildNode(Configuration config) throws Exception {

        String pattern = config.getAttribute("pattern", null);
        String name = config.getAttribute("name", null);

        String type = this.treeBuilder.getTypeForStatement(config, Matcher.ROLE);

        // Get the type and class for this matcher
        ServiceSelector selector = (ServiceSelector)this.manager.lookup(Matcher.ROLE + "Selector");

        Class clazz = null;
        try {
            // Find matcher class
            Matcher matcher = (Matcher)selector.select(type);
            clazz = matcher.getClass();
            selector.release(matcher);
        } finally {
            this.manager.release(selector);
        }

        // PreparableMatcher are only prepared if pattern doesn't need request-time resolution.
        boolean preparable =
            PreparableMatcher.class.isAssignableFrom(clazz) &&
            !VariableResolverFactory.needsResolve(pattern);

        // Instanciate appropriate node
        SimpleSelectorProcessingNode node;
        VariableResolver patternResolver = VariableResolverFactory.getResolver(pattern, this.manager);

        if (preparable) {
            node = new PreparableMatchNode(type, VariableResolverFactory.unescape(pattern),name);
        } else {
            node = new MatchNode(type, patternResolver,name);
        }

        this.treeBuilder.setupNode(node, config);

        // Get all children
        ProcessingNode[] children = buildChildNodes(config);

        node.setChildren(children);

        return node;
    }
}
