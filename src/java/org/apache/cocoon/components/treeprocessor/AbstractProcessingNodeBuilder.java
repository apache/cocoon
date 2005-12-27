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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceManager;

/**
 *
 * @version $Id$
 */
public abstract class AbstractProcessingNodeBuilder extends AbstractLogEnabled
                                                    implements ProcessingNodeBuilder {

    protected TreeBuilder treeBuilder;

    protected ServiceManager manager;

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.treeprocessor.ProcessingNodeBuilder#setBuilder(org.apache.cocoon.components.treeprocessor.TreeBuilder)
     */
    public void setBuilder(TreeBuilder treeBuilder) {
        this.treeBuilder = treeBuilder;
        this.manager = treeBuilder.getBuiltProcessorManager();
    }

    /**
     * Does this node accept parameters ? Default is true : if a builder that doesn't
     * have parameters doesn't override this method, erroneous parameters will be silently
     * ignored.
     */
    protected boolean hasParameters() {
        return true;
    }

    /**
     * Check if the namespace URI of the given configuraition is the same as the
     * one given by the builder.
     */
    protected void checkNamespace(Configuration config) throws ConfigurationException {
        if (!this.treeBuilder.getNamespace().equals(config.getNamespace())) {
            String msg = "Invalid namespace '" + config.getNamespace() + "' at " + config.getLocation();
            throw new ConfigurationException(msg);
        }
    }
}
