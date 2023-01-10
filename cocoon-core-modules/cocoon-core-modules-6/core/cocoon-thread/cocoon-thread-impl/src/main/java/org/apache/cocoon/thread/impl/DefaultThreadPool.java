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

import org.apache.cocoon.thread.RunnableManager;
import org.apache.cocoon.thread.ThreadFactory;
import org.apache.cocoon.thread.ThreadPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * The DefaultThreadPool class implements the {@link ThreadPool} interface.
 * Instances of this class are made by the {@link RunnableManager} passing a
 * threadpool into the <code>init</code> method.
 *
 * <pre>
 *   &lt;!--+
 *       | More indepth information can be found at
 *       | http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/PooledExecutor.html
 *       | The following elements can be used:
 *       +--&gt;
 *   &lt;!-- required name of the pool --&gt;
 *   &lt;property name=&quot;name&quot; value=&quot;default&quot; /&gt;
 *   &lt;!--+
 *       | optional priority all threads of the pool will have (the ThreadFactory will be
 *       | set to this priority).The possible values  are:
 *       |    MIN:  corresponds to Thread#MIN_PRIORITY
 *       |    NORM: corresponds to Thread#NORM_PRIORITY (default)
 *       |    MAX:  corresponds to Thread#MAX_PRIORITY
 *       +--&gt;
 *   &lt;property name=&quot;poolPriority&quot; value=&quot;NORM&quot; /&gt;
 *   &lt;!--+
 *       | whether newly created Threads should run in daemon mode or not. Default to false.
 *       +--&gt;
 *   &lt;property name=&quot;daemon&quot; value=&quot;false&quot; /&gt;
 *   &lt;!--+
 *       | optional size of a queue to hold Runnables if the pool is full. Possible values are:
 *      |    less than 0:    unbounded (default)
 *       |    equal to 0:     no queue at all
 *       |    greater than 0: size of the queue
 *    --&gt;
 *   &lt;property name=&quot;queueSize&quot; value=&quot;-1&quot; /&gt;
 *   &lt;!--+
 *       | optional maximum number of threads in the pool. Defaults to 5.
 *       | NOTE: if a queue is specified (queue-sie != 0)
 *       |       this value will be ignored.
 *       +--&gt;
 *   &lt;property name=&quot;maxPoolSize&quot; value=&quot;5&quot; /&gt;
 *   &lt;!--+
 *       | optional minimum number of threads in the pool. Defaults to 5.
 *       | NOTE: if a queue has been specified (queue-sie != 0)
 *       |       this value will be used as the maximum of thread running concurrently.
 *       +--&gt;
 *   &lt;property name=&quot;minPoolSize&quot; value=&quot;5&quot; /&gt;
 *   &lt;!--+
 *       | The time in ms an idle thread should keep alive before it might get garbage collected.
 *       | This defaults to 60000 ms.
 *       +--&gt;
 *   &lt;property name=&quot;keepAliveTime&quot; value=&quot;60000&quot; /&gt;
 *   &lt;!--+
 *       | The policy to be used if all resources (thread in the pool and
 *       | slots in the queue) are exhausted.
 *       | Possible values are:
 *       |    ABORT:         Throw a RuntimeException
 *       |    DISCARD:       Throw away the current request and return.
 *       |    DISCARDOLDEST: Throw away the oldest request and return.
 *       |    RUN (default): The thread making the execute request runs the task itself.
 *       |                   This policy helps guard against lockup.
 *       |    WAIT:          Wait until a thread becomes available. This policy should, in
 *       |                   general, not be used if the minimum number of threads is zero,
 *       |                   in which case a thread may never become available.
 *       +--&gt;
 *   &lt;property name=&quot;blockPolicy&quot; value=&quot;ABORT&quot; /&gt;
 *   &lt;!--+
 *       | Terminate thread pool after processing all Runnables currently in queue. Any
 *       | Runnable entered after this point will be discarded. A shut down pool cannot
 *       | be restarted. This also means that a pool will need keep-alive-time-ms to
 *       | terminate. The default value not to shutdown graceful.
 *       +--&gt;
 *   &lt;property name=&quot;shutdownGraceful&quot; value=&quot;false&quot; /&gt;
 *   &lt;!--+
 *       | The time in ms to wait before issuing an immediate shutdown after a graceful shutdown
 *       | has been requested.
 *       +--&gt;
 *   &lt;property name=&quot;shutdownWaitTimeMs&quot; value=&quot;-1&quot; /&gt;
 *   &lt;!--+
 *       | specifies the fully qualified class name of an org.apache.cocoon.thread.ThreadFactory
 *       | implementation. It is responsible to create Thread classes.
 *       +--&gt;
 *   &lt;property name=&quot;factory&quot; ref=&quot;defaultThreadFactory&quot;/&gt;
 * </pre>
 *
 * @version $Id$
 */
