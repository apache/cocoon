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
package org.apache.cocoon.acting;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.cocoon.caching.EventAware;
import org.apache.cocoon.caching.validity.Event;
import org.apache.cocoon.caching.validity.NamedEvent;
import org.apache.cocoon.components.jms.AbstractMessageListener;

/**
 * JMS listener will notify an {@link org.apache.cocoon.caching.EventAware} component of external events. This could be
 * used for example to do external cache invalidation.
 */
public class JMSEventMessageListener extends AbstractMessageListener {

    /**
     * Cache injected by Spring.
     */
    private EventAware cache;

    /**
     * Default constructor.
     * 
     * @throws JMSException In case, JMS initialization fails.
     */
    public JMSEventMessageListener() throws JMSException {
        super();
    }

    /**
     * Notifies the event cache of events occurred.
     */
    public synchronized void onMessage(Message message) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Receiving message: " + message);
        }
        Event event;
        try {
            event = eventFromTextMessage(message);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Notifying of " + event);
            }
            this.cache.processEvent(event);
        } catch (JMSException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Could not process message: " + message + ": " + e.getMessage());
            }
        }
    }

    /**
     * Convert the message contents to (a series of) cache event. The default implementation assumes that the message
     * contains the trigger name, a '|', and a table name. It extracts the tablename and creates a NamedEvent with it.
     * Override this method to provide a custom message to event mapping.
     * 
     * @param message the JMS message.
     * @return the cache event.
     */
    protected Event eventFromTextMessage(Message message) throws JMSException {
        String name = ((TextMessage) message).getText();
        return new NamedEvent(name);
    }

    /**
     * @param cache the cache to set
     */
    public final void setCache(final EventAware cache) {
        this.cache = cache;
    }
}