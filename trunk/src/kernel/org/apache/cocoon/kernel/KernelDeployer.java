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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.kernel.configuration.Configuration;
import org.apache.cocoon.kernel.configuration.ConfigurationException;
import org.apache.cocoon.kernel.configuration.Parameters;
import org.apache.cocoon.kernel.deployment.Block;
import org.apache.cocoon.kernel.deployment.Deployer;
import org.apache.cocoon.kernel.deployment.DeploymentException;
import org.apache.cocoon.kernel.deployment.Instance;
import org.apache.cocoon.kernel.deployment.Loader;
import org.apache.cocoon.kernel.identification.BlockDescriptor;
import org.apache.cocoon.kernel.identification.Descriptor;
import org.apache.cocoon.kernel.identification.DescriptorBuilder;
import org.apache.cocoon.kernel.identification.IdentificationException;
import org.apache.cocoon.kernel.identification.Identifier;
import org.apache.cocoon.kernel.startup.Logger;

/**
 * <p>A {@link KernelDeployer} is a simple implementation of the {@link Deployer}
 * interface.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.9 $)
 */
public class KernelDeployer implements Deployer {

    /** <p>The XML configuration namespace.</p> */
    public static final String NAMESPACE = 
                            "http://apache.org/cocoon/kernel/deployer/1.0";
    
    /* ====================================================================== */

    /** <p>Our {@link Logger} instance.</p> */
    private Logger log = new Logger();

    /** <p>A map of {@link DeployedWirings}s by {@link Instance}.</p> */
    private Map wiringsByInstance = new HashMap();
    
    /** <p>A map of {@link DeployedWirings}s by name.</p> */
    private Map wiringsByName = new HashMap();
    
    /** <p>A simple block loader instance.</p> */
    private Loader loader = new BlockLoader(this.getClass().getClassLoader());
    
    /* ====================================================================== */
    
    /**
     * <p>Create a new {@link Deployer} instance.</p>
     */
    public KernelDeployer() {
        super();
    }

    /* ====================================================================== */
    
    /**
     * <p>Setup the {@link Logger} instance this component can use to
     * perform logging operations.</p>
     *
     * @param logger a <b>non null</b> {@link Logger} instance.
     */
    public void logger(Logger logger) {
        if (logger!= null) this.log = logger;
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Process a given {@link Configuration} preparing the instance of this
     * {@link Descriptor}'s main block loader.</p>
     *
     * @param configuration the {@link Configuration} to process.
     * @throws ConfigurationException if there was an error processing the
     *                                given {@link Configuration}.
     */
    public void configure(Configuration configuration)
    throws ConfigurationException, IdentificationException {
        
        /* Check the deployer configuration namespace */
        if (!NAMESPACE.equals(configuration.namespace()))
            throw new ConfigurationException("Invalid namespace declared \""
                                             + configuration.namespace()
                                             + "\" for configuration",
                                             configuration);
        
        /* Record each configured block and its configuration */
        Configuration current = null;
        Iterator iterator = configuration.children(NAMESPACE, "block");
        while (iterator.hasNext()) {
            
            /* Locate the local descriptor in the deployer configuration */
            current = (Configuration)iterator.next();
            String location = current.getStringAttribute("descriptor");
            
            /* Parse the block descriptor and get the configuration */
            URL url = null;
            try {
                url = new URL(configuration.locationURL(), location);
            } catch (MalformedURLException exception) {
                throw new ConfigurationException("Unable to relativize descript"
                                                 + "or location \"" + location
                                                 + "\" against configuration \""
                                                 + configuration.location()
                                                 + "\"");
            }

            /* Create a new descriptor instance */
            Descriptor descriptor = DescriptorBuilder.newInstance(url);
            if (this.loader.contains(descriptor)) {
                throw new ConfigurationException("Descriptor \"" + descriptor
                                         + "\" configured twice", current);
            }
            
            /* Store the descriptor in the library */
            this.loader.add(descriptor);
            
            /* Log this addition */
            this.log.debug("Descriptor for \"" + descriptor + "\" configured");
        }
    }

    /* ====================================================================== */

    /**
     * <p>Create a new {@link Instance} of a block specifying its
     * {@link Identifier}.</p>
     *
     * <p>Note that <b>only</b> non-interface blocks can be actively deployed.
     * Implemented interface blocks must be available for successful deployment,
     * but those will never be &quot;instantiated&quot; (like in Java &trade;
     * it is impossible to create new instances of interface classes).</p>
     *
     * @param identifier the {@link Identifier} of the block to deploy.
     * @param name an optional (possibly unique) {@link String} instance name.
     * @return a <b>non null</b> block {@link Instance}.
     * @throws DeploymentException if the block identified by the specified
     *                             {@link Identifier} could not be instantiated.
     */
    public Instance instantiate(Identifier identifier, String name)
    throws DeploymentException {
        /* Attempt to retrieve the block instance */
        try {
            LoadedBlock block = (LoadedBlock) this.loader.load(identifier);
            Instance instance = new DeployableInstance(block, name);
            this.wiringsByInstance.put(instance, null);
            this.wiringsByName.put(instance.name(), null);
            this.log.info("Block instance \"" + instance + "\" created");
            return(instance);
        } catch (Exception e) {
            throw new DeploymentException("Unable to create a new instance of "
                                          + "block \"" + identifier + "\"", e);
        }
    }

    /* ====================================================================== */

    /**
     * <p>Deploy the specified {@link Instance} instance previously created
     * by installing a {@link BlockDescriptor}.</p>
     *
     * @param instance the {@link Instance} instance to deploy.
     * @throws DeploymentException if the specified {@link Instance} could
     *                             not be deployed for any reason.
     */
    public void deploy(Instance instance)
    throws DeploymentException {
        /* Check that we created this instance and that it is deployable */
        if (!this.wiringsByInstance.containsKey(instance)) {
            throw new DeploymentException("Attempting to deploy unknown "
                                          + "instance \"" + instance + "\"");
        }
        if (this.wiringsByInstance.get(instance) != null) {
            throw new DeploymentException("Cannot deploy already deployed "
                                          + "instance \"" + instance + "\"");
        }
        if (!(instance instanceof DeployableInstance)) {
            throw new DeploymentException("Unable to deploy non-deployable"
                                          + "instance \"" + instance + "\"");
        }
        
        /* Simple cast of our instance */
        DeployableInstance deployable = (DeployableInstance) instance;

        /* We're deploying */
        this.log.debug("Deploying instance \"" + deployable + "\"");

        /* Retrieve block instance and descriptor */
        Block block = deployable.block();
        BlockDescriptor descriptor = (BlockDescriptor) block.descriptor();
        
        /* Check that all required wirings are there */
        Iterator iterator = descriptor.requirements();
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            Block required = this.loader.load(descriptor.requiredBlock(name));
            Instance wired = deployable.wiring(name);
            if (wired == null) {
                throw new DeploymentException("Required wiring \"" + name
                                              + "\" for \"" + required
                                              + "\" not provided for \""
                                              + deployable + "\"");
            } else if (!this.wiringsByInstance.containsKey(wired)) {
                throw new DeploymentException("Required wiring \"" + name
                                              + "\" for \"" + required
                                              + "\" has invalid target");
            }

            /* We should really process compatible versions */
            if (!wired.block().isAssignableFrom(required)) {
                throw new DeploymentException("Incompatible wiring \"" + name
                                              + "\" requiring \"" + required
                                              + "\" provided \"" + wired + "\""
                                              + " for \"" + deployable + "\"");
            }
        }
        
        /* Check that all required parameters are there */
        Parameters parameters = deployable.configuration();
        iterator = descriptor.parameters();
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            String type = descriptor.parameterType(name);
            if (parameters.verify(name, type)) continue;
            throw new DeploymentException("Incompatible or non specified "
                                          + "parameter \"" + name + "\" with "
                                          + "type \"" + type + "\" for \""
                                          + deployable + "\"");
        }

