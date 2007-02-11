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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.source.SourceDescriptor;
import org.apache.cocoon.components.source.SourceInspector;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AggregatedValidity;

/**
 * This source descriptor acts as container for a set of source inspectors/descriptors.
 * 
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: SourceDescriptorManager.java,v 1.8 2004/03/05 13:02:21 bdelacretaz Exp $
 */
public final class SourceDescriptorManager extends AbstractLogEnabled 
implements SourceDescriptor, Contextualizable, Serviceable, 
Configurable, Initializable, Disposable, ThreadSafe {
    
    // the registered inspectors
    private Set m_inspectors;
    
    private Context m_context;
    private ServiceManager m_manager;
    private Configuration m_configuration;
    
    
    // ---------------------------------------------------- lifecycle
    
    public SourceDescriptorManager() {
    }
    
    public void contextualize(Context context) throws ContextException {
        m_context = context;
    }
    
    public void service(ServiceManager manager) {
        m_manager = manager;
    }
        
    public void configure(Configuration configuration) throws ConfigurationException {
        m_configuration = configuration;
    }
    
    public void initialize() throws Exception {
        m_inspectors = new HashSet();
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final Configuration[] children = m_configuration.getChildren();
        
        for (int i = 0; i < children.length; i++) {
            String className = children[i].getAttribute("class","");
            SourceInspector inspector;
            try {
                final Class inspectorClass = classloader.loadClass(className);
                inspector = (SourceInspector) inspectorClass.newInstance();
            } catch (InstantiationException ie) {
                throw new ConfigurationException(
                    "Could not instantiate class "+className, ie);
            } catch (ClassNotFoundException cnfe) {
                throw new ConfigurationException(
                    "Could not load class "+className, cnfe);
            } catch (IllegalAccessException iae) {
                 throw new ConfigurationException(
                    "Could not load class "+className, iae);
            }
            ContainerUtil.enableLogging(inspector,getLogger());
            ContainerUtil.contextualize(inspector,m_context);
            ContainerUtil.service(inspector,m_manager);
            ContainerUtil.configure(inspector,children[i]);
            ContainerUtil.parameterize(inspector,
                Parameters.fromConfiguration(children[i]));
            ContainerUtil.initialize(inspector);
            
            m_inspectors.add(inspector);
        }
        // done with these
        m_configuration = null;
        m_context = null;
        m_manager = null;
    }
    
    public void dispose() {
        Iterator iter = m_inspectors.iterator();
        while(iter.hasNext()) {
            ContainerUtil.dispose(iter.next());
        }
        m_inspectors = null;
    }
    
    
    // ---------------------------------------------------- SourceDescriptor implementation
    
    /**
     * Loops over the registered inspectors until it finds the property.
     */
    public SourceProperty getSourceProperty(Source source, String namespace, String name) 
            throws SourceException {

        final Iterator inspectors = m_inspectors.iterator();
        while (inspectors.hasNext()) {
            SourceInspector inspector = (SourceInspector) inspectors.next();
            SourceProperty property = inspector.getSourceProperty(source,namespace,name);
            if (property != null) {
                return property;
            }
        }
        return null;
    }
    
    /**
     * Aggregate all properties of all registered inspectors.
     */
    public SourceProperty[] getSourceProperties(Source source) throws SourceException {
        final Set result = new HashSet();
        SourceInspector inspector;
        SourceProperty[] properties;
        final Iterator inspectors = m_inspectors.iterator();
        while (inspectors.hasNext()) {
            inspector = (SourceInspector) inspectors.next();
            properties = inspector.getSourceProperties(source);
            if (properties != null) {
                result.addAll(Arrays.asList(properties));
            }
        }
        return (SourceProperty[]) result.toArray(new SourceProperty[result.size()]);
    }
    
    /**
     * Check if there is an inspector that handles properties of 
     * the given type.
     */
    public boolean handlesProperty(String namespace, String name) {
        SourceInspector inspector;
        final Iterator inspectors = m_inspectors.iterator();
        while(inspectors.hasNext()) {
            inspector = (SourceInspector) inspectors.next();
            if (inspector.handlesProperty(namespace,name)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Loops over the registered descriptors and delegates the call.
     */
    public void removeSourceProperty(Source source, String ns, String name) throws SourceException {
        SourceInspector inspector;
        final Iterator inspectors = m_inspectors.iterator();
        while (inspectors.hasNext()) {
            inspector = (SourceInspector) inspectors.next();
            if (inspector instanceof SourceDescriptor) {
                ((SourceDescriptor) inspector).removeSourceProperty(source,ns,name);
            }
        }
    }
    
    /**
     * Loops over the registered descriptors and calls delegates the call.
     */
    public void setSourceProperty(Source source, SourceProperty property) throws SourceException {
        SourceInspector inspector;
        final Iterator inspectors = m_inspectors.iterator();
        while (inspectors.hasNext()) {
            inspector = (SourceInspector) inspectors.next();
            if (inspector instanceof SourceDescriptor) {
                ((SourceDescriptor) inspector).setSourceProperty(source,property);
            }
        }
    }
    
    /**
     * Returns an aggregate validity describing the validity of all the properties.
     */
    public SourceValidity getValidity(Source source) {
        AggregatedValidity validity = new AggregatedValidity();
        SourceInspector inspector;
        final Iterator inspectors = m_inspectors.iterator();
        while (inspectors.hasNext()) {
            inspector = (SourceInspector) inspectors.next();
            SourceValidity sv = inspector.getValidity(source);
            if (sv == null) {
                return null;
            }
            validity.add(sv);
        }
        return validity;
    }
}

