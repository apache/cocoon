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
    
    /** Testcase for xinclude simple include
     *
     * @throws Exception if ServiceManager enterEnvironment fails
     */
    public void testXInclude1() throws Exception {
        getLogger().debug("testXInclude1");
        
        Parameters parameters = new Parameters();
        
        String input = "resource://org/apache/cocoon/transformation/xinclude-input-1.xml";
        String result = "resource://org/apache/cocoon/transformation/xinclude-result-1.xml";
        String src =  null;
        
        assertEqual( load(result),
        transform("xinclude", src, parameters, load(input)));
    }

    /** Testcase for xinclude simple text include
     *
     * @throws Exception if ServiceManager enterEnvironment fails
     */
    public void testXInclude2() throws Exception {
        getLogger().debug("testXInclude2");
        
        Parameters parameters = new Parameters();
        
        String input = "resource://org/apache/cocoon/transformation/xinclude-input-2.xml";
        String result = "resource://org/apache/cocoon/transformation/xinclude-result-2.xml";
        String src =  null;
        
        assertEqual( load(result),
        transform("xinclude", src, parameters, load(input)));
    }
    
}