public class DefaultThreadPool
    extends PooledExecutor
    implements ThreadPool {

    // ~ Static fields/initializers
    // ---------------------------------------------

    /** Default ThreadPool block policy */
    public static final String POLICY_DEFAULT = POLICY_RUN;

    /** By default we use the logger for this class. */
    protected final Log logger = LogFactory.getLog(getClass());

    // ~ Instance fields
    // --------------------------------------------------------

    /** The name of this thread pool */
    private String name = ThreadPool.DEFAULT_THREADPOOL_NAME;

    /** Is daemon thread pool */
    private boolean daemon = ThreadPool.DEFAULT_DAEMON_MODE;

    /** The priority of this thread pool */
    private int priority = convertPriority(ThreadPool.DEFAULT_THREAD_PRIORITY);

    /** The maximum queue size */
    private int queueSize = ThreadPool.DEFAULT_QUEUE_SIZE;

    /** The maximum pool size */
    private int maxPoolSize = ThreadPool.DEFAULT_MAX_POOL_SIZE;

    /** The minimum pool size */
    private int minPoolSize = ThreadPool.DEFAULT_MIN_POOL_SIZE;

    /** The keep alive time in milliseconds */
    private long keepAliveTime = ThreadPool.DEFAULT_KEEP_ALIVE_TIME;

    /** The blocking policy */
    private String blockPolicy = ThreadPool.DEFAULT_BLOCK_POLICY;

    /** Should we wait for running jobs to terminate on shutdown ? */
    private boolean shutdownGraceful = ThreadPool.DEFAULT_SHUTDOWN_GRACEFUL;

    /** How long to wait for running jobs to terminate on disposition */
    private int shutdownWaitTimeMs = ThreadPool.DEFAULT_SHUTDOWN_WAIT_TIME;

    /** A ThreadFactory implementation */
    private ThreadFactory factory;

    /** Wrapps a channel */
    private ChannelWrapper channelWrapper;

    /** The Queue */
    private Queue queue;

    // ~ Constructors
    // -----------------------------------------------------------

    /**
     * Create a new pool.
     *
     * @param channel
     *                DOCUMENT ME!
     */
    private DefaultThreadPool(final ChannelWrapper channel) {
   	    super(channel);
	    channelWrapper = channel;
    }

    /**
     * Create a new pool.
     */
    DefaultThreadPool() {
  	    this(new ChannelWrapper());
    }

    // ~ Methods
    // ----------------------------------------------------------------

    /** Initialize the bean after properties set */
    public void init() throws IllegalArgumentException {
        if (logger.isInfoEnabled()) {
            logger.info("ThreadPool [" + name + "] initializing ...");
        }

	    initFactory();
	    this.setThreadFactory(factory);
        initMinPoolSize();
        initPriority();
        initDaemon();
        initQueueSize();
        initMaxPoolSize();
        initKeepAliveTime();

        if (logger.isInfoEnabled()) {
            logger.info(this.toString());
            logger.info("ThreadPool [" + name + "] initialized");
        }
    }

    /**
     * Get the block policy
     *
     * @return Returns the blockPolicy.
     */
    public String getBlockPolicy() {
        return blockPolicy;
    }

    /**
     * DOCUMENT ME!
     *
     * @return maximum size of the queue (0 if isQueued() == false)
     *
     * @see org.apache.cocoon.thread.ThreadPool#getQueueSize()
     */
    public int getMaxQueueSize() {
        return ((queueSize < 0) ? Integer.MAX_VALUE : queueSize);
    }

    /**
         * @see org.apache.cocoon.thread.ThreadPool#getName()
         */
    public String getName() {
	return name;
    }

    /**
         * Get hte priority used to create Threads
         *
         * @return {@link Thread#MIN_PRIORITY}, {@link Thread#NORM_PRIORITY},
         *         or {@link Thread#MAX_PRIORITY}
         */
    public int getPriority() {
	return ((ThreadFactory) super.getThreadFactory()).getPriority();
    }

    /**
         * DOCUMENT ME!
         *
         * @return current size of the queue (0 if isQueued() == false)
         *
         * @see org.apache.cocoon.thread.ThreadPool#getQueueSize()
         */
    public int getQueueSize() {
	return queue.getQueueSize();
    }

    /**
         * Whether this DefaultThreadPool has a queue
         *
         * @return Returns the m_isQueued.
         *
         * @see org.apache.cocoon.thread.ThreadPool#isQueued()
         */
    public boolean isQueued() {
	return queueSize != 0;
    }

    /**
         * Execute a command
         *
         * @param command
         *                The {@link Runnable} to execute
         *
         * @throws InterruptedException
         *                 In case of interruption
         */
    public void execute(Runnable command) throws InterruptedException {
	if (logger.isDebugEnabled()) {
	    logger.debug("Executing Command: " + command.toString() + ",pool="
		    + getName());
	}

	super.execute(command);
    }

    /**
         * @see org.apache.cocoon.thread.ThreadPool#shutdown()
         */
    public void shutdown() {
	if (shutdownGraceful) {
	    shutdownAfterProcessingCurrentlyQueuedTasks();
	} else {
	    shutdownNow();
	}

	try {
	    if (getShutdownWaitTimeMs() > 0) {
		if (!awaitTerminationAfterShutdown(getShutdownWaitTimeMs())) {
		    logger.warn("running commands have not terminated within "
			    + getShutdownWaitTimeMs()
			    + "ms. Will shut them down by interruption");
		    interruptAll();
		    shutdownNow();
		}
	    }

	    awaitTerminationAfterShutdown();
	} catch (final InterruptedException ie) {
	    logger.error("cannot shutdown ThreadPool", ie);
	}
    }

    /**
         * Set the blocking policy
         *
         * @param blockPolicy
         *                The blocking policy value
         */
    public void setBlockPolicy(final String blockPolicy) {
	this.blockPolicy = blockPolicy;

	if (POLICY_ABORT.equalsIgnoreCase(blockPolicy)) {
	    abortWhenBlocked();
	} else if (POLICY_DISCARD.equalsIgnoreCase(blockPolicy)) {
	    discardWhenBlocked();
	} else if (POLICY_DISCARD_OLDEST.equalsIgnoreCase(blockPolicy)) {
	    discardOldestWhenBlocked();
	} else if (POLICY_RUN.equalsIgnoreCase(blockPolicy)) {
	    runWhenBlocked();
	} else if (POLICY_WAIT.equalsIgnoreCase(blockPolicy)) {
	    waitWhenBlocked();
	} else {
	    final StringBuffer msg = new StringBuffer();
	    msg.append("WARNING: Unknown block-policy configuration \"")
		    .append(blockPolicy);
	    msg.append("\". Should be one of \"").append(POLICY_ABORT);
	    msg.append("\",\"").append(POLICY_DISCARD);
	    msg.append("\",\"").append(POLICY_DISCARD_OLDEST);
	    msg.append("\",\"").append(POLICY_RUN);
	    msg.append("\",\"").append(POLICY_WAIT);
	    msg.append("\". Will use \"").append(POLICY_DEFAULT).append("\"");
	    logger.warn(msg.toString());
	    setBlockPolicy(POLICY_DEFAULT);
	}
    }

    /**
         * DOCUMENT ME!
         *
         * @param name
         *                The name to set.
         */
    public void setName(String name) {
	this.name = name;
    }

    /**
         * DOCUMENT ME!
         *
         * @param shutdownGraceful
         *                The shutdownGraceful to set.
         */
    public void setShutdownGraceful(boolean shutdownGraceful) {
	this.shutdownGraceful = shutdownGraceful;
    }

    /**
         * DOCUMENT ME!
         *
         * @return Returns the shutdownGraceful.
         */
    public boolean isShutdownGraceful() {
	return shutdownGraceful;
    }

    /**
         * DOCUMENT ME!
         *
         * @param shutdownWaitTimeMs
         *                The shutdownWaitTimeMs to set.
         */
    public void setShutdownWaitTimeMs(int shutdownWaitTimeMs) {
	this.shutdownWaitTimeMs = shutdownWaitTimeMs;
    }

    /**
         * DOCUMENT ME!
         *
         * @return Returns the shutdownWaitTimeMs.
         */
    public int getShutdownWaitTimeMs() {
	return shutdownWaitTimeMs;
    }

    /**
         * @return the keepAliveTime
         */
    public long getKeepAliveTime() {
	return keepAliveTime;
    }

    /**
         * @param keepAliveTime
         *                the keepAliveTime to set
         */
    public void setKeepAliveTime(long keepAliveTime) {
	this.keepAliveTime = keepAliveTime;
    }

    /**
         * @return the maxPoolSize
         */
    public int getMaxPoolSize() {
	return maxPoolSize;
    }

    /**
         * @param maxPoolSize
         *                the maxPoolSize to set
         */
    public void setMaxPoolSize(int maxPoolSize) {
	this.maxPoolSize = maxPoolSize;
    }

    /**
         * @return the minPoolSize
         */
    public int getMinPoolSize() {
	return minPoolSize;
    }

    /**
         * @param minPoolSize
         *                the minPoolSize to set
         */
    public void setMinPoolSize(int minPoolSize) {
	this.minPoolSize = minPoolSize;
    }

    /**
         * @return the threadFactory
         */
    public ThreadFactory getFactory() {
	return factory;
    }

    /**
         * @param priority
         *                the priority to set
         */
    public void setPriority(int priority) {
	this.priority = priority;
    }

    /**
         * @param pool
         *                priority the priority to set
         */
    public void setPoolPriority(String poolPriority) {
	setPriority(convertPriority(poolPriority));
    }

    /**
         * Sets the queue size of the thread pool
         *
         * @param queueSize
         *                the queueSize to set
         */
    public void setQueueSize(int queueSize) {
	this.queueSize = queueSize;
    }

    /**
         * Returns true if thread runs as daemon
         *
         * @return the daemon
         */
    public boolean isDaemon() {
	return daemon;
    }

    /**
         * Set to true if thread shall run as daemon
         *
         * @param daemon
         *                the daemon to set
         */
    public void setDaemon(boolean daemon) {
	this.daemon = daemon;
    }

    /**
         * Overwrite the toString method
         */
    public String toString() {
	if (this.isQueued()) {
	    final StringBuffer msg = new StringBuffer();
	    msg.append("ThreadPool named \"").append(name);
	    msg.append("\" created with maximum queue-size=").append(queueSize);
	    msg.append(",max-pool-size=").append(maximumPoolSize_);
	    msg.append(",min-pool-size=").append(minimumPoolSize_);
	    msg.append(",priority=").append(priority);
	    msg.append(",isDaemon=").append(
		    ((ThreadFactory) this.getThreadFactory()).isDaemon());
	    msg.append(",keep-alive-time-ms=").append(keepAliveTime_);
	    msg.append(",block-policy=\"").append(blockPolicy);
	    msg.append("\",shutdown-wait-time-ms=").append(shutdownWaitTimeMs);
	    return msg.toString();
	} else {
	    final StringBuffer msg = new StringBuffer();
	    msg.append("ThreadPool named \"").append(name);
	    msg.append("\" created with no queue,max-pool-size=").append(
		    maximumPoolSize_);
	    msg.append(",min-pool-size=").append(minimumPoolSize_);
	    msg.append(",priority=").append(priority);
	    msg.append(",isDaemon=").append(
		    ((ThreadFactory) this.getThreadFactory()).isDaemon());
	    msg.append(",keep-alive-time-ms=").append(keepAliveTime_);
	    msg.append(",block-policy=\"").append(blockPolicy);
	    msg.append("\",shutdown-wait-time-ms=").append(shutdownWaitTimeMs);
	    return msg.toString();
	}
    }

    /**
         * DOCUMENT ME!
         *
         * @param priority
         *                The priority to set as string value.
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
	    logger.warn("Unknown thread priority \"" + priority
		    + "\". Set to \"NORM\".");

	    return Thread.NORM_PRIORITY;
	}
    }

    /**
         * @param factory
         *                the factory to set
         */
    public void setFactory(ThreadFactory factory) {
	this.factory = factory;
    }

    private void initFactory() {
	if (factory == null) {
	    logger.warn("No ThreadFactory is configured. Will use a "
		    + DefaultThreadFactory.class.getName());
	    factory = new org.apache.cocoon.thread.impl.DefaultThreadFactory();
	}
    }

    private void initMinPoolSize() {
	// Min pool size
	// make sure we have enough threads for the default thread pool as we
	// need one for ourself
	if (ThreadPool.DEFAULT_THREADPOOL_NAME.equals(name)
		&& ((minPoolSize > 0) && (minPoolSize < ThreadPool.DEFAULT_MIN_POOL_SIZE))) {
	    minPoolSize = ThreadPool.DEFAULT_MIN_POOL_SIZE;
	}
	if (minPoolSize < 1) {
	    minPoolSize = (minPoolSize < 1) ? 1 : minPoolSize;
	    logger
		    .warn("min-pool-size < 1 for pool \"" + name
			    + "\". Set to 1");
	}
	super.setMinimumPoolSize(minPoolSize);
    }

    private void initPriority() {
	// Use priority from factory when changed
	priority = (factory.getPriority() != Thread.NORM_PRIORITY ? factory
		.getPriority() : priority);
	factory.setPriority(priority);
    }

    private void initDaemon() {
	// Use daemon from factory when changed
	daemon = (factory.isDaemon() != false ? factory.isDaemon() : daemon);
	factory.setDaemon(daemon);
    }

    private void initMaxPoolSize() {
	// Max pool size
	maxPoolSize = (maxPoolSize < 0) ? Integer.MAX_VALUE : maxPoolSize;
	super.setMaximumPoolSize(maxPoolSize);
    }

    private void initKeepAliveTime() {
	// Keep alive time
	if (keepAliveTime < 0) {
	    keepAliveTime = 1000;
	    logger.warn("keep-alive-time-ms < 0 for pool \"" + name
		    + "\". Set to 1000");
	}
	super.setKeepAliveTime(keepAliveTime);
    }

    private void initQueueSize() {
	if (queueSize != 0) {
	    if (queueSize > 0) {
		queue = new BoundedQueue(queueSize);
	    } else {
		queue = new LinkedQueue();
	    }
	} else {
	    queue = new SynchronousChannel();
	}
	channelWrapper.setChannel(queue);
    }
}
