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
 * Interface to be implemented by components that can build a Convertor
 * based on a XML configuration (supplied as DOM Element).
 *
 * <p>The element will (should)
 * always have the local name "convertor" and the woody definition namespace,
 * but attributes and content of the element can vary depending on the
 * ConvertorBuilder implementation.
 *
 * @version CVS $Id: ConvertorBuilder.java,v 1.4 2004/03/05 13:02:28 bdelacretaz Exp $
 */
public interface ConvertorBuilder {
    /**
     * @param configElement is allowed to be null!
     */
    Convertor build(Element configElement) throws Exception;
}
