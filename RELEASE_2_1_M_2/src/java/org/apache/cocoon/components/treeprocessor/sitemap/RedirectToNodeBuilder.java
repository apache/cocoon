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
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.CategoryNode;
import org.apache.cocoon.components.treeprocessor.CategoryNodeBuilder;
import org.apache.cocoon.components.treeprocessor.LinkedProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: RedirectToNodeBuilder.java,v 1.4 2003/05/13 20:31:31 sylvain Exp $
 */

public class RedirectToNodeBuilder extends AbstractProcessingNodeBuilder
  implements LinkedProcessingNodeBuilder {

    private CallNode callNode;
    private String resourceName;

    /** This builder has no parameters -- return <code>false</code> */
    protected boolean hasParameters() {
        return false;
    }

    public ProcessingNode buildNode(Configuration config) throws Exception {
        
		if (((SitemapLanguage)this.treeBuilder).isBuildingErrorHandler()) {
			throw new ConfigurationException("'map:redirect' is forbidden inside a 'map:handle-errors', at "
			+ config.getLocation());
		}
		
        // Is it a redirect to resource ?
        this.resourceName = config.getAttribute("resource", null);
        if (this.resourceName != null) {
            
            getLogger().warn("Redirect to resource is deprecated. Use map:call instead at " +
                config.getLocation());

            this.callNode = new CallNode();
            this.treeBuilder.setupNode(this.callNode, config);

            String target = config.getAttribute("target", null);
            if (target != null) {
                Map params = new HashMap(1);
                params.put("target", VariableResolverFactory.getResolver(target, this.manager));
                this.callNode.setParameters(params);
            }
            return this.callNode;
            
        } else {
            ProcessingNode URINode = new RedirectToURINode(
                VariableResolverFactory.getResolver(config.getAttribute("uri"), this.manager),
                config.getAttributeAsBoolean("session", false),
                config.getAttributeAsBoolean("global", false)
            );
            return this.treeBuilder.setupNode(URINode, config);

        }
    }

    public void linkNode() throws Exception {

        if (this.callNode != null) {
            CategoryNode resources = CategoryNodeBuilder.getCategoryNode(this.treeBuilder, "resources");

            if (resources == null) {
                String msg = "This sitemap contains no resources. Cannot redirect at " +
                    this.callNode.getLocation();
                throw new ConfigurationException(msg);
            }

            this.callNode.setResource(
                resources,
                this.resourceName
            );
        }
    }
}
