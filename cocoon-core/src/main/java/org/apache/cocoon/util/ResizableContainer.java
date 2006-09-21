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

package org.apache.cocoon.util;

/**
 * Add-only Container class.
 *
 * @version $Id$
 */
public class ResizableContainer {

    private int pointer = -1;
    private int size = 0;
    private Object[] container;

    public ResizableContainer(int initialCapacity){
        this.container = new Object[initialCapacity];
    }

    public void add(Object o) {
        set(++pointer,o);
    }
    
    public void set(int index, Object o) {
        adjustPointer(index);
        ensureCapacity(index+1);
        container[index] = o;
        size++;
    }
    
    public Object get(int index) {
        return (index < container.length) ? container[index] : null; 
    }    

    public int size() {
        return size;
    }

    private void adjustPointer(int newPointer) {
        this.pointer = Math.max(this.pointer, newPointer);
    }
    
    private void ensureCapacity(int minCapacity) {
        int oldCapacity = container.length;
        if (oldCapacity < minCapacity) {
            Object[] oldContainer = container;
            int newCapacity = (oldCapacity * 3)/2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            container = new Object[newCapacity];
            System.arraycopy(oldContainer, 0, container, 0, oldContainer.length);
        }
    }
}
