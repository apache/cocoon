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
 * Extension to add queue size reporting
 *
 * @author <a href="mailto:giacomo.at.apache.org">Giacomo Pati</a>
 * @version CVS $Id: Queue.java 56702 2004-11-05 22:52:05Z giacomo $
 *
 * @see EDU.oswego.cs.dl.util.concurrent.Channel
 */
public interface Queue
    extends EDU.oswego.cs.dl.util.concurrent.Channel
{
    //~ Methods ----------------------------------------------------------------

    /**
     * get the current queue size
     *
     * @return current size of queue. If the size of the queue is not
     *         maintained by an implementation -1 should be returned.
     */
    int getQueueSize(  );
}
