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
package org.apache.cocoon.components.jms;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.NamingException;

import org.apache.avalon.framework.CascadingException;

/**
 * JMSConnection properties container plus utilities.
 * 
 * @version CVS $Id: JMSConnection.java,v 1.8 2004/03/05 13:01:57 bdelacretaz Exp $
 * @author <a href="mailto:haul@apache.org">haul</a>
 */
public interface JMSConnection {
    
    static final String ROLE = JMSConnection.class.getName();
    
    /**
     * Register a new TopicListener for this connection.
     * 
     * @param listener
     * @param selector
     * 
     * @throws CascadingException if the connection was not successfully 
     * initialized, JMSException or NamingException if errors occur during 
     * JMS methods.  It is up to the MessageListener to determine how to 
     * handle this failure.
     */
    void registerListener(MessageListener listener, String selector)
        throws CascadingException, JMSException, NamingException;
        
    /**
     * Get a new TopicPublisher for this connection.
     * 
     * @return new TopicPublisher
     * @throws JMSException
     * @throws NamingException
     */
    TopicPublisher getPublisher() throws JMSException, NamingException;

    /**
     * Get the session associated with this connection. This is needed for example to
     * create messages.
     * 
     * @return TopicSession
     * @throws NamingException
     * @throws JMSException
     */
    TopicSession getSession() throws NamingException, JMSException;
}