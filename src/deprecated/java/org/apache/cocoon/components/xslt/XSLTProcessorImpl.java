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
package org.apache.cocoon.components.xslt;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.TraxErrorHandler;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.store.Store;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

/**
 * This class defines the implementation of the {@link XSLTProcessor}
 * component.
 *
 * To configure it, add the following lines in the
 * <file>cocoon.xconf</file> file:
 *
 * <pre>
 * &lt;xslt-processor class="org.apache.cocoon.components.xslt.XSLTProcessorImpl"&gt;
 *    &lt;parameter name="use-store" value="true"/&gt;
 *    &lt;parameter name="transformer-factory" value="org.apache.xalan.processor.TransformerFactoryImpl"/&gt;
 * &lt;/xslt-processor&gt;
 * </pre>
 *
 * The &lt;use-store&gt; configuration forces the transformer to put the
 * <code>Templates</code> generated from the XSLT stylesheet into the
 * <code>Store</code>. This property is true by default.
 * <p>
 * The &lt;transformer-factory&gt; configuration tells the transformer to use a particular
 * implementation of <code>javax.xml.transform.TransformerFactory</code>. This allows to force
 * the use of a given TRAX implementation (e.g. xalan or saxon) if several are available in the
 * classpath. If this property is not set, the transformer uses the standard TRAX mechanism
 * (<code>TransformerFactory.newInstance()</code>).
 *
 * @deprecated Use the avalon excalibur xslt processor instead.
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: XSLTProcessorImpl.java,v 1.3 2004/03/01 03:50:59 antonio Exp $
 * @version 1.0
 * @since   July 11, 2001
 */
