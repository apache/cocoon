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

/**
 * A rendezvous channel, similar to those used in CSP and Ada.  Each put must
 * wait for a take, and vice versa.  Synchronous channels are well suited for
 * handoff designs, in which an object running in one thread must synch up
 * with an object running in another thread in order to hand it some
 * information, event, or task.
 * 
 * <p>
 * If you only need threads to synch up without exchanging information,
 * consider using a Barrier. If you need bidirectional exchanges, consider
 * using a Rendezvous.
 * </p>
 * 
 * <p></p>
 * 
 * <p>
 * [<a
 * href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">
 * Introduction to this package. </a>]
 * </p>
 *
 * @see EDU.oswego.cs.dl.util.concurrent.CyclicBarrier
 * @see EDU.oswego.cs.dl.util.concurrent.Rendezvous
 */
public class SynchronousChannel
    extends EDU.oswego.cs.dl.util.concurrent.SynchronousChannel
    // This is ridiculous, but Queue must be fully qualified to compile in JDK1.3
    implements org.apache.cocoon.components.thread.Queue
{
    //~ Methods ----------------------------------------------------------------

    /**
     * @see org.apache.cocoon.components.thread.Queue#getQueueSize()
     */
    public int getQueueSize(  )
    {
        return 0;
    }
}
