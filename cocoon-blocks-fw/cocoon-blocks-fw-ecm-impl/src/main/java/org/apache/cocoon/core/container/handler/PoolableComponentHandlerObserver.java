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

import org.apache.cocoon.util.jmx.ModelMBeanImpl;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;


/**
 * This MBean creates and registers a GaugeMonitor for every PoolableComponentHandler added
 */
public class PoolableComponentHandlerObserver
    extends ModelMBeanImpl {
    //~ Static fields/initializers ----------------------------------------------------------------------

    /** Suffix to add to base ObjectName */
    public static final String JMX_OBJECTNAME_SUFFIX = "type=pool-observer";

    /** JMX Attribute name */
    public static final String JMX_ATTR_EXHAUSTED_POOLS = "exhaustedPools";

    /** JMX Attribute name */
    public static final String JMX_ATTR_NUMOF_EXHAUSTED_POOLS = "numberOfExhaustedPools";

    /** JMX Operation name */
    public static final String JMX_OP_RESET_HIGH_WATER_MARKS = "resetHighWaterMarks";

    //~ Instance fields ---------------------------------------------------------------------------------

    /** The list of PoolableComponentHandlerMBeans ObjectName to observe */
    private List handlerMBeans = new ArrayList();

    /** The list of PoolableComponentHandlers to observe */
    private List handlers = new ArrayList();

    //~ Constructors ------------------------------------------------------------------------------------

    /**
     * Creates a new PoolableComponentHandlerMonitorMBean object.
     */
    public PoolableComponentHandlerObserver() {
        super();
        defineManagedResource();
    }

    //~ Methods -----------------------------------------------------------------------------------------

    /**
     * Returns the array of JMX ObjectName of exhausted pools
     *
     * @return array of JMX ObjectName
     */
    public ObjectName[] getExhaustedPools() {
        final List exhaustedPools = getExhaustedPoolList();

        return (ObjectName[])exhaustedPools.toArray(new ObjectName[exhaustedPools.size()]);
    }

    /**
     * Returns the array of JMX ObjectName of exhausted pools
     *
     * @return array of JMX ObjectName
     */
    public int getNumberOfExhaustedPools() {
        final List exhaustedPools = getExhaustedPoolList();

        return exhaustedPools.size();
    }

    /**
     * Add a PoolableComponentHandler to be monitored
     *
     * @param handler The PoolableComponentHandler to add
     * @param handlerName it's JMX ObjectName
     */
    protected synchronized void addPoolHandler(final PoolableComponentHandler handler,
                                               final ObjectName handlerName) {
        if(this.handlers.contains(handler)) {
            return;
        }

        this.handlers.add(handler);
        this.handlerMBeans.add(handlerName);
    }

    /**
     * Define the JMX interface
     */
    protected void defineManagedResource() {
        super.defineManagedResource();
        defineAttribute(JMX_ATTR_EXHAUSTED_POOLS, false, true);
        defineAttribute(JMX_ATTR_NUMOF_EXHAUSTED_POOLS, false, true);
        defineOperation(JMX_OP_RESET_HIGH_WATER_MARKS, MBeanOperationInfo.INFO);
    }

    /**
     * Returns the list JMX ObjectName of exhausted pools
     *
     * @return list of JMX ObjectName
     */
    private List getExhaustedPoolList() {
        final List exhaustedPools = new ArrayList();

        for(int i = 0; i < this.handlers.size(); i++) {
            final PoolableComponentHandler handler = (PoolableComponentHandler)this.handlers.get(i);

            if((handler.getHighWaterMark() - handler.getMax()) > 0) {
                exhaustedPools.add(this.handlerMBeans.get(i));
            }
        }

        return exhaustedPools;
    }
}
