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
package org.apache.cocoon.components.validation.impl;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>TODO: ...</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class XercesErrorWrapper implements XMLErrorHandler {
    
    private final ErrorHandler errorHandler;
    
    public XercesErrorWrapper(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    
    public void warning(String domain, String key, XMLParseException exception)
    throws XNIException {
        if (this.errorHandler != null) try {
            this.errorHandler.warning(this.makeException(exception));
        } catch (SAXException saxException) {
            throw new XNIException(saxException);
        }
    }

    public void error(String domain, String key, XMLParseException exception)
    throws XNIException {
        if (this.errorHandler != null) try {
            this.errorHandler.warning(this.makeException(exception));
        } catch (SAXException saxException) {
            throw new XNIException(saxException);
        }
    }

    public void fatalError(String domain, String key, XMLParseException exception)
    throws XNIException {
        if (this.errorHandler != null) try {
            this.errorHandler.warning(this.makeException(exception));
        } catch (SAXException saxException) {
            throw new XNIException(saxException);
        }
    }
    
    private SAXParseException makeException(XMLParseException exception) {
        final SAXParseException saxParseException;
        saxParseException = new SAXParseException(exception.getMessage(),
                                                  exception.getPublicId(),
                                                  exception.getLiteralSystemId(),
                                                  exception.getLineNumber(),
                                                  exception.getColumnNumber(),
                                                  exception);
        return (SAXParseException) saxParseException.initCause(exception);
    }
}
