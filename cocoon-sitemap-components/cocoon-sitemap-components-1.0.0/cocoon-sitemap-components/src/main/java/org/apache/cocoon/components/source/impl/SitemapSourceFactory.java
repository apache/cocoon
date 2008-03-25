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
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.URIAbsolutizer;

import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * This class implements the cocoon: protocol.
 * It cannot be used like other source factories
 * as it needs the current <code>Sitemap</code> as input.
 *
 * @version $Id$
 */
public final class SitemapSourceFactory extends AbstractLogEnabled
                                        implements SourceFactory, ThreadSafe, Serviceable,
                                                   URIAbsolutizer {
    
    /** The <code>ServiceManager</code> */
    private ServiceManager manager;

    /**
     * @see Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * @see SourceFactory#getSource(java.lang.String, java.util.Map)
     */
    public Source getSource(String location, Map parameters) throws IOException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Creating source object for " + location);
        }

        return new SitemapSource(this.manager, location, parameters);
    }

    /**
     * @see SourceFactory#release(org.apache.excalibur.source.Source)
     */
    public void release(Source source) {
        if (source != null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Releasing source " + source.getURI());
            }
            ((SitemapSource) source).recycle();
        }
    }

    /**
     * @see URIAbsolutizer#absolutize(java.lang.String, java.lang.String)
     */
    public String absolutize(String baseURI, String location) {
        return SourceUtil.absolutize(baseURI, location, true);
    }
}
