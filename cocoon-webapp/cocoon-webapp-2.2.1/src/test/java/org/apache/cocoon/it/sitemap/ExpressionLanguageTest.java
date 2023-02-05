package org.apache.cocoon.it.sitemap;

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

import org.apache.cocoon.tools.it.HtmlUnitTestCase;

/**
 * Test expression language usage
 */
public class ExpressionLanguageTest extends HtmlUnitTestCase {

    /**
     * JEXL test
     */
    public void testJexl() throws Exception {
        this.loadXmlPage("/cocoon-it/expression-language/jexl?fileName=simple");
        assertTrue(this.response.getStatusCode() == 200);
        assertEquals("text/xml", this.response.getContentType());
        assertXPath("/simple", "simple-text");
    }

    /**
     * JXPath test
     */
    public void testJXPath() throws Exception {
        this.loadXmlPage("/cocoon-it/expression-language/jxpath?fileName=simple");
        assertTrue(this.response.getStatusCode() == 200);
        assertEquals("text/xml", this.response.getContentType());
        assertXPath("/simple", "simple-text");
    }

    /**
     * Map language test
     */
    public void testMap() throws Exception {
        this.loadXmlPage("/cocoon-it/expression-language/map/simple");
        assertTrue(this.response.getStatusCode() == 200);
        assertEquals("text/xml", this.response.getContentType());
        assertXPath("/simple", "simple-text");
    }

    /**
     * Nested matchers/maps
     */
    public void testNextedMap() throws Exception {
        this.loadXmlPage("/cocoon-it/expression-language/nested/simple");
        assertTrue(this.response.getStatusCode() == 200);
        assertEquals("text/xml", this.response.getContentType());
        assertXPath("/simple", "simple-text");
    }

}
