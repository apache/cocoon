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

import java.util.Collections;
import java.util.Map;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TopicPublisher;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

/**
 * Creates new TextMessages containing the event name and publishes them on the
 * {@link org.apache.cocoon.components.jms.JMSConnection}.
 * 
 * <p>Parameters:</p>
 * <table>
 *  <tbody>
 *   <tr><td>connection         </td><td>(required, no default)</td></tr>
 *   <tr><td>priority           </td><td>(4)</td></tr>
 *   <tr><td>time-to-live       </td><td>(10000)</td></tr>
 *   <tr><td>persistent-delivery</td><td>(false)</td></tr>
 *  </tbody>
 * </table>
 * 
 * <p>Sitemap-Parameters:</p>
 * <table>
 *  <tbody>
 *   <tr><td>event              </td><td>Content of TextMessage to publish (required, no default)</td></tr>
 *  </tbody>
 * </table>
 * 
 * @version CVS $Id: JMSPublisherAction.java,v 1.5 2004/03/05 13:01:57 bdelacretaz Exp $
 * @author <a href="mailto:haul@apache.org">haul</a>
 */
public class JMSPublisherAction
    extends AbstractLogEnabled
    implements Action, Serviceable, Parameterizable, ThreadSafe, Initializable, Disposable {

    protected ServiceManager manager = null;
    protected JMSConnection connection = null;
    protected TopicPublisher publisher = null;

    protected int mode = DeliveryMode.NON_PERSISTENT;
    protected boolean persistent = false;
    protected int priority = 4;
    protected int timeToLive = 10000;

    protected String connectionName = null;

    /* 
     * @see org.apache.cocoon.acting.Action#act(org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map act(
        Redirector redirector,
        SourceResolver resolver,
        Map objectModel,
        String source,
        Parameters parameters)
        throws Exception {

        Map result = null;
        Message message = null;
        try {
            // publish message
            message =
                this.connection.getSession().createTextMessage(
                    parameters.getParameter("event"));
            this.publisher.publish(message, this.mode, this.priority, this.timeToLive);
            result = Collections.EMPTY_MAP;
            if (this.getLogger().isInfoEnabled())
                this.getLogger().info("Sent message '"+parameters.getParameter("event")+"'");
        } catch (Exception e) {
            if (this.getLogger().isWarnEnabled())
                this.getLogger().warn("Could not deliver message.", e);
        }

        return result;
    }

    /* 
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /* 
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        this.connectionName = parameters.getParameter("connection");
        this.priority = parameters.getParameterAsInteger("priority", this.priority);
        this.persistent =
            parameters.getParameterAsBoolean("persistent-delivery", this.persistent);
        this.mode =
            (this.persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);
        this.timeToLive =
            parameters.getParameterAsInteger("time-to-live", this.timeToLive);
    }

    /* 
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        this.connection = (JMSConnection) this.manager.lookup(JMSConnection.ROLE + "/"+ this.connectionName);
        this.publisher = this.connection.getPublisher();
    }

    /* 
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.publisher != null) {
            try {
                this.publisher.close();
            } catch (JMSException e) {
                if (this.getLogger().isWarnEnabled()) {
                    this.getLogger().warn("Could not close publisher.", e);
                }
            }
        }
        if (this.connection != null) {
            if (this.manager != null) {
                this.manager.release(this.connection);
                this.connection = null;
            }
        }
    }

}
