/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.store;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import org.apache.avalon.component.Component;
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;
import org.apache.avalon.parameters.Parameters;
import org.apache.avalon.thread.ThreadSafe;

/**
 * This class provides a cache algorithm for the requested documents.
 * It combines a HashMap and a LinkedList to create a so called MRU
 * (Most Recently Used) cache.
 * The cached objects also have a "lifecycle". If the "lifecycle" of a
 * object is over, it "dies" like in real life :-) and a new object will
 * be born.
 * Also could the number of objects in the cache be limited. If the Limit is
 * reache, the last object in the cache will be removed.
 *
 * The idea was token from the "Writing Advanced Applikation Tutorial" from
 * javasoft. Many thanx to the writers!
 *
 * @author <a href="mailto:g-froehlich@gmx.de">Gerhard Froehlich</a>
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 */

public class MRUMemoryStore implements Store, Configurable, ThreadSafe, Runnable {
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
   * Indicates the time in seconds to sleep between memory checks.
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

  /**
   * Indicates the object lifetime
   */
  private int ObjectLifeTime;

  /**
   * Indicates the max. object in the cache
   */
  private int maxobjects;


  /**
   * The heart of the cache
   */
  private HashMap cache;
  private LinkedList mrulist;

  private Runtime jvm;

  public MRUMemoryStore() {
    this.jvm     = Runtime.getRuntime();
    this.cache   = new HashMap();
    this.mrulist = new LinkedList();
  }

  /**
   * Initialize the MRUMemoryStore.
   * A few options can be used :
   * <UL>
   *  <LI>freememory = How much memory to keep free for normal jvm operation. (Default: 1 Mb)</LI>
   *  <LI>heapsize = The size of the heap before cleanup starts. (Default: 60 Mb)</LI>
   *  <LI>usethread = use a cleanup daemon thread. (Default: true)</LI>
   *  <LI>threadpriority = priority to run cleanup thread (1-10). (Default: 10)</LI>
   *  <LI>interval = time in seconds to sleep between memory checks (Default: 10 seconds)</LI>
   *  <LI>objectlifetime = Object lifetime in seconds
   * </UL>
   */

  public void configure(Configuration conf) throws ConfigurationException {
        Parameters params = Parameters.fromConfiguration(conf);

        this.freememory     = params.getParameterAsInteger("freememory",1000000);
        this.heapsize       = params.getParameterAsInteger("heapsize",60000000);
        this.ObjectLifeTime = params.getParameterAsInteger("objectlifetime",300);
        this.interval       = params.getParameterAsInteger("interval",10);
        this.maxobjects     = params.getParameterAsInteger("maxobjects",100);
        this.priority       = params.getParameterAsInteger("threadpriority",Thread.currentThread().getPriority());

        if ((this.priority < 1) || (this.priority > 10)) {
          throw new ConfigurationException("Thread priority must be between 1 and 10");
        }

        this.useThread = params.getParameter("usethread","true").equals("true");
        if (this.useThread) {
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
           while ((this.cache.size() > 0) && (this.jvm.freeMemory() < this.freememory)) {
               this.free();
           }
         }
       }
       try {
         Thread.currentThread().sleep(this.interval * 1000);
       } catch (InterruptedException ignore) {}
     }
   }

  /**
   * Store the given object in a persistent state. It is up to the
   * caller to ensure that the key has a persistent state across
   * different JVM executions.
   */
  public void store(Object key, Object value) {
    this.hold(key,value);
  }

  /**
   * This method holds the requested object in a HashMap combined with a LinkedList to
   * create the MRU.
   */
  public void hold(Object key, Object value) {
    /** ...first test if the max. objects in cache is reached... */
    if(this.mrulist.size() >= this.maxobjects) {
      /** ...ok, heapsize is reached, remove the last element... */
      this.free();
    }
    /** ..put the new object in the cache, on the top of course ... */
    this.cache.put(key, new CacheObject(value,System.currentTimeMillis()));
    this.mrulist.addFirst(key);
  }

  /**
   * Get the object associated to the given unique key.
   */
  public Object get(Object key) {
    try {
      long TimeDiff = System.currentTimeMillis() - ((CacheObject)this.cache.get(key)).getCreateTime();
      /** ...check if the object life time is reached... */
      if(TimeDiff >= (this.ObjectLifeTime * 1000)) {
        this.remove(key);
        return null;
      }
      /** put the accessed key on top of the linked list */
      this.mrulist.remove(key);
      this.mrulist.addFirst(key);
      return ((CacheObject)this.cache.get(key)).getCacheObject();
    } catch(NullPointerException e) {
      return null;
    }
  }

  /**
   * Remove the object associated to the given key and returns
   * the object associated to the given key or null if not found.
   */
  public void remove(Object key) {
    this.cache.remove(key);
    this.mrulist.remove(key);
  }

  /**
   * Indicates if the given key is associated to a contained object.
   */
  public synchronized boolean containsKey(Object key) {
    return this.cache.containsKey(key);
  }

  /**
   * Returns the list of used keys as an Enumeration of Objects.
   */
  public Enumeration keys() {
    /* Not yet implemented */
    return null;
  }

  /**
   * Frees some of the fast memory used by this store.
   * It removes the last element in the cache.
   */
  public void free() {
    this.cache.remove(this.mrulist.getLast());
    this.mrulist.removeLast();
  }

  /**
   * Container object for the documents.
   */
  class CacheObject {
    private long time = -1;
    private Object cacheObject;

    public CacheObject(Object ToCacheObject, long lTime) {
      this.cacheObject = ToCacheObject;
      this.time = lTime;
    }

    public Object getCacheObject() {
      return cacheObject;
    }

    public long getCreateTime() {
      return time;
    }
  }
}
