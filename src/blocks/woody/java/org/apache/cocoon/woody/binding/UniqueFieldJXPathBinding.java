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
package org.apache.cocoon.woody.binding;

import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.cocoon.woody.datatype.convertor.Convertor;
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
 * @version CVS $Id: UniqueFieldJXPathBinding.java,v 1.1 2004/03/02 18:48:18 antonio Exp $
 */
public class UniqueFieldJXPathBinding extends JXPathBindingBase {

    /**
     * The xpath expression to the objectModel property
     */
    private final String xpath;

    /**
     * The id of the Woody form-widget
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
     * context to the Woody-form-widget specified in this object.
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
     * Actively performs the binding from the Woody-form to the ObjectModel
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
