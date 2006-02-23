/*
 * Copyright 2004-2005 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.cocoon.core.Settings;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * A ConfigurationBuilder builds {@link Configuration}s from XML,
 * via a SAX2 compliant parser.
 *
 * <p>
 * The mapping from XML namespaces to {@link Configuration} namespaces is pretty
 * straightforward, with one caveat: attribute namespaces are (deliberately) not
 * supported. Enabling namespace processing has the following effects:</p>
 * <ul>
 *  <li>Attributes starting with <code>xmlns:</code> are interpreted as
 *  declaring a prefix:namespaceURI mapping, and won't result in the creation of
 *  <code>xmlns</code>-prefixed attributes in the <code>Configuration</code>.
 *  </li>
 *  <li>
 *  Prefixed XML elements, like <tt>&lt;doc:title xmlns:doc="http://foo.com"&gt;,</tt>
 *  will result in a <code>Configuration</code> with <code>{@link
 *  Configuration#getName getName()}.equals("title")</code> and <code>{@link
 *  Configuration#getNamespace getNamespace()}.equals("http://foo.com")</code>.
 *  </li>
 * </ul>
 * <p>
 * Whitespace handling. Since mixed content is not allowed in the
 * configurations, whitespace is completely discarded in non-leaf nodes.
 * For the leaf nodes the default behavior is to trim the space
 * surrounding the value. This can be changed by specifying
 * <code>xml:space</code> attribute with value of <code>preserve</code>
 * in that case the whitespace is left intact.
 * </p>
 *
 * @version $Id$
 */
