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
package org.apache.cocoon.webservices;

import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;
import javax.xml.rpc.ServiceException;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.apache.cocoon.components.axis.SoapServer; // or use Constants ?

/**
 * Base class for providing LogEnabled SOAP services.
 *
 * <p>
 *  Note, this class is intended to be used for SOAP Services that require
 *  accessing to a logging object for reporting purposes only.
 * </p>
 *
 * <p>
 *  If you require full Avalon support for your SOAP Service, then consider
 *  using the AvalonProvider support built into Axis itself.
 * </p>
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @version CVS $Id: AbstractLogEnabledService.java,v 1.2 2004/03/05 13:01:43 bdelacretaz Exp $
 */
public abstract class AbstractLogEnabledService
    extends AbstractLogEnabled
    implements ServiceLifecycle {
	    
    // servlet endpoint context reference
    protected ServletEndpointContext m_endPointContext;

    // message context reference
    protected MessageContext m_context;

    /**
     * ServiceLifecycle <code>init</code> method. Updates an internal
     * reference to the given context object, and enables logging for
     * this service.
     *
     * @param context a javax.xml.rpc.ServiceLifecycle context
     *                <code>Object</code> instance
     * @exception ServiceException if an error occurs
     */
    public void init(final Object context) throws ServiceException {
        setContext(context);
        setLogger();
    }

    /**
     * Helper method to set the internal context reference for future
     * use.
     *
     * @param context a javax.xml.rpc.ServiceLifecycle context
     *                <code>Object</code> instance
     * @exception ServiceException if an error occurs
     */
    private void setContext(final Object context) throws ServiceException {
        try {
            m_endPointContext = (ServletEndpointContext) context;

        } catch (final ClassCastException e) {
            throw new ServiceException(
                "Service requires ServletEndPointContext, supplied was " + context, e
            );
        }

        m_context = m_endPointContext.getMessageContext();
    }

    /**
     * Helper method to obtain the Avalon <code>Logger</code> object out of 
     * the context object and enable logging for this service.
     */
    private void setLogger() {
        enableLogging((Logger) m_context.getProperty(SoapServer.LOGGER));
    }

    /**
     * Called by the JAX-RPC runtime to signal the end of this service
     */
    public void destroy() {
        m_context = null;
    }
}
