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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The DefaultRunnableManager implements the {@link RunnableManager} interface
 * and is responsible to create {@link ThreadPool}s and run {@link Runnable}s
 * in them as background commands.
 *
 * <p>
 * The configuration of the <code>DefaultRunnableManager</code>:
 * <pre>
 *   &lt;thread-factory&gt;org.apache.cocoon.components.thread.DefaultThreadFactory&lt;/thread-factory&gt;
 *   &lt;thread-pools&gt;
 *     &lt;thread-pool&gt;
 *       &lt;name&gt;default&lt;/name&gt;
 *       &lt;priority&gt;NORM&lt;/priority&gt;
 *       &lt;daemon&gt;false&lt;/daemon&gt;
 *       &lt;queue-size&gt;-1&lt;/queue-size&gt;
 *       &lt;max-pool-size&gt;-1&lt;/max-pool-size&gt;
 *       &lt;min-pool-size&gt;2&lt;/min-pool-size&gt;
 *       &lt;keep-alive-time-ms&gt;20000&lt;/keep-alive-time-ms&gt;
 *       &lt;block-policy&gt;RUN&lt;/block-policy&gt;
 *       &lt;shutdown-graceful&gt;false&lt;/shutdown-graceful&gt;
 *       &lt;shutdown-wait-time-ms&gt;-1&lt;/shutdown-wait-time-ms&gt;
 *     &lt;/thread-pool&gt;
 *   &lt;/thread-pools&gt;
 * </pre>
 * </p>
 *
 * <p>
 * Have a look at
 * http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/PooledExecutor.html,
 * {@link EDU.oswego.cs.dl.util.concurrent.PooledExecutor} or the cocoon.xconf
 * file for more information.
 * </p>
 *
 * @version $Id$
 */
