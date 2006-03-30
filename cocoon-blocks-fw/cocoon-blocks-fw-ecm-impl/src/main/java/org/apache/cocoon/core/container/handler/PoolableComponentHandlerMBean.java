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

import javax.management.MBeanServer;
import javax.management.ObjectName;


/**
 * The PoolableComponentHandlerMBean adds JMX managability for PoolableComponentHandler.
 *
 * @since 2.2
 */
public class PoolableComponentHandlerMBean
    extends ModelMBeanImpl {
    //~ Static fields/initializers ----------------------------------------------------------------------

    /** The JMX ObjectName prefix used */
    public static final String JMX_OBJECTNAME_PREFIX = "subsys=ECM++,handler=poolable";

    /** JMX Attribute names */
    public static final String JMX_ATTR_INTERFACES = "interfaces";
    public static final String JMX_ATTR_DEFAULT_MAX_POOL_SIZE = "defaultMaxPoolSize";
    public static final String JMX_ATTR_MAX_POOL_SIZE = "maxPoolSize";
    public static final String JMX_ATTR_READY_POOL_SIZE = "readyPoolSize";
    public static final String JMX_ATTR_TOTAL_POOL_SIZE = "totalPoolSize";
    public static final String JMX_ATTR_HIGH_WATER_MARK = "highWaterMark";

    /** The bean observing this MBeans highWaterMark attribute */
    private static PoolableComponentHandlerObserver s_poolMonitor;

    //~ Instance fields ---------------------------------------------------------------------------------

    /** The ComponentInfo the component the handler manages */
    private final ComponentInfo info;

    /** The handler to manage */
    private final PoolableComponentHandler handler;

    //~ Constructors ------------------------------------------------------------------------------------

    /**
     * Construction of PoolableComponentHandlerMBean
     *
     * @param handler The managed PoolableComponentHandler instance
     * @param info The ComponentInfo
     */
    public PoolableComponentHandlerMBean(final PoolableComponentHandler handler,
                                         final ComponentInfo info) {
        super(handler);
        this.handler = handler;
        this.info = info;
    }

    //~ Methods -----------------------------------------------------------------------------------------

    /**
     * A JMX Attribute
     *
     * @return the default max pool size
     */
    public int getDefaultMaxPoolSize() {
        return NonThreadSafePoolableComponentHandler.DEFAULT_MAX_POOL_SIZE;
    }

    /**
     * A JMX Attriute
     *
     * @return the pool size high water mark
     */
    public int getHighWaterMark() {
        return handler.getHighWaterMark();
    }

    /**
     * A JMX Attriute
     *
     * @return the array of implemented interfaces of the component managed by the handler
     */
    public String[] getInterfaces() {
        final String[] ifaces = new String[this.handler.getInterfaces().length];

        for(int i = 0; i < ifaces.length; i++) {
            ifaces[i] = this.handler.getInterfaces()[i].getName();
        }

        return ifaces;
    }

    /**
     * Give this MBean a JMX name
     *
     * @return the JMX name to use for this MBean
     */
    public String getJmxName() {
        //return JMXUtils.genDefaultJmxName(handler.getInfo().getServiceClassName());
        return JMX_OBJECTNAME_PREFIX + ((info.getRole() != null) ? (",role=" + info.getRole()) : "");
    }

    /**
     * A JMX Attriute
     *
     * @return the maximum pool size
     */
    public int getMaxPoolSize() {
        return handler.getMax();
    }

    /**
     * A JMX Attriute
     *
     * @return how many components are ready in the pool
     */
    public int getReadyPoolSize() {
        return handler.getReadySize();
    }

    /**
     * A JMX Attriute
     *
     * @return current size of the pool
     */
    public int getTotalPoolSize() {
        return handler.getSize();
    }

    /**
     * Checks whether the PoolableComponentHandlerPoolObserver MBean is registered and passes it the
     * handler currently  set under JMX for observing the high water mark attribute.
     *
     * @see org.apache.cocoon.util.jmx.ModelMBeanImpl#preRegister(javax.management.MBeanServer,
     *      javax.management.ObjectName)
     */
    public synchronized ObjectName preRegister(final MBeanServer server,
                                               final ObjectName oName) {
        final ObjectName objectName = super.preRegister(server, oName);

        try {
            final ObjectName monitorName =
                new ObjectName(objectName.getDomain() + ":" + JMX_OBJECTNAME_PREFIX + "," +
                               PoolableComponentHandlerObserver.JMX_OBJECTNAME_SUFFIX);

            if(! server.isRegistered(monitorName)) {
                // Now let's register the PoolMonitor
                // We would like to know if we have excessive object creation not served from the pool
                s_poolMonitor = new PoolableComponentHandlerObserver();
                server.registerMBean(s_poolMonitor, monitorName);
            }

            // Setup the monitor: link to the service MBean
            s_poolMonitor.addPoolHandler(handler, objectName);
        } catch(final Exception e) {
            System.out.println("Ignored Exception");
            e.printStackTrace();
        }

        return objectName;
    }

    /**
     * Define the manages resources
     */
    protected void defineManagedResource() {
        super.defineManagedResource();
        defineAttribute(JMX_ATTR_DEFAULT_MAX_POOL_SIZE, false, true);
        defineAttribute(JMX_ATTR_HIGH_WATER_MARK, false, true);
        defineAttribute(JMX_ATTR_INTERFACES, false, true);
        defineAttribute(JMX_ATTR_MAX_POOL_SIZE, false, true);
        defineAttribute(JMX_ATTR_READY_POOL_SIZE, false, true);
        defineAttribute(JMX_ATTR_TOTAL_POOL_SIZE, false, true);
    }
}
