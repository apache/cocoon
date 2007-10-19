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

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceUtil;
import org.apache.slide.common.NamespaceAccessToken;

import org.apache.cocoon.components.slide.SlideRepository;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * A factory for sources from a Jakarta Slide repository.
 *
 * @version $Id$ 
 */
public class SlideSourceFactory extends AbstractLogEnabled
                                implements SourceFactory, Contextualizable, Serviceable,
                                           ThreadSafe {

    private ServiceManager m_manager;
    private SlideRepository m_repository;
    private Context m_context;


    public SlideSourceFactory() {
    }
    
    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     *
     * @param context The context.
     */
    public void contextualize(Context context) throws ContextException {
        m_context = context;
    }
    
    /**
     * Lookup the SlideRepository.
     * 
     * @param manager ServiceManager.
     */
    public void service(ServiceManager manager) throws ServiceException {
        m_manager = manager;
    }

    
    /**
     * Get a <code>Source</code> object.
     *
     * @param location URI of the source.
     * @param parameters This is optional.
     *
     * @return A new source object.
     */
    public Source getSource(String location, Map parameters) throws IOException {

        if (m_repository == null) {
            try {
                m_repository = (SlideRepository) m_manager.lookup(SlideRepository.ROLE);
            } catch (ServiceException se) {
                throw new SourceException("Unable to lookup repository.", se);
            }
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Creating source object for " + location);
        }
        
        final String[] parts = SourceUtil.parseUrl(location);
        final String scheme = parts[SourceUtil.SCHEME];
        final String authority = parts[SourceUtil.AUTHORITY];
        final String query = parts[SourceUtil.QUERY];
        String path = parts[SourceUtil.PATH];
        
        String principal;
        String namespace;
        
        // parse the authority string for [usr][:pwd]@ns
        int index = authority.indexOf('@');
        if (index == -1) {
            principal = "guest";
            namespace = authority;
        }
        else {
            principal = authority.substring(0,index);
            namespace = authority.substring(index+1);
        }
        
        if (path == null || path.length() == 0) {
            path = "/";
        }
        
        NamespaceAccessToken nat = m_repository.getNamespaceToken(namespace);
        if (nat == null) {
            throw new SourceException("No such namespace: " + namespace);
        }

        SourceParameters queryParameters;
        if (query == null || query.length() == 0) {
            queryParameters = new SourceParameters();
        } else {
            queryParameters = new SourceParameters(query);
        }

        String version = queryParameters.getParameter("version",null);
        String scope   = queryParameters.getParameter("scope",
            nat.getNamespaceConfig().getFilesPath());
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("scheme: " + scheme);
            getLogger().debug("principal: " + principal);
            getLogger().debug("namespace: " + namespace);
            getLogger().debug("path: " + path);
            getLogger().debug("version: " + version);
            getLogger().debug("scope: " + scope);
        }

        SlideSource source = new SlideSource(nat, scheme, scope, path, principal, version);
        source.contextualize(m_context);
        source.service(m_manager);
        source.initialize();

        return source;
    }

    /**
     * Release a {@link Source} object.
     *
     * @param source Source, which should be released.
     */
    public void release(Source source) {
        if (null!=source) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Releasing source "+source.getURI());
            }
        }
    }

}

