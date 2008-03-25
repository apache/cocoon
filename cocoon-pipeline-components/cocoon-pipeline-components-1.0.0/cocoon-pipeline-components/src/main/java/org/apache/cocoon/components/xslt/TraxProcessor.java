/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.xslt;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AggregatedValidity;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.excalibur.xml.xslt.XSLTProcessor;
import org.apache.excalibur.xml.xslt.XSLTProcessorException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.util.SourceUtil;
import org.apache.cocoon.util.AbstractLogEnabled;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

/**
 * Adaptation of Excalibur's XSLTProcessor implementation to allow for better
 * error reporting.
 *
 * @version $Id$
 * @since 2.1.8
 */
public class TraxProcessor extends AbstractLogEnabled
                           implements XSLTProcessor, Serviceable, Initializable,
                                      Disposable, Parameterizable, Recyclable, URIResolver {

    /** The store service instance */
    protected Store m_store;

    /** The configured transformer factory to use */
    protected String m_transformerFactory;

    /** The trax TransformerFactory this component uses */
    protected SAXTransformerFactory m_factory;

    /** The default TransformerFactory used by this component */
    protected SAXTransformerFactory m_defaultFactory;

    /** Is the store turned on? (default is off) */
    protected boolean m_useStore;

    /** Is incremental processing turned on? (default for Xalan: no) */
    protected boolean m_incrementalProcessing;

    /** Resolver used to resolve XSLT document() calls, imports and includes */
    protected SourceResolver m_resolver;

    /** Check included stylesheets */
    protected boolean m_checkIncludes;

    /** Map of pairs of System ID's / validities of the included stylesheets */
    protected Map m_includesMap = new HashMap();

    protected SAXParser saxParser;

    /** The ServiceManager */
    protected ServiceManager m_manager;

    /**
     * Compose. Try to get the store
     *
     * @avalon.service interface="XMLizer"
     * @avalon.service interface="SourceResolver"
     * @avalon.service interface="Store/TransientStore" optional="true"
     */
    public void service(final ServiceManager manager) throws ServiceException {
        m_manager = manager;
        saxParser = (SAXParser) m_manager.lookup(SAXParser.ROLE);
        m_resolver = (SourceResolver) m_manager.lookup(SourceResolver.ROLE);

        if (m_manager.hasService(Store.TRANSIENT_STORE)) {
            m_store = (Store) m_manager.lookup(Store.TRANSIENT_STORE);
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        m_factory = getTransformerFactory(m_transformerFactory);
        m_defaultFactory = m_factory;
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (null != m_manager) {
            m_manager.release(m_store);
            m_manager.release(m_resolver);
            m_manager = null;
        }
        saxParser = null;
        m_store = null;
        m_resolver = null;
    }

    /**
     * Configure the component
     */
    public void parameterize(final Parameters params) throws ParameterException {
        m_useStore = params.getParameterAsBoolean("use-store", this.m_useStore);
        m_incrementalProcessing = params.getParameterAsBoolean("incremental-processing", this.m_incrementalProcessing);
        m_transformerFactory = params.getParameter("transformer-factory", null);
        m_checkIncludes = params.getParameterAsBoolean("check-includes", true);
        if (!m_useStore) {
            // release the store, if we don't need it anymore
            m_manager.release(m_store);
            m_store = null;
        } else if (null == m_store) {
            final String message = "XSLTProcessor: use-store is set to true, " + "but unable to aquire the Store.";
            throw new ParameterException(message);
        }
    }

    /**
     * Set the transformer factory used by this component
     */
    public void setTransformerFactory(final String classname) {
        m_factory = getTransformerFactory(classname);
    }

    /**
     * @see org.apache.excalibur.xml.xslt.XSLTProcessor#getTransformerHandler(Source)
     */
    public TransformerHandler getTransformerHandler(final Source stylesheet) throws XSLTProcessorException {
        return getTransformerHandler(stylesheet, null);
    }

    /**
     * @see org.apache.excalibur.xml.xslt.XSLTProcessor#getTransformerHandler(Source, XMLFilter)
     */
    public TransformerHandler getTransformerHandler(final Source stylesheet, final XMLFilter filter)
    throws XSLTProcessorException {
        final XSLTProcessor.TransformerHandlerAndValidity validity = getTransformerHandlerAndValidity(stylesheet, filter);
        return validity.getTransfomerHandler();
    }

    public TransformerHandlerAndValidity getTransformerHandlerAndValidity(final Source stylesheet)
    throws XSLTProcessorException {
        return getTransformerHandlerAndValidity(stylesheet, null);
    }

    public TransformerHandlerAndValidity getTransformerHandlerAndValidity(Source stylesheet, XMLFilter filter)
    throws XSLTProcessorException {

        final String id = stylesheet.getURI();
        TransformerHandlerAndValidity handlerAndValidity;

        try {
            handlerAndValidity = getTemplates(stylesheet, id);
            if (handlerAndValidity != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Reusing Templates for " + id);
                }
                return handlerAndValidity;
            }
        } catch (Exception e) {
            throw new XSLTProcessorException("Error retrieving template", e);
        }

        TraxErrorListener errorListener = new TraxErrorListener(stylesheet.getURI());
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Creating new Templates for " + id);
            }

            m_factory.setErrorListener(errorListener);

            // Create a Templates ContentHandler to handle parsing of the
            // stylesheet.
            TemplatesHandler templatesHandler = m_factory.newTemplatesHandler();

            // Set the system ID for the template handler since some
            // TrAX implementations (XSLTC) rely on this in order to obtain
            // a meaningful identifier for the Templates instances.
            templatesHandler.setSystemId(id);
            if (filter != null) {
                filter.setContentHandler(templatesHandler);
            }

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Source = " + stylesheet + ", templatesHandler = " + templatesHandler);
            }

            // Initialize List for included validities
            SourceValidity validity = stylesheet.getValidity();
            if (validity != null && m_checkIncludes) {
                m_includesMap.put(id, new ArrayList());
            }

            try {
                // Process the stylesheet.
                sourceToSAX(stylesheet, filter != null ? (ContentHandler) filter : (ContentHandler) templatesHandler);

                // Get the Templates object (generated during the parsing of
                // the stylesheet) from the TemplatesHandler.
                final Templates template = templatesHandler.getTemplates();

                if (null == template) {
                    throw new XSLTProcessorException("Unable to create templates for stylesheet: " + stylesheet.getURI());
                }

                // Must set base for Xalan stylesheet.
                // Otherwise document('') in logicsheet causes NPE.
                Class clazz = template.getClass();
                if (clazz.getName().equals("org.apache.xalan.templates.StylesheetRoot")) {
                    Method method = clazz.getMethod("setHref", new Class[]{String.class});
                    method.invoke(template, new Object[]{id});
                }

                putTemplates(template, stylesheet, id);

                // Create transformer handler
                final TransformerHandler handler = m_factory.newTransformerHandler(template);
                handler.getTransformer().setErrorListener(new TraxErrorListener(stylesheet.getURI()));
                handler.getTransformer().setURIResolver(this);

                // Create aggregated validity
                AggregatedValidity aggregated;
                if (validity != null && m_checkIncludes) {
                    List includes = (List) m_includesMap.get(id);
                    if (includes != null) {
                        aggregated = new AggregatedValidity();
                        aggregated.add(validity);
                        for (int i = includes.size() - 1; i >= 0; i--) {
                            aggregated.add((SourceValidity) ((Object[]) includes.get(i))[1]);
                        }
                        validity = aggregated;
                    }
                }

                // Create result
                handlerAndValidity = new MyTransformerHandlerAndValidity(handler, validity);
            } finally {
                if (m_checkIncludes) {
                    m_includesMap.remove(id);
                }
            }

            return handlerAndValidity;
        } catch (Exception e) {
            Throwable realEx = errorListener.getThrowable();
            if (realEx == null) {
                realEx = e;
            }

            if (realEx instanceof RuntimeException) {
                throw (RuntimeException)realEx;
            }

            if (realEx instanceof XSLTProcessorException) {
                throw (XSLTProcessorException)realEx;
            }

            throw new XSLTProcessorException("Exception when creating Transformer from " + stylesheet.getURI(), realEx);
        }
    }

    private void sourceToSAX(Source source, ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        if (source instanceof XMLizable) {
            ((XMLizable) source).toSAX(handler);
        } else {
            this.saxParser.parse(SourceUtil.getInputSource(source), handler);
        }
    }

    public void transform(final Source source, final Source stylesheet, final Parameters params, final Result result)
    throws XSLTProcessorException {
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Transform source = " + source + ", stylesheet = " + stylesheet +
                                  ", parameters = " + params + ", result = " + result);
            }
            final TransformerHandler handler = getTransformerHandler(stylesheet);
            if (params != null) {
                final Transformer transformer = handler.getTransformer();
                transformer.clearParameters();
                String[] names = params.getNames();
                for (int i = names.length - 1; i >= 0; i--) {
                    transformer.setParameter(names[i], params.getParameter(names[i]));
                }
            }

            handler.setResult(result);
            sourceToSAX(source, handler);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Transform done");
            }
        } catch (SAXException e) {
            // Unwrapping the exception will "remove" the real cause with
            // never Xalan versions and makes the exception message unusable
            final String message = "Error in running Transformation";
            throw new XSLTProcessorException(message, e);
            /*
             * if( e.getException() == null ) { final String message = "Error in
             * running Transformation"; throw new XSLTProcessorException(
             * message, e ); } else { final String message = "Got SAXException.
             * Rethrowing cause exception."; getLogger().debug( message, e );
             * throw new XSLTProcessorException( "Error in running
             * Transformation", e.getException() ); }
             */
        } catch (Exception e) {
            final String message = "Error in running Transformation";
            throw new XSLTProcessorException(message, e);
        }
    }

    /**
     * Get the TransformerFactory associated with the given classname. If the
     * class can't be found or the given class doesn't implement the required
     * interface, the default factory is returned.
     */
    private SAXTransformerFactory getTransformerFactory(String factoryName) {
        SAXTransformerFactory _factory;

        if (null == factoryName) {
            _factory = (SAXTransformerFactory) TransformerFactory.newInstance();
        } else {
            try {
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                if (loader == null) {
                    loader = getClass().getClassLoader();
                }
                _factory = (SAXTransformerFactory) loader.loadClass(factoryName).newInstance();
            } catch (ClassNotFoundException cnfe) {
                getLogger().error("Cannot find the requested TrAX factory '" + factoryName + "'. Using default TrAX Transformer Factory instead.");
                if (m_factory != null)
                    return m_factory;
                _factory = (SAXTransformerFactory) TransformerFactory.newInstance();
            } catch (ClassCastException cce) {
                getLogger().error(
                        "The indicated class '" + factoryName
                                + "' is not a TrAX Transformer Factory. Using default TrAX Transformer Factory instead.");
                if (m_factory != null)
                    return m_factory;
                _factory = (SAXTransformerFactory) TransformerFactory.newInstance();
            } catch (Exception e) {
                getLogger().error(
                        "Error found loading the requested TrAX Transformer Factory '" + factoryName
                                + "'. Using default TrAX Transformer Factory instead.");
                if (m_factory != null)
                    return m_factory;
                _factory = (SAXTransformerFactory) TransformerFactory.newInstance();
            }
        }

        _factory.setErrorListener(new TraxErrorListener(null));
        _factory.setURIResolver(this);

        // FIXME (SM): implementation-specific parameter passing should be
        // made more extensible.
        if (_factory.getClass().getName().equals("org.apache.xalan.processor.TransformerFactoryImpl")) {
            _factory.setAttribute("http://xml.apache.org/xalan/features/incremental", Boolean.valueOf(m_incrementalProcessing));
        }
        // SAXON 8 will not report errors unless version warning is set to false.
        if (_factory.getClass().getName().equals("net.sf.saxon.TransformerFactoryImpl")) {
            _factory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
        }

        return _factory;
    }

    private TransformerHandlerAndValidity getTemplates(Source stylesheet, String id)
    throws IOException, TransformerException {
        if (!m_useStore) {
            return null;
        }

        // we must augment the template ID with the factory classname since one
        // transformer implementation cannot handle the instances of a
        // template created by another one.
        String key = "XSLTTemplate: " + id + '(' + m_factory.getClass().getName() + ')';

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("getTemplates: stylesheet " + id);
        }

        // Stored is an array of the templates and the caching time and list of
        // includes
        Object[] templateAndValidityAndIncludes = (Object[]) m_store.get(key);
        if (templateAndValidityAndIncludes == null) {
            // Templates not found in cache
            return null;
        }

        // Check template validity
        SourceValidity storedValidity = (SourceValidity) templateAndValidityAndIncludes[1];
        int valid = storedValidity.isValid();
        if (valid == SourceValidity.UNKNOWN) {
            SourceValidity newValidity = stylesheet.getValidity();
            if (newValidity != null) {
                valid = storedValidity.isValid(newValidity);
            }
        }

        // Only valid stylesheets are stored
        if (valid != SourceValidity.VALID) {
            m_store.remove(key);
            return null;
        }

        // Check includes
        if (m_checkIncludes) {
            AggregatedValidity aggregated;
            List includes = (List) templateAndValidityAndIncludes[2];
            if (includes != null) {
                aggregated = new AggregatedValidity();
                aggregated.add(storedValidity);

                for (int i = includes.size() - 1; i >= 0; i--) {
                    // Every include stored as pair of source ID and validity
                    Object[] pair = (Object[]) includes.get(i);
                    storedValidity = (SourceValidity) pair[1];
                    aggregated.add(storedValidity);

                    valid = storedValidity.isValid();
                    if (valid == SourceValidity.UNKNOWN) {
                        Source includedSource = null;
                        try {
                            includedSource = m_resolver.resolveURI((String) pair[0]);
                            SourceValidity included = includedSource.getValidity();
                            if (included != null) {
                                valid = storedValidity.isValid(included);
                            }
                        } finally {
                            m_resolver.release(includedSource);
                        }
                    }
                    if (valid != SourceValidity.VALID) {
                        m_store.remove(key);
                        return null;
                    }
                }
                storedValidity = aggregated;
            }
        }

        TransformerHandler handler = m_factory.newTransformerHandler((Templates) templateAndValidityAndIncludes[0]);
        handler.getTransformer().setErrorListener(new TraxErrorListener(stylesheet.getURI()));
        handler.getTransformer().setURIResolver(this);
        return new MyTransformerHandlerAndValidity(handler, storedValidity);
    }

    private void putTemplates(Templates templates, Source stylesheet, String id) throws IOException {
        if (!m_useStore) {
            return;
        }

        // we must augment the template ID with the factory classname since one
        // transformer implementation cannot handle the instances of a
        // template created by another one.
        String key = "XSLTTemplate: " + id + '(' + m_factory.getClass().getName() + ')';

        // only stylesheets with a last modification date are stored
        SourceValidity validity = stylesheet.getValidity();
        if (null != validity) {
            // Stored is an array of the template and the current time
            Object[] templateAndValidityAndIncludes = new Object[3];
            templateAndValidityAndIncludes[0] = templates;
            templateAndValidityAndIncludes[1] = validity;
            if (m_checkIncludes) {
                templateAndValidityAndIncludes[2] = m_includesMap.get(id);
            }
            m_store.store(key, templateAndValidityAndIncludes);
        }
    }

    /**
     * Called by the processor when it encounters an xsl:include, xsl:import, or
     * document() function.
     *
     * @param href
     *            An href attribute, which may be relative or absolute.
     * @param base
     *            The base URI in effect when the href attribute was
     *            encountered.
     *
     * @return A Source object, or null if the href cannot be resolved, and the
     *         processor should try to resolve the URI itself.
     *
     * @throws TransformerException
     *             if an error occurs when trying to resolve the URI.
     */
    public javax.xml.transform.Source resolve(String href, String base) throws TransformerException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("resolve(href = " + href + ", base = " + base + "); resolver = " + m_resolver);
        }

        Source xslSource = null;
        try {
            if (base == null || href.indexOf(":") > 1) {
                // Null base - href must be an absolute URL
                xslSource = m_resolver.resolveURI(href);
            } else if (href.length() == 0) {
                // Empty href resolves to base
                xslSource = m_resolver.resolveURI(base);
            } else {
                // is the base a file or a real m_url
                if (!base.startsWith("file:")) {
                    int lastPathElementPos = base.lastIndexOf('/');
                    if (lastPathElementPos == -1) {
                        // this should never occur as the base should
                        // always be protocol:/....
                        return null; // we can't resolve this
                    } else {
                        xslSource = m_resolver.resolveURI(base.substring(0, lastPathElementPos) + "/" + href);
                    }
                } else {
                    File parent = new File(base.substring(5));
                    File parent2 = new File(parent.getParentFile(), href);
                    xslSource = m_resolver.resolveURI(parent2.toURL().toExternalForm());
                }
            }

            InputSource is = getInputSource(xslSource);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("xslSource = " + xslSource + ", system id = " + xslSource.getURI());
            }

            if (m_checkIncludes) {
                // Populate included validities
                List includes = (List) m_includesMap.get(base);
                if (includes != null) {
                    SourceValidity included = xslSource.getValidity();
                    if (included != null) {
                        includes.add(new Object[] { xslSource.getURI(), xslSource.getValidity() });
                    } else {
                        // One of the included stylesheets is not cacheable
                        m_includesMap.remove(base);
                    }
                }
            }

            return new StreamSource(is.getByteStream(), is.getSystemId());
        } catch (SourceException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to resolve " + href + "(base = " + base + "), return null", e);
            }

            // CZ: To obtain the same behaviour as when the resource is
            // transformed by the XSLT Transformer we should return null here.
            return null;
        } catch (java.net.MalformedURLException mue) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to resolve " + href + "(base = " + base + "), return null", mue);
            }

            return null;
        } catch (IOException ioe) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to resolve " + href + "(base = " + base + "), return null", ioe);
            }

            return null;
        } finally {
            m_resolver.release(xslSource);
        }
    }

    /**
     * Return a new <code>InputSource</code> object that uses the
     * <code>InputStream</code> and the system ID of the <code>Source</code>
     * object.
     *
     * @throws IOException
     *             if I/O error occured.
     */
    private static InputSource getInputSource(final Source source) throws IOException {
        final InputSource newObject = new InputSource(source.getInputStream());
        newObject.setSystemId(source.getURI());
        return newObject;
    }

    /**
     * Recycle the component
     */
    public void recycle() {
        m_includesMap.clear();
        // restore default factory
        if (m_factory != m_defaultFactory) {
            m_factory = m_defaultFactory;
        }
    }

    /**
     * Subclass to allow for instanciation, as for some unknown reason the
     * constructor is protected....
     */
    public static class MyTransformerHandlerAndValidity extends TransformerHandlerAndValidity {

        protected MyTransformerHandlerAndValidity(TransformerHandler handler, SourceValidity validity) {
            super(handler, validity);
        }
    }
}
