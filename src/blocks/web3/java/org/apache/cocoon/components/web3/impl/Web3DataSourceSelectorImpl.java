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
 * @version CVS $Id: Web3DataSourceSelectorImpl.java,v 1.6 2004/02/06 22:54:05 joerg Exp $
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
