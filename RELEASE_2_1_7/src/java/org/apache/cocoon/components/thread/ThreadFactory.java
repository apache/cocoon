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
 * The ThreadFactory interface describes the responability of Factories
 * creating Thread for {@link ThreadPool}s of the {@link RunnableManager}
 *
 * @author <a href="mailto:giacomo.at.apache.org">Giacomo Pati</a>
 * @version CVS $Id: ThreadFactory.java 56765 2004-11-06 13:54:31Z giacomo $
 */
public interface ThreadFactory
    extends EDU.oswego.cs.dl.util.concurrent.ThreadFactory
{
    //~ Methods ----------------------------------------------------------------

    /**
     * Set the daemon mode of created <code>Thread</code>s should have
     *
     * @param isDaemon Whether new {@link Thread}s should run as daemons.
     */
    void setDaemon( boolean isDaemon );

    /**
     * Get the daemon mode created <code>Thread</code>s will have
     *
     * @return Whether new {@link Thread}s should run as daemons.
     */
    boolean isDaemon(  );

    /**
     * Set the priority newly created <code>Thread</code>s should have
     *
     * @param priority One of {@link Thread#MIN_PRIORITY}, {@link
     *        Thread#NORM_PRIORITY}, {@link Thread#MAX_PRIORITY}
     */
    void setPriority( int priority );

    /**
     * Get the priority newly created <code>Thread</code>s will have
     *
     * @return One of {@link Thread#MIN_PRIORITY}, {@link
     *         Thread#NORM_PRIORITY}, {@link Thread#MAX_PRIORITY}
     */
    int getPriority(  );

    /**
     * Create a new Thread for a {@link Runnable} command
     *
     * @param command The <code>Runnable</code>
     *
     * @return new <code>Thread</code>
     */
    Thread newThread( Runnable command );
}
