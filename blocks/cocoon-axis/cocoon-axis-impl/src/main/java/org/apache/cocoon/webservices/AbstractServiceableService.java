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

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.axis.providers.AvalonProvider;

/**
 * Base class for providing Serviceable SOAP services.
 *
 * <p>
 *  Note, this class is intended to be used in SOAP Services that require
 *  references to Component's provided by the Cocoon Component Manager.
 * </p>
 *
 * <p>
 *  If you require full Avalon support in your SOAP Service, consider using
 *  the AvalonProvider support built into Axis itself.
 * </p>
 *
 * @version $Id$
 */
public abstract class AbstractServiceableService
        extends AbstractLogEnabledService
        implements Serviceable {
    
    // service manager reference
    protected ServiceManager m_manager;

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
        super.init(context);

        try {
            setServiceManager();

        } catch (org.apache.avalon.framework.service.ServiceException e) {
            throw new ServiceException("ServiceException generated", e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(final ServiceManager manager) 
    throws org.apache.avalon.framework.service.ServiceException {
        m_manager = manager;
    }

    /**
     * Helper method to extract the ServiceManager reference
     * from the context.
     * @exception org.apache.avalon.framework.service.ServiceException if an error occurs
     */
    private void setServiceManager() throws org.apache.avalon.framework.service.ServiceException {
        service(
            (ServiceManager) m_context.getProperty(
                AvalonProvider.SERVICE_MANAGER
            )
        );
    }

    /**
     * Called by the JAX-RPC runtime to signal the end of this service
     */
    public void destroy() {
        super.destroy();

        m_manager = null;
    }
}
