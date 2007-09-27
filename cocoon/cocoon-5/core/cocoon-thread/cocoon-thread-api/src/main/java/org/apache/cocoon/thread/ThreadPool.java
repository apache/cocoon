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
package org.apache.cocoon.thread;

/**
 * The ThreadPool interface gives access to methods needed to inspect and use of
 * a pool of threads
 *
 * @version $Id$
 */
public interface ThreadPool {

    // ~ Instance fields
    // --------------------------------------------------------

    /** The default queue size */
    int DEFAULT_QUEUE_SIZE = -1;

    /** The default maximum pool size */
    int DEFAULT_MAX_POOL_SIZE = 5;

    /** The default minimum pool size */
    int DEFAULT_MIN_POOL_SIZE = 5;

    /** The default thread priority */
    String DEFAULT_THREAD_PRIORITY = "NORM";

    /** The default daemon mode */
    boolean DEFAULT_DAEMON_MODE = false;

    /** The default keep alive time */
    long DEFAULT_KEEP_ALIVE_TIME = 60000L;

    /** The default way to shutdown gracefully */
    boolean DEFAULT_SHUTDOWN_GRACEFUL = false;

    /** The default shutdown waittime time */
    int DEFAULT_SHUTDOWN_WAIT_TIME = -1;

    /** The default shutdown waittime time */
    String DEFAULT_THREADPOOL_NAME = "default";

    /** The default shutdown waittime time */
    String DEFAULT_BLOCK_POLICY = "RUN";

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
    String ROLE = ThreadPool.class.getName();

    // ~ Methods
    // ----------------------------------------------------------------

    /**
     * Gets the block policy
     *
     * @return Returns the blockPolicy.
     */
    String getBlockPolicy();

    /**
     * Gets the maximum queue size
     *
     * @return maximum size of the queue (0 if isQueued() == false)
     */
    int getMaxQueueSize();

    /**
     * Gets the name of the thread pool
     *
     * @return name of the thread pool
     */
    String getName();

    /**
     * Gets the priority used to create Threads
     *
     * @return {@link Thread#MIN_PRIORITY}, {@link Thread#NORM_PRIORITY},
     *         or {@link Thread#MAX_PRIORITY}
     */
    int getPriority();

    /**
     * Gets the queue size of the thread pool
     *
     * @return current size of the queue (0 if isQueued() == false)
     */
    int getQueueSize();

    /**
     * Whether this DefaultThreadPool has a queue
     *
     * @return Returns the m_isQueued.
     */
    boolean isQueued();

    /**
     * Execute a command
     *
     * @param command
     *                The {@link Runnable} to execute
     *
     * @throws InterruptedException
     *                 In case of interruption
     */
    void execute(Runnable command) throws InterruptedException;

    /**
     * Returns true if shutdown is graceful
     *
     * @return Returns the shutdownGraceful.
     */
    boolean isShutdownGraceful();

    /**
     * Gets the shutdownWaitTime in milliseconds
     *
     * @return Returns the shutdownWaitTimeMs.
     */
    int getShutdownWaitTimeMs();

    /**
     * Gets the keepAliveTime in milliseconds
     *
     * @return the keepAliveTime
     */
    long getKeepAliveTime();

    /**
     * Gets the maximum pool size
     *
     * @return the maxPoolSize
     */
    int getMaxPoolSize();

    /**
     * Gets the minimum pool size
     *
     * @return the minPoolSize
     */
    int getMinPoolSize();

    /**
     * Returns true if thread runs as daemon
     *
     * @return the daemon
     */
    boolean isDaemon();

    /**
     * Returns true if a shutDown method has succeeded in terminating all
     * threads
     *
     * @return Whether a shutDown method has succeeded in terminating all
     *         threads
     */
    boolean isTerminatedAfterShutdown();

}
