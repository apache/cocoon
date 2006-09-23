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
package org.apache.cocoon.taglib.jxpath.core;

import org.apache.cocoon.taglib.VarXMLProducerTagSupport;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @version $Id$
 */
public class OutTag extends VarXMLProducerTagSupport {
    private String value;
    
    /*
     * @see Tag#doStartTag(String, String, String, Attributes)
     */
    public int doStartTag(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (value != null) {
            //JXPathContext context = JXPathCocoonContexts.getRequestContext(objectModel);
            Object retValue = getVariable(value);
            if (retValue != null) {
                if (var != null) {
                    setVariable(var, retValue);
                    //context.setValue(var, retValue);
                    //setAttribute(var, retValue);
                } else {
                    char[] charArray = retValue.toString().toCharArray();
                    this.xmlConsumer.characters(charArray, 0, charArray.length);
                }
            }
        }
        return EVAL_BODY;
    }
    
    public void setValue(String value) {
        this.value = value;
    }

    /*
     * @see Recyclable#recycle()
     */
    public void recycle() {
        this.value = null;
        super.recycle();
    }
}
