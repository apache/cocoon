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

/**
 * The RunnableManager interface describes the functionality of an
 * implementation running commands in the background.
 *
 * @author <a href="mailto:giacomo.at.apache.org">Giacomo Pati</a>
 * @version CVS $Id: RunnableManager.java 56786 2004-11-06 23:12:39Z giacomo $
 */
public interface RunnableManager
{
    //~ Instance fields --------------------------------------------------------

    /** The role name */
    String ROLE = RunnableManager.class.getName(  );

    //~ Methods ----------------------------------------------------------------

    /**
     * Create a shared ThreadPool with a specific {@link ThreadFactory}
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
     */
    void createPool( String name,
                     int queueSize,
                     int maxPoolSize,
                     int minPoolSize,
                     int priority,
                     final boolean isDaemon,
                     long keepAliveTime,
                     String blockPolicy,
                     boolean shutdownGraceful,
                     int shutdownWaitTime );

    /**
     * Create a private ThreadPool with a specific {@link ThreadFactory}
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
     * @return The newly created <code>ThreadPool</code>
     */
    ThreadPool createPool( int queueSize,
                           int maxPoolSize,
                           int minPoolSize,
                           int priority,
                           final boolean isDaemon,
                           long keepAliveTime,
                           String blockPolicy,
                           boolean shutdownGraceful,
                           int shutdownWaitTime );

    /**
     * Immediate Execution of a runnable in the background
     *
     * @param command The command to execute
     */
    void execute( Runnable command );

    /**
     * Immediate Execution of a runnable in the background
     *
     * @param command The command to execute
     * @param delay The delay before first run
     */
    void execute( Runnable command,
                  long delay );

    /**
     * Immediate Execution of a runnable in the background
     *
     * @param command The command to execute
     * @param delay The delay before first run
     * @param interval The interval of repeated runs
     */
    void execute( Runnable command,
                  long delay,
                  long interval );

    /**
     * Immediate Execution of a runnable in the background
     *
     * @param threadPoolName The thread pool to use
     * @param command The command to execute
     */
    void execute( String threadPoolName,
                  Runnable command );

    /**
     * Immediate Execution of a runnable in the background
     *
     * @param threadPoolName The thread pool to use
     * @param command The command to execute
     * @param delay The delay before first run
     */
    void execute( String threadPoolName,
                  Runnable command,
                  long delay );

    /**
     * Delayed and repeated Execution of a runnable in the background
     *
     * @param threadPoolName The thread pool to use
     * @param command The command to execute
     * @param delay The delay before first run
     * @param interval The interval of repeated runs
     */
    void execute( String threadPoolName,
                  Runnable command,
                  long delay,
                  long interval );

    /**
     * Remove a {@link Runnable} from the execution stack
     *
     * @param command The command to be removed
     */
    void remove( Runnable command );
}
