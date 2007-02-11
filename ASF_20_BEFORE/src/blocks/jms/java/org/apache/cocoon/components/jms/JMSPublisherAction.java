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
 * @version CVS $Id: JMSPublisherAction.java,v 1.4 2004/02/15 21:30:00 haul Exp $
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
