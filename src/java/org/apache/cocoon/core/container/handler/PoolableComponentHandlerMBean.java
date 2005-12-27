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


import org.apache.cocoon.util.JMXUtils;
import org.mortbay.util.jmx.ModelMBeanImpl;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;

/**
 * The ThreadSafeComponentHandlerMBean adds JMX managability for ThreadSafeComponentHandler.
 *
 * @version $Id: ThreadSafeComponentHandler.java 312637 2005-10-10 13:00:42Z cziegeler $
 * @since 2.2
 */
public class PoolableComponentHandlerMBean
extends ModelMBeanImpl {
    
    private PoolableComponentHandler handler;
    
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
    public PoolableComponentHandlerMBean(final PoolableComponentHandler handler)
        throws MBeanException, InstanceNotFoundException {
        super( handler );
        this.handler = handler;
        try
        {
            super.setManagedResource( handler, "objectReference" );
        }
        catch( final InvalidTargetObjectTypeException e )
        {
            e.printStackTrace();
        }
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
        return JMXUtils.genDefaultJmxName(handler.getInfo().getServiceClassName());
    }
 
    public String getJmxNameAddition()
    {
        return "type=PoolableHandler"; 
    }
}
