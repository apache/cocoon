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

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;

/**
 * A simple testcase for FilterTransformer.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: FilterTransformerTestCase.java,v 1.3 2004/03/05 13:03:03 bdelacretaz Exp $
 */
public class FilterTransformerTestCase extends SitemapComponentTestCase {

    public FilterTransformerTestCase(String name) {
        super(name);
    }
    
    /**
     * Run this test suite from commandline
     *
     * @param args commandline arguments (ignored) 
     */
    public static void main( String[] args ) {
        TestRunner.run(suite());
    }
    
    /** Create a test suite.
     * This test suite contains all test cases of this class.
     * @return the Test object containing all test cases.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(FilterTransformerTestCase.class);
        return suite;
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
