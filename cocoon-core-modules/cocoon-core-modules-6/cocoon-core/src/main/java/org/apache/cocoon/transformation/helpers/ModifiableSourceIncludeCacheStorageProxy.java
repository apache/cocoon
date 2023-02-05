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
package org.apache.cocoon.transformation.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.util.HashUtil;

/**
 * This is the interface between the {@link IncludeCacheManager} and a
 * {@link Source} object that stores the cached content in a directory
 * manner.
 * 
 * @since   2.1
 * @version $Id$
 */
public final class ModifiableSourceIncludeCacheStorageProxy extends AbstractLogEnabled
                                                            implements IncludeCacheStorageProxy {

    private SourceResolver resolver;
    private String         parentURI;

    /**
     * Constructor.
     *
     * @param resolver   For source resolving
     * @param parentURI  The "directory"
     */
    public ModifiableSourceIncludeCacheStorageProxy(SourceResolver resolver,
                                             String         parentURI) {
        this.resolver = resolver;
        this.parentURI= parentURI;
    }
    
    /**
     * Calculate the URI for a child
     * @param uri     Child URI
     * @return String Absolute URI
     */
    private String getURI(String uri) {
        final long hash = HashUtil.hash(uri);
        final StringBuffer buffer = new StringBuffer(this.parentURI);
        buffer.append('/');
        if (hash < 0) {
            buffer.append('M').append(hash * -1);
        } else {
            buffer.append(hash);
        }
        buffer.append(".cxml");
        return buffer.toString();
    }
    
    /**
     * @see IncludeCacheStorageProxy#get(java.lang.String)
     */
    public Serializable get(String uri) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WSCProxy: Getting content for " + uri);
        }

        Source child = null;
        Serializable result = null;
        try {
            child = this.resolver.resolveURI(this.getURI(uri));
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("WSCProxy: Resolved to " + child.getURI());
            }

            if (child.exists()) {
                InputStream is = child.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                result = (Serializable)ois.readObject();
                ois.close();
            }
        } catch (Exception ignore) {
        } finally {
            this.resolver.release( child );
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WSCProxy: Result for " + uri + " : " + (result == null ? "Not in cache" : "Found"));
        }
        return result;
    }

    /**
     * @see IncludeCacheStorageProxy#put(java.lang.String, java.io.Serializable)
     */
    public void put(String uri, Serializable object) 
    throws IOException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WSCProxy: Storing content for " + uri);
        }

        Source child = null;
        try {
            child = this.resolver.resolveURI(this.getURI(uri));
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("WSCProxy: Resolved to " + child.getURI());
            }

            OutputStream os;
            if (child instanceof ModifiableSource) {
                os = ((ModifiableSource)child).getOutputStream();
            } else {
                throw new IOException("Source " + uri + " is not writeable.");
            }
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(object);
            oos.flush();
            oos.close();
        } catch (IOException io) {
            throw io;
        } catch (Exception ignore) {
            throw new CascadingIOException("Exception.", ignore);
        } finally {
            this.resolver.release( child );
        }
    }

    /**
     * @see IncludeCacheStorageProxy#remove(java.lang.String)
     */
    public void remove(String uri) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WSCProxy: Removing content for " + uri);
        }

        Source child = null;
        try {
            child = this.resolver.resolveURI(this.getURI(uri));
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("WSCProxy: Resolved to " + child.getURI());
            }

            if (child instanceof ModifiableSource) {
                ((ModifiableSource) child).delete();
            } else {
                throw new IOException("Source " + uri + " is not writeable.");
            }
        } catch (Exception ignore) {
        } finally {
            this.resolver.release( child );
        }
    }

    /**
     * Compare
     */
    public boolean equals(Object object) {
        if (object instanceof ModifiableSourceIncludeCacheStorageProxy) {
            return this.parentURI.equals(((ModifiableSourceIncludeCacheStorageProxy) object).parentURI);
        }
        return false;
    }

    /**
     * Generate a hash code
     */
    public int hashCode() {
        return this.parentURI.hashCode();
    }
}
