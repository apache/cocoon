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
package org.apache.cocoon.xml;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This class implements a ContentHandler for embedding a full SAX
 * event stream into an existing stream of SAX events. An instance of
 * this class will pass unmodified all the SAX events to the linked
 * ContentHandler, but will ignore the startDocument and endDocument
 * events.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @version CVS $Id: EmbeddedXMLPipe.java,v 1.2 2004/03/05 13:03:01 bdelacretaz Exp $
 */
public class EmbeddedXMLPipe extends AbstractXMLPipe
{
    /**
     * Creates an EmbeddedXMLPipe that writes into the given ContentHandler.
     */
    public EmbeddedXMLPipe(ContentHandler handler) {
        setContentHandler(handler);
    }

    /**
     * Ignore the <code>startDocument</code> event: this method does nothing.
     *
     * @exception SAXException if an error occurs
     */
    public void startDocument()
    throws SAXException
    {
    }

    /**
     * Ignore the <code>endDocument</code> event: this method does nothing.
     *
     * @exception SAXException if an error occurs
     */
    public void endDocument()
    throws SAXException
    {
    }
}
