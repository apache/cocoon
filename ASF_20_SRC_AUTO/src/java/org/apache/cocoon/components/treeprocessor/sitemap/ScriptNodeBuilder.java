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
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.LinkedProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.flow.Interpreter;

/**
 * Builder class for creating a {@link ScriptNode} instance
 * corresponding to a &lt;map:script&gt; element in the sitemap.
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @since March 13, 2002
 * @version CVS $Id: ScriptNodeBuilder.java,v 1.3 2004/03/05 13:02:52 bdelacretaz Exp $
 */
public class ScriptNodeBuilder
  extends AbstractProcessingNodeBuilder
  implements LinkedProcessingNodeBuilder
{
  protected ScriptNode node;

  public ProcessingNode buildNode(Configuration config)
    throws Exception
  {
    String source = config.getAttribute("src");

    this.node = new ScriptNode(source);
    this.treeBuilder.setupNode(this.node, config);

    return this.node;
  }

  /**
   * Call the built node to register the script it contains with the
   * flow interpreter.
   */
  public void linkNode()
    throws Exception
  {
    FlowNode flowNode = (FlowNode)this.treeBuilder.getRegisteredNode("flow");
    Interpreter interpreter = flowNode.getInterpreter();

    this.node.registerScriptWithInterpreter(interpreter);
  }
}
