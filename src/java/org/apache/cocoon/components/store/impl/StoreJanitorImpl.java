/*
 * Copyright 2002-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.StoreJanitor;

/**
 * This class is a implentation of a StoreJanitor. Store classes
 * can register to the StoreJanitor. When memory is too low,
 * the StoreJanitor frees the registered caches until memory is normal.
 *
 * <p>A few parameters can be used:
 * <UL>
 *  <LI><B>freememory</B>: How many bytes shall be always free in the JVM (Default: 1mb)</LI>
 *  <LI><B>heapsize</B>: Maximum possible size of the JVM memory consumption (Default: 64mb)</LI>
 *  <LI><B>cleanupthreadinterval</B>: How often (sec) shall run the cleanup thread (Default: 10s)</LI>
 *  <LI><B>adaptivethreadinterval</B> (experimental): Enable adaptive algorithm to determine thread interval
 *      (Default: false) When true, <code>cleanupthreadinterval</code> defines the maximum cleanup interval.
 *      Cleanup interval then is determined based on the memory fill rate: the faster memory is filled in,
 *      and the less free memory is left, the shorter is the cleanup time.</LI>
 *  <LI><B>threadpriority</B>: priority of the thread (1-10). (Default: 10)</LI>
 *  <LI><B>percent_to_free</B>: What fraction of the store to free when memory is low (1-100). (Default: 10%)</LI>
 *  <LI><B>invokegc</B>: Invoke the gc on low memory first (true|false; default: false)</LI>
 * </UL></p>
 *
 * @avalon.component
 * @avalon.service type=StoreJanitor
 * @x-avalon.info name=store-janitor
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Id: StoreJanitorImpl.java,v 1.4 2004/02/28 11:47:31 cziegeler Exp $
 */
public class StoreJanitorImpl extends AbstractLogEnabled
                              implements StoreJanitor, Parameterizable, ThreadSafe,
                                         Runnable, Startable {

    // Configuration parameters
    private int minFreeMemory = -1;
    private int maxHeapSize = -1;
    private int threadInterval = -1;
    private int minThreadInterval = 500;
    private boolean adaptiveThreadInterval;
    private int priority = -1;
    private double fraction;

    private Runtime jvm;
    private ArrayList storelist;
    private int index = -1;

    /** Should the gc be called on low memory? */
    protected boolean invokeGC;

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
     * Parameterize the StoreJanitorImpl.
     */
    public void parameterize(Parameters params) throws ParameterException {
        this.jvm = Runtime.getRuntime();
        this.minFreeMemory = params.getParameterAsInteger("freememory", 1024 * 1024);
        this.maxHeapSize = params.getParameterAsInteger("heapsize", 60 * 1024 * 1024);
        // Parameter value is in seconds, converted to millis
        this.threadInterval = params.getParameterAsInteger("cleanupthreadinterval", 10) * 1000;
        this.adaptiveThreadInterval = params.getParameterAsBoolean("adaptivethreadinterval", false);
        this.priority = params.getParameterAsInteger("threadpriority", Thread.currentThread().getPriority());
        int percent = params.getParameterAsInteger("percent_to_free", 10);
        this.invokeGC = params.getParameterAsBoolean("invokegc", this.invokeGC);

        if (getMinFreeMemory() < 1) {
            throw new ParameterException("StoreJanitorImpl freememory parameter has to be greater then 1");
        }
        if (getMaxHeapSize() < 1) {
            throw new ParameterException("StoreJanitorImpl heapsize parameter has to be greater then 1");
        }
        if (getThreadInterval() < 1) {
            throw new ParameterException("StoreJanitorImpl cleanupthreadinterval parameter has to be greater then 1");
        }
        if (getPriority() < 1 || getPriority() > 10) {
            throw new ParameterException("StoreJanitorImpl threadpriority has to be between 1 and 10");
        }
        if (percent > 100 && percent < 1) {
            throw new ParameterException("StoreJanitorImpl percent_to_free, has to be between 1 and 100");
        }

        this.fraction = percent / 100.0D;
        this.storelist = new ArrayList();

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("minimum free memory=" + getMinFreeMemory());
            getLogger().debug("heapsize=" + getMaxHeapSize());
            getLogger().debug("thread interval=" + getThreadInterval());
            getLogger().debug("adaptivethreadinterval=" + getAdaptiveThreadInterval());
            getLogger().debug("priority=" + getPriority());
            getLogger().debug("percent=" + percent);
            getLogger().debug("invoke gc=" + this.invokeGC);
        }
    }

    public void start() throws Exception {
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

    public void stop() {
        this.doRun = false;
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
     * Round Robin alghorithm for freeing the registered caches.
     */
    private void freeMemory() {
        // TODO: Alternative to RR might be to free same fraction from every storage.
        try {
            // Determine the store.
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

            // Delete proportionate elements out of the store as configured.
            Store store = (Store)getStoreList().get(getIndex());
            int limit = calcToFree(store);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Freeing " + limit + " items from store #" + getIndex());
            }

            for (int i = 0; i < limit; i++) {
                try {
                    store.free();
                } catch (OutOfMemoryError e) {
                    getLogger().error("OutOfMemoryError in freeMemory()");
                }
            }
        } catch (Exception e) {
            getLogger().error("Error in freeMemory()", e);
        } catch (OutOfMemoryError e) {
            getLogger().error("OutOfMemoryError in freeMemory()");
        }
    }

    /**
     * This method claculates the number of Elements to be freememory
     * out of the Cache.
     *
     * @param store the Store which was selected as victim
     * @return number of elements to be removed!
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

    private void setIndex(int _index) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Setting index=" + _index);
        }
        this.index = _index;
    }

    private int getIndex() {
        return this.index;
    }
}
