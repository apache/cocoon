/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.slide.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.slide.common.NamespaceAccessToken;
import org.apache.slide.common.ServiceAccessException;
import org.apache.slide.common.SlideToken;
import org.apache.slide.content.AbstractContentInterceptor;
import org.apache.slide.content.NodeRevisionContent;
import org.apache.slide.content.NodeRevisionDescriptor;
import org.apache.slide.content.NodeRevisionDescriptors;
import org.apache.slide.lock.ObjectLockedException;
import org.apache.slide.security.AccessDeniedException;
import org.apache.slide.structure.LinkedObjectNotFoundException;
import org.apache.slide.structure.ObjectNotFoundException;
import org.apache.slide.util.logger.Logger;

/**
 * A ContentInterceptor for Slide that publishes 
 * invalidation events to a JMS topic.
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a> 
 */
public class JMSContentInterceptor extends AbstractContentInterceptor {

    
    // ---------------------------------------------------- constants
    
    private static final String LOG_CHANNEL = "JMSContentInterceptor";
    
    private static final String PARAM_TOPIC_FACTORY = "topic-factory";
    private static final String PARAM_TOPIC = "topic";
    private static final String PARAM_PERSISTENT = "persistent-delivery";
    private static final String PARAM_PRIORITY = "priority";
    private static final String PARAM_TIME_TO_LIVE = "time-to-live";
    private static final String PARAM_INITIAL_CONTEXT_FACTORY = Context.INITIAL_CONTEXT_FACTORY;
    private static final String PARAM_PROVIDER_URL = Context.PROVIDER_URL;
    
    private static final String DEFAULT_TOPIC_FACTORY = "JmsTopicConnectionFactory";
    private static final String DEFAULT_TOPIC = "topic1";
    private static final String DEFAULT_PERSISTENT = "false";
    private static final String DEFAULT_PRIORITY = "4";
    private static final String DEFAULT_TIME_TO_LIVE = "1000";
    private static final String DEFAULT_INITIAL_CONTEXT_FACTORY = "org.exolab.jms.jndi.InitialContextFactory";
    private static final String DEFAULT_PROVIDER_URL = "rmi://localhost:1099/";
    
    
    // ---------------------------------------------------- member variables
    
    // JMS objects
    private TopicConnection m_connection;
    private TopicSession m_session;
    private Topic m_topic;
    private TopicPublisher m_publisher;
    
    // configuration options
    private String m_topicFactoryName;
    private String m_topicName;
    private int m_deliveryMode;
    private int m_priority;
    private long m_timeToLive;
    private Hashtable m_jndiProps;
    
    // queue of messages to be published
    private List m_queue = Collections.synchronizedList(new ArrayList());    
    
    // the started state of the interceptor
    private boolean m_started = false;
    
    
    // ---------------------------------------------------- lifecycle

    public JMSContentInterceptor() {
    }
    
    /**
     * Configure the interceptor.
     * <p>
     *  The following parameters are recognized:
     *  <ul>
     *   <li>
     *    <code>java.naming.factory.initial</code> [<code>org.exolab.jms.jndi.InitialContextFactory</code>]
     *          - initial jndi context factory.
     *   </li>
     *   <li>
     *    <code>java.naming.provider.url</code> [<code>rmi://localhost:1099/</code>] - jndi provider url.
     *   </li>
     *   <li>
     *    <code>topic-factory<code> [<code>JmsTopicConnectionFactory</code>] - the JNDI lookup name
     *    of the JMS TopicConnectionFactory.
     *   </li>
     *   <li>
     *    <code>topic</code> [<code>topic1</code>] - the name of the topic to publish messages to.
     *   </li>
     *   <li>
     *    <code>persistent-delivery</code> [<code>false</code>] - message delivery mode.
     *   </li>
     *   <li>
     *    <code>priority</code> [<code>4</code>] - message priority.
     *   </li>
     *   <li>
     *    <code>time-to-live</code> [<code>1000</code>] - message lifetime in ms.
     *   </li>
     *  </ul>
     * </p>
     */
    public void setParameters(Hashtable params) {
        super.setParameters(params);
        
        // parse the JMS related parameters
        m_topicFactoryName = getParameter(PARAM_TOPIC_FACTORY,DEFAULT_TOPIC_FACTORY);
        m_topicName = getParameter(PARAM_TOPIC,DEFAULT_TOPIC);
        boolean persistent = Boolean.valueOf(getParameter(PARAM_PERSISTENT,DEFAULT_PERSISTENT)).booleanValue();
        
        m_deliveryMode = persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
        m_priority = Integer.valueOf(getParameter(PARAM_PRIORITY,DEFAULT_PRIORITY)).intValue();
        m_timeToLive = Long.valueOf(getParameter(PARAM_TIME_TO_LIVE,DEFAULT_TIME_TO_LIVE)).longValue();
        
        // parse the JNDI related parameters
        m_jndiProps = new Hashtable();
        m_jndiProps.put(Context.INITIAL_CONTEXT_FACTORY,
                  getParameter(PARAM_INITIAL_CONTEXT_FACTORY,
                               DEFAULT_INITIAL_CONTEXT_FACTORY)
        );
        m_jndiProps.put(Context.PROVIDER_URL,
                  getParameter(PARAM_PROVIDER_URL,
                               DEFAULT_PROVIDER_URL)
        );
    }
    
