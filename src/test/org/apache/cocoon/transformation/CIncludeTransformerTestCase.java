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
import org.apache.cocoon.Processor;
import org.apache.cocoon.SitemapComponentTestCase;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.environment.mock.MockEnvironment;

/**
 * A simple testcase for FilterTransformer.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id$
 */
public class CIncludeTransformerTestCase extends SitemapComponentTestCase {
    
    /** Create new testcase
     * @param name of testase
     */
    public CIncludeTransformerTestCase(String name) {
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
        TestSuite suite = new TestSuite(CIncludeTransformerTestCase.class);
        return suite;
    }
    
    /** Testcase for cinclude simple include
     *
     * @throws Exception iff ComponentManager enterEnvironment fails
     */
    public void testCInclude1() throws Exception {
        getLogger().debug("testCInclude1");
        
        Parameters parameters = new Parameters();
        parameters.setParameter( "support-caching", "false" );
        
        String input = "resource://org/apache/cocoon/transformation/cinclude-input-1.xml";
        String result = "resource://org/apache/cocoon/transformation/cinclude-result-1.xml";
        String src =  null;
        
        // enter & leave environment, as a manager is looked up using
        // the processing context stack
        MockEnvironment env = new MockEnvironment();
        Processor processor = (Processor)this.manager.lookup(Processor.ROLE);
        
        EnvironmentHelper.enterProcessor(processor, this.manager, env);
        
        assertEqual( load(result),
        transform("cinclude", src, parameters, load(input)));
        
        EnvironmentHelper.leaveProcessor();
    }
    
    /**
     * Testcase for cinclude specifying element for wrapping included content
     *
     * @throws Exception iff ComponentManager enterEnvironment fails
     */
    public void testCInclude2() throws Exception {
        getLogger().debug("testCInclude2");
        
        Parameters parameters = new Parameters();
        parameters.setParameter( "support-caching", "false" );
        
        String input = "resource://org/apache/cocoon/transformation/cinclude-input-2.xml";
        String result = "resource://org/apache/cocoon/transformation/cinclude-result-2.xml";
        String src =  null;
        
        // enter & leave environment, as a manager is looked up using
        // the processing context stack
        MockEnvironment env = new MockEnvironment();
        Processor processor = (Processor)this.manager.lookup(Processor.ROLE);
        
        EnvironmentHelper.enterProcessor(processor, this.manager, env);
        
        assertEqual( load(result),
        transform("cinclude", src, parameters, load(input)));
        
        EnvironmentHelper.leaveProcessor();
    }
    
    /**
     * Testcase for cinclude specifying select attribute, selection elements from the included document
     *
     * @throws Exception iff ComponentManager enterEnvironment fails
     */
    public void testCInclude3() throws Exception {
        getLogger().debug("testCInclude3");
        
        Parameters parameters = new Parameters();
        parameters.setParameter( "support-caching", "false" );
        
        String input = "resource://org/apache/cocoon/transformation/cinclude-input-3.xml";
        String result = "resource://org/apache/cocoon/transformation/cinclude-result-3.xml";
        String src =  null;
        
        // enter & leave environment, as a manager is looked up using
        // the processing context stack
        MockEnvironment env = new MockEnvironment();
        Processor processor = (Processor)this.manager.lookup(Processor.ROLE);
        
        EnvironmentHelper.enterProcessor(processor, this.manager, env);
        
        assertEqual( load(result),
        transform("cinclude", src, parameters, load(input)));
        
        EnvironmentHelper.leaveProcessor();
    }
}
