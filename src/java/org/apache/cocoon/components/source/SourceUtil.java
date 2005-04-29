/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.components.source;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.excalibur.xmlizer.XMLizer;
import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class contains some utility methods for the source resolving.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version $Id$
 */
public final class SourceUtil {

    private static REProgram uripattern = null;

    static {
        try {
            uripattern = new RECompiler().compile("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$");
        } catch (RESyntaxException e) {
            // Should not happen
            e.printStackTrace();
        }
    }

    /** Avoid instantiation */
    protected SourceUtil() {
    }

    /**
     * Generates SAX events from the XMLizable and handle SAXException.
     *
     * @param  source    the data
     */
    static public void toSAX(XMLizable source,
                             ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        try {
            source.toSAX(handler);
        } catch (SAXException e) {
            // Unwrap ProcessingException, IOException, and extreme cases of SAXExceptions.
            // Handle SourceException.
            // See also handleSAXException
            final Exception cause = e.getException();
            if (cause != null) {
                if (cause instanceof SourceException) {
                    throw handle((SourceException) cause);
                }
                if (cause instanceof ProcessingException) {
                    throw (ProcessingException) cause;
                }
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                if (cause instanceof SAXException) {
                    throw (SAXException) cause;
                }
            }

            // Throw original SAX exception
            throw e;
        }
    }

    /**
     * Generates SAX events from the given source.
     *
     * <p><b>NOTE</b>: If the implementation can produce lexical events,
     * care should be taken that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!</p>
     *
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    static public void toSAX(Source source,
                             ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        toSAX(CocoonComponentManager.getSitemapComponentManager(),
              source, null, handler);
    }

    /**
     * Generates SAX events from the given source by using XMLizer.
     * Current sitemap manager will be used to lookup XMLizer.
     *
     * <p><b>NOTE</b>: If the implementation can produce lexical events,
     * care should be taken that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!</p>
     *
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    static public void toSAX(Source source,
                             String mimeTypeHint,
                             ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        toSAX(CocoonComponentManager.getSitemapComponentManager(),
              source, mimeTypeHint, handler);
    }

    /**
     * Generates SAX events from the given source by using XMLizer.
     *
     * <p><b>NOTE</b>: If the implementation can produce lexical events,
     * care should be taken that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!</p>
     *
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     * @deprecated Use the {@link #toSAX(ServiceManager, Source, String, ContentHandler)}
     *             method instead.
     */
    static public void toSAX(ComponentManager manager,
                             Source source,
                             String mimeTypeHint,
                             ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        if (source instanceof XMLizable) {
            toSAX((XMLizable) source, handler);
        } else {
            String mimeType = source.getMimeType();
            if (null == mimeType) {
                mimeType = mimeTypeHint;
            }

            XMLizer xmlizer = null;
            try {
                xmlizer = (XMLizer) manager.lookup(XMLizer.ROLE);
                xmlizer.toSAX(source.getInputStream(),
                              mimeType,
                              source.getURI(),
                              handler);
            } catch (SourceException e) {
                throw SourceUtil.handle(e);
            } catch (ComponentException e) {
                throw new ProcessingException("Exception during streaming source.", e);
            } finally {
                manager.release((Component) xmlizer);
            }
        }
    }

