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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.cocoon.kernel.identification.Descriptor;
import org.apache.cocoon.kernel.identification.Identifier;
import org.apache.cocoon.kernel.deployment.Block;
import org.apache.cocoon.kernel.deployment.DeploymentException;
import org.apache.cocoon.kernel.deployment.Loader;
import org.apache.cocoon.kernel.resolution.LocalResolver;

/**
 * <p>A {@link LoadedBlock} implements the {@link Block} interface and is
 * the default implementation returned by a {@link BlockLoader}.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.4 $)
 */
public class LoadedBlock implements Block {
    
    /** <p>The {@link Descritor} of this {@link Block}.</p> */
    private Descriptor descriptor = null;

    /** <p>The {@link Block} we extend.</p> */
    private LoadedBlock extended = null;
    
    /** <p>All {@link Block}s we implement.</p> */
    private Set implemented = new HashSet();

    /** <p>Our {@link LocalResolver}s.</p> */
    private LocalResolver resolvers[] = new LocalResolver[3];
    
    /** <p>Our private {@link ClassLoader}.</p> */
    private ClassLoader privLoader = null;
    
    /** <p>Our protected {@link ClassLoader}.</p> */
    private ClassLoader protLoader = null;
    
    /* ====================================================================== */

    /**
     * <p>Create a new {@link LoadedBlock} instance. </p>
     *
     * @param descriptor the {@link Descriptor} to associate with this
     *                   {@link Block}.
     * @param loader the {@link Loader} to resolve implemented and extended
     *               {@link Block}s.
     */
    public LoadedBlock(Descriptor descriptor, BlockLoader loader)
    throws DeploymentException {
        if (descriptor == null) throw new NullPointerException();
        if (loader == null) throw new NullPointerException();
        this.descriptor = descriptor;
        
        /* Set up extended block */
        if (descriptor.extendedBlock() != null) {
            extended = (LoadedBlock)loader.load(descriptor.extendedBlock());
            Iterator iterator = this.extended.implementedBlocks();
            while (iterator.hasNext()) {
                this.implemented.add(iterator.next());
            }
        }

        /* Process our implemented interfaces */
        Iterator iterator = descriptor.implementedBlocks();
        while (iterator.hasNext()) {
            this.implemented.add(loader.load((Identifier)iterator.next()));
        }

        /* Allocate our resolvers */
        for (int k = 0; k < 3; k ++) try {
            this.resolvers[k] = new LocalResolver(descriptor.libraries(k));
        } catch (IOException e) {
            throw new DeploymentException("Can't process block libraries", e);
        }
        
        /* Prepare our class loaders */
        try {
            this.prepareLoaders(loader.classLoader());
        } catch (MalformedURLException e) {
            throw new DeploymentException("Can't create class loaders", e);
        }
    }

    /* ====================================================================== */
    
    /**
     * <p>Compare an {@link Object} for equality.</p>
     *
     * <p>A specified {@link Object} equals this one if it is a {@link Block}
     * instance, and its {@link Descriptor} equals our own.</p>
     *
     * @param object an {@link Object} to compare for equality.
     * @return <b>true</b> if the specified object equals this {@link Block}
     *         instance, <b>false</b> otherwise.
     */
    public boolean equals(Object object) {
        /* Simple check */
        if (object == null) return (false);
        
        /* If the o is a Identifier, URL or String compare it using strings */
        if (object instanceof Block) {
            return(this.descriptor().equals(((Block)object).descriptor()));
        }

        /* In all other cases, we're not equals */
        return(false);
    }

    /**
     * <p>Return the hash code of this {@link Identifier}.</p>
     *
     * @return the hash code.
     */
    public int hashCode() {
        return(this.descriptor().hashCode());
    }
    
