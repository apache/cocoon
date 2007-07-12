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
