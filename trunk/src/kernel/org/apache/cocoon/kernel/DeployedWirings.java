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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.cocoon.kernel.composition.Composer;
import org.apache.cocoon.kernel.composition.Lifecycle;
import org.apache.cocoon.kernel.composition.LifecycleException;
import org.apache.cocoon.kernel.composition.Wire;
import org.apache.cocoon.kernel.composition.Wirings;
import org.apache.cocoon.kernel.composition.WiringException;
import org.apache.cocoon.kernel.deployment.DeploymentException;
import org.apache.cocoon.kernel.deployment.Instance;
import org.apache.cocoon.kernel.identification.Descriptor;
import org.apache.cocoon.kernel.identification.BlockDescriptor;
import org.apache.cocoon.kernel.resolution.CompoundResolver;
import org.apache.cocoon.kernel.resolution.Resolver;
import org.apache.cocoon.kernel.resolution.Resource;

/**
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.4 $)
 */
public class DeployedWirings implements Wirings, Lifecycle {

    /** <p>The {@link DeployableInstance} associated with this instance.</p> */
    private DeployableInstance instance = null;
    
    /** <p>The {@link KernelDeployer} associated with this instance.</p> */
    private KernelDeployer deployer = null;
    
    /** <p>The {@link Map} of all wired resources prefixes.</p> */
    private Map prefixes = new HashMap();
    
    /** <p>The {@link Composer} instance associated with this instance.</p> */
    private Composer composer = null;
    
    /** <p>The {@link Resolver} for private resources.</p> */
    private CompoundResolver priresolver = new CompoundResolver();
    
    /** <p>The {@link Resolver} for resources accessible by wired blocks.</p> */
    private CompoundResolver pubresolver = new CompoundResolver();

    /* ====================================================================== */

    /**
     * <p>Create a new {@link DeployedWirings} instance associated with a
     * specified {@link DeployableInstance} and a {@link KernelDeployer}.</p>
     *
     * @param instance the {@link DeployableInstance} associated with this
     *                 {@link DeployedWirings}.
     * @param deployer the {@link KernelDeployer} deploying this instance.
     * @throws DeploymentException if this instance can not be created.
     */
    public DeployedWirings(DeployableInstance instance, KernelDeployer deployer)
    throws DeploymentException {
        this.instance = instance;
        this.deployer = deployer;

        /* Retrieve all we need from our instance regarding composers */
        LoadedBlock block = (LoadedBlock) instance.block();
        BlockDescriptor descriptor = (BlockDescriptor) block.descriptor();
        String composer = descriptor.providedComposer();
        String component = descriptor.providedClass();
        ClassLoader loader = block.loader(Descriptor.ACCESS_PRIVATE);

        /* Attempt to find and create a composer instance */
        try {
            /* If the composer was specified, we load the class */
            Class clazz = SimpleComposer.class;
            if (composer != null) clazz = loader.loadClass(composer);

            /* If the component is not we instantiate with a class */
            if (component != null) {
                Class componentclass = loader.loadClass(component);
                Object newinstparams[] = new Object[] { componentclass };

                Class constrparams[] = new Class[] { Class.class };
                Constructor constr = clazz.getConstructor(constrparams);
                
                this.composer = (Composer) constr.newInstance(newinstparams);

            /* If the component is not null, we make a new instance and hope */
            } else {
                this.composer = (Composer) clazz.newInstance();
            }
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException)
                if (t.getCause() != null) t = t.getCause();
            if (composer == null) composer = "default composer";
            else composer = "composer  class \"" + composer + "\"";
            throw new DeploymentException("Cannot create " + composer
                                          + " instance for block \""
                                          + instance + "\"", t);
        }

