/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.sitemap;

import org.apache.avalon.Component;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Composer;
import org.apache.avalon.Configuration;
import org.apache.avalon.Configurable;
import org.apache.avalon.Poolable;
import org.apache.avalon.util.pool.ObjectFactory;
import org.apache.avalon.util.pool.Pool;
import org.apache.avalon.util.pool.PoolController;
import org.apache.log.Logger;

import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.ComponentPool;
import org.apache.cocoon.Roles;

/**
 * This class holds a sitemap component which is not specially marked as having
 * a spezial behaviour or treatment.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2001-02-14 11:39:16 $
 */
public class PoolableComponentHolder extends DefaultComponentHolder implements ObjectFactory {

    /** The class of this component */
    private Class clazz = null;

    /** Initial increase/decrease amount */
    public final static int DEFAULT_AMOUNT = 16;

    /** Current increase/decrease amount */
    protected int amount = DEFAULT_AMOUNT / 2;

    /** The <code>Pool</code> for this components */
    protected Pool pool;

    /** Creates a DefaultComponentHolder
     * @param className The component class name
     * @param configuration The </CODE>Configuration</CODE> for the component
     * @param manager A <CODE>ComponentManager</CODE> for the component
     */
    public PoolableComponentHolder(Logger log, Class clazz, Configuration configuration, ComponentManager manager, String mime_type)
    throws Exception {
        super(log, clazz, configuration, manager, mime_type);
        this.clazz = clazz;
        PoolController pc = (PoolController)super.manager.lookup (Roles.POOL_CONTROLLER);
        ComponentPool cp = new ComponentPool (this, pc, amount, DEFAULT_AMOUNT);
        cp.setLogger(this.log);
        cp.init();
        this.pool = cp;
    }

    /** Creates a new instance of the <CODE>Component</CODE>
     * @return A <CODE>Component</CODE>
     */
    public Component get() throws Exception {
        return (Component) pool.get();
    }

    /**
     * This method has no sense in this implementation. It is used to return a
     * component to the <CODE>ComponentHolder</CODE>
     * @param component The<CODE>Component</CODE> to return
     */
    public void put(Component component) {
        pool.put((Poolable) component);
    }

    /**
     * This method returns the name of the component hold by this object
     * @return The name of the class this Holder holds
     */
    public String getName() {
        return clazz.getName();
    }

    /**
     * This method returns a new instance of a <code>Poolable</code> component
     * @return A Poolable component
     */
    public Poolable newInstance() throws Exception {
        return (Poolable) super.get();
    }

    /**
     * This method returns the <code>Class</code> a <code>Poolable</code> component
     * @return A Poolable component
     */
    public Class getCreatedClass() {
        return this.clazz;
    }
}
