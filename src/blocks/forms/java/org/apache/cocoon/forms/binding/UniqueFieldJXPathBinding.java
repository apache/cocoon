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

import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.commons.jxpath.JXPathContext;

import java.util.Locale;

/**
 * UniqueFieldJXPathBinding provides an implementation of a {@link Binding}
 * that that allows the specification of a uniquefields defined inside a repeater.
 * <p>
 * NOTES: <ol>
 * <li>This Binding uses the provided widget-id of a defined field in the repeater.</li>
 * </ol>
 * 
 * @version CVS $Id: UniqueFieldJXPathBinding.java,v 1.2 2004/03/11 02:56:32 joerg Exp $
 */
public class UniqueFieldJXPathBinding extends JXPathBindingBase {

    /**
     * The xpath expression to the objectModel property
     */
    private final String xpath;

    /**
     * The id of the CocoonForm widget
     */
    private final String fieldId;

    /**
     * Optional convertor to convert values to and from strings when setting or reading
     * the from the model. Especially used in combination with XML models where everything
     * are strings.
     */
    private final Convertor convertor;

    /**
     * The locale to pass to the {@link #convertor}.
     */
    private final Locale convertorLocale;

    /**
     * Constructs UniqueFieldJXPathBinding.
     *
     * @param convertor may be null
     */
    public UniqueFieldJXPathBinding(JXPathBindingBuilderBase.CommonAttributes commonAtts, String widgetId, String xpath,
                              Convertor convertor, Locale convertorLocale) {
        super(commonAtts);
        this.fieldId = widgetId;
        this.xpath = xpath;
        this.convertor = convertor;
        this.convertorLocale = convertorLocale;
    }

    /**
     * Actively performs the binding from the ObjectModel wrapped in a jxpath
     * context to the form widget specified in this object.
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc) throws BindingException {
        Widget widget = frmModel.getWidget(this.fieldId);
        if (widget == null) {
            throw new BindingException("The widget with the ID [" + this.fieldId
                    + "] referenced in the binding does not exist in the form definition.");
        }

        Object value = jxpc.getValue(this.xpath);
        if (value != null && convertor != null) {
            if (value instanceof String) {
                value = convertor.convertFromString((String)value, convertorLocale, null);
            } else {
                getLogger().warn("Convertor ignored on backend-value which isn't of type String.");
            }
        }

        widget.setValue(value);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Done loading " + toString() + " -- value= " + value);
        }
    }

    /**
     * Actively performs the binding from the form to the ObjectModel
     * wrapped in a jxpath context
     */
    public void doSave(Widget frmModel, JXPathContext jxpc) throws BindingException {
        // Do nothing
    }

    public String toString() {
        return "UniqueFieldJXPathBinding [widget=" + this.fieldId + ", xpath=" + this.xpath + "]";
    }

    /*public void enableLogging(Logger logger) {
        super.enableLogging(logger);
    }*/
    /**
     * @return Returns the convertor.
     */
    public Convertor getConvertor() {
        return convertor;
    }
    /**
     * @return Returns the convertorLocale.
     */
    public Locale getConvertorLocale() {
        return convertorLocale;
    }
    /**
     * @return Returns the fieldId.
     */
    public String getFieldId() {
        return fieldId;
    }
    /**
     * @return Returns the xpath.
     */
    public String getXpath() {
        return xpath;
    }
}
