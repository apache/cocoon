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
package org.apache.cocoon.caching;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import EDU.oswego.cs.dl.util.concurrent.FIFOReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.Sync;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.impl.CacheImpl;
import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.util.NetUtils;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.FileTimeStampValidity;

/**
 * This cache implementation is an extension to the default cache implementation.
 * If a response is not found in the cache or is invalid, it also checks the
 * file system for a cached response. This allows to update the cache information
 * by running batch processes.
 *
 * @since 2.1.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SimpleCache.java,v 1.3 2004/04/15 08:04:39 cziegeler Exp $
 */
public class SimpleCache extends CacheImpl {

    /** The base directory */
    protected String baseDirectory;
    
    protected Map locks = new HashMap(50);
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        super.parameterize(parameters);
        this.baseDirectory = parameters.getParameter("baseDirectory");
        final File dir = new File(this.baseDirectory);
        dir.mkdirs();
    }

    /**
     * Get the filename
     */
    protected File getFile(Serializable key) {
        String filename;
        if ( key instanceof String ) {
            filename = NetUtils.absolutize(this.baseDirectory, (String)key);    
        } else if ( key instanceof SimpleCacheKey ) {
            filename = NetUtils.absolutize(this.baseDirectory, ((SimpleCacheKey)key).getKey());    
        } else {
            filename = NetUtils.absolutize(this.baseDirectory, key.toString());
        }
        return new File(filename);
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.caching.Cache#clear()
     */
    public void clear() {
        super.clear();
        File dir = new File(this.baseDirectory);
        dir.delete();
        // create new file object
        dir = new File(this.baseDirectory);
        dir.mkdir();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.caching.Cache#containsKey(java.io.Serializable)
     */
    public boolean containsKey(Serializable key) {
        // is the response cached in memory?
        boolean result = super.containsKey(key);
        if (!result) {
            // check for files
            File file = this.getFile(key);
            result = file.exists();
            if ( !result ) {
                file = new File(file.getAbsolutePath()+".cxml");
                result = file.exists();
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.caching.Cache#get(java.io.Serializable)
     */
    public CachedResponse get(Serializable key) {
        CachedResponse response = super.get(key);
        if ( response == null || response.getValidityObjects()[0].isValid() != SourceValidity.VALID ) {
            if ( response != null ) {
                // we are invalid!
                this.remove(key);
            }
            File file = this.getFile(key);
            byte[] content = null;
            byte[] altContent = null;
            if ( file.exists() ) {
                content = this.get(file);
            }
            file = new File(file.getAbsolutePath()+".cxml");
            if ( file.exists() ) {
                altContent = this.get(file);
            }
            if ( content != null || altContent != null ) {
                SourceValidity val = new FileTimeStampValidity(file);
                response = new ExtendedCachedResponse(val, content);
                ((ExtendedCachedResponse)response).setAlternativeResponse(altContent);
            }
        }
        return response;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.caching.Cache#remove(java.io.Serializable)
     */
    public void remove(Serializable key) {
        super.remove(key);
        File file = this.getFile(key);
        if ( file.exists() ) {
            file.delete();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.caching.Cache#store(java.io.Serializable, org.apache.cocoon.caching.CachedResponse)
     */
    public void store(Serializable key, CachedResponse response)
    throws ProcessingException {
        // store in memory
        super.store(key, response);
        byte[] content = response.getResponse();
        if ( content != null ) {
            this.store(this.getFile(key), content);
        }
        if ( response instanceof ExtendedCachedResponse ) {
            content = ((ExtendedCachedResponse)response).getAlternativeResponse();
            if ( content != null ) {
                File file = this.getFile(key);
                file = new File(file.getAbsolutePath()+".cxml");
                this.store(file, content);
            }
        }
    }

    /**
     * store the content in a file
     */
    protected void store(File file, byte[] content) {
        ReadWriteLock lock;
        synchronized (this.locks) {
            lock = (ReadWriteLock) this.locks.get(file.getAbsolutePath());
            if ( lock == null ) {
                lock = new FIFOReadWriteLock();
                this.locks.put(file.getAbsolutePath(), lock);
            }
        }
        Sync sync = lock.writeLock();
        try {
            sync.acquire();
            try {
                OutputStream os = new FileOutputStream(file);
                os.write(content);
                os.flush();
                os.close();
            } catch (IOException io) {
                this.getLogger().warn("Exception during caching of content to " + file, io);
            } finally {
                sync.release();
            }
        } catch (InterruptedException ie) {
        }
    }
    
    /**
     * Get the content from a file
     */
    protected byte[] get(File file) {
        ReadWriteLock lock;
        synchronized (this.locks) {
            lock = (ReadWriteLock) this.locks.get(file.getAbsolutePath());
            if ( lock == null ) {
                lock = new FIFOReadWriteLock();
                this.locks.put(file.getAbsolutePath(), lock);
            }
        }
        Sync sync = lock.readLock();
        try {
            sync.acquire();
            try {
                String content = IOUtils.deserializeString(file);
                return content.getBytes();
            } catch (IOException io) {
                this.getLogger().warn("Exception during reading of content from " + file, io);
                return null;
            } finally {
                sync.release();
            }
        } catch (InterruptedException ie) {
            return null;
        }
    }
}
