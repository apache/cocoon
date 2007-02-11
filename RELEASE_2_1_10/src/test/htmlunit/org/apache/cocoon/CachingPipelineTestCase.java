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
package org.apache.cocoon;

/**
 * Test the CachingProcessingPipeline and CachingPointProcessingPipeline.
 *
 * Uses the test-suite/caching module in the Cocoon webapp.  The pipelines
 * invoke the IncrementGenerator and IncrementTransformer that both increment a
 * value in an attribute of the Cocoon context.
 *
 * @version $Id$
 */
public class CachingPipelineTestCase extends HtmlUnitTestCase {
	final String testPipeline = "/test-suite/caching/a";
	final String clearCachePipeline = "/test-suite/caching/clear-cache";
	final String checkPipeline = "/test-suite/caching/check";
	final String resetPipeline = "/test-suite/caching/reset";
    final String sitemapPath = "test-suite/caching/sitemap.xmap";
    final String resultPath = "context/key[@name='count']";
	public void testCachingProcessingPipeline() throws Exception {
		// replace @pipeline.type@ with caching
        copyWebappFile(sitemapPath, "@pipeline.type@", "caching");
		// clear cache
        loadResponse(clearCachePipeline);
        // reset count
        loadResponse(resetPipeline);
		// execute pipeline a1 and a2
        loadResponse(testPipeline + "1");
        loadResponse(testPipeline + "2");
		// check that count is 2
        loadXmlPage(checkPipeline);
        String count = evalXPath(resultPath);
        assertEquals("4", count);
		// check that store has 2 objects
	}
	public void testCachingPointProcessingPipeline() throws Exception {
		// replace @pipeline.type@ with caching-point
        copyWebappFile(sitemapPath, "@pipeline.type@", "caching-point");
		// clear cache
        loadResponse(clearCachePipeline);
        // reset count
        loadResponse(resetPipeline);
		// execute pipeline a1 and a2
        loadResponse(testPipeline + "1");
        loadResponse(testPipeline + "2");
		// check that count is 3
        loadXmlPage(checkPipeline);
        String count = evalXPath(resultPath);
        assertEquals("3", count);
		// check that store has 3 objects
	}
}
