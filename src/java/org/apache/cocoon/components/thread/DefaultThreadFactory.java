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
 * This class is responsible to create new Thread instances to run a command.
 *
 * @author <a href="mailto:info@otego.com">Otego AG, Switzerland</a>
 * @version $Id$
 */
public class DefaultThreadFactory
    implements ThreadFactory, EDU.oswego.cs.dl.util.concurrent.ThreadFactory
{
    //~ Instance fields --------------------------------------------------------

    /** The priority of newly created Threads */
    private int m_priority = Thread.NORM_PRIORITY;

    /**
     * @see org.apache.cocoon.components.thread.ThreadFactory#setPriority(int)
     */
    public void setPriority( final int priority )
    {
        if( ( Thread.MAX_PRIORITY == priority ) ||
            ( Thread.MIN_PRIORITY == priority ) ||
            ( Thread.NORM_PRIORITY == priority ) )
        {
            m_priority = priority;
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @see EDU.oswego.cs.dl.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    public Thread newThread( final Runnable command )
    {
        final Thread thread = new Thread( command );
        thread.setPriority( m_priority );

        return thread;
    }
}
