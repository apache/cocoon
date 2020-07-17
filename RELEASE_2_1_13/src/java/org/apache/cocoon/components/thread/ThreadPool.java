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
package org.apache.cocoon.components.thread;

/**
 * The ThreadPool interface gives access to methods needed to inspect and use
 * of  a pool of threads
 *
 * @author <a href="mailto:giacomo.at.apache.org">Giacomo Pati</a>
 * @version CVS $Id: ThreadPool.java 56702 2004-11-05 22:52:05Z giacomo $
 */
public interface ThreadPool
{
    //~ Instance fields --------------------------------------------------------

    /** ThreadPool block policy ABORT */
    String POLICY_ABORT = "ABORT";

    /** ThreadPool block policy DISCARD */
    String POLICY_DISCARD = "DISCARD";

    /** ThreadPool block policy DISCARD-OLDEST */
    String POLICY_DISCARD_OLDEST = "DISCARDOLDEST";

    /** ThreadPool block policy RUN */
    String POLICY_RUN = "RUN";

    /** ThreadPool block policy WAIT */
    String POLICY_WAIT = "WAIT";

    /** The Role name */
    String ROLE = ThreadPool.class.getName(  );

    //~ Methods ----------------------------------------------------------------

    /**
     * The blocking policy used
     *
     * @return DOCUMENT ME!
     */
    String getBlockPolicy(  );

    /**
     * How long will a thread in this pool be idle before it is allowed to be
     * garbage collected
     *
     * @return maximum idle time
     */
    long getKeepAliveTime(  );

    /**
     * How many threads are in this pool at maximum
     *
     * @return maximum size of pool
     */
    int getMaximumPoolSize(  );

    /**
     * Maximum size of the queue
     *
     * @return current size of queue
     */
    int getMaximumQueueSize(  );

    /**
     * How many threads are in this pool at minimum
     *
     * @return minimum size of pool
     */
    int getMinimumPoolSize(  );

    /**
     * The Name of this thread pool
     *
     * @return The name
     */
    String getName(  );

    /**
     * How many threads are currently in this pool
     *
     * @return current size of pool
     */
    int getPoolSize(  );

    /**
     * Get the thread priority used by this pool
     *
     * @return current size of queue
     */
    int getPriority(  );

    /**
     * Current size of the queue.
     *
     * @return current size of queue. If the size of the queue is not
     *         maintained by an implementation -1 should be returned.
     */
    int getQueueSize(  );

    /**
     * Whether this ThreadPool has a queue
     *
     * @return Returns true if this ThreadPool has a queue
     */
    boolean isQueued(  );

    /**
     * Returns true if a shutDown method has succeeded in terminating all
     * threads
     *
     * @return Whether a shutDown method has succeeded in terminating all
     *         threads
     */
    boolean isTerminatedAfterShutdown(  );

    /**
     * Execute a command using this pool
     *
     * @param command a {@link Runnable} to execute
     *
     * @throws InterruptedException In case of interruption
     */
    void execute( Runnable command )
        throws InterruptedException;

    /**
     * Terminates all threads possibly awaiting processing all elements
     * currently in queue.
     */
    void shutdown(  );
}
