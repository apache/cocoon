/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generation;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import net.php.servlet;

import org.apache.cocoon.components.parser.Parser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Allows PHP to be used as a generator.  Builds upon the PHP servlet
 * functionallity - overrides the output method in order to pipe the
 * results into SAX events.
 *
 * @author <a href="mailto:rubys@us.ibm.com">Sam Ruby</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2000-08-04 21:11:45 $
 */
public class PhpGenerator extends ServletGenerator {

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
                e.printStackTrace(System.err);
            }
        }

        /******************************************************************/
        /*                       runnable interface                       */ 
        /******************************************************************/

        public void run() {
            try {
                service(request, response, input);
            } catch (ServletException e) {
                this.exception = e;
            }

            try {
                output.close();
            } catch (IOException e) {
                // should never happen
            }
        }

    }

    /**
     * Generate XML data from PHP.
     */
    public void generate() throws IOException, SAXException {

        // ensure that we are serving a file...
        InputSource inputSource = this.resolver.resolveEntity(null, this.source);
        String systemId = inputSource.getSystemId();
        if (!systemId.startsWith("file:/"))
            throw new IOException("protocol not supported: " + systemId);

        try {
            // construct both ends of the pipe
            PipedInputStream input = new PipedInputStream();
        
            // start PHP producing results into the pipe
            PhpServlet php = new PhpServlet();
            php.init(new config(context));
            php.setInput(systemId.substring(6));
            php.setOutput(new PipedOutputStream(input));
            php.setRequest(request);
            php.setResponse(response);
            new Thread(php).start();

            // pipe the results into the parser
            Parser parser=(Parser)this.manager.getComponent("parser");
            parser.setContentHandler(this.contentHandler);
            parser.setLexicalHandler(this.lexicalHandler);
            parser.parse(new InputSource(input));

            // clean up
            php.destroy();
        } catch (SAXException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e.toString());
        }
    }    
}
