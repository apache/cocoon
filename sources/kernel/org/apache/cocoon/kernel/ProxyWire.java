/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.kernel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.cocoon.kernel.composition.Component;
import org.apache.cocoon.kernel.composition.Composer;
import org.apache.cocoon.kernel.composition.Wire;
import org.apache.cocoon.kernel.composition.WiringException;
import org.apache.cocoon.kernel.composition.Wirings;
import org.apache.cocoon.kernel.logging.Logger;
import org.apache.cocoon.kernel.logging.Logging;
import org.apache.cocoon.kernel.resolution.Resolver;

/**
 * <p>A proxy handler wrapping an {@link Object} instance and enclosing it into
 * a {@link Wire} instance returnable by a {@link Wirings} instance.</p>
 *
 * <p>This class will wrap an object instance as it would be returned by the
 * {@link Composer#acquire()} method, enabling all the methods specified by
 * the {@link Wire} interface to operate in accordance with the rules
 * defined by this framework indipendently of the object itself.</p>
 * 
 * <p>Note that this class <b>does not</b> implement the {@link Wire}
 * interface as it <b>must not</b> be returned directly in its form by a
 * {@link Wirings}.</p>
 *
 * <p>Any {@link Wirings} using this class <b>must</b> return the instance
 * returned by this instance's {@link #getWire()} method in its
 * {@link Wirings#lookup(Class,String)} method.</p>
 *
 * <p>This implementation does not yet use any {@link java.lang.ref weak, soft
 * or phantom references} to provide automatic unwiring of components when
 * memory constraints require it, but users of the framework must be aware of
 * this planned new feature.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.9 $)
 */
public final class ProxyWire implements InvocationHandler {
    
    /** <p>The {@link Object#finalize()} method used for interception.</p> */
    private static Method finalize = null;
    
    /** <p>The {@link Wire#wired()} method used for interception.</p> */
    private static Method wired = null;

    /** <p>The {@link Wire#release()} method used for interception.</p> */
    private static Method release = null;
    
    /** <p>The {@link Wire#dispose()} method used for interception.</p> */
    private static Method dispose = null;
    
    /** <p>A static flag indicating whether this class was initialized.</p> */
    private static boolean initialized = false;
    
    /* ====================================================================== */

    /* <p>The proxied {@link Object} (should be a weak? reference).</p> */
    private Object instance = null;

    /* <p>The {@link Compser} to which the instance should be released.</p> */
    private Composer composer = null;

    /* <p>The {@link Wire} instance generated with proxies.</p> */
    private Wire wire = null;

    /**
     * <p>Create a new {@link ProxyWire} instance acting as a proxy liaison
     * between a {@link Composer} and a {@link Wirings} instance.</p>
     *
     * <p>A {@link ProxyWire} will acquire an {@link Object} instance
     * from the specified {@link Composer} and return it as a {@link Wire}
     * in its {@link #getWire()} method.</p>
     *
     * <p>The {@link Wire} returned will be <b>guaranteed</b> to implement
     * the interface role specified in this constructor, and all the interfaces
     * implemented by the original object.</p>
     *
     * @param c the {@link Composer} where acquisition, release and/or
     *                 disposal of proxied component instances will occur.
     * @param role an interface {@link Class} to which the {@link Wire}
     *             returned by {@link #getWire()} <b>must</b> be castable to.
     * @param w The {@link Wirings} instance associated with the block instance
     *          where the proxied component is deployed.
     * @param r The {@link Resolver} providing resolution in the context of the
     *          block instance requesting the component instance.
     * @param l The {@link Logger} to provide to the component if it implements
     *          the {@link Logging} interface.
     * @throws WiringException if an error occurred acquiring the original
     *                              object or creating the {@link Wire}.
     * @throws NullPointerException if any of the parameters were <b>null</b>.
     */
    public ProxyWire(Composer c, Class role, Wirings w, Resolver r, Logger l)
    throws WiringException {
        ProxyWire.initialize();

        ClassLoader loader = c.getClass().getClassLoader();

        /* Check that the requested role is actually an interface */
        if (!role.isInterface()) {
            throw new WiringException("Requested role \"" + role.getName()
                                           + "\" is not an interface");
        }

        /* Noone can request the "Wiring" role */
        if (Wire.class.equals(role)) {
            throw new WiringException("Invalid role \"" + role.getName()
                                           + "\" requested");
        }

        /* Acquire and check that the instance implements the given role */
        Object instance = null;
        try {
            instance = c.acquire();
            if (instance instanceof Logging) ((Logging)instance).logger(l);
        } catch (Throwable t) {
            throw new WiringException("Unable to acquire component", t);
        }

        if (!role.isAssignableFrom(instance.getClass())) {
            /* Remember to release components with wrong interfaces */
            c.release(instance);
            throw new WiringException("Acquired object instance \""
                                      + instance.getClass().getName()
                                      + "\" does not implement the "
                                      + "requested interface role \""
                                      + role.getName() + "\"");
        }

        try {
            /* Create the proxy instance */
            Class classes[] = new Class[] { Wire.class, role };
            this.wire = (Wire)Proxy.newProxyInstance(loader, classes, this);

            /* Contextualize the instance with the wire */
            if (instance instanceof Component) {
                ((Component)instance).contextualize(this.wire, r);
            }

            /* Record the original composer and instance */
            this.composer = c;
            this.instance = instance;
        } catch (Throwable t) {
            /* Something bad happened releasing, release the instance */
            c.release(instance);
            throw new WiringException("Unable to create wrapper for "
                                      + "composed component instance", t);
        }
    }

