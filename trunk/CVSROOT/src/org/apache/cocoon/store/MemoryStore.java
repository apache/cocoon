package org.apache.cocoon.store;

import java.io.*;
import java.util.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements a memory-managed hashtable wrapper that uses
 * a weighted mix of LRU and LFU to keep track of object importance.
 *
 * NOTE: this class is _HIGHLY_ un-optimized and this class is _CRITICAL_
 * for a fast performance of the whole system. So, if you find any better
 * way to implement this class (clever data models, smart update algorithms,
 * etc...), please, consider patching this implementation or
 * sending a note about a method to do it.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:12 $
 */

public class MemoryStore extends AbstractActor implements Store, Configurable, Status {
    
    private Runtime jvm;
    private int memory;
    private Hashtable hashtable;
 
    class Container {
        public Object object;
        public long time = 0;
        public int count = 0;
        
        public Container(Object object) {
            this.object = object;
        }
    }

    public MemoryStore() {
        this.jvm = Runtime.getRuntime();
        this.hashtable = new Hashtable(101, 0.75f); // tune later on
    }

    public void init(Configurations confs) throws InitializationException {
        this.memory = Integer.parseInt((String) confs.getNotNull("memory"));

        if (memory <= 0) 
            throw new IllegalArgumentException("Free memory limit must be higher than zero.");
        if (memory > jvm.freeMemory())
            throw new IllegalArgumentException("Free memory is already lower than imposed limit. (free memory available:" + jvm.freeMemory() + ").  See documentation and FAQ on how to solve this problem.");
    }

    /**
     * Store the given object in a persistent state. It is up to the
     * caller to ensure that the key has a persistent state across
     * different JVM executions.
     */ 
    public void store(Object key, Object value) {
    	throw new RuntimeException("Method MemoryStore.store() not implemented!");
    }
    
    /**
     * Holds the given object in a volatile state. This means
     * the object store will discard held objects if the
     * virtual machine is restarted or some error happens.
     */ 
    public synchronized void hold(Object key, Object object) {
        
        while (jvm.freeMemory() < memory) {
            Object worst = this.getWorst();
            this.hashtable.remove(worst);
            jvm.runFinalization();
            jvm.gc();            
        }
        
        this.hashtable.put(key, new Container(object));
    }

    /**
     * Get the object associated to the given unique key.
     */
    public synchronized Object get(Object key) {
        Object o = this.hashtable.get(key);
        if (o != null) {
            Container c = (Container) o;
            c.time = System.currentTimeMillis();
            c.count++;
            return c.object;
        } else {
            return null;
        }
    }

    /**
     * Remove the object associated to the given key and returns
     * the object associated to the given key or null if not found.
     */
    public synchronized void remove(Object key) {
        this.hashtable.remove(key);
    }

    /**
     * Indicates if the given key is associated to a contained object.
     */
    public synchronized boolean containsKey(Object key) {
        return this.hashtable.containsKey(key);
    }

    /**
     * Returns the list of used keys.
     */
    public synchronized Enumeration list() {
        return this.hashtable.keys();
    }
    
    /**
     * This method returns the key to the worst object included.
     *
     * NOTE: this is a very un-optimized method. A better data
     * model using b-trees as side indexes could reduce the
     * work done at this point. This method is an hotspot since
     * it may be called more than once per each insert
     * method in the cache and its complexity is currently
     * linear with the cache size.
     */    
    private Object getWorst() {
        long time = System.currentTimeMillis();
        long minimum = Long.MAX_VALUE;
        Object minimumKey = null;
        
        Enumeration keys = this.hashtable.keys();
        Enumeration values = this.hashtable.elements();
        
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Container value = (Container) values.nextElement();
            long importance = this.evaluate(value, time);
            if (importance < minimum) {
                minimum = importance;
                minimumKey = key;
            }
        }
        
        return minimumKey;
    }
    
    /**
     * This method evaluates every contained object and is
     * a function of LRU (last recently used) and LFU (last frequenly used)
     * paradigms.
     *
     * NOTE: this evaluation system may be rendered pluggable if different
     * paradigms are found better for different situations. At this point, 
     * I'm not sure this counts that much.
     */
    private long evaluate(Container c, long time) {
        return (factor * c.count) + (Long.MAX_VALUE >> 1) - (time - c.time);
    }
    
    /** 
     * Indicates the factor of LFU compared to LRU. See code for more details.
     */
    private static final long factor = 2;
    
    /**
     * Returns the signature of this store implementation
     */
    public String getStatus() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<b>Memory Object Storage System</b><br>");
        Enumeration e = list();
        while (e.hasMoreElements()) {
            buffer.append("<li>");
            buffer.append(e.nextElement());
            buffer.append("</li>");
        }
        return buffer.toString();
    }    
}