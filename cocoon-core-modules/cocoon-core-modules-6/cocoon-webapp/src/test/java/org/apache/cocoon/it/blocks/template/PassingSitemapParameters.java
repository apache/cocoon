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
package org.apache.cocoon.it.blocks.template;

import junit.framework.Assert;

import org.apache.cocoon.tools.it.HtmlUnitTestCase;

public class PassingSitemapParameters extends HtmlUnitTestCase {

    public void testSimpleSitemapParameterPassing() throws Exception {
        this.loadXmlPage("cocoon-template-sample/it/parameter-from-sitemap");
        Assert.assertTrue(this.response.getStatusCode() == 200);
        assertXPath("/parameters/parameter[@name='abc']", "123");
    }

    public void testSitemapParameterPassingInJxImport() throws Exception {
        this.loadXmlPage("cocoon-template-sample/it/parameter-from-sitemap-with-import");
        Assert.assertTrue(this.response.getStatusCode() == 200);
        assertXPath("/page/p1/parameters/parameter[@name='abc']", "123");
        // Bug: https://issues.apache.org/jira/browse/COCOON-2187
        // assertXPath("/page/p2", "123");
    }

}