    /**
     * <p>Return the {@link Wire} instance wrapping the original component
     * instance acquired from the {@link Composer}.</p>
     *
     * <p>The returned {@link Wire} instance will be castable to the role
     * interface specified in this instance's constructor method.</p>
     *
     * @return a <b>non null</b> {@link Wire} instance returnable by the 
     *         {@link Wirings#lookup(Class,String)} method.
     */
    public Wire getWire() {
        return(this.wire);
    }

    
    /**
     * <p>Return the connection status of this {@link ProxyWire} instance.</p>
     *
     * @return a <b>false</b> if the returned {@link Wire} instabce's
     *         {@link Wire#release()} or {@link Wire#dispose()} methods have
     *         been called, or if the {@link #cut(boolean)} method have been
     *         called on this {@link ProxyWire} instance.
     */
    public boolean isConnected() {
        return(this.instance != null);
    }
    
    /**
     * <p>Invoke the specified method on the proxied instance.</p>
     *
     * <p>This method will intercept all calls to the methods specified by the
     * {@link Wire} interfaces and deal with them accordingly.</p>
     *
     * @see InvocationHandler#invoke(Object,Method,Object[])
     * @param proxy the proxy instance where the method was called.
     * @param method the method called on the proxy instance.
     * @param arguments the arguments specified in the method invocation.
     * @return any object returned by the proxy instance method invocation.
     * @throws any {@link Throwable} thrown by the proxy instance.
     */
    public Object invoke(Object proxy, Method method, Object arguments[])
    throws Throwable {
        /* Intercept finalize, release and dispose on the proxied Wire */
        if (this.check(method, finalize)) { this.cut(false); return(null); }
        if (this.check(method, release))  { this.cut(false); return(null); }
        if (this.check(method, dispose))  { this.cut(true);  return(null); }

        /* If it's simply the wiring status they require, that they will get */
        if (this.check(method, wired)) return(new Boolean(this.isConnected()));

        /* Invoke the method on the remote instance */
        if (this.instance != null) try {
            return(method.invoke(this.instance, arguments));
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }

        /* Have we been released? */
        throw new IllegalStateException("Wire has been cut");
    }

    /**
     * <p>Effectively cut the connection between the {@link Wire} instance
     * and the original proxied component.</p>
     *
     * <p>At the same time, this method will return the original component
     * instance to its original {@link Composer} either by releasing or
     * disposing it.</p>
     */
    public void cut(boolean dispose) {
        /* Locally release the object in a synchronized environment */
        Object original = this.instance;
        if (this.instance == null) return;
        synchronized (this) {
            if (this.instance == null) return;
            this.instance = null;
        }

        try {
            /* Release the instance back to the Composer */
            if (dispose) this.composer.dispose(original);
            else this.composer.release(instance);
        } catch (Throwable t) {
            /* Ignore anything thrown by buggy Composer(s) */
        }
    }

    /**
     * <p>Called by the garbage collector when this {@link Wire} instance
     * is garbage collected.</p>
     *
     * <p>This method simply make sure that this wire has been cut, releasing
     * or disposing of the proxied component back to the original
     * {@link Composer}.</p>
     */
    public void finalize() {
        this.cut(false);
    }
    
    /**
     * <p>Compare two methods for equality ignoring their declaring classes
     * possible differences.</p>
     * 
     * @param ma the first method to check for equality.
     * @param mb the second method to check for equality.
     * @return <b>true</b> if the methods have the same name and signature,
     *         <b>false</b> in all other cases.
     */
    public boolean check(Method ma, Method mb) {
        if (!ma.getName().equals(mb.getName())) return(false);
        if (!ma.getReturnType().equals(mb.getReturnType())) return(false);

        Class pa[] = ma.getParameterTypes();
        Class pb[] = mb.getParameterTypes();
        if (pa.length != pb.length) return(false);
        for (int x = 0; x < pa.length; x++) {
            if (!pa[x].equals(pb[x])) return(false);
        }

        return(true);
    }

    /**
     * <p>A static initializer for the {@link ProxyWire} class, locating
     * those methods it must intercept for components.</p>
     */
    private static void initialize() {
        if (ProxyWire.initialized) return;
        synchronized (ProxyWire.class) {
            if (ProxyWire.initialized) return;
            try {
                /* Attempt to locate our own methods */
                Class parameters[] = new Class[0];
                ProxyWire.wired = Wire.class.getDeclaredMethod("wired", parameters);
                ProxyWire.release = Wire.class.getDeclaredMethod("release", parameters);
                ProxyWire.dispose = Wire.class.getDeclaredMethod("dispose", parameters);
                ProxyWire.finalize = Object.class.getDeclaredMethod("finalize", parameters);
                parameters = new Class[] { String.class };
            } catch (NoSuchMethodException exception) {
                /* If the methods were not found, then we're in BIG troubles */
                String message = "Unable to locate interceptable methods in the \""
                                 + Wire.class.getName() + "\" interface";
                NoSuchMethodError error = new NoSuchMethodError(message);
                throw((NoSuchMethodError)error.initCause(exception));
            }
        }
    }

}
