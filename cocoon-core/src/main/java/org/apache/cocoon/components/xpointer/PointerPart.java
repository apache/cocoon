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
import org.apache.cocoon.ResourceNotFoundException;

/**
 * Interface to be implemented by pointer parts (xpointer schemes).
 */
public interface PointerPart {
    /**
     * If this pointer part successfully identifies any subresources, it should
     * stream them to the XMLConsumer available from the XPointerContext and return true.
     * Otherwise this method should return false.
     */
    public boolean process(XPointerContext xpointerContext) throws SAXException, ResourceNotFoundException;
}