        /* Deploy the sucker (finally) */
        DeployedWirings deployed = new DeployedWirings(deployable, this);
        try {
            deployed.init();
        } catch (Throwable t) {
            throw new DeploymentException("Error initializing wirings", t);
        }
        this.wiringsByInstance.put(deployable, deployed);
        this.wiringsByName.put(instance.name(), deployed);
        deployable.deployed(true);
    }

    /* ====================================================================== */

    /**
     * <p>Replace the specified block {@link Instance} with another one.</p>
     *
     * <p>This method will replace a deployed {@link Instance} with another,
     * rewiring all wired blocks to the new {@link Instance}.</p>
     *
     * <p>The old {@link Instance} will not be destroyed, but still returned
     * by the {@link #instances()} method, but its {@link Instance#deployed()}
     * will return <b>false</b>.</p>
     *
     * @param old the old {@link Instance} instance to replace.
     * @param instance the {@link Instance} replacing the old {@link Instance}.
     * @throws DeploymentException if the specified {@link Instance} could
     *                             not be replaced for any reason.
     */
    public void replace(Instance old, Instance instance)
    throws DeploymentException {
        throw new UnsupportedOperationException();
    }

    /* ====================================================================== */

    /**
     * <p>Destroy the specified deployed block {@link Instance}.</p>
     *
     * <p>If the specified {@link Instance} is deployed <b>and</b> is required
     * by any other block (there is an active wiring), this method will throw
     * a {@link DeploymentException}.</p>
     *
     * <p>If this {@link Instance} has been replaced by another or has not been
     * wired to any other block, all the eventual wirings to it will be cut and
     * it will be destroyed.</p>
     *
     * @param instance the {@link Instance} instance to deploy.
     * @throws DeploymentException if the specified {@link Instance} could
     *                             not be deployed for any reason.
     */
    public void destroy(Instance instance)
    throws DeploymentException {
        throw new UnsupportedOperationException();
    }
    
    /* ====================================================================== */

    /**
     * <p>Return an {@link Iterator} over all {@link Instance}s known by this
     * {@link Deployer}.</p>
     *
     * <p>This method will return all instances known by this {@link Deployer},
     * whether they have been simply installed, deployed, or have been replaced
     * by other instances. To check if an {@link Instance} is actively deployed
     * the {@link Instance#deployed()} method can be called.</p>
     */
    public Iterator instances() {
        return(new HashSet(this.wiringsByInstance.keySet()).iterator());
    }

    /* ====================================================================== */

    /**
     * <p>Return a {@link DeployedWirings} instance associated with the
     * specified block {@link Instance}, if any.</p>
     * 
     * @param instance a deployed block {@link Instance} to look up.
     * @return a {@link DeployedWirings} instance or <b>null</b> if the
     *         specified {@link Instance} was not deployed.
     */
    protected DeployedWirings lookup(Instance instance) {
        return((DeployedWirings)this.wiringsByInstance.get(instance));
    }

    /**
     * <p>Return a {@link DeployedWirings} instance associated with the
     * specified block name, if any.</p>
     * 
     * @param name a deployed block name (from configurations) to look up.
     * @return a {@link DeployedWirings} instance or <b>null</b> if the
     *         specified {@link Instance} was not deployed.
     */
    protected DeployedWirings lookup(String name) {
        return((DeployedWirings)this.wiringsByName.get(name));
    }
}
