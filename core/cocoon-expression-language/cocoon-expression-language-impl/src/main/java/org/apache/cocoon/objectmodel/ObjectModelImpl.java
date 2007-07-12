package org.apache.cocoon.objectmodel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.Factory;
import org.apache.commons.collections.iterators.ReverseListIterator;
import org.apache.commons.collections.map.MultiValueMap;

/**
 * Prototype implementation of {@link ObjectModel} interface. It <b>must</b> be initialized manually for now.
 *
 */
public class ObjectModelImpl extends MultiValueMap implements ObjectModel {

    public ObjectModelImpl() {
        super(new HashMap(), new Factory() {
        
            public Object create() {
                return new StackReversedIteration();
            }
        
        });
    }
    
    private static class StackReversedIteration extends ArrayStack {
        
        public Iterator iterator() {
            return new ReverseListIterator(this);
        }
        
        public ListIterator listIterator() {
            throw new UnsupportedOperationException();
        }
    }
}
