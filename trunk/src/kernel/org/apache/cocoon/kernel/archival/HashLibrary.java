/* ========================================================================== *
 *                                                                            *
 * Copyright 2004 The Apache Software Foundation.                             *
 *                                                                            *
 * Licensed  under the Apache License,  Version 2.0 (the "License");  you may *
 * not use this file except in compliance with the License.  You may obtain a *
 * copy of the License at                                                     *
 *                                                                            *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 *                                                                            *
 * Unless  required  by  applicable law or  agreed  to in  writing,  software *
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.           *
 *                                                                            *
 * See  the  License for  the  specific language  governing  permissions  and *
 * limitations under the License.                                             *
 *                                                                            *
 * ========================================================================== */
package org.apache.cocoon.kernel.archival;

import java.util.HashSet;
import java.util.Iterator;
import org.apache.cocoon.kernel.identification.Descriptor;
import org.apache.cocoon.kernel.identification.Identifier;
import org.apache.cocoon.kernel.identification.ParsedIdentifier;

/**
 * <p>The {@link HashLibrary} class is a simple implementation of a
 * {@link Library} backed up by a Java&trade; {@link HashSet}.</p>
 *
 * <p>This default implementation extends an in-memory {@link HashSet}, but
 * extensions of this class can (for example) mirror an entire block structure
 * from the network to a local disk to allow local block deployment.</p>
 *
 * <p>For this purpose it is allowed to the {@link #add(Descriptor)} method
 * to return a modified version of a {@link Descriptor} different from the
 * one specified, with, for example, different path names for libraries
 * used by the block.</p>
 * 
 * <p>Additionally, <b>immutable</b> libraries are allowed to throw
 * {@link UnsupportedOperationException}s in those methods modifying the
 * state of the library (for example, libraries simply performing lookup
 * operations on remote servers).</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public class HashLibrary extends HashSet implements Library {

    /** <p>A flag identifying whether this instance is locked or not.</p> */
    private boolean locked = false;

    /**
     * <p>Create a new empty {@link HashLibrary} instance.</p>
     */
    public HashLibrary() {
        super();
    }

    /* ====================================================================== */
    
    /**
     * <p>Archive a new {@link Descriptor} in this {@link HashLibrary}.</p>
     *
     * @param object the {@link Descriptor} to archive.
     * @return <b>true</b> if the contents of this {@link Library} were changed
     *         by this operation, <b>false</b> otherwise.
     * @throws UnsupportedOperationException if this {@link Library} is
     *                                       immutable (locked).
     * @throws ClassCastException if the object is not a {@link Descriptor}.
     */
    public boolean add(Object object) {
        if (this.locked) throw new UnsupportedOperationException("Locked");
        if (!(object instanceof Descriptor)) {
            throw new ClassCastException("Library cannot store instances of "
                                         + object.getClass().getName());
        }
        if (this.contains(object)) return(false);
        return(super.add(object));
    }

    /**
     * <p>Archive a new {@link Descriptor} in this {@link HashLibrary}.</p>
     *
     * <p>The returned {@link Descriptor} might not be the one specified,
     * as the archiving operation might cache {@link Descriptor} instances
     * and modify them for the local deployment environment (for example,
     * modifying library paths from the original URLs to local files).</p>
     *
     * @param descriptor the {@link Descriptor} to archive.
     * @return a {@link Descriptor} instance.
     * @throws UnsupportedOperationException if this {@link Library} is
     *                                       immutable.
     */
    public Descriptor add(Descriptor descriptor) {
        if (this.contains(descriptor)) {
            Iterator iterator = this.iterator();
            while (iterator.hasNext()) {
                Descriptor current = (Descriptor) iterator.next();
                if (current.equals(descriptor)) return(current);
            }
        }
        /* Doublecheck */
        if (this.add((Object)descriptor)) return(descriptor);
        /* This should _NEVER_ happen */
        throw new RuntimeException("Library implementation failure");
    }
    
    /**
     * <p>Retrieve a previously archived {@link Descriptor}.</p>
     *
     * @param identifier the {@link Identifier} of the {@link Descriptor}.
     * @return a {@link Descriptor} instance or <b>null</b> if not found.
     */
    public Descriptor get(Identifier identifier) {
        if (this.contains(identifier)) {
            Iterator iterator = this.iterator();
            while (iterator.hasNext()) {
                Descriptor descriptor = (Descriptor) iterator.next();
                if (descriptor.equals(identifier)) return(descriptor);
            }
        }
        return(null);
    }

    /**
     * <p>Remove a previously archived {@link Descriptor}.</p>
     *
     * @return <b>true</b> if the contents of this {@link Library} were changed
     *         by this operation, <b>false</b> otherwise.
     * @throws UnsupportedOperationException if this {@link Library} is
     *                                       immutable (locked).
     */
    public boolean remove(Object object) {
        if (this.locked) throw new UnsupportedOperationException("Locked");
        return(super.remove(object));
    }

    /**
     * <p>Retrieve a previously archived {@link Descriptor}.</p>
     *
     * @param identifier the {@link String} id of the {@link Descriptor}.
     * @return a {@link Descriptor} instance or <b>null</b> if not found.
     */
    public Descriptor get(String identifier) {
        try {
            return(this.get(new ParsedIdentifier(identifier)));
        } catch (Exception e) {
            return(null);
        }
    }

    /* ====================================================================== */

    /**
     * <p>Lock this instance, forcing all addition and removal operations to
     * fail with an {@link UnsupportedOperationException}.</p>
     */
    protected void lock() {
        this.locked = true;
    }
}
