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
package org.apache.cocoon.components.treeprocessor;

import org.apache.cocoon.sitemap.ExecutionContext;
import org.apache.cocoon.sitemap.SitemapExecutor;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.util.location.Location;

/**
 * Base class for ProcessingNode implementations.
 *
 * @version $Id$
 */
public abstract class AbstractProcessingNode extends AbstractLogEnabled
                                             implements ProcessingNode, ExecutionContext {

    protected Location location = Location.UNKNOWN;

    /** The type of the component */
    protected String componentName;

    /** The sitemap executor */
    protected SitemapExecutor executor;


    public AbstractProcessingNode() {
    }

    public AbstractProcessingNode(String type) {
        this.componentName = type;
    }

    /**
     * Get the location of this node.
     */
    public Location getLocation() {
        return this.location;
    }

    /**
     * Set the location of this node.
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Set the sitemap executor
     */
    public void setSitemapExecutor(SitemapExecutor executor) {
        this.executor = executor;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.ExecutionContext#getType()
     */
    public String getType() {
        return this.componentName;
    }
}
