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
package org.apache.cocoon.forms.binding;

import org.apache.cocoon.forms.binding.JXPathBindingBuilderBase.CommonAttributes;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.commons.jxpath.JXPathContext;

/**
 * CustomJXPathBinding
 */
public class CustomJXPathBinding extends JXPathBindingBase {
    
    /** 
     * The id of the cforms widget
     */
    private final String widgetId;
    
    /**
     * The path into the objectModel to select
     */
    private final String xpath;
    
    /**
     * The actual custom provided binding
     */
    private final AbstractCustomBinding wrappedBinding;
    
    /**
     * Constructs CustomJXPathBinding
     * 
     * @param commonAtts common configuration attributes {@link JXPathBindingBuilderBase.CommonAttributes}
     * @param widgetId id of the widget to bind to
     * @param xpath jxpath expression to narrow down the context to before calling the wrapped Binding
     * @param wrappedBinding the actual custom written Binding implementation of {@link Binding}
     */
    public CustomJXPathBinding(CommonAttributes commonAtts, String widgetId, 
                               String xpath, AbstractCustomBinding wrappedBinding) {
        super(commonAtts);
        this.widgetId = widgetId;
        this.xpath = xpath;
        this.wrappedBinding = wrappedBinding;
    }
    
    /**
     * Delegates the actual loading operation to the provided Custom Binding Class
     * after narrowing down on the selected widget (@id) and context (@path)
     * 
     * @param frmModel the narrowed widget-scope from the parent binding
     * @param jxpc the narrowed jxpath context from the parent binding 
     * @throws BindingException when the wrapped CustomBinding fails
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc) throws BindingException {
        Widget selectedWidget = selectWidget(frmModel, this.widgetId);
        JXPathContext context = jxpc.getRelativeContext(jxpc.getPointer(this.xpath));
        
        this.wrappedBinding.doLoad(selectedWidget, context);
    }    

    /**
     * Delegates the actual saving operation to the provided Custom Binding Class
     * after narrowing down on the selected widget (@id) and context (@path)
     * 
     * @param frmModel the narrowed widget-scope from the parent binding
     * @param jxpc the narrowed jxpath context from the parent binding 
     * @throws BindingException when the wrapped CustomBinding fails
     */
    public void doSave(Widget frmModel, JXPathContext jxpc) throws BindingException {
        Widget selectedWidget = selectWidget(frmModel, this.widgetId);
        JXPathContext context = jxpc.getRelativeContext(jxpc.getPointer(this.xpath));
        
        this.wrappedBinding.doSave(selectedWidget, context);
    }
    

}
