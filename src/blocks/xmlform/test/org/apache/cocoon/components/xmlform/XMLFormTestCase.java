/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.cocoon.components.xmlform;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.AbstractCompositeTestCase;

/**
 *
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: XMLFormTestCase.java,v 1.3 2004/02/02 12:35:04 stephan Exp $
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
