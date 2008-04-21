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
package org.apache.cocoon.it.servletservice;

import junit.framework.Assert;

import org.apache.cocoon.tools.it.HtmlUnitTestCase;

public class ServletProtocol extends HtmlUnitTestCase {

    public void testSimplePipeline() throws Exception {
        this.loadXmlPage("cocoon-servlet-service-components-sample/1/test");
        Assert.assertTrue(this.response.getStatusCode() == 200);
        assertXPath("/test", "some-text");
    }

    public void testSimpleServletProtocol() throws Exception {
        this.loadXmlPage("cocoon-servlet-service-components-sample/1/test2");
        Assert.assertTrue(this.response.getStatusCode() == 200);
        assertXPath("/test2", "some-text");
    }

    public void testServletProtocolSelfReferencing() throws Exception {
        this.loadXmlPage("cocoon-servlet-service-components-sample/1/test3");
        Assert.assertTrue(this.response.getStatusCode() == 200);
        assertXPath("/test", "some-text");
    }

    public void testResourceReading() throws Exception {
        this.loadXmlPage("cocoon-servlet-service-components-sample/1/test4");
        Assert.assertTrue(this.response.getStatusCode() == 200);
        assertXPath("/test-resource", "some-text");
    }

    public void testServletServiceGenerator() throws Exception {
        this.loadXmlPage("cocoon-servlet-service-components-sample/1/test5");
        Assert.assertTrue(this.response.getStatusCode() == 200);
        assertXPath("/test-result/original-content/test", "some-text");
    }

    public void testServletServiceTransformer() throws Exception {
        this.loadXmlPage("cocoon-servlet-service-components-sample/1/test6");
        Assert.assertTrue(this.response.getStatusCode() == 200);
        assertXPath("/test-result/original-content/test", "some-text");
    }

    public void testServletServiceSerializer() throws Exception {
        this.loadXmlPage("cocoon-servlet-service-components-sample/1/test7");
        Assert.assertTrue(this.response.getStatusCode() == 200);
        assertXPath("/test-result/original-content/test", "some-text");
    }

}
