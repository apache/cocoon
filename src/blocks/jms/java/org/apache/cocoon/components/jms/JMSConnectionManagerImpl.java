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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.cocoon.components.jms.JMSConnectionManager;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * {@link org.apache.cocoon.components.jms.JMSConnectionManager} implementation.
 */
public class JMSConnectionManagerImpl extends AbstractLogEnabled 
implements JMSConnectionManager, Configurable, Initializable, Startable, Disposable, ThreadSafe {

    // ---------------------------------------------------- Constants
    
    private static final int TOPIC_CONNECTION_TYPE = 1;
    private static final int QUEUE_CONNECTION_TYPE = 2;
    private static final int CONNECTION_TYPE = 3;

    private static final String CONNECTION_CONFIG = "connection";
    private static final String TOPIC_CONNECTION_CONFIG = "topic-connection";
    private static final String QUEUE_CONNECTION_CONFIG = "queue-connection";
    private static final String NAME_ATTR = "name";
    
    private static final String CONNECTION_FACTORY_PARAM = "connection-factory";
    private static final String USERNAME_PARAM = "username";
    private static final String PASSWORD_PARAM = "password";
    
    private static final String JNDI_PROPERTY_PREFIX = "java.naming.";

    // ---------------------------------------------------- Instance variables

    private Map m_configurations;
    private Map m_connections;

    // ---------------------------------------------------- Lifecycle

    public JMSConnectionManagerImpl() {
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        m_configurations = new HashMap(configuration.getChildren().length);
        // <connection>s
        Configuration[] configurations = configuration.getChildren(CONNECTION_CONFIG);
        configureConnections(configurations, CONNECTION_TYPE);
        // <topic-connection>s
        configurations = configuration.getChildren(TOPIC_CONNECTION_CONFIG);
        configureConnections(configurations, TOPIC_CONNECTION_TYPE);
        // <queue-connection>s
        configurations = configuration.getChildren(QUEUE_CONNECTION_CONFIG);
        configureConnections(configurations, QUEUE_CONNECTION_TYPE);
    }
    
    private void configureConnections(Configuration[] connections, int type) throws ConfigurationException {
        for (int i = 0; i < connections.length; i++) {
            final String name = connections[i].getAttribute(NAME_ATTR);
            if (m_configurations.containsKey(name)) {
                throw new ConfigurationException("Duplicate connection name '" + name + "'." +
                        " Connection names must be unique.");
            }
            final Parameters parameters = Parameters.fromConfiguration(connections[i]);
            ConnectionConfiguration cc = new ConnectionConfiguration(name, parameters, type);
            m_configurations.put(name, cc);
        }
    }

    public void initialize() throws Exception {
        m_connections = new HashMap(m_configurations.size());
        final Iterator iter = m_configurations.values().iterator();
        try {
            while (iter.hasNext()) {
                final ConnectionConfiguration cc = (ConnectionConfiguration) iter.next();
                final InitialContext context = createInitialContext(cc.getJNDIProperties());
                final ConnectionFactory factory = (ConnectionFactory) context.lookup(cc.getConnectionFactory());
                final Connection connection = createConnection(factory, cc);
                m_connections.put(cc.getName(), connection);
            }
        }
        catch (NamingException e) {
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
        }
        m_configurations = null;
    }

    public void start() throws Exception {
        final Iterator iter = m_connections.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry entry = (Map.Entry) iter.next();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Starting JMS connection " + entry.getKey());
            }
            final Connection connection = (Connection) entry.getValue();
            connection.start();
        }
    }

    public void stop() throws Exception {
        final Iterator iter = m_connections.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry entry = (Map.Entry) iter.next();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Stopping JMS connection " + entry.getKey());
            }
            try {
                final Connection connection = (Connection) entry.getValue();
                connection.stop();
            }
            catch (JMSException e) {
                getLogger().error("Error stopping JMS connection " + entry.getKey(), e);
            }
        }
    }

    public void dispose() {
        final Iterator iter = m_connections.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry entry = (Map.Entry) iter.next();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Closing JMS connection " + entry.getKey());
            }
            try {
                final Connection connection = (Connection) entry.getValue();
                connection.close();
            }
            catch (JMSException e) {
                getLogger().error("Error closing JMS connection " + entry.getKey(), e);
            }
        }
    }

    // ---------------------------------------------------- ConnectionManager

    public Connection getConnection(String name) {
        return (Connection) m_connections.get(name);
    }

    public TopicConnection getTopicConnection(String name) {
        return (TopicConnection) m_connections.get(name);
    }

    public QueueConnection getQueueConnection(String name) {
        return (QueueConnection) m_connections.get(name);
    }

    // ---------------------------------------------------- Implementation

    private InitialContext createInitialContext(Properties properties) throws NamingException {
        if (properties != null) {
            return new InitialContext(properties);
        }
        return new InitialContext();
    }

    private Connection createConnection(ConnectionFactory factory, ConnectionConfiguration cc) throws JMSException {
        if (cc.getUserName() != null) {
            switch (cc.getType()) {
                case CONNECTION_TYPE: {
                    return factory.createConnection(cc.getUserName(), cc.getPassword());
                }
                case TOPIC_CONNECTION_TYPE: {
                    TopicConnectionFactory topicFactory = (TopicConnectionFactory) factory;
                    return topicFactory.createTopicConnection(cc.getUserName(), cc.getPassword());
                }
                case QUEUE_CONNECTION_TYPE: {
                    QueueConnectionFactory queueFactory = (QueueConnectionFactory) factory;
                    return queueFactory.createQueueConnection(cc.getUserName(), cc.getPassword());
                }
            }
        }
        switch (cc.getType()) {
            case CONNECTION_TYPE: {
                return factory.createConnection();
            }
            case TOPIC_CONNECTION_TYPE: {
                TopicConnectionFactory topicFactory = (TopicConnectionFactory) factory;
                return topicFactory.createTopicConnection();
            }
            case QUEUE_CONNECTION_TYPE: {
                QueueConnectionFactory queueFactory = (QueueConnectionFactory) factory;
                return queueFactory.createQueueConnection();
            }
        }
        return null;
    }

    private static final class ConnectionConfiguration {
        
        // ------------------------------------------------ Instance variables

        private final String m_name;
        private final int m_type;
        private final String m_connectionFactory;
        private final String m_username;
        private final String m_password;
        private Properties m_jndiProperties;

        private ConnectionConfiguration(String name, Parameters parameters, int type) 
        throws ConfigurationException {
            m_name = name;
            try {
                m_connectionFactory = parameters.getParameter(CONNECTION_FACTORY_PARAM);
                m_username = parameters.getParameter(USERNAME_PARAM, null);
                m_password = parameters.getParameter(PASSWORD_PARAM, null);
                
                // parse the jndi property parameters
                String[] names = parameters.getNames();
                for (int i = 0; i < names.length; i++) {
                    if (names[i].startsWith(JNDI_PROPERTY_PREFIX)) {
                        if (m_jndiProperties == null) {
                            m_jndiProperties = new Properties();
                        }
                        m_jndiProperties.put(names[i], parameters.getParameter(names[i]));
                    }
                }
                
            }
            catch (ParameterException e) {
                throw new ConfigurationException(e.getLocalizedMessage());
            }
            m_type = type;
        }

        private String getName() {
            return m_name;
        }

        private int getType() {
            return m_type;
        }

        private Properties getJNDIProperties() {
            return m_jndiProperties;
        }

        private String getConnectionFactory() {
            return m_connectionFactory;
        }

        private String getUserName() {
            return m_username;
        }

        private String getPassword() {
            return m_password;
        }

        public int hashCode() {
            return m_name.hashCode();
        }

    }

}
