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

*/
package org.apache.cocoon.components.treeprocessor.sitemap;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.Environment;

/**
 * Handler for &lt;map:flow&gt; element in the sitemap.
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @since September 13, 2002
 * @version CVS $Id: FlowNode.java,v 1.2 2003/03/16 17:49:13 vgritsenko Exp $
 */
public class FlowNode extends AbstractProcessingNode
  implements Composable, Contextualizable
{
  ComponentManager manager;
  String language;
  Context context;
  Interpreter interpreter;

  public FlowNode(String language)
  {
    this.language = language;
  }
  
  /**
   * This method should never be called by the TreeProcessor, since a
   * <code>&lt;map:flow&gt;</code> element should not be in an
   * "executable" sitemap node.
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

  public void contextualize(org.apache.avalon.framework.context.Context context)
    throws ContextException
  {
    this.context = (Context)context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
  }

  /**
   *
   * Lookup an flow {@link
   * org.apache.cocoon.components.flow.Interpreter} instance to hold
   * the scripts defined within the <code>&lt;map:flow&gt;</code> in
   * the sitemap.
   *
   * @param manager a <code>ComponentManager</code> value
   */
  public void compose(ComponentManager manager)
    throws ComponentException
  {
    this.manager = manager;
    
    try {
      ComponentSelector selector
        = (ComponentSelector)manager.lookup(Interpreter.ROLE);
      // Obtain the Interpreter instance for this language
      interpreter = (Interpreter)selector.select(language);
    }
    catch (Exception ex) {
      throw new ComponentException("ScriptNode: Couldn't obtain a flow "
                                   + "interpreter for " + language
                                   + ": " + ex);
    }
  }

  public Interpreter getInterpreter()
  {
    return interpreter;
  }
}