    /**
     * Sets up the JMS connection.
     */
    public void setNamespace(NamespaceAccessToken nat) {
        super.setNamespace(nat);
        
        // setup the JMS connection and session
        try {
            Context context = new InitialContext(m_jndiProps);
            TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) 
                context.lookup(m_topicFactoryName);
            m_connection = topicConnectionFactory.createTopicConnection();
            m_connection.start();
            m_session = m_connection.createTopicSession(false,Session.DUPS_OK_ACKNOWLEDGE);
            m_topic = m_session.createTopic(m_topicName);
            m_publisher = m_session.createPublisher(m_topic);
            
            /* a background thread does the actual publishing
             * of messages. This is because JMS Session
             * and its derivatives are single threaded
             * and so if we were to publish messages synchronously
             * we'd have to create a new Session 
             * each time we need to publish a message.
             */ 
            Thread t = new Thread(new Runnable() {
                public void run() {
                    m_started = true;
                    while (m_started) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // continue
                        }
                        if (m_queue.size() == 0) continue;
                        List list = m_queue;
                        m_queue = Collections.synchronizedList(new ArrayList());
                        Iterator iter = list.iterator();
                        while (iter.hasNext()) {
                            String msg = (String) iter.next();
                            if (getLogger().isEnabled(Logger.INFO)) {
                                getLogger().log("Sending message: " + msg,Logger.INFO);
                            }
                            try {
                                m_publisher.publish(m_session.createTextMessage(msg),
                                    m_deliveryMode,m_priority,m_timeToLive);
                            }
                            catch (JMSException e) {
                                getLogger().log("Failure sending JMS message.",e,LOG_CHANNEL,Logger.ERROR);
                            }
                        }
                    }
                }
            });
            t.setPriority(Thread.NORM_PRIORITY);
            t.start();
        } catch (NamingException e) {
            getLogger().log("Failure while connecting to JMS server.",e,LOG_CHANNEL,Logger.ERROR);
        } catch (JMSException e) {
            getLogger().log("Failure while connecting to JMS server.",e,LOG_CHANNEL,Logger.ERROR);
        }

    }
    
    private String getParameter(String name, String defaultValue) {
        String value = super.getParameter(name);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }
    
    
    // ---------------------------------------------------- interception methods
    
    public void postRemoveContent(
        SlideToken slideToken,
        NodeRevisionDescriptors revisions,
        NodeRevisionDescriptor revision)
        throws
            AccessDeniedException,
            ObjectNotFoundException,
            LinkedObjectNotFoundException,
            ObjectLockedException,
            ServiceAccessException {
       
       if (!m_started) {
           return;
       }
       if (revisions == null) {
           return;
       }
       queueMessage(revisions.getUri(),"remove");

    }
    
    public void postStoreContent(
        SlideToken slideToken,
        NodeRevisionDescriptors revisions,
        NodeRevisionDescriptor revision,
        NodeRevisionContent content)
        throws
            AccessDeniedException,
            ObjectNotFoundException,
            LinkedObjectNotFoundException,
            ObjectLockedException,
            ServiceAccessException {
        
        if (!m_started) {
            return;
        }
        if (revisions == null) {
            return;
        }
        queueMessage(revisions.getUri(),"store");
    }
    
    private void queueMessage(String uri, String type) {
        String msg = "slide-interceptor:" + type + "|" + getNamespace().getName() + uri;
        m_queue.add(msg);
    }
    
    private Logger getLogger() {
        return getNamespace().getLogger();
    }
}
