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

/**
 * Extension to add queue size reporting
 *
 * @version $Id$
 *
 * @see EDU.oswego.cs.dl.util.concurrent.Channel
 */
public interface Queue
    extends EDU.oswego.cs.dl.util.concurrent.Channel {

    //~ Methods ----------------------------------------------------------------

    /**
     * get the current queue size
     *
     * @return current size of queue. If the size of the queue is not
     *         maintained by an implementation -1 should be returned.
     */
    int getQueueSize();
}
