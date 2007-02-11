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
package org.apache.cocoon.forms.datatype.convertor;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds {@link EnumConvertor}s.
 * 
 * @version CVS $Id: EnumConvertorBuilder.java,v 1.3 2004/03/11 02:56:32 joerg Exp $
 */
public class EnumConvertorBuilder implements ConvertorBuilder {

    /* (non-Javadoc)
     * @see org.apache.cocoon.form.datatype.convertor.ConvertorBuilder#build(org.w3c.dom.Element)
     */
    public Convertor build(Element configElement) throws Exception {
        if (configElement == null) {
            return null;
        }
        Element enumEl = DomHelper.getChildElement(configElement,
                Constants.DEFINITION_NS, "enum", true);
        String clazz = enumEl.getFirstChild().getNodeValue();
        return new EnumConvertor(clazz);
    }

}
