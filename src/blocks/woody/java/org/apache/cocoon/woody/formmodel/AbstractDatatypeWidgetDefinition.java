/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version $Id: AbstractDatatypeWidgetDefinition.java,v 1.7 2004/02/11 10:43:30 antonio Exp $
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
