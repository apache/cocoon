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
package org.apache.cocoon.forms.util;

import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A string in an XMLizable form.
 *
 * Will produce exactly one characters call, no start/endDocument calls.
 * 
 * @version $Id: StringMessage.java,v 1.1 2004/03/09 10:34:09 reinhard Exp $
 */
public class StringMessage implements XMLizable {
    private char[] ch;

    public StringMessage(String message) {
        this.ch = message.toCharArray();
    }

    public void toSAX(ContentHandler contentHandler) throws SAXException {
        contentHandler.characters(ch, 0, ch.length);
    }
}
