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
package org.apache.cocoon.servlet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * This class implements a Servlet 2.3 Cocoon post-processing filter.
 * <p>
 *   First all filters of the filter chain are processed.
 *   As a final filter processing step a Cocoon servlet processing is
 *   done.
 *   In most cases you may want to use this filter for JSP pre-processing,
 *   and Cocoon post-processing. You may use this filter not only for
 *   JSP pre-processing, but you can define any servlet/filter-chain for
 *   the pre-processing.
 * </p>
 * <p>
 *   Xml-data of the filters are passed to cocoon via a request attribute.
 *   On Cocoon side the RequestAttributeGenerator will pick the xml-data, and
 *   normal cocoon pipeline processing is done.
 * </p>
 * <p>
 *  Using this filter you have to setup a filter, and a filter mapping
 *  in the webapp deployment descriptor.
 * </p>
 * <p>
 *  In the cocoon sitemap you have to setup the <code>RequestAttributeGenerator
 *  </code>. The pipeline setup should match the request URI, and it
 *  should use the <code>RequestAttributeGenerator</code> as its generator.
 * </p>
 * <p>
 *   A sample web.xml snippet:
 * </p>
 * <pre><tt>
 *  &lt;filter&gt;
 *    &lt;filter-name&gt;CocoonFilterGenerator&lt;/filter-name&gt;
 *    &lt;display-name&gt;CocoonFilterGenerator&lt;/display-name&gt;
 *    &lt;description&gt;Run JSP/Servlet processing before feeding into Cocoon&lt;/description&gt;
 *    &lt;filter-class&gt;org.apache.cocoon.servlet.CocoonFilterGeneratorImpl&lt;/filter-class&gt;
 *    &lt;init-param&gt;
 *      &lt;param-name&gt;cocoon-servlet-class-name&lt;/param-name&gt;
 *      &lt;param-value&gt;org.apache.cocoon.servlet.CocoonServlet&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *      &lt;param-name&gt;verbose&lt;/param-name&gt;
 *      &lt;param-value&gt;true&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *  &lt;/filter&gt;
 *  &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;CocoonFilterGenerator&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;*.jsp&lt;/url-pattern&gt;
 *  &lt;/filter-mapping&gt;
 * </tt></pre>
 * <p>
 *   A simple sitemap snippet:
 * </p>
 * <pre><code>
 *  &lt;map:match pattern="docs/samples/jsp/*"&gt;
 *    &lt;map:generate type="req-attr"/&gt;
 *    &lt;map:transform src="stylesheets/page/simple-page2html.xsl"&gt;
 *    &lt;map:parameter name="view-source" value="docs/samples/jsp/{1}"/&gt;
 *    &lt;!--
 *    Run-time configuration is done through these
 *    &lt;map:parameter/&gt; elements. Again, let's have a look at the
 *    javadocs:
 *    "[...] All &lt;map:parameter&gt; declarations will be made
 *    available in the XSLT stylesheet as xsl:variables. [...]"
 *    --&gt;
 *    &lt;/map:transform&gt;
 *    &lt;map:serialize/&gt;
 *  &lt;/map:match&gt;
 * </code></pre>
 * <p>
 *   Conclusion of this example: The request URI
 *   <code>/cocoon/docs/samples/jsp/hello.jsp</code> is processed
 *   by the filter <code>CocoonFilterGenerator</code>, due to the
 *   filter-mapping matching.
 *   The pipeline of the snippet matches the request, and the
 *   <code>req-attr</code> generator picks the xml-data of the filter-processing
 *   and sends it into the cocoon pipeline.
 * </p>
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id: CocoonFilterGeneratorImpl.java,v 1.3 2004/03/05 10:07:26 bdelacretaz Exp $
 *
 * @servlet:filter-name    CocoonFilterGeneratorImpl
 */
public class CocoonFilterGeneratorImpl implements Filter {

    /**
     * Specifies the name of the filter configuration,
     * definig the Cocoon servlet class name, ie <code>cocoon-servlet-class-name</code>
     *
     * @since 1.0
     */
    public final static String COCOON_SERVLET_CLASS_NAME_PARAM = "cocoon-servlet-class-name";

