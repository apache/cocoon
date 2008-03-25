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
package org.apache.cocoon.core.xml.avalon;

import java.io.IOException;

import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;


/**
 * A wrapper implementation to support the Excalibur XML interfaces.
 * 
 * @version $Id$
 * @since 2.2
 */
public class DefaultSAXParser
implements SAXParser {

    protected org.apache.cocoon.core.xml.SAXParser parser;

    public void setParser(org.apache.cocoon.core.xml.SAXParser parser) {
        this.parser = parser;
    }

    /**
     * @see org.apache.excalibur.xml.sax.SAXParser#parse(org.xml.sax.InputSource, org.xml.sax.ContentHandler, org.xml.sax.ext.LexicalHandler)
     */
    public void parse(InputSource arg0, ContentHandler arg1, LexicalHandler arg2) throws SAXException, IOException {
        this.parser.parse(arg0, arg1, arg2);
    }

    /**
     * @see org.apache.excalibur.xml.sax.SAXParser#parse(org.xml.sax.InputSource, org.xml.sax.ContentHandler)
     */
    public void parse(InputSource arg0, ContentHandler arg1) throws SAXException, IOException {
        this.parser.parse(arg0, arg1);
    }        
}
