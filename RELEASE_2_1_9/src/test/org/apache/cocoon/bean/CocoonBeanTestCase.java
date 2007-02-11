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
package org.apache.cocoon.bean;

import java.io.ByteArrayOutputStream;

import org.apache.cocoon.bean.helpers.OutputStreamListener;

import junit.framework.TestCase;

/**
 * <p>Test case for the CocoonBean.</p>
 * 
 * <p>To function correctly, this test case expects a built webapp at
 *    <code>build/webapp/</code>, which includes the test-suite 
 *    within it. Ensure this is included in build.properties by
 *    commenting out <code>exclude.webapp.test-suite=true</code>.</p>
 * 
 * @version CVS $Id: CocoonBeanTestCase.java,v 1.1 2004/04/30 12:55:55 upayavira Exp $
 */
public class CocoonBeanTestCase extends TestCase {

	/**
	 * Constructor for CocoonBeanTest.
	 * @param arg0
	 */
	public CocoonBeanTestCase(String arg0) {
		super(arg0);
        
	}
    
    public void testProcessToStream() throws Exception {
        CocoonBean cocoon = getCocoonBean();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        cocoon.processURI("test-suite/static-site/index.html", baos);
        String result = baos.toString();
        assertEquals(1603, result.length());
        assertTrue(result.indexOf("Cocoon TestSuite")>-1);
        assertTrue(result.indexOf("<h1>General information</h1>")>-1);
        cocoon.dispose();
    }
    
    private CocoonBean getCocoonBean() throws Exception {
        CocoonBean cocoon = new CocoonBean();
        cocoon.setContextDir("build/webapp");
        cocoon.setConfigFile("WEB-INF/cocoon.xconf");
        cocoon.setPrecompileOnly(false);
        cocoon.setWorkDir("build/work");
        cocoon.setLogKit("build/webapp/WEB-INF/logkit.xconf");
        cocoon.setLogger("cli-test");
        cocoon.setLogLevel("DEBUG");
        //cocoon.setAgentOptions(*something*));
        //cocoon.setAcceptOptions(*something*);
        //cocoon.setDefaultFilename(*something*);
        //listener.setReportFile(*something*);
        cocoon.setFollowLinks(true);
        cocoon.setConfirmExtensions(false);
        //cocoon.addLoadedClasses(Arrays.asList(*something*));
        //cocoon.addTargets(BeanConfigurator.processURIFile(*some file*), destDir);
        cocoon.addListener(new OutputStreamListener(System.out));
        cocoon.initialize();
        return cocoon;        
    }
}
