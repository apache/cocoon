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
package org.apache.cocoon.components.source.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.components.source.SourceInspector;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

/**
 * Abstract base class for SourceInspectors that want to 
 * configure the set of properties they handle beforehand.
 * 
 * <p>
 * Knowing which properties an inspector handles beforehand
 * greatly improves property management performance.
 * </p>
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 */
public abstract class AbstractConfigurableSourceInspector extends AbstractLogEnabled 
implements SourceInspector, Configurable {

    // the set of properties this inspector is configured to handle
    private Set m_properties;


    // ---------------------------------------------------- lifecycle

    public AbstractConfigurableSourceInspector() {
    }

    /**
     * Configure this source inspector to handle properties of required types.
     * <p>
     *  Configuration is in the form of a set of property elements as follows:<br>
     *  <code>&lt;property name="owner" namespace="meta"&gt;</code>
     * </p>
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        final Configuration[] properties = configuration.getChildren("property");
        m_properties = new HashSet(properties.length);
        for (int i = 0; i < properties.length; i++) {
            String namespace = properties[i].getAttribute("namespace");
            String name = properties[i].getAttribute("name");
            if (namespace.indexOf('#') != -1 || name.indexOf('#') != -1) {
                final String message = "Illegal character '#' in definition at " 
                    + properties[i].getLocation();
                throw new ConfigurationException(message);
            }
            String property = namespace + "#" + name;
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Handling '" + property + "'");
            }
            m_properties.add(property);
        }
    }

    // ---------------------------------------------------- SourceInspector methods

    /**
     * Iterates over the configured set of properties to handle,
     * for each property calls <code>doGetSourceProperty()</code>,
     * and returns the list of properties thus obtained. Subclasses
     * may want to overide this behavior to improve performance.
     */
    public SourceProperty[] getSourceProperties(Source source) throws SourceException {
        final Set result = new HashSet();
        final Iterator properties = m_properties.iterator();
        while (properties.hasNext()) {
            String property = (String) properties.next();
            int index = property.indexOf('#');
            String namespace = property.substring(0,index);
            String name      = property.substring(index+1);
            SourceProperty sp = doGetSourceProperty(source,namespace,name);
            if (sp != null) {
                result.add(sp);
            }
        }
        return (SourceProperty[]) result.toArray(new SourceProperty[result.size()]);
    }

    /**
     * Checks if this inspector is configured to handle the requested property
     * and if so forwards the call to <code>doGetSourceProperty</code>.
     */
    public final SourceProperty getSourceProperty(Source source, String namespace, String name) 
        throws SourceException {
        
        if (handlesProperty(namespace,name)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Getting property " + namespace + "#" 
                    + name + " for source " + source.getURI());
            }
            return doGetSourceProperty(source,namespace,name);
        }
        return null;
    }

    // ---------------------------------------------------- abstract methods

    /**
     * Do the actual work of getting the requested SourceProperty for the given Source.
     */
    protected abstract SourceProperty doGetSourceProperty(Source source, String ns, String name)
        throws SourceException;


    // ---------------------------------------------------- utility methods

    /**
     * Check if this inspector is configured to handle properties of 
     * the given type.
     */
    public final boolean handlesProperty(String namespace, String name) {
        String propname;
        if (namespace == null) {
            propname = "#" + name;
        }
        else {
            propname = namespace + "#" + name;
        }
        return m_properties.contains(propname);
    }
    
    /**
     * Provide subclasses access to the set of configured properties.
     */
    protected final Set getPropertyTypes() {
        return m_properties;
    }

}
