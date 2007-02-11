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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.logger.Logger;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.http.HttpEnvironment;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.sax.SAXParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Allows PHP to be used as a generator.  Builds upon the PHP servlet
 * functionallity - overrides the output method in order to pipe the
 * results into SAX events.
 *
 * @author <a href="mailto:rubys@us.ibm.com">Sam Ruby</a>
 * @version CVS $Id: PhpGenerator.java,v 1.2 2004/03/05 13:02:03 bdelacretaz Exp $
 */
public class PhpGenerator extends ServletGenerator  {

    /**
     * Stub implementation of Servlet Config
     */
    class config implements ServletConfig {
        ServletContext c;
        public config(ServletContext c) {this.c = c; }

        public String getServletName() { return "PhpGenerator"; }
        public Enumeration getInitParameterNames()
               { return c.getInitParameterNames(); }
        public ServletContext getServletContext() { return c; }
        public String getInitParameter(String name) { return null; }
    }

    /**
     * Subclass the PHP servlet implementation, overriding the method
     * that produces the output.
     */
    public class PhpServlet extends net.php.servlet implements Runnable {

        String input;
        OutputStream output;
        HttpServletRequest request;
        HttpServletResponse response;
        ServletException exception = null;
        Logger logger;

        protected PhpServlet( Logger logger ) {
            this.logger = logger;
        }
        public void setInput(String input) {
            this.input = input;
        }

        public void setOutput(OutputStream output) {
            this.output = output;
        }

        public void setRequest(HttpServletRequest request) {
            this.request = request;
        }

        public void setResponse(HttpServletResponse response) {
            this.response = response;
        }

        public void write(String data) {
            try {
                output.write(data.getBytes());
            } catch (IOException e) {
                logger.debug("PhpGenerator.write()", e);
                throw new CascadingRuntimeException("PhpGenerator.write()", e);
            }
        }

        /******************************************************************/
        /*                       runnable interface                       */
        /******************************************************************/

        public void run() {
            try {
                service(request, response, input);
            } catch (ServletException e) {
                logger.error("PhpGenerator.run()", e);
                this.exception = e;
            }

            try {
                output.close();
            } catch (IOException e) {
                logger.error("PhpGenerator.run():SHOULD NEVER HAPPEN", e);
                // should never happen
            }
        }

    }

    /**
     * Generate XML data from PHP.
     */
    public void generate() throws IOException, SAXException, ProcessingException {

        // ensure that we are running in a servlet environment
        HttpServletResponse httpResponse =
            (HttpServletResponse)this.objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
        HttpServletRequest httpRequest =
            (HttpServletRequest)this.objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT);
        if (httpResponse == null || httpRequest == null) {
            throw new ProcessingException("HttpServletRequest or HttpServletResponse object not available");
        }

        // ensure that we are serving a file...
        Source inputSource = null;
        SAXParser parser = null;
        try {
            inputSource = this.resolver.resolveURI(this.source);
            String systemId = inputSource.getURI();
            if (!systemId.startsWith("file:/"))
                throw new IOException("protocol not supported: " + systemId);

            // construct both ends of the pipe
            PipedInputStream input = new PipedInputStream();

            // start PHP producing results into the pipe
            PhpServlet php = new PhpServlet( getLogger() );
            php.init(new config((ServletContext)this.objectModel.get(HttpEnvironment.HTTP_SERVLET_CONTEXT)));
            php.setInput(systemId.substring(6));
            php.setOutput(new PipedOutputStream(input));
            php.setRequest(httpRequest);
            php.setResponse(httpResponse);
            new Thread(php).start();

            // pipe the results into the parser
            parser = (SAXParser)this.manager.lookup(SAXParser.ROLE);
            parser.parse(new InputSource(input), this.xmlConsumer);

            // clean up
            php.destroy();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ProcessingException(e.toString(), e);
        } finally {
            this.resolver.release( inputSource );
            this.manager.release( (Component)parser );
        }
    }
}
