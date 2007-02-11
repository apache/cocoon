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

import org.apache.cocoon.woody.formmodel.Union;
import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.commons.jxpath.JXPathContext;

/**
 * CaseJXPathBinding provides an implementation of a {@link Binding}
 * that narrows the context towards provided childbindings.
 * <p>
 * NOTES: <ol>
 * <li>This Binding assumes that the provided widget-id points to a
 * case of a union.</li>
 * </ol>
 *
 * @author Timothy Larson
 * @version CVS $Id: CaseJXPathBinding.java,v 1.6 2004/02/03 12:26:21 joerg Exp $
 */
public class CaseJXPathBinding extends ComposedJXPathBindingBase {

    private final String xpath;

    private final String widgetId;

    /**
     * Constructs CaseJXPathBinding
     * @param commonAtts
     * @param widgetId
     * @param xpath
     * @param childBindings
     */
    public CaseJXPathBinding(JXPathBindingBuilderBase.CommonAttributes commonAtts, String widgetId, String xpath, JXPathBindingBase[] childBindings) {
        super(commonAtts, childBindings);
        this.widgetId = widgetId;
        this.xpath = xpath;
    }

    /**
     * Narrows the scope on the form-model to the member widget-field, and
     * narrows the scope on the object-model to the member xpath-context
     * before continuing the binding over the child-bindings.
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc) throws BindingException {
        Union unionWidget = (Union)frmModel;
        if (widgetId.equals(unionWidget.getValue())) {
            // JXPathContext subContext = jxpc.getRelativeContext(jxpc.getPointer(this.xpath));
            Binding[] subBindings = getChildBindings();
            if (subBindings != null) {
                int size = subBindings.length;
                for (int i = 0; i < size; i++) {
                    subBindings[i].loadFormFromModel(unionWidget, jxpc);
                }
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("done loading " + toString());
            }
        }
    }

    /**
     * Narrows the scope on the form-model to the member widget-field, and
     * narrows the scope on the object-model to the member xpath-context
     * before continuing the binding over the child-bindings.
     */
    public void doSave(Widget frmModel, JXPathContext jxpc) throws BindingException {
        Union unionWidget = (Union)frmModel;
        if (widgetId.equals(unionWidget.getValue())) {
            // JXPathContext subContext = jxpc.getRelativeContext(jxpc.getPointer(this.xpath));
            Binding[] subBindings = getChildBindings();
            if (subBindings != null) {
                int size = subBindings.length;
                for (int i = 0; i < size; i++) {
                    subBindings[i].saveFormToModel(unionWidget, jxpc);
                }
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("done saving " + toString());
            }
        }
    }

    public String toString() {
        return "CaseJXPathBinding [widget=" + this.widgetId + ", xpath=" + this.xpath + "]";
    }
}
