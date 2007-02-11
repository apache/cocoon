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

package org.apache.cocoon.components.xmlform;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.AbstractCompositeTestCase;

/**
 *
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: XMLFormTestCase.java,v 1.4 2004/03/05 13:02:38 bdelacretaz Exp $
 */
public class XMLFormTestCase extends AbstractCompositeTestCase {

    public XMLFormTestCase(String name) {
        super(name);
    }

    public void testXMLForm() throws Exception {

        getRequest().addParameter("cocoon-action-start", "true");

        Parameters parameters = new Parameters();

        parameters.setParameter("xmlform-validator-schema-ns",
                                "http://www.ascc.net/xml/schematron");
        parameters.setParameter("xmlform-validator-schema",
                                "resource://org/apache/cocoon/components/xmlform/testschema.xml");
        parameters.setParameter("xmlform-id", "testform");
        parameters.setParameter("xmlform-scope", "session");
        parameters.setParameter("xmlform-model",
                                "org.apache.cocoon.components.xmlform.TestBean");

        Map result = act("xmlform", null, parameters);

        assertNotNull("Test if resource exists", result);
        assertEquals("Test for parameter", "view1",
                     (String) result.get("page"));

        String testform1 = "resource://org/apache/cocoon/components/xmlform/testform1.xml";
        String testresult1 = "resource://org/apache/cocoon/components/xmlform/testresult1.xml";

        assertEqual(load(testresult1),
                    transform("xmlform", testform1, new Parameters(),
                              load(testform1)));

        // Second request

        getRequest().reset();
        getRequest().addParameter("cocoon-xmlform-view", "view1");
        getRequest().addParameter("/system/os", "Other");
        getRequest().addParameter("/system/processor", "p3");
        getRequest().addParameter("/system/@ram", "1024");
        getRequest().addParameter("/system/servletEngine", "Jetty");
        getRequest().addParameter("/system/javaVersion", "1.3");
        getRequest().addParameter("cocoon-action-next", "true");

        result = act("xmlform", null, parameters);

        assertNotNull("Test if resource exists", result);
        assertEquals("Test for parameter", "view2",
                     (String) result.get("page"));

        String testform2 = "resource://org/apache/cocoon/components/xmlform/testform2.xml";
        String testresult2 = "resource://org/apache/cocoon/components/xmlform/testresult2.xml";

        // print(transform("xmlform", testform2, new Parameters(), load(testform2)));

        assertEqual(load(testresult2),
                    transform("xmlform", testform1, new Parameters(),
                              load(testform2)));

        // Third request

        getRequest().reset();
        getRequest().addParameter("cocoon-xmlform-view", "view2");
        getRequest().addParameter("/number", "3");
        getRequest().addParameter("/liveUrl", "http://xml.apache.org");
        getRequest().addParameter("/publish", "false");
        getRequest().addParameter("/favorite[1]/.",
                                  "http://cocoon.apache.org");
        getRequest().addParameter("/favorite[2]/.",
                                  "http://jakarta.apache.org");
        getRequest().addParameter("/favorite[3]/.", "http://www.google.com");
        getRequest().addParameter("cocoon-action-next", "true");

        result = act("xmlform", null, parameters);

        assertNotNull("Test if resource exists", result);
        assertEquals("Test for parameter", "start",
                     (String) result.get("page"));
    }
}
