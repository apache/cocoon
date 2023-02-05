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
package org.apache.cocoon.it.blocks.jms;

import org.apache.cocoon.tools.it.HtmlUnitTestCase;

/**
 * Runs various component and integration tests on the JMS block.
 */
public class JMSTest extends HtmlUnitTestCase {

    public void testGetCachedContent() throws Exception {
//        this.loadXmlPage("cocoon-jms-sample/it/demo?pageKey=one");
//        assertEquals(200, this.response.getStatusCode());
//        assertXPath("/eventcache/key", "one");
//        String firstLoaded = this.response.getContentAsString();
//        this.loadXmlPage("cocoon-jms-sample/it/demo?pageKey=one");
//        assertEquals(200, this.response.getStatusCode());
//        assertEquals(this.response.getContentAsString(), firstLoaded);
    }

    /**
     * Verifies a key to be invalidated by invoking an action.
     * 
     * @throws Exception In case of environmental errors.
     */
    public void testUncacheWithJMSAction() throws Exception {
//        this.loadXmlPage("cocoon-jms-sample/it/demo?pageKey=two");
//        assertEquals(200, this.response.getStatusCode());
//        assertXPath("/eventcache/key", "two");
//        String firstLoaded = this.response.getContentAsString();
//        this.loadResponse("cocoon-jms-sample/it/action?event=two&pageKey=two");
//        // Check for correct redirect
//        assertEquals(302, this.response.getStatusCode());
//        // Check, whether the response has changed
//        assertNotSame(this.response.getContentAsString(), firstLoaded);
//        this.loadXmlPage("cocoon-jms-sample/it/demo?pageKey=two");
//        assertXPath("/eventcache/key", "two");
    }
}