        /* Allocate the table of resource prefixes against wiring names */
        Iterator iterator = this.instance.wirings();
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            this.prefixes.put(name + ':', name);
        }

        /* Prepare the resolvers for this composer and wired blocks */
        this.priresolver.add(block.resolver(Descriptor.ACCESS_PRIVATE));
        while (block != null) {
            this.pubresolver.add(block.resolver(Descriptor.ACCESS_PUBLIC));
            this.priresolver.add(block.resolver(Descriptor.ACCESS_PROTECTED));
            block = (LoadedBlock) block.extendedBlock();
        }
    }

    /* ====================================================================== */

    /**
     * <p>Return a {@link Resolver} resolving this instance's public
     * {@link Resource}s.</p>
     *
     * @return a <b>non null</b> {@link Resolver} instance.
     */
    protected Resolver getResolver() {
        return(this.pubresolver);
    }
    
    /**
     * <p>Create and return a new {@link Wire} instance implementing the
     * specified {@link Class} role and resolving resources through a supplied
     * {@link Resolver}.</p>
     *
     * <p>The specified {@link Resolver} is the public resolver exposed by
     * another instance through the {@link DeployedWirings#getResolver()}
     * method.</p>
     *
     * @param role the interface {@link Class} role of the {@link Wire}.
     * @param resolver the {@link Resolver} to use.
     * @return a <b>non null</b> {@link Wire} implementing the specified role.
     * @throws WiringException if the {@link Wire} could not be created.
     */
    protected Wire newWire(Class role, Resolver resolver)
    throws WiringException {
        return(new ProxyWire(this.composer, role, resolver).getWire());
    }

    /* ====================================================================== */

    /**
     * <p>Resolve a {@link Resource} local to this block instance.</p>
     *
     * @return a <b>non null</b> {@link Resource} or <b>null</b> if not found.
     */
    public Resource resolve(String name) {
        /* No name? No resource! */
        if (name == null) return(null);
        if (name.length() == 0) return(null);

        /* Look in our resource wirings for the key matching the prefix */
        Iterator iterator = this.prefixes.keySet().iterator();
        while (iterator.hasNext()) {
            String prefix = (String) iterator.next();

            /* The prefix in rwirings is not the start of the name? Bye! */
            if (!name.startsWith(prefix)) continue;

            /* Access the other wirings public resolver and resolve */
            name = name.substring(prefix.length());
            prefix = (String) this.prefixes.get(prefix);
            Instance instance = this.instance.wiring(prefix);
            DeployedWirings target = this.deployer.lookup(instance);
            if (target != null) return(target.getResolver().resolve(name));
            throw new NullPointerException("Unknown wiring \"" + prefix + "\"");
        }

        /* No prefix matched, resolve locally */
        return(this.priresolver.resolve(name));
    }

    /**
     * <p>Look up and return a {@link Wire} instance accessible from this
     * block instance.</p>
     *
     * @param role the interface {@link Class} instance to which the returned
     *             {@link Wirings} must be castable to.
     * @param name the block's wiring name as required in the block's
     *               descriptor.
     * @return a <b>non null</b> {@link Wire} implementing the specified role.
     * @throws WiringException if an error occurred creating the instance.
     */
    public Wire lookup(Class role, String name)
    throws WiringException {
        /* Fail if either name or role are null */
        if (name == null) throw new WiringException("No name specified");
        if (role == null) throw new WiringException("No role specified");
        
        /* Look in our component wirings for the key matching the name */
        DeployedWirings target = deployer.lookup(this.instance.wiring(name));
        if (target != null) return(target.newWire(role, this.getResolver()));

        /* Wrong wiring name specified */
        throw new WiringException("Unknown wiring \"" + name + "\"");
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Notify this {@link DeployedWirings} of its initialization.</p>
     *
     * @throws LifecycleException if this instance cannot be initialized.
     */
    public void init()
    throws LifecycleException {
        try {
            this.composer.configure(this.instance.configuration());
            this.composer.contextualize(this); // TODO wrap this instance
        } catch (Throwable throwable) {
            throw new LifecycleException("Unable to configure or contextualize"
                                         + " composer instance", throwable);
        }
        if (this.composer instanceof Lifecycle) {
            ((Lifecycle)this.composer).init();
        }
    }
    
    /**
     * <p>Notify this {@link DeployedWirings} of its destruction.</p>
     *
     * @throws LifecycleException if this instance cannot be destroyed.
     */
    public void destroy()
    throws LifecycleException {
        if (this.composer instanceof Lifecycle) {
            ((Lifecycle)this.composer).destroy();
        }
    }
}
