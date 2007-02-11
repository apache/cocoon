/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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
package org.apache.cocoon.storejanitor;

import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.StoreJanitor;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * This class is a implentation of a StoreJanitor. Store classes
 * can register to the StoreJanitor. When memory is too low,
 * the StoreJanitor frees the registered caches until memory is normal.
 *
 *@author     <a href="mailto:cs@ffzj0ia9.bank.dresdner.net">Christian Schmitt</a>
 *@author     <a href="mailto:g-froehlich@gmx.de">Gerhard Froehlich</a>
 *@author     <a href="mailto:proyal@managingpartners.com">Peter Royal</a>
 *@author     <a href="mailto:pdh@totalise.co.uk">Peter Hargreaves</a>
 *
 *@version CVS $Id: StoreJanitorImpl.java,v 1.2 2003/03/24 14:33:56 stefano Exp $
 */
public class StoreJanitorImpl
         extends AbstractLogEnabled
         implements StoreJanitor,
                    Configurable,
                    ThreadSafe,
                    Runnable,
                    Startable {
    private static boolean doRun = false;

    private int freememory = -1;
    private int heapsize = -1;
    private int cleanupthreadinterval = -1;
    private int priority = -1;
    private Runtime jvm;
    private ArrayList storelist;
    private int index = -1;
    private int m_percent;

    private long inUseAfter;// Remember while sleeping.
    private long sleepPeriod;// Remember while sleeping.
    private long maxRateOfChange;

    /**
     * Initialize the StoreJanitorImpl.
     * A few options can be used :
     * <UL>
     *  <LI>freememory = how many bytes shall be always free in the jvm</LI>
     *  <LI>heapsize = max. size of jvm memory consumption</LI>
     *  <LI>cleanupthreadinterval = how often (sec) shall run the cleanup thread</LI>
     *  <LI>threadpriority = priority of the thread (1-10). (Default: 10)</LI>
     * </UL>
     *
     *@param  conf                        Description of Parameter
     *@exception  ConfigurationException
     *@since
     */
    public void configure(Configuration conf) throws ConfigurationException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Configure StoreJanitorImpl");
        }
        this.setJVM(Runtime.getRuntime());

        Parameters params = Parameters.fromConfiguration(conf);
        this.setFreememory(params.getParameterAsInteger("freememory", 1000000));
        this.setHeapsize(params.getParameterAsInteger("heapsize", 60000000));
        this.setCleanupthreadinterval(params.getParameterAsInteger("cleanupthreadinterval", 10));
        this.setPriority(params.getParameterAsInteger("threadpriority",
                Thread.currentThread().getPriority()));
        this.m_percent = params.getParameterAsInteger("percent_to_free", 10);

        if ((this.getFreememory() < 1)) {
            throw new ConfigurationException("StoreJanitorImpl freememory parameter has to be greater then 1");
        }
        if ((this.getHeapsize() < 1)) {
            throw new ConfigurationException("StoreJanitorImpl heapsize parameter has to be greater then 1");
        }
        if ((this.getCleanupthreadinterval() < 1)) {
            throw new ConfigurationException("StoreJanitorImpl cleanupthreadinterval parameter has to be greater then 1");
        }
        if ((this.getPriority() < 1)) {
            throw new ConfigurationException("StoreJanitorImpl threadpriority has to be greater then 1");
        }
        if ((this.m_percent > 100 && this.m_percent < 1)) {
            throw new ConfigurationException("StoreJanitorImpl percent_to_free, has to be between 1 and 100");
        }

        this.setStoreList(new ArrayList());
    }

    /**
     *Description of the Method
     *
     *@since
     */
    public void start() {
        doRun = true;
        Thread checker = new Thread(this);
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Intializing checker thread");
        }
        checker.setPriority(this.getPriority());
        checker.setDaemon(true);
        checker.setName("checker");
        checker.start();
    }

    /**
     *Description of the Method
     *
     *@since
     */
    public void stop() {
        doRun = false;
    }// Remember while sleeping.

    /**
     * The "checker" thread checks if items should be removed from the stores.
     *
     *@since
     */
    public void run() {
        // Initialise for a safe first calculation of rate of change.
        inUseAfter = inUseNow();
        sleepPeriod = Long.MAX_VALUE;
        maxRateOfChange = 1;
        while (doRun) {
            // Monitor the rate of change of heap in use.
            long changeWhileSleeping = inUseNow() - inUseAfter;
            long rateOfChange = longDiv(changeWhileSleeping, sleepPeriod);// bpms (same as or kbps).
            if (maxRateOfChange < rateOfChange) {
                maxRateOfChange = (maxRateOfChange + rateOfChange) / 2;
            }
            // Output debug info.
            debug("Waking after " + sleepPeriod + "ms, in use change " + changeWhileSleeping + " to " + inUseNow() + ", rate " + rateOfChange + "kb/sec, max rate " + maxRateOfChange + "kb/sec");
            debug("maxHeap=" + getMaxHeap() + ", totalHeap=" + jvm.totalMemory() + ", heapIsBig=" + heapIsBig());
            debug("minFree=" + getMinFree() + ",  freeHeap=" + jvm.freeMemory() + ", freeIsLow=" + freeIsLow());
            // If the heap is big, and the free memory is low.
            if (heapIsBig() && freeIsLow()) {
                synchronized (this) {
                    attemptToFreeStorage();
                }
            }
            // Remember memory in use before sleeping in order to calc slope when waking.
            inUseAfter = inUseNow();
            // If time to half fill could be less than max sleep, then sleep for half min time to fill (& remember it to calc slope next time).
            sleepPeriod = minTimeToFill() / 2 < getMaxSleep() ? minTimeToFill() / 2 : getMaxSleep();
            debug("Store checker going to sleep for " + sleepPeriod + "ms, (max sleep=" + getMaxSleep() + "ms), with memory in use=" + inUseAfter);
            try {
                Thread.sleep(sleepPeriod);
            } catch (InterruptedException ignore) {
            }
        }
    }

    /**
     * This method register the stores
     *
     *@param  store  Description of Parameter
     *@since
     */
    public void register(Store store) {
        this.getStoreList().add(store);
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Registering store instance");
            this.getLogger().debug("Size of StoreJanitor now:"
                     + this.getStoreList().size());
        }
    }

    /**
     * This method unregister the stores
     *
     *@param  store  Description of Parameter
     *@since
     */
    public void unregister(Store store) {
        this.getStoreList().remove(store);
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Unregister store instance");
            this.getLogger().debug("Size of StoreJanitor now:"
                     + this.getStoreList().size());
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
     *@return    a java.util.Iterator
     *@since
     */
    public Iterator iterator() {
        return this.getStoreList().iterator();
    }

    /**
     *Sets the freememory attribute of the StoreJanitorImpl object
     *
     *@param  _freememory  The new freememory value
     *@since
     */
    private void setFreememory(int _freememory) {
        this.freememory = _freememory;
    }

    /**
     *Sets the heapsize attribute of the StoreJanitorImpl object
     *
     *@param  _heapsize  The new heapsize value
     *@since
     */
    private void setHeapsize(int _heapsize) {
        this.heapsize = _heapsize;
    }

    /**
     *Sets the cleanupthreadinterval attribute of the StoreJanitorImpl object
     *
     *@param  _cleanupthreadinterval  The new cleanupthreadinterval value
     *@since
     */
    private void setCleanupthreadinterval(int _cleanupthreadinterval) {
        this.cleanupthreadinterval = _cleanupthreadinterval;
    }

    /**
     *Sets the priority attribute of the StoreJanitorImpl object
     *
     *@param  _priority  The new priority value
     *@since
     */
    private void setPriority(int _priority) {
        this.priority = _priority;
    }

    /**
     *Sets the jVM attribute of the StoreJanitorImpl object
     *
     *@param  _runtime  The new jVM value
     *@since
     */
    private void setJVM(Runtime _runtime) {
        this.jvm = _runtime;
    }

    /**
     *Sets the storeList attribute of the StoreJanitorImpl object
     *
     *@param  _storelist  The new storeList value
     *@since
     */
    private void setStoreList(ArrayList _storelist) {
        this.storelist = _storelist;
    }

    // Renaming of parameters due to new functionality.
    /**
     *Gets the maxHeap attribute of the StoreJanitorImpl object
     *
     *@return    The maxHeap value
     *@since
     */
    private int getMaxHeap() {
        return heapsize;
    }

    /**
     *Gets the minFree attribute of the StoreJanitorImpl object
     *
     *@return    The minFree value
     *@since
     */
    private int getMinFree() {
        return freememory;
    }

    /**
     *Gets the maxSleep attribute of the StoreJanitorImpl object
     *
     *@return    The maxSleep value
     *@since
     */
    private int getMaxSleep() {
        return cleanupthreadinterval * 1000;
    }

    /**
     *Gets the reduceBy attribute of the StoreJanitorImpl object
     *
     *@return    The reduceBy value
     *@since
     */
    private int getReduceBy() {
        return m_percent;
    }

    /**
     *Gets the freememory attribute of the StoreJanitorImpl object
     *
     *@return    The freememory value
     *@since
     */
    private int getFreememory() {
        return freememory;
    }

    /**
     *Gets the heapsize attribute of the StoreJanitorImpl object
     *
     *@return    The heapsize value
     *@since
     */
    private int getHeapsize() {
        return this.heapsize;
    }

    /**
     *Gets the cleanupthreadinterval attribute of the StoreJanitorImpl object
     *
     *@return    The cleanupthreadinterval value
     *@since
     */
    private int getCleanupthreadinterval() {
        return this.cleanupthreadinterval;
    }

    /**
     *Gets the priority attribute of the StoreJanitorImpl object
     *
     *@return    The priority value
     *@since
     */
    private int getPriority() {
        return this.priority;
    }

    /**
     *Gets the storeList attribute of the StoreJanitorImpl object
     *
     *@return    The storeList value
     *@since
     */
    private ArrayList getStoreList() {
        return this.storelist;
    }

    /**
     * Starting at the next store, removes items, moving to the next again if necessary, until the specified number of items have been removed or all the stores are empty.
     *
     *@since
     */
    private void attemptToFreeStorage() {
        int storeListSize = getStoreList().size();
        int remove = getReduceBy();
        debug("number of stores is " + storeListSize + ", number of items to be removed is " + remove + ", if possible!");
        incIndex();
        for (int cnt = 0; cnt < storeListSize; incIndex(), cnt++) {// Look in all stores if necessary.
            if ((remove = reduceStoreBy(remove)) == 0) {
                break;// Keep looking till all items removed,
            }
        }// or all stores are empty.
        if (remove < getReduceBy()) {// If items were removed call garbage collector.
            long gcTime = System.currentTimeMillis();
            jvm.gc();
            gcTime = System.currentTimeMillis() - gcTime;
            debug("items removed, so collecting garbage - took " + gcTime + "ms");
            debug("minFree=" + getMinFree() + ",      freeHeap=" + jvm.freeMemory() + ",          freeIsLow=" + freeIsLow());
        }
    }

    /**
     * Increment the store index.
     *
     *@since
     */
    private void incIndex() {
        if (++index >= getStoreList().size()) {
            index = 0;
        }
    }

    /**
     * Reduce the current store by the number of items specified, if possible.
     *
     *@param  remove  The number of items to be removed.
     *@return         the remaining count of items, that could not be removed.
     *@since
     */
    private int reduceStoreBy(int remove) {
        Store store = (Store) storelist.get(index);
        int sizeBefore = countSize(store);
        for (int i = 0; i < sizeBefore & remove > 0; i++, remove--) {
            store.free();
        }
        int sizeAfter = countSize(store);
        debug("store index=" + index + ", size before=" + sizeBefore + ",  size after=" + sizeAfter + ", removed=" + (sizeBefore - sizeAfter));
        return remove;
    }

    /**
     * To check if total memory is big enough to allow stores to be reduced.
     *
     *@return    true if big enough.
     *@since
     */
    private boolean heapIsBig() {
        return jvm.totalMemory() > getMaxHeap();
    }

    /**
     * To check if free memory is small enough to start reducing stores.
     *
     *@return    true if small enough.
     *@since
     */
    private boolean freeIsLow() {
        return jvm.freeMemory() < getMinFree();
    }

    /**
     * To calculate the minimum time in which the memory could be filled at the maximum rate of use.
     *
     *@return    the minimum time to fill.
     *@since
     */
    private long minTimeToFill() {
        return longDiv(jvm.freeMemory(), maxRateOfChange);
    }

    /**
     * Long division, guarding agains accidental divide by zero.
     *
     *@param  top     Description of Parameter
     *@param  bottom  Description of Parameter
     *@return         the result of division.
     *@since
     */
    private long longDiv(long top, long bottom) {
        try {
            return top / bottom;
        } catch (Exception e) {
            return top > 0 ? Long.MAX_VALUE : Long.MIN_VALUE;
        }
    }

    /**
     * Calculate the jvm memory in use now.
     *
     *@return    memory in use.
     *@since
     */
    private long inUseNow() {
        return jvm.totalMemory() - jvm.freeMemory();
    }

    /**
     * Count the size of a store.
     *
     *@param  store  Description of Parameter
     *@return        the size of the store.
     *@since
     */
    private int countSize(Store store) {
        int size = 0;
        Enumeration enum = store.keys();
        while (enum.hasMoreElements()) {
            size++;
            enum.nextElement();
        }
        return size;
    }

    /**
     * Shorten the call to print a debug message.
     *
     *@param  message  Description of Parameter
     *@since
     */
    private void debug(String message) {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug(message);
        }
    }

}

