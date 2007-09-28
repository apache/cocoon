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
package org.apache.cocoon.forms.datatype.convertor;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds {@link EnumConvertor}s.
 * 
 * @version $Id$
 */
public class PlainEnumConvertorBuilder implements EnumConvertorBuilder {

    /* (non-Javadoc)
     * @see org.apache.cocoon.form.datatype.convertor.ConvertorBuilder#build(org.w3c.dom.Element)
     */
    public Convertor build(Element configElement) throws Exception {
        if (configElement == null) {
            return null;
        }
        Element enumEl = DomHelper.getChildElement(configElement,
                FormsConstants.DEFINITION_NS, "enum", true);
        String clazz = enumEl.getFirstChild().getNodeValue();
        return new EnumConvertor(clazz);
    }

}
