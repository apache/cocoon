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

import org.apache.cocoon.util.ClassUtils;

/** 
 * This class holds a sitemap component which is not specially marked as having 
 * a spezial behaviour or treatment.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-10-08 20:58:58 $
 */
public class PoolableComponentHolder implements ComponentHolder, ObjectFactory {

    /** The name of the class */
    private String className;

    /** The class of this component */
    private Class clazz = null;

    /** The <code>Configuration</code> of this component */
    private Configuration configuration;

    /** The <code>ComponentManagercode> of this component */
    private ComponentManager manager;

    /** Initial increase/decrease amount */
    public final static int DEFAULT_AMOUNT = 16;

    /** Current increase/decrease amount */
    protected int amount = DEFAULT_AMOUNT;

    /** The last direction to increase/decrease >0 means increase, <0 decrease */
    protected int sizing_direction = 0;

    /** The <code>Pool</code> for this components */
    protected Pool pool;

    
    /** Creates a DefaultComponentHolder
     * @param className The component class name
     * @param configuration The </CODE>Configuration</CODE> for the component
     * @param manager A <CODE>ComponentManager</CODE> for the component
     */
    public PoolableComponentHolder(String className, Configuration configuration, ComponentManager manager)
    throws Exception {
        this.className = className;
        try {
            this.clazz = ClassUtils.loadClass (className);
        } catch (Exception e) {
            this.clazz = null;
        }
        this.configuration = configuration;
        this.manager = manager;
        PoolController pc = (PoolController)this.manager.getComponent ("sitemap-component-pool-controller");
        this.pool = new ComponentPool (this, pc, amount, amount);
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
        return className;
    }

    /**
     * This method returns a new instance of a <code>Poolable</code> component
     * @return A Poolable component
     */
    public Poolable newInstance() throws Exception {
        Poolable comp = (Poolable) ClassUtils.newInstance (this.className);
        if (comp instanceof Composer) {
            ((Composer) comp).setComponentManager (this.manager);
        }
        if (comp instanceof Configurable) {
            ((Configurable) comp).setConfiguration (this.configuration);
        }
        return comp;
    }

    /**
     * This method returns the <code>Class</code> a <code>Poolable</code> component
     * @return A Poolable component
     */
    public Class getCreatedClass() {
        return this.clazz;
    }
}