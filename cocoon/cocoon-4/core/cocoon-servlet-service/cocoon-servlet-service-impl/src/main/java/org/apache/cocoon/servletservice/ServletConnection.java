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
package org.apache.cocoon.servletservice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.cocoon.callstack.environment.CallFrameHelper;
import org.apache.cocoon.servletservice.util.BlockCallHttpServletRequest;
import org.apache.cocoon.servletservice.util.BlockCallHttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of a {@link URLConnection} that gets its content by
 * invoking the Block. 
 * 
 * TODO Plenty of work left to have a meaningfull implementation of all methods
 *
 * @version $Id$
 */
public final class ServletConnection {

    /** Wrapped request */
    private BlockCallHttpServletRequest request;
    
    /** Wrapped response */
    private BlockCallHttpServletResponse response;

    /** The name of the called block */
    private String blockName;

    /** The current block context */
    private final ServletContext context;
    
    private String systemId;
    
    /** By default we use the logger for this class. */
    private Log logger = LogFactory.getLog(getClass());
    
    /** If already connected */
    private boolean connected; 
    
    private InputStream responseBody;
    private ByteArrayOutputStream requestBody;

    /**
     * Construct a new object
     */
    public ServletConnection(String url)
        throws MalformedURLException {

        URI blockURI = null;
        try {
            blockURI = parseBlockURI(new URI(url.toString()));
        } catch (URISyntaxException e) {
            throw new MalformedURLException("Malformed URI in block source " +
                                            e.getMessage());
        }

        // Super calls are resolved relative the current context and ordinary
        // calls relative the last non super call in the call chain
        if (ServletServiceContext.SUPER.equals(this.blockName))
            this.context = CallStackHelper.getCurrentServletContext();
        else
            this.context = CallStackHelper.getBaseServletContext();
        
        if (this.context == null)
            throw new MalformedURLException("Must be used in a block context " + url);

        this.request = new BlockCallHttpServletRequest(blockURI, CallFrameHelper.getRequest());
        this.response = new BlockCallHttpServletResponse();
        
        this.connected = false;
    }
    
    public void connect() throws IOException, ServletException {
    	//if already connected, do nothing
    	if (connected) return;
    	
    	if (requestBody != null) {
    		request.setInputStream(new ByteArrayInputStream(requestBody.toByteArray()));
    		request.setMethod("POST");
    	}
    	
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        this.response.setOutputStream(os);
        RequestDispatcher dispatcher = null;
        
        try {
            if (this.blockName == null) {
                // FIXME Should be called with path + queryString,
                // but the argument is ignored so it doesn't matter
                dispatcher = this.context.getRequestDispatcher(this.systemId);
            } else {
                dispatcher = this.context.getNamedDispatcher(this.blockName);
            }
            if (dispatcher == null)
                throw new ServletException("No dispatcher for " + this.systemId);
            dispatcher.forward(this.request, this.response);
            this.response.flushBuffer();
            
            byte[] out = os.toByteArray();
            
            responseBody = new ByteArrayInputStream(out);
        } finally {
            os.close();
        }
        connected = true;
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     * @throws Exception 
     */
    public InputStream getInputStream() throws IOException, ServletException {
    	connect();
    	return responseBody;
    }

    public void setIfModifiedSince(long ifmodifiedsince) {
    	if (connected)
    	    throw new IllegalStateException("Already connected");
    	request.setDateHeader("If-Modified-Since", ifmodifiedsince);
    }
    
    public long getLastModified() {
    	return getHeaderFieldDate("Last-Modified", 0);
    }
    
    public String getContentType() {
    	return getHeaderField("Content-Type");
    }

    public long getHeaderFieldDate(String name, long Default) {
    	try {
    	    return response.getDateHeader(name);
    	} catch (Exception e) { }
    	return Default;
    }
    
    public String getHeaderField(String name) {
    	try {
			connect();
		} catch (Exception e) {
			return null;
		}
    	return response.getHeader(name);
    }
    
    public int getResponseCode() throws IOException {
    	if (!connected)
			try {
				connect();
			} catch (ServletException e) {
				throw new IOException("Could not get response status code");
			}
    	return response.getStatus();
    }
    
    /**
     * Returns an output stream that writes as POST to this connection.
     * @return an output stream that writes as POST to this connection.
     * @throws IllegalStateException - if already connected
     */
    public OutputStream getOutputStream() throws IllegalStateException {
    	if (connected) throw new IllegalStateException("You cannot write to the connection already connected.");
    	if (requestBody == null) requestBody = new ByteArrayOutputStream();
    	return requestBody;
    	
    }
    
    // Parse the block protocol.
    private URI parseBlockURI(URI uri) throws URISyntaxException {
        // Can't happen
        if (!uri.isAbsolute()) {
            throw new URISyntaxException(uri.toString(),
                                         "Only absolute URIs are allowed for the block protocol.");
        }
        String scheme = uri.getScheme();

        this.logger.debug("ServletSource: resolving " + uri.toString() + " with scheme " +
                uri.getScheme() + " and ssp " + uri.getRawSchemeSpecificPart());
        uri = new URI(uri.getRawSchemeSpecificPart());
        this.logger.debug("ServletSource: resolved to " + uri.toString());
        
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