public class ConfigurationBuilder
    extends DefaultHandler
    implements ErrorHandler {
    
    private XMLReader parser;
    
    /**
     * Likely number of nested configuration items. If more is
     * encountered the lists will grow automatically.
     */
    private static final int EXPECTED_DEPTH = 4;
    private final ArrayList elements = new ArrayList( EXPECTED_DEPTH );
    private final ArrayList prefixes = new ArrayList( EXPECTED_DEPTH );
    private final ArrayList values = new ArrayList( EXPECTED_DEPTH );
    
    /**
     * Contains true at index n if space in the configuration with
     * depth n is to be preserved.
     */
    private final BitSet preserveSpace = new BitSet();
    private Configuration configuration;
    private Locator locator;
    private final NamespaceSupport namespaceSupport = new NamespaceSupport();
    private final Settings settings;
    
    /**
     * Create a Configuration Builder
     */
    public ConfigurationBuilder(Settings s) {
        this.settings = s;
        try {
            final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

            saxParserFactory.setNamespaceAware( true );

            final SAXParser saxParser = saxParserFactory.newSAXParser();

            this.parser = saxParser.getXMLReader();

            this.parser.setContentHandler( this );
            this.parser.setErrorHandler( this );
        } catch( final Exception se ) {
            throw new Error( "Unable to setup SAX parser" + se );
        }
    }

    /**
     * Build a configuration object using an InputStream.
     * @param inputStream an <code>InputStream</code> value
     * @return a <code>Configuration</code> object
     * @throws SAXException if a parsing error occurs
     * @throws IOException if an I/O error occurs
     */
    public Configuration build( final InputStream inputStream )
    throws SAXException, IOException {
        return this.build( new InputSource( inputStream ) );
    }

    /**
     * Build a configuration object using an InputStream;
     * supplying a systemId to make messages about all
     * kinds of errors more meaningfull.
     * @param inputStream an <code>InputStream</code> value
     * @param systemId the systemId to set on the intermediate sax
     *         inputSource
     * @return a <code>Configuration</code> object
     * @throws SAXException if a parsing error occurs
     * @throws IOException if an I/O error occurs
     */
    public Configuration build( final InputStream inputStream, 
                                final String systemId )
    throws SAXException, IOException {
        final InputSource inputSource = new InputSource( inputStream );
        inputSource.setSystemId( systemId );
        return this.build( inputSource );
    }

    /**
     * Build a configuration object using an URI
     * @param uri a <code>String</code> value
     * @return a <code>Configuration</code> object
     * @throws SAXException if a parsing error occurs
     * @throws IOException if an I/O error occurs
     */
    public Configuration build( final String uri )
    throws SAXException, IOException {
        return this.build( new InputSource( uri ) );
    }

    /**
     * Build a configuration object using an XML InputSource object
     * @param input an <code>InputSource</code> value
     * @return a <code>Configuration</code> object
     * @throws SAXException if a parsing error occurs
     * @throws IOException if an I/O error occurs
     */
    public Configuration build( final InputSource input )
    throws SAXException, IOException {
        synchronized( this ) {
            this.clear();
            this.parser.parse( input );
            return this.configuration;
        }
    }

    /**
     * Sets the <code>EntityResolver</code> to 
     * be used by parser. Useful when dealing with xml
     * files that reference external entities.
     * 
     * @param resolver implementation of <code>EntityResolver</code>
     */
    public void setEntityResolver( final EntityResolver resolver ) {
        this.parser.setEntityResolver( resolver );
    }

    /**
     * Clears all data from this configuration handler.
     */
    protected void clear() {
        this.elements.clear();
        Iterator i = this.prefixes.iterator();
        while( i.hasNext() ) {
            ( (ArrayList)i.next() ).clear();
        }
        this.prefixes.clear();
        this.values.clear();
        this.locator = null;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator( final Locator locator ) {
        this.locator = locator;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument()
    throws SAXException {
        this.namespaceSupport.reset();
        super.startDocument();
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument()
    throws SAXException {
        super.endDocument();
        this.namespaceSupport.reset();
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters( final char[] ch, int start, int end )
    throws SAXException {
        // it is possible to play micro-optimization here by doing
        // manual trimming and thus preserve some precious bits
        // of memory, but it's really not important enough to justify
        // resulting code complexity
        final int depth = this.values.size() - 1;
        final StringBuffer valueBuffer = (StringBuffer)this.values.get( depth );
        valueBuffer.append( ch, start, end );
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement( final String namespaceURI,
                            final String localName,
                            final String rawName )
    throws SAXException {
        final int depth = this.elements.size() - 1;
        final DefaultConfiguration finishedConfiguration =
        (DefaultConfiguration)this.elements.remove( depth );
        final String accumulatedValue =
        ( (StringBuffer)this.values.remove( depth ) ).toString();
        final ArrayList prefixes = (ArrayList)this.prefixes.remove( depth );

        final Iterator i = prefixes.iterator();
        while( i.hasNext() ) {
            endPrefixMapping( (String)i.next() );
        }
        prefixes.clear();

        if( finishedConfiguration.getChildren().length == 0 ) {
            // leaf node
            String finishedValue;
            if( this.preserveSpace.get( depth ) ) {
                finishedValue = accumulatedValue;
            } else if( accumulatedValue.length() == 0 ) {
                finishedValue = null;
            } else {
                finishedValue = accumulatedValue.trim();
            }
            finishedConfiguration.setValue( PropertyHelper.replace(finishedValue, this.settings) );
        } else {
            final String trimmedValue = accumulatedValue.trim();
            if( trimmedValue.length() > 0 ) {
                throw new SAXException( "Not allowed to define mixed content in the " 
                        + "element " + finishedConfiguration.getName() + " at "
                        + finishedConfiguration.getLocation() );
            }
        }

        if( depth == 0 ) {
            this.configuration = finishedConfiguration;
        }
        this.namespaceSupport.popContext();
    }

    /**
     * Create a new <code>DefaultConfiguration</code> with the specified
     * local name, namespace, and location.
     *
     * @param localName a <code>String</code> value
     * @param namespaceURI a <code>String</code> value
     * @param location a <code>String</code> value
     * @return a <code>DefaultConfiguration</code> value
     */
    protected DefaultConfiguration createConfiguration( final String localName,
                                                        final String namespaceURI,
                                                        final String location ) {
        String prefix = this.namespaceSupport.getPrefix( namespaceURI );
        if( prefix == null ) {
            prefix = "";
        }
        return new DefaultConfiguration( localName, location, namespaceURI, prefix );
    }


    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement( final String namespaceURI,
                              final String localName,
                              final String rawName,
                              final Attributes attributes )
    throws SAXException {
        this.namespaceSupport.pushContext();
        final DefaultConfiguration configuration =
            createConfiguration( localName, namespaceURI, getLocationString() );
        // depth of new configuration (not decrementing here, configuration
        // is to be added)
        final int depth = this.elements.size();
        boolean preserveSpace = false; // top level element trims space by default

        if( depth > 0 ) {
            final DefaultConfiguration parent =
                (DefaultConfiguration)this.elements.get( depth - 1 );
            parent.addChild( configuration );
            // inherits parent's space preservation policy
            preserveSpace = this.preserveSpace.get( depth - 1 );
        }

        this.elements.add( configuration );
        this.values.add( new StringBuffer() );

        final ArrayList prefixes = new ArrayList();
        AttributesImpl componentAttr = new AttributesImpl();

        for( int i = 0; i < attributes.getLength(); i++ ) {
            if( attributes.getQName( i ).startsWith( "xmlns" ) ) {
                prefixes.add( attributes.getLocalName( i ) );
                this.startPrefixMapping( attributes.getLocalName( i ),
                                         attributes.getValue( i ) );
            } else if( attributes.getQName( i ).equals( "xml:space" ) ) {
                preserveSpace = attributes.getValue( i ).equals( "preserve" );
            } else {
                componentAttr.addAttribute( attributes.getURI( i ),
                                            attributes.getLocalName( i ),
                                            attributes.getQName( i ),
                                            attributes.getType( i ),
                                            attributes.getValue( i ) );
            }
        }

        if( preserveSpace ) {
            this.preserveSpace.set( depth );
        } else {
            this.preserveSpace.clear( depth );
        }

        this.prefixes.add( prefixes );

        final int attributesSize = componentAttr.getLength();

        for( int i = 0; i < attributesSize; i++ ) {
            final String name = componentAttr.getQName( i );
            final String value = componentAttr.getValue( i );
            configuration.setAttribute( name, PropertyHelper.replace(value, this.settings) );
        }
    }


    /* (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error( final SAXParseException exception )
    throws SAXException {
        throw exception;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning( final SAXParseException exception )
    throws SAXException {
        throw exception;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError( final SAXParseException exception )
    throws SAXException {
        throw exception;
    }

    /**
     * Returns a string showing the current system ID, line number and column number.
     *
     * @return a <code>String</code> value
     */
    private String getLocationString() {
        if( this.locator == null ) {
            return "Unknown";
        } 
        final int columnNumber = this.locator.getColumnNumber();
        return this.locator.getSystemId() + ":"
            + this.locator.getLineNumber()
            + ( columnNumber >= 0 ? ( ":" + columnNumber ) : "" );
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    public void startPrefixMapping( String prefix, String uri )
    throws SAXException {
        this.namespaceSupport.declarePrefix( prefix, uri );
        super.startPrefixMapping( prefix, uri );
    }

}
