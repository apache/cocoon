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
package org.apache.cocoon.components.source.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.source.SourceDescriptor;
import org.apache.cocoon.components.source.SourceInspector;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AggregatedValidity;

/**
 * This source descriptor acts as container for a set of source inspectors.
 * 
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: SourceDescriptorManager.java,v 1.4 2003/10/28 13:48:12 unico Exp $
 */
public final class SourceDescriptorManager extends AbstractLogEnabled 
implements SourceDescriptor, Contextualizable, Composable, 
Configurable, Initializable, Disposable, ThreadSafe {
    
    // the registered inspectors
    private Set m_inspectors;
    
    private Context m_context;
    private ComponentManager m_manager;
    private Configuration m_configuration;
    
    
    // ---------------------------------------------------- lifecycle
    
    public SourceDescriptorManager() {
    }
    
    public void contextualize(Context context) throws ContextException {
        m_context = context;
    }
    
    public void compose(ComponentManager manager) {
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
            ContainerUtil.compose(inspector,m_manager);
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
            if (inspector instanceof SourceDescriptor) {
                SourceValidity sv = ((SourceDescriptor) inspector).getValidity(source);
                if (sv == null) {
                    return null;
                }
                validity.add(sv);
            }
        }
        return validity;
    }
}

