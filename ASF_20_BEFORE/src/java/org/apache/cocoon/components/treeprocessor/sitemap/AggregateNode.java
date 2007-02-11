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

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.ContentAggregator;
import org.apache.cocoon.sitemap.PatternException;

/**
 *
 * View-handling in aggregation :
 * <ul>
 * <li>map:aggregate can have a label, but doesn't match view from-position="first" like generators
 * </li>
 * <li>each map:part can have a label
 * </li>
 * <li>if at least one of the parts has a label matching the current view, only parts matching
 *     this view are added. Otherwise, all parts are added.
 * </li>
 * </ul>
 * For more info on aggregation and views, see the mail archive
 * <a href="http://marc.theaimsgroup.com/?l=xml-cocoon-dev&m=100525751417953">here</a> or
 * <a href="http://marc.theaimsgroup.com/?l=xml-cocoon-dev&m=100517130418424">here</a>.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: AggregateNode.java,v 1.3 2004/03/01 03:50:58 antonio Exp $
 */

public class AggregateNode extends AbstractProcessingNode {

    private VariableResolver element;
    private VariableResolver nsURI;
    private VariableResolver nsPrefix;

    /** All parts */
    private Part[] allParts;

    /** Pre-filtered Part[] for views that have a matching label in any of the parts */
    private Map viewParts;

    /** View nodes to jump to */
    private Map viewNodes;

    public AggregateNode(VariableResolver element, VariableResolver nsURI, VariableResolver nsPrefix) throws PatternException {
        this.element = element;
        this.nsURI = nsURI;
        this.nsPrefix = nsPrefix;
    }

    public void setParts(Part[] allParts, Map viewParts) {
        this.allParts = allParts;
        this.viewParts = viewParts;
    }

    public void setViewNodes(Map viewNodes) {
        this.viewNodes = viewNodes;
    }

    public boolean invoke(Environment env, InvokeContext context)
      throws Exception {

        boolean infoEnabled = getLogger().isInfoEnabled();

        Map objectModel = env.getObjectModel();

        // Setup aggregator
        ProcessingPipeline processingPipeline = context.getProcessingPipeline();

        processingPipeline.setGenerator("<aggregator>", null, Parameters.EMPTY_PARAMETERS, Parameters.EMPTY_PARAMETERS);

        ContentAggregator aggregator = (ContentAggregator)processingPipeline.getGenerator();
        aggregator.setRootElement(
            this.element.resolve(context, objectModel),
            this.nsURI.resolve(context, objectModel),
            this.nsPrefix.resolve(context, objectModel)
        );

        // Get actual parts, potentially filtered by the view
        Part[] actualParts;

        String cocoonView = env.getView();
        if (cocoonView == null) {
            // Keep all parts
            actualParts = this.allParts;

        } else {
            // Are there some parts that match this view ?
            actualParts = (Part[])this.viewParts.get(cocoonView);

            // If not, keep all parts
            if (actualParts == null) {
                actualParts = this.allParts;
            }
        }

        // Add parts
        for (int i = 0; i < actualParts.length; i++) {
            Part part = actualParts[i];
            if (part != null) {
                aggregator.addPart(
                    part.source.resolve(context, objectModel),
                    part.element.resolve(context, objectModel),
                    part.nsURI.resolve(context, objectModel),
                    part.stripRoot.resolve(context, objectModel),
                    part.nsPrefix.resolve(context, objectModel)
                );
            }
        }

        // Bug #7196 : Some parts matched the view : jump to that view
        if (actualParts != this.allParts) {
            ProcessingNode viewNode = (ProcessingNode)this.viewNodes.get(cocoonView);
            if (viewNode != null) {
                if (infoEnabled) {
                    getLogger().info("Jumping to view '" + cocoonView + "' from aggregate part at " + this.getLocation());
                }
                return viewNode.invoke(env, context);
            }
        }

        // Check aggregate-level view
        if (cocoonView != null && this.viewNodes != null) {
            ProcessingNode viewNode = (ProcessingNode)this.viewNodes.get(cocoonView);
            if (viewNode != null) {
                if (infoEnabled) {
                    getLogger().info("Jumping to view '" + cocoonView + "' from aggregate at " + this.getLocation());
                }
                return viewNode.invoke(env, context);
            }
        }

        // Return false to continue sitemap invocation
        return false;
    }

    public static class Part {
        public Part(
            VariableResolver source,
            VariableResolver element,
            VariableResolver nsURI,
            VariableResolver nsPrefix,
            VariableResolver stripRoot)
          throws PatternException {
            this.source = source;
            this.element = element;
            this.nsURI = nsURI;
            this.nsPrefix = nsPrefix;
            this.stripRoot = stripRoot;
        }

        protected VariableResolver source;
        protected VariableResolver element;
        protected VariableResolver nsURI;
        protected VariableResolver nsPrefix;
        protected VariableResolver stripRoot;
        
    }
}
