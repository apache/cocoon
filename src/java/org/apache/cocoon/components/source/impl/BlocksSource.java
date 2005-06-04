/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.blocks.BlocksManager;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.environment.wrapper.EnvironmentWrapper;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.impl.AbstractSource;

/**
 * Implementation of a {@link Source} that gets its content by
 * invoking the BlocksManager. 
 *
 * WARNING: It is created for being able to use the blocks
 * functionality without needing to change the Cocoon object and other
 * fundamental classes. This class will probably be removed later.
 *
 * @version $Id$ */
public final class BlocksSource
    extends AbstractSource {

    /** The current ServiceManager */
    private final ServiceManager manager;

    /** The environment */
    private final EnvironmentWrapper environment;

    /**
     * Construct a new object
     */
    public BlocksSource(ServiceManager manager,
                        String         uri,
                        Map            parameters,
                        Logger         logger)
        throws MalformedURLException {

        Environment env = EnvironmentHelper.getCurrentEnvironment();
        if ( env == null ) {
            throw new MalformedURLException("The blocks protocol can not be used outside an environment.");
        }
        this.manager = manager;

        SitemapSourceInfo info = null;
        try {
            info = parseBlocksURI(env, uri);
        } catch (URISyntaxException e) {
            throw new MalformedURLException("Malformed URI in blocks source " +
                                            e.getMessage());
        }
        setScheme(info.protocol);
        setSystemId(info.systemId);

        // create environment...
        this.environment = new EnvironmentWrapper(env, info, logger);

        // ...and put information passed from the parent request to the internal request
        if ( null != parameters ) {
            this.environment.getObjectModel().put(ObjectModelHelper.PARENT_CONTEXT, parameters);
        } else {
            this.environment.getObjectModel().remove(ObjectModelHelper.PARENT_CONTEXT);
        }

        this.environment.setURI(info.prefix, info.uri);
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     */
    public InputStream getInputStream()
        throws IOException, SourceException {

        BlocksManager blocks = null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            this.environment.setOutputStream(os);

            blocks = (BlocksManager)this.manager.lookup(BlocksManager.ROLE);
            blocks.process(this.environment);
            
            return new ByteArrayInputStream(os.toByteArray());

        } catch (ResourceNotFoundException e) {
            throw new SourceNotFoundException("Exception during processing of " + this.getURI(), e);
        } catch (Exception e) {
            throw new SourceException("Exception during processing of " + this.getURI(), e);
        } finally {
            this.manager.release(blocks);
            // Unhide wrapped environment output stream
            this.environment.setOutputStream(null);
        }
    }

    /**
     * Returns true always.
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        return true;
    }

    /**
     * Recyclable
     */
    public void recycle() {
    }

    // Parse the blocks protocol.
    private static SitemapSourceInfo parseBlocksURI(Environment env, String sitemapURI) 
        throws URISyntaxException {
        URI uri = new URI(sitemapURI);

        if (!uri.isAbsolute() && uri.isOpaque() && uri.getAuthority() == null) {
            throw new URISyntaxException(sitemapURI,
                                         "The blocks protocol must be an absolute, hierachial URI whithout authority part");
        }

        SitemapSourceInfo info = new SitemapSourceInfo();
        // the blocks protocol is for redirecting the call to a
        // mounted block, so using the request parameters from the
        // original request is reasonable.
        info.rawMode = false;
        info.protocol = uri.getScheme();
        // The blocks protocol is always from the root context
        info.prefix = "";
        info.uri = uri.getPath();
        info.requestURI = info.prefix + info.uri;
        info.processFromRoot = true;
        info.queryString = uri.getQuery();
        info.view = SitemapSourceInfo.getView(info.queryString, env);
        info.systemId = uri.toString();

        return info;
    }
}
