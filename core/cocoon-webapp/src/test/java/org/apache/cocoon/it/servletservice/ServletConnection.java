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

public class ServletConnection extends HtmlUnitTestCase {

    public void testRequestDispatcher() throws Exception {
        this.loadResponse("cocoon-servlet-service-impl-sample/test1/test3");
        Assert.assertTrue(this.response.getStatusCode() == 200);
    }

    public void testSourceResolver() throws Exception {
        this.loadResponse("cocoon-servlet-service-impl-sample/test1/test2");
        Assert.assertTrue(this.response.getStatusCode() == 200);
    }

    public void testRelativeServletConnection1() throws Exception {
        this.loadResponse("cocoon-servlet-service-impl-sample/test1/test5");
        Assert.assertTrue(this.response.getStatusCode() == 200);
    }

    public void testRelativeServletConnection2() throws Exception {
        this.loadResponse("cocoon-servlet-service-impl-sample/test1/test6");
        Assert.assertTrue(this.response.getStatusCode() == 200);
    }

    public void testRelativeServletConnectionErrorStatusCode() throws Exception {
        this.loadResponse("cocoon-servlet-service-impl-sample/test1/test9");
        assertTrue(this.response.getContentAsString().indexOf("Forbidden") > 0);
        Assert.assertTrue(this.response.getStatusCode() == 403);
    }

}
