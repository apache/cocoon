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
import org.apache.cocoon.components.treeprocessor.AbstractParentProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;

/**
 * Builder of a {@link FlowNode} instance, corresponding to a
 * <code>&lt;map:flow&gt;</code> element in the sitemap.
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @since September 13, 2002
 * @version CVS $Id: FlowNodeBuilder.java,v 1.3 2004/03/05 13:02:52 bdelacretaz Exp $
 */
public class FlowNodeBuilder
  extends AbstractParentProcessingNodeBuilder
{
  public ProcessingNode buildNode(Configuration config)
    throws Exception
  {
    String language = config.getAttribute("language", "JavaScript");
    FlowNode node = new FlowNode(language);

    this.treeBuilder.registerNode("flow", node);
    this.treeBuilder.setupNode(node, config);

    this.buildChildNodesList(config);

    return node;
  }
}
