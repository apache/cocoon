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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.NamingException;

import org.apache.avalon.framework.CascadingException;
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
import org.apache.cocoon.caching.EventAware;
import org.apache.cocoon.caching.validity.NamedEvent;

/**
 * JMS listener will notify an {@link org.apache.cocoon.caching.EventAware} component
 * of external events. This could be used for example to do external cache invalidation.
 * Configuration parameters default to <a href="http://openjms.sf.net">OpenJMS</a> demo
 * installation on localhost using "topic1".
 * 
 * <p>Parameters:</p>
 * <table>
 *  <tbody>
 *   <tr><td>connection      </td><td>(required, no default)</td></tr>
 *   <tr><td>component       </td><td>(required, no default)</td></tr>
 *   <tr><td>message-selector</td><td>("")</td></tr>
 *  </tbody>
 * </table>
 * 
 * @version CVS $Id: JMSEventListener.java,v 1.4 2003/10/20 07:19:33 cziegeler Exp $
 * @author <a href="mailto:chaul@informatik.tu-darmstadt.de">chaul</a>
 */
public class JMSEventListener
    extends AbstractLogEnabled
    implements Serviceable, Parameterizable, MessageListener, Initializable, Disposable, ThreadSafe {

    protected String selector = "";

    protected ServiceManager manager;

    protected String serviceName = null;
    protected EventAware service = null;
    protected String connectionName = null;
    protected JMSConnection connection = null;


    public void initialize() {

        try {
			this.connection = (JMSConnection) this.manager.lookup(JMSConnection.ROLE+"/"+this.connectionName);
            this.connection.registerListener(this, this.selector);
        } catch (ServiceException e) {
			if (getLogger().isWarnEnabled()) {
                getLogger().warn("Could not obtain JMSConnection");
			}
		} catch (JMSException e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Could not obtain JMSConnection");
            }
		} catch (NamingException e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Could not obtain JMSConnection");
            }
		} catch (CascadingException e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Could not obtain JMSConnection");
            }
		}
    }

        /* 
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) throws ParameterException {

        this.connectionName = parameters.getParameter("connection");
        this.serviceName = parameters.getParameter("component");
        this.selector = parameters.getParameter("message-selector", this.selector);
    }

    /* 
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public synchronized void onMessage(Message message) {
        EventAware service = this.service;
        try {
            if (service == null) {
                service = (EventAware) this.manager.lookup(this.serviceName);
            }
            if (this.getLogger().isInfoEnabled())
                this.getLogger().info(
                    "Notifying "
                        + this.serviceName
                        + " of "
                        + this.convertMessage(message.toString()));
            service.processEvent(new NamedEvent(this.convertMessage(message.toString())));
        } catch (ServiceException e) {
            if (this.getLogger().isErrorEnabled()) {
                this.getLogger().error(
                    "Could not obtain " + this.serviceName + " from component manager.",
                    e);
            }
        } finally {
            if (service instanceof ThreadSafe) {
                this.service = service;
            } else {
                this.manager.release(service);
            }
        }
    }

    /**
     * Convert the message contents to a cache key. Assume that the message contains of
     * the trigger name, a '|', and the table name. Extract the tablename only. You might
     * want to override this method.
     * 
     * @param message
     * @return
     */
    protected String convertMessage(String message) {
        int pos = message.indexOf('|');
        return message.substring(pos + 1);
    }

    /*
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.activity.Disposable#dispose()
	 */
	public void dispose() {
        if (this.manager != null){
            this.manager.release(connection);
            this.manager.release(service);
        }
	}

}
