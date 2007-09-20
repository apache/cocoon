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
package org.apache.cocoon.forms.util;

import java.util.AbstractList;
import java.util.Map;

import org.apache.cocoon.forms.formmodel.Repeater;
import org.apache.cocoon.forms.formmodel.Widget;

/**
 * A <code>List</code> view of a {@link Repeater}, each element of the list being a <code>Map</code>
 * wrapping a repeater row, as defined by {@link ContainerWidgetAsMap}.
 * <p>
 * This implementation of list supports all methods, with the following restrictions:
 * <ul>
 * <li>values stored in the list must be <code>Map</code>s, that will be used with {@link ContainerWidgetAsMap#putAll(Map)}
 *     on the <code>Map</code> representation of the repeater rows,</li>
 * <li>operations that involve testing equality with the list contents (e.g. <code>contains(Object)</code>) will
 *     not function properly, the <code>Map</code> wrapping the rows being created on demand.</li>
 * </ul>
 * 
 * @since 2.1.8
 * @version $Id$
 */
public class RepeaterAsList extends AbstractList {
    
    private Repeater repeater;
    private boolean lowerCase;

    /**
     * Create a <code>List<code> view around a repeater. The <code>keysToLowerCase</code> parameter
     * specifies if <code>Map</code>s wrapping rows should convert input keys to lower case, as
     * specified by {@link ContainerWidgetAsMap#ContainerWidgetAsMap(AbstractContainerWidget, boolean)}.
     * 
     * @param repeater the repeater to wrap
     * @param keysToLowerCase should we convert input keys to lower case?
     */
    public RepeaterAsList(Repeater repeater, boolean keysToLowerCase) {
        this.repeater = repeater;
        this.lowerCase = keysToLowerCase;
    }
    
    /**
     * Same as <code>RepeaterAsList(repeater, false)</code>.
     */
    public RepeaterAsList(Repeater repeater) {
        this(repeater, false);
    }

    /**
     * Get the repeater widget that is wrapped by this <code>List</code>.
     * 
     * @return the wrapped {@link Repeater}
     */
    public Repeater getWidget() {
        return this.repeater;
    }
    
    /**
     * Get a widget relative to the repeater wrapped by this <code>List</code>
     * 
     * @param path a widget lookup path
     * @return the widget pointed to by <code>path</code> or <code>null</code> if it doesn't exist.
     * @see Widget#lookupWidget(String)
     */
    public Widget getWidget(String path) {
        return this.repeater.lookupWidget(path);
    }

    public Object get(int index) {
        return new ContainerWidgetAsMap(repeater.getRow(index), lowerCase);
    }

    public int size() {
        return repeater.getSize();
    }
    
    public Object set(int index, Object o) {
        if (o == null) {
            throw new NullPointerException("Cannot set null to a repeater");
        }
        if (!(o instanceof Map)) {
            throw new IllegalArgumentException("Cannot set a '" + o.getClass().toString() + "' to a repeater");
        }
        Map result = new ContainerWidgetAsMap(repeater.getRow(index));
        result.putAll((Map)o);
        return result;
    }
    
    public void add(int index, Object o) {
        if (o == null) {
            throw new NullPointerException("Cannot add null to a repeater");
        }
        if (!(o instanceof Map)) {
            throw new IllegalArgumentException("Cannot add a '" + o.getClass().toString() + "' to a repeater");
        }
        Repeater.RepeaterRow row = repeater.addRow(index);
        new ContainerWidgetAsMap(row).putAll((Map)o);
    }
    
    public Object remove(int index) {
        Map result = new ContainerWidgetAsMap(repeater.getRow(index));
        repeater.removeRow(index);
        return result;
    }
    
    // Not mandated by the abstract class, but will speed up things
    public void clear() {
        repeater.clear();
    }
}