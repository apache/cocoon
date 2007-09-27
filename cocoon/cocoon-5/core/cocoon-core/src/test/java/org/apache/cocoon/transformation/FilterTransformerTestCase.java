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
public class FilterTransformerTestCase extends SitemapComponentTestCase {

    /* (non-Javadoc)
     * @see org.apache.cocoon.SitemapComponentTestCase#getSitemapComponentInfo()
     */
    protected String[] getSitemapComponentInfo() {
        return new String[] {Transformer.class.getName(),
                             FilterTransformer.class.getName(),
                             "filter"};
    }

    /**
     * Testcase for count=1, blocknr=1
     */
    public void testFilter_1_1() throws Exception {
        getLogger().debug("testFilter_1_1");

        Parameters parameters = new Parameters();
        parameters.setParameter( "element-name", "leaf" );
        parameters.setParameter( "count", "1" );
        parameters.setParameter( "blocknr", "1" );

        String input = "resource://org/apache/cocoon/transformation/filter-input.xml";
        String result = "resource://org/apache/cocoon/transformation/filter-result-1-1.xml";
        String src =  null;
        
        assertEqual(load(result), transform("filter", src, parameters, load(input)));
    }
    
    /**
     * Testcase for count=3, blocknr=1
     */
    public void testFilter_3_1() throws Exception {
        getLogger().debug("testFilter_3_1");
        
        Parameters parameters = new Parameters();
        parameters.setParameter( "element-name", "leaf" );
        parameters.setParameter( "count", "3" );
        parameters.setParameter( "blocknr", "1" );
        
        String input = "resource://org/apache/cocoon/transformation/filter-input.xml";
        String result = "resource://org/apache/cocoon/transformation/filter-result-3-1.xml";
        String src =  null;
        
        assertEqual(load(result), transform("filter", src, parameters, load(input)));
    }

    /**
     * Testcase for count=1, blocknr=3
     */
    public void testFilter_1_3() throws Exception {
        getLogger().debug("testFilter_1_3");
        
        Parameters parameters = new Parameters();
        parameters.setParameter( "element-name", "leaf" );
        parameters.setParameter( "count", "1" );
        parameters.setParameter( "blocknr", "3" );
        
        String input = "resource://org/apache/cocoon/transformation/filter-input.xml";
        String result = "resource://org/apache/cocoon/transformation/filter-result-1-3.xml";
        String src =  null;
        
        assertEqual(load(result), transform("filter", src, parameters, load(input)));
    }
}
