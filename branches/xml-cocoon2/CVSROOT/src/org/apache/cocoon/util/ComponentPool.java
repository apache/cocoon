/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE file.
 */
package org.apache.cocoon.util;

import java.util.Vector;

import org.apache.avalon.Poolable;
import org.apache.avalon.ThreadSafe;
import org.apache.avalon.Loggable;
import org.apache.avalon.util.pool.Pool;
import org.apache.avalon.util.pool.ObjectFactory;
import org.apache.avalon.Recyclable;
import org.apache.cocoon.ComponentFactory;

import org.apache.log.Logger;

/**
 * This is a implementation of <code>Pool</code> for SitemapComponents
 * that is thread safe.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 */
public class ComponentPool implements Pool, ThreadSafe, Loggable {

    public final static int DEFAULT_POOL_SIZE = 8;

    public final static int DEFAULT_WAIT_TIME = (5*100);

    private Logger log;

    /** The resources that are currently free */
	protected Vector availableResources;

    /** Resources that have been allocated out of the pool */
	protected Vector usedResources;

    /** Flag to make sure at least one thread has received notification */
	boolean receivedWakeup;

    /** The number of threads waiting for notification */
	int numThreadsWaiting;

    protected ObjectFactory m_factory = null;

    protected int m_initial = DEFAULT_POOL_SIZE/2;

    protected int m_maximum = DEFAULT_POOL_SIZE;

    public ComponentPool(final ObjectFactory factory) throws Exception {
        init(factory, DEFAULT_POOL_SIZE/2, DEFAULT_POOL_SIZE);
    }

    public ComponentPool(final ObjectFactory factory,
                         final int initial) throws Exception {
        init(factory, initial, initial);
    }

    public ComponentPool(final ObjectFactory factory,
                         final int initial,
                         final int maximum) throws Exception {
        init(factory, initial, maximum);
    }

    private void init(final ObjectFactory factory, 
                      final int initial, 
                      final int maximum) throws Exception {
        m_factory = factory;
        m_initial = initial;
        m_maximum = maximum;
    }

    public void init() throws Exception {
		availableResources = new Vector();
		usedResources = new Vector();
		receivedWakeup = true;
		numThreadsWaiting = 0;

        for( int i = 0; i < m_initial; i++ )
            availableResources.addElement(m_factory.newInstance());
    }

    public void setLogger(Logger log) {
        if (this.log == null) {
            this.log = log;
        }
    }

    /** Allocates a resource when the pool is empty. By default, this method
     *	returns null, indicating that the requesting thread must wait. This
     *	allows a thread pool to expand when necessary, allowing for spikes in
     *	activity.
     *	@return A new resource, or null to force the requester to wait
     */ 
	protected synchronized Poolable getOverflowResource()
        throws Exception
	{
		Poolable poolable = m_factory.newInstance();
        log.debug("Component Pool - creating Overflow Resource:" 
                        + " Resource=" + poolable
                        + " Available=" + availableResources.size()
                        + " Used=" + usedResources.size() );
        return poolable;
	}

    /** Grabs a resource from the free list and moves it to the used list.
     * This method is really the core of the resource pool. The rest of the class
     * deals with synchronization around this method.
     * @return The allocated resource
     */
	protected synchronized Poolable getResourceFromList()
	{
        // See if there is a resource available.
		if (availableResources.size() > 0) {

            // Get the first resource from the free list
			Poolable resource = (Poolable) availableResources.elementAt(0);

            // Remove the resource from the free list
	   	    availableResources.removeElement(resource);

            // Add the resource and its associated info to the used list
			usedResources.addElement(resource);

			return resource;
		}

		return null;
	}

    /** Performs a wait for a specified number of milliseconds.
     * @param timeout The number of milliseconds to wait
     * (wait forever if timeout < 0)
     */
	protected synchronized void doWait(long timeout)
	{
		try {
			if (timeout < 0) {
				wait();
			}
			else {
				wait(timeout);
			}
		}
		catch (Exception ignore) {
		}
	}

    /** Requests a resource from the pool, waiting forever if one is not available.
     * No extra information is associated with the allocated resource.
     * @return The allocated resource
     */
	public Poolable get()
        throws Exception
	{
		return get(DEFAULT_WAIT_TIME);
	}

    /** Requests a resource from the pool, waiting forever if one is not available.
     * @param timeout The maximum amount of time (in milliseconds)
     * to wait for the resource
     * @return The allocated resource
     */
	public Poolable get(long timeout) 
        throws Exception
	{
        // See if there is a resource in the pool already
		Poolable resource = getResourceFromList();
		if (resource != null)
		{
			return resource;
		}

        // Figure out when to stop waiting
		long endTime = System.currentTimeMillis() + timeout;

		do {

			synchronized(this) {
                // See if there are any available resources in the pool
				if (availableResources.size() == 0) {

                    // Allow subclasses to provide overflow resources
		   	 	    resource = getOverflowResource();

                    // If there was a resource allocated for overflow, add it to the used list
			   	    if (resource != null) {
				        usedResources.addElement(resource);
       	 	  	        return resource;
		   		    }
				}
			}

            // Wait for a resource to be allocated

            // Figure out the longest time to wait before timing out
			long maxWait = endTime - System.currentTimeMillis();
			if (timeout < 0) maxWait = -1;

            // Indicate that there is a thread waiting for a wakeup
			numThreadsWaiting++;

            // Wait for a wakeup
			doWait(maxWait);

			numThreadsWaiting--;

            // Only mention the received wakeup if the timeout hasn't expired
			if ((timeout < 0) || (System.currentTimeMillis() < maxWait)) {
				receivedWakeup = true;
			}

            // See if there is now a resource in the free pool
	   	    resource = getResourceFromList();
		    if (resource != null)
			{
				return resource;
	   	    }

            // Keep looping while the timeout hasn't expired (loop forever if there is
            // no timeout.
		} while ((timeout < 0) || (System.currentTimeMillis() < endTime));

		return null;
	}

    /** Releases a resource back to the pool of available resources
     * @param resource The resource to be returned to the pool
     */
	public void put(Poolable resource)
	{
		int pos = -1;

		synchronized(this) {
            // Make sure the resource is in the used list
	    	pos = usedResources.indexOf(resource);

            if( resource instanceof Recyclable )
            {
                ((Recyclable)resource).recycle();
            }

            // If the resource was in the used list, remove it from the used list and
            // add it back to the free list
			if (pos >= 0) {
				usedResources.removeElementAt(pos);
				availableResources.addElement(resource);
			}
		}

        // If we released a resource, wake up any threads that may be waiting
		if (pos >= 0)
		{
			doWakeup();
		}
	}

    /** Performs a notifyAll (which requires a synchronized method) */
	protected synchronized void doNotify()
	{
		try {
			notifyAll();
		}
		catch (Exception ignore) {
		}
	}

	protected void doWakeup()
	{
        // Wake up any threads waiting for the resource
		receivedWakeup = false;
		do {
			try {
		   		doNotify();
			}
			catch (Exception ignore) {
			}
		}
        // Keep looping while there are threads waiting and none have received a wakeup
		while ((numThreadsWaiting > 0) && !receivedWakeup);
	}
}
