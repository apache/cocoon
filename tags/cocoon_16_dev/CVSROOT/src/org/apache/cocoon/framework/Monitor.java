package org.apache.cocoon.framework;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class watches over the changes of indicated resources.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:18 $
 */

public class Monitor {
    
    private Hashtable table;
    
    class Container {
        public Object resource;
        public long timestamp;
        
        public Container(Object resource, long timestamp) {
            this.resource = resource;
            this.timestamp = timestamp;
        }
    }
    
    public Monitor(int capacity) {
        this.table = new Hashtable(capacity);
    }
    
    /**
     * Tells the monitor to watch the given resource, timestamps it
     * and associate it to the given key.
     */
    public void watch(Object key, Object resource) {
        this.table.put(key, new Container(resource, timestamp(resource)));
    }
    
    /**
     * Queries the monitor for changes. For maximum reliability, this
     * method is synchronous, but less reliable for faster asynchronous
     * versions could be implemented. Returns true if the resource
     * associated to the given key has changed since the last call
     * to watch.
     *
     * WARNING: due to a stupid bug in "FileURLConnection", the
     * class that implements the "file:" protocol for the java.net.URL
     * framework, the getLastModified() method always returns 0.
     * For this reason, the use of the File resource is strongly
     * suggested over the "file:" type URL.
     * NOTE: this may not be (and should not be) the case in other
     * virtual machine implementations or if we rewrite the URL
     * handler ourselves (which I don't care to do at this point).
     */
    public boolean hasChanged(Object key) {
        Object o = this.table.get(key);
        if (o != null) {
            Container c = (Container) o;
            return c.timestamp != timestamp(c.resource);
        } else {
            return true;
        }
    }
    
    /**
     * Create a timestamp indicating the last modified time
     * of the given resource.
     */
    private long timestamp(Object resource) {
        long timestamp;
        if (resource instanceof File) {
            timestamp = ((File) resource).lastModified();
        } else if (resource instanceof URL) {
            try {
                timestamp = ((URL) resource).openConnection().getLastModified();
            } catch (IOException e) {
                timestamp = 0;
            }
        } else {
            throw new IllegalArgumentException("Resource not monitorizable.");
        }
        return timestamp;
    }
}