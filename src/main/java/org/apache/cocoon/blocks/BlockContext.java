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
package org.apache.cocoon.blocks;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.apache.cocoon.environment.impl.AbstractContext;

/**
* @version $Id$
*/
public class BlockContext extends AbstractContext {

    private Hashtable attributes;
    private BlockWiring wiring;
    private BlockManager blockManager;
    
    public BlockContext(BlockWiring wiring, BlockManager blockManager) {
        this.wiring = wiring;
        this.blockManager = blockManager;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Context#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Context#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Context#getAttributes()
     */
    public Map getAttributes() {
        return this.attributes;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Context#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Context#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return this.attributes.keys();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Context#getResource(java.lang.String)
     */
    public URL getResource(String path) throws MalformedURLException {
        // A path starting with '/' should be resolved relative to the context and
        // the '/' need to be removed to work with the URI resolver.
        while (path.length() >= 1 && path.charAt(0) == '/') {
            path = path.substring(1);
        }
        String contextURL = this.wiring.getContextURL().toExternalForm();
        URL resolvedURL = null;
        try {
            resolvedURL = ((new URI(contextURL)).resolve(path)).toURL();
        } catch (URISyntaxException e) {
            throw new MalformedURLException("Couldn't resolve " + path + " relative " + contextURL +
                    " error " + e.getMessage());
        }
        return resolvedURL;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Context#getRealPath(java.lang.String)
     */
    public String getRealPath(String path) {
        // We better don't assume that blocks are unpacked
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Context#getMimeType(java.lang.String)
     */
    public String getMimeType(String file) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Context#getInitParameter(java.lang.String)
     */
    public String getInitParameter(String name) {
        return this.blockManager.getProperty(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Context#getResourceAsStream(java.lang.String)
     */
    public InputStream getResourceAsStream(String path) {
        try {
            return this.getResource(path).openStream();
        } catch (IOException e) {
            // FIXME Error handling
            e.printStackTrace();
            return null;
        }
    }

}
