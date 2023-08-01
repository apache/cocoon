package org.apache.cocoon.it.sitemap;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.cocoon.tools.it.HtmlUnitTestCase;
import org.junit.Assert;

/**
 * Test SAX Pipelines
 */
public class SaxPipelineTest extends HtmlUnitTestCase {

    /**
     * A simple pipeline that produces an HTML document.
     */
    public void testSimplePipeline() throws Exception {
        this.loadResponse("/cocoon-it/sax-pipeline/simple");
        Assert.assertEquals(200, this.response.getStatusCode());
        Assert.assertEquals("text/html", this.response.getContentType());
        Assert.assertEquals(-1, this.response.getContentAsString().indexOf("-//W3C//DTD XHTML 1.0 Strict//EN"));
    }

    /**
     * A simple pipeline that produces an XHTML 1.0 document. This implicitly
     * tests if the configuration of serializers works properly.
     */
    public void testSimplePipelineXhtml() throws Exception {
        this.loadResponse("/cocoon-it/sax-pipeline/simple-xhtml");
        Assert.assertEquals(200, this.response.getStatusCode());
        Assert.assertEquals("text/html", this.response.getContentType());
        Assert.assertTrue(this.response.getContentAsString().indexOf("-//W3C//DTD XHTML 1.0 Strict//EN") > 0);
    }

    /**
     * A parameter is passed to an XSLT transformer.
     */
    public void testSimplePipelineParameterPassingToTransformer() throws Exception {
        this.loadXmlPage("/cocoon-it/sax-pipeline/simple-xml");
        Assert.assertEquals(200, this.response.getStatusCode());
        Assert.assertEquals("text/xml", this.response.getContentType());
        assertXPath("/html/body/p", "3");
    }

    /**
     * A status code is set explicitly at a serializer.
     */
    public void testSettingStatusCode() throws Exception {
        // load this resource twice because the first time when a pipeline that
        // doesn't set the status code 200, is being invoked, 200 is returned
        this.loadResponse("/cocoon-it/sax-pipeline/unauthorized");
        this.loadResponse("/cocoon-it/sax-pipeline/unauthorized");
        int statusCode = this.response.getStatusCode();
        Assert.assertEquals(401, statusCode);
        String lastModified = this.response.getResponseHeaderValue("Last-Modified");
        Assert.assertNotNull(lastModified);
        Assert.assertNotEquals("", lastModified);
    }

}
