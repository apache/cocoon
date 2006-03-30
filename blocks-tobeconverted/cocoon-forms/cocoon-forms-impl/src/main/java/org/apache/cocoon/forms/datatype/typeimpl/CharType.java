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

import org.apache.cocoon.forms.datatype.DatatypeBuilder;
import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.datatype.typeimpl.AbstractDatatype;

/**
 * The char datatype.
 * 
 * <p>
 * This datatype is useful when you are binding to a bean which have a char property. In that case you cannot use the
 * string datatype, because JXPath will raise an error not being able to convert it.
 * </p>
 * 
 */
public class CharType extends AbstractDatatype {

    public Class getTypeClass() {
        return java.lang.Character.class;
    }
    public String getDescriptiveName() {
        return "char";
    }
    protected void setArrayType(boolean arrayType) {
        super.setArrayType(arrayType);
    }
    public void setConvertor(Convertor convertor)  {
        super.setConvertor(convertor);
    }
    protected void setBuilder(DatatypeBuilder builder)  {
        super.setBuilder(builder);
    }
}
