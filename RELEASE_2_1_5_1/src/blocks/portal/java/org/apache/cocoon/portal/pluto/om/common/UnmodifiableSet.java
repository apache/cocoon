/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto.om.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: UnmodifiableSet.java,v 1.3 2004/03/05 13:02:15 bdelacretaz Exp $
 */
public class UnmodifiableSet implements Set, Serializable {

    // use serialVersionUID from JDK 1.2.2 for interoperability
    private static final long serialVersionUID = 1820017752578914078L;

    protected Set c;

    public UnmodifiableSet(Set c)
    {
        if (c == null) {
            throw new NullPointerException();
        }
        this.c = c;
    }

    public int size()
    {
        return c.size();
    }

    public boolean isEmpty()
    {
        return c.isEmpty();
    }

    public boolean contains(Object o)
    {
        return c.contains(o);
    }

    public Object[] toArray()
    {
        return c.toArray();
    }

    public Object[] toArray(Object[] a)
    {
        return c.toArray(a);
    }

    public String toString()
    {
        return c.toString();
    }

    public Iterator iterator()
    {
        return new Iterator()
        {
            Iterator i = c.iterator();

            public boolean hasNext()
            {
                return i.hasNext();
            }

            public Object next()
            {
                return i.next();
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean add(Object o)
    {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection coll)
    {
        return c.containsAll(coll);
    }

    public boolean addAll(Collection coll)
    {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection coll)
    {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection coll)
    {
        throw new UnsupportedOperationException();
    }

    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object o)
    {
        return c.equals(o);
    }

    public int hashCode()
    {
        return c.hashCode();
    }

    // additional methods.

    /**
     * This method is only used by the ControllerFactoryImpl
     * to unwrap the unmodifiable Set and allow to
     * modify the set via controllers
     * 
     * @return the modifiable set
     */
    public Set getModifiableSet()
    {
        return c;
    }
}
