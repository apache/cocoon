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

import java.util.HashMap;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.commons.jxpath.JXPathContext;

/**
 * ComposedJXPathBindingBase provides a helper base class for subclassing
 * into specific {@link JXPathBindingBase} implementations that have nested
 * child-bindings.
 *
 * @version CVS $Id: ComposedJXPathBindingBase.java,v 1.6 2004/01/11 20:51:15 vgritsenko Exp $
 */
public class ComposedJXPathBindingBase extends JXPathBindingBase {
    private final JXPathBindingBase[] subBindings;

    /**
     * Constructs ComposedJXPathBindingBase
     *
     * @param childBindings sets the array of childBindings
     */
    protected ComposedJXPathBindingBase(JXpathBindingBuilderBase.CommonAttributes commonAtts, JXPathBindingBase[] childBindings) {
        super(commonAtts);
        this.subBindings = childBindings;
        if (this.subBindings != null) {
            for (int i = 0; i < this.subBindings.length; i++) {
                this.subBindings[i].setParent(this);
            }
        }
    }

    /**
     * Receives the logger to use for logging activity, and hands it over to
     * the nested children.
     */
    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        if (this.subBindings != null) {
            for (int i = 0; i < this.subBindings.length; i++) {
                this.subBindings[i].enableLogging(logger);
            }
        }
    }

    /**
     * Gets a binding class by id.
     * @param id Id of binding class to get.
     */
    public Binding getClass(String id) {
        if (classes == null) {
            classes = new HashMap();
            if (this.subBindings != null) {
                for (int i = 0; i < this.subBindings.length; i++) {
                    Binding binding = this.subBindings[i];
                    String bindingId = binding.getId();
                    if (bindingId != null)
                      classes.put(bindingId, binding);
                }
            }
        }
        return super.getClass(id);
    }

    /**
     * Returns child bindings.
     */
    public JXPathBindingBase[] getChildBindings() {
        return subBindings;
    }

    /**
     * Actively performs the binding from the ObjectModel to the Woody-form
     * by passing the task onto it's children.
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc) {
        if (this.subBindings != null) {
            int size = this.subBindings.length;
            for (int i = 0; i < size; i++) {
                this.subBindings[i].loadFormFromModel(frmModel, jxpc);
            }
        }
    }

    /**
     * Actively performs the binding from the Woody-form to the ObjectModel
     * by passing the task onto it's children.
     */
    public void doSave(Widget frmModel, JXPathContext jxpc) throws BindingException {
        if (this.subBindings != null) {
            int size = this.subBindings.length;
            for (int i = 0; i < size; i++) {
                this.subBindings[i].saveFormToModel(frmModel, jxpc);
            }
        }
    }
}
