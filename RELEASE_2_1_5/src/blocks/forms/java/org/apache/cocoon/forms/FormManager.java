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
package org.apache.cocoon.forms;

import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.forms.formmodel.FormDefinition;
import org.apache.excalibur.source.Source;
import org.w3c.dom.Element;

/**
 * Work interface for the component that can create {@link Form}s.
 * 
 * @version $Id: FormManager.java,v 1.2 2004/03/18 13:56:09 bruno Exp $
 */
public interface FormManager {
    
    String ROLE = FormManager.class.getName();

    /**
     * Creates a form instance based on the XML form definition
     * that can be read from the specified source.
     *
     * <p>The form definition will be cached, so that future form instances
     * can be creted quickly.
     */
    Form createForm(Source source) throws Exception;

    /**
     * Creates a form instance based on the XML form definition that is
     * supplied as a DOM tree.
     *
     * <p>The specified element must be a fd:form element.
     *
     * <p>The Form Definition will not be cached.
     */
    Form createForm(Element formElement) throws Exception;

    /**
     * Creates a form definition based on the XML form definition that is
     * supplied as a DOM tree.
     *
     * <p>The specified element must be a fd:form element.

     * <p>The Form Definition will not be cached.
     */
    FormDefinition createFormDefinition(Element formElement) throws Exception;
}
