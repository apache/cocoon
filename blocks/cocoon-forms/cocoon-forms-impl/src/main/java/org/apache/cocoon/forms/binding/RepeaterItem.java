package org.apache.cocoon.forms.binding;

import org.apache.cocoon.forms.formmodel.Repeater.RepeaterRow;
import org.apache.commons.jxpath.JXPathContext;

public class RepeaterItem {

	private Object handle;
	
	private JXPathContext context;
	private RepeaterRow row;
	
	public RepeaterItem(Object handle) {
		super();
		this.handle = handle;
	}
	public JXPathContext getContext() {
		return context;
	}
	public void setContext(JXPathContext context) {
		this.context = context;
	}
	public Object getHandle() {
		return handle;
	}
	public void setHandle(Object handle) {
		this.handle = handle;
	}
	public RepeaterRow getRow() {
		return row;
	}
	public void setRow(RepeaterRow attribute) {
		this.row = attribute;
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof RepeaterItem)) return false;
		return this.handle.equals(((RepeaterItem)other).handle);
	}
	
	public int hashCode() {
		return this.handle.hashCode();
	}
	
}
