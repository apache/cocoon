/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.source.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.source.URLRewriter;
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceResolver;
import org.apache.cocoon.core.xml.SAXParser;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.excalibur.xmlizer.XMLizer;
import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class contains some utility methods for the source resolving.
 *
 * @version $Id$
 */
public abstract class SourceUtil {

    protected static REProgram uripattern;

    static {
        try {
            uripattern = new RECompiler().compile("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$");
        } catch (RESyntaxException e) {
            // Should not happen
            throw new RuntimeException("Error parsing regular expression.", e);
        }
    }

    /**
     * Generates SAX events from the given source with possible URL rewriting.
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
            toSAX(manager, source, mimeTypeHint, filter);
        } else {
            toSAX(manager, source, mimeTypeHint, handler);
        }
    }

	/**
	 * Generates SAX events from the XMLizable and handle SAXException.
	 *
	 * @param  source    the data
	 */
	public static void toSAX(XMLizable      source, ContentHandler handler) throws SAXException, IOException, ProcessingException {
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
    static public void toSAX(ServiceManager manager,
    		                 Source         source,
                             ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        toSAX(manager, source, null, handler);
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
     */
    public static void toSAX(ServiceManager manager,
                             Source         source,
                             String         mimeTypeHint,
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
     * Generates SAX events from the given source by using XMLizer.
     *
     * <p><b>NOTE</b>: If the implementation can produce lexical events,
     * care should be taken that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!</p>
     *
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    public static void toSAX(XMLizer        xmlizer,
                             Source         source,
                             String         mimeTypeHint,
                             ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        if (source instanceof XMLizable) {
            toSAX((XMLizable) source, handler);
        } else {
            String mimeType = source.getMimeType();
            if (null == mimeType) {
                mimeType = mimeTypeHint;
            }
            try {
                xmlizer.toSAX(source.getInputStream(),
                              mimeType,
                              source.getURI(),
                              handler);
            } catch (SourceException e) {
                throw SourceUtil.handle(e);
            }
        }
    }

	/**
	 * Generates character SAX events from the given source.
	 *
	 * @param source The data
	 * @param encoding The character encoding of the data
	 */
	public static void toCharacters(Source source, String encoding, ContentHandler handler) throws SAXException, IOException, ProcessingException {
	    try {
	        Reader r = encoding == null?
	                new InputStreamReader(source.getInputStream()):
	                new InputStreamReader(source.getInputStream(), encoding);
	
	        int len;
	        char[] chr = new char[4096];
	        try {
	            while ((len = r.read(chr)) > 0) {
	                handler.characters(chr, 0, len);
	            }
	        } finally {
	            r.close();
	        }
	    } catch (SAXException e) {
	        handleSAXException(source.getURI(), e);
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
	 * @deprecated use {@link #parse(SAXParser, Source, ContentHandler)} instead
	 */
	public static void parse(ServiceManager manager, Source source, ContentHandler handler) throws SAXException, IOException, ProcessingException {
	    if (source instanceof XMLizable) {
	        toSAX((XMLizable) source, handler);
	    } else {
	        org.apache.excalibur.xml.sax.SAXParser parser = null;
	        try {
	            parser = (org.apache.excalibur.xml.sax.SAXParser) manager.lookup(org.apache.excalibur.xml.sax.SAXParser.ROLE);
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
	 * Generates SAX events from the given source by parsing it.
	 *
	 * <p><b>NOTE</b>: If the implementation can produce lexical events,
	 * care should be taken that <code>handler</code> can actually
	 * directly implement the LexicalHandler interface!</p>
	 *
	 * @param  source    the data
	 * @throws ProcessingException if error during processing source data occurs
	 */
	public static void parse(SAXParser parser, Source source, ContentHandler handler) throws SAXException, IOException, ProcessingException {
	    if (source instanceof XMLizable) {
	        toSAX((XMLizable) source, handler);
	    } else {
	        try {
	            parser.parse(getInputSource(source), handler);
	        } catch (SourceException e) {
	            throw SourceUtil.handle(e);
	        }
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
	public static Document toDOM(ServiceManager manager, Source source) throws SAXException, IOException, ProcessingException {
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
	public static Document toDOM(ServiceManager manager, String mimeTypeHint, Source source) throws SAXException, IOException, ProcessingException {
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
	public static ProcessingException handle(SourceException se) {
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
	public static ProcessingException handle(String message, SourceException se) {
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
	public static void handleSAXException(String source, SAXException e) throws ProcessingException, IOException, SAXException {
	    final Exception cause = e.getException();
	    if (cause != null) {
	        // Unwrap ProcessingException, IOException, and extreme cases of SAXExceptions.
	        // Handle SourceException.
	        // See also toSax(XMLizable, ContentHandler)
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
	public static InputSource getInputSource(Source source) throws IOException, ProcessingException {
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
	public static Source getSource(String uri, Parameters typeParameters, SourceParameters resourceParameters, SourceResolver resolver) throws IOException, SAXException, SourceException {
	
	    // first step: encode parameters which are already appended to the url
	    int queryPos = uri.indexOf('?');
	    if (queryPos != -1) {
	        String queryString = uri.substring(queryPos+1);
	        SourceParameters queries = new SourceParameters(queryString);
	
	        if (queries.hasParameters()) {
	            StringBuffer buffer = new StringBuffer(uri.substring(0, queryPos));
	            char separator = '?';
	
	            Iterator i = queries.getParameterNames();
	            while (i.hasNext()) {
	                String current = (String) i.next();
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
	
	        StringBuffer buf;
	        if (pos == -1) {
	            buf = new StringBuffer(uri);
	        } else {
	            buf = new StringBuffer(uri.substring(0, pos));
	        }
	        buf.append(((uri.indexOf('?') == -1) ? '?' : '&'));
	        buf.append(resourceParameters.getEncodedQueryString());
	        uri = buf.toString();
	    }
	
	    Map resolverParameters = new HashMap();
	    resolverParameters.put(SourceResolver.METHOD, method);
	    if (typeParameters != null) {
	        String encoding = typeParameters.getParameter("encoding",
	             System.getProperty("file.encoding", "ISO-8859-1"));
	        if (encoding != null && !"".equals(encoding)) {
	            resolverParameters.put(SourceResolver.URI_ENCODING, encoding);
	        }
	    }
	    resolverParameters.put(SourceResolver.URI_PARAMETERS,
	                           resourceParameters);
	
	    return resolver.resolveURI(uri, null, resolverParameters);
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
