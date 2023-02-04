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
package org.apache.cocoon.jms;

import java.util.LinkedList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.cocoon.components.jms.AbstractMessageListener;

/**
 * Provides a simple message consumer for test reasons. Incoming messages are stored in a {@link List}.
 */
public final class SimpleMessageListener extends AbstractMessageListener {

    /**
     * The list of messages received.
     */
    private List messages;

    /**
     * Default constructor.
     * 
     * @throws JMSException If setup fails.
     */
    public SimpleMessageListener() throws JMSException {
        super();
        this.messages = new LinkedList();
    }

    /**
     * Callback method for message receipt called by the broker.
     * 
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message message) {
        try {
            this.messages.add(message);
            message.acknowledge();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    /**
     * Clears all received messages.
     */
    public void flushMessages() {
        this.messages.clear();
    }

    /**
     * Returns all received messages.
     * 
     * @return The received messages.
     */
    public final List getMessages() {
        return this.messages;
    }
}
