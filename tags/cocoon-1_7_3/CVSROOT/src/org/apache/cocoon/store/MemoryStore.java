/*-- $Id: MemoryStore.java,v 1.11 2000-04-08 10:17:36 stefano Exp $ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

 */

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
 * @author <a href="mailto:michel.lehon@outwares.com">Michel Lehon</a>
 * @version $Revision: 1.11 $ $Date: 2000-04-08 10:17:36 $
 */

public class MemoryStore implements Store, Status, Configurable, Runnable {
    /**
     * Indicates how much memory should be left free in the JVM for
     * normal operation.
     */
    private int freememory;

    /**
     * Indicates how big the heap size can grow to before the cleanup thread kicks in.
     * The default value is based on the default maximum heap size of 64Mb.
     */
    private int heapsize;

    /**
     * Indicates the time in millis to sleep between memory checks.
     */ 
    private long interval;
 
    /**
     * Indicates whether we use a cleanup thread or not.
     */
    private boolean useThread;

    /**
     * Indicates the daemon thread priority.
     */
    private int priority;

    private Runtime jvm;
    private Hashtable hashtable;

    class Container {
        public Object object;
        public long time = 0;
        public int count = 0;

        public Container(Object object) {
            this.object = object;
        }
    }

    /**
     * Initialize the MemoryStore. 
     * A few options can be used :
     * <UL>
     *  <LI>freememory = How much memory to keep free for normal jvm operation. (Default: 1 Mb)</LI>
     *  <LI>heapsize = The size of the heap before cleanup starts. (Default: 60 Mb)</LI>
     *  <LI>usethread = use a cleanup daemon thread. (Default: true)</LI>
     *  <LI>threadpriority = priority to run cleanup thread (1-10). (Default: 10)</LI>
     *  <LI>interval = time in millis to sleep between memory checks (Default: 100 millis)</LI> 
     * </UL>
     */
    public void init(Configurations conf) throws InitializationException {
        this.jvm = Runtime.getRuntime();
        this.hashtable = new Hashtable(101, 0.75f); // tune later on

        this.priority = Thread.MAX_PRIORITY;

        try {
            this.freememory = Integer.parseInt((String)conf.get("freememory","1000000"));
            this.heapsize   = Integer.parseInt((String)conf.get("heapsize","60000000"));
            this.interval   = Integer.parseInt((String)conf.get("interval","100"));
            String pri = (String)conf.get("threadpriority");
            if (pri != null) { 
                this.priority = Integer.parseInt(pri);
                if ((this.priority < 1) || (this.priority > 10)) {
                    throw new InitializationException("Thread priority must be between 1 and 10");
                }
            }
        } catch (NumberFormatException e) {
            throw new InitializationException("freememory, heapsize, interval and threadpriority must be valid whole numbers");
        }

        this.useThread = conf.get("usethread","true").equals("true");

        if (useThread) {
            Thread checker = new Thread(this);
            checker.setPriority(this.priority);
            checker.setDaemon(true);
            checker.start();
        }
    }

    /** 
     * Background memory check. 
     * Checks that memory is not running too low in the JVM because of the Store.
     * It will try to keep overall memory usage below the requested levels.
     */
    public void run() {
        while (true) {
            if (this.jvm.totalMemory() > this.heapsize) {
                this.jvm.runFinalization();
                this.jvm.gc();
                synchronized (this) {
                    while ((this.hashtable.size() > 0) && (this.jvm.freeMemory() < this.freememory)) {
                        this.free();
                    }
                }
            }
            
            try {
                Thread.currentThread().sleep(this.interval);
            } catch (InterruptedException ignore) {}
        }
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
        if ((this.jvm.totalMemory() > this.heapsize) && (this.jvm.freeMemory() < this.freememory)) this.free();
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
     * Frees some of the fast memory used by this store.
     */
    public synchronized void free() {
        Object worst = this.getWorst();
        if (worst != null) this.hashtable.remove(worst);
        this.jvm.runFinalization();
        this.jvm.gc();
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
    public synchronized String getStatus() {
        // give back info on the total memory used.
        StringBuffer buffer = new StringBuffer();
        buffer.append("Memory Object Storage System:<br>");
        buffer.append("Using daemon thread: "+ this.useThread+ "<br>");
        if (useThread) {
            buffer.append("Daemon thread priority: "+ this.priority +"<br>");
            buffer.append("Daemon thread check interval: "+ this.interval +"<br>");
        }
        buffer.append("Minimum required free memory: " + this.freememory + "<br>");
        buffer.append("Minimum heap size: " + this.heapsize + "<br>");
        buffer.append("Current free memory: " + this.jvm.freeMemory() + "<br>");
        buffer.append("Current heap size: " + this.jvm.totalMemory() + "<br><ul>");
        Enumeration e = list();
        while (e.hasMoreElements()) {
            buffer.append("<li>");
            buffer.append(e.nextElement());
            buffer.append("</li>");
        }
        buffer.append("</ul>");
        return buffer.toString();
    }
}