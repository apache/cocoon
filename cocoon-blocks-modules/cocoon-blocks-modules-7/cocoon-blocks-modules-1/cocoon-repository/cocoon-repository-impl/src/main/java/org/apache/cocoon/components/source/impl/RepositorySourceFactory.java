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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.source.SourceDescriptor;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;

/**
 * Creates RepositorySources.
 */
public class RepositorySourceFactory extends AbstractLogEnabled
implements SourceFactory, Serviceable, Configurable, ThreadSafe {
    
    private ServiceManager m_manager;
    private SourceResolver m_resolver;
    private SourceDescriptor m_descriptor;
    private String m_name;
    private boolean m_isInitialized;
    
    public RepositorySourceFactory() {
    }
    
    private synchronized void lazyInitialize() throws IOException {
        if (m_isInitialized) {
            return;
        }
        if (m_resolver == null) {
            try {
                m_resolver = (SourceResolver) m_manager.lookup(SourceResolver.ROLE);
            }
            catch (ServiceException e) {
                throw new IOException("Resolver service is not available: " + e.toString());
            }
        }
        if (m_manager.hasService(SourceDescriptor.ROLE)) {
            try {
                m_descriptor = (SourceDescriptor) m_manager.lookup(SourceDescriptor.ROLE);
            }
            catch (ServiceException e) {
                // impossible
            }
        }
        else {
            m_descriptor = null;
            if (getLogger().isInfoEnabled()) {
                final String message =
                    "SourceDescriptor is not available. " +
                    "RepositorySource will not support " +
                    "source properties.";
                getLogger().info(message);
            }
        }
    }
    
    /**
     * Read the <code>name</code> attribute.
     */
    public void configure(final Configuration configuration) throws ConfigurationException {
        m_name = configuration.getAttribute("name");
    }
    
    /**
     * Lookup the SourceDescriptorManager service.
     */
    public void service(final ServiceManager manager) {
        m_manager = manager;
    }
    
    public Source getSource(String location, Map parameters)
        throws IOException, MalformedURLException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Creating RepositorySource for " + location);
        }
        
        // lazy initialization due to circular dependency
        if (!m_isInitialized) {
            lazyInitialize();
        }
        
        // assert location.startsWith(m_name)
        location = location.substring(m_name.length()+1);
        Source source = m_resolver.resolveURI(location);
        if (!(source instanceof ModifiableTraversableSource)) {
            final String message = "Delegate should be a ModifiableTraversableSource";
            throw new SourceException(message);
        }
        
        return new RepositorySource(
            m_name,
            (ModifiableTraversableSource) source, 
            m_descriptor,
            getLogger()
        );
    }
    
    public void release(final Source source) {
        if (source instanceof RepositorySource) {
            m_resolver.release(((RepositorySource) source).m_delegate);
        }
    }

}
