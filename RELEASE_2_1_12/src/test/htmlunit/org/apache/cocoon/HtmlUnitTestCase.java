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
package org.apache.cocoon;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.SubmitMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.xpath.HtmlUnitXPath;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

import junit.framework.TestCase;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import org.apache.commons.io.FileUtils;

import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;

import org.w3c.dom.Document;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class to run test cases on Cocoon samples.
 * <p>
 *   This class extends the JUnit TestCase class to setup an environment which
 *   makes it possible to easily test Cocoon pages.
 *   First call one of the load methods and then assert on the response object,
 *   XML document (@see loadXmlPage), or HTML document (@see loadHtmlPage). 
 * </p>
 * <p>
 *   Examples:
 * </p>
 * <pre>
 *   public void testStatus() {
 *     loadResponse("/samples/test/status");
 *     assertEquals("Status code", 200, response.getStatusCode());
 *   }
 *
 *   public void testTitle() {
 *     loadHtmlPage("/samples/test/title");
 *     assertXPath("html/head/title"", "The Title");
 *   }
 * </pre>
 *
 * <p>
 *   For loading XML and HTML documents currently on GET requests with
 *   optional querystring are supported.  Please add POST requests and
 *   request parameters when you need them.
 * </p>
 * @version $Id: $
 */
public abstract class HtmlUnitTestCase
    extends TestCase
{
    /**
     * Logger for informative output by test cases.
     * The default log level is WARN but may be changed by setting the
     * property junit.test.loglevel to a different numeric value.
     */
    protected Logger logger;

    /**
     * Base URL of the running Cocoon server which is to be tested.
     * Set by property htmlunit.test.baseurl usually as http://localhost:8888/.
     */
    protected URL baseURL;

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
    protected Map namespaces = new HashMap();

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();

        String level = System.getProperty("junit.test.loglevel", "" + ConsoleLogger.LEVEL_WARN);
        this.logger = new ConsoleLogger(Integer.parseInt(level));

        String baseurl = System.getProperty("htmlunit.test.baseurl");
        this.baseURL = new URL(baseurl);
        this.webClient = new WebClient();
        this.webClient.setRedirectEnabled(false);
        this.namespaces.clear();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown()
        throws Exception
    {
        this.response = null;
        this.document = null;

        super.tearDown();
    }

    /**
     * Sends HTTP GET request and loads response object.
     */
    protected void loadResponse(String pageURL)
        throws Exception
    {
        WebRequestSettings webRequestSettings = new WebRequestSettings(new URL(baseURL, pageURL), SubmitMethod.GET);
        this.response = webClient.loadWebResponse(webRequestSettings);
    }

    /**
     * Sends HTTP DELETE request and loads response object.
     */
    protected void loadDeleteResponse(String pageURL)
        throws Exception
    {
        URL url = new URL(baseURL, pageURL);
        DeleteMethod method = new DeleteMethod(url.toExternalForm());
        this.response = new HttpClientResponse(url, method);
    }

    /**
     * Sends HTTP PUT request and loads response object.
     */
    protected void loadPutResponse(String pageURL, String content)
        throws Exception
    {
        URL url = new URL(baseURL, pageURL);
        PutMethod method = new PutMethod(url.toExternalForm());
        method.setRequestEntity(new StringRequestEntity(content));
        this.response = new HttpClientResponse(url, method);
    }

    /**
     * Sends HTTP request and parses response as HTML document.
     */
    protected void loadHtmlPage(String pageURL)
        throws Exception
    {
        URL url = new URL(baseURL, pageURL);
        Page page = webClient.getPage(url);
        this.response = page.getWebResponse();
        assertTrue("Response should be an HTML page", page instanceof HtmlPage);
        this.document = page;
        assertNotNull("Response contains invalid HTML", this.document);
    }

    /**
     * Sends HTTP request and parses response as XML document.
     */
    protected void loadXmlPage(String pageURL)
        throws Exception
    {
        URL url = new URL(baseURL, pageURL);
        Page page = webClient.getPage(url);
        this.response = page.getWebResponse();
        assertTrue("Response should be an XML page", page instanceof XmlPage);
        XmlPage xmlPage = (XmlPage)page;
        this.document = xmlPage.getXmlDocument();
        assertNotNull("Response contains invalid XML", this.document);
    }

    /**
     * Returns XPath expression as string.
     *
     * @param xpathExpr XPath expression
     *
     * @return Value of XPath expression in current document.
     *         Empty string if XPath not matched.
     */
    protected String evalXPath(String xpathExpr)
        throws Exception
    {
        XPath xpath = null;
        if( document == null )
            return null;
        else if( document instanceof HtmlPage )
            xpath = new HtmlUnitXPath(xpathExpr);
        else if( document instanceof Document )
            xpath = new DOMXPath(xpathExpr);
        else
            fail("Document type "+document.getClass().getName());

        xpath.setNamespaceContext(new SimpleNamespaceContext(namespaces));

        return xpath.stringValueOf(document);
    }

    /**
     * Add a namespace mapping for XPath expressions.
     */
    protected void addNamespace(String prefix, String uri)
        throws Exception
    {
        namespaces.put(prefix, uri);
    }

    /**
     * Assert that XPath expression result matches exactly expected value.
     */
    protected void assertXPath(String xpathExpr, String expected)
        throws Exception
    {
        assertEquals(xpathExpr, expected, evalXPath(xpathExpr));
    }

    /**
     * Copy file from webapp source to deployment area filtering content
     * to replace parameter by value.
     * The source and deployment directories are defined by the properties
     * htmlunit.test.source-dir and htmlunit.test.deploy-dir.
     *
     * This method is most useful for testing the automatic reloading of
     * changed files.
     */
    protected void copyWebappFile(String filename, String param, String value)
        throws Exception
    {
        String srcdir = System.getProperty("htmlunit.test.source-dir");
        String dstdir = System.getProperty("htmlunit.test.deploy-dir");
        File srcfile = new File(srcdir+"/"+filename);
        File dstfile = new File(dstdir+"/"+filename);

        final String encoding = "ISO-8859-1";
        StringBuffer content = new StringBuffer(FileUtils.readFileToString(srcfile, encoding));

        int index = content.indexOf(param);
        while( index != -1 ) {
            content.replace(index, index+param.length(), value);
            index = content.indexOf(param, index+1);
        }

        FileUtils.writeStringToFile(dstfile, content.toString(), encoding);

        // Leave server some time to realize that file has changed.
        Thread.sleep(1000);
    }
}
