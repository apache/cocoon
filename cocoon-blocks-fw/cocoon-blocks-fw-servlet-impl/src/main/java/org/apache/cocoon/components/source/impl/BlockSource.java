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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.blocks.BlockCallStack;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.impl.AbstractSource;

/**
 * Implementation of a {@link Source} that gets its content by
 * invoking the Block. 
 *
 * @version $Id$
 */
public final class BlockSource
    extends AbstractSource {

    /** Wrapped request */
    private BlockHttpServletRequestWrapper wrappedRequest;
    
    /** Wrapped response */
    private BlockHttpServletResponseWrapper wrappedResponse;

    /** The name of the called block */
    private String blockName;

    /** The current block context */
    private final ServletContext context;
    
    private String systemId;
    
    private Logger logger;

    /**
     * Construct a new object
     */
    public BlockSource(ServiceManager manager,
                       String         uri,
                       Map            parameters,
                       Logger         logger)
        throws MalformedURLException {

        this.logger = logger;
        
        Environment env = EnvironmentHelper.getCurrentEnvironment();
        if (env == null) {
            throw new MalformedURLException("The block protocol can not be used outside an environment.");
        }
        this.context = BlockCallStack.getCurrentBlockContext();
        if (this.context == null)
            throw new MalformedURLException("Must be used in a block context " + this.getURI());

        URI blockURI = null;
        try {
            blockURI = parseBlockURI(env, uri);
        } catch (URISyntaxException e) {
            throw new MalformedURLException("Malformed URI in block source " +
                                            e.getMessage());
        }
        setScheme(blockURI.getScheme());
        setSystemId(this.systemId);

        // wrap the request
        HttpServletRequest originalRequest =
            (HttpServletRequest) env.getObjectModel().get(HttpEnvironment.HTTP_REQUEST_OBJECT);
        if (originalRequest == null)
            throw new MalformedURLException("Blocks only work in an HttpEnvironment");
        
        this.wrappedRequest = new BlockHttpServletRequestWrapper(originalRequest, blockURI);

        // wrap the response
        HttpServletResponse originalResponse =
            (HttpServletResponse) env.getObjectModel().get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
        if (originalResponse == null)
            throw new MalformedURLException("Blocks only work in an HttpEnvironment");
        
        this.wrappedResponse = new BlockHttpServletResponseWrapper(originalResponse);
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     */
    public InputStream getInputStream()
        throws IOException, SourceException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        this.wrappedResponse.setOutputStream(os);
        
        try {
            if (this.blockName == null) {
                // FIXME Should be called with path + queryString,
                // but the argument is ignored so it doesn't matter
                RequestDispatcher dispatcher =
                    this.context.getRequestDispatcher(this.systemId);
                if (dispatcher == null)
                    throw new ServletException("No dispatcher for " + this.systemId);
                dispatcher.forward(this.wrappedRequest, this.wrappedResponse);
            } else {
                RequestDispatcher dispatcher =
                    this.context.getNamedDispatcher(this.blockName);
                if (dispatcher == null)
                    throw new ServletException("No dispatcher for " + this.blockName);
                dispatcher.forward(this.wrappedRequest, this.wrappedResponse);
            }
            this.wrappedResponse.flushBuffer();
            
            return new ByteArrayInputStream(os.toByteArray());

        } catch (Exception e) {
            throw new SourceException("Exception during processing of " + this.getURI(), e);
        }
    }

    /**
     * Returns true always.
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        return true;
    }
    
    protected final Logger getLogger() {
        return this.logger;
    }

    /**
     * Recyclable
     */
    public void recycle() {
    }

    /**
     * Parses and resolves the scheme specific part of a block URI
     * with respect to the base URI of the current sitemap. The scheme
     * specific part of the block URI has the form
     * <code>foo:/bar</code> when refering to another block, in this
     * case only an absolute path is allowed. For reference to the own
     * block, both absolute <code>/bar</code> and relative
     * <code>./foo</code> paths are allowed.
     */
    public static URI resolveURI(URI uri, URI base) throws URISyntaxException {
        if (uri.getPath() != null && uri.getPath().length() >= 2 &&
            uri.getPath().startsWith("./")) {
            // self reference relative to the current sitemap, e.g. ./foo
            if (uri.isAbsolute())
                throw new URISyntaxException(uri.toString(), "When the protocol refers to other blocks the path must be absolute");
            URI resolvedURI = base.resolve(uri);
            uri = resolvedURI;
        }
        return uri;
    }
    
    // Parse the block protocol.
    private URI parseBlockURI(Environment env, String blockURI) 
        throws URISyntaxException {

        URI uri = new URI(blockURI);

        // Can't happen
        if (!uri.isAbsolute()) {
            throw new URISyntaxException(blockURI,
                                         "Only absolute URIs are allowed for the block protocol.");
        }
        String scheme = uri.getScheme();

        String baseURI = env.getURIPrefix();
        if (baseURI.length() == 0 || !baseURI.startsWith("/"))
            baseURI = "/" + baseURI;
        
        this.getLogger().debug("BlockSource: resolving " + uri.toString() + " with scheme " +
                        uri.getScheme() + " and ssp " + uri.getSchemeSpecificPart());
        uri = BlockSource.resolveURI(new URI(uri.getSchemeSpecificPart()),
                        new URI(null, null, baseURI, null));
        this.getLogger().debug("BlockSource: resolved to " + uri.toString() +
                        " with base URI " + baseURI.toString());
        
        this.blockName = uri.getScheme();
        String path = uri.getPath();
        // All URIs, also relative are resolved and processed from the block manager
        String queryString = uri.getQuery();
        
        // FIXME: This will not be a system global id, as the blockName is block local.
        String ssp = (new URI(this.blockName, null, path, queryString, null)).toString();
        this.systemId = (new URI(scheme, ssp, null)).toString();
        
        return new URI(scheme, null, path, queryString, null);
    }
}
