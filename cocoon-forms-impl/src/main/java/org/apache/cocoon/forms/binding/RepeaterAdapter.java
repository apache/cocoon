package org.apache.cocoon.forms.binding;

import java.util.Collection;

import org.apache.cocoon.forms.formmodel.Repeater.RepeaterRow;

public interface RepeaterAdapter {

	public void setBinding(EnhancedRepeaterJXPathBinding binding);
	public void setJXCollection(RepeaterJXPathCollection collection);
	
	public void setCollection(Collection c);
	
	// TODO expand with widget path
	public RepeaterSorter sortBy(String path);
	public RepeaterFilter getFilter();
	
	public RepeaterItem getItem(int i);
	public RepeaterItem generateItem(RepeaterRow row);
	public void populateRow(RepeaterItem item) throws BindingException;
	
}
