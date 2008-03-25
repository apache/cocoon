/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.matching;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;


public class WildcardURIMatcherTestCase extends SitemapComponentTestCase {

    public void testWildcardURIMatch() throws Exception {
        getRequest().setRequestURI("/test/foo/bla/end");

        final Parameters parameters = new Parameters();

        Map result = match("wildcard-uri", "**", parameters);
        assertNotNull("Test if resource exists", result);
        assertEquals("Test for **", "test/foo/bla/end", result.get("1"));
        
        result = match("wildcard-uri", "**/bla/*", parameters);
        assertNotNull("Test if resource exists", result);
        assertEquals("Test for **/bla/* {1}", "test/foo", result.get("1"));
        assertEquals("Test for **/bla/* {2}", "end", result.get("2"));
    }

    public void testWildcardURIMatchSimplePattern() throws Exception {
        getRequest().setRequestURI("/test");

        final Parameters parameters = new Parameters();

        Map result = match("wildcard-uri", "*", parameters);
        assertNotNull("Test if resource exists", result);
        assertEquals("Test for *", "test", result.get("1"));
    }

    public void testWildcardURIMatchDoublePattern() throws Exception {
        getRequest().setRequestURI("/test/something.xmlbla.xml");

        final Parameters parameters = new Parameters();

        Map result = match("wildcard-uri", "*/*.xml", parameters);
        assertNotNull("Test if resource exists", result);
        assertEquals("Test for */*.xml", "test", result.get("1"));
        assertEquals("Test for */*.xml", "something.xmlbla", result.get("2"));
    }

}
