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
package org.apache.cocoon.components.language.markup.xsp;


import org.apache.cocoon.components.modules.input.InputModuleHelper;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.Iterator;
import java.util.Map;

/**
 * Helper class that caches references to InputModules for use in
 * XSPs. Works in conjunction with the input.xsl
 * logicsheet. References are obtained the first time a module is
 * accessed and kept until the page is completely displayed.
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: XSPModuleHelper.java,v 1.2 2004/04/27 22:25:29 haul Exp $
 */
public class XSPModuleHelper extends InputModuleHelper {

    private static final String PREFIX = "input";
    private static final String URI = "http://apache.org/cocoon/xsp/input/1.0";

    /**
     * Output the request attribute values for a given name to the
     * content handler.
     *
     * @param objectModel The Map objectModel
     * @param contentHandler The SAX content handler
     * @param module a <code>String</code> value holding the module name
     * @param name a <code>String</code> value holding the attribute name
     * @exception SAXException If a SAX error occurs
     * @exception RuntimeException if an error occurs
     */
    public void getAttributeValues(Map objectModel, ContentHandler contentHandler, String module, String name )
        throws SAXException, RuntimeException {

        AttributesImpl attr = new AttributesImpl();
        XSPObjectHelper.addAttribute(attr, "name", name);

        XSPObjectHelper.start(URI, PREFIX, contentHandler,
            "attribute-values", attr);

        Object[] values = this.getAttributeValues(objectModel, module, name, null);

        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                XSPObjectHelper.elementData(URI, PREFIX, contentHandler,
                    "value", String.valueOf(values[i]));
            }
        }

        XSPObjectHelper.end(URI, PREFIX, contentHandler, "attribute-values");
    }

    /**
     * Output attribute names for a given request
     *
     * @param objectModel The Map objectModel
     * @param contentHandler The SAX content handler
     * @param module the module's name
     * @exception SAXException If a SAX error occurs
     * @exception RuntimeException if an error occurs
     */
    public  void getAttributeNames(Map objectModel, ContentHandler contentHandler, String module)
        throws SAXException, RuntimeException {

        XSPObjectHelper.start(URI, PREFIX, contentHandler, "attribute-names");

        Iterator iter = this.getAttributeNames(objectModel, module);
        while (iter.hasNext()) {
            String name = (String) iter.next();
            XSPObjectHelper.elementData(URI, PREFIX, contentHandler, "name", name);
        }

        XSPObjectHelper.end(URI, PREFIX, contentHandler, "attribute-names");
    }


}
