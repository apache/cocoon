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
package org.apache.cocoon.transformation;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.*;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.Tokenizer;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import javax.xml.transform.OutputKeys;

/**
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @author <a href="mailto:giacomo.pati@pwr.ch">Giacomo Pati</a>
 *         (PWR Organisation & Entwicklung)
 * @author <a href="mailto:sven.beauprez@the-ecorp.com">Sven Beauprez</a>
 * @version CVS $Id: SQLTransformer.java,v 1.5 2003/03/24 14:33:56 stefano Exp $
 */
public class SQLTransformer
  extends AbstractSAXTransformer
  implements Composable, Disposable, Configurable {

    /** The SQL namespace **/
    public static final String NAMESPACE = "http://apache.org/cocoon/SQL/2.0";

    /** The SQL namespace element names **/
    public static final String MAGIC_EXECUTE_QUERY = "execute-query";
    public static final String MAGIC_CONNECTION = "use-connection";
    public static final String MAGIC_DBURL = "dburl";
    public static final String MAGIC_USERNAME = "username";
    public static final String MAGIC_PASSWORD = "password";
    public static final String MAGIC_NR_OF_ROWS = "show-nr-of-rows";
    public static final String MAGIC_QUERY = "query";
    public static final String MAGIC_VALUE = "value";
    public static final String MAGIC_DOC_ELEMENT = "doc-element";
    public static final String MAGIC_ROW_ELEMENT = "row-element";
    public static final String MAGIC_IN_PARAMETER = "in-parameter";
    public static final String MAGIC_IN_PARAMETER_NR_ATTRIBUTE = "nr";
    public static final String MAGIC_IN_PARAMETER_VALUE_ATTRIBUTE = "value";
    public static final String MAGIC_OUT_PARAMETER = "out-parameter";
    public static final String MAGIC_OUT_PARAMETER_NAME_ATTRIBUTE = "name";
    public static final String MAGIC_OUT_PARAMETER_NR_ATTRIBUTE = "nr";
    public static final String MAGIC_OUT_PARAMETER_TYPE_ATTRIBUTE = "type";
    public static final String MAGIC_ESCAPE_STRING = "escape-string";
    public static final String MAGIC_ERROR = "error";

    public static final String MAGIC_NS_URI_ELEMENT = "namespace-uri";
    public static final String MAGIC_NS_PREFIX_ELEMENT = "namespace-prefix";

    public static final String MAGIC_ANCESTOR_VALUE = "ancestor-value";
    public static final String MAGIC_ANCESTOR_VALUE_LEVEL_ATTRIBUTE = "level";
    public static final String MAGIC_ANCESTOR_VALUE_NAME_ATTRIBUTE = "name";
    public static final String MAGIC_SUBSTITUTE_VALUE = "substitute-value";
    public static final String MAGIC_SUBSTITUTE_VALUE_NAME_ATTRIBUTE = "name";
    public static final String MAGIC_NAME_ATTRIBUTE = "name";
    public static final String MAGIC_STORED_PROCEDURE_ATTRIBUTE = "isstoredprocedure";
    public static final String MAGIC_UPDATE_ATTRIBUTE = "isupdate";

    /** The states we are allowed to be in **/
    protected static final int STATE_OUTSIDE = 0;
    protected static final int STATE_INSIDE_EXECUTE_QUERY_ELEMENT = 1;
    protected static final int STATE_INSIDE_VALUE_ELEMENT = 2;
    protected static final int STATE_INSIDE_QUERY_ELEMENT = 3;
    protected static final int STATE_INSIDE_ANCESTOR_VALUE_ELEMENT = 4;
    protected static final int STATE_INSIDE_SUBSTITUTE_VALUE_ELEMENT = 5;
    protected static final int STATE_INSIDE_IN_PARAMETER_ELEMENT = 6;
    protected static final int STATE_INSIDE_OUT_PARAMETER_ELEMENT = 7;
    protected static final int STATE_INSIDE_ESCAPE_STRING = 8;

    //
    // Configuration
    //

    /** Is the old-driver turned on? (default is off) */
    private boolean oldDriver = false;

    /** How many connection attempts to do? (default is 5 times) */
    private int connectAttempts = 5;

    /** How long wait between connection attempts? (default is 5000 ms) */
    private int connectWaittime = 5;

    //
    // State
    //

    /** The list of queries that we're currently working on **/
    protected Vector queries;

    /** The offset of the current query in the queries list **/
    protected int current_query_index;

    /** The current state of the event receiving FSM **/
    protected int current_state;

    /** Check if nr of rows need to be written out. **/
    protected boolean showNrOfRows;

    /** Namespace prefix to output */
    protected String outPrefix;

    /** Namespace uri to output */
    protected String outUri;

    /** The database selector */
    protected ComponentSelector dbSelector;

    /** The format for serializing xml */
    protected Properties format;

    protected XMLSerializer compiler;
    protected XMLDeserializer interpreter;
    protected SAXParser parser;

    /**
     * Constructor
     */
    public SQLTransformer() {
        // FIXME (CZ) We have to get the correct encoding from
        // somewhere else (XML Serializer?)
        this.format = new Properties();
        this.format.put(OutputKeys.METHOD, "text");
        this.format.put(OutputKeys.ENCODING, "ISO-8859-1");
        this.format.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
        this.namespaceURI = NAMESPACE;
    }

    /**
     * Composable
     */
    public void compose( ComponentManager manager ) throws ComponentException {
        super.compose(manager);
        this.queries = new Vector();
        try {
            this.dbSelector = (ComponentSelector) manager.lookup( DataSourceComponent.ROLE + "Selector" );
        } catch ( ComponentException cme ) {
            getLogger().warn( "Could not get the DataSource Selector", cme );
        }
    }

    /**
     * Recycle this component
     */
    public void recycle() {
        super.recycle();
        this.queries.clear();
        this.outUri = null;
        this.outPrefix = null;
        this.manager.release((Component)this.parser);
        this.parser = null;
        this.manager.release(this.compiler);
        this.compiler = null;
        this.manager.release(this.interpreter);
        this.interpreter = null;
    }

    /**
     * dispose
     */
    public void dispose() {
        if ( this.dbSelector != null ) {
            this.manager.release( this.dbSelector );
            this.dbSelector = null;
        }
    }

    /**
     * configure
     */
    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);

        this.oldDriver = conf.getChild("old-driver").getValueAsBoolean(false);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("old-driver is " + this.oldDriver + " for " + this);
        }

        this.connectAttempts = conf.getChild("connect-attempts").getValueAsInteger(5);
        this.connectWaittime = conf.getChild("connect-waittime").getValueAsInteger(5000);
    }

    /**
     * Setup for the current request
     */
    public void setup( SourceResolver resolver, Map objectModel,
                       String source, Parameters parameters )
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, source, parameters);
        // setup instance variables
        this.current_query_index = -1;
        this.current_state = SQLTransformer.STATE_OUTSIDE;

        this.showNrOfRows = parameters.getParameterAsBoolean( SQLTransformer.MAGIC_NR_OF_ROWS, false );
        if ( getLogger().isDebugEnabled() ) {
            if ( this.parameters.getParameter( SQLTransformer.MAGIC_CONNECTION , null ) != null ) {
                getLogger().debug( "CONNECTION: " + this.parameters.getParameter( SQLTransformer.MAGIC_CONNECTION , null ) );
            } else {
                getLogger().debug( "DBURL: " + parameters.getParameter( SQLTransformer.MAGIC_DBURL, null ) );
                getLogger().debug( "USERNAME: " + parameters.getParameter( SQLTransformer.MAGIC_USERNAME, null ) );
            }
            getLogger().debug( "DOC-ELEMENT: " + parameters.getParameter( SQLTransformer.MAGIC_DOC_ELEMENT, "rowset" ) );
            getLogger().debug( "ROW-ELEMENT: " + parameters.getParameter( SQLTransformer.MAGIC_ROW_ELEMENT, "row" ) );
            getLogger().debug( "NS-URI: " + parameters.getParameter( SQLTransformer.MAGIC_NS_URI_ELEMENT, NAMESPACE ) );
            getLogger().debug( "NS-PREFIX: " + parameters.getParameter( SQLTransformer.MAGIC_NS_PREFIX_ELEMENT, "" ) );
        }
   }

    /**
     * This will be the meat of SQLTransformer, where the query is run.
     */
    protected void executeQuery( int index )
    throws SAXException {
        if ( getLogger().isDebugEnabled() ) {
            getLogger().debug( "SQLTransformer executing query nr " + index );
        }

        this.outUri = getCurrentQuery().properties.getParameter( SQLTransformer.MAGIC_NS_URI_ELEMENT, NAMESPACE );
        this.outPrefix = getCurrentQuery().properties.getParameter( SQLTransformer.MAGIC_NS_PREFIX_ELEMENT, "" );

        if ( !"".equals( this.outUri ) ) {
            super.startPrefixMapping( this.outPrefix, this.outUri );
        }

        AttributesImpl attr = new AttributesImpl();
        Query query = (Query) queries.elementAt( index );
        boolean query_failure = false;
        try {
            try {
                query.execute();
            } catch ( SQLException e ) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug( "SQLTransformer:.executeQuery() query.execute failed ", e );
                }
                AttributesImpl my_attr = new AttributesImpl();
                this.start( query.rowset_name, my_attr );
                this.start( MAGIC_ERROR, my_attr);
                this.data( e.getMessage());
                this.end( MAGIC_ERROR );
                this.end( query.rowset_name );
                query_failure = true;
            }
            if ( !query_failure ) {

                if ( this.showNrOfRows ) {
                    attr.addAttribute( NAMESPACE, query.nr_of_rows, query.nr_of_rows, "CDATA",
                       String.valueOf( query.getNrOfRows() ) );
                }
                String name = query.getName();
                if ( name != null ) {
                    attr.addAttribute( NAMESPACE, query.name_attribute, query.name_attribute, "CDATA",
                       name );
                }
                this.start( query.rowset_name, attr );
                attr = new AttributesImpl();

                if ( !query.isStoredProcedure() ) {
                    while ( query.next() ) {
                        this.start( query.row_name, attr );
                        query.serializeRow(this.manager);
                        if ( index + 1 < queries.size() ) {
                            executeQuery( index + 1 );
                        }
                        this.end( query.row_name );
                    }
                } else {
                    query.serializeStoredProcedure(this.manager);
                }

                this.end( query.rowset_name );
            }
        } catch ( SQLException e ) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug( "SQLTransformer.executeQuery()", e );
            }
            throw new SAXException( e );
        } finally {
            try {
                query.close();
            } catch ( SQLException e ) {
                getLogger().warn( "SQLTransformer: Could not close JDBC connection", e );
            }
        }
        if ( !"".equals( this.outUri ) ) {
            super.endPrefixMapping( this.outPrefix );
        }
    }

    protected static void throwIllegalStateException( String message ) {
        throw new IllegalStateException( "SQLTransformer: " + message );
    }

    protected void startExecuteQueryElement() {
        switch ( current_state ) {
            case SQLTransformer.STATE_OUTSIDE:
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                current_query_index = queries.size();
                Query query = new Query( this, current_query_index );
                queries.addElement( query );
                current_state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
                break;
            default:
                throwIllegalStateException( "Not expecting a start execute query element" );
        }
    }

    protected void startValueElement( String name )
    throws SAXException {
        switch ( current_state ) {
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                this.stack.push(name);
                this.startTextRecording();
                current_state = SQLTransformer.STATE_INSIDE_VALUE_ELEMENT;
                break;
            default:
                throwIllegalStateException( "Not expecting a start value element: " +
                                            name );
        }
    }

    protected void startQueryElement( Attributes attributes )
    throws SAXException {
        switch ( current_state ) {
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                this.startSerializedXMLRecording(format);
                Query q = getCurrentQuery();
                current_state = SQLTransformer.STATE_INSIDE_QUERY_ELEMENT;
                String isupdate =
                        attributes.getValue( "", SQLTransformer.MAGIC_UPDATE_ATTRIBUTE );
                if ( isupdate != null && !isupdate.equalsIgnoreCase( "false" ) )
                    q.setUpdate( true );
                String isstoredprocedure =
                        attributes.getValue( "", SQLTransformer.MAGIC_STORED_PROCEDURE_ATTRIBUTE );
                if ( isstoredprocedure != null && !isstoredprocedure.equalsIgnoreCase( "false" ) )
                    q.setStoredProcedure( true );
                String name =
                        attributes.getValue( "", SQLTransformer.MAGIC_NAME_ATTRIBUTE );
                if ( name != null ) {
                    q.setName( name );
                }
                break;
            default:
                throwIllegalStateException( "Not expecting a start query element" );
        }
    }

    protected void endQueryElement()
    throws ProcessingException, SAXException {
        switch ( current_state ) {
            case SQLTransformer.STATE_INSIDE_QUERY_ELEMENT:
                final String value = this.endSerializedXMLRecording();
                if ( value.length() > 0 ) {
                    this.getCurrentQuery().addQueryPart( value );
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug( "QUERY IS \"" + value + "\"" );
                    }
                }
                current_state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
                break;
            default:
                throwIllegalStateException( "Not expecting a stop query element" );
        }
    }

    protected void endValueElement()
    throws SAXException {
        switch ( current_state ) {
            case SQLTransformer.STATE_INSIDE_VALUE_ELEMENT:
                final String name = (String)this.stack.pop();
                final String value = this.endTextRecording();
                this.getCurrentQuery().setParameter(name, value);
                this.current_state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug( "SETTING VALUE ELEMENT name {" +
                                   name + "} value {" + value + "}" );
                }
                break;
            default:
                throwIllegalStateException( "Not expecting an end value element" );
        }
    }

    protected void endExecuteQueryElement() throws SAXException {
        switch ( current_state ) {
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                if ( current_query_index == 0 ) {
                    executeQuery( 0 );
                    queries.removeAllElements();
                    current_state = SQLTransformer.STATE_OUTSIDE;
                } else {
                    current_query_index--;
                    current_state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
                }
                break;
            default:
                throwIllegalStateException( "Not expecting an end execute query element" );
        }
    }

    protected void startAncestorValueElement( Attributes attributes )
    throws ProcessingException, SAXException {
        switch ( current_state ) {
            case SQLTransformer.STATE_INSIDE_QUERY_ELEMENT:
                int level = 0;
                try {
                    level = Integer.parseInt( attributes.getValue( NAMESPACE,
                                                                   SQLTransformer.MAGIC_ANCESTOR_VALUE_LEVEL_ATTRIBUTE ) );
                } catch ( Exception e ) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug( "SQLTransformer", e );
                    }
                    throwIllegalStateException( "Ancestor value elements must have a " +
                                                SQLTransformer.MAGIC_ANCESTOR_VALUE_LEVEL_ATTRIBUTE + " attribute" );
                }
                String name = attributes.getValue( NAMESPACE,
                                                   SQLTransformer.MAGIC_ANCESTOR_VALUE_NAME_ATTRIBUTE );
                if ( name == null ) {
                    throwIllegalStateException( "Ancestor value elements must have a " +
                                                SQLTransformer.MAGIC_ANCESTOR_VALUE_NAME_ATTRIBUTE + " attribute" );
                }
                AncestorValue av = new AncestorValue( level, name );
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug( "ANCESTOR VALUE " + level + " " + name );
                }

                final String value = this.endSerializedXMLRecording();
                if ( value.length() > 0 ) {
                    this.getCurrentQuery().addQueryPart( value );
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug( "QUERY IS \"" + value + "\"" );
                    }
                }
                getCurrentQuery().addQueryPart( av );
                this.startSerializedXMLRecording(format);

                current_state = SQLTransformer.STATE_INSIDE_ANCESTOR_VALUE_ELEMENT;
                break;
            default:
                throwIllegalStateException( "Not expecting a start ancestor value element" );
        }
    }

    protected void endAncestorValueElement() {
        current_state = SQLTransformer.STATE_INSIDE_QUERY_ELEMENT;
    }

    protected void startSubstituteValueElement( Attributes attributes )
    throws ProcessingException, SAXException {
        switch ( current_state ) {
            case SQLTransformer.STATE_INSIDE_QUERY_ELEMENT:
                String name = attributes.getValue( NAMESPACE,
                                                   SQLTransformer.MAGIC_SUBSTITUTE_VALUE_NAME_ATTRIBUTE );
                if ( name == null ) {
                    throwIllegalStateException( "Substitute value elements must have a " +
                                                SQLTransformer.MAGIC_SUBSTITUTE_VALUE_NAME_ATTRIBUTE + " attribute" );
                }
                String substitute = parameters.getParameter( name, null );
                //escape single quote
                substitute = replaceCharWithString( substitute, '\'', "''" );
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug( "SUBSTITUTE VALUE " + substitute );
                }
                final String value = this.endSerializedXMLRecording();
                if ( value.length() > 0 ) {
                    this.getCurrentQuery().addQueryPart( value );
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug( "QUERY IS \"" + value + "\"" );
                    }
                }
                this.getCurrentQuery().addQueryPart( substitute );
                this.startSerializedXMLRecording(format);

                current_state = SQLTransformer.STATE_INSIDE_SUBSTITUTE_VALUE_ELEMENT;
                break;
            default:
                throwIllegalStateException( "Not expecting a start substitute value element" );
        }
    }

    protected void endSubstituteValueElement() {
        current_state = SQLTransformer.STATE_INSIDE_QUERY_ELEMENT;
    }

    protected void startEscapeStringElement( Attributes attributes )
    throws ProcessingException, SAXException {
        switch ( current_state ) {
            case SQLTransformer.STATE_INSIDE_QUERY_ELEMENT:
                final String value = this.endSerializedXMLRecording();
                if ( value.length() > 0 ) {
                    this.getCurrentQuery().addQueryPart( value );
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug( "QUERY IS \"" + value + "\"" );
                    }
                }
                this.startTextRecording();

                current_state = SQLTransformer.STATE_INSIDE_ESCAPE_STRING;
                break;
            default:
                throwIllegalStateException( "Not expecting a start escape-string element" );
        }
    }

    protected void endEscapeStringElement()
    throws SAXException {
        switch ( current_state) {
        case SQLTransformer.STATE_INSIDE_ESCAPE_STRING:
            String value = this.endTextRecording();
            if ( value.length() > 0 ) {
                value = replaceCharWithString( value, '\'', "''" );
                value = replaceCharWithString( value, '\\', "\\\\" );
                this.getCurrentQuery().addQueryPart( value );
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug( "QUERY IS \"" + value + "\"" );
                }
            }
            this.startSerializedXMLRecording(format);
            current_state = SQLTransformer.STATE_INSIDE_QUERY_ELEMENT;
            break;
        default:
                throwIllegalStateException( "Not expecting a end escape-string element" );
        }
    }

    protected void startInParameterElement( Attributes attributes ) {
        switch ( current_state ) {
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                String nr = attributes.getValue( NAMESPACE,
                                                 SQLTransformer.MAGIC_IN_PARAMETER_NR_ATTRIBUTE );
                String value = attributes.getValue( NAMESPACE,
                                                    SQLTransformer.MAGIC_IN_PARAMETER_VALUE_ATTRIBUTE );
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug( "IN PARAMETER NR " + nr + "; VALUE " + value );
                }
                int position = Integer.parseInt( nr );
                getCurrentQuery().setInParameter( position, value );
                current_state = SQLTransformer.STATE_INSIDE_IN_PARAMETER_ELEMENT;
                break;
            default:
                throwIllegalStateException( "Not expecting an in-parameter element" );
        }
    }

    protected void endInParameterElement() {
        current_state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
    }

    protected void startOutParameterElement( Attributes attributes ) {
        switch ( current_state ) {
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                String name = attributes.getValue( NAMESPACE,
                                                   SQLTransformer.MAGIC_OUT_PARAMETER_NAME_ATTRIBUTE );
                String nr = attributes.getValue( NAMESPACE,
                                                 SQLTransformer.MAGIC_OUT_PARAMETER_NR_ATTRIBUTE );
                String type = attributes.getValue( NAMESPACE,
                                                   SQLTransformer.MAGIC_OUT_PARAMETER_TYPE_ATTRIBUTE );
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug( "OUT PARAMETER NAME" + name + ";NR " + nr + "; TYPE " + type );
                }
                int position = Integer.parseInt( nr );
                getCurrentQuery().setOutParameter( position, type, name );
                current_state = SQLTransformer.STATE_INSIDE_OUT_PARAMETER_ELEMENT;
                break;
            default:
                throwIllegalStateException( "Not expecting an out-parameter element" );
        }
    }

    protected void endOutParameterElement() {
        current_state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
    }

    protected Query getCurrentQuery() {
        return (Query) queries.elementAt( current_query_index );
    }

    protected Query getQuery( int i ) {
        return (Query) queries.elementAt( i );
    }

    private String replaceCharWithString( String in, char c, String with ) {
        Tokenizer tok;
        StringBuffer replaced = null;
        if ( in.indexOf( c ) > -1 ) {
            tok = new Tokenizer( in, c );
            replaced = new StringBuffer();
            while ( tok.hasMoreTokens() ) {
                replaced.append( tok.nextToken() );
                if ( tok.hasMoreTokens() )
                    replaced.append( with );
            }
        }
        if ( replaced != null ) {
            return replaced.toString();
        } else {
            return in;
        }
    }

    /**
     * Qualifies an element name by giving it a prefix.
     * @param name the element name
     * @param prefix the prefix to qualify with
     * @return a namespace qualified name that is correct
     */
    protected String nsQualify( String name, String prefix ) {
        if ( name == null || "".equals( name ) ) {
            return name;
        }
        if ( prefix != null && !"".equals( prefix ) ) {
            return new StringBuffer( prefix ).append( ":" ).append( name ).toString();
        } else {
            return name;
        }
    }

    /**
     * ContentHandler method
     */
    public void setDocumentLocator( Locator locator ) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug( "PUBLIC ID: " + locator.getPublicId() );
            getLogger().debug( "SYSTEM ID: " + locator.getSystemId() );
        }
        super.setDocumentLocator( locator );
    }

    /**
     * ContentHandler method
     */
    public void startTransformingElement( String uri, String name, String raw,
                              Attributes attributes )
    throws ProcessingException, SAXException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug( "RECEIVED START ELEMENT " + name );
        }

        if ( name.equals( SQLTransformer.MAGIC_EXECUTE_QUERY ) ) {
            this.startExecuteQueryElement();
        } else if ( name.equals( SQLTransformer.MAGIC_QUERY ) ) {
            this.startQueryElement( attributes );
        } else if ( name.equals( SQLTransformer.MAGIC_ANCESTOR_VALUE ) ) {
            this.startAncestorValueElement( attributes );
        } else if ( name.equals( SQLTransformer.MAGIC_SUBSTITUTE_VALUE ) ) {
            this.startSubstituteValueElement( attributes );
        } else if ( name.equals( SQLTransformer.MAGIC_IN_PARAMETER ) ) {
            this.startInParameterElement( attributes );
        } else if ( name.equals( SQLTransformer.MAGIC_OUT_PARAMETER ) ) {
            this.startOutParameterElement( attributes );
        } else if ( name.equals( SQLTransformer.MAGIC_ESCAPE_STRING ) ) {
            this.startEscapeStringElement( attributes );
        } else {
            this.startValueElement( name );
        }
    }

    /**
     * ContentHandler method
     */
    public void endTransformingElement( String uri, String name,
                            String raw )
    throws ProcessingException, IOException, SAXException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug( "RECEIVED END ELEMENT " + name + "(" + uri + ")" );
        }

        if ( name.equals( SQLTransformer.MAGIC_EXECUTE_QUERY ) ) {
            this.endExecuteQueryElement();
        } else if ( name.equals( SQLTransformer.MAGIC_QUERY ) ) {
            this.endQueryElement();
        } else if ( name.equals( SQLTransformer.MAGIC_ANCESTOR_VALUE ) ) {
            this.endAncestorValueElement();
        } else if ( name.equals( SQLTransformer.MAGIC_SUBSTITUTE_VALUE ) ) {
            this.endSubstituteValueElement();
        } else if ( name.equals( SQLTransformer.MAGIC_IN_PARAMETER ) ) {
            this.endInParameterElement();
        } else if ( name.equals( SQLTransformer.MAGIC_OUT_PARAMETER ) ) {
            this.endOutParameterElement();
        } else if ( name.equals( SQLTransformer.MAGIC_VALUE )
                   || current_state == SQLTransformer.STATE_INSIDE_VALUE_ELEMENT ) {
            this.endValueElement();
        } else  if ( name.equals( SQLTransformer.MAGIC_ESCAPE_STRING ) ) {
            this.endEscapeStringElement();
        } else {
            super.endTransformingElement( uri, name, raw );
        }
    }

    /**
     * Helper method for generating SAX events
     */
    private void start( String name, AttributesImpl attr )
    throws SAXException {
        try {
            super.startTransformingElement( outUri, name, nsQualify( name, outPrefix ), attr );
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        } catch (ProcessingException pe) {
            throw new SAXException(pe);
        }
        attr.clear();
    }

    /**
     * Helper method for generating SAX events
     */
    private void end( String name ) throws SAXException {
        try {
            super.endTransformingElement( outUri, name, nsQualify( name, outPrefix ) );
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        } catch (ProcessingException pe) {
            throw new SAXException(pe);
        }
    }

    /**
     * Helper method for generating SAX events
     */
    private void data( String data ) throws SAXException {
        if ( data != null ) {
            super.characters( data.toCharArray(), 0, data.length() );
        }
    }

    protected static String getStringValue( Object object ) {
        if ( object instanceof byte[] ) {
            return new String( (byte[]) object );
        } else if ( object instanceof char[] ) {
            return new String( (char[]) object );
        } else if ( object != null ) {
            return object.toString();
        } else {
            return "";
        }
    }

    public final Logger getTheLogger() {
        return getLogger();
    }


    class Query {

        /** Who's your daddy? **/
        protected SQLTransformer transformer;

        /** What index are you in daddy's queries list **/
        protected int query_index;

        /** SQL configuration information **/
        protected Parameters properties;

        /** Dummy static variables for the moment **/
        protected String rowset_name;
        protected String row_name;
        protected String nr_of_rows = "nrofrows";
        protected String name_attribute = "name";

        /** The connection, once opened **/
        protected Connection conn;

        /** And the statements **/
        protected PreparedStatement pst;
        protected CallableStatement cst;

        /** The results, of course **/
        protected ResultSet rs = null;

        /** And the results' metadata **/
        protected ResultSetMetaData md = null;

        /** If this query is actually an update (insert, update, delete) **/
        protected boolean isupdate = false;

        /** If this query is actually a stored procedure **/
        protected boolean isstoredprocedure = false;

        protected String name = null;

        /** If it is an update/etc, the return value (num rows modified) **/
        protected int rv = -1;

        /** The parts of the query **/
        protected Vector query_parts = new Vector();

        /** In parameters **/
        protected HashMap inParameters = null;

        /** Out parameters **/
        protected HashMap outParameters = null;

        /** Mapping out parameters - objectModel **/
        protected HashMap outParametersNames = null;

        protected Query( SQLTransformer transformer, int query_index ) {
            this.transformer = transformer;
            this.query_index = query_index;
            this.properties = new Parameters();
            this.properties.merge( transformer.parameters );
        }

        protected void setParameter( String name, String value ) {
            properties.setParameter( name, value );
        }

        protected void setUpdate( boolean flag ) {
            isupdate = flag;
        }

        protected void setStoredProcedure( boolean flag ) {
            isstoredprocedure = flag;
        }

        protected boolean isStoredProcedure() {
            return isstoredprocedure;
        }

        protected void setName( String name ) {
            this.name = name;
        }

        protected String getName() {
            return name;
        }

        protected void setInParameter( int pos, String val ) {
            if ( inParameters == null ) {
                inParameters = new HashMap();
            }
            inParameters.put( new Integer( pos ), val );
        }

        protected void setOutParameter( int pos, String type, String name ) {
            if ( outParameters == null ) {
                outParameters = new HashMap();
                outParametersNames = new HashMap();
            }
            outParameters.put( new Integer( pos ), type );
            outParametersNames.put( new Integer( pos ), name );
        }

        private void registerInParameters( PreparedStatement pst ) throws SQLException {
            if ( inParameters == null )
                return;
            Iterator itInKeys = inParameters.keySet().iterator();
            Integer counter;
            String value;
            while ( itInKeys.hasNext() ) {
                counter = (Integer) itInKeys.next();
                value = (String) inParameters.get( counter );
                try {
                    pst.setObject( counter.intValue(), value );
                } catch ( SQLException e ) {
                    transformer.getTheLogger().error( "Caught a SQLException", e );
                    throw e;
                }
            }
        }

        private void registerOutParameters( CallableStatement cst ) throws SQLException {
            if ( outParameters == null )
                return;
            Iterator itOutKeys = outParameters.keySet().iterator();
            Integer counter;
            int index;
            String type, className, fieldName;
            Class clss;
            Field fld;
            while ( itOutKeys.hasNext() ) {
                counter = (Integer) itOutKeys.next();
                type = (String) outParameters.get( counter );
                index = type.lastIndexOf( "." );
                if ( index > -1 ) {
                    className = type.substring( 0, index );
                    fieldName = type.substring( index + 1, type.length() );
                } else {
                    transformer.getTheLogger().error( "Invalid SQLType: " + type, null );
                    throw new SQLException( "Invalid SQLType: " + type);
                }
                try {
                    clss = Class.forName( className );
                    fld = clss.getField( fieldName );
                    cst.registerOutParameter( counter.intValue(), fld.getInt( fieldName ) );
                } catch ( Exception e ) {
                    //lots of different exceptions to catch
                    transformer.getTheLogger().error( "Invalid SQLType: " +
                                                      className + "." + fieldName, e );
                }
            }
        }

        /** Get a Connection. Made this a separate method to separate the logic from the actual execution. */
        protected Connection getConnection() throws SQLException {
            Connection result = null;

            try {
                final String connection = properties.getParameter( SQLTransformer.MAGIC_CONNECTION, null );
                if ( connection != null ) {
                    if (this.transformer.dbSelector == null) {
                        transformer.getTheLogger().error( "No DBSelector found, could not use connection: " + connection);
                    } else {
                        DataSourceComponent datasource = null;

                        try {
                            datasource = (DataSourceComponent) this.transformer.dbSelector.select( connection );
                            for ( int i = 0; i < transformer.connectAttempts && result == null; i++) {
                                try {
                                    result = datasource.getConnection();
                                } catch ( Exception e ) {
                                    final long waittime = transformer.connectWaittime;
                                    transformer.getTheLogger().debug(
                                            "SQLTransformer$Query: could not acquire a Connection -- waiting "
                                            + waittime + " ms to try again." );
                                    try {
                                        Thread.sleep( waittime );
                                    } catch ( InterruptedException ie ) {
                                    }
                                }
                            }
                        } catch ( ComponentException cme ) {
                             transformer.getTheLogger().error( "Could not use connection: " + connection, cme );
                        } finally {
                            if ( datasource != null ) this.transformer.dbSelector.release( datasource );
                        }

                        if (result == null) {
                            throw new SQLException("Failed to obtain connection. Made "
                                    + transformer.connectAttempts + " attempts with "
                                    + transformer.connectWaittime + "ms interval");
                        }
                    }
                } else {
                    final String dburl = properties.getParameter( SQLTransformer.MAGIC_DBURL, null );
                    final String username = properties.getParameter( SQLTransformer.MAGIC_USERNAME, null );
                    final String password = properties.getParameter( SQLTransformer.MAGIC_PASSWORD, null );

                    if ( username == null || password == null ) {
                        result = DriverManager.getConnection( dburl );
                    } else {
                        result = DriverManager.getConnection( dburl, username,
                                                              password );
                    }
                }
            } catch ( SQLException e ) {
                transformer.getTheLogger().error( "Caught a SQLException", e );
                throw e;
            }

            return result;
        }

        protected void execute() throws SQLException {
            this.rowset_name = properties.getParameter( SQLTransformer.MAGIC_DOC_ELEMENT, "rowset" );
            this.row_name = properties.getParameter( SQLTransformer.MAGIC_ROW_ELEMENT, "row" );

            Enumeration enum = query_parts.elements();
            StringBuffer sb = new StringBuffer();
            while ( enum.hasMoreElements() ) {
                Object object = enum.nextElement();
                if ( object instanceof String ) {
                    sb.append( (String) object );
                } else if ( object instanceof AncestorValue ) {
                    /** Do a lookup into the ancestors' result's values **/
                    AncestorValue av = (AncestorValue) object;
                    Query query = transformer.getQuery( query_index - av.level );
                    sb.append( query.getColumnValue( av.name ) );
                }
            }
            
            String query = StringUtils.replace(sb.toString().trim(), "\r", " ", -1);
            // Test, if this is an update (by comparing with select)
            if ( !isstoredprocedure && !isupdate) {
                if (query.length() > 6 && !query.substring(0,6).equalsIgnoreCase("SELECT")) {
                    isupdate = true;
                }
            }
            if (transformer.getTheLogger().isDebugEnabled()) {
                transformer.getTheLogger().debug( "EXECUTING " + query );
            }

            conn = getConnection();

            try {
                if ( !isstoredprocedure ) {
                    if ( oldDriver ) {
                        pst = conn.prepareStatement( query );
                    } else {
                        pst = conn.prepareStatement( query,
                                                     ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                     ResultSet.CONCUR_READ_ONLY );
                    }
                } else {
                    if ( oldDriver ) {
                        cst = conn.prepareCall( query );
                    } else {
                        cst = conn.prepareCall( query,
                                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                ResultSet.CONCUR_READ_ONLY );
                    }
                    registerOutParameters( cst );
                    pst = cst;
                }

                registerInParameters( pst );
                boolean result = pst.execute();
                if ( result ) {
                    rs = pst.getResultSet();
                    md = rs.getMetaData();
                } else {
                    rv = pst.getUpdateCount();
                }
            } catch ( SQLException e ) {
                transformer.getTheLogger().error( "Caught a SQLException", e );
                throw e;
            } finally {
                conn.close();
                conn = null;        // To make sure we don't use this connection again.
            }
        }

        protected int getNrOfRows() throws SQLException {
            int nr = 0;
            if ( rs != null ) {
                if ( oldDriver ) {
                    nr = -1;
                } else {
                    try {
                        rs.last();
                        nr = rs.getRow();
                        rs.beforeFirst();
                    } catch ( NullPointerException e ) {
                        // A NullPointerException here crashes a whole lot of C2 -- catching it so it won't do any harm for now, but seems like it should be solved seriously
                        getTheLogger().error( "NPE while getting the nr of rows", e );
                    }
                }
            } else {
                if ( outParameters != null ) {
                    nr = outParameters.size();
                }
            }
            return nr;
        }

        protected String getColumnValue( int i ) throws SQLException {
            String retval =  SQLTransformer.getStringValue( rs.getObject( i ) );
            if (rs.getMetaData().getColumnType(i) == 8)
            retval = SQLTransformer.getStringValue( rs.getBigDecimal( i ) );
            return retval;
        }

        //fix not applied here because there is no metadata from Name -> number and coltype
        //for a given "name" versus number.  That being said this shouldn't be an issue
        //as this function is only called for ancestor lookups.
        protected String getColumnValue( String name ) throws SQLException {
            String retval =  SQLTransformer.getStringValue( rs.getObject( name ) );
//          if (rs.getMetaData().getColumnType( name ) == 8)
//		retval = transformer.getStringValue( rs.getBigDecimal( name ) );
            return retval;
        }

        protected boolean next() throws SQLException {
            // if rv is not -1, then an SQL insert, update, etc, has
            // happened (see JDBC docs - return codes for executeUpdate)
            if ( rv != -1 )
                return false;
            try {
                if ( rs == null || !rs.next() ) {
                    //close();
                    return false;
                }
            } catch ( NullPointerException e ) {
                getTheLogger().debug( "NullPointerException, returning false.", e );
                return false;
            }
            return true;
        }

        protected void close() throws SQLException {
            try {
                if ( rs != null )
                    try {
                        //getTheLogger().debug("Trying to close resultset "+rs.toString());
                        rs.close();
                        rs = null;      // This prevents us from using the resultset again.
                        //250getTheLogger().debug("Really closed the resultset now.");
                    } catch ( NullPointerException e ) {
                        getTheLogger().debug( "NullPointer while closing the resultset.", e );
                    }
                if ( pst != null )
                    pst.close();
                pst = null;        // Prevent using pst again.
                if ( cst != null )
                    cst.close();
                cst = null;        // Prevent using cst again.
            } finally {
                if ( conn != null )
                    conn.close();
                conn = null;
            }
        }

        protected void addQueryPart( Object object ) {
            query_parts.addElement( object );
        }

        protected void serializeData(ComponentManager manager,
                                     String           value)
        throws SQLException, SAXException {
            if (value != null) {
                value = value.trim();
                // Could this be XML ?
                if (value.length() > 0 && value.charAt(0) == '<') {
                    try {
                        if (transformer.parser != null) {
                            transformer.parser = (SAXParser)manager.lookup(SAXParser.ROLE);
                        }
                        if (transformer.compiler != null) {
                            compiler = (XMLSerializer)manager.lookup(XMLSerializer.ROLE);
                        }
                        if (transformer.interpreter != null) {
                            interpreter = (XMLDeserializer)manager.lookup(XMLDeserializer.ROLE);
                        }
                        parser.parse(new InputSource(new StringReader("<root>"+value+"</root>")), compiler);

                        IncludeXMLConsumer filter = new IncludeXMLConsumer(transformer, transformer);
                        filter.setIgnoreRootElement(true);

                        interpreter.setConsumer(filter);

                        interpreter.deserialize(compiler.getSAXFragment());
                    } catch (Exception local) {
                        // if an exception occured the data was not xml
                        transformer.data(value);
                    }
                } else {
                    transformer.data(value);
                }
            }
        }

        protected void serializeRow(ComponentManager manager)
        throws SQLException, SAXException {
            AttributesImpl attr = new AttributesImpl();
            if ( !isupdate && !isstoredprocedure ) {
                for ( int i = 1; i <= md.getColumnCount(); i++ ) {
                    transformer.start( md.getColumnName( i ).toLowerCase(), attr );
                    this.serializeData(manager, getColumnValue( i ) );
                    transformer.end( md.getColumnName( i ).toLowerCase() );
                }
            } else if ( isupdate && !isstoredprocedure ) {
                transformer.start( "returncode", attr );
                this.serializeData(manager, String.valueOf( rv ) );
                transformer.end( "returncode" );
                rv = -1; // we only want the return code shown once.
            }
        }

        protected void serializeStoredProcedure(ComponentManager manager)
        throws SQLException, SAXException {
            if ( outParametersNames == null || cst == null )
                return;
            //make sure output follows order as parameter order in stored procedure
            Iterator itOutKeys = ( new TreeMap( outParameters ) ).keySet().iterator();
            Integer counter;
            AttributesImpl attr = new AttributesImpl();
            try {
                while ( itOutKeys.hasNext() ) {
                    counter = (Integer) itOutKeys.next();
                    try {
                        if ( cst == null ) getTheLogger().debug( "SQLTransformer: cst is null" );
                        if ( counter == null ) getTheLogger().debug( " SQLTransformer: counter is null" );
                        Object obj = cst.getObject( counter.intValue() );
                        if ( !( obj instanceof ResultSet ) ) {
                            transformer.start( (String) outParametersNames.get( counter ), attr );
                            this.serializeData(manager, SQLTransformer.getStringValue( obj ) );
                            transformer.end( (String) outParametersNames.get( counter ) );
                        } else {
                            ResultSet rs = (ResultSet) obj;
                            try {
                                transformer.start( (String) outParametersNames.get( counter ), attr );
                                ResultSetMetaData md = rs.getMetaData();
                                while ( rs.next() ) {
                                    transformer.start( this.row_name, attr );
                                    for ( int i = 1; i <= md.getColumnCount(); i++ ) {
                                        transformer.start( md.getColumnName( i ).toLowerCase(), attr );
                                        if ( md.getColumnType( i ) == 8 ) {  //prevent nasty exponent notation
                                            this.serializeData(manager, SQLTransformer.getStringValue( rs.getBigDecimal( i ) ));
                                        } else {
                                            this.serializeData(manager, SQLTransformer.getStringValue( rs.getObject( i ) ));
                                        }
                                        transformer.end( md.getColumnName( i ).toLowerCase() );
                                    }
                                    transformer.end( this.row_name );
                                }
                            } finally {
                                rs.close();
                                rs = null;
                            }
                            transformer.end( (String) outParametersNames.get( counter ) );
                        }
                    } catch ( SQLException e ) {
                        transformer.getTheLogger().error( "Caught a SQLException", e );
                        throw e;
                    }
                }
            } finally {
                //close();
            }
        }
    }

    private class AncestorValue {
        protected int level;
        protected String name;

        protected AncestorValue( int level, String name ) {
            this.level = level;
            this.name = name;
        }
    }

}
