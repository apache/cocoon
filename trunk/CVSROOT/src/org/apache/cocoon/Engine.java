/*-- $Id: Engine.java,v 1.39 2000-11-20 18:36:00 greenrd Exp $ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

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
import org.apache.cocoon.logger.*;
import org.apache.cocoon.transformer.*;
import org.apache.cocoon.producer.*;
import org.apache.cocoon.formatter.*;
import org.apache.cocoon.processor.*;
import org.apache.cocoon.framework.*;
import org.apache.cocoon.interpreter.*;
import org.apache.cocoon.response.HttpServletResponseFacade;
import org.apache.cocoon.response.RedirectException;

/**
 * The Cocoon publishing engine.
 *
 * This class implements the engine that does all the document processing.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:greenrd@hotmail.com">Robin Green</a>
 * @version $Revision: 1.39 $ $Date: 2000-11-20 18:36:00 $
 */

public class Engine implements Defaults {

    private Block blocker = new Block();
    private boolean VERBOSE, PROFILE, LASTMODIFIED;

    private static Hashtable engineInstances = new Hashtable(2, 0.90f);

    Configurations configurations;

    boolean showStatus;

    ProducerFactory producers;
    ProcessorFactory processors;
    FormatterFactory formatters;
    InterpreterFactory interpreters;

    Manager manager;
    Browsers browsers;
    Parser parser;
    Transformer transformer;
    Cache cache;
    Store store;
    Logger logger;
    Profiler profiler;

    ServletContext servletContext;

    /**
     * This method initializes the engine. It is private because Engine
     * implements the Singleton pattern and you cannot construct it directly.
     */
    private Engine(Configurations configurations, Object context) throws Exception {

        // Create the object manager which is both Factory and Director
        // and register it
        manager = new Manager();
        manager.setRole("factory", manager);

        // stores the configuration instance
        this.configurations = configurations;

        // stores the engine context
        if (context instanceof ServletContext) {
            // register the context
            this.servletContext = (ServletContext) context;
            manager.setRole("context", context);

            // use the context for the logger
            logger = new ServletLogger(this.servletContext, (String) configurations.get(LOG_LEVEL));
        } else {
            // use the STDIO logger if no context is available
            logger = new StdioLogger((String) configurations.get(LOG_LEVEL));
        }

        // Register the logger
        manager.setRole("logger", logger);

        // Create the parser and register it
        parser = (Parser) manager.create(getConfigTr(PARSER_PROP,
            PARSER_DEFAULT), configurations.getConfigurations(PARSER_PROP));
        manager.setRole("parser", parser);

        // Create the transformer and register it
        transformer = (Transformer) manager.create(getConfigTr(TRANSFORMER_PROP,
            TRANSFORMER_DEFAULT), configurations.getConfigurations(TRANSFORMER_PROP));
        manager.setRole("transformer", transformer);

        // Create the store and register it
        store = (Store) manager.create(getConfigTr(STORE_PROP,
            STORE_DEFAULT), configurations.getConfigurations(STORE_PROP));
        manager.setRole("store", store);

        // Create the cache and register it
        cache = (Cache) manager.create(getConfigTr(CACHE_PROP,
            CACHE_DEFAULT), configurations.getConfigurations(CACHE_PROP));
        manager.setRole("cache", cache);

        // Create the interpreter factory and register it
        interpreters = (InterpreterFactory) manager.create(
            "org.apache.cocoon.interpreter.InterpreterFactory",
            configurations.getConfigurations(INTERPRETER_PROP));
        manager.setRole("interpreters", interpreters);

        // Create the producer factory and register it
        producers = (ProducerFactory) manager.create(
            "org.apache.cocoon.producer.ProducerFactory",
            configurations.getConfigurations(PRODUCER_PROP));
        manager.setRole("producers", producers);

        // Create the processor factory and register it
        processors = (ProcessorFactory) manager.create(
            "org.apache.cocoon.processor.ProcessorFactory",
            configurations.getConfigurations(PROCESSOR_PROP));
        manager.setRole("processors", processors);

        // Create the formatter factory and register it
        formatters = (FormatterFactory) manager.create(
            "org.apache.cocoon.formatter.FormatterFactory",
            configurations.getConfigurations(FORMATTER_PROP));
        manager.setRole("formatters", formatters);

        // Create the browser table and register it
        browsers = (Browsers) manager.create(
            "org.apache.cocoon.Browsers",
            configurations.getConfigurations(BROWSERS_PROP));
        manager.setRole("browsers", browsers);

        VERBOSE = configurations.get ("verbosity", "false").equals ("true");
        LASTMODIFIED = configurations.get ("lastmodified", "true").equals ("true");

        // If enabled, create the profiler and register it
        PROFILE = configurations.get ("profiler.enabled", "false").equals ("true");
        if (PROFILE) {
          profiler = (Profiler) manager.create(getConfigTr(PROFILER_PROP,
              PROFILER_DEFAULT), configurations.getConfigurations(PROFILER_PROP));
          manager.setRole("profiler", profiler);
        }
    }

