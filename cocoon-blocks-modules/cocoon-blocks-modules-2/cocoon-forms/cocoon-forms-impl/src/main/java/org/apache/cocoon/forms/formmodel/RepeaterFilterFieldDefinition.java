package org.apache.cocoon.forms.formmodel;

public class RepeaterFilterFieldDefinition extends FieldDefinition {
	
	private String repeaterName;
	private String field;

	public String getRepeaterName() {
		return repeaterName;
	}

	public void setRepeaterName(String repeaterName) {
		this.repeaterName = repeaterName;
	}

	public Widget createInstance() {
        RepeaterFilterField field = new RepeaterFilterField(this);
        return field;
	}

	public void initializeFrom(WidgetDefinition definition) throws Exception {
		if (!(definition instanceof RepeaterFilterFieldDefinition)) 
			throw new IllegalArgumentException("Wrong definition type to initialize from : " + definition.getClass().getName());
		super.initializeFrom(definition);
		this.repeaterName = ((RepeaterFilterFieldDefinition)definition).getRepeaterName();
		this.field = ((RepeaterFilterFieldDefinition)definition).getField();
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	
	
}
