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
package org.apache.cocoon.forms.formmodel;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.datatype.DynamicSelectionList;
import org.apache.cocoon.forms.datatype.FlowJXPathSelectionList;
import org.apache.cocoon.forms.datatype.SelectionList;
import org.apache.cocoon.forms.event.ValueChangedEvent;
import org.apache.cocoon.forms.event.ValueChangedListener;
import org.apache.cocoon.forms.event.WidgetEventMulticaster;
import org.apache.cocoon.forms.FormsException;
import org.apache.cocoon.forms.FormsRuntimeException;

/**
 * Base class for WidgetDefinitions that use a Datatype and SelectionList.
 *
 * @version $Id$
 */
public abstract class AbstractDatatypeWidgetDefinition extends AbstractWidgetDefinition
                                                       implements Serviceable {

    private Datatype datatype;
    private Object initialValue;
    private SelectionList selectionList;
    private ValueChangedListener listener;
    private ServiceManager manager;


    public void service(ServiceManager manager) throws ServiceException {
        checkMutable();
        this.manager = manager;
    }

    /**
     * checks definition's completeness
     */
    public void checkCompleteness() throws IncompletenessException {
        super.checkCompleteness();

        if (this.datatype == null) {
            throw new IncompletenessException("Widget '" + getId() + "' must have a datatype element.", this);
        }
    }

    /**
     * initialize this definition with the other, sort of like a copy constructor
     */
    public void initializeFrom(WidgetDefinition definition) throws Exception {
        super.initializeFrom(definition);

        if (!(definition instanceof AbstractDatatypeWidgetDefinition)) {
            throw new FormsException("Ancestor definition " + definition.getClass().getName() + " is not an AbstractDatatypeWidgetDefinition.",
                                     getLocation());
        }

        AbstractDatatypeWidgetDefinition other = (AbstractDatatypeWidgetDefinition) definition;

        this.datatype = other.datatype;
        this.initialValue = other.initialValue;
        this.selectionList = other.selectionList;
        this.listener = other.listener;
    }

    public Datatype getDatatype() {
        return datatype;
    }

    public Object getInitialValue() {
        return this.initialValue;
    }

    public void setDatatype(Datatype datatype, Object initialValue) {
        checkMutable();
        this.datatype = datatype;
        this.initialValue = initialValue;
    }

    public void setSelectionList(SelectionList selectionList) {
        checkMutable();
        if (selectionList != null && selectionList.getDatatype() != getDatatype()) {
            throw new FormsRuntimeException("Tried to assign a selection list that is not associated with this widget's datatype.",
                                            getLocation());
        }
        this.selectionList = selectionList;
    }

    public SelectionList getSelectionList() {
        return selectionList;
    }

    /**
     * Builds a dynamic selection list from a source. This is a helper method for widget instances whose selection
     * list source has to be changed dynamically, and it does not modify this definition's selection list,
     * if any.
     * @param uri The URI of the source.
     */
    public SelectionList buildSelectionList(String uri) {
        return new DynamicSelectionList(datatype, uri, this.manager);
    }

    /**
     * Builds a dynamic selection list from an in-memory collection.
     * This is a helper method for widget instances whose selection
     * list has to be changed dynamically, and it does not modify this definition's selection list,
     * if any.
     * @see org.apache.cocoon.forms.formmodel.Field#setSelectionList(Object model, String valuePath, String labelPath)
     * @param model The collection used as a model for the selection list.
     * @param valuePath An XPath expression referring to the attribute used
     * to populate the values of the list's items.
     * @param labelPath An XPath expression referring to the attribute used
     * to populate the labels of the list's items.
     */
    public SelectionList buildSelectionListFromModel(Object model, String valuePath, String labelPath) {
        return new FlowJXPathSelectionList(model, valuePath, labelPath, datatype);
    }

    public void addValueChangedListener(ValueChangedListener listener) {
        checkMutable();
        this.listener = WidgetEventMulticaster.add(this.listener, listener);
    }

    public void fireValueChangedEvent(ValueChangedEvent event) {
        if (this.listener != null) {
            this.listener.valueChanged(event);
        }
    }

    public boolean hasValueChangedListeners() {
        return this.listener != null;
    }

    public ValueChangedListener getValueChangedListener() {
        return this.listener;
    }
}
