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

import java.util.Hashtable;

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
public class JMSEventCacheContentInterceptor
    extends AbstractContentInterceptor {

    private TopicConnection m_connection;
    private TopicSession m_session;
    private Topic m_topic;
    private TopicPublisher m_publisher;
    
    private int m_deliveryMode;
    private int m_priority;
    private long m_timeToLive;
    
    private boolean started = false;
    
    public JMSEventCacheContentInterceptor() {
    }
    
    /* (non-Javadoc)
     * @see org.apache.slide.content.ContentInterceptor#setParameters(java.util.Hashtable)
     */
    public void setParameters(Hashtable params) {
        super.setParameters(params);
        
        // parse the JMS related parameters
        String topicFactoryName = getParameter("topic-factory","JmsTopicConnectionFactory");
        String topicName = getParameter("topic","topic1");
        String ackModeName = getParameter("ack-mode","dups");
        boolean persistent = Boolean.valueOf(getParameter("persistent-delivery","false")).booleanValue();
        
        m_deliveryMode = persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
        m_priority = Integer.valueOf(getParameter("priority","4")).intValue();
        m_timeToLive = Long.valueOf(getParameter("time-to-live","1000")).longValue();
        
        // see if an ack mode has been specified. If it hasn't
        // then assume CLIENT_ACKNOWLEDGE mode.
        int ackMode = Session.CLIENT_ACKNOWLEDGE;
        if (ackModeName.equals("auto")) {
            ackMode = Session.AUTO_ACKNOWLEDGE;
        } else if (ackModeName.equals("dups")) {
            ackMode = Session.DUPS_OK_ACKNOWLEDGE;
        } else if (!ackModeName.equals("client")) {
            // ignore all ack modes, to test no acking
            ackMode = -1;
        }
        
        // parse the JNDI related parameters
        Hashtable props = new Hashtable();
        props.put(Context.INITIAL_CONTEXT_FACTORY,
                  getParameter(Context.INITIAL_CONTEXT_FACTORY,
                               "org.exolab.jms.jndi.InitialContextFactory")
        );
        props.put(Context.PROVIDER_URL,
                  getParameter(Context.PROVIDER_URL,
                               "rmi://localhost:1099/")
        );
        
        // setup the JMS connection and session
        try {
            Context context = new InitialContext(props);
            TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) 
                context.lookup(topicFactoryName);
            m_connection = topicConnectionFactory.createTopicConnection();
            m_connection.start();
            m_session = m_connection.createTopicSession(false,ackMode);
            m_topic = m_session.createTopic(topicName);
            m_publisher = m_session.createPublisher(m_topic);
            started = true;
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    
    private String getParameter(String name, String defaultValue) {
        String value = super.getParameter(name);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }
    
    /* (non-Javadoc)
     * @see org.apache.slide.content.ContentInterceptor#postRemoveContent(org.apache.slide.common.SlideToken, org.apache.slide.content.NodeRevisionDescriptors, org.apache.slide.content.NodeRevisionDescriptor)
     */
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
       
       if (!started) {
           return;
       }
       if (revisions == null) {
           return;
       }
       try {
           sendMessage(revisions.getUri());
       }
       catch (JMSException e) {
           e.printStackTrace();
       }
    }

    /* (non-Javadoc)
     * @see org.apache.slide.content.ContentInterceptor#postStoreContent(org.apache.slide.common.SlideToken, org.apache.slide.content.NodeRevisionDescriptors, org.apache.slide.content.NodeRevisionDescriptor, org.apache.slide.content.NodeRevisionContent)
     */
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
        
        if (!started) {
            return;
        }
        if (revisions == null) {
            return;
        }
        try {
            sendMessage(revisions.getUri());
        }
        catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String uri) throws JMSException {
        String msg = "interceptor|" + getNamespace().getName() + uri;
        if (getLogger().isEnabled(Logger.INFO)) {
            getLogger().log("Sending message: " + msg,Logger.INFO);
        }
        m_publisher.publish(m_session.createTextMessage(msg),m_deliveryMode,m_priority,m_timeToLive);
    }
    
    private Logger getLogger() {
        return getNamespace().getLogger();
    }
}
