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
 * A linked list based channel implementation. The algorithm avoids contention
 * between puts and takes when the queue is not empty. Normally a put and a
 * take can proceed simultaneously. (Although it does not allow multiple
 * concurrent puts or takes.) This class tends to perform more efficently than
 * other Queue implementations in producer/consumer applications.
 * 
 * <p>
 * [<a
 * href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">
 * Introduction to this package. </a>]
 * </p>
 */
public class LinkedQueue
    extends EDU.oswego.cs.dl.util.concurrent.LinkedQueue
    implements Queue
{
    //~ Methods ----------------------------------------------------------------

    /**
     * @see org.apache.cocoon.components.thread.Queue#getQueueSize()
     */
    public int getQueueSize(  )
    {
        return -1;
    }
}
