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

package org.apache.cocoon.forms.formmodel;

import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Assert;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.FormManager;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * Helper class to build Widget test cases.
 * 
 * @version $Id$
 */
public class WidgetTestHelper {
    
    // Private constructor as we only have static methods
    private WidgetTestHelper() {}

    /**
     * Get the result of a widget's generateSaxFragment() method as a Document.
     * <p>
     * The widget's fragment is encapsulated in a root &lt;fi:fragment&gt; element,
     * since there's no guarantee that a widget outputs a single top-level element
     * (there can be several elements, or even none if the widget is invisible)
     * 
     * @param widget the widget of which we want the fragment
     * @param locale the locale to be used to generate the fragment
     * @return the document containing the fragment
     */
    public static Document getWidgetFragment(Widget widget, Locale locale) throws SAXException {
        
        DOMBuilder domBuilder = new DOMBuilder();
        // Start document and "fi:fragment" root element
        domBuilder.startDocument();
        domBuilder.startPrefixMapping(FormsConstants.INSTANCE_PREFIX, FormsConstants.INSTANCE_NS);
        // FIXME: why simply declaring the prefix isn't enough?
        AttributesImpl attr = new AttributesImpl();
        attr.addCDATAAttribute(NamespaceSupport.XMLNS, "fi:", "xmlns:fi", FormsConstants.INSTANCE_NS);
        domBuilder.startElement(FormsConstants.INSTANCE_NS, "fragment", FormsConstants.INSTANCE_PREFIX_COLON + "fragment", attr);
        
        widget.generateSaxFragment(domBuilder, locale);
        
        // End "fi:fragment" element and document
        domBuilder.endElement(FormsConstants.INSTANCE_NS, "fragment", FormsConstants.INSTANCE_PREFIX_COLON + "fragment");
        domBuilder.endPrefixMapping(FormsConstants.INSTANCE_PREFIX);
        domBuilder.endDocument();
        
        // Return the document
        return domBuilder.getDocument();
    }
    
    public static void assertXPathEquals(String expected, String xpath, Document doc) {
        // use xpath as the message
        assertXPathEquals(xpath, expected, xpath, doc);
    }
    
    public static void assertXPathEquals(String message, String expected, String xpath, Document doc) {
        JXPathContext ctx = JXPathContext.newContext(doc);
        ctx.setLenient(true);
        Assert.assertEquals(message, expected, ctx.getValue(xpath));
    }
    
    public static void assertXPathExists(String xpath, Document doc) {
        // use xpath as message
        assertXPathExists(xpath, xpath, doc);
    }
    
    public static void assertXPathExists(String message, String xpath, Document doc) {
        JXPathContext ctx = JXPathContext.newContext(doc);
        ctx.setLenient(true);
        Pointer pointer = ctx.getPointer(xpath);
        Assert.assertNotNull(message, pointer.getNode());
    }
    
    public static void assertXPathNotExists(String xpath, Document doc) {
        // use xpath as message
        assertXPathNotExists(xpath, xpath, doc);
    }
    
    public static void assertXPathNotExists(String message, String xpath, Document doc) {
        JXPathContext ctx = JXPathContext.newContext(doc);
        ctx.setLenient(true);
        Pointer pointer = ctx.getPointer(xpath);
        Assert.assertNull(message, pointer.getNode());
    }
    
    /**
     * Load a Form whose definition relative to a given object (typically, the TestCase class).
     * 
     * @param manager the ServiceManager that will be used to create the form
     * @param obj the object relative to which the resource will be read
     * @param resource the relative resource name for the form definition
     * @return the Form
     * @throws Exception
     */
    public static Form loadForm(ServiceManager manager, Object obj, String resource) throws Exception {
        // Load the document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Grmbl... why isn't this true by default?
        factory.setNamespaceAware(true);
        DocumentBuilder parser = factory.newDocumentBuilder();  
        Document doc = parser.parse(obj.getClass().getResource(resource).toExternalForm());

        // Create the form
        FormManager formManager = (FormManager)manager.lookup(FormManager.ROLE);
        try {
            return formManager.createForm(doc.getDocumentElement());
        } finally {
            manager.release(formManager);
        }
    }
}
