package org.apache.cocoon.forms.binding;

public interface RepeaterFilter {

	public void setFilter(String field, Object value);
	
	public boolean shouldDisplay(RepeaterItem item);
	
}