    /**
     * Specifies the Cocoon servlet class name default value,
     * ie <code>org.apache.cocoon.servlet.CocoonServlet</code>.
     *
     * @since 1.0
     */
    public final static String COCOON_SERVLET_CLASS_NAME_DEFAULT = "org.apache.cocoon.servlet.CocoonServlet";

    /**
     * Name of filter parameter for setting up verbose mode
     *
     * @since 1.0
     */
    final String VERBOSE_PARAM = "verbose";
    /**
     * Default verbose mode, ie <code>true</code>.
     *
     * @since 1.0
     */
    final boolean VERBOSE_DEFAULT = true;

    /**
     * Currently active verbose mode
     *
     * @since 1.0
     */
    private boolean verbose = false;

    /**
     * filter wide cocoon servlet to use
     *
     * @since 1.0
     */
    private Servlet cocoon = null;

    /**
     * the filter configuration currently used.
     *
     * @since 1.0
     */
    private FilterConfig filterConfig;
    
    public void init(FilterConfig cfg) throws ServletException {
        setFilterConfig( cfg );
    }
    
    /**
     * Sets the filterConfig attribute of the JSPFilterImpl object
     *
     * @param  cfg  The new filterConfig value
     * @since 1.0
     */
    public void setFilterConfig(FilterConfig cfg) {
        filterConfig = cfg;
        logFilterConfig();
        String value;

        value = filterConfig.getInitParameter(VERBOSE_PARAM);
        if (value != null) {
            verbose = ("true".compareToIgnoreCase(value) == 0);
        } else {
            verbose = VERBOSE_DEFAULT;
        }

    }


    /**
     * Gets the filterConfig attribute of the JSPFilterImpl object
     *
     * @return    The filterConfig value
     * @since 1.0
     */
    public FilterConfig getFilterConfig() {
        return filterConfig;
    }


    /**
     * callback method, destroying the filter
     * <p>
     *  If cocoon servlet is defined, destroy it.
     * </p>
     *
     * @since 1.0
     */
    public void destroy() {
        if (cocoon != null) {
            cocoon.destroy();
            cocoon = null;
        }
    }


    /**
     * do the filter processing.
     * <p>
     *   The sequence of this filter:
     * </p>
     * <ol>
     *    <li>Process filters defined in the filter chain</li>
     *    <li>Use cocoon as final processing step</li>
     * </ol>
     *
     * @param  req                                 the http request
     * @param  res                                 the final http response
     * @param  fc                                  the filter chain
     * @exception  java.io.IOException             reading/writing failed
     * @exception  javax.servlet.ServletException  processing failed
     * @since                                      1.0
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc)
             throws java.io.IOException, javax.servlet.ServletException {

        log("doFilter...");
        if (verbose) {
            logServletContext();
        }

        CocoonFilterGeneratorResponseWrapper res_wrapper1 = new CocoonFilterGeneratorResponseWrapper((HttpServletResponse) res);

        // start filtering, process filter chain
        log("do filter pre-processing...");
        fc.doFilter(req, res_wrapper1);

        // put response of pre-processing into request attribute
        byte[] res_wrapper_data;
        log("get xml-data...");
        res_wrapper_data = res_wrapper1.getData();
        if (verbose) {
            log("get xml-data: " + new String(res_wrapper_data));
            log("store xml-data in request-attr " + CocoonFilterGeneratorRequestWrapper.XML_DATA_ATTR);
        }
        CocoonFilterGeneratorRequestWrapper cfgrw = new CocoonFilterGeneratorRequestWrapper((HttpServletRequest) req);
        cfgrw.setXMLData(res_wrapper_data);

        // invoke Cocoon now
        try {
            log("create Cocoon instance...");

            createCocoonInstance();

            log("service using Cocoon instance...");
            cocoon.service(cfgrw, res);
        } catch (Exception e) {
            throw new ServletException("Cannot post-process using Cocoon", e);
        }
    }


    /**
     * Gets the cocoonServletClassName
     * <p>
     *   The cocoon servlet class name can be configured in the
     *   filter configurarion.
     * </p>
     *
     * @return    The cocoonServletClassName value
     * @since     1.0
     * @see       #COCOON_SERVLET_CLASS_NAME_PARAM
     * @see       #COCOON_SERVLET_CLASS_NAME_DEFAULT
     */
    protected String getCocoonServletClassName() {
        String clazz_name = filterConfig.getInitParameter(
                COCOON_SERVLET_CLASS_NAME_PARAM);
        if (clazz_name == null) {
            clazz_name = COCOON_SERVLET_CLASS_NAME_DEFAULT;
        }
        return clazz_name;
    }


