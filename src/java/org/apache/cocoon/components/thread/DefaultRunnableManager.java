/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.thread.ThreadSafe;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


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
 *       &lt;priority&gt;NORM&lt;/priority&gt; &lt;!-- MIN | NORM | MAX --&gt; 
 *       &lt;queue-size&gt;-1&lt;/queue-size&gt; &lt;!-- &lt;0 unbounded; ==0 no queue --&gt; 
 *       &lt;max-pool-size&gt;-1&lt;/max-pool-size&gt; &lt;!-- &le;0 unbounded --&gt; 
 *       &lt;min-pool-size&gt;2&lt;/min-pool-size&gt; &lt;!-- &le;0 not allowed --&gt;
 *       &lt;keep-alive-time-ms&gt;20000&lt;/keep-alive-time-ms&gt;
 *       &lt;block-policy&gt;RUN&lt;/block-policy&gt;
 *       &lt;shutdown-graceful&gt;false&lt;/shutdown-graceful&gt;
 *       &lt;shutdown-wait-time-ms&gt;-1&lt;/shutdown-wait-time-ms&gt;
 *     &lt;/thread-pool&gt; 
 *   &lt;/thread-pools&gt;
 * </pre>
 * </p>
 *
 * @author <a href="mailto:giacomo.at.apache.org">Giacomo Pati</a>
 * @version $Id$
 */
