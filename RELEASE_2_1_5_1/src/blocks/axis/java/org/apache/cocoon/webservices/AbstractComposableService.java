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

import javax.xml.rpc.ServiceException;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.cocoon.components.axis.providers.AvalonProvider;

/**
 * Base class for providing Composable SOAP services.
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
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @version CVS $Id: AbstractComposableService.java,v 1.2 2004/03/05 13:01:43 bdelacretaz Exp $
 */
public abstract class AbstractComposableService
    extends AbstractLogEnabledService
    implements Composable {
	    
    // component manager reference
    protected ComponentManager m_manager;

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
            setComponentManager();

        } catch (ComponentException e) {
            throw new ServiceException("ComponentException generated", e);
        }
    }

    /**
     * Compose this service.
     *
     * @param manager a <code>ComponentManager</code> instance
     * @exception ComponentException if an error occurs
     */
    public void compose(final ComponentManager manager) throws ComponentException {
        m_manager = manager;
    }

    /**
     * Helper method to extract the ComponentManager reference
     * from the context.
     * @exception ComponentException if an error occurs
     */
    private void setComponentManager() throws ComponentException {
        compose(
            (ComponentManager) m_context.getProperty(
                AvalonProvider.COMPONENT_MANAGER
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
