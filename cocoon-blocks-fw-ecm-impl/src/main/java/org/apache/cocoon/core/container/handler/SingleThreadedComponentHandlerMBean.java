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
import org.apache.cocoon.util.jmx.ModelMBeanImpl;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;

/**
 * The SingleThreadedComponentHandlerMBean adds JMX managability for SingleThreadedComponentHandler.
 *
 * @version $Id: ThreadSafeComponentHandler.java 312637 2005-10-10 13:00:42Z cziegeler $
 * @since 2.2
 */
public class SingleThreadedComponentHandlerMBean
extends ModelMBeanImpl {
    
    private final SingleThreadedComponentHandler handler;
    private final ComponentInfo info;
    
    protected void defineManagedResource() {
        super.defineManagedResource();
        defineAttribute("maxCreated", false, true);
        defineAttribute("maxDecommissioned", false, true);
        defineAttribute("outstanding", false, true);
    }
    /**
     * Construction of PoolableComponentHandlerMBean
     *
     * @param handler The managed PoolableComponentHandler instance
     */
    public SingleThreadedComponentHandlerMBean(final SingleThreadedComponentHandler handler, final ComponentInfo info)
        throws MBeanException, InstanceNotFoundException {
        super( handler );
        this.handler = handler;
        this.info = info;
    }

    public long getMaxCreated()
    {
        return handler.getMaxCreated();
    }

    public long getMaxDecommissioned()
    {
        return handler.getMaxDecommissioned();
    }

    public long getOutstanding()
    {
        return handler.getMaxCreated() - handler.getMaxDecommissioned();
    }
    
    public String getJmxName() 
    {
        return "subsys=ECM++,handler=single-threaded" + (info.getRole() != null ? ",role=" + info.getRole() : "");
    }
}
