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
package org.apache.cocoon.forms.datatype.typeimpl;

import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.datatype.DatatypeManager;
import org.w3c.dom.Element;

/**
 * Builds {@link EnumType}s.
 * 
 * @version CVS $Id: EnumTypeBuilder.java,v 1.1 2004/03/09 10:33:57 reinhard Exp $
 */
public class EnumTypeBuilder extends AbstractDatatypeBuilder {

    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.datatype.DatatypeBuilder#build(org.w3c.dom.Element, boolean, org.apache.cocoon.woody.datatype.DatatypeManager)
     */
    public Datatype build(Element datatypeElement,
						  boolean arrayType,
						  DatatypeManager datatypeManager) throws Exception {
        EnumType type = new EnumType();
        type.setArrayType(arrayType);
        type.setBuilder(this);

        buildValidationRules(datatypeElement, type, datatypeManager);
        buildConvertor(datatypeElement, type);

        return type;
    }
}
