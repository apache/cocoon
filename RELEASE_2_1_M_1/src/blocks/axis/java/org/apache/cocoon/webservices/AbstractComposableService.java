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
 * @version CVS $Id: AbstractComposableService.java,v 1.1 2003/03/09 00:02:28 pier Exp $
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
