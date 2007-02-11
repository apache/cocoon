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
package org.apache.cocoon.woody.formmodel;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.woody.datatype.Datatype;
import org.apache.cocoon.woody.datatype.DynamicSelectionList;
import org.apache.cocoon.woody.datatype.FlowJXPathSelectionList;
import org.apache.cocoon.woody.datatype.SelectionList;
import org.apache.cocoon.woody.event.ValueChangedEvent;
import org.apache.cocoon.woody.event.ValueChangedListener;
import org.apache.cocoon.woody.event.WidgetEventMulticaster;

/**
 * Base class for WidgetDefinitions that use a Datatype and SelectionList.
 * 
 * @version $Id: AbstractDatatypeWidgetDefinition.java,v 1.8 2004/03/05 13:02:31 bdelacretaz Exp $
 */
public abstract class AbstractDatatypeWidgetDefinition extends AbstractWidgetDefinition implements Serviceable {
    private Datatype datatype;
    private SelectionList selectionList;
    private ValueChangedListener listener;
    private ServiceManager manager;

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    public Datatype getDatatype() {
        return datatype;
    }

    public void setDatatype(Datatype datatype) {
        this.datatype = datatype;
    }

    public void setSelectionList(SelectionList selectionList) {
        if (selectionList.getDatatype() != getDatatype())
            throw new RuntimeException("Tried to assign a SelectionList that is not associated with this widget's datatype.");
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
     * @see org.apache.cocoon.woody.formmodel.Field#setSelectionList(Object model, String valuePath, String labelPath)
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

}
