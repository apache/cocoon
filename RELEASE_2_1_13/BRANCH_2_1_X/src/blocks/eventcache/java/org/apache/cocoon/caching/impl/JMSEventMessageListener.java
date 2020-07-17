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
 */package org.apache.cocoon.caching.impl;

import javax.jms.Message;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.caching.EventAware;
import org.apache.cocoon.caching.validity.Event;
import org.apache.cocoon.caching.validity.NamedEvent;
import org.apache.cocoon.components.jms.AbstractMessageListener;

/**
 * JMS listener will notify an {@link org.apache.cocoon.caching.EventAware} component
 * of external events. This could be used for example to do external cache invalidation.
 * 
 * <p>
 * Besides those inherited from 
 * {@link org.apache.cocoon.components.jms.AbstractMessageListener} 
 * parameters are:
 * </p>
 * <table border="1">
 *  <tbody>
 *   <tr>
 *     <th align="left">parameter</th>
 *     <th align="left">required</th>
 *     <th align="left">default</th>
 *     <th align="left">description</th>
 *   </tr>
 *   <tr>
 *     <td valign="top">eventcache-role</td>
 *     <td valign="top">no</td>
 *     <td valign="top">org.apache.cocoon.caching.Cache/EventAware</td>
 *     <td valign="top">The role name to lookup the event cache from the service manager.</td>
 *   </tr>
 *  </tbody>
 * </table>
 */
public class JMSEventMessageListener extends AbstractMessageListener implements ThreadSafe {

    // ---------------------------------------------------- Constants

    private static final String DEFAULT_EVENTCACHE_ROLE = Cache.ROLE + "/EventAware";
    private static final String EVENTCACHE_ROLE_PARAM = "eventcache-role";

    // ---------------------------------------------------- Instance variables

    private String m_eventAwareRole;
    private EventAware m_eventCache;

    // ---------------------------------------------------- Lifecycle

    public JMSEventMessageListener() {
    }

    public void parameterize(Parameters parameters) throws ParameterException {
        super.parameterize(parameters);
        m_eventAwareRole = parameters.getParameter(EVENTCACHE_ROLE_PARAM, DEFAULT_EVENTCACHE_ROLE);
    }

    public void initialize() throws Exception {
        super.initialize();
        m_eventCache = (EventAware) m_manager.lookup(m_eventAwareRole);
    }

    public void dispose() {
        super.dispose();
        this.m_manager.release(m_eventCache);
    }

    /**
     * Notifies the event cache of events occurred.
     */
    public synchronized void onMessage(Message message) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Receiving message: " + message);
        }
        final Event[] events = eventsFromMessage(message);
        for (int i = 0; i < events.length; i++) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Notifying " + m_eventAwareRole + " of " + events[i]);
            }
            m_eventCache.processEvent(events[i]);
        }
    }

    /**
     * Convert the message contents to (a series of) cache event. The default implementation 
     * assumes that the message contains the trigger name, a '|', and a table name. 
     * It extracts the tablename and creates a NamedEvent with it. 
     * Override this method to provide a custom message to event mapping.
     * 
     * @param message  the JMS message.
     * @return  the cache event.
     */
    protected Event[] eventsFromMessage(Message message) {
        String name = message.toString();
        int pos = name.indexOf('|');
        return new Event[] { new NamedEvent(name.substring(pos + 1)) };
    }
    
}
