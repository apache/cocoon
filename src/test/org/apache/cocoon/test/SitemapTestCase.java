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
package org.apache.cocoon.test;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.core.BootstrapEnvironment;
import org.apache.cocoon.core.CoreUtil;
import org.apache.cocoon.test.core.TestBootstrapEnvironment;
import org.apache.cocoon.test.core.TestCoreUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.mock.MockContext;
import org.apache.cocoon.environment.mock.MockEnvironment;
import org.apache.cocoon.environment.mock.MockRequest;
import org.apache.cocoon.environment.mock.MockResponse;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

public class SitemapTestCase extends TestCase {

    private MockRequest request = new MockRequest();
    private MockResponse response = new MockResponse();
    private MockContext environmentContext = new MockContext();
    private Map objectmodel = new HashMap();

    private Logger logger;
    private CoreUtil coreUtil;
    private Cocoon cocoon;
    private String classDir;

    protected void setUp() throws Exception {
        super.setUp();

        String level = System.getProperty("junit.test.loglevel", "" + ConsoleLogger.LEVEL_DEBUG);
        this.logger = new ConsoleLogger(Integer.parseInt(level));

        objectmodel.clear();

        request.reset();
        objectmodel.put(ObjectModelHelper.REQUEST_OBJECT, request);

        response.reset();
        objectmodel.put(ObjectModelHelper.RESPONSE_OBJECT, response);

        environmentContext.reset();
        objectmodel.put(ObjectModelHelper.CONTEXT_OBJECT, environmentContext);

        String className = this.getClass().getName();
        this.classDir = this.getClassDirURL().toExternalForm();
        BootstrapEnvironment env = 
            new TestBootstrapEnvironment(className.substring(className.lastIndexOf('.') + 1) + ".xconf",
                                         this.getClass().getClassLoader(),
                                         this.classDir,
                                         environmentContext,
                                         this.logger);

        this.coreUtil = new TestCoreUtil(env);
        this.cocoon = this.coreUtil.createCocoon();
    }

    protected void tearDown() throws Exception {
        this.coreUtil.destroy();
        super.tearDown();
    }

    /** Return the logger */
    protected Logger getLogger() {
        return this.logger;
    }
    
    protected final Object lookup( final String key )
    throws ServiceException {
        return this.cocoon.getServiceManager().lookup( key );
    }

    protected final void release( final Object object ) {
        this.cocoon.getServiceManager().release( object );
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
                getClass().getClassLoader().getResource( className ).toExternalForm();
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
     * @param source Source location.
     *
     * @return Binary data.
     */
    public final byte[] loadByteArray(String source) {

        SourceResolver resolver = null;
        Source assertionsource = null;
        byte[] assertiondocument = null;

        try {
            resolver = (SourceResolver) this.cocoon.getSourceResolver();
            assertNotNull("Test lookup of source resolver", resolver);

            assertionsource = resolver.resolveURI(source);
            assertNotNull("Test lookup of assertion source", assertionsource);
            assertTrue("Test if source exist", assertionsource.exists());

            assertNotNull("Test if inputstream of the assertion source is not null",
                          assertionsource.getInputStream());

            InputStream input = assertionsource.getInputStream();
            long size = assertionsource.getContentLength();

            assertiondocument = new byte[(int) size];
            int i = 0;
            int c;

            while ((c = input.read())!=-1) {
                assertiondocument[i] = (byte) c;
                i++;
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

    protected MockEnvironment getEnvironment(String uri) {
        MockEnvironment env = new MockEnvironment();
        env.setURI("", uri);
        this.request.setEnvironment(env);
        env.setObjectModel(this.objectmodel);

        return env;
    }

    protected byte[] process(String uri) throws Exception {
        MockEnvironment env = getEnvironment(uri);
        this.cocoon.process(env);
        getLogger().info("Output: " + new String(env.getOutput(), "UTF-8"));

        return env.getOutput();
    }

    protected void pipeTest(String uri, String expectedSource) throws Exception {
        byte[] expected = loadByteArray(this.classDir + expectedSource);
        byte[] actual = process(uri);
        assertIdentical(expected, actual);
    }
}
