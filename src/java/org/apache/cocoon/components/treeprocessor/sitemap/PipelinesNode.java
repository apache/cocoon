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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.SimpleParentProcessingNode;
import org.apache.cocoon.components.treeprocessor.TreeProcessor;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Redirector;

/**
 * Handles &lt;map:pipelines&gt;
 *
 * @author <a href="mailto:juergen.seitz@basf-it-services.com">J&uuml;rgen Seitz</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: PipelinesNode.java,v 1.8 2004/01/19 14:57:14 joerg Exp $
 */

public final class PipelinesNode extends SimpleParentProcessingNode
  implements Composable, Disposable {

    private ComponentManager manager;
    
    private ErrorHandlerHelper errorHandlerHelper = new ErrorHandlerHelper();

    private ProcessingNode errorHandler;

    /**
     * Constructor
     */
    public PipelinesNode() {
    }

    /**
     * Keep the component manager used everywhere in the tree so that we can
     * cleanly dispose it.
     */
    public void compose(ComponentManager manager) {
        this.manager = manager;
        this.errorHandlerHelper.compose(manager);
    }

    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        this.errorHandlerHelper.enableLogging(logger);
    }

    public void setErrorHandler(ProcessingNode node) {
        this.errorHandler = node;
    }
    
    public void setChildren(ProcessingNode[] nodes) {
        // Mark the last pipeline so that it can throw a ResourceNotFoundException
        ((PipelineNode)nodes[nodes.length - 1]).setLast(true);

        super.setChildren(nodes);
    }

    public static Redirector getRedirector(Environment env) {
        return (Redirector)env.getAttribute(TreeProcessor.REDIRECTOR_ATTR);
    }

    /**
     * Process the environment. Also adds a <code>SourceResolver</code>
     * and a <code>Redirector</code> in the object model. The previous resolver and
     * redirector, if any, are restored before return.
     */
    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {
    
        // Perform any common invoke functionality 
        super.invoke(env, context);

        // Recompose context (and pipelines) to the local component manager
        context.recompose(this.manager);

        try {
            // FIXME : is there any useful information that can be passed as top-level parameters,
            //         such as the URI of the mount point ?

            return invokeNodes(this.children, env, context);
        } catch (Exception ex) {
            if (this.errorHandler != null) {
                // Invoke pipelines handler
                return this.errorHandlerHelper.invokeErrorHandler(this.errorHandler, ex, env);
            } else {
                // No handler : propagate
                throw ex;
            }
        }
    }

    /**
     * Dispose the component manager.
     */
    public void dispose() {
        if (this.manager instanceof Disposable) {
            ((Disposable)this.manager).dispose();
        }
        this.manager = null;
    }
}
