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

/**
 * Binding declares the methods to 'bind' (i.e. 'load' and 'save')
 * information elements from some back-end model (2nd argument) to and from
 * a existing Woody Widget.
 *
 * @version CVS $Id: Binding.java,v 1.1 2004/03/09 10:33:55 reinhard Exp $
 */
public interface Binding {

    /**
     * Sets parent binding.
     * @param binding Parent of this binding.
     */
    void setParent(Binding binding);

    /**
     * Gets binding definition id.
     */
    String getId();

    /**
     * Gets a binding class.
     * @param id Id of binding class to get.
     */
    Binding getClass(String id);

    /**
     * Loads the information-elements from the objModel to the frmModel.
     *
     * @param frmModel
     * @param objModel
     */
    void loadFormFromModel(Widget frmModel, Object objModel)
            throws BindingException;

    /**
     * Saves the infortmation-elements to the objModel from the frmModel.
     * @param frmModel
     * @param objModel
     */
    void saveFormToModel(Widget frmModel, Object objModel)
            throws BindingException;
}
