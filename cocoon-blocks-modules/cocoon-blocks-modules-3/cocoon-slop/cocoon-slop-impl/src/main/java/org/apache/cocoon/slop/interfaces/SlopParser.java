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

package org.apache.cocoon.slop.interfaces;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.apache.cocoon.ProcessingException;

/** Interface to SLOP parsers
 *
 * @version $Id$
 */
public interface SlopParser {

   /** must be called before any call to processLine() */
    void startDocument(ContentHandler destination)
        throws SAXException, ProcessingException;

    /** must be called once all calls to processLine() are done */
    void endDocument()
        throws SAXException, ProcessingException;

    /** call this to process input lines, does the actual parsing */
    void processLine(String line)
        throws SAXException, ProcessingException;
}