    /**
     * Gets the verbose attribute of the CocoonFilterGeneratorImpl object
     *
     * @return     The verbose value
     * @since 1.0
     */
    protected boolean isVerbose() {
        return verbose;
    }


    /**
     * create Cocoon servlet instance.
     *
     * @return                             The cocoonInstance value
     * @exception  ClassNotFoundException  Cocoon servlet class is not available
     * @exception  InstantiationException  failed to instaniate Cocoon servlet class
     * @exception  IllegalAccessException  initializing of Cocoon servlet instance
     *   failed
     * @exception  ServletException        initializing of Cocoon servlet instance
     *   failed
     * @since                              1.0
     */
    protected Servlet createCocoonInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException, ServletException {
        String clazz_name = getCocoonServletClassName();
        Class clazz = Thread.currentThread().getContextClassLoader().loadClass(clazz_name);
        if (cocoon == null) {
            cocoon = (Servlet) clazz.newInstance();
            cocoon.init(new CocoonFilterGeneratorConfig(filterConfig.getServletContext()));
        }
        return cocoon;
    }


    /**
     * log message to servlet context log
     *
     * @param  s  message to log
     * @since 1.0
     */
    protected void log(String s) {
        filterConfig.getServletContext().log("[" + this.getClass().getName() + "] " + String.valueOf(s));
        //System.out.println("[" + this.getClass().getName() + "] " + String.valueOf(s));
    }


