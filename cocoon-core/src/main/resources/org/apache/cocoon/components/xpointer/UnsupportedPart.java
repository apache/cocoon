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
package org.apache.cocoon.components.xpointer;

import org.xml.sax.SAXException;

public class UnsupportedPart implements PointerPart {
    private String schemeName;

    public UnsupportedPart(String schemeName) {
        this.schemeName = schemeName;
    }

    public boolean process(XPointerContext xpointerContext) throws SAXException {
        throw new SAXException("Scheme " + schemeName + " not supported by this XPointer implementation, as used in the fragment identifier " + xpointerContext.getXPointer());
    }
}
