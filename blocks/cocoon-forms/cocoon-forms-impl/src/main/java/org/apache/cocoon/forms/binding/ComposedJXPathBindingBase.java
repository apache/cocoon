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
package org.apache.cocoon.forms.binding;

import java.util.Collections;
import java.util.HashMap;

import org.apache.cocoon.forms.formmodel.Widget;

import org.apache.commons.jxpath.JXPathContext;

/**
 * ComposedJXPathBindingBase provides a helper base class for subclassing
 * into specific {@link JXPathBindingBase} implementations that have nested
 * child-bindings.
 *
 * @version $Id$
 */
public class ComposedJXPathBindingBase extends JXPathBindingBase {

    private final JXPathBindingBase[] subBindings;


    /**
     * Constructs ComposedJXPathBindingBase
     *
     * @param childBindings sets the array of childBindings
     */
    public ComposedJXPathBindingBase(JXPathBindingBuilderBase.CommonAttributes commonAtts,
                                     JXPathBindingBase[] childBindings) {
        super(commonAtts);
        this.subBindings = childBindings;
        if (this.subBindings != null) {
            for (int i = 0; i < this.subBindings.length; i++) {
                this.subBindings[i].setParent(this);
            }
        }
    }

    public String getXPath() {
        return null;
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
                    if (binding instanceof ClassJXPathBinding) {
                        String bindingId = binding.getId();
                        if (bindingId != null)
                          classes.put(bindingId, binding);
                    }
                }

                // Fee memory used by an empty map
                if (classes.size() == 0) {
                    classes = Collections.EMPTY_MAP;
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
     * Actively performs the binding from the ObjectModel to the CForms-form
     * by passing the task onto it's children.
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc) throws BindingException {
        if (this.subBindings != null) {
            int size = this.subBindings.length;
            for (int i = 0; i < size; i++) {
                this.subBindings[i].loadFormFromModel(frmModel, jxpc);
            }
        }
    }

    /**
     * Actively performs the binding from the CForms-form to the ObjectModel
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
