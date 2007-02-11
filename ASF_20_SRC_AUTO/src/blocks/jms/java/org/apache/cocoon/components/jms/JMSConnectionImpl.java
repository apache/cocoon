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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * JMSConnection properties container plus utilities.
 * 
 * <p>Configuration:</p>
 * <pre>&lt;jndi-info&gt;
 *      &lt;parameter name="" value=""/&gt;
 * &lt;jndi-info&gt;</pre>
 * 
 * <p>Other parameters:</p>
 * <table>
 *  <tbody>
 *   <tr><td>topic-factory  </td><td><i>(required, no default)</i></td></tr>
 *   <tr><td>topic          </td><td><i>(required, no default)</i></td></tr>
 *   <tr><td>ack-mode       </td><td>("dups")</td></tr>
 *   <tr><td>durable-subscription-id       </td><td><i>(optional)</i></td></tr>
 *  </tbody>
 * </table>
 * 
 * @version CVS $Id: JMSConnectionImpl.java,v 1.11 2004/03/05 13:01:57 bdelacretaz Exp $
 * @author <a href="mailto:haul@apache.org">haul</a>
 */
public class JMSConnectionImpl extends AbstractLogEnabled 
                               implements Configurable, 
                                          Disposable, 
                                          ThreadSafe,
                                          Initializable,  
                                          JMSConnection {

    private boolean available = false;
    protected String topicFactoryName;
    protected String topicName;
    protected String ackModeName = "dups";
    protected String durableSubscriptionID;

    protected TopicConnection connection = null;
    protected TopicSession session = null;
    protected List subscribers = null;
    protected Topic topic = null;
    protected int ackMode = Session.DUPS_OK_ACKNOWLEDGE;
    protected Context context = null;
    protected TopicConnectionFactory topicConnectionFactory;

    private Parameters jndiParams;
    
    
    public void configure(Configuration conf) throws ConfigurationException {
        Parameters parameters = Parameters.fromConfiguration(conf);
        this.jndiParams = Parameters.fromConfiguration(conf.getChild("jndi-info"));
        this.topicFactoryName =
                parameters.getParameter("topic-factory", null);
        this.topicName = parameters.getParameter("topic", null);
        this.durableSubscriptionID =
            parameters.getParameter(
                "durable-subscription-id",null);

        this.ackModeName =
            parameters.getParameter("ack-mode", this.ackModeName).toLowerCase();
        // see if an ack mode has been specified. If it hasn't
        // then assume CLIENT_ACKNOWLEDGE mode.
        this.ackMode = Session.CLIENT_ACKNOWLEDGE;
        if (this.ackModeName.equals("auto")) {
            this.ackMode = Session.AUTO_ACKNOWLEDGE;
        } else if (this.ackModeName.equals("dups")) {
            this.ackMode = Session.DUPS_OK_ACKNOWLEDGE;
        } else if (!this.ackModeName.equals("client")) {
            // ignore all ack modes, to test no acking
            this.ackMode = -1;
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        try {
            this.context = setupContext();
            this.setupConnection();
            this.setupSession();
            this.available = true;
        } catch (NamingException e) {
            if (getLogger().isWarnEnabled()) {
                Throwable rootCause = e.getRootCause();
                if (rootCause != null) {
                    String message = e.getRootCause().getMessage();
                    if (rootCause instanceof ClassNotFoundException) {
                        String info = "WARN! *** JMS block is installed but jms client library not found. ***\n" + 
                            "- For the jms block to work you must install and start a JMS server and " +
                            "place the client jar in WEB-INF/lib.";
                            if (message.indexOf("exolab") > 0 ) {
                                info += "\n- The default server, OpenJMS is configured in cocoon.xconf but is not bundled with Cocoon.";
                            }
                        System.err.println(info);
                        getLogger().warn(info,e);
                    } else {
                        System.out.println(message);
                        getLogger().warn("Cannot get Initial Context. Is the JNDI server reachable?",e);
                    }
                }
                else {
                    getLogger().warn("Failed to initialize JMS.",e);
                }
            }
        } catch (JMSException e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Failed to initialize JMS.",e);
            }
        }
    }

    /* 
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        try {
            this.disconnect();
        } catch (JMSException e) {
        } catch (NamingException e) {
        }
    }
    
	/**
     * Register a new TopicListener for this connection.
     * 
     * @param listener
     * @param selector
     */
    public synchronized void registerListener(
        MessageListener listener,
        String selector)
        throws CascadingException, JMSException, NamingException {

        if (!this.available) {
            // Connection was not successfully initialized.
            throw new CascadingException("Attempt to register Listener on unavailable JMS Connection");
        }
        
        TopicSubscriber subscriber = null;
        if (this.durableSubscriptionID != null) {
            subscriber =
                this.getSession().createDurableSubscriber(
                    this.topic,
                    this.durableSubscriptionID,
                    selector,
                    false);
        } else {
            subscriber = this.getSession().createSubscriber(this.topic, selector, false);
        }
        if (this.subscribers == null) {
            this.subscribers = new LinkedList();
        }
        this.subscribers.add(subscriber);

        subscriber.setMessageListener(listener);
    }

    /**
     * Get a new TopicPublisher for this connection.
     * 
     * @return TopicPublisher
     * @throws JMSException
     * @throws NamingException
     */
    public TopicPublisher getPublisher() throws JMSException, NamingException {
        TopicSession session = this.getSession();
        if (session != null) {
			return session.createPublisher(this.topic);
        } else {
        	return null;
        }
    }

    /**
     * Get the session associated with this connection. This is needed for example to
     * create messages.
     * 
     * @return session associated with this connection
     * @throws NamingException
     * @throws JMSException
     */
    public TopicSession getSession() throws NamingException, JMSException {
        return this.session;
    }

    /**
      * Get initial context.
      * 
      * @return initial context
      * @throws NamingException
      */
    protected Context setupContext() throws NamingException {

        String[] jndiKeys = jndiParams.getNames();
        InitialContext ctx;
        if (jndiKeys.length > 0) {
            // Params specified in cocoon.xconf
            Hashtable properties = null;
            properties = new Hashtable();
            for (int i = 0 ; i < jndiKeys.length ; i++) {
                properties.put(jndiKeys[i],jndiParams.getParameter(jndiKeys[i],""));
            }
            ctx = new InitialContext(properties);
        } else {
            // Use jndi.properties from the classpath or container
            ctx = new InitialContext();
        }
        return ctx;
    }


    /**
     * Setup connection.
     * 
     * @throws NamingException
     * @throws JMSException
     */
    private void setupConnection() throws NamingException, JMSException {
        // setup JMS connection
        //this.context = this.getContext();
        if (this.context != null) {
            this.topicConnectionFactory =
                (TopicConnectionFactory) this.context.lookup(this.topicFactoryName);
            this.connection = this.topicConnectionFactory.createTopicConnection();
            this.connection.start();
        }
    }

    /**
     * Setup session for this connection.
     * 
     * @throws JMSException
     */
    private void setupSession() throws JMSException {
        if (this.connection != null) {
            this.session = connection.createTopicSession(false, this.ackMode);
            this.topic = session.createTopic(this.topicName);
        }
    }

    /**
     * Disconnect session and connection, close all subscribers.
     * 
     * @throws JMSException
     * @throws NamingException
     */
    private void disconnect() throws JMSException, NamingException {
        if (this.subscribers != null) {
            for (Iterator i = this.subscribers.iterator(); i.hasNext();) {
                ((TopicSubscriber) i.next()).close();
            }
            this.subscribers.clear();
        }
        this.session.close();
        this.connection.close();
    }

}