    /**
     * Log all filter config parameter
     *
     * @since    1.0
     */
    protected void logFilterConfig() {
        log("filter config name " + filterConfig.getFilterName());

        Enumeration names;
        names = filterConfig.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            Object value = filterConfig.getInitParameter(name);
            log("filter config init parameter " +
                    "name " + String.valueOf(name) + ", " +
                    "value " + String.valueOf(value));
        }
    }


    /**
     * Log all servlet attribute, and parameters.
     *
     * @since    1.0
     */
    protected void logServletContext() {
        ServletContext context = filterConfig.getServletContext();

        Enumeration names;
        names = context.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            Object value = context.getAttribute(name);
            log("context attribute " +
                    "name " + String.valueOf(name) + ", " +
                    "value " + String.valueOf(value));
        }

        names = context.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            Object value = context.getInitParameter(name);
            log("context init parameter " +
                    "name " + String.valueOf(name) + ", " +
                    "value " + String.valueOf(value));
        }
    }


    /**
     * Local request wrapper storing xml-data as a request-attribute.
     *
     * @author     HuberB1
     * @version
     */
    class CocoonFilterGeneratorRequestWrapper extends HttpServletRequestWrapper {
        /**
         * put xml-data into request-attribute under this name, ie. org.apache.cocoon.xml-data
         *
         * @since 1.0
         */
        public final static String XML_DATA_ATTR = "org.apache.cocoon.xml-data";


        /**
         * Constructor for the CocoonGeneratorRequestWrapper object
         *
         * @param  request  Description of Parameter
         * @since 1.0
         */
        public CocoonFilterGeneratorRequestWrapper(HttpServletRequest request) {
            super(request);
        }


        /**
         * Sets the request attribute of the CocoonGeneratorRequestWrapper object
         *
         * @param  xmlData  The new xml-data value
         * @since 1.0
         */
        public void setXMLData(byte[] xmlData) {
            setAttribute(XML_DATA_ATTR, xmlData);
        }


        /**
         * Gets the xml-data from the request attribute
         *
         * @return    The xml-data value
         * @since 1.0
         */
        public byte[] getXMLData() {
            return (byte[]) getAttribute(XML_DATA_ATTR);
        }
    }


    /**
     * Stub implementation of Servlet Config
     *
     * @author     HuberB1
     * @version
     */
    class CocoonFilterGeneratorConfig implements ServletConfig {
        ServletContext c;


        /**
         * Constructor for the config object
         *
         * @param  c  The servlet context in use.
         * @since 1.0
         */
        public CocoonFilterGeneratorConfig(ServletContext c) {
            this.c = c;
        }


        /**
         * Gets the servletName attribute of the config object
         *
         * @return    The servletName value
         * @since 1.0
         */
        public String getServletName() {
            return "JSPEngineImpl";
        }


        /**
         * Gets the initParameterNames attribute of the config object
         *
         * @return    The initParameterNames value
         * @since 1.0
         */
        public Enumeration getInitParameterNames() {
            return c.getInitParameterNames();
        }


        /**
         * Gets the servletContext attribute of the config object
         *
         * @return    The servletContext value
         * @since 1.0
         */
        public ServletContext getServletContext() {
            return c;
        }


        /**
         * Gets the initParameter attribute of the config object
         *
         * @param  name  Description of Parameter
         * @return       The initParameter value
         * @since 1.0
         */
        public String getInitParameter(String name) {
            return null;
        }
    }


    /**
     * A response wrapper storing response in a byte array output stream.
     *
     * @author     HuberB1
     * @version
     */
    class CocoonFilterGeneratorResponseWrapper extends HttpServletResponseWrapper {
        private ByteArrayOutputStream output;
        private int contentLength;
        private String contentType;


        /**
         * Constructor for the GenericResponseWrapper object
         *
         * @param  response  Description of Parameter
         * @since 1.0
         */
        CocoonFilterGeneratorResponseWrapper(HttpServletResponse response) {
            super(response);
            output = new ByteArrayOutputStream();
        }


        /**
         * Sets the contentType attribute of the GenericResponseWrapper object
         *
         * @param  type  The new contentType value
         * @since 1.0
         */
        public void setContentType(String type) {
            this.contentType = type;
            super.setContentType(type);
        }


        /**
         * Gets the outputStream attribute of the GenericResponseWrapper object
         *
         * @return    The outputStream value
         * @since 1.0
         */
        public ServletOutputStream getOutputStream() {
            return new CocoonFilterGeneratorOutputStream(output);
        }


        /**
         * Gets the data attribute of the GenericResponseWrapper object
         *
         * @return    The data value
         * @since 1.0
         */
        public byte[] getData() {
            return output.toByteArray();
        }


        /**
         * Gets the writer attribute of the GenericResponseWrapper object
         *
         * @return                  The writer value
         * @exception  IOException  Description of Exception
         * @since 1.0
         */
        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(getOutputStream(), true);
        }


        /**
         * Gets the contentLength attribute of the GenericResponseWrapper object
         *
         * @return    The contentLength value
         * @since 1.0
         */
        public int getContentLength() {
            return contentLength;
        }


        /**
         * Gets the contentType attribute of the GenericResponseWrapper object
         *
         * @return    The contentType value
         * @since 1.0
         */
        public String getContentType() {
            return contentType;
        }
    }


    /**
     * A servlet output stream storing output into a DataOutputStream.
     *
     * @author     HuberB1
     * @version
     */
    class CocoonFilterGeneratorOutputStream extends ServletOutputStream {

        private DataOutputStream stream;


        /**
         * Constructor for the FilterServletOutputStream object
         *
         * @param  output  Description of Parameter
         * @since 1.0
         */
        public CocoonFilterGeneratorOutputStream(OutputStream output) {
            stream = new DataOutputStream(output);
        }



        /**
         * Description of the Method
         *
         * @param  b                Description of Parameter
         * @exception  IOException  Description of Exception
         * @since 1.0
         */
        public void write(int b) throws IOException {
            stream.write(b);
        }


        /**
         * Description of the Method
         *
         * @param  b                Description of Parameter
         * @exception  IOException  Description of Exception
         * @since 1.0
         */
        public void write(byte[] b) throws IOException {
            stream.write(b);
        }


        /**
         * Description of the Method
         *
         * @param  b                Description of Parameter
         * @param  off              Description of Parameter
         * @param  len              Description of Parameter
         * @exception  IOException  Description of Exception
         * @since 1.0
         */
        public void write(byte[] b, int off, int len) throws IOException {
            stream.write(b, off, len);
        }
    }
}


