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

package org.apache.cocoon.forms.datatype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;

import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.mock.MockRequest;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.xml.dom.DOMBuilder;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.impl.ResourceSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test case for CForms's FlowModelSelectionList datatype.
 * @version $Id$
 */
public class FlowJXPathSelectionListTestCase extends AbstractSelectionListTestCase {

    /**
     * Test the generateSaxFragment method.
     */
    public void testGenerateSaxFragment() throws Exception {
        List beans = new ArrayList(2);
        beans.add(new TestBean("1", "One"));
        beans.add(new TestBean("2", "Two"));
        Map flowContextObject = new HashMap();
        flowContextObject.put("beans", beans);
        Request request = new MockRequest();
        Map objectModel = new HashMap();
        FlowHelper.setContextObject(objectModel, flowContextObject);
        objectModel.put(ObjectModelHelper.REQUEST_OBJECT, request);
        Map contextObjectModel = new HashMap();
        contextObjectModel.put(ContextHelper.CONTEXT_OBJECT_MODEL, objectModel);
        Context context = new DefaultContext(contextObjectModel);
        Source sampleSource = new ResourceSource("resource://org/apache/cocoon/forms/datatype/FlowJXPathSelectionListTestCase.source.xml");
        Document sample = this.parser.parse(sampleSource.getInputStream());
        Element datatypeElement = (Element) sample.getElementsByTagNameNS(FormsConstants.DEFINITION_NS, "datatype").item(0);
        Datatype datatype = this.datatypeManager.createDatatype(datatypeElement, false);
        FlowJXPathSelectionList list = new FlowJXPathSelectionList
            (context, "beans", "key", "value", datatype,null,false,null,false);
        DOMBuilder dest = new DOMBuilder();
        list.generateSaxFragment(dest, Locale.ENGLISH);
        Source expectedSource = new ResourceSource("resource://org/apache/cocoon/forms/datatype/FlowJXPathSelectionListTestCase.dest.xml");
        Document expected = this.parser.parse(expectedSource.getInputStream());
        Document destDocument = dest.getDocument();
        assertEqual("Test if generated list matches expected",
            expected, destDocument);
    }
    
    /**
     * Test the generateSaxFragment method with a list containing a null value.
     */
    public void testGenerateSaxFragmentWithNull() throws Exception {
        List beans = new ArrayList(2);
        beans.add(null);
        beans.add(new TestBean("1", "One"));
        beans.add(new TestBean("2", "Two"));
        Map flowContextObject = new HashMap();
        flowContextObject.put("beans", beans);
        Request request = new MockRequest();
        Map objectModel = new HashMap();
        FlowHelper.setContextObject(objectModel, flowContextObject);
        objectModel.put(ObjectModelHelper.REQUEST_OBJECT, request);
        Map contextObjectModel = new HashMap();
        contextObjectModel.put(ContextHelper.CONTEXT_OBJECT_MODEL, objectModel);
        Context context = new DefaultContext(contextObjectModel);
        Source sampleSource = new ResourceSource("resource://org/apache/cocoon/forms/datatype/FlowJXPathSelectionListTestCase.source.xml");
        Document sample = this.parser.parse(sampleSource.getInputStream());
        Element datatypeElement = (Element) sample.getElementsByTagNameNS(FormsConstants.DEFINITION_NS, "datatype").item(0);
        Datatype datatype = this.datatypeManager.createDatatype(datatypeElement, false);
        FlowJXPathSelectionList list = new FlowJXPathSelectionList
            (context, "beans", "key", "value", datatype,null,false,null,false);
        DOMBuilder dest = new DOMBuilder();
        list.generateSaxFragment(dest, Locale.ENGLISH);
        Source expectedSource = new ResourceSource("resource://org/apache/cocoon/forms/datatype/FlowJXPathSelectionListTestCaseWithNull.dest.xml");
        Document expected = this.parser.parse(expectedSource.getInputStream());
        Document destDocument = dest.getDocument();
        assertEqual("Test if generated list matches expected",
                expected, destDocument);
    }
    
    public static class TestBean {
        private String key;
        private String value;

        public TestBean(String key, String value) {
            this.key = key;
            this.value = value;
        }
        
        public String getKey() {
            return this.key;
        }
        
        public String getValue() {
            return this.value;
        }
        
        public String toString() {
            return "{ " + this.key + " : " + this.value + " }";
        }
    }
}
