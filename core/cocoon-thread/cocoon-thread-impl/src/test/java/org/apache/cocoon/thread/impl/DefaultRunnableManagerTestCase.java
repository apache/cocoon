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

/**
 * The $classType$ class ...
 *
 * FIXME - We disabled all tests for now as we moved the component from Avalon to Spring!!
 *
 * @version $Id$
 */
public class DefaultRunnableManagerTestCase
    extends AbstractTestCase {

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor for DefaultRunnableManagerTestCase.
     *
     * @param name
     */
    public DefaultRunnableManagerTestCase( String name ) {
        super( name );
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final void testConfigureDaemonPool()
    throws Exception {
        if ( true ) return;
/*        final MockControl threadPoolConfigControl =
            createStrictControl( Configuration.class );
        final Configuration threadPoolConfig =
            (Configuration)threadPoolConfigControl.getMock(  );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "name" ),
                                                 createValueConfigMock( "daemon" ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "queue-size" ),
                                                 createIntegerConfigMock( 2 * DefaultRunnableManager.DEFAULT_QUEUE_SIZE,
                                                                          DefaultRunnableManager.DEFAULT_QUEUE_SIZE ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "max-pool-size" ),
                                                 createIntegerConfigMock( 2 * DefaultRunnableManager.DEFAULT_MAX_POOL_SIZE,
                                                                          DefaultRunnableManager.DEFAULT_MAX_POOL_SIZE ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "min-pool-size" ),
                                                 createIntegerConfigMock( DefaultRunnableManager.DEFAULT_MIN_POOL_SIZE / 3,
                                                                          DefaultRunnableManager.DEFAULT_MIN_POOL_SIZE ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "priority" ),
                                                 createValueConfigMock( "LOW",
                                                                        DefaultRunnableManager.DEFAULT_THREAD_PRIORITY ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "daemon" ),
                                                 createBooleanConfigMock( false,
                                                                          DefaultRunnableManager.DEFAULT_DAEMON_MODE ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "keep-alive-time-ms" ),
                                                 createLongConfigMock( DefaultRunnableManager.DEFAULT_KEEP_ALIVE_TIME / 2,
                                                                       DefaultRunnableManager.DEFAULT_KEEP_ALIVE_TIME ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "block-policy" ),
                                                 createValueConfigMock( "WAIT",
                                                                        DefaultThreadPool.POLICY_DEFAULT ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "shutdown-graceful" ),
                                                 createBooleanConfigMock( true,
                                                                          DefaultRunnableManager.DEFAULT_SHUTDOWN_GRACEFUL ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "shutdown-wait-time-ms" ),
                                                 createIntegerConfigMock( DefaultRunnableManager.DEFAULT_SHUTDOWN_WAIT_TIME / 2,
                                                                          DefaultRunnableManager.DEFAULT_SHUTDOWN_WAIT_TIME ) );
        threadPoolConfigControl.replay();

        final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration []
                                                                     {
                                                                         threadPoolConfig
                                                                     } ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerDaemonControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDaemon =
            (Logger)childLoggerDaemonControl.getMock(  );
        childLoggerDaemonControl.replay(  );

        final MockControl childLoggerDefaultControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDefault =
            (Logger)childLoggerDefaultControl.getMock(  );
        childLoggerDefaultControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        logger.warn( "Unknown thread priority \"LOW\". Set to \"NORM\"." );
        loggerControl.expectAndReturn( logger.getChildLogger( "daemon" ),
                                       childLoggerDaemon );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"daemon\" created with maximum queue-size=2147483647,max-pool-size=10,min-pool-size=1,priority=5,isDaemon=false,keep-alive-time-ms=30000,block-policy=\"WAIT\",shutdown-wait-time-ms=0" );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLoggerDefault );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=5,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool daemon" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool daemon disposed" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );

        //runnableManager.configure( mainConfig );

        runnableManager.init();

        runnableManager.destroy();
        verify(  );*/
    }

    /**
     * DOCUMENT ME!
     */
    public final void testConfigureMinimal()
    throws Exception {
        if ( true ) return;
/*        final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerDefaultControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDefault =
            (Logger)childLoggerDefaultControl.getMock(  );
        childLoggerDefaultControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLoggerDefault );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=5,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );

        //runnableManager.configure( mainConfig );
        runnableManager.init();

        runnableManager.destroy();
        verify(  );*/
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final void testConfigureMyPool(  )
    throws Exception {
        if ( true ) return;
 /*       final MockControl threadPoolConfigControl =
            createStrictControl( Configuration.class );
        final Configuration threadPoolConfig =
            (Configuration)threadPoolConfigControl.getMock(  );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "name" ),
                                                 createValueConfigMock( "mypool" ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "queue-size" ),
                                                 createIntegerConfigMock( 2 * DefaultRunnableManager.DEFAULT_QUEUE_SIZE,
                                                                          DefaultRunnableManager.DEFAULT_QUEUE_SIZE ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "max-pool-size" ),
                                                 createIntegerConfigMock( 2 * DefaultRunnableManager.DEFAULT_MAX_POOL_SIZE,
                                                                          DefaultRunnableManager.DEFAULT_MAX_POOL_SIZE ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "min-pool-size" ),
                                                 createIntegerConfigMock( DefaultRunnableManager.DEFAULT_MIN_POOL_SIZE / 3,
                                                                          DefaultRunnableManager.DEFAULT_MIN_POOL_SIZE ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "priority" ),
                                                 createValueConfigMock( "MIN",
                                                                        DefaultRunnableManager.DEFAULT_THREAD_PRIORITY ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "daemon" ),
                                                 createBooleanConfigMock( false,
                                                                          DefaultRunnableManager.DEFAULT_DAEMON_MODE ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "keep-alive-time-ms" ),
                                                 createLongConfigMock( DefaultRunnableManager.DEFAULT_KEEP_ALIVE_TIME / 2,
                                                                       DefaultRunnableManager.DEFAULT_KEEP_ALIVE_TIME ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "block-policy" ),
                                                 createValueConfigMock( "WAIT",
                                                                        DefaultThreadPool.POLICY_DEFAULT ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "shutdown-graceful" ),
                                                 createBooleanConfigMock( true,
                                                                          DefaultRunnableManager.DEFAULT_SHUTDOWN_GRACEFUL ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "shutdown-wait-time-ms" ),
                                                 createIntegerConfigMock( DefaultRunnableManager.DEFAULT_SHUTDOWN_WAIT_TIME / 2,
                                                                          DefaultRunnableManager.DEFAULT_SHUTDOWN_WAIT_TIME ) );
        threadPoolConfigControl.replay(  );

        final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration []
                                                                     {
                                                                         threadPoolConfig
                                                                     } ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerDefaultControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDefault =
            (Logger)childLoggerDefaultControl.getMock(  );
        childLoggerDefaultControl.replay(  );

        final MockControl childLoggerMyPoolControl =
            createStrictControl( Logger.class );
        final Logger childLoggerMyPool =
            (Logger)childLoggerMyPoolControl.getMock(  );
        childLoggerMyPoolControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "mypool" ),
                                       childLoggerMyPool );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"mypool\" created with maximum queue-size=2147483647,max-pool-size=10,min-pool-size=1,priority=1,isDaemon=false,keep-alive-time-ms=30000,block-policy=\"WAIT\",shutdown-wait-time-ms=0" );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLoggerDefault );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=5,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool mypool" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool mypool disposed" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );

        //runnableManager.configure( mainConfig );

        runnableManager.init();

        runnableManager.destroy();
        verify(  );*/
    }

    /**
     * Class under test for void createPool(String, int, int, int, int,
     * boolean, long, String, boolean, int)
     */
    public final void testCreatePoolStringintintintintbooleanlongStringbooleanint()
    throws Exception {
        if ( true ) return;
  /*      final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerDefaultControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDefault =
            (Logger)childLoggerDefaultControl.getMock(  );
        childLoggerDefaultControl.replay(  );

        final MockControl childLoggerMyPoolControl =
            createStrictControl( Logger.class );
        final Logger childLoggerMyPool =
            (Logger)childLoggerMyPoolControl.getMock(  );
        childLoggerMyPoolControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLoggerDefault );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=5,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.getChildLogger( "mypool" ),
                                       childLoggerMyPool );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"mypool\" created with maximum queue-size=230,max-pool-size=15,min-pool-size=12,priority=1,isDaemon=false,keep-alive-time-ms=15500,block-policy=\"DISCARD\",shutdown-wait-time-ms=22200" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool mypool" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool mypool disposed" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );

        //runnableManager.configure( mainConfig );

        runnableManager.createPool( "mypool", 230, 15, 12, Thread.MIN_PRIORITY,
                                    false, 15500, "DISCARD", false, 22200 );
        runnableManager.init();

        runnableManager.destroy();
        verify(  );*/
    }

    /**
     * Class under test for ThreadPool createPool(int, int, int, int, boolean,
     * long, String, boolean, int)
     */
    public final void testCreatePoolintintintintbooleanlongStringbooleanint()
    throws Exception {
        if ( true ) return;
    /*    final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerDefaultControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDefault =
            (Logger)childLoggerDefaultControl.getMock(  );
        childLoggerDefaultControl.replay(  );

        final MockControl childLoggerAnonControl =
            createStrictControl( Logger.class );
        final Logger childLoggerAnon =
            (Logger)childLoggerAnonControl.getMock(  );
        childLoggerAnonControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLoggerDefault );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=5,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.getChildLogger( "anon-xxx" ),
                                       childLoggerAnon );
        loggerControl.setMatcher( MockControl.ALWAYS_MATCHER );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"anon-xxx\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=10,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.setMatcher( MockControl.ALWAYS_MATCHER );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool anon-xxx" );
        loggerControl.setMatcher( MockControl.ALWAYS_MATCHER );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool anon-xxx disposed" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.setMatcher( MockControl.ALWAYS_MATCHER );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );

        //runnableManager.configure( mainConfig );

        final ThreadPool threadPool =
            runnableManager.createPool( 200, 5, 2, Thread.MAX_PRIORITY, true,
                                        15000, "ABORT", true, 22000 );
        assertEquals( "queue-size", 200, threadPool.getMaximumQueueSize(  ) );
        assertEquals( "max-pool-size", 5, threadPool.getMaximumPoolSize(  ) );
        assertEquals( "min-pool-size", 2, threadPool.getMinimumPoolSize(  ) );
        assertEquals( "priority", Thread.MAX_PRIORITY,
                      threadPool.getPriority(  ) );
        assertEquals( "keep-alive-time-ms", 15000,
                      threadPool.getKeepAliveTime(  ) );
        assertEquals( "block-policy", "ABORT", threadPool.getBlockPolicy(  ) );
        runnableManager.init();

        runnableManager.destroy();
        verify(  );*/
    }

    /**
     * Class under test for void execute(Runnable)
     */
    public final void testExecuteRunnable(  )
    throws Exception {
        if ( true ) return;
   /*     final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerControl =
            createStrictControl( Logger.class );
        final Logger childLogger = (Logger)childLoggerControl.getMock(  );
        childLoggerControl.expectAndReturn( childLogger.isDebugEnabled(  ), true );
        childLogger.debug( "Executing Command: org.apache.cocoon.thread.impl.DefaultRunnableManager" );
        childLoggerControl.setMatcher( MockControl.ALWAYS_MATCHER );
        childLoggerControl.expectAndReturn( childLogger.isDebugEnabled(  ), true );
        childLogger.debug( "Executing Command: org.apache.cocoon.thread.impl.DefaultRunnableManager" );
        childLoggerControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLogger );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=5,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Starting the heart" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Entering loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Command entered: EasyMock for interface java.lang.Runnable, pool=default, delay=0, interval=0" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Executing command EasyMock for interface java.lang.Runnable in pool \"default\", schedule with interval=0" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Exiting loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );

        //runnableManager.configure( mainConfig );

        runnableManager.init();

        final MockControl runnableControl =
            createStrictControl( Runnable.class );
        final Runnable runnable = (Runnable)runnableControl.getMock(  );
        runnable.run(  );
        runnableControl.replay(  );

        try {
            runnableManager.start(  );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.execute( runnable );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.stop(  );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.destroy();
            Thread.sleep( 20 );
        } catch( final Throwable ex ) {
            ex.printStackTrace(  );
            assertTrue( "Unexpected Exception", false );
        }

        verify(  );*/
    }

    /**
     * Class under test for void execute(Runnable, long)
     */
    public final void testExecuteRunnablelong(  )
    throws Exception {
        if ( true ) return;
  /*      final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerControl =
            createStrictControl( Logger.class );
        final Logger childLogger = (Logger)childLoggerControl.getMock(  );
        childLoggerControl.expectAndReturn( childLogger.isDebugEnabled(  ), true );
        childLogger.debug( "Executing Command: org.apache.cocoon.thread.impl.DefaultRunnableManager" );
        childLoggerControl.setMatcher( MockControl.ALWAYS_MATCHER );
        childLoggerControl.expectAndReturn( childLogger.isDebugEnabled(  ), true );
        childLogger.debug( "Executing Command: org.apache.cocoon.thread.impl.DefaultRunnableManager" );
        childLoggerControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLogger );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=5,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Starting the heart" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Entering loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Command entered: EasyMock for interface java.lang.Runnable, pool=default, delay=100, interval=0" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Executing command EasyMock for interface java.lang.Runnable in pool \"default\", schedule with interval=0" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Exiting loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );

        //runnableManager.configure( mainConfig );

        final MockControl runnableControl =
            createStrictControl( Runnable.class );
        final Runnable runnable = (Runnable)runnableControl.getMock(  );
        runnable.run(  );
        runnableControl.replay(  );

        try {
            runnableManager.init();
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.execute( runnable, 100, 0 );
            Thread.yield(  );
            Thread.sleep( 200 );
            runnableManager.stop(  );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.destroy();
            Thread.sleep( 20 );
        } catch( final Throwable ex ) {
            ex.printStackTrace(  );
            assertTrue( "Unexpected Exception", false );
        }

        verify(  );*/
    }

    /**
     * Class under test for void execute(Runnable, long, long)
     */
    public final void testExecuteRunnablelonglong(  )
    throws Exception {
        if ( true ) return;
  /*      final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerControl =
            createStrictControl( Logger.class );
        final Logger childLogger = (Logger)childLoggerControl.getMock(  );
        childLoggerControl.expectAndReturn( childLogger.isDebugEnabled(  ), true );
        childLogger.debug( "Executing Command: org.apache.cocoon.thread.impl.DefaultRunnableManager" );
        childLoggerControl.setMatcher( MockControl.ALWAYS_MATCHER );
        childLoggerControl.expectAndReturn( childLogger.isDebugEnabled(  ), true );
        childLogger.debug( "Executing Command: org.apache.cocoon.thread.impl.DefaultRunnableManager" );
        childLoggerControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLogger );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=5,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Starting the heart" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Entering loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Command entered: EasyMock for interface java.lang.Runnable, pool=default, delay=100, interval=100" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Executing command EasyMock for interface java.lang.Runnable in pool \"default\", schedule with interval=100" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Exiting loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );

        //runnableManager.configure( mainConfig );

        final MockControl runnableControl =
            createStrictControl( Runnable.class );
        final Runnable runnable = (Runnable)runnableControl.getMock(  );
        runnable.run(  );
        runnableControl.setVoidCallable( MockControl.ONE_OR_MORE );
        runnableControl.replay(  );

        try {
            runnableManager.init();
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.execute( runnable, 100, 100 );
            Thread.yield(  );
            Thread.sleep( 200 );
            runnableManager.destroy();
            Thread.yield(  );
            Thread.sleep( 20 );
        } catch( final Throwable ex ) {
            ex.printStackTrace(  );
            assertTrue( "Unexpected Exception", false );
        }

        verify(  );*/
    }

    /**
     * Class under test for void execute(String, Runnable)
     */
    public final void testExecuteStringRunnable(  )
    throws Exception {
        if ( true ) return;
  /*      final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerDefaultControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDefault =
            (Logger)childLoggerDefaultControl.getMock(  );
        childLoggerDefaultControl.expectAndReturn( childLoggerDefault.isDebugEnabled(  ),
                                                   true );
        childLoggerDefault.debug( "Executing Command: org.apache.cocoon.thread.impl.DefaultRunnableManager" );
        childLoggerDefaultControl.setMatcher( MockControl.ALWAYS_MATCHER );
        childLoggerDefaultControl.replay(  );

        final MockControl childLoggerMyPoolControl =
            createStrictControl( Logger.class );
        final Logger childLoggerMyPool =
            (Logger)childLoggerMyPoolControl.getMock(  );
        childLoggerMyPoolControl.expectAndReturn( childLoggerMyPool.isDebugEnabled(  ),
                                                  true );
        childLoggerMyPool.debug( "Executing Command: EasyMock for interface java.lang.Runnable,pool=mypool" );
        childLoggerMyPoolControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLoggerDefault );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=5,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Starting the heart" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Entering loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.getChildLogger( "mypool" ),
                                       childLoggerMyPool );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"mypool\" created with maximum queue-size=230,max-pool-size=15,min-pool-size=12,priority=1,isDaemon=false,keep-alive-time-ms=15500,block-policy=\"DISCARD\",shutdown-wait-time-ms=22200" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Command entered: EasyMock for interface java.lang.Runnable, pool=mypool, delay=0, interval=0" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Executing command EasyMock for interface java.lang.Runnable in pool \"mypool\", schedule with interval=0" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Exiting loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool mypool" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool mypool disposed" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );

        //runnableManager.configure( mainConfig );

        final MockControl runnableControl =
            createStrictControl( Runnable.class );
        final Runnable runnable = (Runnable)runnableControl.getMock(  );
        runnable.run(  );
        runnableControl.replay(  );

        try {
            runnableManager.init();
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.createPool( "mypool", 230, 15, 12,
                                        Thread.MIN_PRIORITY, false, 15500,
                                        "DISCARD", false, 22200 );
            runnableManager.execute( "mypool", runnable );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.destroy();
            Thread.yield(  );
            Thread.sleep( 20 );
        } catch( final Throwable ex ) {
            ex.printStackTrace(  );
            assertTrue( "Unexpected Exception", false );
        }

        verify();*/
    }

    /**
     * Class under test for void execute(String, Runnable, long)
     */
    public final void testExecuteStringRunnablelong(  )
    throws Exception {
        if ( true ) return;
   /*     final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay();

        final MockControl childLoggerDefaultControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDefault =
            (Logger)childLoggerDefaultControl.getMock(  );
        childLoggerDefaultControl.expectAndReturn( childLoggerDefault.isDebugEnabled(  ),
                                                   true );
        childLoggerDefault.debug( "Executing Command: org.apache.cocoon.thread.impl.DefaultRunnableManager" );
        childLoggerDefaultControl.setMatcher( MockControl.ALWAYS_MATCHER );
        childLoggerDefaultControl.replay(  );

        final MockControl childLoggerMyPoolControl =
            createStrictControl( Logger.class );
        final Logger childLoggerMyPool =
            (Logger)childLoggerMyPoolControl.getMock(  );
        childLoggerMyPoolControl.expectAndReturn( childLoggerMyPool.isDebugEnabled(  ),
                                                  true );
        childLoggerMyPool.debug( "Executing Command: EasyMock for interface java.lang.Runnable,pool=mypool" );
        childLoggerMyPoolControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLoggerDefault );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=5,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Starting the heart" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Entering loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.getChildLogger( "mypool" ),
                                       childLoggerMyPool );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"mypool\" created with maximum queue-size=230,max-pool-size=15,min-pool-size=12,priority=1,isDaemon=false,keep-alive-time-ms=15500,block-policy=\"DISCARD\",shutdown-wait-time-ms=22200" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Command entered: EasyMock for interface java.lang.Runnable, pool=mypool, delay=100, interval=0" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Executing command EasyMock for interface java.lang.Runnable in pool \"mypool\", schedule with interval=0" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Exiting loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool mypool" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool mypool disposed" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );

        //runnableManager.configure( mainConfig );

        final MockControl runnableControl =
            createStrictControl( Runnable.class );
        final Runnable runnable = (Runnable)runnableControl.getMock(  );
        runnable.run();
        runnableControl.replay();

        try {
            runnableManager.init();
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.createPool( "mypool", 230, 15, 12,
                                        Thread.MIN_PRIORITY, false, 15500,
                                        "DISCARD", false, 22200 );
            runnableManager.execute( "mypool", runnable, 100, 0 );
            Thread.yield();
            Thread.sleep( 200 );
            runnableManager.destroy();
            Thread.yield();
            Thread.sleep( 20 );
        } catch( final Throwable ex ) {
            ex.printStackTrace();
            assertTrue( "Unexpected Exception", false );
        }

        verify();*/
    }

    /**
     * Class under test for void execute(String, Runnable, long, long)
     */
    public final void testExecuteStringRunnablelonglong(  )
    throws Exception {
        if ( true ) return;
   /*     final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerDefaultControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDefault =
            (Logger)childLoggerDefaultControl.getMock(  );
        childLoggerDefaultControl.expectAndReturn( childLoggerDefault.isDebugEnabled(  ),
                                                   true );
        childLoggerDefault.debug( "Executing Command: org.apache.cocoon.thread.impl.DefaultRunnableManager" );
        childLoggerDefaultControl.setMatcher( MockControl.ALWAYS_MATCHER );
        childLoggerDefaultControl.replay(  );

        final MockControl childLoggerMyPoolControl =
            createStrictControl( Logger.class );
        final Logger childLoggerMyPool =
            (Logger)childLoggerMyPoolControl.getMock(  );
        childLoggerMyPoolControl.expectAndReturn( childLoggerMyPool.isDebugEnabled(  ),
                                                  true );
        childLoggerMyPool.debug( "Executing Command: EasyMock for interface java.lang.Runnable,pool=mypool" );
        childLoggerMyPoolControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLoggerDefault );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=5,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Starting the heart" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Entering loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.getChildLogger( "mypool" ),
                                       childLoggerMyPool );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"mypool\" created with maximum queue-size=230,max-pool-size=15,min-pool-size=12,priority=1,isDaemon=false,keep-alive-time-ms=15500,block-policy=\"DISCARD\",shutdown-wait-time-ms=22200" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Command entered: EasyMock for interface java.lang.Runnable, pool=mypool, delay=100, interval=100" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Executing command EasyMock for interface java.lang.Runnable in pool \"mypool\", schedule with interval=100" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Exiting loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool mypool" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool mypool disposed" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager();

        //runnableManager.configure( mainConfig );

        final MockControl runnableControl =
            createStrictControl( Runnable.class );
        final Runnable runnable = (Runnable)runnableControl.getMock(  );
        runnable.run();
        runnableControl.replay(  );

        try {
            runnableManager.init();
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.createPool( "mypool", 230, 15, 12,
                                        Thread.MIN_PRIORITY, false, 15500,
                                        "DISCARD", false, 22200 );
            runnableManager.execute( "mypool", runnable, 100, 100 );
            Thread.yield(  );
            Thread.sleep( 200 );
            runnableManager.destroy();
            Thread.yield(  );
            Thread.sleep( 20 );
        } catch( final Throwable ex ) {
            ex.printStackTrace();
            assertTrue( "Unexpected Exception", false );
        }

        verify();*/
    }
}
