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
package org.apache.cocoon.beans;

import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.cocoon.BaseSpringTest;
import org.apache.cocoon.components.jms.AbstractMessageListener;
import org.apache.cocoon.jms.SimpleMessageListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * Provides a base class to verify message delivery via JMS based on
 * {@link AbstractMessageListener} including a Spring IoC container.
 */
public abstract class BaseMessageListenerTest extends BaseSpringTest {

    /**
     * Spring consumer bean name.
     */
    private static final String SIMPLE_CONSUMER = "simpleConsumer";

    /**
     * Spring producer bean name.
     */
    private static final String PRODUCER = "myJmsTemplate";

    /**
     * Returns a JMS {@link Destination} (e.g. Topic or Queue) to use.
     * 
     * @return A Destination.
     */
    protected abstract Destination getDestination();

    /**
     * Sends a simple text message to a {@link Destination} and verifies receipt
     * by using a {@link SimpleMessageListener}.
     * 
     * @throws JMSException
     *             In case, JMS delivery fails.
     */
    public void testMessageListener() throws JMSException {
        SimpleMessageListener consumer = (SimpleMessageListener) factory
                .getBean(SIMPLE_CONSUMER);
        JmsTemplate template = (JmsTemplate) factory.getBean(PRODUCER);

        MessageCreator creator = new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                String text = "This is a message";
                TextMessage message = session.createTextMessage(text);
                return message;
            }
        };

        consumer.flushMessages();
        
        // Get the destination (e.g. Topic or Queue) from the superclass.
        Destination destination = getDestination();
        // Send a message.
        template.send(destination, creator);

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            // Do nothing.
        }

        List messages = consumer.getMessages();
        assertEquals(1, messages.size());
        assertEquals("This is a message", ((ActiveMQTextMessage) messages
                .get(0)).getText());
        
        consumer.destroy();
    }

}