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
 * Test error handling of sitemaps.
 */
public class ErrorHandlingTest extends HtmlUnitTestCase {

    /**
     * If no pipeline matches, the error handling section gets activated.
     */
    public void testPerSitemap404() throws Exception {
        // load this resource twice because the first time when a pipeline that
        // doesn't set the status code 200, is being invoked, 200 is returned
        this.loadResponse("/cocoon-it/12345678901234567890");
        this.loadResponse("/cocoon-it/12345678901234567890");
        int statusCode = this.response.getStatusCode();
        assertTrue(statusCode == 404);
        assertTrue(this.response.getContentAsString().indexOf("404 Resource Not Available (Cocoon Integration Tests)") > 0);
    }

    public void testPerSitemapCustomError() throws Exception {
        // load this resource twice because the first time when a pipeline that
        // doesn't set the status code 200, is being invoked, 200 is returned
        this.loadResponse("/cocoon-it/error-handling/custom-error");
        this.loadResponse("/cocoon-it/error-handling/custom-error");
        int statusCode = this.response.getStatusCode();
        assertTrue(statusCode == 500);
        assertTrue(this.response.getContentAsString().indexOf("Error 500 (Cocoon Integration Tests)") > 0);
    }

    public void testPerPipelineCustomError() throws Exception {
        // load this resource twice because the first time when a pipeline that
        // doesn't set the status code 200, is being invoked, 200 is returned
        this.loadResponse("/cocoon-it/error-handling/custom-error-per-pipeline-error-handling");
        this.loadResponse("/cocoon-it/error-handling/custom-error-per-pipeline-error-handling");
        int statusCode = this.response.getStatusCode();
        assertTrue(statusCode == 501);
        assertTrue(this.response.getContentAsString().indexOf("Error 501 (Cocoon Integration Tests)") > 0);
    }

    /*
     * Doesn't work. See https://issues.apache.org/jira/browse/COCOON-2179
     */
    public void testExceptionGenerator() throws Exception {
        for(int i = 0; i < 5; i++) {
            this.loadResponse("/cocoon-it/error-handling/another-custom-error");
            int statusCode = this.response.getStatusCode();
            assertTrue(statusCode == 506);
        }
    }

}
