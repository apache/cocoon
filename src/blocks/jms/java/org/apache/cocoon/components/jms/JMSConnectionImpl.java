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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * JMSConnection properties container plus utilities.
 * 
 * <p>Parameters:</p>
 * <table>
 *  <tbody>
 *   <tr><td>component      </td><td>(required, null)</td></tr>
 *   <tr><td>scheme         </td><td>(rmi)</td></tr>
 *   <tr><td>host           </td><td>(localhost)</td></tr>
 *   <tr><td>port           </td><td>(for rmi 1099)</td></tr>
 *   <tr><td>jndiname       </td><td>("")</td></tr>
 *   <tr><td>context-factory</td><td>(org.exolab.jms.jndi.InitialContextFactory)</td></tr>
 *   <tr><td>topic-factory  </td><td>(JmsTopicConnectionFactory)</td></tr>
 *   <tr><td>topic          </td><td>(topic1)</td></tr>
 *   <tr><td>ack-mode       </td><td>(dups)</td></tr>
 *  </tbody>
 * </table>
 * 
 * @version CVS $Id: JMSConnectionImpl.java,v 1.1 2003/10/14 16:40:09 haul Exp $
 * @author <a href="mailto:haul@informatik.tu-darmstadt.de">haul</a>
 */
public class JMSConnectionImpl implements Parameterizable, Disposable, ThreadSafe, JMSConnection {

    // defaults to go with OpenJMS demo on localhost 
    protected String contextFactoryName = "org.exolab.jms.jndi.InitialContextFactory";
    protected String scheme = "rmi";
    protected String host = "localhost";
    protected String port = "";
    protected String jndiname = "";
    protected String topicFactoryName = "JmsTopicConnectionFactory";
    protected String topicName = "topic1";
    protected String ackModeName = "dups";
    protected String durableSubscriptionID = null;

    protected TopicConnection connection = null;
    protected TopicSession session = null;
    protected List subscribers = null;
    protected Topic topic = null;
    protected int ackMode = Session.DUPS_OK_ACKNOWLEDGE;
    protected Context context = null;
    protected TopicConnectionFactory topicConnectionFactory = null;


    /**
     * Register a new TopicListener for this connection.
     * 
     * @param listener
     * @param string
     */
    public synchronized void registerListener(
        MessageListener listener,
        String selector)
        throws JMSException, NamingException {

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
     * @return
     * @throws JMSException
     * @throws NamingException
     */
    public TopicPublisher getPublisher() throws JMSException, NamingException {
        return this.getSession().createPublisher(this.topic);
    }

    /**
     * Get the session associated with this connection. This is needed for example to
     * create messages.
     * 
     * @return
     * @throws NamingException
     * @throws JMSException
     */
    public synchronized TopicSession getSession() throws NamingException, JMSException {
        if (this.session == null) {
            this.connect();
        }
        return this.session;
    }

    /**
      * Get initial context.
      * 
      * @return
      * @throws NamingException
      */
    protected Context getContext() throws NamingException {

        Hashtable properties = new Hashtable();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, this.contextFactoryName);

        String name = "";
        if (scheme.equals("rmi")) {
            name = this.jndiname;
        }

        String url = scheme + "://" + host + ":" + port + "/" + name;

        properties.put(Context.PROVIDER_URL, url);
        return new InitialContext(properties);
    }


    /**
     * Setup connection.
     * 
     * @throws NamingException
     * @throws JMSException
     */
    private void setupConnection() throws NamingException, JMSException {
        // setup JMS connection
        this.context = this.getContext();
        this.topicConnectionFactory =
            (TopicConnectionFactory) this.context.lookup(this.topicFactoryName);
        this.connection = this.topicConnectionFactory.createTopicConnection();
        this.connection.start();
    }

    /**
     * Setup session for this connection.
     * 
     * @throws JMSException
     */
    private void setupSession() throws JMSException {
        this.session = connection.createTopicSession(false, this.ackMode);
        this.topic = session.createTopic(this.topicName);
    }

    /**
     * Setup connection and session for this connection.
     * 
     * @throws NamingException
     * @throws JMSException
     */
    private void connect() throws NamingException, JMSException {
        if (this.connection == null)
            this.setupConnection();
        if (this.session == null)
            this.setupSession();
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

    /* 
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) throws ParameterException {

        this.scheme = parameters.getParameter("scheme", this.scheme).toLowerCase();
        if (scheme.equals("tcp") || scheme.equals("tcps")) {
            port = "3035";
        } else if (scheme.equals("http")) {
            port = "8080";
        } else if (scheme.equals("https")) {
            port = "8443";
        } else if (scheme.equals("rmi")) {
            port = "1099";
        }
        this.host = parameters.getParameter("host", this.host);
        this.port = parameters.getParameter("port", this.port);
        this.jndiname = parameters.getParameter("jndiname", this.jndiname);
        this.contextFactoryName =
            parameters.getParameter("context-factory", this.contextFactoryName);
        this.topicFactoryName =
            parameters.getParameter("topic-factory", this.topicFactoryName);
        this.topicName = parameters.getParameter("topic", this.topicName);
        this.durableSubscriptionID =
            parameters.getParameter(
                "durable-subscription-id",
                this.durableSubscriptionID);

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

}
