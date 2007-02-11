/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

public class Bug36872HttpHeaderActionTestCase 
	extends HtmlUnitTestCase {

    static final String BASE_URL = "/samples/test/http-header-action/";
    
    static final String HEADER = "X-HttpHeaderActionTest";
    
    /** some pages must return the X-HttpHeaderActionTest header with the value "from-" + page */
    private void assertHeaderPresent(String page) throws Exception {
        loadResponse(BASE_URL + page);
        final String value = response.getResponseHeaderValue(HEADER);
        assertNotNull("Header '" + HEADER + "' must be present in'" + page + "' response",value);
        final String expected = "from-" + page;
        assertEquals("Header '" + HEADER + "' must match expected value in'" + page + "' response",expected,value);
    }
    
    private void assertHeaderNotPresent(String page) throws Exception {
        loadResponse(BASE_URL + page);
        final String value = response.getResponseHeaderValue(HEADER);
        assertNull("Header '" + HEADER + "' must not be present in'" + page + "' response",value);
    }
    
    public void testMountedSitemap() throws Exception {
    		assertHeaderPresent("mounted-sitemap");
    }

    public void testInternalRequestNoFlow()  throws Exception {
    		// currently, internal requests do not allow HTTP headers to be set, see bugzilla 36872
		assertHeaderNotPresent("internal-request");
}

    public void testInternalRequestWithFlow()  throws Exception {
		assertHeaderPresent("internal-request-flow");
}

}
