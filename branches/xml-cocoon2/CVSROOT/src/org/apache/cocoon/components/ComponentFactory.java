/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.components;

import org.apache.avalon.util.pool.ObjectFactory;
import org.apache.avalon.util.pool.Pool;

import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.Composer;
import org.apache.avalon.ThreadSafe;
import org.apache.avalon.Contextualizable;
import org.apache.avalon.Context;
import org.apache.avalon.Poolable;
import org.apache.avalon.Initializable;
import org.apache.avalon.Disposable;
import org.apache.avalon.Stoppable;
import org.apache.avalon.Startable;

import org.apache.log.Logger;
import org.apache.avalon.Loggable;

/** Factory for Cocoon components.
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-03-16 19:54:01 $
 */
public class ComponentFactory implements ObjectFactory, ThreadSafe, Loggable {
    private Logger log;

    /** The class which this <code>ComponentFactory</code>
     * should create.
     */
    private Class componentClass;

    /** The configuration for this component.
     */
    private Configuration conf;

    /** The component manager for this component.
     */
    private ComponentManager manager;

    /** The Context for the component
     */
    private Context context;

    /** Construct a new component factory for the specified component.
     * @param componentClass the class to instantiate (must have a default constructor).
     * @param config the <code>Configuration</code> object to pass to new instances.
     * @param manager the component manager to pass to <code>Composer</code>s.
     */
    public ComponentFactory(Class componentClass, Configuration config, ComponentManager manager, Context context) {
        this.componentClass = componentClass;
        this.conf = config;
        this.manager = manager;
        this.context = context;
    }

    public void setLogger(Logger logger) {
        if (this.log == null) {
            this.log = logger;
        }
    }

    public Object newInstance() throws Exception {
        Object comp = componentClass.newInstance();

        log.debug("ComponentFactory creating new instance of "
            + componentClass.getName() + "."
        );

        if ( comp instanceof Contextualizable ) {
            ((Contextualizable)comp).contextualize(this.context);
        }

        if ( comp instanceof Loggable) {
            ((Loggable)comp).setLogger(this.log);
        }

        if ( comp instanceof Composer) {
            ((Composer)comp).compose(this.manager);
        }

        if ( comp instanceof Configurable ) {
            ((Configurable)comp).configure(this.conf);
        }

        if ( comp instanceof Initializable ) {
            ((Initializable)comp).init();
        }

        if ( comp instanceof Startable ) {
            ((Startable)comp).start();
        }

        return comp;
    }

    public final Class getCreatedClass() {
        return componentClass;
    }

    public final void decommission(Object comp) throws Exception {
        if ( comp instanceof Stoppable ) {
            ((Stoppable)comp).stop();
        }

        if ( comp instanceof Disposable ) {
            ((Disposable)comp).dispose();
        }

        comp = null;
    }
}