    /**
     * Generates SAX events from the given source
     *
     * <p><b>NOTE</b>: If the implementation can produce lexical events,
     * care should be taken that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!</p>
     *
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    static public void toSAX(ServiceManager manager,
                             Source source,
                             String mimeTypeHint,
                             ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        if (source instanceof XMLizable) {
            toSAX((XMLizable) source, handler);
        } else {
            String mimeType = source.getMimeType();
            if (null == mimeType) {
                mimeType = mimeTypeHint;
            }

            XMLizer xmlizer = null;
            try {
                xmlizer = (XMLizer) manager.lookup(XMLizer.ROLE);
                xmlizer.toSAX(source.getInputStream(),
                              mimeType,
                              source.getURI(),
                              handler);
            } catch (SourceException e) {
                throw SourceUtil.handle(e);
            } catch (ServiceException e) {
                throw new ProcessingException("Exception during streaming source.", e);
            } finally {
                manager.release(xmlizer);
            }
        }
    }

    /**
     * Generates SAX events from the given source by parsing it.
     *
     * <p><b>NOTE</b>: If the implementation can produce lexical events,
     * care should be taken that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!</p>
     *
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     * @deprecated Use {@link #parse(ServiceManager, Source, ContentHandler)}.
     */
    static public void parse(ComponentManager manager,
                             Source source,
                             ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        if (source instanceof XMLizable) {
            toSAX((XMLizable) source, handler);
        } else {
            SAXParser parser = null;
            try {
                parser = (SAXParser) manager.lookup(SAXParser.ROLE);
                parser.parse(getInputSource(source), handler);
            } catch (SourceException e) {
                throw SourceUtil.handle(e);
            } catch (ComponentException e) {
                throw new ProcessingException("Exception during parsing source.", e);
            } finally {
                manager.release((Component) parser);
            }
        }
    }

    /**
     * Generates SAX events from the given source by parsing it.
     *
     * <p><b>NOTE</b>: If the implementation can produce lexical events,
     * care should be taken that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!</p>
     *
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    static public void parse(ServiceManager manager,
                             Source source,
                             ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        if (source instanceof XMLizable) {
            toSAX((XMLizable) source, handler);
        } else {
            SAXParser parser = null;
            try {
                parser = (SAXParser) manager.lookup(SAXParser.ROLE);
                parser.parse(getInputSource(source), handler);
            } catch (SourceException e) {
                throw SourceUtil.handle(e);
            } catch (ServiceException e) {
                throw new ProcessingException("Exception during parsing source.", e);
            } finally {
                manager.release(parser);
            }
        }
    }

    /**
     * Generates SAX events from the given source
     *
     * <p><b>NOTE</b>: If the implementation can produce lexical events,
     * care should be taken that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!</p>
     *
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    static public void toSAX(Source source,
                             ContentHandler handler,
                             Parameters typeParameters,
                             boolean filterDocumentEvent)
    throws SAXException, IOException, ProcessingException {
        // Test for url rewriting
        if (typeParameters != null
            && typeParameters.getParameter(URLRewriter.PARAMETER_MODE, null) != null) {
            handler = new URLRewriter(typeParameters, handler);
        }
        String mimeTypeHint = null;
        if (typeParameters != null) {
            mimeTypeHint = typeParameters.getParameter("mime-type", mimeTypeHint);
        }
        if (filterDocumentEvent) {
            IncludeXMLConsumer filter = new IncludeXMLConsumer(handler);
            toSAX(source, mimeTypeHint, filter);
        } else {
            toSAX(source, mimeTypeHint, handler);
        }
    }

    /**
     * Generates a DOM from the given source
     * @param source The data
     *
     * @return Created DOM document.
     *
     * @throws IOException If a io exception occurs.
     * @throws ProcessingException if no suitable converter is found
     * @throws SAXException If a SAX exception occurs.
     */
    static public Document toDOM(Source source)
    throws SAXException, IOException, ProcessingException {
        DOMBuilder builder = new DOMBuilder();

        toSAX(source, builder);

        Document document = builder.getDocument();
        if (document == null) {
            throw new ProcessingException("Could not build DOM for '" +
                                          source.getURI() + "'");
        }

        return document;
    }

    /**
     * Generates a DOM from the given source
     * @param source The data
     *
     * @return Created DOM document.
     *
     * @throws IOException If a io exception occurs.
     * @throws ProcessingException if no suitable converter is found
     * @throws SAXException If a SAX exception occurs.
     */
    static public Document toDOM(ServiceManager manager, Source source)
    throws SAXException, IOException, ProcessingException {
        DOMBuilder builder = new DOMBuilder();

        toSAX(manager, source, null, builder);

        Document document = builder.getDocument();
        if (document == null) {
            throw new ProcessingException("Could not build DOM for '" +
                                          source.getURI() + "'");
        }

        return document;
    }

