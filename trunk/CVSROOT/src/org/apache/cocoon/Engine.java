/*-- $Id: Engine.java,v 1.4 1999-11-30 16:30:03 stefano Exp $ -- 

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
import org.w3c.dom.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.cocoon.cache.*;
import org.apache.cocoon.store.*;
import org.apache.cocoon.parser.*;
import org.apache.cocoon.producer.*;
import org.apache.cocoon.formatter.*;
import org.apache.cocoon.processor.*;
import org.apache.cocoon.framework.*;
import org.apache.cocoon.interpreter.*;

/**
 * The Cocoon publishing engine.
 *
 * This class implements the engine that does all the document processing.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.4 $ $Date: 1999-11-30 16:30:03 $
 */

public class Engine implements Defaults {

    Configurations configurations;
    
    boolean showStatus;

    ProducerFactory producers;
    ProcessorFactory processors;
    FormatterFactory formatters;

    Manager manager;
    Browsers browsers;
    Parser parser;
    Cache cache;
    Store store;

    /**
     * This method initializes the engine.
     */
    public Engine(Configurations configurations) throws Exception {

		// stores the configuration instance
        this.configurations = configurations;

        // Create the object manager which is both Factory and Director
        // and register it
        manager = new Manager();
        manager.setRole("factory", manager);

        // Create the parser and register it
        parser = (Parser) manager.create((String) configurations.get(PARSER_PROP, PARSER_DEFAULT), configurations.getConfigurations(PARSER_PROP));
        manager.setRole("parser", parser);

        // Create the store and register it
        store = (Store) manager.create((String) configurations.get(STORE_PROP, STORE_DEFAULT), configurations.getConfigurations(STORE_PROP));
        manager.setRole("store", store);

        // Create the cache and register it
        cache = (Cache) manager.create((String) configurations.get(CACHE_PROP, CACHE_DEFAULT), configurations.getConfigurations(CACHE_PROP));
        manager.setRole("cache", cache);

        // Create the interpreter factory and register it
        InterpreterFactory interpreters = (InterpreterFactory) manager.create("org.apache.cocoon.interpreter.InterpreterFactory", 
            configurations.getConfigurations(INTERPRETER_PROP));
        manager.setRole("interpreters", interpreters);

        // Create the producer factory and register it
        producers = (ProducerFactory) manager.create("org.apache.cocoon.producer.ProducerFactory", 
            configurations.getConfigurations(PRODUCER_PROP));
        manager.setRole("producers", producers);

        // Create the processor factory and register it
        processors = (ProcessorFactory) manager.create("org.apache.cocoon.processor.ProcessorFactory", 
            configurations.getConfigurations(PROCESSOR_PROP));
        manager.setRole("processors", processors);

        // Create the formatter factory and register it
        formatters = (FormatterFactory) manager.create("org.apache.cocoon.formatter.FormatterFactory", 
            configurations.getConfigurations(FORMATTER_PROP));
        manager.setRole("formatters", formatters);

        // Create the browser table and register it
        browsers = (Browsers) manager.create("org.apache.cocoon.Browsers", 
            configurations.getConfigurations(BROWSERS_PROP));
    }

    /**
     * This method is called to start the processing when calling the engine
     * from the Cocoon servlet.
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // if verbose mode is on, take a time snapshot for later evaluation
        long time = 0;
        if (VERBOSE) time = System.currentTimeMillis();

        // this may be needed if debug is turned on
        ByteArrayOutputStream debugStream = null;

        // get the request flags
        boolean CACHE = getFlag(request, "cache", true);
        boolean DEBUG = getFlag(request, "debug", false);
        
        // get the request user agent
        String agent = request.getParameter("user-Agent");
        if (agent == null) agent = request.getHeader("user-Agent");

        // turn on debugging facilities redirecting the standard
        // streams to the output stream
        // WARNING: this is not thread-safe. Debugging a request
        // while another is being processed may result in mixed
        // debug output.
        if (DEBUG) {
            debugStream = new ByteArrayOutputStream();
            PrintStream stream = new PrintStream(new BufferedOutputStream(debugStream), true);
            System.setOut(stream);
            System.setErr(stream);
        }
        
        Page page = null;
        
        // ask if the cache contains the page requested and if it's
        // a valid instance (no changeable points have changed)
        if (CACHE) page = cache.getPage(request);

        // the page was not found in the cache or the cache was 
        // disabled, we need to process it
        if (page == null) {
        	
            // create the Page wrapper
            page = new Page();
            
            // get the document producer
            Producer producer = producers.getProducer(request);
            
            // set the producer as a page changeable point
            page.setChangeable(producer);
            
            // pass the produced stream to the parser
            Document document = producer.getDocument(request);

            // pass needed parameters to the processor pipeline
            Hashtable environment = new Hashtable();
            environment.put("path", producer.getPath(request));
            environment.put("browser", browsers.map(agent));
            environment.put("request", request);

            // process the document through the document processors
            while (true) {
                Processor processor = processors.getProcessor(document);
                if (processor == null) break;
                document = processor.process(document, environment);
                page.setChangeable(processor);
            }
            
            // get the right formatter for the page
            Formatter formatter = formatters.getFormatter(document);
            
            // FIXME: I know it sucks to encapsulate a nice stream into
            // a long String to push it into the cache. In the future,
            // we'll find a smarter way to do it.
            
            // format the page
            StringWriter writer = new StringWriter();
            formatter.format(document, writer, environment);
            
            // fill the page bean with content
            page.setContent(writer.toString());
            page.setContentType(formatter.getMIMEType());
        }

        if (DEBUG) {
            // send the debug message and restore the streams
            Frontend.print(response, "Debugging " + request.getRequestURI(), debugStream.toString());
            System.setOut(System.out);
            System.setErr(System.err);
        } else {
            // set the response content type
            response.setContentType(page.getContentType());
	        // get the output writer
        	PrintWriter out = response.getWriter();

			// send the page
            out.println(page.getContent());
            
	        // if verbose mode is on the the output type allows it
	        // print some processing info as a comment
	        if (VERBOSE && (page.isText())) {
	            time = System.currentTimeMillis() - time;
	            out.println("<!-- This page was served " 
	                + (page.isCached() ? "from cache " : "") 
	                + "in " + time + " milliseconds by " 
	                + Cocoon.version() + " -->");
	        }
	        
	        // send all content so that client doesn't wait while caching.
	        out.flush();
        }
        
        // cache the created page.
        cache.setPage(page, request);
    }
    
    /**
     * Returns the value of the request flag
     */
    private boolean getFlag(HttpServletRequest request, String name, boolean normal) {
        String flag = request.getParameter(name);
        return (flag != null) ? flag.toLowerCase().equals("true") : normal;
    }
    
    /**
     * Returns an hashtable of parameters used to report the internal status.
     */
    public Hashtable getStatus() {
        Hashtable table = new Hashtable();
        table.put("Browsers", ((Status) browsers).getStatus());
        Enumeration e = manager.getRoles();
        while (e.hasMoreElements()) {
            String role = (String)e.nextElement();
            Actor actor = manager.getActor(role);
            // Pretty print upper case first letter
            StringBuffer roleBuffer = new StringBuffer(role);
            if (roleBuffer.length() > 0) {
                roleBuffer.setCharAt(0,Character.toUpperCase(roleBuffer.charAt(0)));
            }
            String formattedRole = roleBuffer.toString();
            if (actor instanceof Status) {
                table.put(formattedRole, ((Status) actor).getStatus());
            } else {
                table.put(formattedRole, actor.getClass().getName());
            }
        }
        return table;
    }
}