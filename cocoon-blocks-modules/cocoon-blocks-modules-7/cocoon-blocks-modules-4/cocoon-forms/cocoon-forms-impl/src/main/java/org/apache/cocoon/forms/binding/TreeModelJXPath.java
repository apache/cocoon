package org.apache.cocoon.forms.binding;

import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.formmodel.tree.Tree;
import org.apache.cocoon.forms.formmodel.tree.TreeModel;
import org.apache.commons.jxpath.JXPathContext;

public class TreeModelJXPath extends JXPathBindingBase {

    /**
     * The xpath expression to the objectModel property
     */
    private final String xpath;

    /**
     * The id of the CForms form-widget
     */
    private final String fieldId;
	
    /**
     * Constructs FieldJXPathBinding.
     */
    public TreeModelJXPath(JXPathBindingBuilderBase.CommonAttributes commonAtts,
                              String widgetId,
                              String xpath) {
        super(commonAtts);
        this.fieldId = widgetId;
        this.xpath = xpath;
    }

    public String getId() { return fieldId; }

    /**
     * Actively performs the binding from the ObjectModel wrapped in a jxpath
     * context to the CForms-form-widget specified in this object.
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc) throws BindingException {
        Widget widget = selectWidget(frmModel, this.fieldId);
        if (widget == null) {
            throw new BindingException("The widget with the ID [" + this.fieldId
                    + "] referenced in the binding does not exist in the form definition.");
        }
        if (!(widget instanceof Tree)) throw new BindingException("Widget " + this.fieldId + " is not a Tree!");

        Object value = jxpc.getValue(this.xpath);
        if (value != null) {
            if (!(value instanceof TreeModel)) throw new BindingException("Value found in " + this.xpath + " is not a TreeModel, instead it is a " + value.getClass().getName());
            ((Tree)widget).setModel((TreeModel)value);
        }
    }

	public void doSave(Widget frmModel, JXPathContext jxpc)
    throws BindingException {
		// Does nothing
	}

}
