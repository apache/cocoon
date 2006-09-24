package org.apache.cocoon.forms.binding;

import java.util.Collection;
import java.util.Comparator;

public interface RepeaterSorter extends Comparator {

	public void setCollection(Collection c);
	
}
