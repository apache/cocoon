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

import org.apache.cocoon.forms.binding.library.Library;
import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.forms.formmodel.Widget;

import org.apache.commons.jxpath.JXPathContext;

/**
 * AbstractCustomBinding
 * @version $Id$
 */
public abstract class AbstractCustomBinding implements Binding {

    //TODO: following stuff should be removed after we cleaned out the Binding interface
    private Binding parent;
    private String id;
    private String xpath;

    public void setXpath(String path) {
        this.xpath = path;
    }

    public String getXpath() {
        return xpath;
    }

    /**
     * Sets parent binding.
     */
    public void setParent(Binding binding) {
        this.parent = binding;
    }

    /**
     * Returns binding definition id.
     */
    public String getId() {
        return this.id;
    }

    public Binding getClass(String id) {
        return this.parent.getClass(id);
    }
    //TODO: end of stuff to clean out over time
    //below is the real useful stuff...

    public boolean isValid() {
    	return false; // pessimistic
    }

    // needed for the Binding interface, should never need to be used in a subclass
    public Library getEnclosingLibrary() {
    	return null;
    }

    public void setEnclosingLibrary(Library lib) {
    }

    /**
     * Binding service method called upon loading.
     * This will delegate to the overloaded version specific for this base-class.
     * {@link #doLoad(Widget, JXPathContext)}
     *
     * @param frmModel
     * @param objModel
     * @throws BindingException
     */
    public final void loadFormFromModel(Widget frmModel, Object objModel) throws BindingException {
        if (frmModel instanceof Form) {
            ((Form) frmModel).informStartLoadingModel();
        }
        try {
            doLoad(frmModel, (JXPathContext) objModel);
        } catch (Exception e) {
            throw new BindingException("Error executing custom binding", e);
        }
        if (frmModel instanceof Form) {
            ((Form) frmModel).informEndLoadingModel();
        }
    }

    /**
     * Binding service method called upon saving.
     * This will delegate to the overloaded version specific for this base-class.
     * {@link #doSave(Widget, JXPathContext)}
     *
     * @param frmModel
     * @param objModel
     * @throws BindingException
     */
    public final void saveFormToModel(Widget frmModel, Object objModel) throws BindingException {
        if (frmModel instanceof Form) {
            ((Form) frmModel).informStartSavingModel();
        }
        try {
            doSave(frmModel, (JXPathContext) objModel);
        } catch (Exception e) {
            throw new BindingException("Error executing custom binding", e);
        }
        if (frmModel instanceof Form) {
            ((Form) frmModel).informEndSavingModel();
        }
    }

    /**
     * @param frmModel
     * @param context
     */
    protected abstract void doLoad(Widget frmModel, JXPathContext context) throws Exception;
    protected abstract void doSave(Widget frmModel, JXPathContext context) throws Exception;
}
