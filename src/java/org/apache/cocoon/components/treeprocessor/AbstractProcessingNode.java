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
package org.apache.cocoon.components.treeprocessor;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: AbstractProcessingNode.java,v 1.4 2004/05/25 14:27:32 cziegeler Exp $
 */

public abstract class AbstractProcessingNode extends AbstractLogEnabled implements ProcessingNode {

    protected String location = "unknown location";

    /**
     * Get the location of this node.
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Set the location of this node.
     */
    public void setLocation(String location) {
        this.location = location;
    }
}
