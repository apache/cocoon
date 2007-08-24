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
package org.apache.cocoon.thread.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.cocoon.thread.RunnableManager;
import org.apache.cocoon.thread.ThreadPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The DefaultRunnableManager implements the {@link RunnableManager} interface
 * and is responsible to create {@link ThreadPool}s and run {@link Runnable}s
 * in them as background commands.
 *
 * <p>
 * The configuration of the <code>DefaultRunnableManager</code>:
 *
 * <pre>
 *     &lt;property name=&quot;workerThreadPools&quot;&gt;
 *       &lt;configurator:bean-map type=&quot;org.apache.cocoon.thread.ThreadPool&quot; strip-prefix=&quot;true&quot;/&gt;
 *     &lt;/property&gt;
 * </pre>
 *
 * </p>
 *
 * @version $Id: DefaultRunnableManager.java 498489 2007-01-21 23:19:09Z
 *          jjohnston $
 */
public class DefaultRunnableManager implements RunnableManager, Runnable {

    // ~ Static fields/initializers
    // ---------------------------------------------

    /** By default we use the logger for this class. */
    private Log logger = LogFactory.getLog(getClass());

    // ~ Instance fields
    // --------------------------------------------------------

    /**
     * Sorted set of <code>ExecutionInfo</code> instances, based on their next
     * execution time.
     */
    protected SortedSet commandStack = new TreeSet();

    /** The managed thread pools */
    final Map pools = new HashMap();

    /** Keep us running? */
    private boolean keepRunning = false;

    /** Map of the configured worker thread pools */
    private Map workerThreadPools;

    // ~ Methods
    // ----------------------------------------------------------------

    public Log getLogger() {
        return this.logger;
    }

    public void setLogger(Log l) {
        this.logger = l;
    }

    /**
     * Initialize
     */
    public void init() throws Exception {
        if (workerThreadPools != null) {
            final Iterator iter = workerThreadPools.keySet().iterator();
            while (iter.hasNext()) {
                final String key = (String) iter.next();
                final ThreadPool pool = (ThreadPool) workerThreadPools.get(key);
                synchronized (pools) {
                    pools.put(pool.getName(), pool);
                }
            }
        }

        // Check if a "default" pool has been created
        final ThreadPool defaultThreadPool = (ThreadPool) pools.get(ThreadPool.DEFAULT_THREADPOOL_NAME);

        if (null == defaultThreadPool) {
            createPool(ThreadPool.DEFAULT_THREADPOOL_NAME, ThreadPool.DEFAULT_QUEUE_SIZE,
                            ThreadPool.DEFAULT_MAX_POOL_SIZE, ThreadPool.DEFAULT_MIN_POOL_SIZE,
                            convertPriority(ThreadPool.DEFAULT_THREAD_PRIORITY), ThreadPool.DEFAULT_DAEMON_MODE,
                            ThreadPool.DEFAULT_KEEP_ALIVE_TIME, ThreadPool.DEFAULT_BLOCK_POLICY,
                            ThreadPool.DEFAULT_SHUTDOWN_GRACEFUL, ThreadPool.DEFAULT_SHUTDOWN_WAIT_TIME);
        }
        // now start
        this.start();
    }

    /**
     * Create a shared ThreadPool
     *
     * @param name
     *            The name of the thread pool
     * @param queueSize
     *            The size of the queue
     * @param maxPoolSize
     *            The maximum number of threads
     * @param minPoolSize
     *            The maximum number of threads
     * @param priority
     *            The priority of threads created by this pool. This is one of
     *            {@link Thread#MIN_PRIORITY}, {@link Thread#NORM_PRIORITY},
     *            or {@link Thread#MAX_PRIORITY}
     * @param isDaemon
     *            Whether or not thread from the pool should run in daemon mode
     * @param keepAliveTime
     *            How long should a thread be alive for new work to be done
     *            before it is GCed
     * @param blockPolicy
     *            What's the blocking policy is resources are exhausted
     * @param shutdownGraceful
     *            Should we wait for the queue to finish all pending commands?
     * @param shutdownWaitTime
     *            After what time a normal shutdown should take into account if
     *            a graceful shutdown has not come to an end
     *
     * @throws IllegalArgumentException
     *             If the pool already exists
     */
    public ThreadPool createPool(final String name, final int queueSize, final int maxPoolSize, final int minPoolSize,
                    final int priority, final boolean isDaemon, final long keepAliveTime, final String blockPolicy,
                    final boolean shutdownGraceful, final int shutdownWaitTimeMs) {
        if (null != pools.get(name)) {
            throw new IllegalArgumentException("ThreadPool \"" + name + "\" already exists");
        }

        final DefaultThreadPool pool = new DefaultThreadPool();
        pool.setName(name);
        pool.setQueueSize(queueSize);
        pool.setMaxPoolSize(maxPoolSize);
        pool.setMinPoolSize(minPoolSize);
        pool.setPriority(priority);
        pool.setDaemon(isDaemon);
        pool.setBlockPolicy(blockPolicy);
        pool.setShutdownGraceful(shutdownGraceful);
        pool.setShutdownWaitTimeMs(shutdownWaitTimeMs);
        synchronized (pools) {
            pools.put(pool.getName(), pool);
        }
        return pool;
    }

