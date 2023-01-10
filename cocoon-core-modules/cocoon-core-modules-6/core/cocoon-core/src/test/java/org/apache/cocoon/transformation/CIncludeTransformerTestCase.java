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
public class CIncludeTransformerTestCase extends SitemapComponentTestCase {
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.SitemapComponentTestCase#getSitemapComponentInfo()
     */
    protected String[] getSitemapComponentInfo() {
        return new String[] {Transformer.class.getName(),
                             CIncludeTransformer.class.getName(),
                             "cinclude"};
    }

    /** Testcase for cinclude simple include
     *
     * @throws Exception if ServiceManager enterEnvironment fails
     */
    public void testCInclude1() throws Exception {
        getLogger().debug("testCInclude1");
        
        Parameters parameters = new Parameters();
        parameters.setParameter( "support-caching", "false" );
        
        String input = "resource://org/apache/cocoon/transformation/cinclude-input-1.xml";
        String result = "resource://org/apache/cocoon/transformation/cinclude-result-1.xml";
        String src =  null;
        
        assertEqual( load(result),
        transform("cinclude", src, parameters, load(input)));
    }
    
    /**
     * Testcase for cinclude specifying element for wrapping included content
     *
     * @throws Exception if  enterEnvironment fails
     */
    public void testCInclude2() throws Exception {
        getLogger().debug("testCInclude2");
        
        Parameters parameters = new Parameters();
        parameters.setParameter( "support-caching", "false" );
        
        String input = "resource://org/apache/cocoon/transformation/cinclude-input-2.xml";
        String result = "resource://org/apache/cocoon/transformation/cinclude-result-2.xml";
        String src =  null;
        
        assertEqual( load(result),
        transform("cinclude", src, parameters, load(input)));
    }
    
    /**
     * Testcase for cinclude specifying select attribute, selection elements from the included document
     *
     * @throws Exception if  enterEnvironment fails
     */
    public void testCInclude3() throws Exception {
        getLogger().debug("testCInclude3");
        
        Parameters parameters = new Parameters();
        parameters.setParameter( "support-caching", "false" );
        
        String input = "resource://org/apache/cocoon/transformation/cinclude-input-3.xml";
        String result = "resource://org/apache/cocoon/transformation/cinclude-result-3.xml";
        String src =  null;
        
        assertEqual( load(result),
        transform("cinclude", src, parameters, load(input)));
    }
}
