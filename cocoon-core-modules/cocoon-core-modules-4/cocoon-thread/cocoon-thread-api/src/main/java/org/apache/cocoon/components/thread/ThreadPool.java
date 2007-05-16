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
 * The ThreadPool interface gives access to methods needed to inspect and use of
 * a pool of threads
 * 
 * @version $Id$
 */
public interface ThreadPool {

    // ~ Instance fields
    // --------------------------------------------------------

    /** The default queue size */
    public static int DEFAULT_QUEUE_SIZE = -1;

    /** The default maximum pool size */
    public static int DEFAULT_MAX_POOL_SIZE = 5;

    /** The default minimum pool size */
    public static int DEFAULT_MIN_POOL_SIZE = 5;

    /** The default thread priority */
    public static String DEFAULT_THREAD_PRIORITY = "NORM";

    /** The default daemon mode */
    public static boolean DEFAULT_DAEMON_MODE = false;

    /** The default keep alive time */
    public static long DEFAULT_KEEP_ALIVE_TIME = 60000L;

    /** The default way to shutdown gracefully */
    public static boolean DEFAULT_SHUTDOWN_GRACEFUL = false;

    /** The default shutdown waittime time */
    public static int DEFAULT_SHUTDOWN_WAIT_TIME = -1;

    /** The default shutdown waittime time */
    public static String DEFAULT_THREADPOOL_NAME = "default";

    /** The default shutdown waittime time */
    public static String DEFAULT_BLOCK_POLICY = "RUN";

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
    public String getBlockPolicy();

    /**
         * Gets the maximum queue size
         * 
         * @return maximum size of the queue (0 if isQueued() == false)
         */
    public int getMaxQueueSize();

    /**
         * Gets the name of the thread pool
         * 
         * @return name of the thread pool
         */
    public String getName();

    /**
         * Gets the priority used to create Threads
         * 
         * @return {@link Thread#MIN_PRIORITY}, {@link Thread#NORM_PRIORITY},
         *         or {@link Thread#MAX_PRIORITY}
         */
    public int getPriority();

    /**
         * Gets the queue size of the thread pool
         * 
         * @return current size of the queue (0 if isQueued() == false)
         */
    public int getQueueSize();

    /**
         * Whether this DefaultThreadPool has a queue
         * 
         * @return Returns the m_isQueued.
         */
    public boolean isQueued();

    /**
         * Execute a command
         * 
         * @param command
         *                The {@link Runnable} to execute
         * 
         * @throws InterruptedException
         *                 In case of interruption
         */
    public void execute(Runnable command) throws InterruptedException;

    /**
         * Set the blocking policy
         * 
         * @param blockPolicy
         *                The blocking policy value
         */
    void setBlockPolicy(String blockPolicy);

    /**
         * DOCUMENT ME!
         * 
         * @param name
         *                The name to set.
         */
    void setName(String name);

    /**
         * Sets the shutdownGraceful (true if graceful)
         * 
         * @param shutdownGraceful
         *                The shutdownGraceful to set.
         */
    void setShutdownGraceful(boolean shutdownGraceful);

    /**
         * Returns true if shutdown is graceful
         * 
         * @return Returns the shutdownGraceful.
         */
    public boolean isShutdownGraceful();

    /**
         * Sets the shutdownWaitTime in milliseconds
         * 
         * @param shutdownWaitTimeMs
         *                The shutdownWaitTimeMs to set.
         */
    void setShutdownWaitTimeMs(int shutdownWaitTimeMs);

    /**
         * Gets the shutdownWaitTime in milliseconds
         * 
         * @return Returns the shutdownWaitTimeMs.
         */
    public int getShutdownWaitTimeMs();

    /**
         * Gets the keepAliveTime in milliseconds
         * 
         * @return the keepAliveTime
         */
    public long getKeepAliveTime();

    /**
         * Sets the keepAliveTime in milliseconds
         * 
         * @param keepAliveTime
         *                the keepAliveTime to set
         */
    public void setKeepAliveTime(long keepAliveTime);

    /**
         * Gets the maximum pool size
         * 
         * @return the maxPoolSize
         */
    public int getMaxPoolSize();

    /**
         * Sets the maximum pool size
         * 
         * @param maxPoolSize
         *                the maxPoolSize to set
         */
    public void setMaxPoolSize(int maxPoolSize);

    /**
         * Gets the minimum pool size
         * 
         * @return the minPoolSize
         */
    public int getMinPoolSize();

    /**
         * Sets the minimum pool size
         * 
         * @param minPoolSize
         *                the minPoolSize to set
         */
    public void setMinPoolSize(int minPoolSize);

    /**
         * Sets the priority of this thread pool: {@link Thread#MIN_PRIORITY},
         * {@link Thread#NORM_PRIORITY}, or {@link Thread#MAX_PRIORITY}
         * 
         * @param priority
         *                the priority to set
         */
    public void setPriority(int priority);

    /**
         * Sets the queue size of the thread pool
         * 
         * @param queueSize
         *                the queueSize to set
         */
    public void setQueueSize(int queueSize);

    /**
         * Terminates all threads possibly awaiting processing all elements
         * currently in queue.
         */
    void shutdown();

    /**
         * Returns true if thread runs as daemon
         * 
         * @return the daemon
         */
    public boolean isDaemon();

    /**
         * Set to true if thread shall run as daemon
         * 
         * @param daemon
         *                the daemon to set
         */
    public void setDaemon(boolean daemon);

    /**
         * Returns true if a shutDown method has succeeded in terminating all
         * threads
         * 
         * @return Whether a shutDown method has succeeded in terminating all
         *         threads
         */
    public boolean isTerminatedAfterShutdown();

}
