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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.kernel.configuration.Configuration;
import org.apache.cocoon.kernel.configuration.ConfigurationException;
import org.apache.cocoon.kernel.configuration.Parameters;
import org.apache.cocoon.kernel.deployment.Deployer;
import org.apache.cocoon.kernel.deployment.DeploymentException;
import org.apache.cocoon.kernel.deployment.Instance;
import org.apache.cocoon.kernel.identification.Descriptor;
import org.apache.cocoon.kernel.identification.IdentificationException;
import org.apache.cocoon.kernel.identification.Identifier;
import org.apache.cocoon.kernel.identification.ParsedIdentifier;

/**
 * <p>An {@link Installer} processes a given configuration, installing and
 * deploying each block in the correct order.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.6 $)
 */
public class Installer {

    /** <p>The {@link Deployer} XML configuration namespace.</p> */
    public static final String NAMESPACE = KernelDeployer.NAMESPACE;
    
    /* ====================================================================== */
    
    /** <p>The {@link Set} of all already deployed instances.</p> */
    private Set deployed = new HashSet();
    
    /** <p>The {@link Set} of the instances we're currently deploying.</p> */
    private Set deploying = new HashSet();
    
    /** <p>The {@link Deployer} instance used for deployment.</p> */
    private Deployer deployer = null;

    /** <p>The {@link HashMap} of all instances by name.</p> */
    private Map instances = new HashMap();

    /** <p>The {@link HashMap} of all instances wirings.</p> */
    private Map configurations = new HashMap();
    
    /* ====================================================================== */
    
    /**
     * <p>Create a new {@link Installer} installing and deploying blocks
     * on a specified block {@link Deployer}.</p>
     *
     * @param deployer a <b>non null</b> {@link Deployer} instance.
     * @throws NullPointerException if the {@link Deployer} was <b>null</b>.
     */
    public Installer(Deployer deployer) {
        if (deployer == null) throw new NullPointerException();
        this.deployer = deployer;
    }

    /* ====================================================================== */

    /**
     * <p>Process a given {@link Configuration} installing all instantiated
     * blocks, and later deploying them.</p>
     *
     * @param configuration the {@link Configuration} for the {@link Deployer}.
     * @throws ConfigurationException if there was an error processing the
     *                                given {@link Configuration}
     */
    public void process(Configuration configuration)
    throws ConfigurationException, IdentificationException {

        /* Check the deployer configuration namespace */
        if (!NAMESPACE.equals(configuration.namespace()))
            throw new ConfigurationException("Invalid namespace declared \""
                                             + configuration.namespace()
                                             + "\" for configuration",
                                             configuration);
        
        /* Record each configured block and its configuration */
        Configuration current = null;
        Descriptor descriptor = null;
        Iterator iterator = configuration.children(NAMESPACE, "instance");
        while (iterator.hasNext()) try {
            
            /* Locate the local block name (id or alias) */
            current = (Configuration)iterator.next();
            String block = current.getStringAttribute("block");
            String name = current.getStringAttribute("name");
            
            /* Prepare an identifier for the specified block */
            Identifier identifier = new ParsedIdentifier(block);
            
            /* Create a new block instance, and check it */
            Instance instance = this.deployer.instantiate(identifier, name);
            if (this.instances.put(name, instance) != null) {
                throw new ConfigurationException("Duplicate block instance "
                                                 + "specified for name \""
                                                 + name + "\"", current);
            }

            /* Store all configured parameters and wirings for deployment */
            this.configurations.put(name, current);

        } catch (DeploymentException e) {
            /* Whops, install made a boo-boo */
            throw new ConfigurationException("Unable to install block \""
                                             + descriptor, current, e);
        }

        /* Process each instance one-by-one */
        iterator = this.configurations.keySet().iterator();
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            this.process(name, (Configuration)this.configurations.get(name));
        }
    }

    /* ====================================================================== */
    
    /**
     * <p>Recursively process a configured block instance, deploying first
     * all its wired block, and then deploying its instance.</p>
     *
     * <p>This method will take care of both resolving and deploying all
     * wired block instances, and setting up each {@link Instance} with
     * all its required parameters.</p>
     * 
     * @param name the name of the instance as specified in the configuration.
     * @param configuration the element containing both wirings and parameters.
     * @throws ConfigurationException if there was an error deploying, pssibly
     *                                wraps a {@link DeploymentException} adding
     *                                specifics about the broken configuration.
     */
    protected Instance process(String name, Configuration configuration)
    throws ConfigurationException {
        /* Get our instance */
        Instance instance = (Instance)this.instances.get(name);
        if (instance == null) {
            throw new ConfigurationException("Internal error: cannot retrieve "
                                             + " block instance for \"" + name
                                             + "\"", configuration);
        }
        
        /* Check if this instance was already deployed */
        if(this.deployed.contains(name)) return(instance);
        
        /* Check if this instance is being deployed */
        if(this.deploying.contains(name)) {
            throw new ConfigurationException("Circular wiring dependancy for "
                                             + "instance \"" + name + "\"",
                                             configuration);
        }

        /* All is cool, no circular dependancies */
        this.deploying.add(name);

        /* Process wirings first */
        Configuration current = configuration.child("wirings");
        Iterator iterator = current.children("requirement");
        while (iterator.hasNext()) {
            current = (Configuration) iterator.next();
            String rname = current.getStringAttribute("name");
            String rinst = current.getStringAttribute("instance");
            Configuration rconf = (Configuration)this.configurations.get(rinst);
            if (rconf == null) {
                throw new ConfigurationException("Can not retrieve instance \""
                                                 + rinst + "\" to wire to \""
                                                 + name + "\"", current);
            }

            /* Attempt to perform the actual wiring */
            instance.wire(rname, this.process(rinst, rconf));
        }

        /* Process parameters */
        try {
            instance.configure(new Parameters(configuration.child("parameters")));
        } catch (ConfigurationException exception) {
            throw(exception);
        } catch (Exception exception) {
            String message = "Problems configuring instnace \"" + name + "\""; 
            throw new ConfigurationException(message, exception);
        }

        /* Finally, deploy the baby*/
        try {
            this.deployer.deploy(instance);
        } catch (DeploymentException e) {
            throw new ConfigurationException("Unable to deploy instance \""
                                             + name + "\"", current, e);
        }

        /* We have deployed the block, we can modify the lists */
        this.deploying.remove(name);
        this.deployed.add(name);
        return(instance);
    }
}
