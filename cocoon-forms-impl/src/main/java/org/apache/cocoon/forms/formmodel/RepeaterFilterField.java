package org.apache.cocoon.forms.formmodel;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.forms.binding.BindingException;
import org.apache.cocoon.forms.event.ValueChangedEvent;
import org.apache.cocoon.forms.event.ValueChangedListener;

public class RepeaterFilterField extends Field {

	private EnhancedRepeater repeater;
	private String field;

	public RepeaterFilterField(RepeaterFilterFieldDefinition fieldDefinition) {
		super(fieldDefinition);
		this.field = fieldDefinition.getField();
	}

	public void initialize() {
		super.initialize();
		String name = ((RepeaterFilterFieldDefinition)getDefinition()).getRepeaterName();
		Widget w = getParent().lookupWidget(name);
		if (w == null) throw new IllegalArgumentException("Cannot find repeater named " + name);
		if (!(w instanceof EnhancedRepeater)) throw new IllegalArgumentException("The repeater named " + name + " is not an enhanced repeater");
		this.repeater = (EnhancedRepeater) w;
		
		this.addValueChangedListener(new ValueChangedListener() {
			public void valueChanged(ValueChangedEvent event) {
				if (repeater.validate()) {
					try {
						repeater.setFilter(field, event.getNewValue());
					} catch (BindingException e) {
						throw new CascadingRuntimeException("Error setting filter",e);
					}
				}
			}
		});
	}
	
	

}