    /**
     * Generates a DOM from the given source
     * @param source The data
     *
     * @return Created DOM document.
     *
     * @throws IOException If a io exception occurs.
     * @throws ProcessingException if no suitable converter is found
     * @throws SAXException If a SAX exception occurs.
     */
    static public Document toDOM(ServiceManager manager, String mimeTypeHint, Source source)
    throws SAXException, IOException, ProcessingException {
        DOMBuilder builder = new DOMBuilder();

        toSAX(manager, source, mimeTypeHint, builder);

        Document document = builder.getDocument();
        if (document == null) {
            throw new ProcessingException("Could not build DOM for '" +
                                          source.getURI() + "'");
        }

        return document;
    }

    /**
     * Make a ProcessingException from a SourceException.
     * If the exception is a SourceNotFoundException then a
     * ResourceNotFoundException is thrown.
     *
     * @param se Source exception
     * @return Created processing exception.
     */
    static public ProcessingException handle(SourceException se) {
        if (se instanceof SourceNotFoundException) {
            return new ResourceNotFoundException("Resource not found.", se);
        }
        return new ProcessingException("Exception during source resolving.",
                                       se);
    }

    /**
     * Make a ProcessingException from a SourceException.
     * If the exception is a SourceNotFoundException then a
     * ResourceNotFoundException is thrown.
     *
     * @param message Additional exception message.
     * @param se Source exception.
     * @return Created processing exception.
     */
    static public ProcessingException handle(String message,
                                             SourceException se) {
        if (se instanceof SourceNotFoundException) {
            return new ResourceNotFoundException(message, se);
        }
        return new ProcessingException(message, se);
    }

    /**
     * Handle SAXException catched in Generator's generate method.
     *
     * @param source Generator's source
     * @param e SAXException happened in the generator's generate method.
     */
    static public void handleSAXException(String source, SAXException e)
    throws ProcessingException, IOException, SAXException {
        final Exception cause = e.getException();
        if (cause != null) {
            // Unwrap ProcessingException, IOException, and extreme cases of SAXExceptions.
            // Handle SourceException.
            // See also toSax(XMLizable, ContentHandler
            if (cause instanceof SourceException) {
                throw handle((SourceException) cause);
            }
            if (cause instanceof ProcessingException) {
                throw (ProcessingException) cause;
            }
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            if (cause instanceof SAXException) {
                throw (SAXException) cause;
            }
            throw new ProcessingException("Could not read resource " +
                                          source, cause);
        }
        throw e;
    }

    /**
     * Get an InputSource object
     *
     * @param source Source.
     *
     * @return Input stream of the source.
     *
     * @throws IOException If a io exception occurs.
     * @throws ProcessingException If an exception occurs during
     *                             processing.
     */
    static public InputSource getInputSource(Source source)
      throws IOException, ProcessingException {
        try {
            final InputSource newObject = new InputSource(source.getInputStream());

            newObject.setSystemId(source.getURI());
            return newObject;
        } catch (SourceException se) {
            throw handle(se);
        }
    }

