/* 
 * Copyright 2002-2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.handler;


import org.apache.cocoon.components.ComponentInfo;
import org.mortbay.util.jmx.ModelMBeanImpl;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;

/**
 * The PoolableComponentHandlerMBean adds JMX managability for PoolableComponentHandler.
 *
 * @version $Id: ThreadSafeComponentHandler.java 312637 2005-10-10 13:00:42Z cziegeler $
 * @since 2.2
 */
public class PoolableComponentHandlerMBean
extends ModelMBeanImpl {
    
    private final PoolableComponentHandler handler;
    private final ComponentInfo info;
    
    protected void defineManagedResource() {
        super.defineManagedResource();
        defineAttribute("interfaces", false, true);
        defineAttribute("defaultMaxPoolSize", false, true);
        defineAttribute("maxPoolSize", false, true);
        defineAttribute("readyPoolSize", false, true);
        defineAttribute("totalPoolSize", false, true);
        defineAttribute("highWaterMark", false, true);
    }
    /**
     * Construction of PoolableComponentHandlerMBean
     *
     * @param handler The managed PoolableComponentHandler instance
     */
    public PoolableComponentHandlerMBean(final PoolableComponentHandler handler, final ComponentInfo info)
        throws MBeanException, InstanceNotFoundException {
        super( handler );
        this.handler = handler;
        this.info = info;
    }
    
    public String[] getInterfaces()
    {
        final String [] ifaces = new String[this.handler.interfaces.length];
        for(int i = 0; i < ifaces.length; i++) {
            ifaces[i] = this.handler.interfaces[i].getName();
        }
        return ifaces;
    }

    public int getDefaultMaxPoolSize()
    {
        return NonThreadSafePoolableComponentHandler.DEFAULT_MAX_POOL_SIZE;
    }

    public int getMaxPoolSize()
    {
        return handler.getMax();
    }

    public int getReadyPoolSize()
    {
        return handler.getReadySize();
    }

    public int getHighWaterMark()
    {
        return handler.getHighWaterMark();
    }

    public int getTotalPoolSize()
    {
        return handler.getSize();
    }
    
    public String getJmxName() 
    {
        //return JMXUtils.genDefaultJmxName(handler.getInfo().getServiceClassName());
        return "subsys=ECM++,handler=poolable" + (info.getRole() != null ? ",role=" + info.getRole() : "");
    }
}
