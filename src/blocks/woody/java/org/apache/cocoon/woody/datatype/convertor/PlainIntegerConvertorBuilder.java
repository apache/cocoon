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
package org.apache.cocoon.woody.datatype.convertor;

import org.w3c.dom.Element;

/**
 * Builds {@link PlainIntegerConvertor}s.
 * 
 * @author <a href="mailto:antonio@apache.org">Antonio Gallardo</a>
 * @version CVS $Id: PlainIntegerConvertorBuilder.java,v 1.6 2004/03/09 13:54:15 reinhard Exp $
 */
public class PlainIntegerConvertorBuilder implements ConvertorBuilder {
    public Convertor build(Element configElement) throws Exception {
        return new PlainIntegerConvertor();
    }
}
