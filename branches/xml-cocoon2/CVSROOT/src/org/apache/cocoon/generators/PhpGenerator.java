/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generators;

import java.io.*;
import java.util.*;
import java.net.*;

import javax.servlet.*;

import net.php.servlet;
import net.php.reflect;

import org.apache.cocoon.Response;
import org.apache.cocoon.Request;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.ProcessingException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Allows PHP to be used as a generator.  Builds upon the PHP servlet
 * functionallity.  Taking a step back, the PHP functionality needs to
 * be restructured so that faking out all this servlet infrastructure
 * is not required in order to simply get a response produced from a
 * request...
 *
 * @author <a href="mailto:rubys@us.ibm.com">Sam Ruby</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-06-28 00:28:16 $
 */
public class PhpGenerator extends ComposerGenerator {

    /**
     * Stub implementation of Servlet Context
     */
    class Context implements ServletContext {
        Vector v = new Vector();
        public ServletContext getContext(String uripath) { return null; }
        public int getMajorVersion() { return 0; }
        public int getMinorVersion() { return 0; }
        public String getMimeType(String file) { return ""; }
        public java.net.URL getResource(String path) { return null; }
        public InputStream getResourceAsStream(String path) { return null; }
        public RequestDispatcher getRequestDispatcher(String path) 
               { return null; }
        public RequestDispatcher getNamedDispatcher(String name) 
               { return null; }
        public void log(String msg) {}
        public void log(String message, Throwable throwable) {}
        public String getRealPath(String path) { return ""; }
        public String getServerInfo() { return "Cocoon PhpGenerator"; }
        public String getInitParameter(String name) { return ""; }
        public Enumeration getInitParameterNames() { return v.elements(); }
        public Object getAttribute(String name) { return null; }
        public Enumeration getAttributeNames() { return v.elements(); }
        public void setAttribute(String name, Object object) {};
        public void removeAttribute(String name) {};

        /**
         * @deprecated
         */
        public Servlet getServlet(String name) { return null; };
        /**
         * @deprecated
         */
        public Enumeration getServlets() { return v.elements(); }
        /**
         * @deprecated
         */
        public Enumeration getServletNames() { return v.elements(); }
        /**
         * @deprecated
         */
        public void log(Exception exception, String msg) {}
    }

    /**
     * Stub implementation of Servlet Config
     */
    class Config implements ServletConfig {
        Context c = new Context();
        public String getServletName() { return "PhpGenerator"; }
        public Enumeration getInitParameterNames() 
               { return c.getInitParameterNames(); }
        public ServletContext getServletContext() { return c; }
        public String getInitParameter(String name) { return null; }
    }

    /**
     * Subclass the PHP servlet implementation, replacing calls to the
     * javax.servlet objects like request and response with the cocoon
     * equivalents.
     */
    public class PhpServlet extends net.php.servlet implements Runnable {

        Request request;
        Response response;
        OutputStream output;
        String source;

        /******************************************************************/
        /*                       php sapi callbacks                       */ 
        /******************************************************************/

        public String readPost(int bytes) {
            String result;

            Enumeration e = request.getParameterNames();
            result="";
            String concat="";
            while (e.hasMoreElements()) {
                String name = (String)e.nextElement();
                String value = request.getParameter(name);
                result+=concat+name+"="+URLEncoder.encode(value);
                concat="&";
            }

            return result; 
        }

        public String readCookies() {
            reflect.setResult(define("request"), request);
            reflect.setResult(define("response"), response);
            reflect.setResult(define("PHP_SELF"), request.getUri());
            return request.getHeader("cookie");
        }

        public void header(String data) {

            try {
                if (data.startsWith("Content-type: ")) {
                    response.setContentType(data.substring(data.indexOf(" ")+0));
                } else {
                    int colon = data.indexOf(": ");
                    if (colon > 0) {
                        response.setHeader(data.substring(0,colon), 
                                           data.substring(colon+2) );
                    } else {
                        output.write((data+"\n").getBytes());
                    }
                }
            } catch (IOException e) {
                  e.printStackTrace(System.err);
            }

        }

        public void write(String data) {
            try {
                output.write(data.getBytes());
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }

        /******************************************************************/
        /*                        servlet interface                       */ 
        /******************************************************************/

        public void service(Request request, Response response, 
                            OutputStream output, String source)
        {
            this.request=request;
            this.response=response;
            this.output=output;
            this.source=source;
        }

        /******************************************************************/
        /*                       runnable interface                       */ 
        /******************************************************************/

        public void run() {
            send("GET", null, request.getUri(), source, null, -1, null, false);

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
        Cocoon cocoon=(Cocoon)this.manager.getComponent("cocoon");
        Parser parser=(Parser)this.manager.getComponent("parser");
        parser.setContentHandler(this.contentHandler);
        parser.setLexicalHandler(this.lexicalHandler);
        
        // ensure that we are serving a file...
        String systemId = cocoon.resolveEntity(this.source).getSystemId();
        if (!systemId.startsWith("file:/"))
            throw new IOException("protocol not supported: " + systemId);

        try {
            // construct both ends of the pipe
            PipedInputStream input = new PipedInputStream();
            PipedOutputStream output = new PipedOutputStream(input);
        
            // start PHP producing results into the pipe
            PhpServlet php = new PhpServlet();
            php.init(new Config());
            php.service(request, response, output, systemId.substring(6));
            new Thread(php).start();

            // pipe the results into the parser
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
