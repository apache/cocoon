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
package org.apache.cocoon.generation;

import java.net.URL;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.Constants;
import org.apache.cocoon.Processor;
import org.apache.cocoon.SitemapComponentTestCase;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.environment.mock.MockEnvironment;

public class VirtualPipelineGeneratorTestCase extends SitemapComponentTestCase {

    private Processor processor;
    private String classDir;
    private URL classDirURL;

    public void setUp() throws Exception {
        this.classDirURL = getClassDirURL();
        this.classDir = this.classDirURL.toExternalForm();
        super.setUp();
        this.processor = (Processor)this.lookup(Processor.ROLE);
    }

    public void tearDown() throws Exception {
        this.release(this.processor);
        super.tearDown();
    }

    // Hack to get the URL to the directory that this class is in
    private URL getClassDirURL() throws RuntimeException {
        String className = getClass().getName().replace( '.', '/' ) + ".class";
        try {
            String classURL =
                getClass().getClassLoader().getResource( className ).toExternalForm();
            String classDir = classURL.substring(0, classURL.lastIndexOf('/') + 1);

            return new URL(classDir);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't create URL for " + className, e);
        }
    }

    protected void addContext(DefaultContext context) {
        super.addContext(context);
        context.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, getContext());
        context.put(ContextHelper.CONTEXT_ROOT_URL, this.classDirURL);
    }

    protected boolean addSourceFactories() {
        return false;
    }

    public byte[] process(String uri) throws Exception {
        MockEnvironment env = new MockEnvironment();
        env.setURI("", uri);
        getRequest().setEnvironment(env);
        env.setObjectModel(getObjectModel());

        EnvironmentHelper.enterProcessor(this.processor, this.getManager(), env);
        try {
            this.processor.process(env);
            getLogger().info("Output: " + new String(env.getOutput(), "UTF-8"));

            return env.getOutput();
        } finally {
            EnvironmentHelper.leaveProcessor();
        }
    }

    public void pipeTest(String uri, String expectedSource) throws Exception {
        byte[] expected = loadByteArray(this.classDir + expectedSource);
        byte[] actual = process(uri);
        assertIdentical(expected, actual);
    }

    public void testSimplePipe() throws Exception {
        pipeTest("test", "vpc-test.xml");
    }

    public void testVirtualPipe() throws Exception {
        pipeTest("v1", "vpc-test.xml");
    }

    public void testVirtualPipeParam() throws Exception {
        pipeTest("v2", "vpc-param-expected.xml");
    }

    public void testVirtualPipeSourceParam() throws Exception {
        process("v3");
    }

    public void testVirtualSubPipeSourceParam() throws Exception {
        process("sub/v3");
    }

    public void testVirtualPipeSrc() throws Exception {
        process("v4");
    }

    public void testVirtualSubPipeSrc() throws Exception {
        process("sub/v4");
    }
}