    /**
     * Get a <code>Source</code> object
     *
     * @param uri URI of the source.
     * @param typeParameters Type of Source query.  Currently, only
     * <code>method</code> parameter (value typically <code>GET</code> or
     * <code>POST</code>) is recognized.  May be <code>null</code>.
     * @param resourceParameters Parameters of the source.  May be <code>null</code>
     * @param resolver Resolver for the source.
     *
     * @return The resolved source.
     *
     * @throws IOException If a io exception occurs.
     * @throws SAXException If a SAX exception occurs.
     * @throws SourceException If the source an exception throws.
     */
    static public Source getSource(String uri,
                                   Parameters typeParameters,
                                   SourceParameters resourceParameters,
                                   SourceResolver resolver)
    throws IOException, SAXException, SourceException {

        // first step: encode parameters which are already appended to the url
        int queryPos = uri.indexOf('?');
        if (queryPos != -1) {
            String queryString = uri.substring(queryPos+1);
            SourceParameters queries = new SourceParameters(queryString);

            if (queries.hasParameters()) {
                StringBuffer buffer = new StringBuffer(uri.substring(0, queryPos));
                char separator = '?';

                Iterator iter = queries.getParameterNames();
                while (iter.hasNext()==true) {
                    String current = (String) iter.next();
                    Iterator values = queries.getParameterValues(current);
                    while (values.hasNext()) {
                        buffer.append(separator)
                                .append(current)
                                .append('=')
                                .append(NetUtils.encode((String) values.next(), "utf-8"));
                        separator = '&';
                    }
                }
                uri = buffer.toString();
            }
        }

        String method = ((typeParameters!=null)
                         ? typeParameters.getParameter("method", "GET")
                         : "GET");
        if (method.equalsIgnoreCase("POST") &&
                (resourceParameters == null ||
                !resourceParameters.hasParameters())) {
            method = "GET";
        }
        if (uri.startsWith("cocoon:") && resourceParameters != null &&
                resourceParameters.hasParameters()) {
            int pos = uri.indexOf(";jsessionid=");

            if (uri.startsWith("cocoon:")==false) {
                // It looks like this block is never called (JT)
                if (pos!=-1) {
                    uri = uri.substring(0, pos);
                }
                uri = org.apache.excalibur.source.SourceUtil.appendParameters(uri,
                    resourceParameters);
            } else {
                StringBuffer buf;

                if (pos==-1) {
                    buf = new StringBuffer(uri);
                } else {
                    buf = new StringBuffer(uri.substring(0, pos));
                }
                buf.append(((uri.indexOf('?')==-1) ? '?' : '&'));
                buf.append(resourceParameters.getEncodedQueryString());
                uri = buf.toString();
            }
        }
        Map resolverParameters = new java.util.HashMap();

        resolverParameters.put(SourceResolver.METHOD, method);
        if (typeParameters != null) {
            String encoding = typeParameters.getParameter("encoding",
                 System.getProperties().getProperty("file.encoding", "ISO-8859-1"));
            if (encoding != null && !"".equals(encoding)) {
                resolverParameters.put(SourceResolver.URI_ENCODING, encoding);
            }
        }
        resolverParameters.put(SourceResolver.URI_PARAMETERS,
                               resourceParameters);

        return resolver.resolveURI(uri, null, resolverParameters);
    }

