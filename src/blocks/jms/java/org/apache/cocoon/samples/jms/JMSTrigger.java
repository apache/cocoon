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
 * @version CVS $Id: JMSTrigger.java,v 1.5 2004/02/15 21:30:00 haul Exp $
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