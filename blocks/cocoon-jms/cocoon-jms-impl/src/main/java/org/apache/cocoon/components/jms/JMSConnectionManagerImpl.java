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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.cron.CronJob;
import org.apache.cocoon.components.cron.JobScheduler;

/**
 * {@link org.apache.cocoon.components.jms.JMSConnectionManager} implementation.
 */
public class JMSConnectionManagerImpl extends AbstractLogEnabled 
implements JMSConnectionManager, Serviceable, Configurable, Initializable,
           Startable, Disposable, ThreadSafe, JMSConnectionEventNotifier {

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
    private static final String AUTO_RECONNECT_PARAM = "auto-reconnect";
    private static final String AUTO_RECONNECT_DELAY_PARAM = "auto-reconnect-delay";
    
    private static final int DEFAULT_AUTO_RECONNECT_DELAY = 1000;
    
    private static final String JNDI_PROPERTY_PREFIX = "java.naming.";

    // ---------------------------------------------------- Instance variables

    private ServiceManager m_serviceManager;
    
    private Map m_configurations;
    private Map m_connections;
    private Map m_listeners;

    // ---------------------------------------------------- Lifecycle

    public JMSConnectionManagerImpl() {
    }
    
    public void service(ServiceManager manager) {
        m_serviceManager = manager;
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
        m_listeners = new HashMap();
        m_connections = new HashMap(m_configurations.size());
        final Iterator iter = m_configurations.values().iterator();

        while (iter.hasNext()) {
            final ConnectionConfiguration cc = (ConnectionConfiguration) iter.next();
            try {
                final Connection connection = createConnection(cc);
                
                m_connections.put(cc.getName(), connection);
            }
            catch (NamingException e) {
                // ignore, warnings for NamingExceptions are logged by createConnection method
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
            stopConnection((String) entry.getKey(), (Connection) entry.getValue());
        }
    }

    void stopConnection(String name, Connection connection) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Stopping JMS connection " + name);
        }
        try {
            connection.stop();
        }
        catch (JMSException e) {
            // ignore
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

    public synchronized Connection getConnection(String name) {
        return (Connection) m_connections.get(name);
    }

    public synchronized TopicConnection getTopicConnection(String name) {
        return (TopicConnection) m_connections.get(name);
    }

    public synchronized QueueConnection getQueueConnection(String name) {
        return (QueueConnection) m_connections.get(name);
    }

    // ---------------------------------------------------- JMSConnectionEventNotifier
    
    public synchronized void addConnectionListener(String name, JMSConnectionEventListener listener) {
       Set connectionListeners = (Set) m_listeners.get(name);
       if (connectionListeners == null) {
           connectionListeners = new HashSet();
           m_listeners.put(name, connectionListeners);
       }
       connectionListeners.add(listener);
    }

    public synchronized void removeConnectionListener(String name, JMSConnectionEventListener listener) {
        Set connectionListeners = (Set) m_listeners.get(name);
        if (connectionListeners != null) {
            connectionListeners.remove(listener);
        }
     }

    // ---------------------------------------------------- Implementation

    Connection createConnection(ConnectionConfiguration cc) throws NamingException, JMSException {
        try {
            final InitialContext context = createInitialContext(cc.getJNDIProperties());
            final ConnectionFactory factory = (ConnectionFactory) context.lookup(cc.getConnectionFactory());
            final Connection connection = createConnection(factory, cc);
            if (cc.isAutoReconnect()) {
                connection.setExceptionListener(new ReconnectionListener(this, cc));
            }
            return connection;
        }
        catch (NamingException e) {
            if (getLogger().isWarnEnabled()) {
                final Throwable rootCause = e.getRootCause();
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
            throw e;
        }
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

    private InitialContext createInitialContext(Properties properties) throws NamingException {
        if (properties != null) {
            return new InitialContext(properties);
        }
        return new InitialContext();
    }
    
    synchronized void removeConnection(String name) {
        notifyListenersOfDisconnection(name);
        final Connection connection = (Connection) m_connections.remove(name);
        stopConnection(name, connection);
    }
    
    synchronized void addConnection(String name, Connection connection) {
        m_connections.put(name, connection);
        notifyListenersOfConnection(name);
    }
    
    void scheduleReconnectionJob(ConnectionConfiguration configuration) {
        if (getLogger().isInfoEnabled()) {
            getLogger().info("Scheduling JMS reconnection job for: " + configuration.getName());
        }
        JobScheduler scheduler = null;
        try {
            scheduler = (JobScheduler) m_serviceManager.lookup(JobScheduler.ROLE);
            Date executionTime = new Date(System.currentTimeMillis() + configuration.getAutoReconnectDelay());
            ReconnectionJob job = new ReconnectionJob(this, configuration);
            scheduler.fireJobAt(executionTime, "reconnect_" + configuration.getName(), job);
        }
        catch (ServiceException e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Cannot obtain scheduler.",e);
            }
        }
        catch (CascadingException e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Unable to schedule reconnection job.",e);
            }
        }
        finally {
            if (scheduler != null) {
                m_serviceManager.release(scheduler);
            }
        }
    }
    
    private void notifyListenersOfConnection(String name) {
        Set connectionListeners = (Set) m_listeners.get(name);
        if (connectionListeners != null) {
            for (Iterator listenersIterator = connectionListeners.iterator(); listenersIterator.hasNext();) {
                JMSConnectionEventListener listener = (JMSConnectionEventListener) listenersIterator.next();
                listener.onConnection(name);
            }
        }
    }
    
    private void notifyListenersOfDisconnection(String name) {
        Set connectionListeners = (Set) m_listeners.get(name);
        if (connectionListeners != null) {
            for (Iterator listenersIterator = connectionListeners.iterator(); listenersIterator.hasNext();) {
                JMSConnectionEventListener listener = (JMSConnectionEventListener) listenersIterator.next();
                listener.onDisconnection(name);
            }
        }
    }

    static final class ConnectionConfiguration {
        
        // ------------------------------------------------ Instance variables

        private final String m_name;
        private final int m_type;
        private final String m_connectionFactory;
        private final String m_username;
        private final String m_password;
        private final boolean m_autoReconnect;
        private final int m_autoReconnectDelay;
        
        private Properties m_jndiProperties = new Properties();

        ConnectionConfiguration(String name, Parameters parameters, int type) 
        throws ConfigurationException {
            m_name = name;
            try {
                m_connectionFactory = parameters.getParameter(CONNECTION_FACTORY_PARAM);
                m_username = parameters.getParameter(USERNAME_PARAM, null);
                m_password = parameters.getParameter(PASSWORD_PARAM, null);
                m_autoReconnect = parameters.getParameterAsBoolean(AUTO_RECONNECT_PARAM, false);
                m_autoReconnectDelay = parameters.getParameterAsInteger(AUTO_RECONNECT_DELAY_PARAM, DEFAULT_AUTO_RECONNECT_DELAY);
                
                // parse the jndi property parameters
                String[] names = parameters.getNames();
                for (int i = 0; i < names.length; i++) {
                    if (names[i].startsWith(JNDI_PROPERTY_PREFIX)) {
                        m_jndiProperties.put(names[i], parameters.getParameter(names[i]));
                    }
                }
            }
            catch (ParameterException e) {
                throw new ConfigurationException(e.getLocalizedMessage());
            }
            m_type = type;
        }

        String getName() {
            return m_name;
        }

        int getType() {
            return m_type;
        }

        Properties getJNDIProperties() {
            return m_jndiProperties;
        }

        String getConnectionFactory() {
            return m_connectionFactory;
        }

        String getUserName() {
            return m_username;
        }

        String getPassword() {
            return m_password;
        }
        
        boolean isAutoReconnect() {
            return m_autoReconnect;
        }
        
        int getAutoReconnectDelay() {
            return m_autoReconnectDelay;
        }

        public int hashCode() {
            return m_name.hashCode();
        }

    }

    static final class ReconnectionListener implements ExceptionListener {

        private final JMSConnectionManagerImpl m_manager;
        private final ConnectionConfiguration m_configuration;
        
        ReconnectionListener(JMSConnectionManagerImpl manager, ConnectionConfiguration configuration) {
            super();
            m_manager = manager;
            m_configuration = configuration;
        }

        public void onException(JMSException exception) {
            m_manager.removeConnection(m_configuration.getName());
            m_manager.scheduleReconnectionJob(m_configuration);
        }

    }

    static final class ReconnectionJob implements CronJob {

        private final JMSConnectionManagerImpl m_manager;        
        private final ConnectionConfiguration m_configuration;

        ReconnectionJob(JMSConnectionManagerImpl manager, ConnectionConfiguration configuration) {
            super();
            m_manager = manager;
            m_configuration = configuration;
        }
        
        public void execute(String jobname) {
            final Logger logger = m_manager.getLogger();
            if (logger.isInfoEnabled()) {
                logger.info("Reconnecting JMS connection: " + m_configuration.getName());
            }
            try {
                final Connection connection = m_manager.createConnection(m_configuration);
                m_manager.addConnection(m_configuration.getName(), connection);
                if (logger.isInfoEnabled()) {
                    logger.info("Successfully reconnected JMS connection: " + m_configuration.getName());
                }
            }
            catch (NamingException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to reconnect.",e);
                }
                m_manager.scheduleReconnectionJob(m_configuration);
            }
            catch (JMSException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to reconnect.",e);
                }
                m_manager.scheduleReconnectionJob(m_configuration);
            }
        }
    }

}
