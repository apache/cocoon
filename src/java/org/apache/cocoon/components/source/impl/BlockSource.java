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
import org.apache.cocoon.components.blocks.BlockManager;
import org.apache.cocoon.components.blocks.BlocksManager;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.internal.BlockEnvironmentHelper;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.environment.wrapper.EnvironmentWrapper;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.impl.AbstractSource;

/**
 * Implementation of a {@link Source} that gets its content by
 * invoking the BlockManager. 
 *
 * @version $Id$ */
public final class BlockSource
    extends AbstractSource {

    /** The current ServiceManager */
    private final ServiceManager manager;

    /** The environment */
    private final EnvironmentWrapper environment;

    /** The name of the called block */
    private String blockName;

    /**
     * Construct a new object
     */
    public BlockSource(ServiceManager manager,
                       String         uri,
                       Map            parameters,
                       Logger         logger)
        throws MalformedURLException {

        Environment env = EnvironmentHelper.getCurrentEnvironment();
        if ( env == null ) {
            throw new MalformedURLException("The block protocol can not be used outside an environment.");
        }
        this.manager = manager;

        SitemapSourceInfo info = null;
        try {
            info = parseBlockURI(env, uri);
        } catch (URISyntaxException e) {
            throw new MalformedURLException("Malformed URI in block source " +
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
        this.environment.setAttribute(BlockManager.NAME, this.blockName);
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     */
    public InputStream getInputStream()
        throws IOException, SourceException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        this.environment.setOutputStream(os);

        BlockManager block = BlockEnvironmentHelper.getCurrentBlock();
        if (block == null)
            throw new SourceNotFoundException("Must be used in a block context " + this.getURI());

        try {
            block.process(this.environment);
            
            return new ByteArrayInputStream(os.toByteArray());

        } catch (Exception e) {
            throw new SourceException("Exception during processing of " + this.getURI(), e);
        } finally {
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
    private SitemapSourceInfo parseBlockURI(Environment env, String blockURI) 
        throws URISyntaxException {

        SitemapSourceInfo info = new SitemapSourceInfo();
        // Maybe rawMode should be available for the block protocol.
        info.rawMode = false;

        URI uri = new URI(blockURI);

        // Can't happen
        if (!uri.isAbsolute()) {
            throw new URISyntaxException(blockURI,
                                         "Only absolute URIs are allowed for the block protocol.");
        }
        info.protocol = uri.getScheme();
        
        if (uri.isOpaque()) {
            // Sub protocol or relative self reference
            uri = new URI(uri.getSchemeSpecificPart());
            if (uri.isAbsolute()) {
                // Sub protocol refering to other block, e.g. block:foo:/bar
                if (uri.isOpaque()) {
                    throw new URISyntaxException(blockURI,
                                                 "The protocol must have the form block:foo:/bar");
                }
                this.blockName = uri.getScheme();
                info.uri = uri.getPath();
            } else {
                // self reference relative to the current sitemap, e.g. block:./foo
                this.blockName = null;
                URI base = new URI(null, null, env.getURIPrefix(), null);
                info.uri = base.resolve(uri).toString();
            }
        } else {
            // reference to the base sitemap of the own block, block:/foo
            this.blockName = null;
            info.uri = uri.getPath();
        }

        // All URIs, also relative are resolved and processed from the block manager
        info.processFromRoot = true;
        info.prefix = "";
        info.requestURI = info.uri;
        info.queryString = uri.getQuery();
        info.view = SitemapSourceInfo.getView(info.queryString, env);
        
        // FIXME: This will not be a system global id, as the blockName is block local.
        String ssp = (new URI(this.blockName, null, info.requestURI, info.queryString, null)).toString();
        info.systemId = (new URI(info.protocol, ssp, null)).toString();
        
        return info;
    }
}
