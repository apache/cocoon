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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.treeprocessor.AbstractParentProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.ProcessingNodeBuilder;

/**
 * Builds all nodes below the top-level &lt;sitemap&gt; element, and returns the
 * &lt;pipelines&gt; node. There is no node for &gt;sitemap&lt; since no processing
 * occurs at this level.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: SitemapNodeBuilder.java,v 1.2 2003/08/12 15:48:02 sylvain Exp $
 */

public class SitemapNodeBuilder extends AbstractParentProcessingNodeBuilder implements ThreadSafe {
    
    // Name of children that have to be built in a particular order.
    // For example, views have to be built before resources and both before pipelines.
    private static final String[] orderedNames = { "components", "views", "resources" };

    public ProcessingNode buildNode(Configuration config) throws Exception {
        
        // Start by explicitely ordered children
        for (int i = 0; i < orderedNames.length; i++) {
            Configuration childConfig = config.getChild(orderedNames[i], false);
            if (childConfig != null) {
                ProcessingNodeBuilder builder = this.treeBuilder.createNodeBuilder(childConfig);
                // Don't build them since "pipelines" is not present in this list
                builder.buildNode(childConfig);
            }
        }
        
        ProcessingNode pipelines = null;

        // Now build all those that have no particular order
        Configuration[] childConfigs = config.getChildren();
        
        loop: for (int i = 0; i < childConfigs.length; i++) {
            
            Configuration childConfig = childConfigs[i];
            if (isChild(childConfig)) {
                // Is it in the ordered list ?
                for (int j = 0; j < orderedNames.length; j++) {
                    if (orderedNames[j].equals(childConfig.getName())) {
                        // yep : already built above
                        continue loop;
                    }
                }
                
                ProcessingNodeBuilder builder = this.treeBuilder.createNodeBuilder(childConfig);
                ProcessingNode node = builder.buildNode(childConfig);
                if (node instanceof PipelinesNode) {
                    if (pipelines != null) {
                        String msg = "Only one 'pipelines' is allowed, at " + childConfig.getLocation();
                        throw new ConfigurationException(msg);
                    }
                    pipelines = node;
                }
            }
        }

        if (pipelines == null) {
            String msg = "Invalid sitemap : there must be a 'pipelines' at " + config.getLocation();
            throw new ConfigurationException(msg);
        }

        return pipelines;
    }
}