    /** Gets a configuration string with spaces trimmed from each end. */
    private String getConfigTr (String name, String defaultStr) {
      return ((String) configurations.get (name, defaultStr)).trim ();
    }

    /**
     * This will return a new instance of the Engine class, and handle
     *   pooling of instances.  In this implementation, one instance is
     *   created per servlet context.  This replaces using the constructor
     *   directly, because now the Cocoon servlet can initialize the
     *   Engine, and other servlets and classes can use the same engine,
     *   in order to funnel requests through Cocoon.
     *
     * @param confs - Configuration file information
     * @param context - Object to use for Servlet Context
     * @return Engine - instance to operate on
     * @throws Exception - when things go awry
     */
    public static Engine getInstance(Configurations confs, Object context) throws Exception {

        Engine engine = (Engine) engineInstances.get(context);

        if (engine == null) {
           synchronized (Engine.class) {
              engine = new Engine(confs, context);
              engineInstances.put(context, engine);
           }
        }

        return engine;
   }

    /**
     * This is the <code>getInstance()</code> version that should be used by
     *   anything other than the Cocoon servlet itself.  This assumes that
     *   the engine has been set up and is ready to be used.  If this is called
     *   before the instance has been correctly created, it throws an
     *   exception.
     *
     * @return Engine - instance to operate on
     */
    public static Engine getInstance() throws Exception {
        if (!engineInstances.isEmpty()) {
            // return the first engine instance found
            return (Engine) engineInstances.elements().nextElement();
        }

        throw new Exception("The Cocoon engine has not been initialized!");
    }

