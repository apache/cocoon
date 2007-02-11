/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @author  <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: MRUBucketMap.java,v 1.3 2003/07/12 00:26:17 vgritsenko Exp $
 */
public final class MRUBucketMap implements Map
{
    private static final int DEFAULT_BUCKETS = 255;
    private final Node[] m_buckets;
    private final Object[] m_locks;
    private final Node m_header = new Node();
    private int m_size = 0;

    /**
     * Creates map with default number of buckets.
     */
    public MRUBucketMap()
    {
        this( DEFAULT_BUCKETS );
    }

    /**
     * Creates map with specified number of buckets.
     */
    public MRUBucketMap( int numBuckets )
    {
        int size = Math.max( 17, numBuckets );

        // Ensure that bucketSize is never a power of 2 (to ensure maximal distribution)
        if( size % 2 == 0 )
        {
            size--;
        }

        m_buckets = new Node[ size ];
        m_locks = new Object[ size ];

        for( int i = 0; i < size; i++ )
        {
            m_locks[ i ] = new Object();
        }

        m_header.mru_next = m_header.mru_previous = m_header;
    }

    private final int getHash( Object key )
    {
        final int hash = key.hashCode() % m_buckets.length;
        return ( hash < 0 ) ? hash * -1 : hash;
    }

    public Set keySet()
    {
        Set keySet = new HashSet();

        for( int i = 0; i < m_buckets.length; i++ )
        {
            synchronized( m_locks[ i ] )
            {
                Node n = m_buckets[ i ];

                while( n != null )
                {
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
    public int size()
    {
        return m_size;
    }

    /**
     * Put a reference in the Map.
     */
    public Object put( final Object key, final Object value )
    {
        if( null == key || null == value )
        {
            return null;
        }

        int isNew = 0;
        Node node;
        Object oldValue = null;

        int hash = getHash( key );

        synchronized( m_locks[ hash ] )
        {
            Node n = m_buckets[ hash ];
            if( n == null )
            {
                node = new Node();
                node.key = key;
                node.value = value;
                m_buckets[ hash ] = node;
                isNew = 1;
            }
            else
            {
                // Set n to the last node in the linked list.  Check each key along the way
                //  If the key is found, then change the value of that node and return
                //  the old value.
                for( Node next = n; next != null; next = next.next )
                {
                    n = next;

                    if( n.key.equals( key ) )
                    {
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

        synchronized ( m_header )
        {
            if (isNew == 0) {
                // Remove
                node.mru_previous.mru_next = node.mru_next;
                node.mru_next.mru_previous = node.mru_previous;
            }
            // Move node to the head.
            node.mru_previous = m_header;
            node.mru_next = m_header.mru_next;
            node.mru_previous.mru_next = node;
            node.mru_next.mru_previous = node;
            m_size += isNew;
        }

        return oldValue;
    }

    public Object get( final Object key )
    {
        if( null == key )
        {
            return null;
        }

        Node n;

        int hash = getHash( key );

        synchronized( m_locks[ hash ] )
        {
            n = m_buckets[ hash ];

            while( n != null )
            {
                if( n.key.equals( key ) )
                {
                    break;
                }

                n = n.next;
            }
        }

        if( n != null ) {
            synchronized( m_header ) {
                // Remove
                n.mru_previous.mru_next = n.mru_next;
                n.mru_next.mru_previous = n.mru_previous;
                // Add first
                n.mru_previous = m_header;
                n.mru_next = m_header.mru_next;
                n.mru_previous.mru_next = n;
                n.mru_next.mru_previous = n;
            }
            return n.value;
        }

        return null;
    }

    public boolean containsKey( final Object key )
    {
        if( null == key )
        {
            return false;
        }

        int hash = getHash( key );

        synchronized( m_locks[ hash ] )
        {
            Node n = m_buckets[ hash ];

            while( n != null )
            {
                if( n.key.equals( key ) )
                {
                    return true;
                }

                n = n.next;
            }
        }

        return false;
    }

    public boolean containsValue( final Object value )
    {
        if( null == value )
        {
            return false;
        }

        synchronized( m_header )
        {
            for( Node n = m_header.mru_next; n != m_header; n = n.mru_next )
            {
                if( n.value.equals( value ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    public Collection values()
    {
        Set valueSet = new HashSet();

        synchronized( m_header )
        {
            for( Node n = m_header.mru_next; n != m_header; n = n.mru_next )
            {
                valueSet.add( n.value );
            }
        }

        return valueSet;
    }

    public Set entrySet()
    {
        Set entrySet = new HashSet();

        synchronized( m_header )
        {
            for( Node n = m_header.mru_next; n != m_header; n = n.mru_next )
            {
                entrySet.add( n );
            }
        }

        return entrySet;
    }

    /**
     * Add all the contents of one Map into this one.
     */
    public void putAll( Map other )
    {
        Iterator i = other.keySet().iterator();

        while( i.hasNext() )
        {
            Object key = i.next();
            put( key, other.get( key ) );
        }
    }

    public Object remove( Object key )
    {
        if( null == key )
        {
            return null;
        }

        Node n;

        int hash = getHash( key );

        synchronized( m_locks[ hash ] )
        {
            n = m_buckets[ hash ];
            Node prev = null;

            while( n != null )
            {
                if( n.key.equals( key ) )
                {
                    // Remove this node from the linked list of nodes.
                    if( null == prev )
                    {
                        // This node was the head, set the next node to be the new head.
                        m_buckets[ hash ] = n.next;
                    }
                    else
                    {
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
            synchronized(m_header) {
                // Remove
                n.mru_previous.mru_next = n.mru_next;
                n.mru_next.mru_previous = n.mru_previous;
                m_size--;
            }

            return n.value;
        }

        return null;
    }

    public final boolean isEmpty()
    {
        return m_size == 0;
    }

    public final void clear()
    {
        synchronized ( m_header ) {
            for( int i = 0; i < m_buckets.length; i++ )
            {
                m_buckets[ i ] = null;
            }

            m_header.mru_next = m_header.mru_previous = m_header;
            m_size = 0;
        }
    }

    public Map.Entry removeLast()
    {
        Node node = m_header.mru_previous;
        if (node == m_header) {
            throw new NoSuchElementException("MRUBucketMap is empty");
        }

        remove(node.key);
        node.mru_next = null;
        node.mru_previous = null;
        return node;
    }

    private static final class Node implements Map.Entry
    {
        protected Object key;
        protected Object value;
        protected Node next;

        protected Node mru_previous;
        protected Node mru_next;

        public Object getKey()
        {
            return key;
        }

        public Object getValue()
        {
            return value;
        }

        public Object setValue( Object val )
        {
            Object retVal = value;
            value = val;
            return retVal;
        }
    }


    static class X {
        String x;
        public X (String s) {
            x = s;
        }
        public int hashCode() {
            return 1;
        }

        public boolean equals(Object obj) {
            return this == obj;
        }

        public String toString() {
            return x;
        }
    }
}
