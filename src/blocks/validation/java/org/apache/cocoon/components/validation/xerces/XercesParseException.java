/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.components.validation.xerces;

import org.apache.xerces.xni.parser.XMLParseException;
import org.xml.sax.SAXParseException;

/**
 * <p>A simple wrapper around a {@link XMLParseException} exposing a
 * {@link SAXParseException}.</p>
 *
 * <p>Most of this code has been derived from the Xerces JAXP Validation interface
 * available in the <code>org.xml.xerces.jaxp.validation</code> package.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class XercesParseException extends SAXParseException {

    public XercesParseException(XMLParseException exception) {
        super(exception.getMessage(),
              exception.getPublicId(),
              exception.getLiteralSystemId(),
              exception.getLineNumber(),
              exception.getColumnNumber(),
              exception);
        super.initCause(exception); 
    }
}
