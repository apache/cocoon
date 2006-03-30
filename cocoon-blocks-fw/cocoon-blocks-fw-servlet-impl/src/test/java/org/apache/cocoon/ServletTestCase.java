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
package org.apache.cocoon;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;


import junit.framework.TestCase;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.Cocoon;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public class ServletTestCase extends TestCase {

	private ServletRunner servletRunner;
    protected ServletUnitClient client;

    private Logger logger;
    private URL classDirURL;
    
    protected String processorClassName = Cocoon.class.getName();

    protected void setUp() throws Exception {
        super.setUp();
        
        String level = System.getProperty("junit.test.loglevel", "" + ConsoleLogger.LEVEL_DEBUG);
        this.logger = new ConsoleLogger(Integer.parseInt(level));

        this.classDirURL = this.getClassDirURL();
        URL webInf = new URL(classDirURL, "WEB-INF/web.xml");
        File webInfFile = new File(webInf.getPath());
        this.servletRunner = new ServletRunner(webInfFile, "");
        this.client = this.servletRunner.newClient();
    }

    protected void tearDown() throws Exception {
    	this.servletRunner.shutDown();
        super.tearDown();
    }

    /** Return the logger */
    protected Logger getLogger() {
        return this.logger;
    }
    
    /**
     * Utility method for geting the URL to the directory that this class is in
     */
    protected URL getClassDirURL() throws RuntimeException {
        String className = getClass().getName().replace( '.', '/' ) + ".class";
        String classURL = null;
        String classDir = null;
        try {
            classURL =
                getClass().getClassLoader().getResource(className).toExternalForm();
            getLogger().debug("classURL=" + classURL);
            classDir = classURL.substring(0, classURL.lastIndexOf('/') + 1);
            getLogger().debug("classDir=" + classDir);
            return new URL(classDir);
        } catch (SecurityException e) {
            throw new RuntimeException("Not allowed to access classloader for " + className, e);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL for className=" + className +
                                       " classURL=" + classURL + " classDir=" + classDir, e);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't create URL for " + className, e);
        }
    }

    /**
     * Load a binary document.
     *
     * @param input Stream containing the document.
     *
     * @return Binary data.
     */
    public final byte[] loadByteArray(InputStream input) {

        byte[] assertiondocument = null;

        try {
            Vector document = new Vector();
            int i = 0;
            int c;

            while ((c = input.read())!=-1) {
                document.add(new Byte((byte) c)); 
                i++;
            }
            assertiondocument = new byte[document.size()];
            for (i = 0; i < document.size(); i++) {
                assertiondocument[i] = ((Byte)document.get(i)).byteValue();
            }

        } catch (Exception e) {
            getLogger().error("Could not execute test", e);
            fail("Could not execute test: "+e);
        }

        return assertiondocument;
    }

    /**
     * Assert that the result of a byte comparison is identical.
     *
     * @param expected The expected byte array
     * @param actual The actual byte array
     */
    public final void assertIdentical(byte[] expected, byte[] actual) {
        assertEquals("Byte arrays of differing sizes, ", expected.length,
                     actual.length);

        if (expected.length>0) {
            for (int i = 0; i<expected.length; i++) {
                assertEquals("Byte array differs at index "+i, expected[i],
                             actual[i]);
            }
        }

    }

    protected InputStream process(String uri) throws Exception {
    	String dummySite = "http://www.test.org";
    	WebRequest request = new GetMethodWebRequest(dummySite + uri);

    	InvocationContext ic = this.client.newInvocation(request);
    	ic.getServlet();
    	ic.service();
    	WebResponse response = ic.getServletResponse();

    	//WebResponse response = this.client.getResponse(request);
    	getLogger().info("Content type: " + response.getContentType());
    	getLogger().info("Content length: " + response.getContentLength());
    	getLogger().info("Output: " + response.getText());

        return response.getInputStream();
    }

    protected void pipeTest(String uri, String expectedSource) throws Exception {
        byte[] expected = loadByteArray((new URL(classDirURL, expectedSource)).openStream());
        byte[] actual = loadByteArray(process(uri));
        assertIdentical(expected, actual);
    }
}
