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

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

/**
 * Abstract JMS message publisher. Use this as a basis for components 
 * that want to publish JMS messages.
 * When used in conjunction with the default {@link org.apache.cocoon.components.jms.JMSConnectionManager} 
 * implementation this class supports automatic reconnection when the connection gets severed.
 * 
 * <p>Parameters:</p>
 * <table border="1">
 *  <tbody>
 *   <tr>
 *     <th align="left">parameter</th>
 *     <th align="left">required</th>
 *     <th align="left">default</th>
 *     <th align="left">description</th>
 *   </tr>
 *   <tr>
 *     <td valign="top">connection</td>
 *     <td valign="top">yes</td>
 *     <td>&nbsp;</td>
 *     <td valign="top">
 *       Name of the connection registered with 
 *       {@link org.apache.cocoon.components.jms.JMSConnectionManager}. 
 *       This must be a topic connection.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td valign="top">topic</td>
 *     <td valign="top">yes</td>
 *     <td>&nbsp;</td>
 *     <td valign="top">The name of the topic to publish messages to.</td>
 *   </tr>
 *   <tr>
 *     <td valign="top">priority</td>
 *     <td valign="top">no</td>
 *     <td>4</td>
 *     <td valign="top">the priority of the published messages</td>
 *   </tr>
 *   <tr>
 *     <td valign="top">time-to-live</td>
 *     <td valign="top">no</td>
 *     <td>10000</td>
 *     <td valign="top">the message's lifetime in milliseconds</td>
 *   </tr>
 *   <tr>
 *     <td valign="top">persistent-delivery</td>
 *     <td valign="top">no</td>
 *     <td>false</td>
 *     <td valign="top">whether to use persistent delivery mode when publishing messages</td>
 *   </tr>
 *  </tbody>
 * </table>
 * 
 * @version CVS $Id: AbstractMessagePublisher.java 30941 2004-07-29 19:56:58Z vgritsenko $
 */
public abstract class AbstractMessagePublisher extends AbstractLogEnabled
implements Serviceable, Parameterizable, Initializable, Disposable, JMSConnectionEventListener {

    // ---------------------------------------------------- Constants

    private static final String CONNECTION_PARAM = "connection";
    private static final String TOPIC_PARAM = "topic";
    private static final String PRIORITY_PARAM = "priority";
    private static final String TIME_TO_LIVE_PARAM = "time-to-live";
    private static final String PERSISTENT_DELIVERY_PARAM = "persistent-delivery";
    
    private static final int DEFAULT_PRIORITY = 4;
    private static final int DEFAULT_TIME_TO_LIVE = 10000;

    // ---------------------------------------------------- Instance variables

    private ServiceManager m_manager;
    private JMSConnectionManager m_connectionManager;

    protected TopicSession m_session;
    protected TopicPublisher m_publisher;

    protected int m_mode;
    protected int m_priority;
    protected int m_timeToLive;
    protected String m_topicName;
    protected int m_acknowledgeMode;
    protected String m_connectionName;

    // ---------------------------------------------------- Lifecycle

    public AbstractMessagePublisher() {
    }

    public void service(ServiceManager manager) throws ServiceException {
        m_manager = manager;
        m_connectionManager = (JMSConnectionManager) m_manager.lookup(JMSConnectionManager.ROLE);
    }

    public void parameterize(Parameters parameters) throws ParameterException {
        m_connectionName = parameters.getParameter(CONNECTION_PARAM);
        m_topicName = parameters.getParameter(TOPIC_PARAM);
        m_priority = parameters.getParameterAsInteger(PRIORITY_PARAM, DEFAULT_PRIORITY);
        boolean persistent = parameters.getParameterAsBoolean(PERSISTENT_DELIVERY_PARAM, false);
        m_mode = (persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);
        m_timeToLive = parameters.getParameterAsInteger(TIME_TO_LIVE_PARAM, DEFAULT_TIME_TO_LIVE);
    }

    public void initialize() throws Exception {
        if (m_connectionManager instanceof JMSConnectionEventNotifier) {
            ((JMSConnectionEventNotifier) m_connectionManager).addConnectionListener(m_connectionName, this);
        }
        createSessionAndPublisher();
    }

    public void dispose() {
        closePublisherAndSession();
        if (m_manager != null) {
            if (m_connectionManager != null) {
                m_manager.release(m_connectionManager);
            }
        }
    }

    // ---------------------------------------------------- JMSConnectionEventListener

    public void onConnection(String name) {
        if (getLogger().isInfoEnabled()) {
            getLogger().info("Creating publisher because of reconnection");
        }
        try {
            createSessionAndPublisher();
        }
        catch (JMSException e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Reinitialization after reconnection failed", e);
            }
        }
    }

    public void onDisconnection(String name) {
        if (getLogger().isInfoEnabled()) {
            getLogger().info("Closing subscriber because of disconnection");
        }
        closePublisherAndSession();
    }

    // ---------------------------------------------------- Implementation

    /**
     * Concrete classes call this method to publish messages.
     */
    protected synchronized void publishMessage(Message message) throws JMSException {
        // TODO: discover disconnected state and queue messages until connected.
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Publishing message '" + message + "'");
        }
        m_publisher.publish(message, m_mode, m_priority, m_timeToLive);
    }

    private void createSessionAndPublisher() throws JMSException {
        // set the default acknowledge mode
        // concrete implementations may override this
        m_acknowledgeMode = Session.DUPS_OK_ACKNOWLEDGE;

        // create the message publisher
        final TopicConnection connection = (TopicConnection) m_connectionManager.getConnection(m_connectionName);
        if (connection != null) {
            m_session = connection.createTopicSession(false, m_acknowledgeMode);
            final Topic topic = m_session.createTopic(m_topicName);
            m_publisher = m_session.createPublisher(topic);
        }
        else {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Could not obtain JMS connection '" + m_connectionName + "'");
            }
        }
    }

    private void closePublisherAndSession() {
        if (m_publisher != null) {
            try {
                m_publisher.close();
            } catch (JMSException e) {
                getLogger().error("Error closing publisher.", e);
            }
        }
        if (m_session != null) {
            try {
                m_session.close();
            }
            catch (JMSException e) {
                getLogger().warn("Error closing session.", e);
            }
        }
    }

}
