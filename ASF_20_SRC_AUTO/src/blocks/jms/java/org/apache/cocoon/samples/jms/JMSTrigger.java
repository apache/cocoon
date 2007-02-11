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
package org.apache.cocoon.samples.jms;

import java.util.Hashtable;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hsqldb.Trigger;

/**
 * Example Trigger for HSQLDB doing cache invalidation through the eventcache
 * block and JMS messages. 
 * 
 * @version CVS $Id: JMSTrigger.java,v 1.6 2004/03/05 13:01:57 bdelacretaz Exp $
 * @author <a href="mailto:haul@apache.org">haul</a>
 */
public class JMSTrigger implements Trigger {

    // this exmaple class uses defaults to run with OpenJMS
    // TODO make this somehow configurable...
    protected String contextFactoryName = "org.exolab.jms.jndi.InitialContextFactory";
    protected String scheme = "rmi";
    protected String host = "localhost";
    protected String port = "";
    protected String jndiname = "";
    protected String topicFactoryName = "JmsTopicConnectionFactory";
    protected String topicName = "topic1";
    protected int deliveryMode = DeliveryMode.NON_PERSISTENT;
    protected int priority = 4;
    protected long timeToLive = 10000;

    protected Topic topic = null;
    protected TopicPublisher publisher = null;
    protected TopicSession session = null;
    protected TopicConnection connection = null;
    protected Context context = null;
    protected TopicConnectionFactory topicConnectionFactory = null;

    /**
     * 
     */
    public JMSTrigger() {
        super();
    }

    /**
     * Get initial context.
     * 
     * @return initial context
     * @throws NamingException
     */
    public Context getContext() throws NamingException {

        Hashtable properties = new Hashtable();

        properties.put(Context.INITIAL_CONTEXT_FACTORY, this.contextFactoryName);

        if (this.port.equals("")) {
            if (scheme.equals("tcp") || scheme.equals("tcps")) {
                port = "3035";
            } else if (scheme.equals("http")) {
                port = "8080";
            } else if (scheme.equals("https")) {
                port = "8443";
            } else if (scheme.equals("rmi")) {
                port = "1099";
            }
        }

        String name = "";
        if (scheme.equals("rmi")) {
            name = this.jndiname;
        }

        String url = scheme + "://" + host + ":" + port + "/" + name;

        properties.put(Context.PROVIDER_URL, url);
        return new InitialContext(properties);
    }

    private void setupConnection() throws NamingException, JMSException {
        // setup JMS connection
        this.context = this.getContext();
        this.topicConnectionFactory = (TopicConnectionFactory) this.context.lookup(this.topicFactoryName);
        this.connection = this.topicConnectionFactory.createTopicConnection();
        this.connection.start();
    }

    private void setupSession() throws JMSException {
        this.session = connection.createTopicSession(false, Session.CLIENT_ACKNOWLEDGE);
        this.topic = session.createTopic(this.topicName);
        this.publisher = session.createPublisher(topic);
    }

    private void connect() throws NamingException, JMSException {
        if (this.connection == null)
            this.setupConnection();
        if (this.session == null)
            this.setupSession();
    }

    private void disconnect() throws JMSException, NamingException {
        // do we really need to do this every time??
        // OTOH we should expect to run this trigger rather infrequently.
        this.session.close();
        this.session = null;
        this.connection.close();
        this.connection = null;
        this.topicConnectionFactory = null;
        this.context.close();
        this.context = null;
    }

    /* 
     * @see org.hsqldb.Trigger#fire(java.lang.String, java.lang.String, java.lang.Object[])
     */
    public void fire(String trigName, String tabName, Object[] row) {
        try {
            connect();
            TextMessage message =
                this.session.createTextMessage(
                    trigName.toLowerCase() + "|" + tabName.toLowerCase());
            this.publisher.publish(
                this.topic,
                message,
                this.deliveryMode,
                this.priority,
                this.timeToLive);
            disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}