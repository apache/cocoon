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
import org.w3c.dom.Document;


/**
 * A simple testcase for AugmentTransformer.
 *
 * @author <a href="mailto:huber@apache.org">Bernhard Huber</a>
 * @version CVS $Id$
 */
public class AugmentTransformerTestCase extends SitemapComponentTestCase {
    
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
        TestSuite suite = new TestSuite(AugmentTransformerTestCase.class);
        return suite;
    }
    
    /** Testcase for augment transformation
     *
     * @throws Exception if ServiceManager enterEnvironment fails
     */
    public void testAugment1() throws Exception {
        getLogger().debug("testAugment1");
        
        Parameters parameters = new Parameters();
        parameters.setParameter( "mount", "portal1/sect1/" );
        
        String input = "resource://org/apache/cocoon/transformation/augment-input-1.xml";
        String result = "resource://org/apache/cocoon/transformation/augment-result-1.xml";
        String src =  null;
        
        // enter & leave environment, as a manager is looked up using
        // the processing context stack
        MockEnvironment env = new MockEnvironment();
        Processor processor = (Processor)this.lookup(Processor.ROLE);
        
        EnvironmentHelper.enterProcessor(processor, this.getManager(), env);
        
        Document resultDocument = load(result);
        Document inputDocument = load(input);
        Document transformDocument = transform("augment", src, parameters, inputDocument );
        
        printDocs( resultDocument, inputDocument, transformDocument );
        
        assertIdentical( resultDocument, transformDocument );
        
        EnvironmentHelper.leaveProcessor();
    }
    
    /**
     * print documents to System.out
     *
     * @param resultDocument the expected result document
     * @param inputDocument the input document
     * @param transformDocument  the transformed input document
     */
    protected void printDocs( Document resultDocument, Document inputDocument, Document transformDocument ) {
        System.out.println( "resultDocument" );
        this.print( resultDocument );
        System.out.println( "inputDocument" );
        this.print( inputDocument );
        System.out.println( "transformDocument" );
        this.print( transformDocument );
    }
}
