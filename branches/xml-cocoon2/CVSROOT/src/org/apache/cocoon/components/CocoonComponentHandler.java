/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.components;

import org.apache.avalon.Component;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.Context;
import org.apache.avalon.Disposable;
import org.apache.avalon.Initializable;
import org.apache.avalon.Loggable;
import org.apache.avalon.Poolable;
import org.apache.avalon.Stoppable;
import org.apache.log.Logger;

class CocoonComponentHandler implements Initializable, Disposable, Loggable {
    final static int THREADSAFE  = 0;
    final static int POOLABLE = 1;
    final static int SINGLETHREADED = 2;

    private Logger log = null;
    private ComponentFactory factory;
    private ComponentPool pool;
    private Component instance;
    private final int type;
    private boolean initialized = false;
    private boolean disposed = false;

    /**
     * Create a ComponentHandler that takes care of hiding the details of
     * whether a Component is ThreadSafe, Poolable, or SingleThreaded.
     * It falls back to SingleThreaded if not specified.
     */
    CocoonComponentHandler(Class componentClass,
                           Configuration config,
                           ComponentManager manager,
                           Context context)
    throws Exception {
        this.factory = new ComponentFactory(componentClass, config, manager, context);

        if (org.apache.avalon.Poolable.class.isAssignableFrom(componentClass)) {
            this.pool = new ComponentPool(this.factory);
            this.type = CocoonComponentHandler.POOLABLE;
        } else if (org.apache.avalon.ThreadSafe.class.isAssignableFrom(componentClass)) {
            this.type = CocoonComponentHandler.THREADSAFE;
        } else {
            this.type = CocoonComponentHandler.SINGLETHREADED;
        }
    }

    /**
     * Create a ComponentHandler that takes care of hiding the details of
     * whether a Component is ThreadSafe, Poolable, or SingleThreaded.
     * It falls back to SingleThreaded if not specified.
     */
    CocoonComponentHandler(Component comp)
    throws Exception {
        this.type = CocoonComponentHandler.THREADSAFE;
        this.instance = comp;
    }

    /**
     * Sets the logger that the ComponentHandler will use.
     */
    public void setLogger(Logger log) {
        this.factory.setLogger(log);

        if (this.pool != null) {
            this.pool.setLogger(log);
        }

        this.log = log;
    }

    /**
     * Initialize the ComponentHandler.
     */
    public void init() {
        if (this.initialized) return;

        switch (this.type) {
            case CocoonComponentHandler.THREADSAFE:
                try {
                    if (this.instance == null) {
                        this.instance = (Component)this.factory.newInstance();
                    }
                } catch (Exception e) {
                    this.log.error("Cannot use component: " + this.factory.getCreatedClass().getName(), e);
                }
                break;
            case CocoonComponentHandler.POOLABLE:
                try {
                    this.pool.init();
                } catch (Exception e) {
                    this.log.error("Cannot use component: " + this.factory.getCreatedClass().getName(), e);
                }
                break;
            default:
                // Nothing to do for SingleThreaded Components
                break;
        }
        this.initialized = true;
    }

    /**
     * Get a reference of the desired Component
     */
    public Component get() throws Exception {
        if (! this.initialized) {
            throw new IllegalStateException("You cannot get a component from an uninitialized holder.");
        }

        if (this.disposed) {
            throw new IllegalStateException("You cannot get a component from a disposed holder");
        }

        Component comp = null;

        switch (this.type) {
            case CocoonComponentHandler.THREADSAFE:
                comp = this.instance;
                break;
            case CocoonComponentHandler.POOLABLE:
                comp = (Component)this.pool.get();
                break;
            default:
                comp = (Component)this.factory.newInstance();
                break;
        }

        return comp;
    }

    /**
     * Return a reference of the desired Component
     */
    public void put(Component comp) {
        if (! this.initialized) {
            throw new IllegalStateException("You cannot put a component in an uninitialized holder.");
        }

        if (this.disposed) {
            throw new IllegalStateException("You cannot put a component in a disposed holder");
        }

        switch (this.type) {
            case CocoonComponentHandler.THREADSAFE:
                // Nothing to do for ThreadSafe Components
                break;
            case CocoonComponentHandler.POOLABLE:
                this.pool.put((Poolable) comp);
                break;
            default:
                try {
                    this.factory.decommission(comp);
                } catch (Exception e) {
                    this.log.warn("Error decommissioning component: " + this.factory.getCreatedClass().getName(), e);
                }
                break;
        }
    }

    /**
     * Dispose of the ComponentHandler and any associated Pools and Factories.
     */
    public void dispose() {
        this.disposed = true;

        try {
            switch (this.type) {
                case CocoonComponentHandler.THREADSAFE:
                    if (this.factory != null) {
                        this.factory.decommission(this.instance);
                    } else {
                        if ( this.instance instanceof Stoppable ) {
                            ((Stoppable) this.instance).stop();
                        }

                        if ( this.instance instanceof Disposable ) {
                            ((Disposable) this.instance).dispose();
                        }
                    }
                    this.instance = null;
                    break;
                case CocoonComponentHandler.POOLABLE:
                    if (this.pool instanceof Disposable) {
                        ((Disposable) this.pool).dispose();
                    }

                    this.pool = null;
                    break;
                default:
                    // do nothing here
                    break;
            }

            if (this.factory instanceof Disposable) {
                ((Disposable) this.factory).dispose();
            }

            this.factory = null;
        } catch (Exception e) {
            this.log.warn("Error decommissioning component: " + this.factory.getCreatedClass().getName(), e);
        }
    }
}
