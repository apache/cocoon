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
package org.apache.cocoon.transformation.helpers;

import org.xml.sax.SAXException;


/**
 * This class records SAX Events and generates a String from all
 * characters events
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: TextRecorder.java,v 1.3 2004/03/08 14:03:31 cziegeler Exp $
*/
public final class TextRecorder
extends NOPRecorder {

    public TextRecorder() {
        super();
    }

    private StringBuffer buffer = new StringBuffer();

    public void characters(char ary[], int start, int length)
    throws SAXException {
        buffer.append(new String(ary, start, length));
    }

    public String getText() {
        return buffer.toString().trim();
    }
}
