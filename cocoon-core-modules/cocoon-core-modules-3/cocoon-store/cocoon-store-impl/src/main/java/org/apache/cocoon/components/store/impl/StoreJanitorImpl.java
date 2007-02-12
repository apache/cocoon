/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.store.impl;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.StoreJanitor;

/**
 * This class is a implentation of a StoreJanitor. Store classes
 * can register to the StoreJanitor. When memory is too low,
 * the StoreJanitor frees the registered caches until memory is normal.
 *
 * <p>A few parameters can be used:
 * <ul>
 *  <li><b>freememory</b>: How many bytes shall be always free in the JVM (Default: 1mb)</li>
 *  <li><b>heapsize</b>: Maximum possible size of the JVM memory consumption (Default: 64mb)</li>
 *  <li><b>cleanupthreadinterval</b>: How often (sec) shall run the cleanup thread (Default: 10s)</li>
 *  <li><b>adaptivethreadinterval</b> (experimental): Enable adaptive algorithm to determine thread interval
 *      (Default: false) When true, <code>cleanupthreadinterval</code> defines the maximum cleanup interval.
 *      Cleanup interval then is determined based on the memory fill rate: the faster memory is filled in,
 *      and the less free memory is left, the shorter is the cleanup time.</li>
 *  <li><b>threadpriority</b>: priority of the thread (1-10). (Default: current thread's priority)</li>
 *  <li><b>percent_to_free</b>: What fraction of the store to free when memory is low (1-100). (Default: 10%)</li>
 *  <li><b>invokegc</b>: Invoke the gc on low memory first (true|false; default: false)</li>
 *  <li><b>freeingalgorithm</b>: Currently there are two algorithms available. (Default: round-robun)
 *   <dl>
 *    <dt>round-robin</dt>
 *    <dd>The registered caches are cycled through,
 *     and each time there is a low memory situation one of the
 *     registered caches has objects freed from it.</dd>
 *    <dt>all-stores</dt>
 *    <dd>All registered stores have objects removed from them
 *    each time there is a low memory situation.</dd>
 * </ul></p>
 *
 * @version $Id$
 */
public class StoreJanitorImpl implements StoreJanitor, Runnable {

    // Cleaning Algorithms
    private static final String ALG_ROUND_ROBIN = "round-robin";
    private static final String ALG_ALL_STORES = "all-stores";

    // Note: this class doesn't need to be Startable. This allows the janitor thread to be
    // lazily created the first time a store registers itsefl

    private static final int FREE_MEMORY = 1024 * 1024;
    private static final int HEAP_SIZE = 66600000;
    private static final int CLEAN_UP_THREAD_INTERVAL = 10 * 1000;
    private static final boolean ADAPTIVE_THREAD_INTERVAL = false;
    private static final int PERCENT_TO_FREE = 10;

    /** By default we use the logger for this class. */
    private Log logger = LogFactory.getLog(getClass());

    // Configuration parameters
    private int minFreeMemory = FREE_MEMORY;
    private int maxHeapSize = HEAP_SIZE;
    private int threadInterval = CLEAN_UP_THREAD_INTERVAL;
    private int minThreadInterval = 500;
    private boolean adaptiveThreadInterval = ADAPTIVE_THREAD_INTERVAL;
    private int priority = -1;
    private String freeingAlgorithm = ALG_ROUND_ROBIN;
    private double fraction = PERCENT_TO_FREE / 100.0D;

    /** Should the gc be called on low memory? */
    protected boolean invokeGC;

    // Runtime state
    private Runtime jvm;
    private ArrayList storelist;
    private int index = -1;

    private boolean doRun;

    /**
     * Amount of memory in use before sleep(). Must be initially set a resonable
     * value; ie. <code>memoryInUse()</code>
     */
    protected long inUse;

    private boolean firstRun = true;

    /** The calculated delay for the next checker run in ms */
    protected long interval = Long.MAX_VALUE;

