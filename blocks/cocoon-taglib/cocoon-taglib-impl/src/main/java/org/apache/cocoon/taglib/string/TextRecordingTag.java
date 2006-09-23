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
package org.apache.cocoon.taglib.string;

import org.apache.cocoon.taglib.VarTransformerTagSupport;
import org.xml.sax.SAXException;

/**
 * @version $Id$
 */
public class TextRecordingTag extends VarTransformerTagSupport {
    StringBuffer bodyContent = new StringBuffer();
    
    /*
     * @see Tag#doEndTag(String, String, String)
     */
    public int doEndTag(String namespaceURI, String localName, String qName) throws SAXException {
        String text = getText();

        if (var != null) {
            setVariable(var, text);
        } else {
            char[] charArray = text.toCharArray();
            this.xmlConsumer.characters(charArray, 0, charArray.length);
        }

        return EVAL_PAGE;
    }

    /** 
     * @return String result of recording
     */
    protected final String getText() {
        return bodyContent.toString();
    }

    /*
     * @see ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        bodyContent.append(ch, start, length);
    }

    /*
     * @see Recyclable#recycle()
     */
    public void recycle() {
        bodyContent.setLength(0);
        super.recycle();
    }

}
