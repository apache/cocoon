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
import java.util.Iterator;
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

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.util.SourceUtil;
import org.apache.cocoon.core.xml.SAXParser;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AggregatedValidity;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

/**
 * Adaptation of Excalibur's XSLTProcessor implementation to allow for better
 * error reporting. This is a bean implementation that can be configured in
 * a spring context.
 *
 * @version $Id$
 */
public class XSLTProcessorImpl
    extends AbstractLogEnabled
    implements XSLTProcessor, URIResolver {

    /** The store service instance */
    protected Store store;

    /** The configured transformer factory to use */
    protected String transformerFactory;

    /** The trax TransformerFactory this component uses */
    protected SAXTransformerFactory factory;

    /** Is incremental processing turned on? (default for Xalan: no) */
    protected boolean incrementalProcessing;

    /** Resolver used to resolve XSLT document() calls, imports and includes */
    protected SourceResolver resolver;

    /** Check included stylesheets */
    protected boolean checkIncludes;

    /** Map of pairs of System ID's / validities of the included stylesheets */
    protected Map includesMap = new HashMap();

    protected SAXParser saxParser;

    /**
     * Initialize this component.
     */
    public void init() throws Exception {
        this.factory = getTransformerFactory(this.transformerFactory);
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public void setIncrementalProcessing(boolean incrementalProcessing) {
        this.incrementalProcessing = incrementalProcessing;
    }

    public void setResolver(SourceResolver resolver) {
        this.resolver = resolver;
    }

    public void setCheckIncludes(boolean checkIncludes) {
        this.checkIncludes = checkIncludes;
    }

    public void setSaxParser(SAXParser saxParser) {
        this.saxParser = saxParser;
    }

    /**
     * Set the transformer factory used by this component
     */
    public void setTransformerFactory(final String classname) {
        this.transformerFactory = classname;
    }

    /**
     * @see org.apache.excalibur.xml.xslt.XSLTProcessor#getTransformerHandler(org.apache.excalibur.source.Source)
     */
    public TransformerHandler getTransformerHandler(final Source stylesheet) throws XSLTProcessorException {
        return getTransformerHandler(stylesheet, null);
    }

    /**
     * @see org.apache.excalibur.xml.xslt.XSLTProcessor#getTransformerHandler(org.apache.excalibur.source.Source,
     *      org.xml.sax.XMLFilter)
     */
    public TransformerHandler getTransformerHandler(final Source stylesheet, final XMLFilter filter) throws XSLTProcessorException {
        final XSLTProcessor.TransformerHandlerAndValidity validity = getTransformerHandlerAndValidity(stylesheet, filter);
        return validity.getTransfomerHandler();
    }

    /**
     * @see org.apache.excalibur.xml.xslt.XSLTProcessor#getTransformerHandlerAndValidity(org.apache.excalibur.source.Source)
     */
    public TransformerHandlerAndValidity getTransformerHandlerAndValidity(final Source stylesheet) throws XSLTProcessorException {
        return getTransformerHandlerAndValidity(stylesheet, null);
    }

    /**
     * @see org.apache.excalibur.xml.xslt.XSLTProcessor#getTransformerHandlerAndValidity(org.apache.excalibur.source.Source, org.xml.sax.XMLFilter)
     */
    public TransformerHandlerAndValidity getTransformerHandlerAndValidity(Source stylesheet, XMLFilter filter) throws XSLTProcessorException {

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
        } catch(Exception e) {
            throw new XSLTProcessorException("Error retrieving template", e);
        }

        XSLTProcessorErrorListener errorListener = new XSLTProcessorErrorListener(getLogger(), stylesheet.getURI());
        try{
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Creating new Templates for " + id);
            }

            factory.setErrorListener(errorListener);

            // Create a Templates ContentHandler to handle parsing of the
            // stylesheet.
            TemplatesHandler templatesHandler = factory.newTemplatesHandler();

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
            if (validity != null && checkIncludes) {
                includesMap.put(id, new ArrayList());
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
                final TransformerHandler handler = factory.newTransformerHandler(template);
                handler.getTransformer().setErrorListener(new XSLTProcessorErrorListener(getLogger(), stylesheet.getURI()));
                handler.getTransformer().setURIResolver(this);

                // Create aggregated validity
                AggregatedValidity aggregated = null;
                if (validity != null && checkIncludes) {
                    List includes = (List) includesMap.get(id);
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
                if (checkIncludes)
                    includesMap.remove(id);
            }

            return handlerAndValidity;
        } catch (Exception e) {
            Throwable realEx = errorListener.getThrowable();
            if (realEx == null) realEx = e;

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
    throws SAXException, IOException, SourceException, ProcessingException {
        if (source instanceof XMLizable) {
            ((XMLizable) source).toSAX(handler);
        } else {
            this.saxParser.parse(SourceUtil.getInputSource(source), handler);
        }
    }

    /**
     * @see org.apache.cocoon.components.xslt.XSLTProcessor#transform(org.apache.excalibur.source.Source, org.apache.excalibur.source.Source, java.util.Map, javax.xml.transform.Result)
     */
    public void transform(final Source source, final Source stylesheet, final Map params, final Result result) throws XSLTProcessorException {
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(
                        "Transform source = " + source + ", stylesheet = " + stylesheet + ", parameters = " + params + ", result = " + result);
            }
            final TransformerHandler handler = getTransformerHandler(stylesheet);
            if (params != null) {
                final Transformer transformer = handler.getTransformer();
                transformer.clearParameters();
                final Iterator i = params.entrySet().iterator();
                while ( i.hasNext() ) {
                    final Map.Entry current = (Map.Entry)i.next();
                    transformer.setParameter(current.getKey().toString(), current.getValue());
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
                if (factory != null)
                    return factory;
                _factory = (SAXTransformerFactory) TransformerFactory.newInstance();
            } catch (ClassCastException cce) {
                getLogger().error(
                        "The indicated class '" + factoryName
                                + "' is not a TrAX Transformer Factory. Using default TrAX Transformer Factory instead.");
                if (factory != null)
                    return factory;
                _factory = (SAXTransformerFactory) TransformerFactory.newInstance();
            } catch (Exception e) {
                getLogger().error(
                        "Error found loading the requested TrAX Transformer Factory '" + factoryName
                                + "'. Using default TrAX Transformer Factory instead.");
                if (factory != null)
                    return factory;
                _factory = (SAXTransformerFactory) TransformerFactory.newInstance();
            }
        }

        _factory.setErrorListener(new XSLTProcessorErrorListener(getLogger(), null));
        _factory.setURIResolver(this);

        // FIXME (SM): implementation-specific parameter passing should be
        // made more extensible.
        if (_factory.getClass().getName().equals("org.apache.xalan.processor.TransformerFactoryImpl")) {
            _factory.setAttribute("http://xml.apache.org/xalan/features/incremental", Boolean.valueOf(incrementalProcessing));
        }
        // SAXON 8 will not report errors unless version warning is set to false.
        if (_factory.getClass().getName().equals("net.sf.saxon.TransformerFactoryImpl")) {
            _factory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
        }

        return _factory;
    }

    private TransformerHandlerAndValidity getTemplates(Source stylesheet, String id)
    throws IOException, TransformerException {
        if (this.store == null) {
            return null;
        }

        // we must augment the template ID with the factory classname since one
        // transformer implementation cannot handle the instances of a
        // template created by another one.
        String key = "XSLTTemplate: " + id + '(' + factory.getClass().getName() + ')';

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("getTemplates: stylesheet " + id);
        }

        SourceValidity newValidity = stylesheet.getValidity();

        // Only stylesheets with validity are stored
        if (newValidity == null) {
            // Remove an old template
            store.remove(key);
            return null;
        }

        // Stored is an array of the templates and the caching time and list of
        // includes
        Object[] templateAndValidityAndIncludes = (Object[]) store.get(key);
        if (templateAndValidityAndIncludes == null) {
            // Templates not found in cache
            return null;
        }

        // Check template modification time
        SourceValidity storedValidity = (SourceValidity) templateAndValidityAndIncludes[1];
        int valid = storedValidity.isValid();
        boolean isValid;
        if (valid == 0) {
            valid = storedValidity.isValid(newValidity);
            isValid = (valid == 1);
        } else {
            isValid = (valid == 1);
        }
        if (!isValid) {
            store.remove(key);
            return null;
        }

        // Check includes
        if (checkIncludes) {
            AggregatedValidity aggregated = null;
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
                    isValid = false;
                    if (valid == 0) {
                        Source includedSource = null;
                        try {
                            includedSource = resolver.resolveURI((String) pair[0]);
                            SourceValidity included = includedSource.getValidity();
                            if (included != null) {
                                valid = storedValidity.isValid(included);
                                isValid = (valid == 1);
                            }
                        } finally {
                            resolver.release(includedSource);
                        }
                    } else {
                        isValid = (valid == 1);
                    }
                    if (!isValid) {
                        store.remove(key);
                        return null;
                    }
                }
                storedValidity = aggregated;
            }
        }

        TransformerHandler handler = factory.newTransformerHandler((Templates) templateAndValidityAndIncludes[0]);
        handler.getTransformer().setErrorListener(new XSLTProcessorErrorListener(getLogger(), stylesheet.getURI()));
        handler.getTransformer().setURIResolver(this);
        return new MyTransformerHandlerAndValidity(handler, storedValidity);
    }

    private void putTemplates(Templates templates, Source stylesheet, String id) throws IOException {
        if (this.store == null) {
            return;
        }
        // we must augment the template ID with the factory classname since one
        // transformer implementation cannot handle the instances of a
        // template created by another one.
        String key = "XSLTTemplate: " + id + '(' + factory.getClass().getName() + ')';

        // only stylesheets with a last modification date are stored
        SourceValidity validity = stylesheet.getValidity();
        if (null != validity) {
            // Stored is an array of the template and the current time
            Object[] templateAndValidityAndIncludes = new Object[3];
            templateAndValidityAndIncludes[0] = templates;
            templateAndValidityAndIncludes[1] = validity;
            if (checkIncludes) {
                templateAndValidityAndIncludes[2] = includesMap.get(id);
            }
            store.store(key, templateAndValidityAndIncludes);
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
            getLogger().debug("resolve(href = " + href + ", base = " + base + "); resolver = " + resolver);
        }

        Source xslSource = null;
        try {
            if (base == null || href.indexOf(":") > 1) {
                // Null base - href must be an absolute URL
                xslSource = resolver.resolveURI(href);
            } else if (href.length() == 0) {
                // Empty href resolves to base
                xslSource = resolver.resolveURI(base);
            } else {
                // is the base a file or a real m_url
                if (!base.startsWith("file:")) {
                    int lastPathElementPos = base.lastIndexOf('/');
                    if (lastPathElementPos == -1) {
                        // this should never occur as the base should
                        // always be protocol:/....
                        return null; // we can't resolve this
                    } else {
                        xslSource = resolver.resolveURI(base.substring(0, lastPathElementPos) + "/" + href);
                    }
                } else {
                    File parent = new File(base.substring(5));
                    File parent2 = new File(parent.getParentFile(), href);
                    xslSource = resolver.resolveURI(parent2.toURL().toExternalForm());
                }
            }

            InputSource is = getInputSource(xslSource);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("xslSource = " + xslSource + ", system id = " + xslSource.getURI());
            }

            if (checkIncludes) {
                // Populate included validities
                List includes = (List) includesMap.get(base);
                if (includes != null) {
                    SourceValidity included = xslSource.getValidity();
                    if (included != null) {
                        includes.add(new Object[] { xslSource.getURI(), xslSource.getValidity() });
                    } else {
                        // One of the included stylesheets is not cacheable
                        includesMap.remove(base);
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
            resolver.release(xslSource);
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
    private static InputSource getInputSource(final Source source) throws IOException, SourceException {
        final InputSource newObject = new InputSource(source.getInputStream());
        newObject.setSystemId(source.getURI());
        return newObject;
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
