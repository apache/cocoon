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

import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.commons.jxpath.JXPathContext;

/**
 * AbstractCustomBinding
 */
public abstract class AbstractCustomBinding implements Binding {

    //TODO: following stuff should be removed after we cleaned out the Binding interface
    private Binding parent;
    private String id;

    public void setParent(Binding binding) {
        this.parent = binding;
    }
    public String getId() {
        return this.id;
    }
    public Binding getClass(String id) {
        return this.parent.getClass(id);        
    }
    //TODO: end of stuff to clean out over time
    //below is the real usefull stuff...
    
    
    /**
     * Binding service method called upon loading.
     * This will delegate to the overloaded version specific for this base-class.
     * {@link #doLoad(Widget, JXPathContext)
     * 
     * @param frmModel
     * @param objModel
     * @throws BindingException
     */
    public final void loadFormFromModel(Widget frmModel, Object objModel) throws BindingException {
        doLoad(frmModel, (JXPathContext)objModel);
    }

    /**
     * Binding service method called upon saving.
     * This will delegate to the overloaded version specific for this base-class.
     * {@link #doSave(Widget, JXPathContext)
     * 
     * @param frmModel
     * @param objModel
     * @throws BindingException
     */
    public final void saveFormToModel(Widget frmModel, Object objModel) throws BindingException {
        doSave(frmModel, (JXPathContext)objModel);
    }
    
    /**
     * 
     * @param frmModel
     * @param context
     */
    protected abstract void doLoad(Widget frmModel, JXPathContext context) throws BindingException;
    protected abstract void doSave(Widget frmModel, JXPathContext context) throws BindingException;
}