public class DefaultRunnableManager
    extends AbstractLogEnabled
    implements RunnableManager, Configurable, Disposable, Runnable, Startable,
                   ThreadSafe
{
    //~ Static fields/initializers ---------------------------------------------

    /** The default {@link ThreadFactory} */
    public static final String DEFAULT_THREAD_FACTORY =
        DefaultThreadFactory.class.getName(  );

    /** The default queue size */
    public static final int DEFAULT_QUEUE_SIZE = -1;

    /** The default maximum pool size */
    public static final int DEFAULT_MAX_POOL_SIZE = 5;

    /** The default minimum pool size */
    public static final int DEFAULT_MIN_POOL_SIZE = 5;

    /** The default thread priority */
    public static final String DEFAULT_THREAD_PRIORITY = "NORM";

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
    protected SortedSet m_executionInfo = new TreeSet(  );

    /** The managed thread pools */
    final Map m_pools = new HashMap(  );

    /** The configured default ThreadFactory class instance */
    private Class m_defaultThreadFactoryClass;

    /** Keep us running? */
    private boolean m_keepRunning = false;

    //~ Methods ----------------------------------------------------------------

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure( final Configuration config )
        throws ConfigurationException
    {
        final String defaultThreadFactoryName =
            config.getChild( "thread-factory" ).getValue( DEFAULT_THREAD_FACTORY );

        try
        {
            m_defaultThreadFactoryClass =
                Thread.currentThread(  ).getContextClassLoader(  ).loadClass( defaultThreadFactoryName );
        }
        catch( final Exception ex )
        {
            throw new ConfigurationException( "Cannot create instance of default thread factory " +
                                              defaultThreadFactoryName, ex );
        }

        final Configuration [] threadpools =
            config.getChildren( "thread-pools" );

        for( int i = 0; i < threadpools.length; i++ )
        {
            final DefaultThreadPool pool = configThreadPool( threadpools[ i ] );

            if( getLogger(  ).isInfoEnabled(  ) )
            {
                if( pool.isQueued(  ) )
                {
                    final StringBuffer msg = new StringBuffer(  );
                    msg.append( "ThreadPool named \"" ).append( pool.getName(  ) );
                    msg.append( "\" created with queue-size=" );
                    msg.append( pool.getQueueSize(  ) );
                    msg.append( ",max-pool-size=" ).append( pool.getMaximumPoolSize(  ) );
                    msg.append( ",min-pool-size=" ).append( pool.getMinimumPoolSize(  ) );
                    msg.append( ",priority=" ).append( pool.getPriority(  ) );
                    msg.append( ",keep-alive-time-ms=" ).append( pool.getKeepAliveTime(  ) );
                    msg.append( ",block-policy=\"" ).append( pool.getBlockPolicy(  ) );
                    msg.append( "\",shutdown-wait-time-ms=" ).append( pool.getShutdownWaitTimeMs(  ) );
                    getLogger(  ).info( msg.toString(  ) );
                }
                else
                {
                    final StringBuffer msg = new StringBuffer(  );
                    msg.append( "ThreadPool named \"" ).append( pool.getName(  ) );
                    msg.append( "\" created with no queue,max-pool-size=" )
                       .append( pool.getMaximumPoolSize(  ) );
                    msg.append( ",min-pool-size=" ).append( pool.getMinimumPoolSize(  ) );
                    msg.append( ",priority=" ).append( pool.getPriority(  ) );
                    msg.append( ",keep-alive-time-ms=" ).append( pool.getKeepAliveTime(  ) );
                    msg.append( ",block-policy=" ).append( pool.getBlockPolicy(  ) );
                    msg.append( ",shutdown-wait-time-ms=" ).append( pool.getShutdownWaitTimeMs(  ) );
                    getLogger(  ).info( msg.toString(  ) );
                }
            }
        }

        // Check if a "default" pool has been created
        final ThreadPool defaultThreadPool =
            (ThreadPool)m_pools.get( DEFAULT_THREADPOOL_NAME );

        if( null == defaultThreadPool )
        {
            createPool( DEFAULT_THREADPOOL_NAME, DEFAULT_QUEUE_SIZE,
                        DEFAULT_MAX_POOL_SIZE, DEFAULT_MIN_POOL_SIZE,
                        getPriority( DEFAULT_THREAD_PRIORITY ),
                        DEFAULT_KEEP_ALIVE_TIME,
                        DefaultThreadPool.POLICY_DEFAULT,
                        DEFAULT_SHUTDOWN_GRACEFUL, DEFAULT_SHUTDOWN_WAIT_TIME );
        }
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
     * @param keepAliveTime How long should a thread be alive for new work to
     *        be done before it is GCed
     * @param blockPolicy What's the blocking policy is resources are exhausted
     * @param shutdownGraceful Should we wait for the queue to finish all
     *        pending commands?
     * @param shutdownWaitTime After what time a normal shutdown should take
     *        into account if a graceful shutdown has not come to an end
     */
    public void createPool( final String name,
                            final int queueSize,
                            final int maxPoolSize,
                            final int minPoolSize,
                            final int priority,
                            final long keepAliveTime,
                            final String blockPolicy,
                            final boolean shutdownGraceful,
                            final int shutdownWaitTime )
    {
        createPool( new DefaultThreadPool(  ), name, queueSize, maxPoolSize,
                    minPoolSize, priority, keepAliveTime, blockPolicy,
                    shutdownGraceful, shutdownWaitTime );
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
                                  final long keepAliveTime,
                                  final String blockPolicy,
                                  final boolean shutdownGraceful,
                                  final int shutdownWaitTime )
    {
        final DefaultThreadPool pool = new DefaultThreadPool(  );
        final String name = "anon-" + pool.hashCode(  );

        return createPool( pool, name, queueSize, maxPoolSize, minPoolSize,
                           priority, keepAliveTime, blockPolicy,
                           shutdownGraceful, shutdownWaitTime );
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose(  )
    {
        if( getLogger(  ).isDebugEnabled(  ) )
        {
            getLogger(  ).debug( "Disposing all thread pools" );
        }

        for( final Iterator i = m_pools.keySet(  ).iterator(  ); i.hasNext(  ); )
        {
            final String poolName = (String)i.next(  );
            final DefaultThreadPool pool =
                (DefaultThreadPool)m_pools.get( poolName );

            if( getLogger(  ).isDebugEnabled(  ) )
            {
                getLogger(  ).debug( "Disposing thread pool " +
                                     pool.getName(  ) );
            }

            pool.shutdown(  );

            if( getLogger(  ).isDebugEnabled(  ) )
            {
                getLogger(  ).debug( "Thread pool " + pool.getName(  ) +
                                     " disposed" );
            }
        }

        try
        {
            m_pools.clear(  );
        }
        catch( final Throwable t )
        {
            getLogger(  ).error( "Cannot dispose", t );
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
                         long interval )
    {
        if( delay < 0 )
        {
            throw new IllegalArgumentException( "delay < 0" );
        }

        if( interval < 0 )
        {
            throw new IllegalArgumentException( "interval < 0" );
        }

        ThreadPool pool = (ThreadPool)m_pools.get( threadPoolName );

        if( null == pool )
        {
            getLogger(  ).warn( "ThreadPool \"" + threadPoolName +
                                "\" is not known. Will use ThreadPool \"" +
                                DEFAULT_THREADPOOL_NAME + "\"" );
            pool = (ThreadPool)m_pools.get( DEFAULT_THREADPOOL_NAME );
        }

        if( getLogger(  ).isDebugEnabled(  ) )
        {
            getLogger(  ).debug( "Command entered: " + command.toString(  ) +
                                 ",pool=" + pool.getName(  ) + ",delay=" +
                                 delay + ",interval=" + interval );
        }

        new ExecutionInfo( pool, command, delay, interval, getLogger(  ) );
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
                         final long interval )
    {
        execute( DEFAULT_THREADPOOL_NAME, command, delay, interval );
    }

    /**
     * Run a {@link Runnable} in the background using a {@link ThreadPool}
     *
     * @param command The {@link Runnable} to execute
     * @param delay the delay befor first run
     */
    public void execute( final Runnable command,
                         final long delay )
    {
        execute( DEFAULT_THREADPOOL_NAME, command, delay, 0 );
    }

    /**
     * Run a {@link Runnable} in the background using a {@link ThreadPool}
     *
     * @param command The {@link Runnable} to execute
     */
    public void execute( final Runnable command )
    {
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
                         final long delay )
    {
        execute( threadPoolName, command, delay, 0 );
    }

    /**
     * Run a {@link Runnable} in the background using a {@link ThreadPool}
     *
     * @param threadPoolName The thread pool name to be used
     * @param command The {@link Runnable} to execute
     */
    public void execute( final String threadPoolName,
                         final Runnable command )
    {
        execute( threadPoolName, command, 0, 0 );
    }

    /**
     * The heart of the command manager
     */
    public void run(  )
    {
        if( getLogger(  ).isDebugEnabled(  ) )
        {
            getLogger(  ).debug( "Entering loop" );
        }

        while( m_keepRunning )
        {
            synchronized( m_executionInfo )
            {
                try
                {
                    if( m_executionInfo.size(  ) > 0 )
                    {
                        final ExecutionInfo info =
                            (ExecutionInfo)m_executionInfo.first(  );
                        final long delay =
                            info.m_nextRun - System.currentTimeMillis(  );

                        if( delay > 0 )
                        {
                            m_executionInfo.wait( delay );
                        }
                    }
                    else
                    {
                        if( getLogger(  ).isDebugEnabled(  ) )
                        {
                            getLogger(  ).debug( "No commands available. Will just wait for one" );
                        }

                        m_executionInfo.wait(  );
                    }
                }
                catch( final InterruptedException ie )
                {
                    if( getLogger(  ).isDebugEnabled(  ) )
                    {
                        getLogger(  ).debug( "I've been interrupted" );
                    }
                }

                if( m_keepRunning )
                {
                    final ExecutionInfo info =
                        (ExecutionInfo)m_executionInfo.first(  );
                    final long delay =
                        info.m_nextRun - System.currentTimeMillis(  );

                    if( delay < 0 )
                    {
                        info.execute(  );
                    }
                }
            }
        }

        if( getLogger(  ).isDebugEnabled(  ) )
        {
            getLogger(  ).debug( "Exiting loop" );
        }
    }

    /**
     * Start the managing thread
     *
     * @throws Exception DOCUMENT ME!
     */
    public void start(  )
        throws Exception
    {
        if( getLogger(  ).isDebugEnabled(  ) )
        {
            getLogger(  ).debug( "starting heart" );
        }

        m_keepRunning = true;
        ( (ThreadPool)m_pools.get( DEFAULT_THREADPOOL_NAME ) ).execute( this );
    }

    /**
     * Stop the managing thread
     *
     * @throws Exception DOCUMENT ME!
     */
    public void stop(  )
        throws Exception
    {
        m_keepRunning = false;

        synchronized( m_executionInfo )
        {
            m_executionInfo.notifyAll(  );
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param priority The priority to set as string value.
     *
     * @return The priority as int value.
     */
    private int getPriority( final String priority )
    {
        if( "MIN".equalsIgnoreCase( priority ) )
        {
            return Thread.MIN_PRIORITY;
        }
        else if( "NORM".equalsIgnoreCase( priority ) )
        {
            return Thread.NORM_PRIORITY;
        }
        else if( "MAX".equalsIgnoreCase( priority ) )
        {
            return Thread.MAX_PRIORITY;
        }
        else
        {
            return Thread.NORM_PRIORITY;
        }
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
    private DefaultThreadPool configThreadPool( final Configuration config )
        throws ConfigurationException
    {
        final String name = config.getChild( "name" ).getValue(  );
        final int queueSize =
            config.getChild( "queue-size" ).getValueAsInteger( DEFAULT_QUEUE_SIZE );
        final int maxPoolSize =
            config.getChild( "max-pool-size" ).getValueAsInteger( DEFAULT_MAX_POOL_SIZE );
        int minPoolSize =
            config.getChild( "min-pool-size" ).getValueAsInteger( DEFAULT_MIN_POOL_SIZE );

        // make sure we have enough threads for the default thread pool as we 
        // need one for ourself
        if( DEFAULT_THREADPOOL_NAME.equals( name ) &&
            ( minPoolSize < DEFAULT_MIN_POOL_SIZE ) )
        {
            minPoolSize = DEFAULT_MIN_POOL_SIZE;
        }

        final String priority =
            config.getChild( "priority" ).getValue( DEFAULT_THREAD_PRIORITY );
        final long keepAliveTime =
            config.getChild( "keep-alive-time-ms" ).getValueAsLong( DEFAULT_KEEP_ALIVE_TIME );
        final String blockPolicy =
            config.getChild( "block-policy" ).getValue( DefaultThreadPool.POLICY_DEFAULT );
        final boolean shutdownGraceful =
            config.getChild( "shutdown-graceful" ).getValueAsBoolean( DEFAULT_SHUTDOWN_GRACEFUL );
        final int shutdownWaitTime =
            config.getChild( "shutdown-wait-time-ms" ).getValueAsInteger( DEFAULT_SHUTDOWN_WAIT_TIME );

        return createPool( new DefaultThreadPool(  ), name, queueSize,
                           maxPoolSize, minPoolSize, getPriority( priority ),
                           keepAliveTime, blockPolicy, shutdownGraceful,
                           shutdownWaitTime );
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
                                          final long keepAliveTime,
                                          final String blockPolicy,
                                          final boolean shutdownGraceful,
                                          final int shutdownWaitTime )
    {
        pool.enableLogging( getLogger(  ).getChildLogger( name ) );
        pool.setName( name );

        ThreadFactory factory = null;

        try
        {
            factory =
                (ThreadFactory)m_defaultThreadFactoryClass.newInstance(  );
        }
        catch( final Exception ex )
        {
            getLogger(  ).warn( "Cannot instantiate a ThreadFactory from class " +
                                m_defaultThreadFactoryClass.getName(  ) +
                                ". Will use a " +
                                DefaultThreadFactory.class.getName(  ), ex );
            factory = new DefaultThreadFactory(  );
        }

        factory.setPriority( priority );
        pool.setThreadFactory( factory );
        pool.setQueue( queueSize );
        pool.setMaximumPoolSize( maxPoolSize );
        pool.setMinimumPoolSize( minPoolSize );
        pool.setKeepAliveTime( keepAliveTime );
        pool.setBlockPolicy( blockPolicy );
        pool.setShutdownGraceful( shutdownGraceful );
        pool.setShutdownWaitTimeMs( shutdownWaitTime );

        synchronized( m_pools )
        {
            m_pools.put( name, pool );
        }

        return pool;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * The $classType$ class ...
     *
     * @author <a href="mailto:giacomo.at.apache.org">Giacomo Pati</a>
     * @version $Id$
     */
    private class ExecutionInfo
        implements Comparable
    {
        //~ Instance fields ----------------------------------------------------

        /** Our logger */
        final Logger m_logger;

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
                       final Logger logger )
        {
            m_pool = pool;
            m_command = command;
            m_delay = delay;
            m_interval = interval;
            m_logger = logger;
            m_nextRun = System.currentTimeMillis(  ) + delay;

            synchronized( m_executionInfo )
            {
                m_executionInfo.add( this );
                m_executionInfo.notifyAll(  );
            }
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param other DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public int compareTo( final Object other )
        {
            final ExecutionInfo otherInfo = (ExecutionInfo)other;

            return (int)( m_nextRun - otherInfo.m_nextRun );
        }

        /**
         * DOCUMENT ME!
         */
        void execute(  )
        {
            if( getLogger(  ).isDebugEnabled(  ) )
            {
                getLogger(  ).debug( "Executing Command: " +
                                     m_command.toString(  ) + ",pool=" +
                                     m_pool.getName(  ) + ",delay=" + m_delay +
                                     ",interval=" + m_interval );
            }

            synchronized( m_executionInfo )
            {
                m_executionInfo.remove( this );
                m_nextRun = ( ( m_interval > 0 )
                              ? ( System.currentTimeMillis(  ) + m_interval ) : 0 );

                if( m_nextRun > 0 )
                {
                    m_executionInfo.add( this );
                    m_executionInfo.notifyAll(  );
                }
            }

            try
            {
                m_pool.execute( m_command );
            }
            catch( final InterruptedException ie )
            {
                if( m_logger.isDebugEnabled(  ) )
                {
                    m_logger.debug( m_command + " has been interrupted" );
                }
            }
            catch( final Throwable t )
            {
                m_logger.error( "Exception thrown by Command " + m_command, t );
            }
        }
    }
}