    /**
     * <p>Return the full URL of this identifier as a {@link String}.</p>
     *
     * @return a <b>non null</b> {@link String}.
     */
    public String toString() {
        return(this.descriptor().toString());
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Check wether this {@link Block} is assignable from the specified
     * {@link Block}.</p>
     *
     * <p>Assignability means that this block either extends through
     * inheritance the specified {@link Block}, or that one of its specified
     * or inherited implemented interfaces is the specified {@link Block}.</p>
     *
     * @param requirement the {@link Block} to check for assignability.
     * @return <b>true</b> if this {@link Block} can is assignable as the
     *         specified {@link Block}, <b>false</b> otherwise.
     */
    public boolean isAssignableFrom(Block requirement) {
        /* Are we equals? */
        if (requirement.equals(this)) return(true);

        /* Check if the specified block is assignable from the interfaces */
        Iterator iterator = this.implementedBlocks();
        while (iterator.hasNext()) {
            if (((Block)iterator.next()).isAssignableFrom(requirement)) {
                return(true);
            }
        }

        /* Check if the specified block is assignable from the extended */
        if (this.extendedBlock() != null) {
            return(this.extendedBlock().isAssignableFrom(requirement));
        }

        /* Boo-boo */
        return(false);
    }

    /**
     * <p>Check whether this {@link Block} is an interface block or not.</p>
     *
     * @return <b>true</b> if this {@link Block} is an interface block,
     *         <b>false</b> otherwise.
     */
    public boolean isInterface() {
        return(this.descriptor.isInterface());
    }

    /**
     * <p>Return the {@link Descriptor} associated with this {@link Block}.</p>
     *
     * @return a <b>non null</b> {@link Descriptor} instance.
     */
    public Descriptor descriptor() {
        return(this.descriptor);
    }

    /**
     * <p>Return the {@link Block} instance extended by this {@link Block}.</p>
     *
     * @return a {@link Block} instance or <b>null</b> if this {@link Block}
     *         does not extend another.
     */
    public Block extendedBlock() {
        return(this.extended);
    }

    /**
     * <p>Return an iterator over all {@link Block} instances implemented by
     * this {@link Block}.</p>
     *
     * @return an {@link Iterator} over {@link Block} instances.
     */
    public Iterator implementedBlocks() {
        return(new HashSet(this.implemented).iterator());
    }

    /* ====================================================================== */
    
    /**
     * <p>Return the {@link LocalResolver} associated with the specified access
     * level.</p>
     *
     * @param access a number as passed to {@link Descriptor#libraries(int)}.
     * @return a <b>non null</b> {@link LocalResolver} instance.
     */
    protected LocalResolver resolver(int access) {
        return(this.resolvers[access]);
    }

    /**
     * <p>Return the {@link ClassLoader} associated with the specified access
     * level.</p>
     *
     * @param access a number as passed to {@link Descriptor#libraries(int)}.
     * @return a <b>non null</b> {@link ClassLoader} instance, or <b>null</b>
     *         if no {@link ClassLoader} was available for the access.
     */
    protected ClassLoader loader(int access) {
        if (access == Descriptor.ACCESS_PRIVATE) return(this.privLoader);
        if (access == Descriptor.ACCESS_PROTECTED) return(this.protLoader);
        return(null);
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Prepare all {@link ClassLoader}s for this block.</p>
     *
     * @param root the root of all blocks class loaders.
     */
    private void prepareLoaders(ClassLoader root)
    throws MalformedURLException {
        this.protLoader = this.prepareLoader(Descriptor.ACCESS_PROTECTED, root);
        this.privLoader = this.prepareLoader(Descriptor.ACCESS_PRIVATE, root);
    }

    /**
     * <p>Prepare the {@link ClassLoader} associated with the specified access
     * level.</p>
     *
     * @param access a number as passed to {@link Descriptor#libraries(int)}.
     * @return a <b>non null</b> {@link ClassLoader} instance.
     */
    private ClassLoader prepareLoader(int access, ClassLoader root)
    throws MalformedURLException {

        /* Resolve our parent ClassLoader */
        ClassLoader parent = null;
        if (access == Descriptor.ACCESS_PRIVATE) {
            /* If creating the private class loader, proteced is parent */
            parent = this.protLoader;
        } else if (this.extended != null) {
            /* If creating the protected class loader, parent block is parent */
            parent = this.extended.loader(Descriptor.ACCESS_PROTECTED);
        }
        /* If the parent is null, the parent is root */
        if (parent == null) parent = root;

        /* Access the URLs of this class loader and create it */
        URL urls[] = this.resolver(access).urls();
        return(new URLClassLoader(urls, parent));
    }
}
