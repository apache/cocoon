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
package org.apache.cocoon.servletservice.components;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.URIAbsolutizer;
import org.apache.excalibur.store.Store;

/**
 * <p>This class implements the <code>servlet:</code> protocol. A servlet URI can
 * be relative (actually means absolute in the context of the calling servlet service) or absolute.</p>
 *
 * <p>An absolute URI has following syntax:</p>
 * <p><code>servlet:[servlet-service-name]+:[path]</code>. Here is an example:<br/>
 * <code>servlet:com.mycompany.mySkin.servlet:/abc</code>.</p>
 *
 * <p>A relative URI has following syntax:</p>
 * <p><code>servlet:[connection-name]:/[path]</code>. Here is an example:<br/>
 * <code>servlet:mySkin:/abc</code>.
 *
 * @version $Id: ServletSourceFactory.java 577520 2007-09-20 03:06:12Z
 *          vgritsenko $
 * @since 1.0.0
 */
public final class ServletSourceFactory implements SourceFactory, URIAbsolutizer {

    /** By default we use the logger for this class. */
    private Log logger = LogFactory.getLog(getClass());

    /**
     * Store that will be used by {@link ServletSource}.
     */
    private Store store;

    private Log getLogger() {
        return this.logger;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Source getSource(String location, Map parameters) throws MalformedURLException, IOException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Creating source object for " + location);
        }

        return new ServletSource(location, store);
    }

    public void release(Source source) {
        if (source != null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Releasing source " + source.getURI());
            }
        }
    }

    public String absolutize(String baseURI, String location) {
        return SourceUtil.absolutize(baseURI, location, true);
    }

}
