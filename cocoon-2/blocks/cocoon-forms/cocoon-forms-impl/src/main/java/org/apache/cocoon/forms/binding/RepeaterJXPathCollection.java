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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.forms.formmodel.Repeater.RepeaterRow;

import org.apache.commons.jxpath.JXPathContext;

/**
 * Implements a collection that takes care about removed, updated and inserted
 * elements, obtaining from a {@link RepeaterAdapter} all the needed objects.
 *
 * @version $Id$
 */
public class RepeaterJXPathCollection {

    private JXPathContext storageContext;

    private Map updatedRows;
    private Set deletedRows;
    private List insertedRows;

	private int collectionSize;

	private RepeaterSorter sorter = null;
	private RepeaterFilter filter = null;
	private RepeaterAdapter adapter = null;

	private List itemsCache = new ArrayList();


    public void init(JXPathContext storageContext, String rowpath, RepeaterAdapter adapter) {
        this.storageContext = storageContext;
        collectionSize = 0;
        Object value = storageContext.getValue(rowpath);
        if (value != null) {
            if (value instanceof Collection) {
                collectionSize = ((Collection)value).size();
            } else {
                collectionSize = ((Double) storageContext.getValue("count("+rowpath+")")).intValue();
            }
        }

        this.updatedRows = new HashMap();
        this.deletedRows = new HashSet();
        this.insertedRows = new ArrayList();
        this.adapter = adapter;
        this.sorter = adapter.sortBy(null);
    }

    private int getStartIndex(int start) {
    	int i = start;
    	RepeaterItem item = adapter.getItem(i);
    	// In case start is after the end of the collection try to go back
    	// until a valid item is found
    	while (item == null && i > 0) {
    		i--;
    		item = adapter.getItem(i);
    	}
    	if (item == null) return 0;
    	// Now move the index ahead of one for each deleted item "before"
    	// the desired one
        for (Iterator iter = deletedRows.iterator(); iter.hasNext();) {
			RepeaterItem delitem = (RepeaterItem) iter.next();
			if (sorter.compare(delitem, item) < 0) {
				i++;
			}
		}
        // And move it backward for each inserted row before the actual index
        for (Iterator iter = insertedRows.iterator(); iter.hasNext();) {
			RepeaterItem insitem = (RepeaterItem) iter.next();
			if (sorter.compare(insitem, item) < 0) {
				i--;
			}
		}
        if (i < 0) return 0;
        // Now we should have the correct start
        return i;
    }

    public List getItems(int start, int length) {
    	List ret = new ArrayList();
    	int rlength = length;
    	int rstart = getStartIndex(start);
    	RepeaterItem startItem = null;
    	if (rstart > 0) {
    		// Try to fetch one element before, so that we can distinguish
    		// where we started after inferring added elements.
    		startItem = getItem(rstart - 1);
    	}
    	if (startItem != null) {
    		ret.add(startItem);
    	}
    	int i = rstart;
    	RepeaterItem item;
        while (length > 0) {
        	item = getItem(i);
        	if (item == null) break;
        	// skip deleted items
            while (isDeleted(item)) {
            	i++;
            	item = getItem(i);
                if (item == null) break;
            }
            if (filter != null) {
                while (!filter.shouldDisplay(item)) {
                	i++;
                	item = getItem(i);
                    if (item == null) break;
                }
            }
            if (item == null) break;
        	ret.add(item);
        	i++;
        	length--;
        }
        // Infer the inserted rows.
        if (this.insertedRows.size() > 0) {
            if (filter != null) {
            	for (Iterator iter = this.insertedRows.iterator(); iter.hasNext();) {
					RepeaterItem acitem = (RepeaterItem) iter.next();
					if (filter.shouldDisplay(acitem)) {
						ret.add(acitem);
					}
				}
            } else {
            	ret.addAll(this.insertedRows);
            }
	    	Collections.sort(ret, this.sorter);
        }
    	if (startItem != null) {
	    	// Now get from the element after our start element.
	    	int pos = ret.indexOf(startItem);
	    	for (int j = 0; j <= pos; j++) {
	    		ret.remove(0);
	    	}
    	}
    	while (ret.size() > rlength) ret.remove(ret.size() - 1);

        this.itemsCache.clear();
        this.itemsCache.addAll(ret);
        return ret;
    }

    public List getCachedItems() {
    	return this.itemsCache;
    }

    public void flushCachedItems() {
    	this.itemsCache.clear();
    }

    private RepeaterItem getItem(int i) {
        // Take the element from the original collection and check if it was updated
        RepeaterItem item = this.adapter.getItem(i);
        if (item == null) return null;
        if (isUpdated(item)) {
        	item = (RepeaterItem) this.updatedRows.get(item.getHandle());
        }
        return item;
    }

    public void updateRow(RepeaterItem item) {
    	if (!isInserted(item) && !isDeleted(item)) {
    		this.updatedRows.put(item.getHandle(), item);
    	}
    }

    public void deleteRow(RepeaterItem item) {
    	if (isInserted(item)) {
    		this.insertedRows.remove(item);
    		return;
    	} else if (isUpdated(item)) {
    		this.updatedRows.remove(item);
    	}
    	this.deletedRows.add(item);
    }

    public void addRow(RepeaterItem item) {
    	this.insertedRows.add(item);
    }

    public int getOriginalCollectionSize() {
        return collectionSize;
    }

    public int getActualCollectionSize() {
    	return getOriginalCollectionSize() - this.deletedRows.size() + this.insertedRows.size();
    }

    /*
     * convenience methods to search the cache
     */

    private boolean isUpdated(RepeaterItem item) {
        return this.updatedRows.containsKey(item.getHandle());
    }

    private boolean isDeleted(RepeaterItem item) {
        return this.deletedRows.contains(item);
    }

    private boolean isInserted(RepeaterItem item) {
        return this.insertedRows.contains(item);
    }

	public JXPathContext getStorageContext() {
		return storageContext;
	}

	public List getDeletedRows() {
		// FIXME we should sort by natural order
		List ret = new ArrayList(this.deletedRows);
    	Collections.sort(ret, this.sorter);
    	Collections.reverse(ret);
		return ret;
	}

	public List getInsertedRows() {
		return insertedRows;
	}

	public Collection getUpdatedRows() {
		return updatedRows.values();
	}

	public RepeaterAdapter getAdapter() {
		return this.adapter;
	}

	public void addRow(RepeaterRow row) {
		RepeaterItem item = this.adapter.generateItem(row);
		this.addRow(item);
	}

	public void sortBy(String field) {
		this.sorter = this.adapter.sortBy(field);
	}

	public void filter(String field, Object value) {
		if (filter == null) {
			filter = this.adapter.getFilter();
		}
		filter.setFilter(field, value);
	}
}
