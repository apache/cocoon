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
package org.apache.cocoon.components.store.impl;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.thread.RunnableManager;


/**
 * The CocoonStoreJanitor class just subclasses the {@link StoreJanitorImpl} to
 * overwrite the start method for background thread creation using the Cocoon
 * {@link RunnableManager}.
 *
 * @author <a href="mailto:giacomo.at.apache.org">Giacomo Pati</a>
 * @version $Id$
 */
public class CocoonStoreJanitor
    extends StoreJanitorImpl
    implements Serviceable
{
    //~ Instance fields --------------------------------------------------------

    /** Our {@link ServiceManager} */
    private ServiceManager m_serviceManager;

    /** Flags to ignore memory bursts in the startup */
    private boolean m_firstRun = true;

    /** Flags to ignore memory bursts in the startup */
    private boolean m_secondRun = false;

    //~ Methods ----------------------------------------------------------------

    /**
     * The "checker" thread checks if memory is running low in the jvm.
     */
    public void run(  )
    {
        // ignoring memory bursts in the first two invokations
        if( m_firstRun || m_secondRun )
        {
            super.inUse = super.memoryInUse(  );
            m_secondRun = m_firstRun;
            m_firstRun = false;
        }

        super.checkMemory(  );

        // Relaunch
        relaunch( super.interval );
    }

    /**
     * Get the <code>ServiceManager</code>
     *
     * @param serviceManager The <code>ServiceManager</code>
     *
     * @throws ServiceException Should not happen
     */
    public void service( final ServiceManager serviceManager )
        throws ServiceException
    {
        m_serviceManager = serviceManager;
    }

    /**
     * Start this instance using a default thread from the
     * <code>RunnableManager</code>
     */
    public void start(  )
    {
        relaunch( 0 );
    }

    /**
     * Does a delayed (re-)start of this instance using a default thread from
     * the<code>RunnableManager</code> with a delay
     *
     * @param delay the delay to apply before next run
     *
     * @throws CascadingRuntimeException in case we cannot get a
     *         <code>RunnableManager</code>
     */
    private void relaunch( final long delay )
    {
        try
        {
            if( getLogger(  ).isDebugEnabled(  ) )
            {
                getLogger(  ).debug( "(Re-)Start CocoonStoreJaitor" );
            }

            final RunnableManager runnableManager =
                (RunnableManager)m_serviceManager.lookup( RunnableManager.ROLE );
            runnableManager.execute( this, delay, 0 );
            m_serviceManager.release( runnableManager );
        }
        catch( final ServiceException se )
        {
            throw new CascadingRuntimeException( "Cannot lookup RunnableManager",
                                                 se );
        }
    }
}
