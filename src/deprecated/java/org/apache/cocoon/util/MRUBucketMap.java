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
package org.apache.cocoon.util;

import java.util.Set;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Map;
import java.util.Collection;
import java.util.Iterator;

/**
 * A MRUBucketMap is an efficient ThreadSafe implementation of a Map with
 * addition of the removeLast method implementing LRU removal policy.
 * <br />
 * MRUBucketMap is based on the Avalon's BucketMap.
 *
 * @deprecated Use a corresponding map from commons-collections
 * @author  <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: MRUBucketMap.java,v 1.1 2004/05/27 13:52:20 cziegeler Exp $
 */
public final class MRUBucketMap implements Map {
    
    private static final int DEFAULT_BUCKETS = 255;
    private final Node[] buckets;
    private final Object[] locks;
    private final Node header = new Node();
    private int size;

    /**
     * Creates map with default number of buckets.
     */
    public MRUBucketMap() {
        this( DEFAULT_BUCKETS );
    }

    /**
     * Creates map with specified number of buckets.
     */
    public MRUBucketMap( int numBuckets ) {
        int size = Math.max( 17, numBuckets );

        // Ensure that bucketSize is never a power of 2 (to ensure maximal distribution)
        if( size % 2 == 0 ) {
            size--;
        }

        this.buckets = new Node[ size ];
        this.locks = new Object[ size ];

        for( int i = 0; i < size; i++ ) {
            this.locks[ i ] = new Object();
        }

        this.header.mru_next = this.header.mru_previous = this.header;
    }

    private final int getHash( Object key ) {
        final int hash = key.hashCode() % this.buckets.length;
        return ( hash < 0 ) ? hash * -1 : hash;
    }

    public Set keySet() {
        Set keySet = new HashSet();

        for( int i = 0; i < this.buckets.length; i++ ) {
            synchronized( this.locks[ i ] ) {
                Node n = this.buckets[ i ];

                while( n != null ) {
                    keySet.add( n.key );
                    n = n.next;
                }
            }
        }

        return keySet;
    }

    /**
     * Returns the current number of key, value pairs.
     */
    public int size() {
        return this.size;
    }

    /**
     * Put a reference in the Map.
     */
    public Object put( final Object key, final Object value ) {
        if( null == key || null == value ) {
            return null;
        }

        int isNew = 0;
        Node node;
        Object oldValue = null;

        int hash = getHash( key );

        synchronized( this.locks[ hash ] ) {
            Node n = this.buckets[ hash ];
            if( n == null ) {
                node = new Node();
                node.key = key;
                node.value = value;
                this.buckets[ hash ] = node;
                isNew = 1;
            } else {
                // Set n to the last node in the linked list.  Check each key along the way
                //  If the key is found, then change the value of that node and return
                //  the old value.
                for( Node next = n; next != null; next = next.next ) {
                    n = next;

                    if( n.key.equals( key ) ) {
                        oldValue = n.value;
                        n.value = value;
                        break;
                    }
                }

                if (oldValue == null) {
                    // The key was not found in the current list of nodes,
                    // add it to the end in a new node.
                    node = new Node();
                    node.key = key;
                    node.value = value;
                    n.next = node;
                    isNew = 1;
                } else {
                    // The key was found in the list. Move it to the head.
                    node = n;
                }
            }
        }

        synchronized ( this.header ) {
            if (isNew == 0) {
                // Remove
                node.mru_previous.mru_next = node.mru_next;
                node.mru_next.mru_previous = node.mru_previous;
            }
            // Move node to the head.
            node.mru_previous = this.header;
            node.mru_next = this.header.mru_next;
            node.mru_previous.mru_next = node;
            node.mru_next.mru_previous = node;
            this.size += isNew;
        }

        return oldValue;
    }

