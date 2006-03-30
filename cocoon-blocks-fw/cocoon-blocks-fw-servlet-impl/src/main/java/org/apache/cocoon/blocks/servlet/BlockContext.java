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
package org.apache.cocoon.blocks.servlet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;

import javax.servlet.ServletContext;


/**
 * @version $Id$
 */
public class BlockContext extends org.apache.cocoon.blocks.BlockContext {

    private URL contextURL;
    private Dictionary connections;

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getResource(java.lang.String)
     */
    public URL getResource(String path) throws MalformedURLException {
        if (path.length() == 0 || path.charAt(0) != '/')
            throw new MalformedURLException("The path must start with '/' " + path);
        path = path.substring(1);
        return new URL(this.contextURL, path);
    }
    
    /**
     * Get the context of a block with a given name.
     */
    public ServletContext getNamedContext(String name) {
        ServletContext context = null;
        String blockId = (String) this.connections.get(name);
        if (blockId != null)
            context = ((BlocksContext)super.servletContext).getNamedContext(blockId);

        return context;
    }
    
    /**
     * @param contextURL The contextURL to set.
     */
    public void setContextURL(URL contextURL) {
        this.contextURL = contextURL;
    }

    /**
     * @param connections The connections to set.
     */
    public void setConnections(Dictionary connections) {
        this.connections = connections;
    }

}
