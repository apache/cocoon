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
package org.apache.cocoon.components.xmlform;

/**
 * Defines events fired by a Form object.
 *
 * @author Ivelin Ivanov, ivelin@apache.org
 * @version CVS $Id: FormListener.java,v 1.4 2004/03/05 13:02:38 bdelacretaz Exp $
 */
public interface FormListener {

    /**
     * This method is called before
     * the form is populated with request parameters.
     *
     * Semantically similar to that of the
     * ActionForm.reset() in Struts
     *
     * Can be used for clearing checkbox fields,
     * because the browser will not send them when
     * not checked.
     *
     * This method does nothing by default
     * Subclasses should override it to implement custom logic
     *
     * @param form       
     */
    void reset(Form form);

    /**
     * Filters custom request parameter
     * not refering to the model.
     *
     * @param form       
     * @param parameterName
     */
    boolean filterRequestParameter(Form form, String parameterName);
}

