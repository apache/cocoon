/*
 
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.
 
 */

package org.apache.cocoon.transformation;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.Processor;
import org.apache.cocoon.SitemapComponentTestCase;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.environment.mock.MockEnvironment;

/**
 * A simple testcase for FilterTransformer.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: XIncludeTransformerTestCase.java,v 1.1 2003/12/10 18:52:39 huber Exp $
 */
public class XIncludeTransformerTestCase extends SitemapComponentTestCase {
    
    /** Create new testcase
     * @param name of testase
     */
    public XIncludeTransformerTestCase(String name) {
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
        TestSuite suite = new TestSuite(XIncludeTransformerTestCase.class);
        return suite;
    }
    
    /** Testcase for xinclude simple include
     *
     * @throws Exception iff ComponentManager enterEnvironment fails
     */
    public void testXInclude1() throws Exception {
        getLogger().debug("testXInclude1");
        
        Parameters parameters = new Parameters();
        
        String input = "resource://org/apache/cocoon/transformation/xinclude-input-1.xml";
        String result = "resource://org/apache/cocoon/transformation/xinclude-result-1.xml";
        String src =  null;
        
        // enter & leave environment, as a manager is looked up using
        // the processing context stack
        MockEnvironment env = new
        MockEnvironment(null);
        Processor processor = (Processor)this.manager.lookup(Processor.ROLE);
        
        CocoonComponentManager.enterEnvironment(
        env, this.manager, processor);
        
        assertEqual( load(result),
        transform("xinclude", src, parameters, load(input)));
        
        CocoonComponentManager.leaveEnvironment();
    }
    /** Testcase for xinclude simple text include
     *
     * @throws Exception iff ComponentManager enterEnvironment fails
     */
    public void testXInclude2() throws Exception {
        getLogger().debug("testXInclude2");
        
        Parameters parameters = new Parameters();
        
        String input = "resource://org/apache/cocoon/transformation/xinclude-input-2.xml";
        String result = "resource://org/apache/cocoon/transformation/xinclude-result-2.xml";
        String src =  null;
        
        // enter & leave environment, as a manager is looked up using
        // the processing context stack
        MockEnvironment env = new
        MockEnvironment(null);
        Processor processor = (Processor)this.manager.lookup(Processor.ROLE);
        
        CocoonComponentManager.enterEnvironment(
        env, this.manager, processor);
        
        assertEqual( load(result),
        transform("xinclude", src, parameters, load(input)));
        
        CocoonComponentManager.leaveEnvironment();
    }
    
}
