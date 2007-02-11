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

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.easymock.MockControl;

/**
 * The $classType$ class ...
 *
 * @author <a href="mailto:giacomo.at.apache.org">Giacomo Pati</a>
 * @version $Id$
 */
public class DefaultThreadPoolTestCase extends AbstractTestCase
{
    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public final void testDefaultThreadPool(  )
    {
        final DefaultThreadPool pool = new DefaultThreadPool(  );
        pool.enableLogging( new ConsoleLogger( ConsoleLogger.LEVEL_DEBUG ) );
        pool.setName( "mypool" );

        // We cannot mock the DefaultThreadFactory as the underlying
        // PooledExecutor of the DefaultThreadPool will again wrapp it into a
        // PooledExecutor.Worker instance that does some bookeeping.
        // Using a easymocked DefaultThreadFactory will prevent the
        // PooledExecutor from shutting down and thus hangs forever.
        final ThreadFactory threadFactory = new DefaultThreadFactory();
        threadFactory.setPriority( Thread.MAX_PRIORITY );
        pool.setThreadFactory( threadFactory );
        pool.setQueue( 230 );
        pool.setMaximumPoolSize( 15 );
        pool.setMinimumPoolSize( 9 );
        pool.setKeepAliveTime( 11000 );
        pool.setBlockPolicy( "ABORT" );
        pool.setShutdownGraceful( false );
        pool.setShutdownWaitTimeMs( 12345 );

        assertEquals( "block-policy", "ABORT", pool.getBlockPolicy(  ) );
        assertEquals( "keep-alive-time-ms", 11000L, pool.getKeepAliveTime(  ) );
        assertEquals( "max-queueu-size", 230, pool.getMaximumQueueSize(  ) );
        assertEquals( "max-pool-size", 15, pool.getMaximumPoolSize(  ) );
        assertEquals( "min-pool-size", 9, pool.getMinimumPoolSize(  ) );
        assertEquals( "name", "mypool", pool.getName(  ) );
        assertEquals( "priority", Thread.MAX_PRIORITY, pool.getPriority(  ) );
        assertEquals( "queue-size", 0, pool.getQueueSize(  ) );
        assertEquals( "isQueued", true, pool.isQueued(  ) );
        assertEquals( "isTerminatedAfterShutdown", false,
                      pool.isTerminatedAfterShutdown(  ) );
        verify(  );
    }

    /*
     * Class under test for void execute(Runnable)
     */
    public final void testExecuteRunnable(  )
        throws InterruptedException
    {
        final MockControl runnableControl =
            createStrictControl( Runnable.class );
        final Runnable runnable = (Runnable)runnableControl.getMock(  );
        runnable.run(  );
        runnableControl.replay(  );

        final DefaultThreadPool pool = new DefaultThreadPool(  );
        pool.enableLogging( new ConsoleLogger( ConsoleLogger.LEVEL_DEBUG ) );
        pool.setName( "mypool" );
        // We cannot mock the DefaultThreadFactory as the underlying
        // PooledExecutor of the DefaultThreadPool will again wrapp it into a
        // PooledExecutor.Worker instance that does some bookeeping.
        // Using a easymocked DefaultThreadFactory will prevent the
        // PooledExecutor from shutting down and thus hangs forever.
        pool.setThreadFactory( new DefaultThreadFactory() );
        pool.setQueue( 230 );
        pool.setMaximumPoolSize( 15 );
        pool.setMinimumPoolSize( 9 );
        pool.setKeepAliveTime( 100 );
        pool.setBlockPolicy( "ABORT" );
        pool.setShutdownGraceful( false );
        pool.setShutdownWaitTimeMs( 1234 );
        pool.execute( runnable );
        Thread.yield(  );
        Thread.sleep( 100 );
        pool.shutdown();
        verify(  );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws InterruptedException DOCUMENT ME!
     */
    public final void testShutdown(  )
    throws InterruptedException
    {
        final Runnable runnable = new Runnable(){
            public void run()
            {
                final ConsoleLogger logger = new ConsoleLogger( ConsoleLogger.LEVEL_DEBUG );
                logger.info( "runnable runs" );
                try
                {
                    Thread.sleep( 1000 );
                }
                catch( final InterruptedException ie )
                {
                    logger.info( "runnable has been interrupted ");
                }
                logger.info( "runnable terminated" );
            }
        };

        final DefaultThreadPool pool = new DefaultThreadPool(  );
        pool.enableLogging( new ConsoleLogger( ConsoleLogger.LEVEL_DEBUG ) );
        pool.setName( "mypool" );
        pool.setThreadFactory( new DefaultThreadFactory() );
        pool.setQueue( 0 );
        pool.setMaximumPoolSize( 15 );
        pool.setMinimumPoolSize( 9 );
        pool.setKeepAliveTime( 1000 );
        pool.setBlockPolicy( "ABORT" );
        pool.setShutdownGraceful( true );
        pool.setShutdownWaitTimeMs( 100 );
        pool.execute( runnable );
        pool.execute( runnable );
        Thread.yield();
        Thread.sleep( 200 );
        pool.shutdown(  );
        Thread.sleep( 200 );
        verify(  );
    }
}
