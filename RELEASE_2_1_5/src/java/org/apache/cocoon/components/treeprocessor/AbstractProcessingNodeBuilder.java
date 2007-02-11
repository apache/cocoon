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

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Recomposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.sitemap.SitemapParameters;

import java.util.Map;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: AbstractProcessingNodeBuilder.java,v 1.4 2004/03/08 12:07:39 cziegeler Exp $
 */


public abstract class AbstractProcessingNodeBuilder extends AbstractLogEnabled
  implements ProcessingNodeBuilder, Recomposable {

    protected TreeBuilder treeBuilder;
    
    protected ComponentManager manager;

    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

    public void recompose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

    public void setBuilder(TreeBuilder treeBuilder) {
        this.treeBuilder = treeBuilder;
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
     * Get &lt;xxx:parameter&gt; elements as a <code>Map</code> of </code>ListOfMapResolver</code>s,
     * that can be turned into parameters using <code>ListOfMapResolver.buildParameters()</code>.
     *
     * @return the Map of ListOfMapResolver, or <code>null</code> if there are no parameters.
     */
    protected Map getParameters(Configuration config) throws ConfigurationException {
        Configuration[] children = config.getChildren("parameter");

        if (children.length == 0) {
            // Parameters are only the component's location
            // TODO Optimize this
            return new SitemapParameters.ExtendedHashMap(config);
        }

        Map params = new SitemapParameters.ExtendedHashMap(config, children.length+1);
        for (int i = 0; i < children.length; i++) {
            Configuration child = children[i];
            if (true) { // FIXME : check namespace
                String name = child.getAttribute("name");
                String value = child.getAttribute("value");
                try {
                    params.put(
                        VariableResolverFactory.getResolver(name, this.manager),
                        VariableResolverFactory.getResolver(value, this.manager));
                } catch(PatternException pe) {
                    String msg = "Invalid pattern '" + value + " at " + child.getLocation();
                    throw new ConfigurationException(msg, pe);
                }
            }
        }

        return params;
    }

    /**
     * Check if the namespace URI of the given configuraition is the same as the
     * one given by the builder.
     */
    protected void checkNamespace(Configuration config) throws ConfigurationException {
        if (!this.treeBuilder.getNamespace().equals(config.getNamespace()))
        {
            String msg = "Invalid namespace '" + config.getNamespace() + "' at " + config.getLocation();
            throw new ConfigurationException(msg);
        }
    }
}
