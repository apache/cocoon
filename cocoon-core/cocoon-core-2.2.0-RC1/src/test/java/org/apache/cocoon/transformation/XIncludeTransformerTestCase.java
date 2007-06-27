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

package org.apache.cocoon.transformation;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;

/**
 * A simple testcase for FilterTransformer.
 *
 * @version $Id$
 */
public class XIncludeTransformerTestCase extends SitemapComponentTestCase {

    /* (non-Javadoc)
     * @see org.apache.cocoon.SitemapComponentTestCase#getSitemapComponentInfo()
     */
    protected String[] getSitemapComponentInfo() {
        return new String[] {Transformer.class.getName(),
                             XIncludeTransformer.class.getName(),
                             "xinclude"};
    }

    private void xincludeTest(String input, String result) throws Exception {
        assertEqual(load(result), transform("xinclude", null, new Parameters(), load(input)));
    }
    /** Testcase for xinclude simple include
     *
     * @throws Exception if ServiceManager enterEnvironment fails
     */
    public void testXInclude1() throws Exception {
        getLogger().debug("testXInclude1");
        xincludeTest("resource://org/apache/cocoon/transformation/xinclude-input-1.xml",
                "resource://org/apache/cocoon/transformation/xinclude-result-1.xml");
    }

    /** Testcase for xinclude simple text include
     *
     * @throws Exception if ServiceManager enterEnvironment fails
     */
    public void testXInclude2() throws Exception {
        getLogger().debug("testXInclude2");
        xincludeTest("resource://org/apache/cocoon/transformation/xinclude-input-2.xml",
                "resource://org/apache/cocoon/transformation/xinclude-result-2.xml");
    }

    /** Testcase for xinclude simple fallback
     * Check issue: COCOON-1489
     *
     * @throws Exception if ComponentManager enterEnvironment fails
     */
    public void testXIncludeSimpleFallback() throws Exception {
        getLogger().debug("testXIncludeSimpleFallback");
        xincludeTest("resource://org/apache/cocoon/transformation/xinclude-input-fallbackTest.xml",
                "resource://org/apache/cocoon/transformation/xinclude-result-fallbackTest.xml");
    }

    /** Testcase for xinclude with a nested xinclude elemento into the fallback
     * Check issue: COCOON-1489
     *
     * @throws Exception if ComponentManager enterEnvironment fails
     */
    public void testXIncludeNestedXincludeElementInAFallback() throws Exception {
         getLogger().debug("testXIncludeNestedXincludeElementInAFallback");
         xincludeTest("resource://org/apache/cocoon/transformation/xinclude-input-nestedXincludeFallbackTest.xml",
                 "resource://org/apache/cocoon/transformation/xinclude-result-1.xml");
     }

    /**
     * Testcase for xinclude with multiple nested fallbacks
     */
    public void testXIncludeMultipleNestedFallback() throws Exception {
        getLogger().debug("testXIncludeMultipleNestedFallback");
        xincludeTest("resource://org/apache/cocoon/transformation/xinclude-input-multipleNestedFallbackTest.xml",
                "resource://org/apache/cocoon/transformation/xinclude-result-fallbackTest.xml");
    }

    /** Testcase for xinclude simple fallback when parse attribute is 'text'
     *  Check issue: COCOON-1110 
     *
     * @throws Exception if ComponentManager enterEnvironment fails
     */
    public void testXIncludeSimpleFallbackForTextParse() throws Exception {
        getLogger().debug("testXIncludeSimpleFallbackForTextParse");
        xincludeTest("resource://org/apache/cocoon/transformation/xinclude-input-simpleFallbackForTextParseTest.xml",
                "resource://org/apache/cocoon/transformation/xinclude-result-fallbackTest.xml");
    }
}
