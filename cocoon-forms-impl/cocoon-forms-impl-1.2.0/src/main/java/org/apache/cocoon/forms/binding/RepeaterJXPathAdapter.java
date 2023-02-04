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
package org.apache.cocoon.forms.binding;

import org.apache.cocoon.forms.formmodel.Repeater.RepeaterRow;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

import java.util.*;

/**
 * @version $Id$
 */
public class RepeaterJXPathAdapter implements RepeaterAdapter {

    private int progressive = 100000;

    private EnhancedRepeaterJXPathBinding binding;
    private RepeaterJXPathCollection jxCollection;
    private List sortedItems;


    public RepeaterFilter getFilter() {
        return new RepeaterJXPathFilter();
    }

    private String findPathFor(String field) {
        JXPathBindingBase[] childBindings = binding.getRowBinding().getChildBindings();
        String path = null;
        for (int i = 0; i < childBindings.length; i++) {
            if (childBindings[i] instanceof ValueJXPathBinding) {
                ValueJXPathBinding bnd = (ValueJXPathBinding) childBindings[i];
                if (bnd.getFieldId().equals(field)) {
                    path = bnd.getXPath();
                    break;
                }
            }
        }
        return path;
    }

    public RepeaterSorter sortBy(String field) {
        if (field == null) {
            sortedItems = null;
            return new NormalOrderJXPathSorter();
        }
        String path = findPathFor(field);
        if (path == null) throw new IllegalStateException("Cannot find a path for sorting on widget " + field);
        RepeaterSorter sort = new RepeaterJXPathSorter(path, field, false);
        if (sortedItems == null) {
            List tsortedItems = new ArrayList();
            int i = 0;
            RepeaterItem item = getItem(i);
            while (item != null) {
                tsortedItems.add(item);
                i++;
                item = getItem(i);
            }
            this.sortedItems = tsortedItems;
        }
        Collections.sort(sortedItems, sort);
        return sort;
    }

    public void setBinding(EnhancedRepeaterJXPathBinding binding) {
        this.binding = binding;
    }

    public void setCollection(Collection c) {
    }

    public void setJXCollection(RepeaterJXPathCollection collection) {
        this.jxCollection = collection;
    }

    public RepeaterItem getItem(int i) {
        if (i < 0) return null;
        if (i >= jxCollection.getOriginalCollectionSize()) return null;
        if (this.sortedItems == null) {
            JXPathContext storageContext = this.jxCollection.getStorageContext();
            Pointer pointer = storageContext.getPointer(binding.getRowPath() + "[" + (i + 1) + "]");
            JXPathContext rowContext = storageContext.getRelativeContext(pointer);
            RepeaterItem item = new RepeaterItem(new Integer(i + 1));
            item.setContext(rowContext);
            return item;
        } else {
            return (RepeaterItem) sortedItems.get(i);
        }
    }


    class RepeaterJXPathFilter implements RepeaterFilter {

        private Map fieldsPaths = new HashMap();
        private Map fieldsValues = new HashMap();

        public boolean shouldDisplay(RepeaterItem item) {
            for (Iterator iter = fieldsValues.keySet().iterator(); iter.hasNext();) {
                String field = (String) iter.next();
                Object value = fieldsValues.get(field);
                Object acvalue = null;
                if (item.getRow() == null) {
                    String path = (String) fieldsPaths.get(field);
                    acvalue = item.getContext().getValue(path);
                } else {
                    acvalue = item.getRow().getChild(field).getValue();
                }
                if (acvalue == null) return false;
                if (acvalue instanceof String && value instanceof String) {
                    return ((String) acvalue).startsWith((String) value);
                } else {
                    return acvalue.equals(value);
                }
            }
            return true;
        }

        public void setFilter(String field, Object value) {
            if (value == null || ((value instanceof String) && ((String) value).length() == 0)) {
                fieldsPaths.remove(field);
                fieldsValues.remove(field);
            } else {
                String path = findPathFor(field);
                if (path == null)
                    throw new IllegalStateException("Cannot find a path for filtering on widget " + field);
                fieldsPaths.put(field, path);
                fieldsValues.put(field, value);
            }
        }

    }


    /**
     * Sorter for JXPath based repeaters
     */
    static class RepeaterJXPathSorter implements RepeaterSorter {

        private String path;
        private String field;
        private boolean nullsAreHigher;

        /**
         * JXPath based sorter for repeater
         * @param path the path of the field to sort on
         * @param field the name of the field to sort on
         * @param nullsAreHigher indicates if null values are seen as higher then other values.
         * When this value is set to false, it  means that nulls are lower than anything else.
         */
        public RepeaterJXPathSorter(String path, String field, boolean nullsAreHigher) {
            this.path = path;
            this.field = field;
            this.nullsAreHigher = nullsAreHigher;
        }

        public void setCollection(Collection c) {
        }

        public int compare(Object o1, Object o2) {
            RepeaterItem i1 = (RepeaterItem) o1;
            RepeaterItem i2 = (RepeaterItem) o2;

            Object val1;
            if (i1.getRow() != null) {
                val1 = i1.getRow().getChild(field).getValue();
            } else {
                val1 = i1.getContext().getValue(path);
            }

            Object val2;
            if (i2.getRow() != null) {
                val2 = i2.getRow().getChild(field).getValue();
            } else {
                val2 = i2.getContext().getValue(path);
            }

            if(val1 == null) {
                return (this.nullsAreHigher ? 1 : -1);
            }
            if(val2 == null) {
                return (this.nullsAreHigher ? -1 : 1);
            }

            if (val1 instanceof Comparable) {
                return ((Comparable) val1).compareTo(val2);
            }
            return val1.toString().compareTo(val2.toString());
        }

    }

    static class NormalOrderJXPathSorter implements RepeaterSorter {

        public void setCollection(Collection c) {
        }

        public int compare(Object o1, Object o2) {
            RepeaterItem i1 = (RepeaterItem) o1;
            RepeaterItem i2 = (RepeaterItem) o2;
            return ((Integer) i1.getHandle()).compareTo((Integer) i2.getHandle());
        }
    }

    public RepeaterItem generateItem(RepeaterRow row) {
        RepeaterItem item = new RepeaterItem(new Integer(progressive++));
        item.setRow(row);
        return item;
    }

    public void populateRow(RepeaterItem item) throws BindingException {
        binding.getRowBinding().loadFormFromModel(item.getRow(), item.getContext());
    }

}
