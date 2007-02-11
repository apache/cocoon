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
package org.apache.cocoon.forms.datatype;

import org.w3c.dom.Element;
import org.apache.cocoon.forms.datatype.convertor.Convertor;

/**
 * Work interface for the component that manages the datatypes.
 *
 * <p>See also {@link Datatype} and {@link DefaultDatatypeManager}.
 * 
 * @version $Id: DatatypeManager.java,v 1.1 2004/03/09 10:34:00 reinhard Exp $
 */
public interface DatatypeManager {
    
    String ROLE = DatatypeManager.class.getName();

    /**
     * Creates a datatype from an XML description.
     */
    Datatype createDatatype(Element datatypeElement, boolean arrayType) throws Exception;

    /**
     * Creates a validation rule from an XML description. This will usually be used by the
     * {@link DatatypeBuilder}s while building a {@link Datatype}.
     */
    ValidationRule createValidationRule(Element validationRuleElement) throws Exception;

    /**
     * Creates a convertor based on an XML description.
     */
    Convertor createConvertor(String dataTypeName, Element convertorElement) throws Exception;
}
