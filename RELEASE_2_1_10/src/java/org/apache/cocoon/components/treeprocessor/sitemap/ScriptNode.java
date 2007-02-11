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

import org.apache.cocoon.components.flow.AbstractInterpreter;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.environment.Environment;

/**
 * Handler for &lt;map:script&gt; elements in the sitemap. It registers the 
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @since March 13, 2002
 * @version CVS $Id$
 */
public class ScriptNode extends AbstractProcessingNode
{
  String source;

  public ScriptNode(String source)
  {
    this.source = source;
  }
  
  /**
   * This method should never be called by the TreeProcessor, since a
   * <map:script> element should not be in an "executable" sitemap
   * node.
   *
   * @param env an <code>Environment</code> value
   * @param context an <code>InvokeContext</code> value
   * @return a <code>boolean</code> value
   * @exception Exception if an error occurs
   */
  public boolean invoke(Environment env, InvokeContext context)
    throws Exception
  {
    return true;
  }

  public void registerScriptWithInterpreter(Interpreter interpreter)
  {
    if (interpreter instanceof AbstractInterpreter)
      ((AbstractInterpreter)interpreter).register(source);
  }
}
