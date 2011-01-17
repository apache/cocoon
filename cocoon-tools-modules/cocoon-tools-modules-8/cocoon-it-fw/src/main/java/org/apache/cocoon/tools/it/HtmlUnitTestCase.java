/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.tools.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.junit.After;
import org.junit.Before;
import org.w3c.dom.Document;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.SubmitMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.xpath.HtmlUnitXPath;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/**
 * This class is useful as base class for JUnit TestCase classes to setup an
 * environment which makes it possible to easily test web pages. First call one
 * of the load methods and then assert on the response object, XML document
 * (@see loadXmlPage), or HTML document (@see loadHtmlPage).
 *
 * @version $Id$
 */
public abstract class HtmlUnitTestCase {

    private static final String BASEURL = "http://localhost:8888";

    protected Log log = LogFactory.getLog(this.getClass());

    /**
     * Low-level access to WebClient object.
     */
    protected WebClient webClient;

    /**
     * Low-level access to WebResponse object.
     */
    protected WebResponse response;

    /**
     * Low-level access to XML document (org.w3c.dom.Document) or HTML document
     * (com.gargoylesoftware.htmlunit.html.HtmlPage).
     */
    protected Object document;

    /**
     * Low-level access to namespace mappings for XPath expressions.
     */
    protected Map<String, String> namespaces = new HashMap<String, String>();

    @Before
    public void createClient() throws Exception {
        this.webClient = new WebClient();
        this.webClient.setRedirectEnabled(false);
        this.namespaces.clear();
    }

    @After
    public void reset() throws Exception {
        this.response = null;
        this.document = null;
        this.webClient = null;
    }


    protected URL setupBaseUrl() throws Exception {
        String baseUrl = System.getProperty("htmlunit.base-url");
        if (baseUrl == null) {
            baseUrl = BASEURL;
        }
        return new URL(baseUrl);
    }

    /**
     * Sends HTTP GET request and loads response object.
     */
    protected void loadResponse(String pageURL) throws Exception {
        URL url = new URL(this.setupBaseUrl(), pageURL);
        this.response = this.webClient.loadWebResponse(new WebRequestSettings(url, SubmitMethod.GET));
    }

    /**
     * Sends HTTP DELETE request and loads response object.
     */
    protected void loadDeleteResponse(String pageURL) throws Exception {
        URL url = new URL(this.setupBaseUrl(), pageURL);
        DeleteMethod method = new DeleteMethod(url.toExternalForm());
        this.response = new HttpClientResponse(url, method);
    }

    /**
     * Sends HTTP PUT request and loads response object.
     */
    protected void loadPutResponse(String pageURL, String content) throws Exception {
        URL url = new URL(this.setupBaseUrl(), pageURL);
        PutMethod method = new PutMethod(url.toExternalForm());
        method.setRequestEntity(new StringRequestEntity(content));
        this.response = new HttpClientResponse(url, method);
    }

    /**
     * Sends HTTP POST request and loads response object.
     */
    protected void loadPostResponse(String pageURL, String content) throws Exception {
        URL url = new URL(this.setupBaseUrl(), pageURL);
        PostMethod method = new PostMethod(url.toExternalForm());
        method.setRequestEntity(new StringRequestEntity(content));
        this.response = new HttpClientResponse(url, method);
    }

    /**
     * Sends HTTP request and parses response as HTML document.
     */
    protected void loadHtmlPage(String pageURL) throws Exception {
        URL url = new URL(this.setupBaseUrl(), pageURL);
        Page page = this.webClient.getPage(url);
        this.response = page.getWebResponse();
        assertTrue("Response should be an HTML page", page instanceof HtmlPage);
        this.document = page;
        assertNotNull("Response contains invalid HTML", this.document);
    }

    /**
     * Sends HTTP request and parses response as XML document.
     */
    protected void loadXmlPage(String pageURL) throws Exception {
        URL url = new URL(this.setupBaseUrl(), pageURL);
        Page page = this.webClient.getPage(url);
        this.response = page.getWebResponse();
        assertTrue("Response should be an XML page", page instanceof XmlPage);
        XmlPage xmlPage = (XmlPage) page;
        this.document = xmlPage.getXmlDocument();
        assertNotNull("Response contains invalid XML", this.document);
    }

    /**
     * Returns XPath expression as string.
     *
     * @param xpathExpr
     *            XPath expression
     *
     * @return Value of XPath expression in current document. Empty string if
     *         XPath not matched.
     */
    protected String evalXPath(String xpathExpr) throws Exception {
        XPath xpath = null;
        if (this.document == null) {
            return null;
        } else if (this.document instanceof HtmlPage) {
            xpath = new HtmlUnitXPath(xpathExpr);
        } else if (this.document instanceof Document) {
            xpath = new DOMXPath(xpathExpr);
        } else {
            fail("Document type " + this.document.getClass().getName());
        }

        xpath.setNamespaceContext(new SimpleNamespaceContext(this.namespaces));

        return xpath.stringValueOf(this.document);
    }

    /**
     * Add a namespace mapping for XPath expressions.
     */
    protected void addNamespace(String prefix, String uri) throws Exception {
        this.namespaces.put(prefix, uri);
    }

    /**
     * Assert that XPath expression result matches exactly expected value.
     */
    protected void assertXPath(String xpathExpr, String expected) throws Exception {
        assertEquals(xpathExpr, expected, this.evalXPath(xpathExpr));
    }

}
