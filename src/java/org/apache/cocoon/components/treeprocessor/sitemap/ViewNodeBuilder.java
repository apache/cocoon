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
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.treeprocessor.NamedContainerNodeBuilder;
import org.apache.cocoon.components.treeprocessor.NamedProcessingNode;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;

/**
 * Builds a &lt;map:view&gt;
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ViewNodeBuilder.java,v 1.2 2004/03/05 13:02:52 bdelacretaz Exp $
 */

public class ViewNodeBuilder extends NamedContainerNodeBuilder implements ThreadSafe {

    public ProcessingNode buildNode(Configuration config) throws Exception {

        // Get the label or position (pseudo-label) of this view.
        String label = config.getAttribute("from-label", null);

        if (label == null) {
            String position = config.getAttribute("from-position");
            if ("first".equals(position)) {
                label = SitemapLanguage.FIRST_POS_LABEL;
            } else if ("last".equals(position)) {
                label = SitemapLanguage.LAST_POS_LABEL;
            } else {
                String msg = "Bad value for 'from-position' at " + config.getLocation();
                throw new ConfigurationException(msg);
            }
        }

        SitemapLanguage sitemapBuilder = (SitemapLanguage)this.treeBuilder;

        // Indicate to child builders that we're in a view (they won't perform view branching)
        sitemapBuilder.setBuildingView(true);

        // Build children
        NamedProcessingNode result = (NamedProcessingNode)super.buildNode(config);

        sitemapBuilder.addViewForLabel(label, result.getName());

        // Clear the flag
        sitemapBuilder.setBuildingView(false);

        return result;
    }
}