    /**
     * Write a DOM Fragment to a source.
     * If the source is a ModifiableSource the interface is used.
     * If not, the source is invoked with an additional parameter named
     * "content" containing the XML.
     *
     * @param location URI of the Source
     * @param typeParameters Type of Source query.  Currently, only
     * <code>method</code> parameter (value typically <code>GET</code> or
     * <code>POST</code>) is recognized.  May be <code>null</code>.
     * @param parameters Parameters (e.g. URL params) of the source.
     * May be <code>null</code>
     * @param frag DOM fragment to serialize to the Source
     * @param resolver Resolver for the source.
     * @param serializerName The serializer to use
     *
     * @throws ProcessingException
     */
    public static void writeDOM(String location,
                                Parameters typeParameters,
                                SourceParameters parameters,
                                DocumentFragment frag,
                                SourceResolver resolver,
                                String serializerName)
    throws ProcessingException {
        Source source = null;

        try {
            source = SourceUtil.getSource(location, typeParameters,
                                          parameters, resolver);
            if (source instanceof ModifiableSource) {
                ModifiableSource ws = (ModifiableSource) source;

                frag.normalize();

                if (null != serializerName) {
                    ComponentManager manager = CocoonComponentManager.getSitemapComponentManager();

                    ComponentSelector selector = null;
                    Serializer serializer = null;
                    OutputStream oStream = null;
                    try {
                        selector = (ComponentSelector)manager.lookup(Serializer.ROLE + "Selector");
                        serializer = (Serializer)selector.select(serializerName);
                        oStream = ws.getOutputStream();
                        serializer.setOutputStream(oStream);
                        serializer.startDocument();
                        DOMStreamer streamer = new DOMStreamer(serializer);
                        streamer.stream(frag);
                        serializer.endDocument();
                    } catch (ComponentException e) {
                        throw new ProcessingException("Unable to lookup serializer.", e);
                    } finally {
                        if (oStream != null) {
                            oStream.flush();
                            try {
                                oStream.close();
                            } catch (Exception ignore) {
                            }
                        }
                        if (selector != null) {
                            selector.release(serializer);
                            manager.release(selector);
                        }
                    }
                } else {
                    Properties props = XMLUtils.createPropertiesForXML(false);
                    props.put(OutputKeys.ENCODING, "ISO-8859-1");
                    final String content = XMLUtils.serializeNode(frag, props);
                    OutputStream oStream = ws.getOutputStream();

                    oStream.write(content.getBytes());
                    oStream.flush();
                    oStream.close();
                }
            } else {
                String content;
                if (null != serializerName) {
                    ComponentManager  manager = CocoonComponentManager.getSitemapComponentManager();

                    ComponentSelector selector = null;
                    Serializer serializer = null;
                    ByteArrayOutputStream oStream = new ByteArrayOutputStream();
                    try {
                        selector = (ComponentSelector)manager.lookup(Serializer.ROLE + "Selector");
                        serializer = (Serializer)selector.select(serializerName);
                        serializer.setOutputStream(oStream);
                        serializer.startDocument();
                        DOMStreamer streamer = new DOMStreamer(serializer);
                        streamer.stream(frag);
                        serializer.endDocument();
                    } catch (ComponentException e) {
                        throw new ProcessingException("Unable to lookup serializer.", e);
                    } finally {
                        if (oStream != null) {
                            oStream.flush();
                            try {
                                oStream.close();
                            } catch (Exception ignore) {
                            }
                        }
                        if (selector != null) {
                            selector.release(serializer);
                            manager.release(selector);
                        }
                    }
                    content = oStream.toString();
                } else {
                    Properties props = XMLUtils.createPropertiesForXML(false);
                    props.put(OutputKeys.ENCODING, "ISO-8859-1");
                    content = XMLUtils.serializeNode(frag, props);
                }

                if (parameters == null) {
                    parameters = new SourceParameters();
                } else {
                    parameters = (SourceParameters) parameters.clone();
                }
                parameters.setSingleParameterValue("content", content);

                source = SourceUtil.getSource(location, typeParameters,
                                              parameters, resolver);
                SourceUtil.toSAX(source, new DefaultHandler());
            }
        } catch (SourceException e) {
            throw SourceUtil.handle(e);
        } catch (IOException e) {
            throw new ProcessingException(e);
        } catch (SAXException e) {
            throw new ProcessingException(e);
        } finally {
            resolver.release(source);
        }
    }

