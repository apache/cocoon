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
package org.apache.cocoon.webapps.session;

import org.apache.cocoon.ProcessingException;
import org.w3c.dom.DocumentFragment;

/**
 * Form manager
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: FormManager.java,v 1.3 2004/03/05 13:02:22 bdelacretaz Exp $
*/
public interface FormManager {

    /** Avalon role */
    String ROLE = FormManager.class.getName();;

    /**
     * Register input field and return the current value of the field.
     */
    DocumentFragment registerInputField(String contextName,
                                        String path,
                                        String name,
                                        String formName)
    throws ProcessingException;

    /**
     * Process the request.
     * The incoming parameters are evaluated, if they contain information
     * for a previously registered input field.
     */
    void processInputFields();
}
