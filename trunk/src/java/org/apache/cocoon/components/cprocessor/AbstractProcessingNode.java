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
package org.apache.cocoon.components.cprocessor;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.cprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.sitemap.PatternException;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: AbstractProcessingNode.java,v 1.5 2004/03/08 13:57:39 cziegeler Exp $
 */
public abstract class AbstractProcessingNode extends AbstractNode 
implements ProcessingNode, Configurable {

    private static final String PARAMETER_ELEMENT = "parameter";
    private static final String PARAMETER_NAME_ATTR = "name";
    private static final String PARAMETER_VALUE_ATTR = "value";
    
    protected Map m_parameters;
    
    public AbstractProcessingNode() {
    }
    
    public void configure(final Configuration config) throws ConfigurationException {
        super.configure(config);
        if (hasParameters()) {
            setParameters(config);
        }
    }
    
    /**
     * Parametrizable ProcessingNodes can overide this method to
     * have resolvable parameters set at configuration time.
     * 
     * @return  whether this processing node is parametrizable.
     */
    protected boolean hasParameters() {
        return false;
    }
    
    /**
     * Set &lt;xxx:parameter&gt; elements as a <code>Map</code> of </code>ListOfMapResolver</code>s,
     * that can be turned into parameters using <code>ListOfMapResolver.buildParameters()</code>.
     *
     * @return the Map of ListOfMapResolver, or <code>null</code> if there are no parameters.
     */
    private final void setParameters(Configuration config) throws ConfigurationException {
        final Configuration[] children = config.getChildren(PARAMETER_ELEMENT);
        if (children.length == 0) {
            return;
        }
        m_parameters = new HashMap();
        for (int i = 0; i < children.length; i++) {
            Configuration child = children[i];
            String name = child.getAttribute(PARAMETER_NAME_ATTR);
            String value = child.getAttribute(PARAMETER_VALUE_ATTR);
            try {
                m_parameters.put(
                    VariableResolverFactory.getResolver(name, m_manager),
                    VariableResolverFactory.getResolver(value, m_manager));
            } catch(PatternException pe) {
                String msg = "Invalid pattern '" + value + "' at " + getConfigLocation(child);
                throw new ConfigurationException(msg, pe);
            }
        }
    }
    
    /**
     * Check if the namespace URI of the given configuraition is the same as the
     * one given by the builder.
     */
    protected final void checkNamespace(Configuration config) throws ConfigurationException {
        if (TreeProcessor.SITEMAP_NS.equals(config.getNamespace())) {
            String msg = "Invalid namespace '" + config.getNamespace() + "' at " + getConfigLocation(config);
            throw new ConfigurationException(msg);
        }
    }

}
