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
package org.apache.cocoon.components.web3.impl;

import org.apache.cocoon.components.web3.Web3DataSource;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.cocoon.util.ClassUtils;

import java.util.Enumeration;
import java.util.Hashtable;

import EDU.oswego.cs.dl.util.concurrent.Mutex;

/**
 * The Default implementation for R3DataSources in Web3.  This uses the
 * normal <code>com.sap.mw.jco.JCO</code> classes.
 *
 * @author <a href="mailto:michael.gerzabek@at.efp.cc">Michael Gerzabek</a>
 * @since 2.1
 * @version CVS $Id: Web3DataSourceSelectorImpl.java,v 1.7 2004/03/05 13:02:25 bdelacretaz Exp $
 */
public class Web3DataSourceSelectorImpl
    extends AbstractLogEnabled
    implements ServiceSelector, Disposable, Serviceable, Configurable, ThreadSafe {

    /** The service manager instance */
    protected ServiceManager manager = null;
    protected Configuration configuration = null;
    private static Hashtable pools = new Hashtable();
    private static Mutex lock = new Mutex();

    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    public void configure(Configuration configuration)
        throws ConfigurationException {
        if (null != configuration) {
            this.configuration = configuration;
        } else {
            getLogger().error(
                "Couldn't configure Web3DataSourceSelector."
                    + " No configuration provided!");
        }
    }

    public boolean isSelectable(Object obj) {
        return Web3DataSourceSelectorImpl.pools.containsKey(obj);
    }

    public Object select(Object obj) throws ServiceException {
        Web3DataSource pool = null;
        try {
            Web3DataSourceSelectorImpl.lock.acquire();
            if (null != obj) {
                if (Web3DataSourceSelectorImpl.pools.containsKey(obj)) {
                    pool = (Web3DataSource)Web3DataSourceSelectorImpl.pools.get(obj);
                } else {
                    Configuration a[] = this.configuration.getChildren("backend");
                    Configuration c = null;

                    if (null != a)
                        for (int i = 0; i < a.length; i++) {
                            try {
                                String s = a[i].getAttribute("name");
                                if (null != s && s.equals(obj.toString())) {
                                    // a backend with a name can be defined only once
                                    c = a[i];
                                    break;
                                }
                            } catch (ConfigurationException x) {
                                // this configuration element has no mandatory
                                //attribute name
                            }
                        }
                    // No configuration for this backend-id found!
                    if (null == c) {
                        return null;
                    }
                    Class theClass =
                        Class.forName(
                            c.getChild("class").getValue(
                                "org.apache.cocoon.components.web3.impl.Web3DataSourceImpl"),
                            true,
                            ClassUtils.getClassLoader());
                    pool = (Web3DataSource) theClass.newInstance();
                    if (pool instanceof LogEnabled) {
                        ((LogEnabled) pool).enableLogging(getLogger());
                    }
                    pool.service(this.manager);
                    pool.configure(c);
                    pool.initialize();
                    Web3DataSourceSelectorImpl.pools.put(obj, pool);
                }
            }
        } catch (Exception ex) {
            getLogger().error(ex.getMessage(), ex);
            throw new ServiceException(null, ex.getMessage());
        } finally {
            Web3DataSourceSelectorImpl.lock.release();
        }
        getLogger().debug("Returning Web3DataSource[" + pool + "]");
        return pool;
    }

    public void release(Object object) {
    }

    /** Dispose properly of the pool */
    public void dispose() {
        this.manager = null;
        try {
            Web3DataSourceSelectorImpl.lock.acquire();
            String sid = null;
            Web3DataSource pool;
            for (Enumeration enum = Web3DataSourceSelectorImpl.pools.keys();
                enum.hasMoreElements();
                ) {
                sid = (String) enum.nextElement();
                pool =
                    (Web3DataSource) Web3DataSourceSelectorImpl.pools.get(sid);
                pool.dispose();
            }
            Web3DataSourceSelectorImpl.pools.clear();
        } catch (Exception ex) {
        } finally {
            Web3DataSourceSelectorImpl.lock.release();
        }
        Web3DataSourceSelectorImpl.lock = null;
    }
}
