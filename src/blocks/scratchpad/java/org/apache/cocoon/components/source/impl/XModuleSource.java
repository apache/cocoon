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
package org.apache.cocoon.components.source.impl;


import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;

import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.impl.AbstractSource;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.xml.sax.XMLizable;

import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.components.modules.output.OutputModule;
import org.apache.cocoon.serialization.XMLSerializer;
import org.apache.cocoon.util.jxpath.DOMFactory;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DOMStreamer;

import org.apache.commons.jxpath.JXPathContext;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A <code>ModifiableSource</code> that takes its content from a
 * module.
 * <p>The URI syntax is
 * "xmodule:[<input-module>|<output-module>]:attribute-name[#XPath]",
 * where :
 * <ul>
 * <li>an input-module name is used for finding an input-module for reading data from</li>,
 * <li>an output-module name is used for finding an output-module for writing data to</li>,
 * <li>"attribute-name" is the name of the attribute found in the module</li>,
 * <li>"XPath" is an XPath that is aplied on the object in the
 * attribute, by using JXPath.</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:danielf@nada.kth.se">Daniel Fagerstom</a>
 */

public class XModuleSource
    extends AbstractSource
    implements ModifiableSource, XMLizable, DOMBuilder.Listener {

    private final static String SCHEME = "xmodule";
    private String attributeType;
    private String attributeName;
    private String xPath;
    private ServiceManager manager;
    private Map objectModel;
    private Logger logger;
    
    /**
     * Create a xmodule source from a 'xmodule:' uri and a the object model.
     * <p>The uri is of the form "xmodule:/attribute-type/attribute-name/xpath</p>
     */
    public XModuleSource( Map objectModel, String uri,
                          ServiceManager manager, Logger logger )
        throws MalformedURLException, SourceException {

        this.objectModel = objectModel;
        this.manager = manager;
        this.logger = logger;

        setSystemId( uri );

        // Scheme
        int start = 0;
        int end = uri.indexOf( ':' );
        if ( end == -1 )
            throw new MalformedURLException("Malformed uri for xmodule source (cannot find scheme) : " + uri);

        String scheme = uri.substring( start, end );
        if ( !SCHEME.equals( scheme ) )
            throw new MalformedURLException("Malformed uri for a xmodule source : " + uri);

        setScheme( scheme );

        // Attribute type
        start = end + 1;
        end = uri.indexOf( ':', start );
        if ( end == -1 ) {
            throw new MalformedURLException("Malformed uri for xmodule source (cannot find attribute type) : " + uri);
        }
        this.attributeType = uri.substring( start, end );

        // Attribute name
        start = end + 1;
        end = uri.indexOf( '#', start );

        if ( end == -1 )
            end = uri.length();

        if ( end == start )
            throw new MalformedURLException("Malformed uri for xmodule source (cannot find attribute name) : " + uri);

        this.attributeName = uri.substring( start, end );

        // xpath
        start = end + 1;
        this.xPath = start < uri.length() ? uri.substring( start ) : "";
    }
    
    /**
     * Implement this method to obtain SAX events.
     *
     */

    public void toSAX(ContentHandler handler)
        throws SAXException {

        Object obj = getInputAttribute( this.attributeType, this.attributeName );
        if ( obj == null )
            throw new SAXException( " The attribute: " + this.attributeName +
                                    " is empty" );

        if ( !(this.xPath.length() == 0 || this.xPath.equals( "/" )) ) {
            JXPathContext context = JXPathContext.newContext( obj );

            obj = context.getPointer( this.xPath ).getNode();

            if ( obj == null )
                throw new SAXException( "the xpath: " + this.xPath +
                                        " applied on the attribute: " +
                                        this.attributeName +
                                        " returns null");
        }

        if ( obj instanceof Document ) {
            DOMStreamer domStreamer = new DOMStreamer( handler );
            domStreamer.stream( (Document)obj );
        } else if ( obj instanceof Node ) {
            DOMStreamer domStreamer = new DOMStreamer( handler );
            handler.startDocument();
            domStreamer.stream( (Node)obj );
            handler.endDocument();
        } else if ( obj instanceof XMLizable ) {
            ((XMLizable)obj).toSAX( handler );
        } else {
            throw new SAXException( "The object type: " + obj.getClass() +
                                    " could not be serialized to XML: " + obj );
        }
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     *
     * @throws IOException if I/O error occured.
     */
    // Stolen from QDoxSource
    public InputStream getInputStream() throws IOException, SourceException {
        if ( this.logger.isDebugEnabled() ) {
            this.logger.debug( "Getting InputStream for " + getURI() );
        }

        // Serialize the SAX events to the XMLSerializer:

        XMLSerializer serializer = new XMLSerializer();
        ByteArrayInputStream inputStream = null;

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( 2048 );
            serializer.setOutputStream( outputStream );
            toSAX( serializer );
            inputStream = new ByteArrayInputStream( outputStream.toByteArray() );
        } catch ( SAXException se ) {
            logger.error( "SAX exception!", se );
            throw new SourceException( "Serializing SAX to a ByteArray failed!", se );
        }

        return inputStream;
    }

    /**
     * Does this source actually exist ?
     *
     * @return true if the resource exists.
     *
     */
    public boolean exists() {
        boolean exists = false;
        try {
            exists = getInputAttribute( this.attributeType, this.attributeName ) != null;
        } catch ( SAXException e ) {
            exists = false;
        }
        return exists;
    }

    /**
     * Get an <code>InputStream</code> where raw bytes can be written to.
     * The signification of these bytes is implementation-dependent and
     * is not restricted to a serialized XML document.
     *
     * @return a stream to write to
     */ 
    public OutputStream getOutputStream() throws IOException {
        return new DOMOutputStream();
    }

    /**
     * Delete the source 
     */
    public void delete() throws SourceException {
        if ( !(this.xPath.length() == 0 || this.xPath.equals( "/" )) ) {
            Object value;
            try {
                value = getInputAttribute( this.attributeType, this.attributeName );
            } catch ( SAXException e ) {
                throw new SourceException( "delete: ", e );
            }
            if ( value == null )
                throw new SourceException( " The attribute: " + this.attributeName +
                                           " is empty" );

            JXPathContext context = JXPathContext.newContext( value );
            context.removeAll( this.xPath );
        } else {
            try {
                setOutputAttribute( this.attributeType, this.attributeName, null );
            } catch ( SAXException e ) {
                throw new SourceException( "delete: ", e );
            }
        }
    }

    /**
     * FIXME
     * delete is an operator in java script, this method is for
     * testing puposes in java script only
     */
    public void deleteTest() throws SourceException {
        delete();
    }

    /**
     * Can the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()} be cancelled ?
     *
     * @return true if the stream can be cancelled
     */
    public boolean canCancel( OutputStream stream ) { return false; }

    /**
     * Cancel the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()}.
     * <p>
     * After cancel, the stream should no more be used.
     */
    public void cancel(OutputStream stream) throws IOException {}

    /**
     * Get a <code>ContentHandler</code> where an XML document can
     * be written using SAX events.
     * <p>
     * Care should be taken that the returned handler can actually
     * be a {@link org.apache.cocoon.xml.XMLConsumer} supporting also
     * lexical events such as comments.
     *
     * @return a handler for SAX events
     */
    public ContentHandler getContentHandler() {
        return new DOMBuilder( this );
    }

    public void notify( Document insertDoc ) throws SAXException {

        // handle xpaths, we are only handling inserts, i.e. if there is no
        // attribute of the given name and type the operation will fail
        if ( !(this.xPath.length() == 0 || this.xPath.equals( "/" )) ) {

            Object value = getInputAttribute( this.attributeType, this.attributeName );
            if ( value == null )
                throw new SAXException( " The attribute: " + this.attributeName +
                                        " is empty" );

            JXPathContext context = JXPathContext.newContext( value );

            if ( value instanceof Document ) {
                // If the attribute contains a dom document we
                // create the elements in the given xpath if
                // necesary, import the input document and put it
                // in the place described by the xpath.
                Document doc = (Document)value;
                
                Node importedNode =
                    doc.importNode( insertDoc.getDocumentElement(), true );
                
                context.setLenient( true );
                context.setFactory( new DOMFactory() );
                context.createPathAndSetValue( this.xPath, importedNode );
            } else {
                // Otherwise just try to put a the input document in
                // the place pointed to by the xpath
                context.setValue( this.xPath, insertDoc );
            }
                    
        } else {
            setOutputAttribute( this.attributeType, this.attributeName, insertDoc );
        }
    }

    private class DOMOutputStream extends ByteArrayOutputStream {
        public void close() throws IOException {
            SAXParser parser = null;
            try {
                parser = (SAXParser)XModuleSource.this.manager.lookup( SAXParser.ROLE );

                parser.parse( new InputSource( new ByteArrayInputStream( super.toByteArray() ) ),
                              XModuleSource.this.getContentHandler());
            } catch (Exception e){
                throw new IOException("Exception during processing of " +
                                       XModuleSource.super.getURI() +
                                       e.getMessage());
            } finally {
                if (parser != null) XModuleSource.this.manager.release( parser );
            }
            super.close();
        }
    }


    private Object getInputAttribute( String inputModuleName, String attributeName )
        throws SAXException {
        Object obj;
        ServiceSelector selector = null;
        InputModule inputModule = null;
        try {
            selector = (ServiceSelector) this.manager.lookup( InputModule.ROLE + "Selector" );
            inputModule = (InputModule) selector.select( inputModuleName );
            obj = inputModule.getAttribute( attributeName, null, this.objectModel );

        } catch ( ServiceException e ) {
            throw new SAXException( "Could not find an InputModule of the type " + 
                                    inputModuleName , e );
        } catch ( ConfigurationException e ) {
            throw new SAXException( "Could not find an attribute: " + attributeName +
                                    " from the InputModule " + inputModuleName, e );
        } finally {
            if ( inputModule != null ) selector.release( inputModule );
            this.manager.release( selector );
        }

        return obj;
    }

    private void setOutputAttribute( String outputModuleName,
                                     String attributeName, Object value )
        throws SAXException{
        ServiceSelector selector = null;
        OutputModule outputModule = null;
        try {
            selector = (ServiceSelector) this.manager.lookup( OutputModule.ROLE + "Selector" );
            outputModule = (OutputModule) selector.select( outputModuleName );
            outputModule.setAttribute( null, this.objectModel, attributeName, value );
            outputModule.commit( null, this.objectModel );

        } catch ( ServiceException e ) {
            throw new SAXException( "Could not find an OutputModule of the type " + 
                                    outputModuleName , e );
        } finally {
            if ( outputModule != null ) selector.release( outputModule );
            this.manager.release( selector );
        }
    }
}
