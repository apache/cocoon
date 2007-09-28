/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.forms.formmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.forms.binding.BindingException;
import org.apache.cocoon.forms.binding.RepeaterItem;
import org.apache.cocoon.forms.binding.RepeaterJXPathCollection;
import org.apache.cocoon.forms.datatype.StaticSelectionList;
import org.apache.cocoon.xml.AttributesImpl;

public class EnhancedRepeater extends Repeater {
    private RepeaterJXPathCollection collection;
    private String customPageFieldId;
    private Field customPageField;
	
    // pagination
    private int currentPage;
    private int pageSize;

	
    public EnhancedRepeater(RepeaterDefinition repeaterDefinition) {
		super(repeaterDefinition);
    	this.currentPage = this.definition.getInitialPage();
    	this.pageSize = this.definition.getPageSize();
        this.customPageFieldId = this.definition.getCustomPageId();
	}

	public void doPageLoad() throws BindingException {
        clearAllRows();
        collection.flushCachedItems();
        int start = getStartIndex();
        List items = collection.getItems(start, this.pageSize);
        for (Iterator iter = items.iterator(); iter.hasNext();) {
			RepeaterItem item = (RepeaterItem) iter.next();
	        if (item == null) break;
            if (item.getRow() != null) {
            	addRow(item.getRow());
            } else {
                RepeaterRow thisRow = addRow();
                item.setRow(thisRow);
                collection.getAdapter().populateRow(item);
            }
        }
        
        // set customPageField
        if (this.customPageField != null) {
            StaticSelectionList selectionList = new StaticSelectionList(this.customPageField.getDatatype());
            int j;
            for (j = 0; j <= this.getMaxPage();j++) {
                selectionList.addItem(new Integer(j),(j+1)+"");
            }
            this.customPageField.setSelectionList(selectionList);
            this.customPageField.setValue(new Integer(this.currentPage));
        }
    }

    /**
     * save current page to cache-collections (updatedRows, deleted rows, insertedRows)
     * @throws BindingException
     */
    public void doPageSave() throws BindingException {
        List tempUpdatedRows = new ArrayList();
        List tempInsertedRows = new ArrayList();
        
        List cache = collection.getCachedItems();
        // iterate rows in the form model...
        int formRowCount = getSize();
        for (int i = 0; i < formRowCount; i++) {
            Repeater.RepeaterRow thisRow = getRow(i);
            boolean found = false;
            for (int j = 0; j < cache.size(); j++) {
            	RepeaterItem item = (RepeaterItem) cache.get(j);
            	if (item == null) break;
            	if (item.getRow() == thisRow) {
                	// Found the matching row
                	// TODO we need a way to know if the row was really modified or not, maybe a FormHandler?
                	tempUpdatedRows.add(item);
                    found = true;
                    break;
                }
            }
            if (!found) {
            	tempInsertedRows.add(thisRow);
            }
        }
        
        List toDelete = new ArrayList();
        for (int j = 0; j < cache.size(); j++) {
        	RepeaterItem item = (RepeaterItem) cache.get(j);
        	if (item == null) break;
        	boolean found = false;
            for (int i = 0; i < formRowCount; i++) {
                Repeater.RepeaterRow thisRow = getRow(i);
                if (thisRow == item.getRow()) {
                	found = true;
                	break;
                }
            }
            if (!found) {
            	toDelete.add(item);
            }
        }
        for (Iterator iter = tempUpdatedRows.iterator(); iter.hasNext();) {
			RepeaterItem ele = (RepeaterItem) iter.next();
			collection.updateRow(ele);
		}
        for (Iterator iter = tempInsertedRows.iterator(); iter.hasNext();) {
        	RepeaterRow row = (RepeaterRow) iter.next();
			collection.addRow(row);
		}
        for (Iterator iter = toDelete.iterator(); iter.hasNext();) {
        	RepeaterItem ele = (RepeaterItem) iter.next();
			collection.deleteRow(ele);
		}
        collection.flushCachedItems();
    }
	
    private int getStartIndex() {
        return this.currentPage * this.pageSize;
    }
            
    public int getMaxPage() {
        return ((int)(Math.ceil((double)collection.getActualCollectionSize() / (double)pageSize))) - 1;
    }
    
    public int getCustomPageWidgetValue() {
        return ((Integer)this.customPageField.getValue()).intValue();
    }
    
    public int getCurrentPage() {
        return currentPage;
    }

    
    
    /*
     * convenience methods for presentation
     */
    
    public int getDisplayableCurrentPage() {
        return this.getCurrentPage() + 1;
    }
    
    public int getDisplayableLastPage() {
        // increment if we created a new page for insertion
        if (this.getCurrentPage() > this.getMaxPage()) {
            return this.getMaxPage() + 2;
        }
        return this.getMaxPage() + 1;
    }
    
    public boolean isFirstPage() {
        return this.getCurrentPage() == 0;
    }
    
    public boolean isLastPage() {
    	return this.getCurrentPage() >= this.getMaxPage();
    }

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public boolean isEnhanced() {
		return true;
	}

	public AttributesImpl getXMLElementAttributes() {
		AttributesImpl elementAttributes = super.getXMLElementAttributes();
	    if (this.pageSize < Integer.MAX_VALUE) {
	    	elementAttributes.addCDATAAttribute("page", String.valueOf(currentPage));
	    }
		return elementAttributes;
	}
	
    private void addRow(RepeaterRow row) {
    	rows.add(row);
        getForm().addWidgetUpdate(this);
    }
    
    private void clearAllRows() {
        rows.clear();    	
        getForm().addWidgetUpdate(this);
    }

	public void setCollection(RepeaterJXPathCollection collection) {
		this.collection = collection;
	}

	public void initialize() {
		super.initialize();
        Widget widget = getForm().lookupWidget(this.customPageFieldId);
        if (widget instanceof Field) {
            this.customPageField = (Field)widget;
        }
	}

	public RepeaterJXPathCollection getCollection() {
		return collection;
	}

	public void refreshPage() throws BindingException {
		doPageSave();
		doPageLoad();
	}

	public void goToPage(int page) throws BindingException {
		doPageSave();
		this.currentPage = page;
		doPageLoad();
	}

	public void sortBy(String field) throws BindingException {
		doPageSave();
		this.collection.sortBy(field);
		this.currentPage = 0;
		doPageLoad();
	}

	public void setFilter(String field, Object value) throws BindingException {
		doPageSave();
		this.collection.filter(field, value);
		this.currentPage = 0;
		doPageLoad();		
	}
    
}
