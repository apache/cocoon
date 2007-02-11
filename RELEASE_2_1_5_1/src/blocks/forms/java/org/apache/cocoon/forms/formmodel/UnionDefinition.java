/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.forms.formmodel;


/**
 * The {@link WidgetDefinition} corresponding to a {@link Union} widget.
 *
 * @version $Id: UnionDefinition.java,v 1.2 2004/04/12 14:05:09 tim Exp $
 */
public class UnionDefinition extends AbstractContainerDefinition {
    private String caseWidgetId;

    /*
    public void setDatatype(Datatype datatype) {
        if (!String.class.isAssignableFrom(datatype.getTypeClass()))
            throw new RuntimeException("Only datatype string is allowed for this widget at " + getLocation() + ".");
        super.setDatatype(datatype);
    }

    public void setDefault(Object value) throws Exception {
        if (!(value == null || String.class.isAssignableFrom(value.getClass())))
            throw new Exception("UnionDefinition: Default case must be supplied as a string (" + getLocation() + ")");
        if (value == null || value.equals("")) {
            if (isRequired())
                throw new Exception("UnionWidget: Union is marked required, but no default case was supplied (" + getLocation() + ")");
            this.defaultValue = "";
        } else {
            if (!hasWidget((String)value))
                throw new Exception("UnionWidget: The default value \"" + value + "\" does not match a union case (" + getLocation() + ")");
            this.defaultValue = (String)value;
        }
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
    */

    public void setCaseWidgetId(String id) {
        caseWidgetId = id;
    }

    public String getCaseWidgetId() {
        return caseWidgetId;
    }

    public Widget createInstance() {
        Union unionWidget = new Union(this);
        return unionWidget;
    }
}
