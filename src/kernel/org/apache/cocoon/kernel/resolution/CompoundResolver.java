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
package org.apache.cocoon.kernel.resolution;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * <p>A {@link CompoundResolver} is an implementation of the {@link Resolver}
 * interface delegating resource resolution to other {@link Resolver}s.</p>
 *
 * <p>This instance is backed up by an {@link ArrayList}.</p>
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.3 $)
 */
public class CompoundResolver extends AbstractSet implements Resolver {

    /** <p>Our {@link List}.</p> */
    private List list = new ArrayList();

    /**
     * <p>Create a new {@link CompoundResolver} instance backed up by another
     * {@link Resolver} instance.</p>
     */
    public CompoundResolver() {
        super();
    }

    /**
     * <p>Resolve a specified name into a {@link Resource}.</p>
     *
     * @param name a non null {@link String} identifying the resource name.
     * @return a {@link Resource} instance or <b>null</b> if not found.
     */
    public Resource resolve(String name) {
        Resolver r[] = (Resolver[])list.toArray(new Resolver[list.size()]);
        for (int x = 0; x < r.length; x ++) {
            Resource s = r[x].resolve(name);
            if (s != null) return(s);
        }
        return(null);
    }

    /**
     * <p>Returns an {@link Iterator} over all {@link Resolver} elements
     * contained in this {@link CompoundResolver}.</p>
     *
     * @return a <b>non null</b> {@link Iterator} instance.
     */
     public Iterator iterator() {
        return(this.list.iterator());
    }
    
    /**
     * <p>Returns the number of all {@link Resolver} elements contained in this
     * {@link CompoundResolver}.</p>
     *
     * @return a non negative number.
     */
    public int size() {
        return(this.list.size());
    }

    /**
     * <p>Checks whether this {@link CompoundResolver} contains the specified
     * object instance.</p>
     *
     * <p>Note that this implementation <b>does not</b> check on sub-elements
     * implementing the {@link Collection} iterface, it is therefore non
     * recursive.</p>
     *
     * @see #add(Object)
     * @return <b>true</b> if this {@link CompoundResolver} contains the
     *         specified {@link Object} directly, <b>false</b> otherwise.
     */
    public boolean contains(Object object) {
        return(this.list.contains(object));
    }

    /**
     * <p>Return a {@link Resolver} array of all elements contained in this
     * {@link CompoundResolver}.</p>
     *
     * @return an <b>non null</b> array castable to a {@link Resolver} array.
     */
    public Object[] toArray() {
        return(this.list.toArray(new Resolver[this.list.size()]));
    }
    
    /**
     * <p>Return an array of all elements contained in this
     * {@link CompoundResolver}.</p>
     *
     * @param array the array into which the elements of the collection are to
     *              be stored, if it is big enough; otherwise, a new array of
     *              the same runtime type is allocated for this purpose.
     * @return an <b>non null</b> array castable to a {@link Resolver} array.
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in this
     *         collection.
     */
    public Object[] toArray(Object array[]) {
        return(this.list.toArray(array));
    }

    /**
     * <p>Ensures that this {@link CompoundResolver} contains the specified
     * element.</p>
     *
     * <p>Note that this method will check all {@link Resolver}s contained in
     * this {@link CompoundResolver}, and recursively the contents of all those
     * elements also implementing the {@link Collection} interface.
     * 
     * @param object a {@link Object} castable to {@link Resolver}.
     * @return <b>true</b> if this instance changed in result of the call.
     * @throws ClassCastException if the specified {@link Object} is not an
     *                            instance of {@link Resolver}.
     */
    public boolean add(Object object) {
        if (object == null) return(false);
        Resolver resolver = (Resolver) object;
        Iterator iterator = this.list.iterator();
        while (iterator.hasNext()) {
            Resolver current = (Resolver) iterator.next();
            if (current == resolver) return(false);
            if (!(current instanceof Collection)) continue;
            if (((Collection)current).contains(resolver)) return(false);
        }
        return(this.list.add(resolver));
    }

    /**
     * <p>Ensures that this {@link CompoundResolver} does not contain the
     * specified element.</p>
     *
     * <p>Note that this implementation <b>does not</b> check on sub-elements
     * implementing the {@link Collection} iterface, it is therefore non
     * recursive.</p>
     *
     * @param object a {@link Object} to check.
     * @return <b>true</b> if this instance changed in result of the call.
     */
    public boolean remove(Object object) {
        if (object == null) return(false);
        return(this.list.remove(object));
        
    }

    /**
     * <p>Removes all of the elements from this {@link CompoundResolver}.</p>
     */
    public void clear() {
        this.list.clear();
    }
}
