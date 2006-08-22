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
package org.apache.cocoon.xml;

import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * Default implementation of SAX's <code>LexicalHandler</code> interface. Empty implementation
 * of all methods so that you only have to redefine the ones of interest.
 *
 * @version $Id$
 */
public class DefaultLexicalHandler implements LexicalHandler {
    
    /**
     * Shared instance that can be used when lexical events should be ignored.
     */
    public static final LexicalHandler NULL_HANDLER = new DefaultLexicalHandler();

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        // nothing
    }

    public void endDTD() throws SAXException {
        // nothing
    }

    public void startEntity(String name) throws SAXException {
        // nothing
    }

    public void endEntity(String name) throws SAXException {
        // nothing
    }

    public void startCDATA() throws SAXException {
        // nothing
    }

    public void endCDATA() throws SAXException {
        // nothing
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        // nothing
    }
}