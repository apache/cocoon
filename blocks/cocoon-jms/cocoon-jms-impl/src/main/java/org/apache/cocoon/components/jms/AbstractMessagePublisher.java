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
package org.apache.cocoon.components.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.cocoon.util.AbstractLogEnabled;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * Abstract JMS message publisher. Use this as a basis for components that want to publish JMS messages.
 * 
 * @version $Id$
 */
public abstract class AbstractMessagePublisher extends AbstractLogEnabled {

    /**
     * JMS template injected by Spring.
     */
    protected JmsTemplate template;

    /**
     * Destination (e.g. Topic or Queue) injected by Spring.
     */
    protected Destination destination;

    /**
     * Publishes a provided message.
     */
    protected synchronized void publishMessage(final Message message) throws JMSException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Publishing message '" + message + "'");
        }
        this.template.send(this.destination, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return message;
            }
        });
    }

    /**
     * @param template the template to set
     */
    public final void setTemplate(final JmsTemplate template) {
        this.template = template;
    }

    /**
     * @param destination the destination to set
     */
    public final void setDestination(final Destination destination) {
        this.destination = destination;
    }
}