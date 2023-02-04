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
 * Test readers
 */
public class ReaderTest extends HtmlUnitTestCase {

	/**
	 * Call a pipeline that explicitly sets the mime-type of the resource.
	 */
    public void testReadingResourceWithExplicitMimeType() throws Exception {
        this.loadResponse("/cocoon-it/read/javascript-resource-explicit");
        assertTrue(this.response.getStatusCode() == 200);
        assertEquals("text/javascript", this.response.getContentType());
        assertEquals("853", this.response.getResponseHeaderValue("Content-Length"));
    }

    /**
     * Call a pipeline that automatically sets the mime-type of the resource.
     */
    public void testReadingResourceWithImplicitMimeType() throws Exception {
    	this.loadResponse("/cocoon-it/read/javascript-resource-implicit");
    	assertTrue(this.response.getStatusCode() == 200);
    	assertEquals("application/x-javascript", this.response.getContentType());
    }

    /**
     * A resource reader supports conditional gets.
     */
    public void testConditionalGet() throws Exception {
    	this.loadResponse("/cocoon-it/read/javascript-resource-implicit");
    	String lastModified = this.response.getResponseHeaderValue("Last-Modified");
    	this.webClient.addRequestHeader("If-Modified-Since", lastModified);
    	this.loadResponse("/cocoon-it/read/javascript-resource-implicit");
    	assertEquals(304, this.response.getStatusCode());
    }

}
