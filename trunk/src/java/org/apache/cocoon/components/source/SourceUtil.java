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
    include  the following  acknowledgment:   "This product includes software
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
package org.apache.cocoon.components.source;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.EnvironmentHelper;
import org.apache.cocoon.serialization.Serializer;
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
 * @version CVS $Id: SourceUtil.java,v 1.10 2004/01/07 15:48:32 cziegeler Exp $
 */
public final class SourceUtil {

    private static REProgram uripattern = null;

    static {
        try {
            uripattern = (new RECompiler()).compile("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$");
        } catch (RESyntaxException rese) {
            rese.printStackTrace();
        }
    }

    /** Avoid instantiation */
    protected SourceUtil() {
    }

    /**
     * Generates SAX events from the given source
     * <b>NOTE</b> : if the implementation can produce lexical events, care should be taken
     * that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    static public void toSAX( Source source,
                              ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        toSAX(EnvironmentHelper.getSitemapServiceManager(), 
              source, null, handler);
    }

    /**
     * Generates SAX events from the given source
     * <b>NOTE</b> : if the implementation can produce lexical events, care should be taken
     * that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    static public void toSAX( Source source,
                                String mimeTypeHint,
                              ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        toSAX(EnvironmentHelper.getSitemapServiceManager(), 
              source, mimeTypeHint, handler);
    }

    /**
     * Generates SAX events from the given source
     * <b>NOTE</b> : if the implementation can produce lexical events, care should be taken
     * that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    static public void toSAX( ServiceManager manager, Source source,
                                String mimeTypeHint,
                                ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        if ( source instanceof XMLizable ) {
            ((XMLizable)source).toSAX( handler );
        } else {
            String mimeType = source.getMimeType();
            if ( null == mimeType) mimeType = mimeTypeHint;
            XMLizer xmlizer = null;
            try {
                xmlizer = (XMLizer) manager.lookup( XMLizer.ROLE);
                xmlizer.toSAX( source.getInputStream(),
                               mimeType,
                               source.getURI(),
                               handler );
            } catch (SourceException se) {
                throw SourceUtil.handle(se);
            } catch (ServiceException ce) {
                throw new ProcessingException("Exception during streaming source.", ce);
            } finally {
                manager.release( xmlizer );
            }
        }
    }

    /**
     * Generates SAX events from the given source by parsing it.
     * <b>NOTE</b> : if the implementation can produce lexical events, care should be taken
     * that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    static public void parse( ServiceManager manager, 
                              Source source,
                              ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        if ( source instanceof XMLizable ) {
            ((XMLizable)source).toSAX( handler );
        } else {
            SAXParser parser = null;
            try {
                parser = (SAXParser) manager.lookup( SAXParser.ROLE);
                parser.parse( getInputSource( source ), handler );
            } catch (SourceException se) {
                throw SourceUtil.handle(se);
            } catch (ServiceException ce) {
                throw new ProcessingException("Exception during parsing source.", ce);
            } finally {
                manager.release( parser );
            }
        }
    }

    /**
     * Generates SAX events from the given source
     * <b>NOTE</b> : if the implementation can produce lexical events, care should be taken
     * that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!
     * 
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    static public void toSAX( Source         source,
                              ContentHandler   handler,
                              Parameters       typeParameters,
                              boolean         filterDocumentEvent)
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
            throw new ProcessingException("Could not build DOM for '"+
                                          source.getURI()+"'");
        }

        return document;
    }

    /**
     * Make a ProcessingException from a SourceException
     * If the exception is a SourceNotFoundException than a
     * ResourceNotFoundException is thrown
     *
     * @param se Source exception
     *
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
     * Make a ProcessingException from a SourceException
     * If the exception is a SourceNotFoundException than a
     * ResourceNotFoundException is thrown
     *
     * @param message Additional exception message.
     * @param se Source exception.
     *
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
    static public Source getSource( String           uri,
                                    Parameters       typeParameters,
                                    SourceParameters resourceParameters,
                                    SourceResolver   resolver)
    throws IOException, SAXException, SourceException {

        // first step: encode parameters which are already appended to the url
        int queryPos = uri.indexOf('?');

        if (queryPos!=-1) {
            String queryString = uri.substring(queryPos+1);
            SourceParameters queries = new SourceParameters(queryString);

            if (queries.hasParameters()) {
                StringBuffer buffer;

                buffer = new StringBuffer(uri.substring(0, queryPos));
                String current;
                Iterator iter = queries.getParameterNames();
                char separator = '?';
                Iterator values;

                while (iter.hasNext()==true) {
                    current = (String) iter.next();
                    values = queries.getParameterValues(current);
                    while (values.hasNext()) {
                        buffer.append(separator).append(current).append('=').append(org.apache.excalibur.source.SourceUtil.encode((String) values.next()));
                        separator = '&';
                    }
                }
                uri = buffer.toString();
            }
        }
        // FIXME: followRedirects: Is it something which is not completed?
//        boolean followRedirects = (typeParameters != null ?
//                                   typeParameters.getParameterAsBoolean("followRedirects", true)
//                                      : true);
        String method = ((typeParameters!=null)
                         ? typeParameters.getParameter("method", "GET")
                         : "GET");
        if (method.equalsIgnoreCase("POST") &&
                (resourceParameters == null ||
                !resourceParameters.hasParameters())) {
            method = "GET";
        }
        if (uri.startsWith("cocoon:") && (resourceParameters!=null) &&
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
        resolverParameters.put(SourceResolver.URI_PARAMETERS,
                               resourceParameters);

        return resolver.resolveURI(uri, null, resolverParameters);
    }

    /**
     * Write a DOM Fragment to a source
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
     * @param serializerName
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

                if ( null != serializerName) {
					ServiceManager manager = EnvironmentHelper.getSitemapServiceManager();

	                ServiceSelector selector = null;
	                Serializer serializer = null;
	                OutputStream oStream = null;
	                try {
	                     selector = (ServiceSelector)manager.lookup(Serializer.ROLE + "Selector");
	                     serializer = (Serializer)selector.select(serializerName);
	                     oStream = ws.getOutputStream();
	                     serializer.setOutputStream(oStream);
                         serializer.startDocument();
	                     DOMStreamer streamer = new DOMStreamer(serializer);
	                     streamer.stream(frag);
                         serializer.endDocument();
	                } catch (ServiceException e) {
	                	throw new ProcessingException("Unable to lookup serializer.", e);
					} finally {
	                    if (oStream != null) {
	                        oStream.flush();
	                        try {
	                            oStream.close();
	                        } catch (Exception ignore) {
                            }
                        }
						if ( selector != null ) {
							selector.release( serializer );
							manager.release( selector );
						}
	                }
                } else {
	                final String content = XMLUtils.serializeNode(frag,
	                                           XMLUtils.defaultSerializeToXMLFormat(false));
	                OutputStream oStream = ws.getOutputStream();
	
	                oStream.write(content.getBytes());
	                oStream.flush();
	                oStream.close();
                }
            } else {
            	String content;
				if ( null != serializerName) {
					ServiceManager  manager = EnvironmentHelper.getSitemapServiceManager();
                    
                    ServiceSelector selector = null;
                    Serializer serializer = null;
                    ByteArrayOutputStream oStream = new ByteArrayOutputStream();
                    try {
                        selector = (ServiceSelector)manager.lookup(Serializer.ROLE + "Selector");
                        serializer = (Serializer)selector.select(serializerName);
                        serializer.setOutputStream(oStream);
                        serializer.startDocument();
                        DOMStreamer streamer = new DOMStreamer(serializer);
                        streamer.stream(frag);
                        serializer.endDocument();
					} catch (ServiceException e) {
						throw new ProcessingException("Unable to lookup serializer.", e);
                    } finally {
                        if (oStream != null) {
                            oStream.flush();
                            try {
                                oStream.close();
                            } catch (Exception ignore) {
                            }
                        }
						if ( selector != null ) {
							selector.release( serializer );
							manager.release( selector );
						}
                    }
					content = oStream.toString();
				} else {
                    content = XMLUtils.serializeNode(frag,
                                           XMLUtils.defaultSerializeToXMLFormat(false));
				}
				
                if (parameters==null) {
                    parameters = new SourceParameters();
                } else {
                    parameters = (SourceParameters) parameters.clone();
                }
                parameters.setSingleParameterValue("content", content);
                source = SourceUtil.getSource(location, typeParameters,
                                              parameters, resolver);
                SourceUtil.toSAX(source, new DefaultHandler());
            }
        } catch (SourceException se) {
            throw SourceUtil.handle(se);
        } catch (IOException ce) {
            throw new ProcessingException(ce);
        } catch (SAXException ce) {
            throw new ProcessingException(ce);
            // } catch (ComponentException ce) {
            // throw new ProcessingException("Exception during lookup of component.", ce);
        } finally {
            resolver.release(source);
        }
    }

    /**
     * Read a DOM Fragment from a source
     *
     * @param location URI of the Source
     * @param typeParameters Type of Source query.  Currently, only
     * <code>method</code> parameter (value typically <code>GET</code> or
     * <code>POST</code>) is recognized.  May be <code>null</code>.
     * @param parameters Parameters (e.g. URL params) of the source.
     * May be <code>null</code>
     * @param resolver Resolver for the source.
     *
     * @return DOM <code>DocumentFragment</code> constructed from the specified
     * Source
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
        } catch (SourceException se) {
            throw SourceUtil.handle(se);
        } catch (IOException ce) {
            throw new ProcessingException(ce);
        } catch (SAXException ce) {
            throw new ProcessingException(ce);
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
            throw new IllegalArgumentException("'"+uri+
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
            throw new IllegalArgumentException("'"+uri+
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
            throw new IllegalArgumentException("'"+uri+
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
            return re.getParen(4)+re.getParen(5);
        } else {
            throw new IllegalArgumentException("'"+uri+
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
            throw new IllegalArgumentException("'"+uri+
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
            throw new IllegalArgumentException("'"+uri+
                                               "' is not a correct URI");
        }
    }
}