    /**
     * This method is called to start the processing when calling the engine
     * from the Cocoon servlet.
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // get the request flags
        boolean CACHE = getFlag(request, "cache", true);
        boolean DEBUG = getFlag(request, "debug", false);
        boolean VERBOSE = getFlag(request, "verbose", this.VERBOSE);
        boolean PROFILE = getFlag(request, "profile", this.PROFILE);

        Profiler.RequestMarker requestMarker = null;
        if (PROFILE) {
            // We cannot guarantee that the request object will not be recycled by the
            // servlet runner, so use requestMarker instead.
            requestMarker = new Profiler.RequestMarker (request);
            profiler.startEvent (requestMarker, WHOLE_REQUEST);
        }

        if (LOG) logger.log(this, "Starting request", Logger.INFO);

        // if verbose mode is on, take a time snapshot for later evaluation
        long time = 0;
        if (VERBOSE) time = System.currentTimeMillis();

        // this may be needed if debug is turned on
        ByteArrayOutputStream debugStream = null;

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

        response = new HttpServletResponseFacade (response);

        Page page = null;

        boolean lock = false;

        String encodedRequest = Utils.encode( request );

        try {
            if ( CACHE ) {
                // ask if the cache contains the page requested and if it's
                // a valid instance (no changeable points have changed)
                page = cache.getPage(request);

                // if the page isn't in cache block any further access to this page
                // until it get's put into cache
                if ( page == null ) {

                    // lock this page while we build it and put it in cache
                    lock = this.blocker.lock( encodedRequest );

                    if ( ! lock ) {
                        // we were blocked so by another thread processing this page
                        // so maybe it's in cache now
                        page = cache.getPage(request);
                    }
                }
            }

            boolean wasInCache = (page != null);

            if (!wasInCache) {

                // the page was not found in the cache or the cache was
                // disabled, we need to process it

                if (LOG) logger.log(this, "Creating page", Logger.DEBUG);

                // continue until the page is done.
                for (int i = 0; i < LOOPS; i++) {
                    // catch if any OutOfMemoryError is thrown
                    try {
                        // create the Page wrapper
                        page = new Page();

                        // get the document producer
                        Producer producer = producers.getProducer(request);

                        // set the producer as a page changeable point
                        page.setChangeable(producer);

                        // pass the request object to the producer and produce the initial
                        // Document
                        if (PROFILE) profiler.startEvent (requestMarker, producer.getClass ());
                        Document document = producer.getDocument(request);
                        if (PROFILE) profiler.finishEvent (requestMarker, producer.getClass ());

                        if (LOG) logger.log(this, "Document produced", Logger.DEBUG);

                        // pass needed parameters to the processor pipeline
                        Hashtable environment = new Hashtable();
                        environment.put("path", producer.getPath(request));
                        environment.put("browser", browsers.map(agent));
                        environment.put("request", request);
                        environment.put("response", response);
                        if (servletContext != null) environment.put("context", servletContext);

                        // process the document through the document processors
                        for (int processNum = 0; true; processNum++) {
                            Processor processor = processors.getProcessor(document);
                            if (processor == null) break;
                            String processDesc = processor.getClass ().getName () + "-" + processNum;
                            if (PROFILE) profiler.startEvent (requestMarker, processDesc);
                            document = processor.process(document, environment);
                            page.setChangeable(processor);
                            if (PROFILE) profiler.finishEvent (requestMarker, processDesc);
                            if (LOG) logger.log(this, "Document processed", Logger.DEBUG);
                        }

                        // get the right formatter for the page
                        Formatter formatter = formatters.getFormatter(document);

                        // FIXME: I know it sucks to encapsulate a nice stream into
                        // a long String to push it into the cache. In the future,
                        // we'll find a smarter way to do it.

                        // format the page
                        if (PROFILE) profiler.startEvent (requestMarker, formatter.getClass ());
                        StringWriter writer = new StringWriter();
                        formatter.format(document, writer, environment);
                        if (PROFILE) profiler.finishEvent (requestMarker, formatter.getClass ());

                        if (LOG) logger.log(this, "Document formatted", Logger.DEBUG);

                        // fill the page bean with content
                        if (PROFILE) profiler.startEvent (requestMarker, OUTPUTTING);
                        page.setContent(writer.toString());

                        // set content type together with encoding if appropriate
                        String encoding = formatter.getEncoding();
                        if (encoding != null) {
                            page.setContentType(formatter.getMIMEType() + "; charset=" + encoding);
                        } else {
                            page.setContentType(formatter.getMIMEType());
                        }

                        // cache the created page, if it is cacheable
                        cache.setPage(page, request);

                        // page is done without memory errors so exit the loop
                        break;
                    } catch (OutOfMemoryError e) {
                        if (LOG) logger.log(this, "Triggered OutOfMemory", Logger.WARNING);
                        // force the cache to free some of its content.
                        cache.flush();
                        // reset the page to signal the error
                        page = null;
                    }
                }
            }

            if (page == null) {
                if (LOG) logger.log(this, "System is out of memory", Logger.EMERGENCY);
                throw new Exception("FATAL ERROR: the system ran out of memory when"
                    + " processing the request. Increase your JVM memory.");
            }

            if (DEBUG) {
                // send the debug message and restore the streams
                Frontend.print(response, "Debugging " + request.getRequestURI(), debugStream.toString());
                System.setOut(System.out);
                System.setErr(System.err);
            } else {
                // set the response content type
                response.setContentType(page.getContentType());

                // set the Last-Modified header if this option is enabled in cocoon.properties
                // and the request has been cached
                if (LASTMODIFIED) {
                   long lastMod = cache.getLastModified (request);
                   if (lastMod > -1) {
                      response.setDateHeader ("Last-Modified", lastMod);
                   }
                }

                // get the output writer
                PrintWriter out = response.getWriter();

                // send the page
                out.println(page.getContent());

                // if verbose mode is on the the output type allows it
                // and the HTTP request isn't a HEAD
                // print some processing info as a comment
                if (VERBOSE && (page.isText()) && !"HEAD".equals(request.getMethod())) {
                    time = System.currentTimeMillis() - time;
                    out.println("<!-- This page was served "
                        + (wasInCache ? "from cache " : "")
                        + "in " + time + " milliseconds by "
                        + Cocoon.version() + " -->");
                    //out.println("<!-- free memory: " + Runtime.getRuntime().freeMemory() + " -->");
                }

                // send all content so that client doesn't wait while caching.
                out.flush();
                if (PROFILE) profiler.finishEvent (requestMarker, OUTPUTTING);
            }

            if (LOG) logger.log(this, "response sent to client", Logger.WARNING);
            if (PROFILE) profiler.finishEvent (requestMarker, WHOLE_REQUEST);

        } catch (RedirectException ex) {
            // Do nothing - this is used to immediately stop Cocoon processing
            // to fix a redirect bug on some servlet engines, e.g. Tomcat.
        } finally {
            // if there is a lock make sure it is released,
            // otherwise this page could never be served
            if ( lock ) {
                this.blocker.unlock( encodedRequest );
            }
        }

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
            Object actor = manager.getActor(role);
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

    public void destroy () {
      manager.destroyAll ();
    }

    /**
     * LAF: August 2, 2000
     *
     * Problem: For large pages that require huge amounts of time and CPU usage
     * if the page is not in cache and multiple requests are made for that page
     * all requests will attempt to build the page thereby puting undue stress on
     * the system.
     *
     * Solution: If caching is on block requests to build a page if another request
     * is currently building that same page, i.e. subsequent requests wait until the
     * page gets put into cache.  Also, if the first attempt to build the page fails
     * to put the page in cache, allow the next queued request to build the page.
     * See PersistantCache and PersistantStore (not currently included in Cocoon)
     * for further enhancments to large pages.
     */
    private class Block
    {
        // holds the locked objects
        private Hashtable blocks = new Hashtable();



