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
 * Test pipeline caching
 */
public class CachingOfPipelinesTest extends HtmlUnitTestCase {

    /**
     * A non-caching pipeline mustn't produce the same result twice.
     */
    public void testNonCachingPipeline() throws Exception {
        this.loadResponse("/cocoon-it/caching-pipeline/off");
        String content1 = this.response.getContentAsString();
        assertNotNull(content1);
        this.loadResponse("/cocoon-it/caching-pipeline/off");
        String content2 = this.response.getContentAsString();
        assertNotNull(content2);
        assertFalse("The response has to change with every request.", content1.equals(content2));
    }

    /**
     * This caching pipeline always returns the same.
     */
    public void testCachingPipeline() throws Exception {
        this.loadResponse("/cocoon-it/caching-pipeline/on");
        String content1 = this.response.getContentAsString();
        assertNotNull(content1);
        this.loadResponse("/cocoon-it/caching-pipeline/on");
        String content2 = this.response.getContentAsString();
        assertNotNull(content2);
        assertTrue("The response has to be always the same.", content1.equals(content2));
    }

}