    public Object get( final Object key ) {
        if( null == key ) {
            return null;
        }

        Node n;

        int hash = getHash( key );

        synchronized( this.locks[ hash ] ) {
            n = this.buckets[ hash ];

            while( n != null ) {
                if( n.key.equals( key ) ) {
                    break;
                }

                n = n.next;
            }
        }

        if( n != null ) {
            synchronized( this.header ) {
                // Remove
                n.mru_previous.mru_next = n.mru_next;
                n.mru_next.mru_previous = n.mru_previous;
                // Add first
                n.mru_previous = this.header;
                n.mru_next = this.header.mru_next;
                n.mru_previous.mru_next = n;
                n.mru_next.mru_previous = n;
            }
            return n.value;
        }

        return null;
    }

    public boolean containsKey( final Object key ) {
        if( null == key ) {
            return false;
        }

        int hash = getHash( key );

        synchronized( this.locks[ hash ] ) {
            Node n = this.buckets[ hash ];

            while( n != null ) {
                if( n.key.equals( key ) ) {
                    return true;
                }

                n = n.next;
            }
        }

        return false;
    }

    public boolean containsValue( final Object value ) {
        if( null == value ) {
            return false;
        }

        synchronized( this.header ) {
            for( Node n = this.header.mru_next; n != this.header; n = n.mru_next ) {
                if( n.value.equals( value ) ) {
                    return true;
                }
            }
        }

        return false;
    }

    public Collection values() {
        Set valueSet = new HashSet();

        synchronized( this.header ) {
            for( Node n = this.header.mru_next; n != this.header; n = n.mru_next ) {
                valueSet.add( n.value );
            }
        }

        return valueSet;
    }

    public Set entrySet() {
        Set entrySet = new HashSet();

        synchronized( this.header ) {
            for( Node n = this.header.mru_next; n != this.header; n = n.mru_next ) {
                entrySet.add( n );
            }
        }

        return entrySet;
    }

    /**
     * Add all the contents of one Map into this one.
     */
    public void putAll( Map other ) {
        Iterator i = other.keySet().iterator();

        while( i.hasNext() ) {
            Object key = i.next();
            put( key, other.get( key ) );
        }
    }

    public Object remove( Object key ) {
        if( null == key ) {
            return null;
        }

        Node n;

        int hash = getHash( key );

        synchronized( this.locks[ hash ] ) {
            n = this.buckets[ hash ];
            Node prev = null;

            while( n != null ) {
                if( n.key.equals( key ) ) {
                    // Remove this node from the linked list of nodes.
                    if( null == prev ) {
                        // This node was the head, set the next node to be the new head.
                        this.buckets[ hash ] = n.next;
                    } else {
                        // Set the next node of the previous node to be the node after this one.
                        prev.next = n.next;
                    }

                    break;
                }

                prev = n;
                n = n.next;
            }
        }

        if (n != null) {
            synchronized(this.header) {
                // Remove
                n.mru_previous.mru_next = n.mru_next;
                n.mru_next.mru_previous = n.mru_previous;
                this.size--;
            }

            return n.value;
        }

        return null;
    }

    public final boolean isEmpty() {
        return this.size == 0;
    }

    public final void clear() {
        synchronized ( this.header ) {
            for( int i = 0; i < this.buckets.length; i++ ) {
                this.buckets[ i ] = null;
            }

            this.header.mru_next = this.header.mru_previous = this.header;
            this.size = 0;
        }
    }

    public Map.Entry removeLast() {
        Node node = this.header.mru_previous;
        if (node == this.header) {
            throw new NoSuchElementException("MRUBucketMap is empty");
        }

        remove(node.key);
        node.mru_next = null;
        node.mru_previous = null;
        return node;
    }

    private static final class Node implements Map.Entry {
        
        protected Object key;
        protected Object value;
        protected Node next;

        protected Node mru_previous;
        protected Node mru_next;

        public Object getKey() {
            return this.key;
        }

        public Object getValue() {
            return this.value;
        }

        public Object setValue( Object val ) {
            Object retVal = this.value;
            this.value = val;
            return retVal;
        }
    }
}
