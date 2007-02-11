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
 * Efficient array-based bounded buffer class. Adapted from CPJ, chapter 8,
 * which describes design.
 * 
 * <p>
 * [<a
 * href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">
 * Introduction to this package. </a>]
 * </p>
 * 
 * <p></p>
 */
public class BoundedQueue
    extends EDU.oswego.cs.dl.util.concurrent.BoundedBuffer
    implements Queue
{
    //~ Constructors -----------------------------------------------------------

    /**
     * Create a buffer with the current default capacity
     */
    public BoundedQueue(  )
    {
        super(  );
    }

    /**
     * Create a BoundedQueue with the given capacity.
     *
     * @param capacity The capacity
     *
     * @exception IllegalArgumentException if capacity less or equal to zero
     */
    public BoundedQueue( int capacity )
        throws IllegalArgumentException
    {
        super( capacity );
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return current size of queue.
     */
    public int getQueueSize(  )
    {
        return usedSlots_;
    }
}