    /** Used memory change rate in bytes per second */
    private long maxRateOfChange = 1;


    /**
     * How much free memory shall be available in the jvm?
     * If not specified, defaults to 1Mb.
     * 
     * @param freeMemory
     */
    public void setFreeMemory(int freeMemory) {
        this.minFreeMemory = freeMemory;
    }

    /**
     * How much memory at max jvm can consume?
     * The default max heapsize for Sun's JVM is (almost) 64Mb,
     * can be increased by specifying -Xmx command line parameter.
     * If not specified, defaults to 66600000 bytes.
     *
     * @param heapSize
     */
    public void setHeapSize(int heapSize) {
        this.maxHeapSize = heapSize;
    }

    /**
     * How often shall the cleanup thread check memory?
     * If not specified, defaults to 10 seconds.
     * 
     * @param cleanupThreadInterval
     */
    public void setCleanupThreadInterval(int cleanupThreadInterval) {
        this.threadInterval = cleanupThreadInterval * 1000;
    }

    /**
     * Experimental adaptive algorithm for cleanup interval
     *
     * @param adaptiveThreadInterval
     */
    public void setAdaptiveThreadInterval(boolean adaptiveThreadInterval) {
        this.adaptiveThreadInterval = adaptiveThreadInterval;
    }

    /**
     * What percent of the store elements shall be dropped on low memory?
     * If not specified, defaults to 10%
     * 
     * @param percentToFree
     */
    public void setPercentToFree(double percentToFree) {
        this.fraction = percentToFree / 100.0D;
    }

    /**
     * Shall garbage collector be invoked on low memory?
     * If not specified, defaults to false.
     * 
     * @param invokeGC
     */
    public void setInvokeGC(boolean invokeGC) {
        this.invokeGC = invokeGC;
    }

    /**
     * What should be the priority of the cleanup thread?
     * This property is used only by older implementation of the janitor.
     * New implementation uses centrally configured thread pool (see
     * thread-pools element below).
     * 
     * @param threadPriority
     */
    public void setThreadPriority(int threadPriority) {
        this.priority = threadPriority;
    }

    public void setFreeingAlgorithm(String algorithm) {
        this.freeingAlgorithm = algorithm;
    }

    public Log getLogger() {
        return this.logger;
    }

    public void setLogger(Log l) {
        this.logger = l;
    }

    /**
     * Initialize the StoreJanitorImpl.
     * @throws Exception 
     */
    public void init() throws Exception {
        this.jvm = Runtime.getRuntime();
        if (this.priority == -1) {
            this.priority = Thread.currentThread().getPriority();
        }

        if (getMinFreeMemory() < 1) {
            throw new Exception("StoreJanitorImpl freememory parameter has to be greater then 1");
        }
        if (getMaxHeapSize() < 1) {
            throw new Exception("StoreJanitorImpl heapsize parameter has to be greater then 1");
        }
        if (getThreadInterval() < 1) {
            throw new Exception("StoreJanitorImpl cleanupthreadinterval parameter has to be greater then 1");
        }
        if (getPriority() < 1 || getPriority() > 10) {
            throw new Exception("StoreJanitorImpl threadpriority has to be between 1 and 10");
        }
        if (fraction > 1 && fraction < 0.01) {
            throw new Exception("StoreJanitorImpl percentToFree, has to be between 1 and 100");
        }
        if (!(this.freeingAlgorithm.equals(ALG_ROUND_ROBIN) || this.freeingAlgorithm.equals(ALG_ALL_STORES))) {
            throw new Exception("StoreJanitorImpl freeingAlgorithm, has to be 'round-robin' or 'all-stores'. '" 
                    + this.freeingAlgorithm + "' is not supported.");
        }

        this.storelist = new ArrayList();

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("minimum free memory=" + getMinFreeMemory());
            getLogger().debug("heapsize=" + getMaxHeapSize());
            getLogger().debug("thread interval=" + getThreadInterval());
            getLogger().debug("adaptivethreadinterval=" + getAdaptiveThreadInterval());
            getLogger().debug("priority=" + getPriority());
            getLogger().debug("percent=" + fraction * 100);
            getLogger().debug("invoke gc=" + this.invokeGC);
        }

