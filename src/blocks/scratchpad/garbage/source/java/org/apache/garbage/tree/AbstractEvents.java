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
package org.apache.garbage.tree;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: AbstractEvents.java,v 1.2 2004/03/05 10:07:24 bdelacretaz Exp $
 */
public class AbstractEvents implements Events {

    /** Our array of events. */
    private Event events[] = new Event[1024];
    /** The length of our array. */
    private int length = 0;

    /**
     * Add a new event to this <code>AbstractEvents</code>.
     *
     * @param event The <code>Event</code> instance to add.
     */
    public synchronized void append(Event event) {
        if (this.length == this.events.length) {
            int newlen = this.events.length + (this.events.length >> 1);
            Event array[] = new Event[newlen];
            System.arraycopy(this.events, 0, array, 0, this.length);
            this.events = array;
        }

        /* If this is the first event, just add it */
        if (this.length == 0) {
            this.events[this.length ++] = event;
            return;
        }

        /* If this event was merged to its previous, do nothing */
        if (event.merge(this.events[this.length - 1])) {
            return;
        }

        /* In all other cases, add this event to the list */
        this.events[this.length ++] = event;
    }

    /**
     * Return an <code>Iterator</code> over the events contained in this
     * <code>AbstractEvents</code> instance.
     */
    public Iterator iterator() {
        return(new EventIterator(this));
    }

    /**
     * The private implementation of the <code>Iterator</code> interface.
     */
    private final static class EventIterator implements Iterator {

        /** The <code>AbstractEvents</code> associated with this instance. */
        private AbstractEvents events = null;

        /** The current position of this <code>Iterator</code>. */
        private int position = 0;

        /**
         * Create a new <code>EventIterator</code> instance associated
         * with a specified <code>AbstractEvents</code>.
         *
         * @param events The <code>AbstractEvents</code> instance.
         */
        private EventIterator(AbstractEvents events) {
            super();
            this.events = events;
        }

        /**
         * Returns true if the iteration has more elements.
         *
         * @return <b>true</b> if the iterator has more elements.
         */
        public boolean hasNext() {
            return(this.position < this.events.length);
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return The next element in the iteration.
         */
        public Object next() {
            if (this.position < this.events.length) {
                return(this.events.events[this.position ++]);
            }
            throw new NoSuchElementException();
        }

        /**
         * Operation not supported.
         *
         * @throws UnsupportedOperationException Always.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
