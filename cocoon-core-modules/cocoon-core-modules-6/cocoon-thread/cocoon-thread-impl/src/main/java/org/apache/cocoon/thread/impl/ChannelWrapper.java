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

import EDU.oswego.cs.dl.util.concurrent.Channel;

/**
 * Wrapper around a Channel implementation for constructor convenience
 *
 * @version $Id$
 */
public class ChannelWrapper
    implements Channel {

    //~ Instance fields --------------------------------------------------------

    /** The wrapped Channel */
    private Channel channel;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param channel DOCUMENT ME!
     */
    public void setChannel( final Channel channel ) {
        this.channel = channel;
    }

    /**
     * @see EDU.oswego.cs.dl.util.concurrent.Puttable#offer(java.lang.Object,
     *      long)
     */
    public boolean offer( final Object obj,
                          final long timeout )
    throws InterruptedException {
        return channel.offer( obj, timeout );
    }

    /**
     * @see EDU.oswego.cs.dl.util.concurrent.Channel#peek()
     */
    public Object peek() {
        return channel.peek();
    }

    /**
     * @see EDU.oswego.cs.dl.util.concurrent.Takable#poll(long)
     */
    public Object poll( final long timeout )
    throws InterruptedException {
        return channel.poll( timeout );
    }

    /**
     * @see EDU.oswego.cs.dl.util.concurrent.Puttable#put(java.lang.Object)
     */
    public void put( final Object obj )
    throws InterruptedException {
        channel.put( obj );
    }

    /**
     * @see EDU.oswego.cs.dl.util.concurrent.Takable#take()
     */
    public Object take()  throws InterruptedException {
        return channel.take();
    }
}
