/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.util;

import org.apache.avalon.Component;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Composer;
import org.apache.avalon.Configuration;
import org.apache.avalon.Configurable;
import org.apache.avalon.ThreadSafe;
import org.apache.avalon.util.pool.PoolController;

import org.apache.cocoon.util.ClassUtils;

/**
 * This class holds a sitemap component which is not specially marked as having
 * a spezial behaviour or treatment.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2001-02-20 20:34:22 $
 */
public class ComponentPoolController implements PoolController, ThreadSafe, Component {

    /** Initial increase/decrease amount */
    public final static int DEFAULT_AMOUNT = 8;

    /** Current increase/decrease amount */
    protected int amount = DEFAULT_AMOUNT;

    /** The last direction to increase/decrease >0 means increase, <0 decrease */
    protected int sizing_direction = 0;

    /** Creates a PoolController */
    public ComponentPoolController() {
        super();
    }

    /**
     * Called when a Pool reaches it's minimum.
     * Return the number of elements to increase minimum and maximum by.
     * @return the element increase
     */
    public int grow() {
    /*
        if (sizing_direction < 0 && amount > 1)
            amount /= 2;
        sizing_direction = 1;
    */
        return amount;
    }

    /**
     * Called when a pool reaches it's maximum.
     * Returns the number of elements to decrease mi and max by.
     * @return the element decrease
     */
    public int shrink() {
    /*
        if (sizing_direction > 0 && amount > 1)
            amount /= 2;
        sizing_direction = -1;
    */
        return amount;
    }
}