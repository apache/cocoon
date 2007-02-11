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
package org.apache.cocoon.woody.datatype.typeimpl;

import org.apache.cocoon.woody.datatype.Datatype;
import org.apache.cocoon.woody.datatype.DatatypeManager;
import org.apache.cocoon.woody.datatype.typeimpl.StringType;
import org.w3c.dom.Element;

/**
 * Builds {@link StringType}s.
 * @version $Id: StringTypeBuilder.java,v 1.7 2004/03/09 13:53:53 reinhard Exp $
 */
public class StringTypeBuilder extends AbstractDatatypeBuilder {

    public Datatype build(Element datatypeElement, boolean arrayType, DatatypeManager datatypeManager) throws Exception {
        StringType type = new StringType();
        type.setArrayType(arrayType);
        type.setBuilder(this);

        buildValidationRules(datatypeElement, type, datatypeManager);
        buildConvertor(datatypeElement, type);

        return type;
    }
}