public class XSLTProcessorImpl
  extends AbstractLogEnabled
  implements XSLTProcessor,
             Composable,
             Disposable,
             Parameterizable,
             URIResolver {

    protected ComponentManager manager;

    /** The store service instance */
    protected Store store;

    /** The trax TransformerFactory lookup table*/
    protected HashMap factories;

    /** The trax TransformerFactory this component uses */
    protected SAXTransformerFactory factory;

    /** Is the store turned on? (default is on) */
    protected boolean useStore = true;

    /** Is incremental processing turned on? (default for Xalan: no) */
    protected boolean incrementalProcessing = false;

    /** The source resolver used by this processor **/
    protected SourceResolver resolver;

    /** The error handler for the transformer */
    protected TraxErrorHandler errorHandler;

    /**
     * Compose. Try to get the store
     */
    public void compose(ComponentManager manager)
    throws ComponentException {
        this.manager = manager;
        if (this.getLogger().isDebugEnabled())
            this.getLogger().debug("XSLTProcessorImpl component initialized.");
        this.store = (Store) manager.lookup(Store.TRANSIENT_STORE);
        this.errorHandler = new TraxErrorHandler( this.getLogger() );
        this.resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }

    /**
     * Dispose
     */
    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.store);
            this.store = null;
            this.manager.release(this.resolver);
            this.resolver = null;
        }
        this.errorHandler = null;
        this.manager = null;
    }

    /**
     * Configure the component
     */
    public void parameterize(Parameters params)
    throws ParameterException {
        this.useStore = params.getParameterAsBoolean("use-store", true);
        this.incrementalProcessing = params.getParameterAsBoolean("incremental-processing", false);
        this.factory = getTransformerFactory(params.getParameter("transformer-factory", DEFAULT_FACTORY));
    }

    /**
     * Set the source resolver used by this component
     * @deprecated The processor can now simply lookup the source resolver.
     */
    public void setSourceResolver(org.apache.cocoon.environment.SourceResolver resolver) {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("XSLTProcessor: the setSourceResolver() method is deprecated.");
        }
    }

    /**
     * Set the transformer factory used by this component
     */
    public void setTransformerFactory(String classname) {
        this.factory = getTransformerFactory(classname);
    }

    public TransformerHandler getTransformerHandler(org.apache.cocoon.environment.Source stylesheet)
    throws ProcessingException {
        return getTransformerHandler(stylesheet, null);
    }

    public TransformerHandler getTransformerHandler(org.apache.cocoon.environment.Source stylesheet,
                                                    XMLFilter filter)
    throws ProcessingException {
        try {
            final String id = stylesheet.getSystemId();
            Templates templates = getTemplates(stylesheet, id);
            if (templates == null) {
                if (this.getLogger().isDebugEnabled()) {
                    getLogger().debug("Creating new Templates for " + id);
                }

                // Create a Templates ContentHandler to handle parsing of the
                // stylesheet.
                TemplatesHandler templatesHandler = this.factory.newTemplatesHandler();

                // Set the system ID for the template handler since some
                // TrAX implementations (XSLTC) rely on this in order to obtain
                // a meaningful identifier for the Templates instances.
                templatesHandler.setSystemId(id);

                if (filter != null) {
                    filter.setContentHandler(templatesHandler);
                }

                if (this.getLogger().isDebugEnabled()) {
                    getLogger().debug("Source = " + stylesheet
                    + ", templatesHandler = " + templatesHandler);
                }

                // Process the stylesheet.
                stylesheet.toSAX(filter != null ?
                            (ContentHandler)filter : (ContentHandler)templatesHandler);

                // Get the Templates object (generated during the parsing of
                // the stylesheet) from the TemplatesHandler.
                templates = templatesHandler.getTemplates();
                putTemplates (templates, stylesheet, id);
            } else {
                if (this.getLogger().isDebugEnabled()) {
                    getLogger().debug("Reusing Templates for " + id);
                }
            }

            TransformerHandler handler = this.factory.newTransformerHandler(templates);
            handler.getTransformer().setErrorListener(this.errorHandler);
            handler.getTransformer().setURIResolver(this);
            return handler;
        } catch (ProcessingException e) {
            throw e;
        } catch (SAXException e) {
            if (e.getException() == null) {
                throw new ProcessingException("Exception in creating Transform Handler", e);
            } else {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Got SAXException. Rethrowing cause exception.", e);
                throw new ProcessingException("Exception in creating Transform Handler", e.getException());
            }
        } catch (Exception e) {
            throw new ProcessingException("Exception in creating Transform Handler", e);
        }
    }

    public void transform(org.apache.cocoon.environment.Source source,
                          org.apache.cocoon.environment.Source stylesheet,
                          Parameters params,
                          Result result)
    throws ProcessingException {
        try {
            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("XSLTProcessorImpl: transform source = " + source
                    + ", stylesheet = " + stylesheet
                    + ", parameters = " + params
                    + ", result = " + result);
            }
            TransformerHandler handler = getTransformerHandler(stylesheet);

            Transformer transformer = handler.getTransformer();
            if (params != null) {
                transformer.clearParameters();
                String[] names = params.getNames();
                for (int i = names.length -1 ; i >= 0; i--) {
                    transformer.setParameter(names[i], params.getParameter(names[i]));
                }
            }

            if (getLogger().isDebugEnabled())
                this.getLogger().debug("XSLTProcessorImpl: starting transform");
            // Is it possible to use Source's toSAX method?
            handler.setResult(result);
            source.toSAX(handler);

            if (getLogger().isDebugEnabled())
                this.getLogger().debug("XSLTProcessorImpl: transform done");
        } catch (Exception e) {
            throw new ProcessingException("Error in running Transformation", e);
        }
    }

    /**
     * Get the TransformerFactory associated with the given classname. If
     * the class can't be found or the given class doesn't implement
     * the required interface, the default factory is returned.
     */
    private SAXTransformerFactory getTransformerFactory(String factoryName) {
        SAXTransformerFactory _factory;

        if ((factoryName == null) || (factoryName == XSLTProcessor.DEFAULT_FACTORY)) {
            _factory = (SAXTransformerFactory) TransformerFactory.newInstance();
        } else {
            try {
                _factory = (SAXTransformerFactory) ClassUtils.loadClass(factoryName).newInstance();
            } catch (ClassNotFoundException cnfe) {
                if (getLogger().isErrorEnabled())
                    getLogger().error("Cannot find the requested TrAX factory '" + factoryName
                                      + "'. Using default TrAX Transformer Factory instead.");
                if (this.factory != null) return this.factory;
                _factory = (SAXTransformerFactory) TransformerFactory.newInstance();
            } catch (ClassCastException cce) {
                if (getLogger().isErrorEnabled())
                    getLogger().error("The indicated class '" + factoryName
                                      + "' is not a TrAX Transformer Factory. Using default TrAX Transformer Factory instead.");
                if (this.factory != null) return this.factory;
                _factory = (SAXTransformerFactory) TransformerFactory.newInstance();
            } catch (Exception e) {
                if (getLogger().isErrorEnabled())
                    getLogger().error("Error found loading the requested TrAX Transformer Factory '"
                                      + factoryName + "'. Using default TrAX Transformer Factory instead.");
                if (this.factory != null) return this.factory;
                _factory = (SAXTransformerFactory) TransformerFactory.newInstance();
            }
        }

        _factory.setErrorListener(this.errorHandler);
        _factory.setURIResolver(this);

        // implementation-specific parameter passing should be
        // made more extensible.
        if (_factory.getClass().getName().equals("org.apache.xalan.processor.TransformerFactoryImpl")) {
            _factory.setAttribute("http://xml.apache.org/xalan/features/incremental",
                    new Boolean (incrementalProcessing));
        }

        return _factory;
    }

    private Templates getTemplates(org.apache.cocoon.environment.Source stylesheet,
                                   String id)
    throws IOException, ProcessingException {
        if (!useStore) {
            return null;
        }

        // we must augment the template ID with the factory classname since one
        // transformer implementation cannot handle the instances of a
        // template created by another one.
        id += factory.getClass().getName();

        Templates templates = null;
        // only stylesheets with a last modification date are stored
        if (stylesheet.getLastModified() != 0) {
            // Stored is an array of the template and the caching time
            if (store.containsKey(id)) {
                Object[] templateAndTime = (Object[])store.get(id);

                if(templateAndTime != null && templateAndTime[1] != null) {
                    long storedTime = ((Long)templateAndTime[1]).longValue();

                    if (storedTime < stylesheet.getLastModified()) {
                        store.remove(id);
                    } else {
                        templates = (Templates)templateAndTime[0];
                    }
                }
            }
        } else if (store.containsKey(id)) {
            // remove an old template if it exists
            store.remove(id);
        }
        return templates;
    }

    private void putTemplates (Templates templates, org.apache.cocoon.environment.Source stylesheet,
                               String id)
    throws IOException, ProcessingException {
        if (!useStore) {
            return;
        }

        // we must augment the template ID with the factory classname since one
        // transformer implementation cannot handle the instances of a
        // template created by another one.
        id += factory.getClass().getName();

        // only stylesheets with a last modification date are stored
        if (stylesheet.getLastModified() != 0) {

            // Stored is an array of the template and the current time
            Object[] templateAndTime = new Object[2];
            templateAndTime[0] = templates;
            templateAndTime[1] = new Long(stylesheet.getLastModified());
            store.store(id, templateAndTime);
        }
    }

    /**
     * Called by the processor when it encounters
     * an xsl:include, xsl:import, or document() function.
     *
     * @param href An href attribute, which may be relative or absolute.
     * @param base The base URI in effect when the href attribute
     * was encountered.
     *
     * @return A Source object, or null if the href cannot be resolved,
     * and the processor should try to resolve the URI itself.
     *
     * @throws TransformerException if an error occurs when trying to
     * resolve the URI.
     */
    public javax.xml.transform.Source resolve(String href, String base)
    throws TransformerException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("resolve(href = " + href +
                                   ", base = " + base + "); resolver = " + resolver);
        }

        Source xslSource = null;
        try {
            if (href.indexOf(":") > 1) {
                xslSource = resolver.resolveURI(href);
            } else {
                // patch for a null pointer passed as base
                if (base == null)
                    throw new IllegalArgumentException("Null pointer passed as base");

                // is the base a file or a real url
                if (!base.startsWith("file:")) {
                    int lastPathElementPos = base.lastIndexOf('/');
                    if (lastPathElementPos == -1) {
                        // this should never occur as the base should
                        // always be protocol:/....
                        return null; // we can't resolve this
                    } else {
                        xslSource = resolver.resolveURI(new StringBuffer(base.substring(0, lastPathElementPos))
                        .append("/").append(href).toString());
                    }
                } else {
                    File parent = new File(base.substring(5));
                    File parent2 = new File(parent.getParentFile(), href);
                    xslSource = resolver.resolveURI(parent2.toURL().toExternalForm());
                }
            }

            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("xslSource = " + xslSource
                + ", system id = " + xslSource.getURI());
            }

            return new StreamSource(xslSource.getInputStream(), xslSource.getURI());

        } catch (java.net.MalformedURLException mue) {
            return null;
        } catch (SourceException pe) {
            throw new TransformerException(pe);
        } catch (IOException ioe) {
            return null;
        } finally {
            this.resolver.release( xslSource );
        }
    }
}
