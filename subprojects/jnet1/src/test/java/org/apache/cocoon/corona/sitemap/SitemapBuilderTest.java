/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.corona.sitemap;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.cocoon.corona.pipeline.action.CustomException;
import org.apache.cocoon.corona.servlet.node.StatusCodeCollector;
import org.apache.cocoon.corona.servlet.util.HttpContextHelper;
import org.apache.cocoon.corona.sitemap.node.InvocationResult;
import org.apache.cocoon.corona.sitemap.node.Sitemap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SitemapBuilderTest extends TestCase {

    private Sitemap sitemap;
    private SitemapBuilder sitemapBuilder;
    private ComponentProvider componentProvider;

    public void testErrorHandlingGlobal() {
        // TODO: currently this cannot work since some components for error
        // handling are still missing
        // Invocation invocation =
        // this.buildInvocation("error-handling/custom-error");
        // InvocationResult invocationResult = this.sitemap.invoke(invocation);
        // assertNotNull(invocationResult);
        // assertSame(InvocationResult.COMPLETED, invocationResult);
        //
        // // invocation should be marked as error-invocation
        // assertTrue(invocation.isErrorInvocation());
        // // the throwable should be our exception
        // assertTrue(invocation.getThrowable().toString(),
        // invocation.getThrowable() instanceof CustomException);
    }

    public void testErrorHandlingPipeline() {
        Invocation invocation = this.buildInvocation("error-handling/custom-error-per-pipeline-error-handling");
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        HttpContextHelper.storeResponse(mockHttpServletResponse, invocation.getParameters());

        this.sitemap.invoke(invocation);
        // invocation should be marked as error-invocation
        assertTrue(invocation.isErrorInvocation());
        // the throwable should be our exception
        assertTrue("Expected CustomException but received " + invocation.getThrowable(),
                invocation.getThrowable() instanceof CustomException);

        assertEquals(501, StatusCodeCollector.getStatusCode());
    }

    public void testGenerator() {
        Invocation invocation = this.buildInvocation("sax-pipeline/unauthorized");
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        HttpContextHelper.storeResponse(mockHttpServletResponse, invocation.getParameters());

        InvocationResult invocationResult = this.sitemap.invoke(invocation);
        assertNotNull(invocationResult);
        assertSame(InvocationResult.COMPLETED, invocationResult);

        // invocation should not be marked as error-invocation
        assertFalse(invocation.isErrorInvocation());
        assertEquals(401, StatusCodeCollector.getStatusCode());
    }

    public void testNoMatchingPipeline() {
        Invocation invocation = this.buildInvocation("unknown");
        InvocationResult invocationResult = this.sitemap.invoke(invocation);

        assertNotNull(invocationResult);
        assertTrue(invocation.isErrorInvocation());
        assertTrue("Expected NoMatchingPipelineException but received " + invocation.getThrowable(),
                invocation.getThrowable() instanceof NoMatchingPipelineException);
    }


    public void testController() {
        Invocation invocation = this.buildInvocation("controller/invoke");
        InvocationResult invocationResult = sitemap.invoke(invocation);
        assertNotNull(invocationResult);
        assertTrue(invocationResult.isCompleted());
    }

    public void testXSLT() {
        Invocation invocation = this.buildInvocation("xslt/main");
        InvocationResult invocationResult = this.sitemap.invoke(invocation);

        assertNotNull(invocationResult);
    }

    public void testObjectModelPipeline() {
        Invocation invocation = this.buildInvocation("object-model/request-parameters");
        Map<String, String> requestParameters = new HashMap<String, String>();
        requestParameters.put("a", "1");
        requestParameters.put("b", "2");
        requestParameters.put("c", "3");
        HttpContextHelper.storeRequest(new MockHttpServletRequest(requestParameters), invocation.getParameters());
        this.sitemap.invoke(invocation);
        // invocation not should be marked as error-invocation
        assertFalse(invocation.isErrorInvocation());
    }

    public void testExpressionLanguage() {
        Invocation invocation = this.buildInvocation("expression-language/map/simple");
        this.sitemap.invoke(invocation);
        // invocation should not be marked as error-invocation
        assertFalse("InvocationImpl is marked as erroneous", invocation.isErrorInvocation());
    }

    public void testExpressionLanguage2() {
        Invocation invocation = this.buildInvocation("expression-language/nested/simple");
        this.sitemap.invoke(invocation);
        // invocation should not be marked as error-invocation
        assertFalse("InvocationImpl is marked as erroneous", invocation.isErrorInvocation());
    }

    // TODO: steven.dolg [2008-02-21]: cannot work until expression-language is
    // integrated correctly
    // public void testExpressionLanguage2() {
    // InvocationImpl invocation =
    // this.buildInvocation("expression-language/nested2/test");
    // this.sitemap.invoke(invocation);
    // // invocation should not be marked as error-invocation
    // assertFalse(invocation.isErrorInvocation());
    // }

    // TODO: steven.dolg [2008-02-21]: cannot work until expression-language is
    // integrated correctly
    // public void testExpressionLanguage3() {
    // InvocationImpl invocation =
    // this.buildInvocation("expression-language/nested3/test");
    // this.sitemap.invoke(invocation);
    // // invocation should not be marked as error-invocation
    // assertFalse(invocation.isErrorInvocation());
    // }

    public void testReadPipelineExplicit() {
        Invocation invocation = this.buildInvocation("read/javascript-resource-explicit");
        assertTrue(this.sitemap.invoke(invocation).isCompleted());
        // invocation should not be marked as error-invocation
        assertFalse(invocation.isErrorInvocation());
    }

    public void testReadPipelineImplicit() {
        Invocation invocation = this.buildInvocation("read/javascript-resource-implicit");
        assertTrue(this.sitemap.invoke(invocation).isCompleted());
        // invocation should not be marked as error-invocation
        assertFalse(invocation.isErrorInvocation());
    }

    public void testRedirectPipeline() {
        Invocation invocation = this.buildInvocation("redirect/www.orf.at");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(HttpServletResponse.class.getName(), response);
        invocation.setParameters(parameters);

        assertTrue(this.sitemap.invoke(invocation).isCompleted());
        // invocation should not be marked as error-invocation
        assertFalse("InvocationImpl is marked as erroneous.", invocation.isErrorInvocation());
        assertTrue(response.hasRedirected());
    }

    @Override
    protected void setUp() throws Exception {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {
                "META-INF/cocoon/spring/applicationContext.xml", "META-INF/cocoon/spring/corona-pipeline-action.xml",
                "META-INF/cocoon/spring/corona-pipeline-component.xml", "META-INF/cocoon/spring/corona-pipeline.xml",
                "META-INF/cocoon/spring/corona-sitemap-node.xml", "META-INF/cocoon/spring/corona-expression-language.xml",
                "META-INF/cocoon/spring/corona-servlet-node.xml", "META-INF/cocoon/spring/corona-servlet-component.xml",
                "META-INF/cocoon/spring/corona-controller.xml"});

        this.componentProvider = (ComponentProvider) applicationContext.getBean("org.apache.cocoon.corona.sitemap.ComponentProvider");

        URL sitemapURL = this.getClass().getResource("/COB-INF/sitemap.xmap");
        this.sitemapBuilder = (SitemapBuilder) applicationContext.getBean("org.apache.cocoon.corona.sitemap.SitemapBuilder");
        this.sitemap = this.sitemapBuilder.build(sitemapURL);
    }

    private Invocation buildInvocation(String request) {
        InvocationImpl invocation = new InvocationImpl(System.out);

        invocation.setRequestURI(request);
        invocation.setComponentProvider(this.componentProvider);

        return invocation;
    }
}
