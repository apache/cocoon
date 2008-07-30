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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.cocoon.util.AbstractLogEnabled;
import org.springframework.jms.core.JmsTemplate;

/**
 * Abstract {@link javax.jms.MessageListener} implementation. Registers as listener for an injected {@link Destination},
 * where Destination could either be a Topic or a Queue.
 * 
 * @version $Id$
 */
public abstract class AbstractMessageListener extends AbstractLogEnabled implements MessageListener {

    /**
     * JMS template injected by Spring.
     */
    private JmsTemplate template;

    /**
     * JMS {@link Connection}.
     */
    private Connection connection;

    /**
     * JMS {@link Session}.
     */
    private Session session;

    /**
     * Acknowledge Mode.
     */
    private int acknowledgeMode = Session.DUPS_OK_ACKNOWLEDGE;

    /**
     * JMS {@link MessageConsumer}.
     */
    private MessageConsumer consumer;

    /**
     * JMS {@link Destination}, injected by Spring.
     */
    private Destination destination;

    /**
     * Subscription Id, injected by Spring.
     */
    private String subscriptionId;

    /**
     * Initialization method called by Spring. Starts connection, creates a session and a consumer, depending on the
     * injected Destination and registers as listener.
     * 
     * @throws JMSException In case, initialization fails.
     */
    public void init() throws JMSException {
        ConnectionFactory factory = this.template.getConnectionFactory();
        this.connection = factory.createConnection();
        synchronized (this.connection) {
            if (this.connection.getClientID() == null) {
                this.connection.setClientID(this.subscriptionId);
            }
        }
        this.connection.start();
        this.session = this.connection.createSession(false, this.acknowledgeMode);
        this.consumer = this.session.createConsumer(this.destination);
        // register this class as callback
        this.consumer.setMessageListener(this);
        this.session.recover();
    }

    /**
     * Destroy method, called by Spring.
     */
    public void destroy() {
        if (this.consumer != null) {
            try {
                this.consumer.close();
            } catch (JMSException e) {
                this.getLogger().error("Error closing consumer", e);
            } finally {
                this.consumer = null;
            }
        }
        if (this.session != null) {
            try {
                this.session.close();
            } catch (JMSException e) {
                this.getLogger().error("Error closing session", e);
            } finally {
                this.session = null;
            }
        }
        if (this.connection != null) {
            try {
                this.connection.stop();
                this.connection.close();
            } catch (JMSException e) {
                this.getLogger().error("Error closing session", e);
            } finally {
                this.connection = null;
            }
        }
    }

    /**
     * @param template the template to set
     */
    public final void setTemplate(JmsTemplate template) {
        this.template = template;
    }

    /**
     * @param subscriptionId the subscriptionId to set
     */
    public final void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * @param topic the topic to set
     */
    public final void setDestination(Destination destination) {
        this.destination = destination;
    }
}