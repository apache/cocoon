/*-- $Id: Cocoon.java,v 1.11 2000-01-27 11:50:12 stefano Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */
package org.apache.cocoon;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.cocoon.framework.*;

/**
 * The Cocoon Publishing Framework.
 *
 * This servlet implements an XML/XSL server side publishing framework to
 * separate different knowledge contexts in different processing layers.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.11 $ $Date: 2000-01-27 11:50:12 $
 */

public class Cocoon extends HttpServlet implements Defaults {

    Engine engine = null;
    String message = null;
    Exception exception = null;
    Configurations confs = null;
    String confsName = null;
    String server = null;
    String statusURL = null;
    boolean errorsInternally = false;
    boolean showStatus = false;

    /**
     * Returns the version signature of Cocoon
     */
    public static String version() {
        return NAME + " " + VERSION;
    }

    /**
     * This method initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // Get the servlet environment
        server = config.getServletContext().getServerInfo();

        // Get the initialization argument
        confsName = config.getInitParameter(INIT_ARG);

        if (confsName == null) {
            exception = null;
            message = "<p>The servlet initialization argument <i>\"" + INIT_ARG + "\"</i> was not found. " +
                "Please, make sure Cocoon is able to find its configurations or it won't be able to execute correctly.</p>" +
                "<p>A template for such configurations may be found in the file \"/bin/cocoon.properties\" in the distribution.</p>";
            return;
        }

        try {
            // Create the configuration object
            confs = new Configurations(confsName);
            
            // Save servlet configurations
            showStatus = ((String) confs.get(SHOW_STATUS, "false")).toLowerCase().equals("true");
            statusURL = (String) confs.get(STATUS_URL, STATUS_URL_DEFAULT);
            errorsInternally = ((String) confs.get(ERROR_INTERNALLY, "false")).toLowerCase().equals("true");

            // create the engine
            engine = Engine.getInstance(confs, this.getServletConfig().getServletContext());
        } catch (Exception e) {
            exception = e;
            message = "Publishing Engine could not be initialized.";
        }
    }

    /**
     * This method is called by the servlet engine to handle the request.
     */
    public void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        // if engine is null it means something went wrong during init()
        if (engine == null) {
            Frontend.error(response, message, exception);
        } else {
            // now check if the request is valid to avoid possible security
            // holes using the servlet directly to access information or
            // to bypass web server security restrictions.
            if ((showStatus) && (request.getRequestURI().endsWith(statusURL))) {
                // if the status is enabled and the request matches the status
                // URL indicated in the properties, show the internal status
                Frontend.status(response, getStatus(), engine.getStatus());
            } else {
                try {
                    engine.handle(request, response);
                } catch (FileNotFoundException e) {
                    if (errorsInternally) {
                        Frontend.error(response, "File not found.", e);
                    } else {
                        response.sendError(404, Utils.getStackTraceAsString(e));
                    }
                } catch (Throwable t) {
                    if (errorsInternally) {
                        Frontend.error(response, "Error found handling the request.", t);
                    } else {
                        response.sendError(500, Utils.getStackTraceAsString(t));
                    }
                }
            }
        }
    }

    /**
     * Method called to show the servlet status.
     */
    private Hashtable getStatus() {
        Runtime jvm = Runtime.getRuntime();
        Hashtable table = new Hashtable();
        table.put("Servlet Engine", server);
        table.put("Configurations", confsName);
        table.put("Free Memory", new Long(jvm.freeMemory()));
        table.put("Total Memory", new Long(jvm.totalMemory()));
        return table;
    }

    /**
     * This method returns the Servlet information string.
     */
    public String getServletInfo() {
        return version();
    }
    
    /**
     * The entry point for standalone usage of Cocoon.
     *
     * This part is a little hack to be able to process XML
     * files from the command line. It's not, by no means, a 
     * complete application and it's a dirty patch.
     *
     * If would be nice to have things like wildcards processing
     * to be able to generate static sites from XML+XSL using
     * cron processes and such. Plus the ability to look for 
     * XSL PI to get the stylesheets from inside, plus the ability
     * to print on file, to get URLS instead of files, etc, etc...
     * 
     * As you see, there's room for tons on work on this section.
     */
    public static void main(String[] argument) throws Exception {
        
        String properties = null;
        String xml = null;
        String xsl = null;
        String out = null;
        int i = 0;

        if ((argument.length < 2) || (argument.length > 4)) {
            usage();
        }

        if (argument[i].charAt(0) == '-') {
            properties = getProperties(argument[++i]);
            i++;
        }
        
        xml = argument[i++];
        out = argument[i];
        
        EngineWrapper engine = new EngineWrapper(new Configurations(properties));
        engine.handle(new PrintWriter(new FileWriter(out), true), new File(xml));
    }

    private static void usage() {
        System.err.println("Usage: java org.apache.cocoon.Cocoon [-p properties] Input Output");
        System.err.println("\nOptions:");
        System.err.println("  -p : indicates the property file");
        System.err.println("\nNote: if the property file is not specified, Cocoon looks for a file named");
        System.err.println("\"cocoon.properties\" in the current working directory, in the user directory");
        System.err.println("and in the \"/usr/local/etc/\" directory before giving up.");
        System.exit(1);
    }

    private static String getProperties(String file) throws Exception {
                
        File f;
         
        // look for the indicated file 
        if (file != null) {
            f = new File(file);
            if (f.canRead()) return f.toString();
        }
        
        // look in the current working directory
        f = new File(PROPERTIES);
        if (f.canRead()) return f.toString();

        // then in the user directory
        f = new File(System.getProperty("user.dir") + File.separator + PROPERTIES);
        if (f.canRead()) return f.toString();

        // finally in the /usr/local/etc/ directory (for Unix systems).
        f = new File("/usr/local/etc/" + PROPERTIES);
        if (f.canRead()) return f.toString();
        
        throw new Exception("The property file could not be found.");
    }
}