        doStart();
    }

    private void doStart() throws Exception {
        this.doRun = true;
        Thread checker = new Thread(this);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Intializing checker thread");
        }
        checker.setPriority(getPriority());
        checker.setDaemon(true);
        checker.setName("checker");
        checker.start();
    }

    private void doStop() {
        this.doRun = false;
    }

    public void destroy() {
        doStop();
    }

    /**
     * The "checker" thread loop.
     */
    public void run() {
        this.inUse = memoryInUse();
        while (this.doRun) {
            checkMemory();

            // Sleep
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Sleeping for " + this.interval + "ms");
            }
            try {
                Thread.sleep(this.interval);
            } catch (InterruptedException ignore) {
                /* ignored */
            }

            // Ignore change in memory during the first run (startup)
            if (this.firstRun) {
                this.firstRun = false;
                this.inUse = memoryInUse();
            }
        }
    }

    /**
     * The "checker" thread checks if memory is running low in the jvm.
     */
    protected void checkMemory() {
        if (getAdaptiveThreadInterval()) {
            // Monitor the rate of change of heap in use.
            long change = memoryInUse() - inUse;
            long rateOfChange = longDiv(change * 1000, interval); // bps.
            if (maxRateOfChange < rateOfChange) {
                maxRateOfChange = (maxRateOfChange + rateOfChange) / 2;
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Waking after " + interval + "ms, in use change "
                                  + change + "b to " + memoryInUse() + "b, rate "
                                  + rateOfChange + "b/sec, max rate " + maxRateOfChange + "b/sec");
            }
        }

        // Amount of memory used is greater than heapsize
        if (memoryLow()) {
            if (this.invokeGC) {
                freePhysicalMemory();
            }

            synchronized (this) {
                if (!this.invokeGC
                        || (memoryLow() && getStoreList().size() > 0)) {

                    freeMemory();
                    setIndex(getIndex() + 1);
                }
            }
        }

        if (getAdaptiveThreadInterval()) {
            // Calculate sleep interval based on the change rate and free memory left
            interval = minTimeToFill(maxRateOfChange) * 1000 / 2;
            if (interval > this.threadInterval) {
                interval = this.threadInterval;
            } else if (interval < this.minThreadInterval) {
                interval = this.minThreadInterval;
            }
            inUse = memoryInUse();
        } else {
            interval = this.threadInterval;
        }
    }

    /**
     * Method to check if memory is running low in the JVM.
     *
     * @return true if memory is low
     */
    private boolean memoryLow() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("JVM Memory total: " + getJVM().totalMemory()
                              + ", free: " + getJVM().freeMemory());
        }

        if ((getJVM().totalMemory() >= getMaxHeapSize())
                && (getJVM().freeMemory() < getMinFreeMemory())) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Memory is low!");
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Calculate the JVM memory in use now.
     *
     * @return memory in use.
     */
    protected long memoryInUse() {
        return jvm.totalMemory() - jvm.freeMemory();
    }

    /**
     * Calculate amount of time needed to fill all free memory with given
     * fill rate.
     *
     * @param rate memory fill rate in time per bytes
     * @return amount of time to fill all the memory with given fill rate
     */
    private long minTimeToFill(long rate) {
        return longDiv(jvm.freeMemory(), rate);
    }

    private long longDiv(long top, long bottom) {
        try {
            return top / bottom;
        } catch (Exception e) {
            return top > 0 ? Long.MAX_VALUE : Long.MIN_VALUE;
        }
    }

    /**
     * This method register the stores
     *
     * @param store the store to be registered
     */
    public synchronized void register(Store store) {
        getStoreList().add(store);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Registered store instance " + store + ". Stores now: "
                              + getStoreList().size());
        }
    }

    /**
     * This method unregister the stores
     *
     * @param store the store to be unregistered
     */
    public synchronized void unregister(Store store) {
        getStoreList().remove(store);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Unregistered store instance " + store + ". Stores now: "
                              + getStoreList().size());
        }
    }

    /**
     * This method return a java.util.Iterator of every registered stores
     *
     * <i>The iterators returned is fail-fast: if list is structurally
     * modified at any time after the iterator is created, in any way, the
     * iterator will throw a ConcurrentModificationException.  Thus, in the
     * face of concurrent modification, the iterator fails quickly and
     * cleanly, rather than risking arbitrary, non-deterministic behavior at
     * an undetermined time in the future.</i>
     *
     * @return a java.util.Iterator
     */
    public Iterator iterator() {
        return getStoreList().iterator();
    }

    /**
     * Free configured percentage of objects from stores
     * based on selected algorithm.
     */
    private void freeMemory() {
        try {
            // What algorithm was selected?

            // Option 1: Downsize all registered stores
            if (this.freeingAlgorithm.equals(ALG_ALL_STORES)) {
                for (Iterator i = iterator(); i.hasNext(); ) {
                    removeStoreObjects((Store) i.next());
                }

                return;
            }

            // Option 2: Default to Round Robin
            // Determine the store to clear this time around.
            if (getIndex() < getStoreList().size()) {
                if (getIndex() == -1) {
                    setIndex(0);
                }
            } else {
                // Store list changed (one or more store has been removed).
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Restarting from the beginning");
                }
                setIndex(0);
            }

            // Remove the objects from this store
            removeStoreObjects((Store) getStoreList().get(getIndex()));

        } catch (Exception e) {
            getLogger().error("Error in freeMemory()", e);
        } catch (OutOfMemoryError e) {
            getLogger().error("OutOfMemoryError in freeMemory()");
        }
    }

    /**
     * This method clears the configured amount of objects from
     * the provided store
     *
     * @param store the Store from which to release the objects
     */
    private void removeStoreObjects(Store store) {
        // Calculate how many objects to release from the store
        int limit = calcToFree(store);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Freeing " + limit + " items from store " + store + " with "
                              + store.size() + " items.");
        }

        // Remove the calculated number of objects from the current store
        for (int i = 0; i < limit; i++) {
            try {
                store.free();
            } catch (OutOfMemoryError e) {
                getLogger().error("OutOfMemoryError while releasing an object from the store.");
            }
        }
    }

    /**
     * This method calculates the number of items to free
     * from the store.
     *
     * @param store the Store which was selected as a victim
     * @return number of items to be removed
     */
    private int calcToFree(Store store) {
        int cnt = store.size();
        if (cnt < 0) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Unknown size of the store: " + store);
            }
            return 0;
        }

        final int res = (int) (cnt * fraction);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Calculating size for store " + store + " with size " + cnt + ": " + res);
        }

        return res;
    }

    /**
     * This method forces the garbage collector
     */
    private void freePhysicalMemory() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Invoking GC. Memory total: "
                              + getJVM().totalMemory() + ", free: "
                              + getJVM().freeMemory());
        }

        getJVM().runFinalization();
        getJVM().gc();

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("GC complete. Memory total: "
                              + getJVM().totalMemory() + ", free: "
                              + getJVM().freeMemory());
        }
    }

    private int getMinFreeMemory() {
        return this.minFreeMemory;
    }

    private int getMaxHeapSize() {
        return this.maxHeapSize;
    }

    private int getPriority() {
        return this.priority;
    }

    private int getThreadInterval() {
        return this.threadInterval;
    }

    private boolean getAdaptiveThreadInterval() {
        return this.adaptiveThreadInterval;
    }

    private Runtime getJVM() {
        return this.jvm;
    }

    private ArrayList getStoreList() {
        return this.storelist;
    }

    private int getIndex() {
        return this.index;
    }

    private void setIndex(int _index) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Setting index=" + _index);
        }
        this.index = _index;
    }
}
