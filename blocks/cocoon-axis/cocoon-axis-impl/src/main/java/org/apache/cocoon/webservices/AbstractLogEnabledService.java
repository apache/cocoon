/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.cocoon.util.AbstractLogEnabled;

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
 * @version $Id$
 */
public abstract class AbstractLogEnabledService extends AbstractLogEnabled
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
     * Called by the JAX-RPC runtime to signal the end of this service
     */
    public void destroy() {
        m_context = null;
    }
}
