/*
 * File CustomJXPathBinding.java 
 * created by mpo
 * on Apr 1, 2004 | 4:17:26 PM
 * 
 * (c) 2004 - Outerthought BVBA
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
    private final Binding wrappedBinding;
    
    /**
     * Constructs CustomJXPathBinding
     * @param commonAtts common configuration attributes {@link JXPathBindingBase.CommonAttributes}
     * @param widgetId id of the widget to bind to
     * @param xpath jxpath expression to narrow down the context to before calling the wrapped Binding
     * @param wrappedBinding the actual custom written Binding implementation of {@link Binding}
     */
    public CustomJXPathBinding(CommonAttributes commonAtts, String widgetId, String xpath, Binding wrappedBinding) {
        super(commonAtts);
        this.widgetId = widgetId;
        this.xpath = xpath;
        this.wrappedBinding = wrappedBinding;
    }
    
    /**
     * Delegates the actual loading operation to the provided Custom Binding Class
     * after narrowing down on the selected widget (@id) and context (@path)
     * @param frmModel
     * @param jxpc
     * @throws BindingException
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc) throws BindingException {
        Widget selectedWidget = selectWidget(frmModel);
        Object contextValue = jxpc.getValue(this.xpath);
        
        this.wrappedBinding.loadFormFromModel(selectedWidget, contextValue);
    }    

    /**
     * Delegates the actual saving operation to the provided Custom Binding Class
     * after narrowing down on the selected widget (@id) and context (@path)
     * @param frmModel
     * @param jxpc
     * @throws BindingException
     */
    public void doSave(Widget frmModel, JXPathContext jxpc) throws BindingException {
        Widget selectedWidget = selectWidget(frmModel);
        Object contextValue = jxpc.getValue(this.xpath);
        
        this.wrappedBinding.saveFormToModel(selectedWidget, contextValue);
    }
    
    
    /**
     * Helper method which selects down the identified widget from the formModel.
     * If no 'widgetId' is set the formModel will just be returned.
     *  
     * @param frmModel
     * @return
     * @throws BindingException
     */
    private Widget selectWidget(Widget frmModel) throws BindingException {
        if (this.widgetId == null) return frmModel;
        
        Widget selectedWidget = frmModel.getWidget(this.widgetId);            
        if (selectedWidget == null) {
            throw new BindingException("The widget with the ID [" + this.widgetId
                    + "] referenced in the binding does not exist in the form definition.");
        }
        return selectedWidget;
    }
}
