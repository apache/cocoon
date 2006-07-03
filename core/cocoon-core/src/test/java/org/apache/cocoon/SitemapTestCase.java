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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Processor;
import org.apache.cocoon.core.BootstrapEnvironment;
import org.apache.cocoon.core.CoreUtil;
import org.apache.cocoon.core.TestBootstrapEnvironment;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.environment.mock.MockContext;
import org.apache.cocoon.environment.mock.MockEnvironment;
import org.apache.cocoon.environment.mock.MockRequest;
import org.apache.cocoon.environment.mock.MockResponse;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

public class SitemapTestCase extends TestCase {

    private MockRequest request = new MockRequest();
    private MockResponse response = new MockResponse();
    private MockContext environmentContext = new MockContext();
    private Map objectmodel = new HashMap();

    private Logger logger;
    private String classDir;
    private ConfigurableBeanFactory container;
    private ServiceManager serviceManager;
    private Processor rootProcessor;

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

        this.classDir = this.getClassDirURL().toExternalForm();
        BootstrapEnvironment env = 
            new TestBootstrapEnvironment(this.getConfiguration());

        this.container = CoreUtil.createRootContainer(new MockContext(), env);
        this.serviceManager = (ServiceManager)this.container.getBean(ServiceManager.class.getName());
        this.rootProcessor = (Processor)this.container.getBean(Processor.ROLE);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        if ( this.container != null ) {
            this.container.destroySingletons();
        }
        super.tearDown();
    }

    /** Return the logger */
    protected Logger getLogger() {
        return this.logger;
    }
    
    protected final Object lookup( final String key ) throws ServiceException {
        return this.serviceManager.lookup(key);
    }

    protected final void release( final Object object ) {
        this.serviceManager.release(object);
    }
    
    protected String getConfiguration() {
        String className = this.getClass().getName();
        String dir = this.classDir;
        return dir + className.substring(className.lastIndexOf('.') + 1) + ".xconf";
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

        byte[] assertiondocument = null;

        try {
            URL url = new URL(source);
            InputStream input = url.openStream();

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

    protected MockEnvironment getEnvironment(String uri) {
        MockEnvironment env = new MockEnvironment();
        env.setURI("", uri);
        this.request.setEnvironment(env);
        env.setObjectModel(this.objectmodel);

        return env;
    }

    protected byte[] process(String uri) throws Exception {
        MockEnvironment env = getEnvironment(uri);
        this.process(env);
        getLogger().info("Output: " + new String(env.getOutput(), "UTF-8"));

        return env.getOutput();
    }

    protected boolean process(Environment environment) throws Exception {
        environment.startingProcessing();
        final int environmentDepth = EnvironmentHelper.markEnvironment();
        EnvironmentHelper.enterProcessor(this.rootProcessor, environment);
        try {
            boolean result;

            result = this.rootProcessor.process(environment);

            // commit response on success
            environment.commitResponse();

            return result;
        } catch (Exception any) {
            // reset response on error
            environment.tryResetResponse();
            throw any;
        } finally {
            EnvironmentHelper.leaveProcessor();
            environment.finishingProcessing();
            ProcessingUtil.cleanup();

            EnvironmentHelper.checkEnvironment(environmentDepth, this.getLogger());
        }
    }

    protected void pipeTest(String uri, String expectedSource) throws Exception {
        byte[] expected = loadByteArray(this.classDir + expectedSource);
        byte[] actual = process(uri);
        assertIdentical(expected, actual);
    }
}
