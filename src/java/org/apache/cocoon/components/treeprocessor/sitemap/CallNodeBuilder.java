/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.treeprocessor.sitemap;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.CategoryNode;
import org.apache.cocoon.components.treeprocessor.CategoryNodeBuilder;
import org.apache.cocoon.components.treeprocessor.LinkedProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @version CVS $Id: CallNodeBuilder.java,v 1.1 2003/03/09 00:09:21 pier Exp $
 */

public class CallNodeBuilder extends AbstractProcessingNodeBuilder
  implements LinkedProcessingNodeBuilder {

    protected ProcessingNode node;
    protected String resourceName;
    protected String functionName;
    protected String continuationId;

    public ProcessingNode buildNode(Configuration config)
        throws Exception
    {
        resourceName = config.getAttribute("resource", null);
        functionName = config.getAttribute("function", null);
        continuationId = config.getAttribute("continuation", null);

        if (resourceName == null) {
          if (functionName == null && continuationId == null)
            throw new ConfigurationException("<map:call> must have either a 'resource', a 'function' or a 'continuation' attribute!");

          node = new CallFunctionNode(functionName, continuationId);
        }
        else {
            if (functionName != null || continuationId != null)
              throw new ConfigurationException("<map:call> can be used to call either a resource, or a function/continuation in the control flow! Please specify either <map:call resource=\"...\"/> or <map:call function=\"...\" continuation=\"...\"/>.");
            node = new CallNode();
        }

        this.treeBuilder.setupNode(this.node, config);
        if (node instanceof Configurable)
            ((Configurable)this.node).configure(config);

        return this.node;
    }

    public void linkNode()
        throws Exception
    {
      if (resourceName != null) {
        // We have a <map:call resource="..."/>
        CategoryNode resources
            = CategoryNodeBuilder.getCategoryNode(treeBuilder, "resources");

        if (resources == null)
            throw new ConfigurationException("This sitemap contains no resources. Cannot call at " + node.getLocation());

        ((CallNode)this.node).setResource(resources, this.resourceName);
      }
      else {
        // We have a <map:call> with either "function" or
        // "continuation", or both specified

        // Check to see if a flow has been defined in this sitemap
        FlowNode flow = (FlowNode)treeBuilder.getRegisteredNode("flow");
        if (flow == null)
            throw new ConfigurationException("This sitemap contains no control flows defined, cannot call at " + node.getLocation() + ". Define a control flow using <map:flow>, with embedded <map:script> elements.");

        // Get the Interpreter instance and set it up in the
        // CallFunctionNode function
        Interpreter interpreter = flow.getInterpreter();
        ((CallFunctionNode)node).setInterpreter(interpreter);
      }
    }
}
