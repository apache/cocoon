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
package org.apache.cocoon.components.store.impl;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
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
public class CocoonStoreJanitor extends StoreJanitorImpl
                                implements Serviceable, Disposable {

    //~ Instance fields --------------------------------------------------------

    /** Name of the thread pool to use. Defaults to 'daemon'. */
    private String threadPool;

    /** Our {@link ServiceManager} */
    private ServiceManager serviceManager;

    /** Our {@link RunnableManager} */
    private RunnableManager runnableManager;

    /** Flags to ignore memory bursts in the startup */
    private boolean m_firstRun = true;

    /** Flags to ignore memory bursts in the startup */
    private boolean m_secondRun = false;

    //~ Methods ----------------------------------------------------------------

    public void parameterize(Parameters params) throws ParameterException {
        super.parameterize(params);
        this.threadPool = params.getParameter("thread-pool", "daemon");
    }

    /**
     * Get the <code>RunnableManager</code>
     *
     * @param serviceManager The <code>ServiceManager</code>
     * @throws ServiceException If RunnableManager is not available
     */
    public void service(final ServiceManager serviceManager)
    throws ServiceException {
        this.serviceManager = serviceManager;
        this.runnableManager = (RunnableManager) serviceManager.lookup(RunnableManager.ROLE);
    }

    /**
     * Release <code>RunnableManager</code>
     */
    public void dispose() {
        this.serviceManager.release(this.runnableManager);
        this.runnableManager = null;
        this.serviceManager = null;
    }

    /**
     * The "checker" thread checks if memory is running low in the jvm.
     */
    public void run() {
        // Ignoring memory bursts in the first two invokations
        if (m_firstRun || m_secondRun) {
            super.inUse = super.memoryInUse();
            m_secondRun = m_firstRun;
            m_firstRun = false;
        }

        super.checkMemory();

        // Relaunch
        relaunch(super.interval);
    }

    /**
     * Start this instance using a default thread from the
     * <code>RunnableManager</code>
     */
    public void start() {
        relaunch(0);
    }

    /**
     * Does a delayed (re-)start of this instance using a default thread from
     * the<code>RunnableManager</code> with a delay
     *
     * @param delay the delay to apply before next run
     */
    private void relaunch(final long delay) {
        getLogger().debug("(Re-)Start CocoonStoreJanitor");
        this.runnableManager.execute(this.threadPool, this, delay, 0);
    }
}
