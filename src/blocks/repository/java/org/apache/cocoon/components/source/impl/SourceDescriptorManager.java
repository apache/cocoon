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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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

/**
 * This source descriptor acts as container for a list registered 
 * source inspectors.
 * 
 * TODO  the manager currently assumes a ThreadSafe lifestyle for
 * the contained inspectors. Consider supporting other lifstyles as well.
 * 
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: SourceDescriptorManager.java,v 1.1 2003/10/23 17:17:13 unico Exp $
 */
public final class SourceDescriptorManager extends AbstractLogEnabled 
implements SourceDescriptor, ThreadSafe, Contextualizable, Composable, 
Configurable, Initializable, Disposable {
    
    public static final String ROLE = SourceDescriptorManager.class.getName();
    
    // the registered inspectors
    private Map m_inspectors;
    
    // cached list of all supported property types
    private String[] m_types;
    
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

        m_inspectors = new HashMap();
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
            ContainerUtil.parameterize(inspector,
                Parameters.fromConfiguration(children[i]));
            ContainerUtil.initialize(inspector);
            
            String[] types = inspector.getExposedSourcePropertyTypes();
            for (int j = 0; i < types.length; i++) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Registering " + inspector 
                        + " to handle property " + types[i]);
                }
                m_inspectors.put(types[i],inspector);
            }
        }
        // done with these
        m_configuration = null;
        m_context = null;
        m_manager = null;
    }
    
    public void dispose() {
        SourceInspector inspector;
        Iterator iter = m_inspectors.values().iterator();
        while(iter.hasNext()) {
            ContainerUtil.dispose(iter.next());
        }
        m_inspectors = null;
        m_types = null;
    }
    
    public SourceProperty getSourceProperty(Source source, String namespace, String name) 
            throws SourceException {

        String propname = namespace + "#" + name;
        SourceInspector inspector = (SourceInspector) m_inspectors.get(propname);
        if (inspector != null) {
            return inspector.getSourceProperty(source,namespace,name);
        } 
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("No inspector registered for property " + propname);
        }
        return null;
    }

    public SourceProperty[] getSourceProperties(Source source) throws SourceException {
        
        Set result = new HashSet(getExposedSourcePropertyTypes().length);
        
        SourceInspector inspector;
        SourceProperty[] properties;
        
        Iterator inspectors = m_inspectors.values().iterator();
        while(inspectors.hasNext()) {
            inspector = (SourceInspector) inspectors.next();
            properties = inspector.getSourceProperties(source);
            if (properties != null) {
                result.addAll(Arrays.asList(properties));
            }
        }

        return (SourceProperty[]) result.toArray(new SourceProperty[result.size()]);
    }
    
    public String[] getExposedSourcePropertyTypes() {
        if (m_types == null) {
            Set types = m_inspectors.keySet();
            m_types = (String[]) types.toArray(new String[types.size()]);
        }
        return m_types;
    }
    
    public void removeSourceProperty(Source source, String ns, String name) throws SourceException {
        String prop = ns + "#" + name;
        SourceInspector inspector = (SourceInspector) m_inspectors.get(prop);
        if (inspector == null) {
            throw new SourceException("No descriptor registered for property " + prop);
        }
        if (!(inspector instanceof SourceDescriptor)) {
            throw new SourceException("Cannot modify a read-only property.");
        }
        ((SourceDescriptor) inspector).removeSourceProperty(source,ns,name);
    }

    public void setSourceProperty(Source source, SourceProperty property) throws SourceException {
        
        String prop = property.getNamespace() + "#" + property.getName();
        SourceInspector inspector = (SourceInspector) m_inspectors.get(prop);
        if (inspector == null) {
            throw new SourceException("No descriptor registered for property " + prop);
        }
        if (!(inspector instanceof SourceDescriptor)) {
            throw new SourceException("Cannot modify a read-only property.");
        }
        ((SourceDescriptor) inspector).setSourceProperty(source,property);
    }
    
    

}

