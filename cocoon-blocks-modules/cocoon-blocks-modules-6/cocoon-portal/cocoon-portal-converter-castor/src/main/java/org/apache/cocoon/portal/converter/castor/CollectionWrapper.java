/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.converter.castor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Holds mapped instances.
 *
 * @version $Id$
 */
public class CollectionWrapper implements Collection {

    protected Collection objects;

    public CollectionWrapper() {
        this.objects = new ArrayList();
    }

    public CollectionWrapper(Collection c) {
        this.objects = c;
    }

    public Collection getObjects() {
        return this.objects;
    }

    /**
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(Object o) {
        return this.objects.add(o);
    }

    /**
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection c) {
        return this.objects.addAll(c);
    }

    /**
     * @see java.util.Collection#clear()
     */
    public void clear() {
        this.objects.clear();
    }

    /**
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        return this.objects.contains(o);
    }

    /**
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection c) {
        return this.objects.containsAll(c);
    }

    /**
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
        return this.objects.isEmpty();
    }

    /**
     * @see java.util.Collection#iterator()
     */
    public Iterator iterator() {
        return this.objects.iterator();
    }

    /**
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        return this.objects.remove(o);
    }

    /**
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection c) {
        return this.objects.removeAll(c);
    }

    /**
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection c) {
        return this.objects.retainAll(c);
    }

    /**
     * @see java.util.Collection#size()
     */
    public int size() {
        return this.objects.size();
    }

    /**
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray() {
        return this.objects.toArray();
    }

    /**
     * @see java.util.Collection#toArray(java.lang.Object[])
     */
    public Object[] toArray(Object[] a) {
        return this.objects.toArray(a);
    }
}
