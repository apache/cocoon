/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.components.ComponentContext;
import org.apache.cocoon.components.language.generator.CompiledComponent;
import org.apache.cocoon.components.language.generator.ProgramGenerator;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.impl.URLSource;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * The Cocoon Object is the main Kernel for the entire Cocoon system.
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a> (Apache Software Foundation)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:leo.sutic@inspireinfrastructure.com">Leo Sutic</a>
 * @version CVS $Id: Cocoon.java,v 1.19 2003/10/17 17:49:24 bloritsch Exp $
 */
public class Cocoon
        extends AbstractLogEnabled
        implements Serviceable,
                   Initializable,
                   Disposable,
                   Modifiable,
                   Processor,
                   Contextualizable {

    /** The application context */
    private Context context;

    /** The configuration file */
    private Source configurationFile;

    /** The parent component manager. */
    private ServiceManager serviceManager;

    /** flag for disposed or not */
    private boolean disposed = false;

    /** active request count */
    private volatile int activeRequestCount = 0;

    private static final String lineSeparator = System.getProperty("line.separator");

    /** The source resolver */
    protected SourceResolver sourceResolver;

    /**
     * Creates a new <code>Cocoon</code> instance.
     *
     * @exception ConfigurationException if an error occurs
     */
    public Cocoon() throws ConfigurationException {
        // Set the system properties needed by Xalan2.
        setSystemProperties();
    }

    /**
     * Get the parent service manager. For purposes of
     * avoiding extra method calls, the manager parameter may be null.
     *
     * @param manager the parent component manager. May be <code>null</code>
     */
    public void service(ServiceManager manager) {
        this.serviceManager = manager;
    }

    /**
     * Describe <code>contextualize</code> method here.
     *
     * @param context a <code>Context</code> value
     * @exception ContextException if an error occurs
     */
    public void contextualize(Context context) throws ContextException {
        if (this.context == null) {
            this.context = new ComponentContext(context);

            //this.classpath = (String)context.get(Constants.CONTEXT_CLASSPATH);
            //this.workDir = (File)context.get(Constants.CONTEXT_WORK_DIR);
            try {
                // FIXME : add a configuration option for the refresh delay.
                // for now, hard-coded to 1 second.
                URLSource urlSource = new URLSource();
                urlSource.init((URL)context.get(Constants.CONTEXT_CONFIG_URL), null);
                this.configurationFile = new DelayedRefreshSourceWrapper(urlSource,
                    1000L
                );

            } catch (IOException ioe) {
                throw new ContextException("Could not open configuration file.", ioe);
            } catch (Exception e) {
                throw new ContextException("contextualize(..) Exception", e);
            }
        }
    }

    /**
     * The <code>initialize</code> method
     *
     * @exception Exception if an error occurs
     */
    public void initialize() throws Exception {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("New Cocoon object.");
        }

        // Log the System Properties.
        dumpSystemProperties();
    }

    /** Dump System Properties */
    private void dumpSystemProperties() {
        if (getLogger().isDebugEnabled()) {
            try {
                Enumeration e = System.getProperties().propertyNames();
                getLogger().debug("===== System Properties Start =====");
                for (; e.hasMoreElements();) {
                    String key = (String) e.nextElement();
                    getLogger().debug(key + "=" + System.getProperty(key));
                }
                getLogger().debug("===== System Properties End =====");
            } catch (SecurityException se) {
                // Ignore Exceptions.
            }
        }
    }

    /**
     * Queries the class to estimate its ergodic period termination.
     *
     * @param date a <code>long</code> value
     * @return a <code>boolean</code> value
     */
    public boolean modifiedSince(long date) {
        return date < this.configurationFile.getLastModified();
    }

    /**
     * Sets required system properties.
     */
    protected void setSystemProperties() {
        java.util.Properties props = new java.util.Properties();
        // FIXME We shouldn't have to specify the SAXParser...
        // This is needed by Xalan2, it is used by org.xml.sax.helpers.XMLReaderFactory
        // to locate the SAX2 driver.
        props.put("org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser");
        java.util.Properties systemProps = System.getProperties();
        Enumeration propEnum = props.propertyNames();
        while (propEnum.hasMoreElements()) {
            String prop = (String)propEnum.nextElement();
            if (!systemProps.containsKey(prop)) {
                systemProps.put(prop, props.getProperty(prop));
            }
        }
        // FIXME We shouldn't have to specify these. Needed to override jaxp implementation of weblogic.
        if (systemProps.containsKey("javax.xml.parsers.DocumentBuilderFactory") &&
            systemProps.getProperty("javax.xml.parsers.DocumentBuilderFactory").startsWith("weblogic")) {
            systemProps.put("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
            systemProps.put("javax.xml.parsers.SAXParserFactory","org.apache.xerces.jaxp.SAXParserFactoryImpl");
        }
        System.setProperties(systemProps);
    }

    /**
     * Dispose this instance
     */
    public void dispose() {
        this.context = null;
        this.disposed = true;
    }

    /**
     * Log debug information about the current environment.
     *
     * @param environment an <code>Environment</code> value
     */
    protected void debug(Environment environment, boolean internal) {
        if ( ! getLogger().isDebugEnabled() ) return;

        Map objectModel = environment.getObjectModel();
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession(false);
        StringBuffer msg = new StringBuffer();
        msg.append("DEBUGGING INFORMATION:").append(lineSeparator);
        if (internal) {
            msg.append("INTERNAL ");
        }
        msg.append("REQUEST: ").append(request.getRequestURI()).append(lineSeparator).append(lineSeparator);
        msg.append("CONTEXT PATH: ").append(request.getContextPath()).append(lineSeparator);
        msg.append("SERVLET PATH: ").append(request.getServletPath()).append(lineSeparator);
        msg.append("PATH INFO: ").append(request.getPathInfo()).append(lineSeparator).append(lineSeparator);

        msg.append("REMOTE HOST: ").append(request.getRemoteHost()).append(lineSeparator);
        msg.append("REMOTE ADDRESS: ").append(request.getRemoteAddr()).append(lineSeparator);
        msg.append("REMOTE USER: ").append(request.getRemoteUser()).append(lineSeparator);
        msg.append("REQUEST SESSION ID: ").append(request.getRequestedSessionId()).append(lineSeparator);
        msg.append("REQUEST PREFERRED LOCALE: ").append(request.getLocale().toString()).append(lineSeparator);
        msg.append("SERVER HOST: ").append(request.getServerName()).append(lineSeparator);
        msg.append("SERVER PORT: ").append(request.getServerPort()).append(lineSeparator).append(lineSeparator);

        msg.append("METHOD: ").append(request.getMethod()).append(lineSeparator);
        msg.append("CONTENT LENGTH: ").append(request.getContentLength()).append(lineSeparator);
        msg.append("PROTOCOL: ").append(request.getProtocol()).append(lineSeparator);
        msg.append("SCHEME: ").append(request.getScheme()).append(lineSeparator);
        msg.append("AUTH TYPE: ").append(request.getAuthType()).append(lineSeparator).append(lineSeparator);
        msg.append("CURRENT ACTIVE REQUESTS: ").append(activeRequestCount).append(lineSeparator);

        // log all of the request parameters
        Enumeration e = request.getParameterNames();

        msg.append("REQUEST PARAMETERS:").append(lineSeparator).append(lineSeparator);

        while (e.hasMoreElements()) {
            String p = (String) e.nextElement();

            msg.append("PARAM: '").append(p).append("' ")
               .append("VALUES: '");
            String[] params = request.getParameterValues(p);
            for (int i = 0; i < params.length; i++) {
                msg.append("[" + params[i] + "]");
                if (i != (params.length - 1)) {
                    msg.append(", ");
                }
            }

            msg.append("'").append(lineSeparator);
        }

        // log all of the header parameters
        Enumeration e2 = request.getHeaderNames();

        msg.append("HEADER PARAMETERS:").append(lineSeparator).append(lineSeparator);

        while (e2.hasMoreElements()) {
            String p = (String) e2.nextElement();

            msg.append("PARAM: '").append(p).append("' ")
               .append("VALUES: '");
            Enumeration e3 = request.getHeaders(p);
            while (e3.hasMoreElements()) {
                msg.append("[" + e3.nextElement() + "]");
                if (e3.hasMoreElements()) {
                    msg.append(", ");
                }
            }

            msg.append("'").append(lineSeparator);
        }

        msg.append(lineSeparator).append("SESSION ATTRIBUTES:").append(lineSeparator).append(lineSeparator);

        // log all of the session attributes
        if (session != null) {
            // Fix bug #12139: Session can be modified while still
            // being enumerated here
            synchronized (session) {
                e = session.getAttributeNames();
                while (e.hasMoreElements()) {
                    String p = (String) e.nextElement();
                    msg.append("PARAM: '").append(p).append("' ")
                       .append("VALUE: '").append(session.getAttribute(p)).append("'")
                       .append(lineSeparator);
                }
            }
        }

        getLogger().debug(msg.toString());
    }

    /**
     * Process the given <code>Environment</code> to produce the output.
     *
     * @param environment an <code>Environment</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean process(Environment environment)
    throws Exception {
        if (this.disposed) {
            throw new IllegalStateException("You cannot process a Disposed Cocoon engine.");
        }

        Object key = CocoonComponentManager.startProcessing(environment);
        CocoonComponentManager.enterEnvironment(environment,
                                                this.serviceManager,
                                                this);
        try {
            boolean result;
            if (this.getLogger().isDebugEnabled()) {
                ++activeRequestCount;
                this.debug(environment, false);
            }

            Processor processor = null;
            try {
                processor = (Processor) this.serviceManager.lookup( Processor.ROLE );
                result = processor.process(environment);
            }
            finally {
                this.serviceManager.release(processor);
            }
            // commit response on success
            environment.commitResponse();

            return result;
        } catch (Exception any) {
            // reset response on error
            environment.tryResetResponse();
            throw any;
        } finally {
            CocoonComponentManager.leaveEnvironment();
            CocoonComponentManager.endProcessing(environment, key);
            if (this.getLogger().isDebugEnabled()) {
                --activeRequestCount;
            }

            // TODO (CZ): This is only for testing - remove it later on
            CocoonComponentManager.checkEnvironment(this.getLogger());
        }
    }

    /**
     * Process the given <code>Environment</code> to assemble
     * a <code>ProcessingPipeline</code>.
     * @since 2.1
     */
    public ProcessingPipeline buildPipeline(Environment environment)
    throws Exception {
        if (disposed) {
            throw new IllegalStateException("You cannot process a Disposed Cocoon engine.");
        }

        try {
            if (this.getLogger().isDebugEnabled()) {
                ++activeRequestCount;
                this.debug(environment, true);
            }

            Processor processor = null;
            try {
                processor = (Processor) this.serviceManager.lookup( Processor.ROLE );
                return processor.buildPipeline(environment);
            }
            finally {
                this.serviceManager.release(processor);
            }

        } finally {
            if (this.getLogger().isDebugEnabled()) {
                --activeRequestCount;
            }
        }
    }

    /**
     * Get the sitemap component configurations
     * @since 2.1
     */
    public Map getComponentConfigurations() {
        return Collections.EMPTY_MAP;
    }

    /**
     * Return this (Cocoon is always at the root of the processing chain).
     * @since 2.1.1
     */
    public Processor getRootProcessor() {
        return this;
    }

    /**
     * Process the given <code>Environment</code> to generate Java code for specified XSP files.
     *
     * @param fileName a <code>String</code> value
     * @param environment an <code>Environment</code> value
     * @exception Exception if an error occurs
     */
    public void precompile(String fileName,
                           Environment environment,
                           String markupLanguage,
                           String programmingLanguage)
    throws Exception {
        ProgramGenerator programGenerator = null;
        Source source = null;
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("XSP generation begin:" + fileName);
            }

            programGenerator = (ProgramGenerator) this.serviceManager.lookup(ProgramGenerator.ROLE);
            source = environment.resolveURI(fileName);
            CompiledComponent xsp = programGenerator.load(this.serviceManager,
                    source,
                    markupLanguage, programmingLanguage, environment);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("XSP generation complete:" + xsp);
            }
        } finally {
            environment.release(source);
            this.serviceManager.release(programGenerator);
        }
    }

    /**
     * Accessor for active request count
     */
    public int getActiveRequestCount() {
        return activeRequestCount;
    }

    public ServiceManager getServiceManager()
    {
        return serviceManager;
    }
}

