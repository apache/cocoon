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

import org.apache.cocoon.thread.impl.DefaultThreadFactory;

import junit.framework.TestCase;


/**
 * The $classType$ class ...
 *
 * @version $Id$
 */
public class DefaultThreadFactoryTestCase extends TestCase
{
    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public final void testGetPriority(  )
    {
        final DefaultThreadFactory factory = new DefaultThreadFactory(  );
        factory.setPriority( Thread.MAX_PRIORITY );
        assertEquals( "priority", Thread.MAX_PRIORITY, factory.getPriority(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testIsDaemon(  )
    {
        final DefaultThreadFactory factory = new DefaultThreadFactory(  );
        factory.setDaemon( false );
        assertEquals( "daemon mode", false, factory.isDaemon(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testNewThread(  )
    {
        final DefaultThreadFactory factory = new DefaultThreadFactory(  );
        factory.setDaemon( true );
        factory.setPriority( Thread.MIN_PRIORITY );

        final Thread thread = factory.newThread( new DummyRunnable(  ) );
        assertEquals( "new thread daemon mode", true, thread.isDaemon(  ) );
        assertEquals( "new thread priority", Thread.MIN_PRIORITY,
                      thread.getPriority(  ) );
        assertEquals( "factory daemon mode", factory.isDaemon(  ),
                      thread.isDaemon(  ) );
        assertEquals( "factory priority", factory.getPriority(  ),
                      thread.getPriority(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testSetDaemon(  )
    {
        final DefaultThreadFactory factory = new DefaultThreadFactory(  );
        factory.setDaemon( false );

        final Thread thread = factory.newThread( new DummyRunnable(  ) );
        assertEquals( "daemon mode", false, thread.isDaemon(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testSetPriority(  )
    {
        final DefaultThreadFactory factory = new DefaultThreadFactory(  );
        factory.setPriority( Thread.MAX_PRIORITY );

        final Thread thread = factory.newThread( new DummyRunnable(  ) );
        assertEquals( "priority", Thread.MAX_PRIORITY, thread.getPriority(  ) );
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * The $classType$ class ...
     *
     * @version $Id$
     */
    private static class DummyRunnable implements Runnable
    {
        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        public void run(  )
        {
            // nothing
        }
    }
}
