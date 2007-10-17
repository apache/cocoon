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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;

import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * @version $Id$
 */
public class XMLizableSourceFactory extends AbstractLogEnabled
                                    implements SourceFactory, Serviceable, ThreadSafe {
    
    private ServiceManager m_manager;
    private SourceResolver m_resolver;
    
    private volatile boolean m_initialized;

    
    public void service(ServiceManager manager) throws ServiceException {
        m_manager = manager;
    }
    
    private synchronized void lazyInitialize() throws SourceException {
        if (!m_initialized) {
            try {
                m_resolver = (SourceResolver) m_manager.lookup(SourceResolver.ROLE);
            } catch (ServiceException e) {
                throw new SourceException("Missing service dependency: SourceResolver",e);
            }
            m_initialized = true;
        }
    }
    
    public Source getSource(String location, Map parameters)
    throws IOException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Creating source object for " + location);
        }
        if (!m_initialized) {
            lazyInitialize();
        }

        final String uri = location.substring(XMLizableSource.SCHEME.length() + 1);
        final Source delegate = m_resolver.resolveURI(uri, null, parameters);

        return new XMLizableSource(delegate, m_manager);
    }

    public void release(Source source) {
        if (source instanceof XMLizableSource) {
            m_resolver.release(((XMLizableSource) source).getSource());
        }
    }
}