public class DefaultRunnableManager
    implements RunnableManager, Runnable {
    
    /** By default we use the logger for this class. */
    private Log logger = LogFactory.getLog(getClass());

    //~ Static fields/initializers ---------------------------------------------

    /** The default {@link ThreadFactory} */
    public static final String DEFAULT_THREAD_FACTORY =
        DefaultThreadFactory.class.getName();

    /** The default queue size */
    public static final int DEFAULT_QUEUE_SIZE = -1;

    /** The default maximum pool size */
    public static final int DEFAULT_MAX_POOL_SIZE = 5;

    /** The default minimum pool size */
    public static final int DEFAULT_MIN_POOL_SIZE = 5;

    /** The default thread priority */
    public static final String DEFAULT_THREAD_PRIORITY = "NORM";

    /** The default daemon mode */
    public static final boolean DEFAULT_DAEMON_MODE = false;

    /** The default keep alive time */
    public static final long DEFAULT_KEEP_ALIVE_TIME = 60000L;

    /** The default way to shutdown gracefully */
    public static final boolean DEFAULT_SHUTDOWN_GRACEFUL = false;

    /** The default shutdown waittime time */
    public static final int DEFAULT_SHUTDOWN_WAIT_TIME = -1;

    /** The default shutdown waittime time */
    public static final String DEFAULT_THREADPOOL_NAME = "default";

    //~ Instance fields --------------------------------------------------------

    /**
     * Sorted set of <code>ExecutionInfo</code> instances, based on their next
     * execution time.
     */
    protected SortedSet commandStack = new TreeSet();

    /** The managed thread pools */
    final Map pools = new HashMap();

    /** The configured default ThreadFactory class instance */
    private Class defaultThreadFactoryClass;

    /** Keep us running? */
    private boolean keepRunning = false;

    private String threadFactory = DEFAULT_THREAD_FACTORY;

    private List threadPools;
    //~ Methods ----------------------------------------------------------------

    public Log getLogger() {
        return this.logger;
    }

    public void setLogger(Log l) {
        this.logger = l;
    }

    public void setThreadFactory(String threadFactory) {
        this.threadFactory = threadFactory;
    }

    public void setThreadPools(List threadPools) {
        this.threadPools = threadPools;
    }

    /**
     * Initialize
     */
    public void init() throws Exception {
        try {
            defaultThreadFactoryClass =
                Thread.currentThread().getContextClassLoader().loadClass( this.threadFactory );
        } catch( final Exception ex ) {
            throw new Exception( "Cannot create instance of default thread factory " +
                    this.threadFactory, ex );
        }

        if ( this.threadPools != null ) {
            for( int i = 0; i < this.threadPools.size(); i++ ) {
                configThreadPool( (Map)this.threadPools.get(i) );
            }            
        }

        // Check if a "default" pool has been created
        final ThreadPool defaultThreadPool =
            (ThreadPool)pools.get( DEFAULT_THREADPOOL_NAME );

        if( null == defaultThreadPool ) {
            createPool( DEFAULT_THREADPOOL_NAME, DEFAULT_QUEUE_SIZE,
                        DEFAULT_MAX_POOL_SIZE, DEFAULT_MIN_POOL_SIZE,
                        getPriority( DEFAULT_THREAD_PRIORITY ),
                        DEFAULT_DAEMON_MODE, DEFAULT_KEEP_ALIVE_TIME,
                        DefaultThreadPool.POLICY_DEFAULT,
                        DEFAULT_SHUTDOWN_GRACEFUL, DEFAULT_SHUTDOWN_WAIT_TIME );
        }
        // now start
        this.start();
    }

    /**
     * Create a shared ThreadPool
     *
     * @param name The name of the thread pool
     * @param queueSize The size of the queue
     * @param maxPoolSize The maximum number of threads
     * @param minPoolSize The maximum number of threads
     * @param priority The priority of threads created by this pool. This is
     *        one of {@link Thread#MIN_PRIORITY}, {@link
     *        Thread#NORM_PRIORITY}, or {@link Thread#MAX_PRIORITY}
     * @param isDaemon Whether or not thread from the pool should run in daemon
     *        mode
     * @param keepAliveTime How long should a thread be alive for new work to
     *        be done before it is GCed
     * @param blockPolicy What's the blocking policy is resources are exhausted
     * @param shutdownGraceful Should we wait for the queue to finish all
     *        pending commands?
     * @param shutdownWaitTime After what time a normal shutdown should take
     *        into account if a graceful shutdown has not come to an end
     *
     * @throws IllegalArgumentException If the pool already exists
     */
    public void createPool( final String name,
                            final int queueSize,
                            final int maxPoolSize,
                            final int minPoolSize,
                            final int priority,
                            final boolean isDaemon,
                            final long keepAliveTime,
                            final String blockPolicy,
                            final boolean shutdownGraceful,
                            final int shutdownWaitTime ) {
        if( null != pools.get( name ) ) {
            throw new IllegalArgumentException( "ThreadPool \"" + name +
                                                "\" already exists" );
        }

        createPool( new DefaultThreadPool(  ), name, queueSize, maxPoolSize,
                    minPoolSize, priority, isDaemon, keepAliveTime,
                    blockPolicy, shutdownGraceful, shutdownWaitTime );
    }

    /**
     * Create a private ThreadPool
     *
     * @param queueSize The size of the queue
     * @param maxPoolSize The maximum number of threads
     * @param minPoolSize The maximum number of threads
     * @param priority The priority of threads created by this pool. This is
     *        one of {@link Thread#MIN_PRIORITY}, {@link
     *        Thread#NORM_PRIORITY}, or {@link Thread#MAX_PRIORITY}
     * @param isDaemon Whether or not thread from the pool should run in daemon
     *        mode
     * @param keepAliveTime How long should a thread be alive for new work to
     *        be done before it is GCed
     * @param blockPolicy What's the blocking policy is resources are exhausted
     * @param shutdownGraceful Should we wait for the queue to finish all
     *        pending commands?
     * @param shutdownWaitTime After what time a normal shutdown should take
     *        into account if a graceful shutdown has not come to an end
     *
     * @return A newly created <code>ThreadPool</code>
     */
    public ThreadPool createPool( final int queueSize,
                                  final int maxPoolSize,
                                  final int minPoolSize,
                                  final int priority,
                                  final boolean isDaemon,
                                  final long keepAliveTime,
                                  final String blockPolicy,
                                  final boolean shutdownGraceful,
                                  final int shutdownWaitTime ) {
        final DefaultThreadPool pool = new DefaultThreadPool();
        final String name = "anon-" + pool.hashCode(  );

        return createPool( pool, name, queueSize, maxPoolSize, minPoolSize,
                           priority, isDaemon, keepAliveTime, blockPolicy,
                           shutdownGraceful, shutdownWaitTime );
    }

    /**
     * Destroy
     */
    public void destroy() throws Exception {
        this.stop();
        if( getLogger().isDebugEnabled() ) {
            getLogger().debug( "Disposing all thread pools" );
        }

        for( final Iterator i = pools.keySet().iterator(); i.hasNext(); ) {
            final String poolName = (String)i.next();
            final DefaultThreadPool pool =
                (DefaultThreadPool)pools.get( poolName );

            if( getLogger().isDebugEnabled(  ) ) {
                getLogger().debug( "Disposing thread pool " +
                                     pool.getName() );
            }

            pool.shutdown();

            if( getLogger().isDebugEnabled(  ) ) {
                getLogger().debug( "Thread pool " + pool.getName() +
                                     " disposed" );
            }
        }

        try {
            pools.clear();
        } catch( final Throwable t ) {
            getLogger().error( "Cannot dispose", t );
        }
    }

    /**
     * Run a {@link Runnable} in the background using a {@link ThreadPool}
     *
     * @param threadPoolName The thread pool name to be used
     * @param command The {@link Runnable} to execute
     * @param delay the delay befor first run
     * @param interval The interval for repeated runs
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public void execute( final String threadPoolName,
                         final Runnable command,
                         final long delay,
                         long interval ) {
        if( delay < 0 ) {
            throw new IllegalArgumentException( "delay < 0" );
        }

        if( interval < 0 ) {
            throw new IllegalArgumentException( "interval < 0" );
        }

        ThreadPool pool = (ThreadPool)pools.get( threadPoolName );

        if( null == pool ) {
            getLogger().warn( "ThreadPool \"" + threadPoolName +
                              "\" is not known. Will use ThreadPool \"" +
                              DEFAULT_THREADPOOL_NAME + "\"" );
            pool = (ThreadPool)pools.get( DEFAULT_THREADPOOL_NAME );
        }

        if( getLogger().isDebugEnabled() ) {
            getLogger().debug( "Command entered: " + command +
                               ", pool=" + (null == pool ? "null" : pool.getName()) + ", delay=" +
                               delay + ", interval=" + interval );
        }

        new ExecutionInfo( pool, command, delay, interval, getLogger() );
    }

    /**
     * Run a {@link Runnable} in the background using a {@link ThreadPool}
     *
     * @param command The {@link Runnable} to execute
     * @param delay the delay befor first run
     * @param interval The interval for repeated runs
     */
    public void execute( final Runnable command,
                         final long delay,
                         final long interval ) {
        execute( DEFAULT_THREADPOOL_NAME, command, delay, interval );
    }

    /**
     * Run a {@link Runnable} in the background using a {@link ThreadPool}
     *
     * @param command The {@link Runnable} to execute
     * @param delay the delay befor first run
     */
    public void execute( final Runnable command,
                         final long delay ) {
        execute( DEFAULT_THREADPOOL_NAME, command, delay, 0 );
    }

    /**
     * Run a {@link Runnable} in the background using a {@link ThreadPool}
     *
     * @param command The {@link Runnable} to execute
     */
    public void execute( final Runnable command ) {
        execute( DEFAULT_THREADPOOL_NAME, command, 0, 0 );
    }

    /**
     * Run a {@link Runnable} in the background using a {@link ThreadPool}
     *
     * @param threadPoolName The thread pool name to be used
     * @param command The {@link Runnable} to execute
     * @param delay the delay befor first run
     */
    public void execute( final String threadPoolName,
                         final Runnable command,
                         final long delay ) {
        execute( threadPoolName, command, delay, 0 );
    }

    /**
     * Run a {@link Runnable} in the background using a {@link ThreadPool}
     *
     * @param threadPoolName The thread pool name to be used
     * @param command The {@link Runnable} to execute
     */
    public void execute( final String threadPoolName,
                         final Runnable command ) {
        execute( threadPoolName, command, 0, 0 );
    }

    /**
     * Remove a <code>Runnable</code> from the command stack
     *
     * @param command The <code>Runnable</code> to be removed
     */
    public void remove( Runnable command ) {
        synchronized( commandStack ) {
            for( final Iterator i = commandStack.iterator(); i.hasNext(); ) {
                final ExecutionInfo info = (ExecutionInfo)i.next();

                if( info.m_command == command ) {
                    i.remove();
                    commandStack.notifyAll();

                    return;
                }
            }
        }

        getLogger().warn( "Could not find command " + command +
                          " for removal" );
    }

    /**
     * The heart of the command manager
     */
    public void run() {
        if( getLogger().isDebugEnabled() ) {
            getLogger().debug( "Entering loop" );
        }

        while( keepRunning ) {
            synchronized( commandStack ) {
                try {
                    if( commandStack.size(  ) > 0 ) {
                        final ExecutionInfo info =
                            (ExecutionInfo)commandStack.first();
                        final long delay =
                            info.m_nextRun - System.currentTimeMillis( );

                        if( delay > 0 ) {
                            commandStack.wait( delay );
                        }
                    } else {
                        if( getLogger().isDebugEnabled() ) {
                            getLogger().debug( "No commands available. Will just wait for one" );
                        }

                        commandStack.wait();
                    }
                } catch( final InterruptedException ie ) {
                    if( getLogger().isDebugEnabled() ) {
                        getLogger().debug( "I've been interrupted" );
                    }
                }

                if( keepRunning ) {
                    if( commandStack.size() > 0 ) {
                        final ExecutionInfo info =
                            (ExecutionInfo)commandStack.first();
                        final long delay =
                            info.m_nextRun - System.currentTimeMillis();

                        if( delay < 0 ) {
                            info.execute();
                        }
                    }
                }
            }
        }

        if( getLogger().isDebugEnabled() ) {
            getLogger().debug( "Exiting loop" );
        }
    }
    
    /**
     * Start the managing thread
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void start() throws Exception {
        if( getLogger().isDebugEnabled() ) {
            getLogger().debug( "Starting the heart" );
        }

        keepRunning = true;
        ( (ThreadPool) pools.get( DEFAULT_THREADPOOL_NAME ) ).execute( this );
    }

    /**
     * Stop the managing thread
     */
    protected void stop( ) {
        keepRunning = false;

        synchronized( commandStack ) {
            commandStack.notifyAll();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param priority The priority to set as string value.
     *
     * @return The priority as int value.
     */
    private int getPriority( final String priority ) {
        if( "MIN".equalsIgnoreCase( priority ) ) {
            return Thread.MIN_PRIORITY;
        } else if( "NORM".equalsIgnoreCase( priority ) ) {
            return Thread.NORM_PRIORITY;
        } else if( "MAX".equalsIgnoreCase( priority ) ) {
            return Thread.MAX_PRIORITY;
        } else {
            getLogger().warn( "Unknown thread priority \"" + priority +
                              "\". Set to \"NORM\"." );

            return Thread.NORM_PRIORITY;
        }
    }

    private String getConfigValue( final Map    config,
                                   final String key)
    throws Exception {
        final Object value = config.get(key);
        if ( value == null ) {
            throw new Exception("Required configuration value for key '" + key + "' is missing.");
        }
        return value.toString();
    }

    private String getConfigValue( final Map    config,
                                   final String key,
                                   final String defaultValue) {
        final Object value = config.get(key);
        if ( value == null ) {
            return defaultValue;
        }
        return value.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param config DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws ConfigurationException DOCUMENT ME!
     */
    private DefaultThreadPool configThreadPool( final Map config )
    throws Exception {
        final String name = this.getConfigValue(config, "name");
        final int queueSize = Integer.valueOf(this.getConfigValue(config, "queue-size", String.valueOf(DEFAULT_QUEUE_SIZE))).intValue();
        final int maxPoolSize = Integer.valueOf(this.getConfigValue(config, "max-pool-size", String.valueOf(DEFAULT_MAX_POOL_SIZE))).intValue();
        int minPoolSize = Integer.valueOf(this.getConfigValue(config, "min-pool-size", String.valueOf(DEFAULT_MIN_POOL_SIZE))).intValue();

        // make sure we have enough threads for the default thread pool as we
        // need one for ourself
        if( DEFAULT_THREADPOOL_NAME.equals( name ) &&
            ( ( minPoolSize > 0 ) && ( minPoolSize < DEFAULT_MIN_POOL_SIZE ) ) ) {
            minPoolSize = DEFAULT_MIN_POOL_SIZE;
        }

        final String priority = this.getConfigValue(config, "priority", DEFAULT_THREAD_PRIORITY);
        final boolean isDaemon = Boolean.valueOf(this.getConfigValue(config, "daemon", String.valueOf(DEFAULT_DAEMON_MODE))).booleanValue();
        final long keepAliveTime = Long.valueOf(this.getConfigValue(config, "keep-alive-time-ms", String.valueOf(DEFAULT_KEEP_ALIVE_TIME))).longValue();
        final String blockPolicy = this.getConfigValue(config, "block-policy", DefaultThreadPool.POLICY_DEFAULT );
        final boolean shutdownGraceful = Boolean.valueOf(this.getConfigValue(config, "shutdown-graceful", String.valueOf(DEFAULT_SHUTDOWN_GRACEFUL))).booleanValue();
        final int shutdownWaitTime = Integer.valueOf(this.getConfigValue(config, "shutdown-wait-time-ms", String.valueOf(DEFAULT_SHUTDOWN_WAIT_TIME))).intValue();

        return createPool( new DefaultThreadPool(), name, queueSize,
                           maxPoolSize, minPoolSize, getPriority( priority ),
                           isDaemon, keepAliveTime, blockPolicy,
                           shutdownGraceful, shutdownWaitTime );
    }

    /**
     * Create a ThreadPool
     *
     * @param pool DOCUMENT ME!
     * @param name DOCUMENT ME!
     * @param queueSize The size of the queue
     * @param maxPoolSize The maximum number of threads
     * @param minPoolSize The maximum number of threads
     * @param priority The priority of threads created by this pool. This is
     *        one of {@link Thread#MIN_PRIORITY}, {@link
     *        Thread#NORM_PRIORITY}, or {@link Thread#MAX_PRIORITY}
     * @param isDaemon Whether or not thread from the pool should run in daemon
     *        mode
     * @param keepAliveTime How long should a thread be alive for new work to
     *        be done before it is GCed
     * @param blockPolicy What's the blocking policy is resources are exhausted
     * @param shutdownGraceful Should we wait for the queue to finish all
     *        pending commands?
     * @param shutdownWaitTime After what time a normal shutdown should take
     *        into account if a graceful shutdown has not come to an end
     *
     * @return A newly created <code>ThreadPool</code>
     */
    private DefaultThreadPool createPool( final DefaultThreadPool pool,
                                          final String name,
                                          final int queueSize,
                                          final int maxPoolSize,
                                          final int minPoolSize,
                                          final int priority,
                                          final boolean isDaemon,
                                          final long keepAliveTime,
                                          final String blockPolicy,
                                          final boolean shutdownGraceful,
                                          final int shutdownWaitTime ) {
        pool.setLogger( getLogger() );
        pool.setName( name );

        ThreadFactory factory = null;
        try {
            factory =
                (ThreadFactory)defaultThreadFactoryClass.newInstance(  );
        } catch( final Exception ex ) {
            getLogger().warn( "Cannot instantiate a ThreadFactory from class " +
                              defaultThreadFactoryClass.getName() +
                              ". Will use a " +
                              DefaultThreadFactory.class.getName(), ex );
            factory = new DefaultThreadFactory(  );
        }

        factory.setPriority( priority );
        factory.setDaemon( isDaemon );
        pool.setThreadFactory( factory );
        pool.setQueue( queueSize );
        pool.setMaximumPoolSize( ( maxPoolSize < 0 ) ? Integer.MAX_VALUE
                                 : maxPoolSize );

        if( minPoolSize < 1 ) {
            getLogger().warn( "min-pool-size < 1 for pool \"" +
                              name + "\". Set to 1" );
        }

        pool.setMinimumPoolSize( ( minPoolSize < 1 ) ? 1 : minPoolSize );

        if( keepAliveTime < 0 ) {
            getLogger().warn( "keep-alive-time-ms < 0 for pool \"" +
                              name + "\". Set to 1000" );
        }

        pool.setKeepAliveTime( ( keepAliveTime < 0 ) ? 1000 : keepAliveTime );
        pool.setBlockPolicy( blockPolicy );
        pool.setShutdownGraceful( shutdownGraceful );
        pool.setShutdownWaitTimeMs( shutdownWaitTime );

        synchronized( pools ) {
            pools.put( name, pool );
        }

        printPoolInfo( pool );
        return pool;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pool DOCUMENT ME!
     */
    private void printPoolInfo( final DefaultThreadPool pool ) {
        if( getLogger().isInfoEnabled() ) {
            if( pool.isQueued() ) {
                final StringBuffer msg = new StringBuffer();
                msg.append( "ThreadPool named \"" ).append( pool.getName() );
                msg.append( "\" created with maximum queue-size=" );
                msg.append( pool.getMaxQueueSize(  ) );
                msg.append( ",max-pool-size=" ).append( pool.getMaximumPoolSize() );
                msg.append( ",min-pool-size=" ).append( pool.getMinimumPoolSize() );
                msg.append( ",priority=" ).append( pool.getPriority() );
                msg.append( ",isDaemon=" ).append( ( (ThreadFactory)pool.getThreadFactory() ).isDaemon() );
                msg.append( ",keep-alive-time-ms=" ).append( pool.getKeepAliveTime() );
                msg.append( ",block-policy=\"" ).append( pool.getBlockPolicy() );
                msg.append( "\",shutdown-wait-time-ms=" ).append( pool.getShutdownWaitTimeMs() );
                getLogger().info( msg.toString() );
            } else {
                final StringBuffer msg = new StringBuffer();
                msg.append( "ThreadPool named \"" ).append( pool.getName() );
                msg.append( "\" created with no queue,max-pool-size=" ).append( pool.getMaximumPoolSize() );
                msg.append( ",min-pool-size=" ).append( pool.getMinimumPoolSize() );
                msg.append( ",priority=" ).append( pool.getPriority() );
                msg.append( ",isDaemon=" ).append( ( (ThreadFactory)pool.getThreadFactory() ).isDaemon() );
                msg.append( ",keep-alive-time-ms=" ).append( pool.getKeepAliveTime() );
                msg.append( ",block-policy=" ).append( pool.getBlockPolicy(  ) );
                msg.append( ",shutdown-wait-time-ms=" ).append( pool.getShutdownWaitTimeMs() );
                getLogger().info( msg.toString() );
            }
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * The $classType$ class ...
     *
     * @version $Id$
     */
    private class ExecutionInfo implements Comparable {
        //~ Instance fields ----------------------------------------------------

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

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ExecutionInfo object.
         *
         * @param pool DOCUMENT ME!
         * @param command DOCUMENT ME!
         * @param delay DOCUMENT ME!
         * @param interval DOCUMENT ME!
         * @param logger DOCUMENT ME!
         */
        ExecutionInfo( final ThreadPool pool,
                       final Runnable command,
                       final long delay,
                       final long interval,
                       final Log logger ) {
            m_pool = pool;
            m_command = command;
            m_delay = delay;
            m_interval = interval;
            m_logger = logger;
            m_nextRun = System.currentTimeMillis() + delay;

            synchronized( commandStack )
            {
                commandStack.add( this );
                commandStack.notifyAll();
            }
            Thread.yield(); // Give others a chance to run
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param other DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public int compareTo( final Object other ) {
            final ExecutionInfo otherInfo = (ExecutionInfo)other;
            int diff = (int)( m_nextRun - otherInfo.m_nextRun );
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
            if( m_logger.isDebugEnabled() ) {
                m_logger.debug( "Executing command " + m_command + " in pool \"" +
                                 m_pool.getName() + "\", schedule with interval=" + m_interval );
            }

            synchronized( commandStack ) {
                commandStack.remove( this );
                if( m_interval > 0 ) {
                    m_nextRun = System.currentTimeMillis() + m_interval;
                    commandStack.add( this );
                }
            }

            try {
                m_pool.execute( m_command );
            } catch( final InterruptedException ie ) {
                if( m_logger.isDebugEnabled() ) {
                    m_logger.debug( "Interrupted executing command + " + m_command );
                }
            } catch( final Throwable t ) {
                m_logger.error( "Exception executing command " + m_command, t );
            }
        }
    }
}
