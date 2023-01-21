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
