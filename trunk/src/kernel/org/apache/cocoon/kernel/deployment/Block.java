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
package org.apache.cocoon.kernel.deployment;

import java.util.Iterator;

import org.apache.cocoon.kernel.identification.Descriptor;

/**
 * <p>The {@link Block} interface describes a &quot;static&quot; representation
 * of a generic block.</p>
 *
 * <p>An {@link Instance} is related to {@link Block} more or less in the same
 * way in which a {@link Object} instance is related to its {@link Class}.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.4 $)
 */
public interface Block {

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
    public boolean isAssignableFrom(Block requirement);
    
    /**
     * <p>Check whether this {@link Block} is an interface block or not.</p>
     *
     * @return <b>true</b> if this {@link Block} is an interface block,
     *         <b>false</b> otherwise.
     */
    public boolean isInterface();
    
    /**
     * <p>Return the {@link Descriptor} associated with this {@link Block}.</p>
     *
     * @return a <b>non null</b> {@link Descriptor} instance.
     */
    public Descriptor descriptor();
    
    /**
     * <p>Return the {@link Block} instance extended by this {@link Block}.</p>
     *
     * @return a {@link Block} instance or <b>null</b> if this {@link Block}
     *         does not extend another.
     */
    public Block extendedBlock();
    
    /**
     * <p>Return an iterator over all {@link Block} instances implemented by
     * this {@link Block}.</p>
     *
     * @return an {@link Iterator} over {@link Block} instances.
     */
    public Iterator implementedBlocks();
}
