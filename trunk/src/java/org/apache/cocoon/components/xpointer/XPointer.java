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

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * Represents a fragment identifier conforming to the XML Pointer Language Framework.
 * See also the specification at <a href="http://www.w3.org/TR/2003/REC-xptr-framework-20030325">
 * http://www.w3.org/TR/2003/REC-xptr-framework-20030325</a>.
 *
 * <p>To create an instance of this class, call
 * {@link org.apache.cocoon.components.xpointer.parser.XPointerFrameworkParser#parse XPointerFrameworkParser.parse}.
 */
public class XPointer {
    private List pointerParts = new LinkedList();

    public void addPart(PointerPart part) {
        pointerParts.add(part);
    }

    public void process(XPointerContext context) throws SAXException {
        Iterator pointerPartsIt = pointerParts.iterator();
        while (pointerPartsIt.hasNext()) {
            PointerPart part = (PointerPart)pointerPartsIt.next();
            part.process(context);
        }
    }
}