    /**
     * Create a private ThreadPool
     *
     * @param queueSize
     *            The size of the queue
     * @param maxPoolSize
     *            The maximum number of threads
     * @param minPoolSize
     *            The maximum number of threads
     * @param priority
     *            The priority of threads created by this pool. This is one of
     *            {@link Thread#MIN_PRIORITY}, {@link Thread#NORM_PRIORITY},
     *            or {@link Thread#MAX_PRIORITY}
     * @param isDaemon
     *            Whether or not thread from the pool should run in daemon mode
     * @param keepAliveTime
     *            How long should a thread be alive for new work to be done
     *            before it is GCed
     * @param blockPolicy
     *            What's the blocking policy is resources are exhausted
     * @param shutdownGraceful
     *            Should we wait for the queue to finish all pending commands?
     * @param shutdownWaitTime
     *            After what time a normal shutdown should take into account if
     *            a graceful shutdown has not come to an end
     *
     * @return A newly created <code>ThreadPool</code>
     */
    public ThreadPool createPool(final int queueSize, final int maxPoolSize, final int minPoolSize, final int priority,
                    final boolean isDaemon, final long keepAliveTime, final String blockPolicy,
                    final boolean shutdownGraceful, final int shutdownWaitTime) {
        final DefaultThreadPool pool = new DefaultThreadPool();
        final String name = "anon-" + pool.hashCode();
        pool.setName(name);
        pool.setQueueSize(queueSize);
        pool.setMaxPoolSize(maxPoolSize);
        pool.setMinPoolSize(minPoolSize);
        pool.setPriority(priority);
        pool.setDaemon(isDaemon);
        pool.setKeepAliveTime(keepAliveTime);
        pool.setBlockPolicy(blockPolicy);
        pool.setShutdownGraceful(shutdownGraceful);
        synchronized (pools) {
            pools.put(pool.getName(), pool);
        }

        return pool;
    }

    /**
     * @see org.apache.cocoon.thread.RunnableManager#getPool(java.lang.String)
     */
    public ThreadPool getPool(String name) {
        if ( name == null ) {
            name = ThreadPool.DEFAULT_THREADPOOL_NAME;
        }
        synchronized (pools) {
            return (ThreadPool)pools.get(name);
        }
    }

    /**
     * Destroy
     */
    public void destroy() throws Exception {
        this.stop();
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Disposing all thread pools");
        }

        for (final Iterator i = pools.keySet().iterator(); i.hasNext();) {
            final String poolName = (String) i.next();
            final DefaultThreadPool pool = (DefaultThreadPool) pools.get(poolName);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Disposing thread pool " + pool.getName());
            }

