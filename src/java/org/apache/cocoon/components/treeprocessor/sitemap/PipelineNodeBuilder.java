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

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.treeprocessor.AbstractParentProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.ProcessingNodeBuilder;

/**
 * Builds a &lt;map:pipeline&gt;
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 * @version CVS $Id: PipelineNodeBuilder.java,v 1.3 2004/03/01 03:50:58 antonio Exp $
 */

public class PipelineNodeBuilder
    extends AbstractParentProcessingNodeBuilder
    implements ThreadSafe {

    /** This builder can have parameters -- return <code>true</code> */
    protected boolean hasParameters() {
        return true;
    }

    public ProcessingNode buildNode(Configuration config)
    throws Exception {
        PipelineNode node = new PipelineNode( config );

        this.treeBuilder.setupNode(node, config);
        node.setInternalOnly(config.getAttributeAsBoolean("internal-only", false));
        
        // Main (with no "type" attribute) error handler : new in Cocoon 2.1, must have a generator
        ProcessingNode mainHandler = null;
        
        // 404 & 500 error handlers as in Cocoon 2.0.x, have an implicit generator
        ProcessingNode error404Handler = null;
        ProcessingNode error500Handler = null;
        
        Configuration[] childConfigs = config.getChildren();
        List children = new ArrayList();
        for (int i = 0; i < childConfigs.length; i++) {

            Configuration childConfig = childConfigs[i];
            if (isChild(childConfig)) {

                ProcessingNodeBuilder builder = this.treeBuilder.createNodeBuilder(childConfig);
                if (builder instanceof HandleErrorsNodeBuilder) {
                    // Error handler : check type
                    HandleErrorsNode handler = (HandleErrorsNode)builder.buildNode(childConfig);
                    int type = handler.getStatusCode();

					switch(type) {
					    case -1: // main handler (needs generator)
					        if (mainHandler != null) {
					            throw new ConfigurationException("Duplicate <handle-errors> at " + handler.getLocation());
					        } else if (error500Handler != null || error404Handler != null) {
					            throw new ConfigurationException("Cannot mix <handle-errors> with and without 'type' attribute at " +
					          	    handler.getLocation());
					        } else {
					            mainHandler = handler;
					        }
					    break;
					    
					    case 404:
					        if (error404Handler != null) {
					            throw new ConfigurationException("Duplicate <handle-errors type='404' at " + handler.getLocation());
					        } else if(mainHandler != null) {
                                throw new ConfigurationException("Cannot mix <handle-errors> with and without 'type' attribute at " +
                                    handler.getLocation());
					        } else {
					            error404Handler = handler;
					        }
					    break;
					    
					    case 500:
					    	if (error500Handler != null) {
                                throw new ConfigurationException("Duplicate <handle-errors type='500' at " + handler.getLocation());
					    	} else if (mainHandler != null) {
                                throw new ConfigurationException("Cannot mix <handle-errors> with and without 'type' attribute at " +
                                    handler.getLocation());
                            } else {
                                error500Handler = handler;
                            }
					    break;
					    
					    default:
					    	throw new ConfigurationException("Unknown handle-errors type (" + type + ") at " + handler.getLocation());
					}
                } else {
                    // Regular builder
                    children.add(builder.buildNode(childConfig));
                }
            }
        }
        node.setChildren(toNodeArray(children));
        node.set404Handler(error404Handler);
        // Set either main or error500 handler as only one can exist
        node.set500Handler(error500Handler == null ? mainHandler : error500Handler);
        return node;
    }



}

