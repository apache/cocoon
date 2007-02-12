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
package org.apache.cocoon.components.sax;

import org.apache.cocoon.xml.AbstractSAXFragment;
import org.apache.cocoon.xml.EmbeddedXMLPipe;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * An XMLByteStream wrapped by an XMLFragment implementation. This allows to
 * store SAX events and insert them in an XSP result using &lt;xsp:expr&gt;.
 *
 * @version $Id$
 */
public class XMLByteStreamFragment extends AbstractSAXFragment {

    /** The XML byte stream */
    private Object xmlBytes;

    /**
     * Creates a new <code>XMLByteStreamFragment</code> defined by the given
     * XML byte stream.
     *
     * @param bytes the XML byte stream representing the document fragment
     */
    public XMLByteStreamFragment(Object bytes) {
        this.xmlBytes = bytes;
    }

    /**
     * Output the fragment. If the fragment is a document, start/endDocument
     * events are discarded.
     */
    public void toSAX(ContentHandler ch) throws SAXException {
        // Stream bytes and discard start/endDocument
        XMLByteStreamInterpreter deserializer = new XMLByteStreamInterpreter();
        deserializer.setContentHandler(new EmbeddedXMLPipe(ch));
        deserializer.deserialize(this.xmlBytes);
    }
}
