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
 * @version CVS $Id: AggregateNode.java,v 1.5 2004/06/09 11:59:23 cziegeler Exp $
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
        super(null);
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
