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
 * @version CVS $Id$
 */
public interface ThreadFactory extends EDU.oswego.cs.dl.util.concurrent.ThreadFactory
{
    //~ Methods ----------------------------------------------------------------

    /**
     * Set the priority newly created <code>Thread</code>s should have
     *
     * @param priority One of {@link Thread#MIN_PRIORITY}, {@link
     *        Thread#NORM_PRIORITY}, {@link Thread#MAX_PRIORITY}
     */
    void setPriority( int priority );

    /**
     * Create a new Thread for a {@link Runnable} command
     *
     * @param command The <code>Runnable</code>
     *
     * @return new <code>Thread</code>
     */
    Thread newThread( Runnable command );
}
