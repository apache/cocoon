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
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.xml.sax.SAXParser;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.sax.XMLByteStreamCompiler;
import org.apache.cocoon.components.sax.XMLByteStreamInterpreter;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.xml.IncludeXMLConsumer;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The <code>SQLTransformer</code> can be plugged into a pipeline to transform
 * SAX events into updated or queries and responses to/from a SQL interface.
 *
 * <p>
 * It is declared and configured as follows:
 * <pre>
 * &lt;map:transformers default="..."&gt;
 *   &lt;map:transformer name="sql" src="org.apache.cocoon.transformation.SQLTransformer"&gt;
 *     &lt;old-driver&gt;false&lt;/old-driver&gt;
 *     &lt;connection-attempts&gt;5&lt;/connection-attempts&gt;
 *     &lt;connection-waittime&gt;5000&lt;/connection-waittime&gt;
 *   &lt;/map:transformer&gt;
 * &lt;/map:transformers&gt;
 * </pre>
 * </p>
 *
 * <p>
 * It can be used in the sitemap pipeline as follows:
 * <code>
 * &lt;map:transform type="sql"&gt;
 *   <!-- True to force each query to create its own connection: -->
 *   &lt;map:parameter name="own-connection" value="..."/&gt;
 *   <!-- Specify either name of datasource: -->
 *   &lt;map:parameter name="use-connection" value="..."/&gt;
 *   <!-- Or connection parameters: -->
 *   &lt;map:parameter name="dburl" value="..."/&gt;
 *   &lt;map:parameter name="username" value="..."/&gt;
 *   &lt;map:parameter name="password" value="..."/&gt;
 *
 *   <!-- Default query parameters: -->
 *   &lt;map:parameter name="show-nr-or-rows" value="false"/&gt;
 *   &lt;map:parameter name="doc-element" value="rowset"/&gt;
 *   &lt;map:parameter name="row-element" value="row"/&gt;
 *   &lt;map:parameter name="namespace-uri" value="http://apache.org/cocoon/SQL/2.0"/&gt;
 *   &lt;map:parameter name="namespace-prefix" value="sql"/&gt;
 *   &lt;map:parameter name="clob-encoding" value=""/&gt;
 * &lt;/map:transform&gt;
 * </pre>
 * </p>
 *
 * <p>
 * The following DTD is valid:
 * <code>
 * &lt;!ENTITY % param "(own-connection?,(use-connection|(dburl,username,password))?,show-nr-or-rows?,doc-element?,row-element?,namespace-uri?,namespace-prefix?,clob-encoding?)"&gt;<br>
 * &lt;!ELEMENT execute-query (query,(in-parameter|out-parameter)*,execute-query?, %param;)&gt;<br>
 * &lt;!ELEMENT own-connection (#PCDATA)&gt;<br>
 * &lt;!ELEMENT use-connection (#PCDATA)&gt;<br>
 * &lt;!ELEMENT query (#PCDATA | substitute-value | ancestor-value | escape-string)*&gt;<br>
 * &lt;!ATTLIST query name CDATA #IMPLIED isstoredprocedure (true|false) "false" isupdate (true|false) "false"&gt;<br>
 * &lt;!ELEMENT substitute-value EMPTY&gt;<br>
 * &lt;!ATTLIST substitute-value name CDATA #REQUIRED&gt;<br>
 * &lt;!ELEMENT ancestor-value EMPTY&gt;<br>
 * &lt;!ATTLIST ancestor-value name CDATA #REQUIRED level CDATA #REQUIRED&gt;<br>
 * &lt;!ELEMENT in-parameter EMPTY&gt;<br>
 * &lt;!ATTLIST in-parameter nr CDATA #REQUIRED type CDATA #REQUIRED&gt;<br>
 * &lt;!ELEMENT out-parameter EMPTY&gt;<br>
 * &lt;!ATTLIST out-parameter nr CDATA #REQUIRED name CDATA #REQUIRED type CDATA #REQUIRED&gt;<br>
 * &lt;!ELEMENT escape-string (#PCDATA)&gt;<br>
 * </code>
 * </p>
 *
 * <p>
 * Each query can override default transformer parameters. Nested queries do not inherit parent
 * query parameters, but only transformer parameters. Each query can have connection to different
 * database, directly or using the connection pool. If database connection parameters are the same
 * as for any of the ancestor queries, nested query will re-use ancestor query connection.
 * </p>
 *
 * <p>
 * Connection sharing between queries can be disabled, globally or on per-query basis, using
 * <code>own-connection</code> parameter.
 * </p>
 *
 * <p>
 * By default, CLOBs are read from the database using character stream, so that character
 * decoding is performed by the database. Using <code>clob-encoding</code> parameter,
 * this behavior can be overrided, so that data is read as byte stream and decoded using
 * specified character encoding.
 * </p>
 *
 * <p>
 * TODO: Support inserting of the XML data into the database without need to escape it.
 *       Can be implemented by introducing new &lt;sql:xml/&gt; tag to indicate that
 *       startSerializedXMLRecording(...) should be used.
 * </p>
 *
 * @version $Id$
 */
public class SQLTransformer extends AbstractSAXTransformer {

    private static final int BUFFER_SIZE = 1024;

    /** The SQL transformer namespace */
    public static final String NAMESPACE = "http://apache.org/cocoon/SQL/2.0";

    // The SQL trasformer namespace element names
    public static final String MAGIC_EXECUTE_QUERY = "execute-query";
    private static final String MAGIC_OWN_CONNECTION = "own-connection";
    public static final String MAGIC_CONNECTION = "use-connection";
    public static final String MAGIC_DBURL = "dburl";
    public static final String MAGIC_USERNAME = "username";
    public static final String MAGIC_PASSWORD = "password";
    public static final String MAGIC_NR_OF_ROWS = "show-nr-of-rows";
    public static final String MAGIC_QUERY = "query";
    public static final String MAGIC_VALUE = "value";
    public static final String MAGIC_COLUMN_CASE = "column-case";
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
    public static final String CLOB_ENCODING = "clob-encoding";

    // The states we are allowed to be in
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
    protected boolean oldDriver;

    /** How many connection attempts to do? (default is 5 times) */
    protected int connectAttempts;

    /** How long wait between connection attempts? (default is 5000 ms) */
    protected int connectWaittime;

    //
    // State
    //

    /** The current query we are working on */
    protected Query query;

    /** The current state of the event receiving FSM */
    protected int state;

    /** The "name" of the connection shared by top level queries (if configuration allows) */
    protected String connName;

    /** The connection shared by top level queries (if configuration allows) */
    protected Connection conn;

    // Used to parse XML from database.
    protected SAXParser parser;

    /**
     * Constructor
     */
    public SQLTransformer() {
        super.defaultNamespaceURI = NAMESPACE;
    }

    //
    // Lifecycle Methods
    //

    /**
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager aManager) throws ServiceException {
        super.service(aManager);
        this.parser = (SAXParser)this.manager.lookup(SAXParser.ROLE);
    }

    /**
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.parser);
            this.parser = null;
        }
        super.dispose();
    }

    /**
     * Configure transformer. Supported configuration elements:
     * <ul>
     * <li>old-driver</li>
     * <li>connect-attempts</li>
     * <li>connect-waittime</li>
     * </ul>
     */
    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);

        this.oldDriver = conf.getChild("old-driver").getValueAsBoolean(false);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Value for old-driver is " + this.oldDriver);
        }

        this.connectAttempts = conf.getChild("connect-attempts").getValueAsInteger(5);
        this.connectWaittime = conf.getChild("connect-waittime").getValueAsInteger(5000);
    }

    /**
     * Setup for the current request.
     */
    public void setup(SourceResolver resolver, Map objectModel,
                      String source, Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, source, parameters);

        // Setup instance variables
        this.state = SQLTransformer.STATE_OUTSIDE;
        this.connName = name(super.parameters);
    }

    /**
     * Recycle this component
     */
    public void recycle() {
        this.query = null;
        try {
            // Close the connection used by all top level queries
            if (this.conn != null) {
                this.conn.close();
                this.conn = null;
            }
        } catch (SQLException e) {
            getLogger().info("Could not close connection", e);
        }
        this.connName = null;

        super.recycle();
    }

    /**
     * Return attribute value.
     * First try non-namespaced attribute, then try this transformer namespace.
     * @param name local attribute name
     */
    private String getAttributeValue(Attributes attr, String name) {
        String value = attr.getValue("", name);
        if (value == null) {
            value = attr.getValue(this.namespaceURI, name);
        }

        return value;
    }

    //
    // SAX Events Handlers
    //

    protected static void throwIllegalStateException(String message) {
        throw new IllegalStateException("Illegal state: " + message);
    }

    /** &lt;execute-query&gt; */
    protected void startExecuteQueryElement() {
        switch (state) {
            case SQLTransformer.STATE_OUTSIDE:
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                // Create root query (if query == null), or child query
                this.query = new Query(this.query);
                state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
                break;

            default:
                throwIllegalStateException("Not expecting a start execute query element");
        }
    }

    /** &lt;*&gt; */
    protected void startValueElement(String name)
    throws SAXException {
        switch (state) {
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                this.stack.push(name);
                startTextRecording();
                state = SQLTransformer.STATE_INSIDE_VALUE_ELEMENT;
                break;

            default:
                throwIllegalStateException("Not expecting a start value element: " + name);
        }
    }

    /** &lt;query&gt; */
    protected void startQueryElement(Attributes attributes)
    throws SAXException {
        switch (state) {
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                startTextRecording();
                state = SQLTransformer.STATE_INSIDE_QUERY_ELEMENT;

                String isUpdate = attributes.getValue("", SQLTransformer.MAGIC_UPDATE_ATTRIBUTE);
                if (isUpdate != null && !isUpdate.equalsIgnoreCase("false")) {
                    query.setUpdate(true);
                }

                String isProcedure = attributes.getValue("", SQLTransformer.MAGIC_STORED_PROCEDURE_ATTRIBUTE);
                if (isProcedure != null && !isProcedure.equalsIgnoreCase("false")) {
                    query.setStoredProcedure(true);
                }

                String name = attributes.getValue("", SQLTransformer.MAGIC_NAME_ATTRIBUTE);
                if (name != null) {
                    query.setName(name);
                }
                break;

            default:
                throwIllegalStateException("Not expecting a start query element");
        }
    }

    /** &lt;/query&gt; */
    protected void endQueryElement()
    throws ProcessingException, SAXException {
        switch (state) {
            case SQLTransformer.STATE_INSIDE_QUERY_ELEMENT:
                final String value = endTextRecording();
                if (value.length() > 0) {
                    query.addQueryPart(value);
                }
                state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
                break;

            default:
                throwIllegalStateException("Not expecting a stop query element");
        }
    }

    /** &lt;/*&gt; */
    protected void endValueElement()
    throws SAXException {
        switch (state) {
            case SQLTransformer.STATE_INSIDE_VALUE_ELEMENT:
                final String name = (String) this.stack.pop();
                final String value = endTextRecording();
                query.setParameter(name, value);
                this.state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
                break;

            default:
                throwIllegalStateException("Not expecting an end value element");
        }
    }

    /** &lt;/execute-query&gt; */
    protected void endExecuteQueryElement() throws SAXException {
        switch (state) {
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                if (query.parent == null) {
                    query.executeQuery();
                    query = null;
                    state = SQLTransformer.STATE_OUTSIDE;
                } else {
                    query.parent.addNestedQuery(query);
                    query = query.parent;
                    state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
                }
                break;

            default:
                throwIllegalStateException("Not expecting an end execute query element");
        }
    }

    /** &lt;ancestor-value&gt; */
    protected void startAncestorValueElement(Attributes attributes)
    throws ProcessingException, SAXException {
        switch (state) {
            case SQLTransformer.STATE_INSIDE_QUERY_ELEMENT:
                int level = 0;
                try {
                    level = Integer.parseInt(getAttributeValue(attributes, SQLTransformer.MAGIC_ANCESTOR_VALUE_LEVEL_ATTRIBUTE));
                } catch (Exception e) {
                    getLogger().debug("Invalid or missing value for " + SQLTransformer.MAGIC_ANCESTOR_VALUE_LEVEL_ATTRIBUTE + " attribute", e);
                    throwIllegalStateException("Ancestor value elements must have a " +
                                               SQLTransformer.MAGIC_ANCESTOR_VALUE_LEVEL_ATTRIBUTE + " attribute");
                }

                String name = getAttributeValue(attributes, SQLTransformer.MAGIC_ANCESTOR_VALUE_NAME_ATTRIBUTE);
                if (name == null) {
                    throwIllegalStateException("Ancestor value elements must have a " +
                                               SQLTransformer.MAGIC_ANCESTOR_VALUE_NAME_ATTRIBUTE + " attribute");
                }

                final String value = endTextRecording();
                if (value.length() > 0) {
                    query.addQueryPart(value);
                }
                query.addQueryPart(new AncestorValue(level, name));
                startTextRecording();

                state = SQLTransformer.STATE_INSIDE_ANCESTOR_VALUE_ELEMENT;
                break;
            default:
                throwIllegalStateException("Not expecting a start ancestor value element");
        }
    }

    /** &lt;/ancestor-value&gt; */
    protected void endAncestorValueElement() {
        state = SQLTransformer.STATE_INSIDE_QUERY_ELEMENT;
    }

    /** &lt;substitute-value&gt; */
    protected void startSubstituteValueElement(Attributes attributes)
    throws ProcessingException, SAXException {
        switch (state) {
            case SQLTransformer.STATE_INSIDE_QUERY_ELEMENT:
                String name = getAttributeValue(attributes, SQLTransformer.MAGIC_SUBSTITUTE_VALUE_NAME_ATTRIBUTE);
                if (name == null) {
                    throwIllegalStateException("Substitute value elements must have a " +
                                               SQLTransformer.MAGIC_SUBSTITUTE_VALUE_NAME_ATTRIBUTE + " attribute");
                }
                String substitute = parameters.getParameter(name, null);
                // Escape single quote
                substitute = StringEscapeUtils.escapeSql(substitute);

                final String value = endTextRecording();
                if (value.length() > 0) {
                    query.addQueryPart(value);
                }
                query.addQueryPart(substitute);
                startTextRecording();

                state = SQLTransformer.STATE_INSIDE_SUBSTITUTE_VALUE_ELEMENT;
                break;

            default:
                throwIllegalStateException("Not expecting a start substitute value element");
        }
    }

    /** &lt;/substitute-value&gt; */
    protected void endSubstituteValueElement() {
        state = SQLTransformer.STATE_INSIDE_QUERY_ELEMENT;
    }

    /** &lt;escape-string&gt; */
    protected void startEscapeStringElement(Attributes attributes)
    throws ProcessingException, SAXException {
        switch (state) {
            case SQLTransformer.STATE_INSIDE_QUERY_ELEMENT:
                final String value = endTextRecording();
                if (value.length() > 0) {
                    query.addQueryPart(value);
                }
                startTextRecording();

                state = SQLTransformer.STATE_INSIDE_ESCAPE_STRING;
                break;

            default:
                throwIllegalStateException("Not expecting a start escape-string element");
        }
    }

    /** &lt;/escape-string&gt; */
    protected void endEscapeStringElement()
    throws SAXException {
        switch (state) {
            case SQLTransformer.STATE_INSIDE_ESCAPE_STRING:
                String value = endTextRecording();
                if (value.length() > 0) {
                    value = StringEscapeUtils.escapeSql(value);
                    value = StringUtils.replace(value, "\\", "\\\\");
                    query.addQueryPart(value);
                }
                startTextRecording();
                state = SQLTransformer.STATE_INSIDE_QUERY_ELEMENT;
                break;

            default:
                throwIllegalStateException("Not expecting a end escape-string element");
        }
    }

    /** &lt;in-parameter&gt; */
    protected void startInParameterElement(Attributes attributes) {
        switch (state) {
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                String nr = getAttributeValue(attributes, SQLTransformer.MAGIC_IN_PARAMETER_NR_ATTRIBUTE);
                String value = getAttributeValue(attributes, SQLTransformer.MAGIC_IN_PARAMETER_VALUE_ATTRIBUTE);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("IN PARAMETER NR " + nr + "; VALUE " + value);
                }

                int position = Integer.parseInt(nr);
                query.setInParameter(position, value);
                state = SQLTransformer.STATE_INSIDE_IN_PARAMETER_ELEMENT;
                break;

            default:
                throwIllegalStateException("Not expecting an in-parameter element");
        }
    }

    /** &lt;/in-parameter&gt; */
    protected void endInParameterElement() {
        state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
    }

    /** &lt;out-parameter&gt; */
    protected void startOutParameterElement(Attributes attributes) {
        switch (state) {
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                String name = getAttributeValue(attributes, SQLTransformer.MAGIC_OUT_PARAMETER_NAME_ATTRIBUTE);
                String nr = getAttributeValue(attributes, SQLTransformer.MAGIC_OUT_PARAMETER_NR_ATTRIBUTE);
                String type = getAttributeValue(attributes, SQLTransformer.MAGIC_OUT_PARAMETER_TYPE_ATTRIBUTE);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("OUT PARAMETER NAME" + name + ";NR " + nr + "; TYPE " + type);
                }

                int position = Integer.parseInt(nr);
                query.setOutParameter(position, type, name);
                state = SQLTransformer.STATE_INSIDE_OUT_PARAMETER_ELEMENT;
                break;

            default:
                throwIllegalStateException("Not expecting an out-parameter element");
        }
    }

    /** &lt;/out-parameter&gt; */
    protected void endOutParameterElement() {
        state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
    }

    /**
     * ContentHandler method
     */
    public void startTransformingElement(String uri, String name, String raw, Attributes attributes)
    throws ProcessingException, SAXException {
        if (name.equals(SQLTransformer.MAGIC_EXECUTE_QUERY)) {
            startExecuteQueryElement();
        } else if (name.equals(SQLTransformer.MAGIC_QUERY)) {
            startQueryElement(attributes);
        } else if (name.equals(SQLTransformer.MAGIC_ANCESTOR_VALUE)) {
            startAncestorValueElement(attributes);
        } else if (name.equals(SQLTransformer.MAGIC_SUBSTITUTE_VALUE)) {
            startSubstituteValueElement(attributes);
        } else if (name.equals(SQLTransformer.MAGIC_IN_PARAMETER)) {
            startInParameterElement(attributes);
        } else if (name.equals(SQLTransformer.MAGIC_OUT_PARAMETER)) {
            startOutParameterElement(attributes);
        } else if (name.equals(SQLTransformer.MAGIC_ESCAPE_STRING)) {
            startEscapeStringElement(attributes);
        } else {
            startValueElement(name);
        }
    }

    /**
     * ContentHandler method
     */
    public void endTransformingElement(String uri, String name, String raw)
    throws ProcessingException, IOException, SAXException {
        if (name.equals(SQLTransformer.MAGIC_EXECUTE_QUERY)) {
            endExecuteQueryElement();
        } else if (name.equals(SQLTransformer.MAGIC_QUERY)) {
            endQueryElement();
        } else if (name.equals(SQLTransformer.MAGIC_ANCESTOR_VALUE)) {
            endAncestorValueElement();
        } else if (name.equals(SQLTransformer.MAGIC_SUBSTITUTE_VALUE)) {
            endSubstituteValueElement();
        } else if (name.equals(SQLTransformer.MAGIC_IN_PARAMETER)) {
            endInParameterElement();
        } else if (name.equals(SQLTransformer.MAGIC_OUT_PARAMETER)) {
            endOutParameterElement();
        } else if (name.equals(SQLTransformer.MAGIC_ESCAPE_STRING)) {
            endEscapeStringElement();
        } else {
            endValueElement();
        }
    }

    //
    // Helper methods for the Query
    //

    /**
     * Qualifies an element name by giving it a prefix.
     * @param name the element name
     * @param prefix the prefix to qualify with
     * @return a namespace qualified name that is correct
     */
    protected String nsQualify(String name, String prefix) {
        if (StringUtils.isEmpty(name)) {
            return name;
        }

        if (StringUtils.isNotEmpty(prefix)) {
            return prefix + ":" + name;
        }

        return name;
    }

    /**
     * Helper method for generating SAX events
     */
    protected void start(String uri, String prefix, String name, Attributes attr)
    throws SAXException {
        try {
            super.startTransformingElement(uri, name, nsQualify(name, prefix), attr);
        } catch (IOException e) {
            throw new SAXException(e);
        } catch (ProcessingException e) {
            throw new SAXException(e);
        }
    }

    /**
     * Helper method for generating SAX events
     */
    protected void end(String uri, String prefix, String name) throws SAXException {
        try {
            super.endTransformingElement(uri, name, nsQualify(name, prefix));
        } catch (IOException e) {
            throw new SAXException(e);
        } catch (ProcessingException e) {
            throw new SAXException(e);
        }
    }

    /**
     * Helper method for generating SAX events
     */
    protected void data(String data) throws SAXException {
        if (data != null) {
            super.characters(data.toCharArray(), 0, data.length());
        }
    }

    /**
     * Get 'name' for the connection which can be obtained using provided
     * connection parameters.
     */
    private String name(Parameters params) {
        final boolean ownConnection = params.getParameterAsBoolean(SQLTransformer.MAGIC_OWN_CONNECTION, false);
        if (ownConnection) {
            return null;
        }

        final String datasourceName = params.getParameter(SQLTransformer.MAGIC_CONNECTION, null);
        if (datasourceName != null) {
            return "ds:" + datasourceName;
        }

        final String dburl = params.getParameter(SQLTransformer.MAGIC_DBURL, null);
        if (dburl != null) {
            final String username = params.getParameter(SQLTransformer.MAGIC_USERNAME, null);
            final String password = params.getParameter(SQLTransformer.MAGIC_PASSWORD, null);

            if (username == null || password == null) {
                return "db:@" + dburl;
            } else {
                return "db:" + username + ":" + password + "@" + dburl;
            }
        }

        // Nothing configured
        return "";
    }

    /**
     * Open database connection using provided parameters.
     * Return null if neither datasource nor jndi URL configured.
     */
    private Connection open(Parameters params) throws SQLException {
        Connection result = null;

        // First check datasource name parameter
        final String datasourceName = params.getParameter(SQLTransformer.MAGIC_CONNECTION, null);
        if (datasourceName != null) {
            DataSourceComponent datasource = null;
            try {
                datasource = (DataSourceComponent) this.manager.lookup(DataSourceComponent.ROLE + '/' + datasourceName);
                for (int i = 0; i < this.connectAttempts && result == null; i++) {
                    try {
                        result = datasource.getConnection();
                    } catch (SQLException e) {
                        if (i + 1 < this.connectAttempts) {
                            final long waittime = this.connectWaittime;
                            // Log exception if debug enabled.
                            if (getLogger().isDebugEnabled()) {
                                getLogger().info("Unable to get connection; waiting " +
                                                  waittime + "ms to try again.", e);
                            } else {
                                getLogger().info("Unable to get connection; waiting " +
                                                  waittime + "ms to try again.");
                            }
                            try {
                                Thread.sleep(waittime);
                            } catch (InterruptedException ex) {
                                /* ignored */
                            }
                        }
                    }
                }
            } catch (ServiceException e) {
                throw new SQLException("Unable to get connection from datasource '" + datasourceName + "': " +
                                       "No such datasource.");
            } finally {
                this.manager.release(datasource);
            }

            if (result == null) {
                throw new SQLException("Failed to obtain connection from datasource '" + datasourceName + "'. " +
                                       "Made " + this.connectAttempts + " attempts with "
                                       + this.connectWaittime + "ms interval");
            }
        } else {
            // Then, check connection URL parameter
            final String dburl = params.getParameter(SQLTransformer.MAGIC_DBURL, null);
            if (dburl != null) {
                final String username = params.getParameter(SQLTransformer.MAGIC_USERNAME, null);
                final String password = params.getParameter(SQLTransformer.MAGIC_PASSWORD, null);

                if (username == null || password == null) {
                    result = DriverManager.getConnection(dburl);
                } else {
                    result = DriverManager.getConnection(dburl, username, password);
                }
            } else {
                // Nothing configured
            }
        }

        return result;
    }

    /**
     * Attempt to parse string value
     */
    private void stream(String value)
    throws ServiceException, SAXException, IOException {
        // Strip off the XML Declaration if there is one!
        if (value.startsWith("<?xml ")) {
            value = value.substring(value.indexOf("?>") + 2);
        }

        final XMLByteStreamCompiler compiler = new XMLByteStreamCompiler();
        final XMLByteStreamInterpreter interpreter = new XMLByteStreamInterpreter();

        this.parser.parse(new InputSource(new StringReader("<root>" + value + "</root>")),
                          compiler);

        final IncludeXMLConsumer filter = new IncludeXMLConsumer(this, this);
        filter.setIgnoreRootElement(true);

        interpreter.setConsumer(filter);
        interpreter.deserialize(compiler.getSAXFragment());
    }

    /**
     * One of the queries in the query tree formed from nested queries.
     */
    private class Query extends AbstractLogEnabled {

        /** Parent query, or null for top level query */
        protected Query parent;

        /** Nested sub-queries we have. */
        protected final List nested = new ArrayList();

        /** The parts of the query */
        protected final List parts = new ArrayList();

        //
        // Query Configuration
        //

        /** Name of the query */
        protected String name;

        /** If this query is actually an update (insert, update, delete) */
        protected boolean isUpdate;

        /** If this query is actually a stored procedure */
        protected boolean isStoredProcedure;

        /** Query configuration parameters */
        protected Parameters params;

        /** The namespace uri of the XML output. Defaults to {@link SQLTransformer#namespaceURI}. */
        protected String outUri;

        /** The namespace prefix of the XML output. Defaults to 'sql'. */
        protected String outPrefix;

        /** rowset element name */
        protected String rowsetElement;

        /** row element name */
        protected String rowElement;

        /** number of rows attribute name */
        protected String nrOfRowsAttr = "nrofrows";

        /** Query name attribute name */
        protected String nameAttr = "name";

        /** Handling of case of column names in results */
        protected int columnCase;

        /** Registered IN parameters */
        protected Map inParameters;

        /** Registered OUT parameters */
        protected Map outParameters;

        /** Mapping out parameters - objectModel */
        protected Map outParametersNames;

        /** Check if nr of rows need to be written out. */
        protected boolean showNrOfRows;

        /** Encoding we use for CLOB field */
        protected String clobEncoding;

        //
        // Query State
        //

        /** The connection */
        protected Connection conn;

        /** The 'name' of the connection */
        protected String connName;

        /** Is it our own connection? */
        protected boolean ownConn;

        /** Prepared statement */
        protected PreparedStatement pst;

        /** Callable statement */
        protected CallableStatement cst;

        /** The results, of course */
        protected ResultSet rs;

        /** And the results' metadata */
        protected ResultSetMetaData md;

        /** If it is an update/etc, the return value (num rows modified) */
        protected int rv = -1;


        protected Query(Query parent) {
            this.parent = parent;
            this.params = new Parameters();
            this.params.merge(SQLTransformer.this.parameters);
        }

        /** Add nested sub-query. */
        protected void addNestedQuery(Query query) {
            nested.add(query);
        }

        protected void addQueryPart(Object value) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Adding query part \"" + value + "\"");
            }
            parts.add(value);
        }

        protected String getName() {
            return name;
        }

        protected void setName(String name) {
            this.name = name;
        }

        protected void setParameter(String name, String value) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Adding parameter name {" + name + "} value {" + value + "}");
            }
            params.setParameter(name, value);
        }

        protected void setUpdate(boolean flag) {
            isUpdate = flag;
        }

        protected void setStoredProcedure(boolean flag) {
            isStoredProcedure = flag;
        }

        protected void setInParameter(int pos, String val) {
            if (inParameters == null) {
                inParameters = new HashMap();
            }
            inParameters.put(new Integer(pos), val);
        }

        protected void setOutParameter(int pos, String type, String name) {
            if (outParameters == null) {
                // make sure output parameters are ordered
                outParameters = new TreeMap();
                outParametersNames = new HashMap();
            }
            outParameters.put(new Integer(pos), type);
            outParametersNames.put(new Integer(pos), name);
        }

        private void setColumnCase(String columnCase) {
            if (columnCase.equals("lowercase")) {
                this.columnCase = -1;
            } else if (columnCase.equals("uppercase")) {
                this.columnCase = +1;
            } else if (columnCase.equals("preserve")) {
                // Do nothing
                this.columnCase = 0;
            } else {
                getLogger().warn("[" + columnCase + "] is not a valid value for <column-case>. " +
                                 "Column name retrieved from database will be used.");
            }
        }

        private void registerInParameters() throws SQLException {
            if (inParameters == null) {
                return;
            }

            Iterator i = inParameters.keySet().iterator();
            while (i.hasNext()) {
                Integer counter = (Integer) i.next();
                String value = (String) inParameters.get(counter);
                try {
                    pst.setObject(counter.intValue(), value);
                } catch (SQLException e) {
                    getLogger().error("Caught a SQLException", e);
                    throw e;
                }
            }
        }

        private void registerOutParameters(CallableStatement cst) throws SQLException {
            if (outParameters == null) {
                return;
            }

            Iterator i = outParameters.keySet().iterator();
            while (i.hasNext()) {
                Integer counter = (Integer) i.next();
                String type = (String) outParameters.get(counter);

                int index = type.lastIndexOf(".");
                if (index == -1) {
                    getLogger().error("Invalid SQLType: " + type, null);
                    throw new SQLException("Invalid SQLType: " + type);
                }

                String className = type.substring(0, index);
                String fieldName = type.substring(index + 1, type.length());
                try {
                    Class clss = Class.forName(className);
                    Field fld = clss.getField(fieldName);
                    cst.registerOutParameter(counter.intValue(), fld.getInt(fieldName));
                } catch (Exception e) {
                    // Lots of different exceptions to catch
                    getLogger().error("Invalid SQLType: " + className + "." + fieldName, e);
                }
            }
        }

        /**
         * Open database connection
         */
        private void open() throws SQLException {
            this.connName = SQLTransformer.this.name(this.params);

            // Check first if connection sharing disabled
            if (this.connName == null) {
                this.conn = SQLTransformer.this.open(this.params);
                this.ownConn = true;
                return;
            }

            // Iterate through parent queries and get appropriate connection
            Query query = this.parent;
            while (query != null) {
                if (this.connName.equals(query.connName)) {
                    this.conn = query.conn;
                    this.ownConn = false;
                    return;
                }
                query = query.parent;
            }

            // Check 'global' connection
            if (this.connName.equals(SQLTransformer.this.connName)) {
                // Use SQLTransformer configuration: it has same connection parameters
                if (SQLTransformer.this.conn == null) {
                    SQLTransformer.this.conn = SQLTransformer.this.open(SQLTransformer.this.parameters);
                }

                this.conn = SQLTransformer.this.conn;
                this.ownConn = false;
                return;
            }

            // Create own connection
            this.conn = SQLTransformer.this.open(this.params);
            this.ownConn = true;
        }

        /**
         * This will be the meat of SQLTransformer, where the query is run.
         */
        protected void executeQuery() throws SAXException {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Executing query " + this);
            }

            this.outUri = this.params.getParameter(SQLTransformer.MAGIC_NS_URI_ELEMENT, SQLTransformer.this.namespaceURI);
            this.outPrefix = this.params.getParameter(SQLTransformer.MAGIC_NS_PREFIX_ELEMENT, "sql");
            this.rowsetElement = this.params.getParameter(SQLTransformer.MAGIC_DOC_ELEMENT, "rowset");
            this.rowElement = this.params.getParameter(SQLTransformer.MAGIC_ROW_ELEMENT, "row");

            this.showNrOfRows = parameters.getParameterAsBoolean(SQLTransformer.MAGIC_NR_OF_ROWS, false);
            this.clobEncoding = parameters.getParameter(SQLTransformer.CLOB_ENCODING, "");
            if (this.clobEncoding.length() == 0) {
                this.clobEncoding = null;
            }

            // Start prefix mapping for output namespace, only if it's not mapped yet
            final String prefix = SQLTransformer.this.findPrefixMapping(this.outUri);
            if (prefix == null) {
                SQLTransformer.this.startPrefixMapping(this.outPrefix, this.outUri);
            } else {
                this.outPrefix = prefix;
            }

            boolean success = false;
            try {
                try {
                    open();
                    execute();
                    success = true;
                } catch (SQLException e) {
                    getLogger().info("Failed to execute query " + this, e);
                    start(this.rowsetElement, EMPTY_ATTRIBUTES);
                    start(MAGIC_ERROR, EMPTY_ATTRIBUTES);
                    data(e.getMessage());
                    end(MAGIC_ERROR);
                    end(this.rowsetElement);
                }

                if (success) {
                    AttributesImpl attr = new AttributesImpl();
                    if (showNrOfRows) {
                        attr.addAttribute("", this.nrOfRowsAttr, this.nrOfRowsAttr, "CDATA", String.valueOf(getNrOfRows()));
                    }
                    String name = getName();
                    if (name != null) {
                        attr.addAttribute("", this.nameAttr, this.nameAttr, "CDATA", name);
                    }
                    start(this.rowsetElement, attr);

                    // Serialize stored procedure output parameters
                    if (isStoredProcedure) {
                        serializeStoredProcedure();
                    }

                    // Serialize result set
                    while (next()) {
                        start(this.rowElement, EMPTY_ATTRIBUTES);
                        serializeRow();
                        for (Iterator i = this.nested.iterator(); i.hasNext();) {
                            ((Query) i.next()).executeQuery();
                        }
                        end(this.rowElement);
                    }

                    end(this.rowsetElement);
                }
            } catch (SQLException e) {
                getLogger().debug("Exception in executeQuery()", e);
                throw new SAXException(e);
            } finally {
                close();
            }

            if (prefix == null) {
                SQLTransformer.this.endPrefixMapping(this.outPrefix);
            }
        }

        /**
         * Execute the query. Connection must be set already.
         */
        private void execute() throws SQLException {
            setColumnCase(params.getParameter(SQLTransformer.MAGIC_COLUMN_CASE, "lowercase"));

            // Construct query string
            StringBuffer sb = new StringBuffer();
            for (Iterator i = parts.iterator(); i.hasNext();) {
                Object object = i.next();
                if (object instanceof String) {
                    sb.append((String) object);
                } else if (object instanceof AncestorValue) {
                    // Do a lookup into the ancestors' result's values
                    AncestorValue av = (AncestorValue) object;
                    Query query = this;
                    for (int k = av.level; k > 0; k--) {
                        query = query.parent;
                    }
                    sb.append(query.getColumnValue(av.name));
                }
            }

            String query = StringUtils.replace(sb.toString().trim(), "\r", " ", -1);
            // Test, if this is an update (by comparing with select)
            if (!isStoredProcedure && !isUpdate) {
                if (query.length() > 6 && !query.substring(0, 6).equalsIgnoreCase("SELECT")) {
                    isUpdate = true;
                }
            }

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Executing " + query);
            }
            if (!isStoredProcedure) {
                if (oldDriver) {
                    pst = conn.prepareStatement(query);
                } else {
                    pst = conn.prepareStatement(query,
                                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                ResultSet.CONCUR_READ_ONLY);
                }
            } else {
                if (oldDriver) {
                    cst = conn.prepareCall(query);
                } else {
                    cst = conn.prepareCall(query,
                                           ResultSet.TYPE_SCROLL_INSENSITIVE,
                                           ResultSet.CONCUR_READ_ONLY);
                }
                registerOutParameters(cst);
                pst = cst;
            }

            registerInParameters();
            boolean result = pst.execute();
            if (result) {
                rs = pst.getResultSet();
                md = rs.getMetaData();
            } else {
                rv = pst.getUpdateCount();
            }
        }

        protected int getNrOfRows() throws SQLException {
            int nr = 0;

            if (rs != null) {
                if (oldDriver) {
                    nr = -1;
                } else {
                    try {
                        rs.last();
                        nr = rs.getRow();
                        rs.beforeFirst();
                    } catch (NullPointerException e) {
                        // A NullPointerException here crashes a whole lot of C2 --
                        // catching it so it won't do any harm for now, but seems like it should be solved seriously
                        getLogger().error("NPE while getting the nr of rows", e);
                    }
                }
            } else {
                if (outParameters != null) {
                    nr = outParameters.size();
                }
            }
            return nr;
        }

        protected String getColumnValue(ResultSet rs, int i) throws SQLException {
            final int type = rs.getMetaData().getColumnType(i);
            if (type == java.sql.Types.DOUBLE) {
                return getStringValue(rs.getBigDecimal(i));
            } else if (type == java.sql.Types.CLOB) {
                return getStringValue(rs.getClob(i));
            } else {
                return getStringValue(rs.getObject(i));
            }
        }

        // fix not applied here because there is no metadata from Name -> number and coltype
        // for a given "name" versus number.  That being said this shouldn't be an issue
        // as this function is only called for ancestor lookups.
        protected String getColumnValue(String name) throws SQLException {
            //noinspection UnnecessaryLocalVariable
            String retval = getStringValue(rs.getObject(name));
            // if (rs.getMetaData().getColumnType( name ) == java.sql.Types.DOUBLE)
            // retval = transformer.getStringValue( rs.getBigDecimal( name ) );
            return retval;
        }

        protected boolean next() throws SQLException {
            // If rv is not -1, then an SQL insert, update, etc, has
            // happened (see JDBC docs - return codes for executeUpdate)
            if (rv != -1) {
                // Output row with return code. Once.
                return true;
            }

            if (rs != null && rs.next()) {
                // Have next row
                return true;
            }

            while (pst.getMoreResults()) {
                rs = pst.getResultSet();
                md = rs.getMetaData();
                if (rs.next()) {
                    // Have next row in next result set
                    return true;
                }
            }

            // Nothing left
            return false;
        }

        /**
         * Closes all the resources, ignores (but logs) exceptions.
         */
        protected void close() {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    getLogger().info("Unable to close the result set.", e);
                }
                // This prevents us from using the resultset again.
                rs = null;
            }

            if (pst != null && pst != cst) {
                try {
                    pst.close();
                } catch (SQLException e) {
                    getLogger().info("Unable to close the statement.", e);
                }
            }
            // Prevent using pst again.
            pst = null;

            if (cst != null) {
                try {
                    cst.close();
                } catch (SQLException e) {
                    getLogger().info("Unable to close the statement.", e);
                }
                // Prevent using cst again.
                cst = null;
            }

            try {
                if (ownConn && conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                getLogger().info("Unable to close the connection", e);
            }
            // Prevent using conn again.
            conn = null;
        }

        protected void serializeData(String value)
        throws SQLException, SAXException {
            if (value != null) {
                value = value.trim();
                // Could this be XML ?
                if (value.length() > 0 && value.charAt(0) == '<') {
                    try {
                        stream(value);
                    } catch (Exception ignored) {
                        // FIXME: bad coding "catch(Exception)"
                        // If an exception occured the data was not (valid) xml
                        data(value);
                    }
                } else {
                    data(value);
                }
            }
        }

        protected void serializeRow()
        throws SQLException, SAXException {
            if (rv != -1) {
                start("returncode", EMPTY_ATTRIBUTES);
                serializeData(String.valueOf(rv));
                end("returncode");
                // We only want the return code shown once.
                // Reset rv so next() returns false next time.
                rv = -1;
            } else {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    String columnName = getColumnName(md.getColumnName(i));
                    start(columnName, EMPTY_ATTRIBUTES);
                    serializeData(getColumnValue(rs, i));
                    end(columnName);
                }
            }
        }

        private void serializeResultSet(ResultSet rs) throws SQLException, SAXException {
            final ResultSetMetaData md = rs.getMetaData();
            final int n = md.getColumnCount();

            // Get column names
            final String[] columns = new String[n + 1];
            for (int i = 1; i <= n; i++) {
                columns[i] = getColumnName(md.getColumnName(i));
            }

            // Process rows
            while (rs.next()) {
                start(rowElement, EMPTY_ATTRIBUTES);
                for (int i = 1; i <= n; i++) {
                    start(columns[i], EMPTY_ATTRIBUTES);
                    serializeData(getColumnValue(rs, i));
                    end(columns[i]);
                }
                end(this.rowElement);
            }
        }

        protected void serializeStoredProcedure()
        throws SQLException, SAXException {
            if (outParametersNames == null || cst == null) {
                return;
            }

            Iterator itOutKeys = outParameters.keySet().iterator();
            while (itOutKeys.hasNext()) {
                final Integer counter = (Integer) itOutKeys.next();
                try {
                    final Object obj = cst.getObject(counter.intValue());
                    final String name = (String) outParametersNames.get(counter);
                    start(name, EMPTY_ATTRIBUTES);

                    if (!(obj instanceof ResultSet)) {
                        serializeData(getStringValue(obj));
                    } else {
                        final ResultSet rs = (ResultSet) obj;
                        try {
                            serializeResultSet(rs);
                        } finally {
                            try {
                                rs.close();
                            } catch (SQLException e) { /* ignored */ }
                        }
                    }

                    end(name);
                } catch (SQLException e) {
                    getLogger().error("Caught a SQLException", e);
                    throw e;
                }
            }
        }

        private String getColumnName(String columnName) {
            switch (this.columnCase) {
                case -1:
                    columnName = columnName.toLowerCase();
                    break;
                case +1:
                    columnName = columnName.toUpperCase();
                    break;
                default:
                    // Do nothing
            }
            return columnName;
        }

        /**
         * Convert object to string represenation
         */
        private String getStringValue(Object object) throws SQLException {
            if (object instanceof byte[]) {
                // FIXME Encoding?
                return new String((byte[]) object);
            }

            if (object instanceof char[]) {
                return new String((char[]) object);
            }

            // Old behavior: Read bytes & decode
            if (object instanceof Clob && this.clobEncoding != null) {
                Clob clob = (Clob) object;
                StringBuffer buffer = new StringBuffer();
                InputStream is = clob.getAsciiStream();
                try {
                    byte[] bytes = new byte[BUFFER_SIZE];
                    int n;
                    while ((n = is.read(bytes)) > -1) {
                        buffer.append(new String(bytes, 0, n, this.clobEncoding));
                    }
                } catch (IOException e) {
                    throw new SQLException("Error reading stream from CLOB");
                }
                return buffer.toString();
            }

            // Correct behavior: Read character data
            if (object instanceof Clob) {
                Clob clob = (Clob) object;
                StringBuffer buffer = new StringBuffer();
                Reader cs = clob.getCharacterStream();
                try {
                    char[] chars = new char[BUFFER_SIZE];
                    int n;
                    while ((n = cs.read(chars)) > -1) {
                        buffer.append(chars, 0, n);
                    }
                } catch (IOException e) {
                    throw new SQLException("Error reading stream from CLOB");
                }
                return buffer.toString();
            }

            if (object != null) {
                return object.toString();
            }

            return "";
        }

        private void start(String name, Attributes attr)
        throws SAXException {
            SQLTransformer.this.start(this.outUri, this.outPrefix, name, attr);
        }

        private void end(String name) throws SAXException {
            SQLTransformer.this.end(this.outUri, this.outPrefix, name);
        }

        private void data(String data) throws SAXException {
            SQLTransformer.this.data(data);
        }
    }

    private static class AncestorValue {
        protected int level;
        protected String name;

        protected AncestorValue(int level, String name) {
            this.level = level;
            this.name = name;
        }

        public String toString() {
            return "<ancestor level " + level + ", name " + name + ">";
        }
    }
}