        /**
         * Checks if a key is being blocked, and if so blocks this request until notified.
         * If the key is not locked then it becomes locked, so any subsequent calls will block.
         *
         * @param key - The object to be locked.
         * @return boolean - true = a lock was obtained, false = the caller was blocked.
         */
        public synchronized boolean lock( Object key )
        {
            boolean locked = false;

            if ( ! this.blocks.containsKey(key) ) {
                // flag the key as locked
                if (LOG) logger.log(this, "Locking: " + key, Logger.INFO);
                this.blocks.put( key, key );
                locked = true;

            } else {
                // block the call
                if (LOG) logger.log(this, "Blocking: " + key, Logger.INFO);

                try {
                    // wait until the block is released by the blocking thread
                    while( this.blocks.containsKey(key) ) {
                        wait();
                    }
                } catch( InterruptedException ie ) {
                    if (LOG) logger.log(this,
                      "Wait for '" + key + "' was interrupted", Logger.WARNING);
                }
            }

            if (LOG) logger.log(this,
                      "Total locks for all pages: " + this.blocks.size(), Logger.INFO);
            return locked;
        } // lock(Object)


        /**
         * All waiting threads are notified.
         * @param key - The object whose lock is to be removed.
         */
        public synchronized void unlock( Object key )
        {
            // notify all waiting threads if there was a lock on this key
            if ( this.blocks.remove(key) != null ) {
                if (LOG) logger.log(this, "Releasing lock on: " + key, Logger.INFO);
                notifyAll();
            }
        } // unlock(Object)

    } // inner class Block

} // class Engine
