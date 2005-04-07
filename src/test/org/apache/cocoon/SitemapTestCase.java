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

import java.net.URL;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.Constants;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.environment.mock.MockEnvironment;

public class SitemapTestCase extends SitemapComponentTestCase {

    protected Processor processor;
    protected String classDir;
    protected URL classDirURL;

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

    protected void prepare()
    throws Exception {
        // The context is set in addContext instead
        Configuration context = new DefaultConfiguration("", "-");

        URL rolesURL =
            getClass().getClassLoader().getResource("org/apache/cocoon/cocoon.roles");
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        Configuration roles = builder.build( rolesURL.openStream() );

        String componentsName = getClass().getName().replace( '.', '/' ) + ".xconf";
        URL componentsURL = getClass().getClassLoader().getResource( componentsName );
        Configuration components;
        if ( componentsURL != null ) {
            components = builder.build( componentsURL.openStream() );
        } else {
            components = new DefaultConfiguration("", "-");
        }
        prepare(context, roles, components);
    }


    // Hack to get the URL to the directory that this class is in
    protected URL getClassDirURL() throws RuntimeException {
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

    protected byte[] process(String uri) throws Exception {
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

    protected void pipeTest(String uri, String expectedSource) throws Exception {
        byte[] expected = loadByteArray(this.classDir + expectedSource);
        byte[] actual = process(uri);
        assertIdentical(expected, actual);
    }
}
