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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.kernel.archival.HashLibrary;
import org.apache.cocoon.kernel.archival.Library;
import org.apache.cocoon.kernel.deployment.Block;
import org.apache.cocoon.kernel.deployment.DeploymentException;
import org.apache.cocoon.kernel.deployment.Loader;
import org.apache.cocoon.kernel.identification.Descriptor;
import org.apache.cocoon.kernel.identification.Identifier;

/**
 * <p>A {@link BlockLoader} provides a default implementation of the
 * {@link Loader} interface.</p>
 *
 * <p>This class will always return unwrapped instances of {@link LoadedBlock}
 * in every method returning a {@link Block}.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.7 $)
 */
public class BlockLoader extends HashLibrary implements Loader {

    /** <p>The {@link HashMap} of all created blocks.</p> */
    private Map blocks = new HashMap();

    /** <p>The {@link Set} of the blocks we're creating.</p> */
    private Set creating = new HashSet();

    /** <p>The {@link Library} instance to use.</p> */
    private Library library = null;

    /** <p>The {@link ClassLoader} root of all blocks.</p> */
    private RootClassLoader loader = null;
    
    /* ====================================================================== */
    
    /**
     * <p>Create a new {@link BlockLoader} instance.</p>
     */
    public BlockLoader() {
        this(null);
    }
    
    /**
     * <p>Create a new {@link BlockLoader} instance.</p>
     *
     * @param loader the parent {@link ClassLoader} for all blocks.
     */
    public BlockLoader(ClassLoader loader) {
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
        }
        this.loader = new RootClassLoader(loader);
    }

    /* ====================================================================== */
    
    /**
     * <p>Return the parent {@link ClassLoader} for all blocks.</p>
     *
     * @return a <b>non null</b> {@link ClassLoader} instance.
     */
    protected ClassLoader classLoader() {
        return(this.loader);
    }
    
    /* ====================================================================== */

    /**
     * <p>Load the instance of the {@link Block} described by the specified
     * {@link String}.</p>
     *
     * @param identifier the {@link String} identifier of the {@link Block}
     * @return a <b>non null</b> {@link LoadedBlock} instance.
     * @throws DeploymentException if an error occurred loading the instance.
     */
    public Block load(String identifier)
    throws DeploymentException {
        Descriptor descriptor = this.get(identifier);
        if (descriptor != null) return(this.load(descriptor));
        throw new DeploymentException("Unable to retrieve descriptor for block "
                                      + "\"" + identifier + "\"");
    }

    /**
     * <p>Load the instance of the {@link Block} described by the specified
     * {@link Identifier}.</p>
     *
     * @param identifier the {@link Identifier} of the {@link Block} to load.
     * @return a <b>non null</b> {@link LoadedBlock} instance.
     * @throws DeploymentException if an error occurred loading the instance.
     */
    public Block load(Identifier identifier)
    throws DeploymentException {
        Descriptor descriptor = this.get(identifier);
        if (descriptor != null) return(this.load(descriptor));
        throw new DeploymentException("Unable to retrieve descriptor for block "
                                      + "\"" + identifier + "\"");
    }

    /**
     * <p>Load the instance of the {@link Block} described by the specified
     * {@link Descriptor}.</p>
     *
     * @param descriptor the {@link Descriptor} of the {@link Block} to load.
     * @return a <b>non null</b> {@link LoadedBlock} instance.
     * @throws DeploymentException if an error occurred loading the instance.
     */
    public LoadedBlock load(Descriptor descriptor)
    throws DeploymentException {

        /* Check if we already have a block instance. */
        if (this.blocks.containsKey(descriptor)) {
            return((LoadedBlock)this.blocks.get(descriptor));
        }

        /* Make sure that we don't end up in an infinite loop */
        if (this.creating.contains(descriptor)) {
            throw new DeploymentException("Circular dependancy creating"
                                          + "block \"" + descriptor + "\"");
        }

        /* Record the descriptor to check for circular dependancies (bad) */
        this.creating.add(descriptor);

        /* Create the actual block */
        LoadedBlock block = new LoadedBlock(descriptor, this);

        /* If it is an interface, its public URLs will be added to root */
        if (block.isInterface()) {
            /* Check for different versions of the same interface */
            Iterator iterator = this.blocks.keySet().iterator();
            while (iterator.hasNext()) {
                Descriptor current = ((Descriptor)iterator.next());
                if (!descriptor.base().equals(current.base())) continue;
                throw new DeploymentException("Unable to load interface \""
                                              + descriptor + "\" as another"
                                              + " incompatible version ("
                                              + current.version() +
                                              ") was found");
            }
            /* If we got here w/o exceptions we can add the interface urls */
            try {
                URL urls[] = block.resolver(Descriptor.ACCESS_PUBLIC).urls();
                this.loader.addURL(urls);
            } catch (Exception e) {
                throw new DeploymentException("Unable to append interface \""
                                              + descriptor + "\" libraries to "
                                              + "blocks class loader");
            }
        }

        /* Record the new instance */
        this.blocks.put(descriptor, block);

        /* Remove circular dependancies checking and return the block */
        this.creating.remove(descriptor);
        return(block);
    }

    /* ====================================================================== */

    /**
     * <p>A simple extension of a {@link URLClassLoader} republishing the
     * {@link #addURL(URL)} method.</p>
     *
     * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
     * @version 1.0 (CVS $Revision: 1.7 $)
     */
    private static final class RootClassLoader extends URLClassLoader {

        /**
         * <p>Create a new instance with a parent {@link ClassLoader}.</p>
         */
        private RootClassLoader(ClassLoader parent) {
            super(new URL[0], parent);
        }

        /**
         * <p>Add a {@link URL} to the ones managed by this instance.</p>
         */
        protected void addURL(URL url) {
            super.addURL(url);
        }

        /**
         * <p>Add an array of {@link URL}s to the ones managed by this
         * instance.</p>
         */
        protected void addURL(URL urls[]) {
            for (int x = 0; x < urls.length; x++) this.addURL(urls[x]);
        }
    }
}