            pool.shutdown();

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Thread pool " + pool.getName() + " disposed");
            }
        }

        try {
            pools.clear();
        } catch (final Throwable t) {
            getLogger().error("Cannot dispose", t);
        }
    }

    /**
     * Run a {@link Runnable} in the background using a {@link ThreadPool}
     *
     * @param threadPoolName
     *            The thread pool name to be used
     * @param command
     *            The {@link Runnable} to execute
     * @param delay
     *            the delay befor first run
     * @param interval
     *            The interval for repeated runs
     *
     * @throws IllegalArgumentException
     *             DOCUMENT ME!
     */
    public void execute(final String threadPoolName, final Runnable command, final long delay, long interval) {
        if (delay < 0) {
            throw new IllegalArgumentException("delay < 0");
        }

        if (interval < 0) {
            throw new IllegalArgumentException("interval < 0");
        }

        ThreadPool pool = (ThreadPool) pools.get(threadPoolName);

        if (null == pool) {
            getLogger().warn(
                            "ThreadPool \"" + threadPoolName + "\" is not known. Will use ThreadPool \""
                                            + ThreadPool.DEFAULT_THREADPOOL_NAME + "\"");
            pool = (ThreadPool) pools.get(ThreadPool.DEFAULT_THREADPOOL_NAME);
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug(
                            "Command entered: " + command + ", pool=" + (null == pool ? "null" : pool.getName())
                                            + ", delay=" + delay + ", interval=" + interval);
        }

        new ExecutionInfo(pool, command, delay, interval, getLogger());
    }

    /**
     * Run a {@link Runnable} in the background using a {@link ThreadPool}
     *
     * @param command
     *            The {@link Runnable} to execute
     * @param delay
     *            the delay befor first run
     * @param interval
     *            The interval for repeated runs
     */
    public void execute(final Runnable command, final long delay, final long interval) {
        execute(ThreadPool.DEFAULT_THREADPOOL_NAME, command, delay, interval);
    }

    /**
     * Run a {@link Runnable} in the background using a {@link ThreadPool}
     *
     * @param command
     *            The {@link Runnable} to execute
     * @param delay
     *            the delay befor first run
     */
    public void execute(final Runnable command, final long delay) {
        execute(ThreadPool.DEFAULT_THREADPOOL_NAME, command, delay, 0);
    }

    /**
     * Run a {@link Runnable} in the background using a {@link ThreadPool}
     *
     * @param command
     *            The {@link Runnable} to execute
     */
    public void execute(final Runnable command) {
        execute(ThreadPool.DEFAULT_THREADPOOL_NAME, command, 0, 0);
    }

    /**
     * Run a {@link Runnable} in the background using a {@link ThreadPool}
     *
     * @param threadPoolName
     *            The thread pool name to be used
     * @param command
     *            The {@link Runnable} to execute
     * @param delay
     *            the delay befor first run
     */
    public void execute(final String threadPoolName, final Runnable command, final long delay) {
        execute(threadPoolName, command, delay, 0);
    }

    /**
     * Run a {@link Runnable} in the background using a {@link ThreadPool}
     *
     * @param threadPoolName
     *            The thread pool name to be used
     * @param command
     *            The {@link Runnable} to execute
     */
    public void execute(final String threadPoolName, final Runnable command) {
        execute(threadPoolName, command, 0, 0);
    }

    /**
     * Remove a <code>Runnable</code> from the command stack
     *
     * @param command
     *            The <code>Runnable</code> to be removed
     */
    public void remove(Runnable command) {
        synchronized (commandStack) {
            for (final Iterator i = commandStack.iterator(); i.hasNext();) {
                final ExecutionInfo info = (ExecutionInfo) i.next();

                if (info.m_command == command) {
                    i.remove();
                    commandStack.notifyAll();

                    return;
                }
            }
        }

        getLogger().warn("Could not find command " + command + " for removal");
    }

    /**
     * The heart of the command manager
     */
    public void run() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Entering loop");
        }

        while (keepRunning) {
            synchronized (commandStack) {
                try {
                    if (commandStack.size() > 0) {
                        final ExecutionInfo info = (ExecutionInfo) commandStack.first();
                        final long delay = info.m_nextRun - System.currentTimeMillis();

                        if (delay > 0) {
                            commandStack.wait(delay);
                        }
                    } else {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("No commands available. Will just wait for one");
                        }

                        commandStack.wait();
                    }
                } catch (final InterruptedException ie) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("I've been interrupted");
                    }
                }

                if (keepRunning) {
                    if (commandStack.size() > 0) {
                        final ExecutionInfo info = (ExecutionInfo) commandStack.first();
                        final long delay = info.m_nextRun - System.currentTimeMillis();

                        if (delay < 0) {
                            info.execute();
                        }
                    }
                }
            }
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Exiting loop");
        }
    }

    /**
     * Start the managing thread
     *
     * @throws Exception
     *             DOCUMENT ME!
     */
    protected void start() throws Exception {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Starting the heart");
        }

        keepRunning = true;
        ((ThreadPool) pools.get(ThreadPool.DEFAULT_THREADPOOL_NAME)).execute(this);
    }

    /**
     * Stop the managing thread
     */
    protected void stop() {
        keepRunning = false;

        synchronized (commandStack) {
            commandStack.notifyAll();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param priority
     *            The priority to set as string value.
     *
     * @return The priority as int value.
     */
    private int convertPriority(final String priority) {
        if ("MIN".equalsIgnoreCase(priority)) {
            return Thread.MIN_PRIORITY;
        } else if ("NORM".equalsIgnoreCase(priority)) {
            return Thread.NORM_PRIORITY;
        } else if ("MAX".equalsIgnoreCase(priority)) {
            return Thread.MAX_PRIORITY;
        } else {
            getLogger().warn("Unknown thread priority \"" + priority + "\". Set to \"NORM\".");

            return Thread.NORM_PRIORITY;
        }
    }

    // ~ Inner Classes
    // ----------------------------------------------------------

    /**
     * The $classType$ class ...
     *
     * @version $Id: DefaultRunnableManager.java 498489 2007-01-21 23:19:09Z
     *          jjohnston $
     */
    private class ExecutionInfo implements Comparable {
        // ~ Instance fields
        // ----------------------------------------------------

        /** Our logger */
        final Log m_logger;

        /** DOCUMENT ME! */
        final Runnable m_command;

        /** DOCUMENT ME! */
        final ThreadPool m_pool;

        /** DOCUMENT ME! */
        final long m_delay;

        /** DOCUMENT ME! */
        final long m_interval;

        /** DOCUMENT ME! */
        long m_nextRun = 0;

        // ~ Constructors
        // -------------------------------------------------------

        /**
         * Creates a new ExecutionInfo object.
         *
         * @param pool
         *            DOCUMENT ME!
         * @param command
         *            DOCUMENT ME!
         * @param delay
         *            DOCUMENT ME!
         * @param interval
         *            DOCUMENT ME!
         * @param logger
         *            DOCUMENT ME!
         */
        ExecutionInfo(final ThreadPool pool, final Runnable command, final long delay, final long interval,
                        final Log logger) {
            m_pool = pool;
            m_command = command;
            m_delay = delay;
            m_interval = interval;
            m_logger = logger;
            m_nextRun = System.currentTimeMillis() + delay;

            synchronized (commandStack) {
                commandStack.add(this);
                commandStack.notifyAll();
            }
            Thread.yield(); // Give others a chance to run
        }

        // ~ Methods
        // ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param other
         *            DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public int compareTo(final Object other) {
            final ExecutionInfo otherInfo = (ExecutionInfo) other;
            int diff = (int) (m_nextRun - otherInfo.m_nextRun);
            if (diff == 0) {
                if (this == other) {
                    // Same object, return 0.
                    return 0;
                } else {
                    // NOT the same object, MUST return non-0 value.
                    return System.identityHashCode(this) - System.identityHashCode(other);
                }
            }
            return diff;
        }

        /**
         * DOCUMENT ME!
         */
        void execute() {
            if (m_logger.isDebugEnabled()) {
                m_logger.debug("Executing command " + m_command + " in pool \"" + m_pool.getName()
                                + "\", schedule with interval=" + m_interval);
            }

            synchronized (commandStack) {
                commandStack.remove(this);
                if (m_interval > 0) {
                    m_nextRun = System.currentTimeMillis() + m_interval;
                    commandStack.add(this);
                }
            }

            try {
                m_pool.execute(m_command);
            } catch (final InterruptedException ie) {
                if (m_logger.isDebugEnabled()) {
                    m_logger.debug("Interrupted executing command + " + m_command);
                }
            } catch (final Throwable t) {
                m_logger.error("Exception executing command " + m_command, t);
            }
        }
    }

    /**
     * @param workerThreadPools
     *            the workerThreadPools to set
     */
    public void setWorkerThreadPools(Map workerThreadPools) {
        this.workerThreadPools = workerThreadPools;
    }
}
