/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.it.servletservice;

import junit.framework.Assert;

import org.apache.cocoon.tools.it.HtmlUnitTestCase;

public class RequestInformationPassing extends HtmlUnitTestCase {

    public void testAttributes() throws Exception {
        this.webClient.addRequestHeader("my-header-param", "6");
        this.loadXmlPage("cocoon-servlet-service-impl-sample/test1/test4?foo=5");
        Assert.assertTrue(this.response.getStatusCode() == 200);

        // external request
        // ~~~~~~~~~~~~~~~~

        assertXPath("/page/request-parameters/parameter[@name='foo']/value", "5");
        assertXPath("/page/header-parameters/parameter[@name='my-header-param']/value", "6");
        assertXPath("/page/request-attributes/attribute[@name='foo']/value", "bar");


        // sub request: requst attributes, parameters and header
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        assertXPath("/page/sub-request/page/request-parameters/parameter[@name='foo']", "5");  // from the original request
        assertXPath("/page/sub-request/page/request-parameters/parameter[@name='xyz']", "5");  // passed value
        assertXPath("/page/sub-request/page/header-parameters/parameter[@name='my-header-param']/value", "6"); // from the original request
        assertXPath("/page/sub-request/page/request-attributes/attribute[@name='foo1']/value", "bar1");
        assertXPath("/page/check-sub/request-attribute[@name='foo1']/value", "null");


        // sub request: session handling
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        // a session attribute set from within the called request
        assertXPath("/page/sub-request[@name='demo2-test1']/page/session-attributes/attribute[@name='attribute-from-called-request']",
                        "84");

        // a session attribute set from within the calling request
        assertXPath("/page/sub-request[@name='demo2-test1']/page/session-attributes/attribute[@name='attribute-from-calling-request']",
                        "42");

        // check if a session attribute set in a called request is not accessible in the calling request session
        assertXPath("/page/check-sub/session-attribute[@name='attribute-from-called-request']/value", "null");

        // check if the attribute set the called request before, is still available
        assertXPath("/page/sub-request[@name='demo2-test2']/page/session-attributes/attribute[@name='attribute-from-called-request']",
                    "84");
        // check if the attribute set the calling request is still available
        assertXPath("/page/sub-request[@name='demo2-test2']/page/session-attributes/attribute[@name='attribute-from-calling-request']",
                    "42");  // a session attribute set from within the calling request


    }

}