    /**
     * Read a DOM Fragment from a source
     *
     * @param location URI of the Source
     * @param typeParameters Type of Source query. Currently, only
     *        <code>method</code> parameter (value typically <code>GET</code> or
     *        <code>POST</code>) is recognized. May be <code>null</code>.
     * @param parameters Parameters (e.g. URL params) of the source.
     *        May be <code>null</code>
     * @param resolver Resolver for the source.
     *
     * @return DOM <code>DocumentFragment</code> constructed from the specified
     *         source.
     *
     * @throws ProcessingException
     */
    public static DocumentFragment readDOM(String location,
                                           Parameters typeParameters,
                                           SourceParameters parameters,
                                           SourceResolver resolver)
    throws ProcessingException {

        Source source = null;
        try {
            source = SourceUtil.getSource(location, typeParameters,
                                          parameters, resolver);
            Document doc = SourceUtil.toDOM(source);

            DocumentFragment fragment = doc.createDocumentFragment();
            fragment.appendChild(doc.getDocumentElement());

            return fragment;
        } catch (SourceException e) {
            throw SourceUtil.handle(e);
        } catch (IOException e) {
            throw new ProcessingException(e);
        } catch (SAXException e) {
            throw new ProcessingException(e);
        } finally {
            resolver.release(source);
        }
    }

    /**
     * Return the scheme of a URI. Just as there are many different methods
     * of access to resources, there are a variety of schemes for identifying
     * such resources.
     * (see <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>).
     *
     * @param uri Uniform resource identifier.
     *
     * @return Scheme of the URI.
     */
    public static String getScheme(String uri) {
        RE re = new RE(uripattern);
        if (re.match(uri)) {
            return re.getParen(2);
        } else {
            throw new IllegalArgumentException("'" + uri +
                                               "' is not a correct URI");
        }
    }

    /**
     * Return the authority of a URI. This authority is
     * typically defined by an Internet-based server or a scheme-specific
     * registry of naming authorities
     * (see <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>).
     *
     * @param uri Uniform resource identifier.
     *
     * @return Scheme of the URI.
     */
    public static String getAuthority(String uri) {
        RE re = new RE(uripattern);
        if (re.match(uri)) {
            return re.getParen(4);
        } else {
            throw new IllegalArgumentException("'" + uri +
                                               "' is not a correct URI");
        }
    }

    /**
     * Return the path of a URI. The path contains data, specific to the
     * authority (or the scheme if there is no authority component),
     * identifying the resource within the scope of that scheme and authority
     * (see <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>).
     *
     * @param uri Uniform resource identifier.
     *
     * @return Path of the URI.
     */
    public static String getPath(String uri) {
        RE re = new RE(uripattern);
        if (re.match(uri)) {
            return re.getParen(5);
        } else {
            throw new IllegalArgumentException("'" + uri +
                                               "' is not a correct URI");
        }
    }

    /**
     * Return the path of a URI, if the URI can't contains a authority.
     * This implementation differ to the RFC 2396.
     *
     * @param uri Uniform resource identifier.
     *
     * @return Path of the URI.
     */
    public static String getPathWithoutAuthority(String uri) {
        RE re = new RE(uripattern);
        if (re.match(uri)) {
            return re.getParen(4) + re.getParen(5);
        } else {
            throw new IllegalArgumentException("'" + uri +
                                               "' is not a correct URI");
        }
    }

    /**
     * Return the query of a URI. The query is a string of information to
     * be interpreted by the resource
     * (see <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>).
     *
     * @param uri Uniform resource identifier.
     *
     * @return Query of the URI.
     */
    public static String getQuery(String uri) {
        RE re = new RE(uripattern);
        if (re.match(uri)) {
            return re.getParen(7);
        } else {
            throw new IllegalArgumentException("'" + uri +
                                               "' is not a correct URI");
        }
    }

    /**
     * Return the fragment of a URI. When a URI reference is used to perform
     * a retrieval action on the identified resource, the optional fragment
     * identifier, consists of additional reference information to be
     * interpreted by the user agent after the retrieval action has been
     * successfully completed
     * (see <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>).
     *
     * @param uri Uniform resource identifier.
     *
     * @return Fragment of the URI.
     */
    public static String getFragment(String uri) {
        RE re = new RE(uripattern);
        if (re.match(uri)) {
            return re.getParen(9);
        } else {
            throw new IllegalArgumentException("'" + uri +
                                               "' is not a correct URI");
        }
    }
}
