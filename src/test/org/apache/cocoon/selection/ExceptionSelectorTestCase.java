/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.selection;

import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.SitemapComponentTestCase;
import org.apache.cocoon.environment.ObjectModelHelper;

public class ExceptionSelectorTestCase extends SitemapComponentTestCase {

    private static final String EXCEPTION_SELECTOR = "exception";    

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
        TestSuite suite = new TestSuite(ExceptionSelectorTestCase.class);
        return suite;
    }
    
    /**
     * A simple exception select test
     */
    public void testExceptionSelect() throws Exception {
        
        // create an exception
        final java.lang.NullPointerException npe = new java.lang.NullPointerException( "ExceptionSelectorTestCase");

        // put the exception into the objectModel
        Map objectModel = this.getObjectModel();
        objectModel.put( ObjectModelHelper.THROWABLE_OBJECT, npe );
        
        Parameters parameters = new Parameters();
        boolean result;
        
        // test selection success
        result = this.select( EXCEPTION_SELECTOR, "npe", parameters );
        System.out.println( result );
        assertTrue( "Test if a npe is selected", result );
        
        // test selection failure
        result = this.select( EXCEPTION_SELECTOR, "non-specified-exception", parameters );
        System.out.println( result );
        assertTrue( "Test if a non specified exception is not selected", !result );
    }

    /**
     * A simple exception select test
     */
    public void testExceptionSelectUnknownException() throws Exception {
        
        // create an exception
        final java.lang.IllegalArgumentException iae = new IllegalArgumentException( "ExceptionSelectorTestCase");

        // put the exception into the objectModel
        Map objectModel = this.getObjectModel();
        objectModel.put( ObjectModelHelper.THROWABLE_OBJECT, iae );
        
        Parameters parameters = new Parameters();
        boolean result;
        
        // test selection failure
        result = this.select( EXCEPTION_SELECTOR, "npe", parameters );
        System.out.println( result );
        assertTrue( "Test if a npe is not selected selected", !result );
    }
    
    /**
     * A simple exception select test
     * The causing exception is listed, thus selecting the unrolling
     * exception fails, selecting the causing exception succeeds.
     */
    public void testExceptionSelectProcessingException() throws Exception {
        
        // create an exception
        final java.lang.NullPointerException npe = new NullPointerException( "NullPointerExceptionSelectorTestCase" );
        final ProcessingException pe = new ProcessingException( "ProcessingExceptionSelectorTestCase", npe );
        
        // put the exception into the objectModel
        Map objectModel = this.getObjectModel();
        objectModel.put( ObjectModelHelper.THROWABLE_OBJECT, pe );
        
        Parameters parameters = new Parameters();
        boolean result;
        
        // test selection success
        result = this.select( EXCEPTION_SELECTOR, "npe", parameters );
        System.out.println( result );
        assertTrue( "Test if a npe is selected", result );
        
        // test selection failure
        result = this.select( EXCEPTION_SELECTOR, "pe", parameters );
        System.out.println( result );
        assertTrue( "Test if a pe is not selected", !result );
    }
    
    /**
     * A simple exception select test.
     * The causing exception is not listed, thus matching the unrolling
     * exception succeeds
     */
    public void testExceptionSelectProcessingException2() throws Exception {
        
        // create an exception
        final java.lang.IllegalArgumentException iae = new IllegalArgumentException( "ExceptionSelectorTestCase");
        final ProcessingException pe = new ProcessingException( "ProcessingExceptionSelectorTestCase", iae );
        
        // put the exception into the objectModel
        Map objectModel = this.getObjectModel();
        objectModel.put( ObjectModelHelper.THROWABLE_OBJECT, pe );
        
        Parameters parameters = new Parameters();
        boolean result;
        
        // test selection success
        result = this.select( EXCEPTION_SELECTOR, "pe", parameters );
        System.out.println( result );
        assertTrue( "Test if a pe is not selected", result );
    }
}
