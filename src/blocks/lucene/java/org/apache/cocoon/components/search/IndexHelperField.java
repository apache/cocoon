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
package org.apache.cocoon.components.search;

import org.xml.sax.Attributes;

/**
 * A helper class for generating a lucene document in a SAX ContentHandler.
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id: IndexHelperField.java,v 1.3 2004/03/05 13:01:59 bdelacretaz Exp $
 */
class IndexHelperField
{
    String localFieldName;
    String qualifiedFieldName;
    StringBuffer text;
    Attributes attributes;


    /**
     *Constructor for the IndexHelperField object
     *
     * @param  lfn   Description of Parameter
     * @param  qfn   Description of Parameter
     * @param  atts  Description of Parameter
     * @since
     */
    IndexHelperField(String lfn, String qfn, Attributes atts) {
        this.localFieldName = lfn;
        this.qualifiedFieldName = qfn;
        this.attributes = atts;
        this.text = new StringBuffer();
    }


    /**
     *Gets the localFieldName attribute of the IndexHelperField object
     *
     * @return    The localFieldName value
     * @since
     */
    public String getLocalFieldName() {
        return localFieldName;
    }


    /**
     *Gets the qualifiedFieldName attribute of the IndexHelperField object
     *
     * @return    The qualifiedFieldName value
     * @since
     */
    public String getQualifiedFieldName() {
        return qualifiedFieldName;
    }


    /**
     *Gets the attributes attribute of the IndexHelperField object
     *
     * @return    The attributes value
     * @since
     */
    public Attributes getAttributes() {
        return attributes;
    }


    /**
     *Gets the text attribute of the IndexHelperField object
     *
     * @return    The text value
     * @since
     */
    public StringBuffer getText() {
        return text;
    }


    /**
     *Description of the Method
     *
     * @param  text  Description of Parameter
     * @since
     */
    public void appendText(String text) {
        this.text.append(text);
    }


    /**
     *Description of the Method
     *
     * @param  str     Description of Parameter
     * @param  offset  Description of Parameter
     * @param  length  Description of Parameter
     * @since
     */
    public void appendText(char[] str, int offset, int length) {
        this.text.append(str, offset, length);
    }
}

