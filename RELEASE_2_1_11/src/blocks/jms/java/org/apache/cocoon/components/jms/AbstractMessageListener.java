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

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
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
 * Abstract {@link javax.jms.MessageListener} implementation. 
 * Use this as a basis for concrete MessageListener implementations. 
 * When used in conjunction with the default {@link org.apache.cocoon.components.jms.JMSConnectionManager} 
 * implementation this class supports automatic reconnection when the connection gets severed.
 * 
 * <p>Parameters:</p>
 * <table border="1">
 *  <tbody>
 *   <tr>
 *     <th align="left">parameter</th>
 *     <th align="left">required/default</th>
 *     <th align="left">description</th>
 *   </tr>
 *   <tr>
 *     <td valign="top">connection</td>
 *     <td valign="top">required</td>
 *     <td valign="top">
 *       Name of the connection registered with 
 *       {@link org.apache.cocoon.components.jms.JMSConnectionManager}. 
 *       This must be a topic connection.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>topic</td>
 *     <td>required</td>
 *     <td>The name of the topic to subscribe to.</td>
 *   </tr>
 *   <tr>
 *     <td>subscription-id</td>
 *     <td>(<code>null</code>)</td>
 *     <td>An optional durable subscription id.</td>
 *   </tr>
 *   <tr>
 *     <td>message-selector</td>
 *     <td>(<code>null</code>)</td>
 *     <td>An optional message selector.</td>
 *   </tr>
 *  </tbody>
 * </table>
 * 
 * @version CVS $Id: AbstractMessageListener.java 30941 2004-07-29 19:56:58Z vgritsenko $
 */
public abstract class AbstractMessageListener extends AbstractLogEnabled
implements MessageListener, Serviceable, Parameterizable, Initializable, Disposable,
           JMSConnectionEventListener {

    // ---------------------------------------------------- Constants

    private static final String CONNECTION_PARAM = "connection";
    private static final String TOPIC_PARAM = "topic";
    private static final String SUBSCRIPTION_ID_PARAM = "subscription-id";
    private static final String MESSAGE_SELECTOR_PARAM = "message-selector";

    // ---------------------------------------------------- Instance variables

    protected ServiceManager m_manager;

    /* configuration */
    protected String m_connectionName;
    protected String m_topicName;
    protected String m_subscriptionId;
    protected String m_selector;
    protected int m_acknowledgeMode;

    /* connection manager component */
    private JMSConnectionManager m_connectionManager;

    /* our session */
    private TopicSession m_session;

    /* our subscriber */
    private TopicSubscriber m_subscriber;

    // ---------------------------------------------------- Lifecycle

    public AbstractMessageListener () {
    }

    public void service(ServiceManager manager) throws ServiceException {
        m_manager = manager;
        m_connectionManager = (JMSConnectionManager) m_manager.lookup(JMSConnectionManager.ROLE);
    }

    public void parameterize(Parameters parameters) throws ParameterException {

        m_connectionName = parameters.getParameter(CONNECTION_PARAM);
        m_topicName = parameters.getParameter(TOPIC_PARAM);

        m_subscriptionId = parameters.getParameter(SUBSCRIPTION_ID_PARAM, null);
        m_selector = parameters.getParameter(MESSAGE_SELECTOR_PARAM, null);

    }

    /**
     * Registers this MessageListener as a TopicSubscriber to the configured Topic.
     * @throws Exception
     */
    public void initialize() throws Exception {
        if (m_connectionManager instanceof JMSConnectionEventNotifier) {
            ((JMSConnectionEventNotifier) m_connectionManager).addConnectionListener(m_connectionName, this);
        }
        createSessionAndSubscriber();
    }

    public void dispose() {
        closeSubscriberAndSession();
        m_manager.release(m_connectionManager);
    }

    public void onConnection(String name) {
        if (getLogger().isInfoEnabled()) {
            getLogger().info("Creating subscriber because of reconnection");
        }
        try {
            createSessionAndSubscriber();
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
        closeSubscriberAndSession();
    }

    private void createSessionAndSubscriber() throws JMSException {
        // set the default acknowledge mode to dups
        // concrete implementations may want to override this
        m_acknowledgeMode = Session.DUPS_OK_ACKNOWLEDGE;

        // register this MessageListener with a TopicSubscriber
        final TopicConnection connection = (TopicConnection) m_connectionManager.getConnection(m_connectionName);
        if (connection != null) {
            m_session = connection.createTopicSession(false, m_acknowledgeMode);
            final Topic topic = m_session.createTopic(m_topicName);
            if (m_subscriptionId != null) {
                m_subscriber = m_session.createDurableSubscriber(topic, m_subscriptionId, m_selector, false);
            }
            else {
                m_subscriber = m_session.createSubscriber(topic, m_selector, false);
            }
            m_subscriber.setMessageListener(this);
            // recover in case of reconnection
            m_session.recover();
        }
        else {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Could not obtain JMS connection '" + m_connectionName + "'");
            }
        }
    }

    private void closeSubscriberAndSession() {
        if (m_subscriber != null) {
            try {
                m_subscriber.close();
            } catch (JMSException e) {
                getLogger().error("Error closing subscriber", e);
            }
            finally {
                m_subscriber = null;
            }
        }
        if (m_session != null) {
            try {
                m_session.close();
            }
            catch (JMSException e) {
                getLogger().error("Error closing session", e);
            }
            finally {
                m_session = null;
            }
        }
    }
}
