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
package org.apache.cocoon.components.container;

import org.apache.avalon.fortress.impl.handler.AbstractComponentHandler;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.GlobalRequestLifecycleComponent;
import org.apache.cocoon.components.RequestLifecycleComponent;
import org.apache.excalibur.mpool.Pool;
import org.apache.excalibur.mpool.PoolManager;


/**
 * RequestLifestyleComponentHandler does XYZ
 *
 * @author <a href="bloritsch.at.apache.org">Berin Loritsch</a>
 * @version CVS $ Revision: 1.1 $
 */
public class RequestLifestyleComponentHandler 
extends AbstractComponentHandler 
implements Configurable {
    private static int lastInstance = 0;

    /** The instance of the PoolManager to create the Pool for the Handler */
    private PoolManager m_poolManager;

    /** The pool of components for <code>Poolable</code> Components */
    private Pool m_pool;

    /** The Config element for the poolable */
    private int m_poolMin;

    private String m_role = generateRole();

    /**
     * Application of suporting services to the handler.
     * @param serviceManager the service manager
     * @exception ServiceException if a service related error occurs
     * @avalon.dependency type="PoolManager"
     */
    public void service( final ServiceManager serviceManager )
            throws ServiceException
    {
        super.service( serviceManager );
        m_poolManager =
        (PoolManager) serviceManager.lookup( PoolManager.ROLE );
    }

    /**
     * Configuration of the handler under which the minimum pool size
     * is established.
     * @param configuration the configuration fragment
     * @exception ConfigurationException if the supplied configuration attribute
     *    for 'pool-min' cannot be resolved to an integer value
     */
    public void configure( final Configuration configuration )
            throws ConfigurationException
    {
        m_poolMin = configuration.getAttributeAsInteger( "pool-min", 10 );
    }

    /**
     * Initialize the ComponentHandler.
     * @exception Exception if an error occurs
     */
    protected void doPrepare()
            throws Exception
    {
        m_pool = m_poolManager.getManagedPool( m_factory, m_poolMin );
    }

    /**
     * Get a reference of the desired Component
     * @exception Exception if an error occurs
     */
    protected Object doGet()
    throws Exception {
        EnvironmentDescription desc = RequestLifecycleHelper.getEnvironmentDescription();
        if ( null != desc ) {
            if ( null != desc ) {
                Object component = desc.getRequestLifecycleComponent( m_role );
                if ( null != component )
                {
                    return component;
                }
                component = desc.getGlobalRequestLifecycleComponent( m_role );
                if ( null != component )
                {
                    return component;
                }
            }
        }

        final Object component = m_pool.acquire();
        if ( null != component && component instanceof RequestLifecycleComponent )
        {
            if ( desc == null )
            {
                throw new ServiceException( m_role, "ComponentManager has no Environment Stack." );
            }

            // first test if the parent CM has already initialized this component
            if ( !desc.containsRequestLifecycleComponent( m_role ) )
            {
                try
                {   // FIXME - we need a source resolver here!
                    ( (RequestLifecycleComponent) component ).setup(
                            null,
                            desc.objectModel );
                }
                catch ( Exception local )
                {
                    throw new ServiceException( m_role, "Exception during setup of RequestLifecycleComponent.", local );
                }
                desc.addRequestLifecycleComponent( m_role, component, this );
            }
        }

        if ( null != component && component instanceof GlobalRequestLifecycleComponent )
        {
            if ( desc == null  )
            {
                throw new ServiceException( m_role, "ComponentManager has no Environment Stack." );
            }

            // first test if the parent CM has already initialized this component
            if ( !desc.containsGlobalRequestLifecycleComponent( m_role ) )
            {
                try
                {   // FIXME - we need a source resolver here!
                    ( (GlobalRequestLifecycleComponent) component ).setup(
                            null,
                            desc.objectModel );
                }
                catch ( Exception local )
                {
                    throw new ServiceException( m_role, "Exception during setup of RequestLifecycleComponent.", local );
                }
                desc.addGlobalRequestLifecycleComponent( m_role, component, this );
            }
        }

        return component;
    }

    /**
     * Return a reference of the desired Component
     * @param component the component to return to the handler
     */
    protected void doPut( final Object component )
    {}

    void release( final Object component )
    {
        m_pool.release( component );
    }

    private static synchronized String generateRole()
    {
        return "component" + lastInstance++;
    }
}
