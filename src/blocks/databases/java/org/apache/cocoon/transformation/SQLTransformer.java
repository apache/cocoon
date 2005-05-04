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
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.IncludeXMLConsumer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.xml.sax.SAXParser;
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
 *   <!-- Specify either name of datasource: -->
 *   &lt;map:parameter name="use-connection" value="..."/&gt;
 *   <!-- Or connection parameters: -->
 *   &lt;map:parameter name="dburl" value="..."/&gt;
 *   &lt;map:parameter name="username" value="..."/&gt;
 *   &lt;map:parameter name="password" value="..."/&gt;
 *   <!-- Common parameters: -->
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
 * &lt;!ENTITY % param  "((use-connection|(dburl,username,password))?,show-nr-or-rows?,doc-element?,row-element?,namespace-uri?,namespace-prefix?,clob-encoding?)"&gt;<br>
 * &lt;!ELEMENT execute-query (query,(in-parameter|out-parameter)*,execute-query?, %param;)&gt;<br>
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
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @author <a href="mailto:giacomo.pati@pwr.ch">Giacomo Pati</a>
 *         (PWR Organisation &amp; Entwicklung)
 * @author <a href="mailto:sven.beauprez@the-ecorp.com">Sven Beauprez</a>
 * @author <a href="mailto:a.saglimbeni@pro-netics.com">Alfio Saglimbeni</a>
 * @author <a href="mailto:pmhahn@titan.lahn.de">Philipp Hahn</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version $Id$
 */
public class SQLTransformer extends AbstractSAXTransformer
                            implements Disposable, Configurable {

    /** The SQL transformer namespace */
    public static final String NAMESPACE = "http://apache.org/cocoon/SQL/2.0";

    // The SQL trasformer namespace element names
    public static final String MAGIC_EXECUTE_QUERY = "execute-query";
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
    protected int current_state;

    /** Check if nr of rows need to be written out. */
    protected boolean showNrOfRows;

    /** The namespace uri of the XML output. Defaults to {@link #namespaceURI}. */
    protected String outUri;

    /** The namespace prefix of the XML output. Defaults to 'sql'. */
    protected String outPrefix;

    /** The database selector */
    protected ServiceSelector dbSelector;

    /** Encoding we use for CLOB field */
	protected String clobEncoding;

    /** The connection used by all top level queries */
    protected Connection connection;

    // Used to parse XML from database.
    protected XMLSerializer compiler;
    protected XMLDeserializer interpreter;
    protected SAXParser parser;

    /**
     * Constructor
     */
    public SQLTransformer() {
        super.defaultNamespaceURI = NAMESPACE;
    }

    /**
     * Serviceable
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        try {
            this.dbSelector = (ServiceSelector) manager.lookup(DataSourceComponent.ROLE + "Selector");
        } catch (ServiceException cme) {
            getLogger().warn("Could not get the DataSource Selector", cme);
        }
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
     * Recycle this component
     */
    public void recycle() {
        try {
            // Close the connection used by all top level queries
            if (this.connection != null) {
                this.connection.close();
                this.connection = null;
            }
        } catch (SQLException e) {
            getLogger().warn("Could not close the connection", e);
        }

        this.query = null;
        this.outUri = null;
        this.outPrefix = null;

        this.manager.release(this.parser);
        this.parser = null;
        this.manager.release(this.compiler);
        this.compiler = null;
        this.manager.release(this.interpreter);
        this.interpreter = null;

        super.recycle();
    }

    /**
     * Dispose
     */
    public void dispose() {
        if (this.dbSelector != null) {
            this.manager.release(this.dbSelector);
            this.dbSelector = null;
        }
    }

    /**
     * Setup for the current request.
     */
    public void setup(SourceResolver resolver, Map objectModel,
                      String source, Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, source, parameters);

        // Setup instance variables
        this.current_state = SQLTransformer.STATE_OUTSIDE;

        this.showNrOfRows = parameters.getParameterAsBoolean(SQLTransformer.MAGIC_NR_OF_ROWS, false);
        this.clobEncoding = parameters.getParameter(SQLTransformer.CLOB_ENCODING, "");

        if (getLogger().isDebugEnabled()) {
            if (this.parameters.getParameter(SQLTransformer.MAGIC_CONNECTION, null) != null) {
                getLogger().debug("CONNECTION: " + this.parameters.getParameter(SQLTransformer.MAGIC_CONNECTION, null));
            } else {
                getLogger().debug("DBURL: " + parameters.getParameter(SQLTransformer.MAGIC_DBURL, null));
                getLogger().debug("USERNAME: " + parameters.getParameter(SQLTransformer.MAGIC_USERNAME, null));
            }
            getLogger().debug("DOC-ELEMENT: " + parameters.getParameter(SQLTransformer.MAGIC_DOC_ELEMENT, "rowset"));
            getLogger().debug("ROW-ELEMENT: " + parameters.getParameter(SQLTransformer.MAGIC_ROW_ELEMENT, "row"));
            getLogger().debug("Using CLOB encoding: " + clobEncoding);
        }
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

    /**
     * This will be the meat of SQLTransformer, where the query is run.
     */
    protected void executeQuery(Query query)
    throws SAXException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Executing query " + query);
        }

        this.outUri = query.properties.getParameter(SQLTransformer.MAGIC_NS_URI_ELEMENT, super.namespaceURI);
        this.outPrefix = query.properties.getParameter(SQLTransformer.MAGIC_NS_PREFIX_ELEMENT, "sql");

        // Start prefix mapping for output namespace
        // only if its URI is not empty, and if it is not this transformer namespace.
        final boolean startPrefixMapping = !"".equals(this.outUri) && !namespaceURI.equals(this.outUri);
        if (startPrefixMapping) {
            super.startPrefixMapping(this.outPrefix, this.outUri);
        }

        boolean success = false;
        Connection conn = null;
        try {
            try {
                if (query.parent == null) {
                    if (this.connection == null) {
                        // The first top level execute-query
                        this.connection = query.getConnection();
                    }
                    // Reuse the global connection for all top level queries
                    conn = this.connection;
                } else {
                    // Sub queries are always executed in an own connection
                    conn = query.getConnection();
                }

                query.setConnection(conn);
                query.execute();
                success = true;
            } catch (SQLException e) {
                getLogger().info("Failed to execute query " + query, e);
                start(query.rowset_name, EMPTY_ATTRIBUTES);
                start(MAGIC_ERROR, EMPTY_ATTRIBUTES);
                data(e.getMessage());
                end(MAGIC_ERROR);
                end(query.rowset_name);
            }

            if (success) {
                AttributesImpl attr = new AttributesImpl();
                if (this.showNrOfRows) {
                    attr.addAttribute("", query.nr_of_rows, query.nr_of_rows, "CDATA", String.valueOf(query.getNrOfRows()));
                }
                String name = query.getName();
                if (name != null) {
                    attr.addAttribute("", query.name_attribute, query.name_attribute, "CDATA", name);
                }
                start(query.rowset_name, attr);

                if (!query.isStoredProcedure()) {
                    while (query.next()) {
                        start(query.row_name, EMPTY_ATTRIBUTES);
                        query.serializeRow(this.manager);
                        for (Iterator i = query.nested(); i.hasNext();) {
                            executeQuery((Query) i.next());
                        }
                        end(query.row_name);
                    }
                } else {
                    query.serializeStoredProcedure(this.manager);
                }

                end(query.rowset_name);
            }
        } catch (SQLException e) {
            getLogger().debug("Exception in executeQuery()", e);
            throw new SAXException(e);
        } finally {
            query.close();
            if (query.parent != null) {
                try {
                    // Close the connection used by a sub query
                    conn.close();
                } catch (SQLException e) {
                    getLogger().warn("Unable to close JDBC connection", e);
                }
            }
        }

        if (startPrefixMapping) {
            super.endPrefixMapping(this.outPrefix);
        }
    }

    protected static void throwIllegalStateException(String message) {
        throw new IllegalStateException("Illegal state: " + message);
    }

    /** &lt;execute-query&gt; */
    protected void startExecuteQueryElement() {
        switch (current_state) {
            case SQLTransformer.STATE_OUTSIDE:
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                // Create root query (if query == null), or child query
                this.query = new Query(this, this.query);
                this.query.enableLogging(getLogger().getChildLogger("query"));
                current_state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
                break;

            default:
                throwIllegalStateException("Not expecting a start execute query element");
        }
    }

    /** &lt;*&gt; */
    protected void startValueElement(String name)
    throws SAXException {
        switch (current_state) {
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                this.stack.push(name);
                startTextRecording();
                current_state = SQLTransformer.STATE_INSIDE_VALUE_ELEMENT;
                break;

            default:
                throwIllegalStateException("Not expecting a start value element: " + name);
        }
    }

    /** &lt;query&gt; */
    protected void startQueryElement(Attributes attributes)
    throws SAXException {
        switch (current_state) {
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                startTextRecording();
                current_state = SQLTransformer.STATE_INSIDE_QUERY_ELEMENT;

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
        switch (current_state) {
            case SQLTransformer.STATE_INSIDE_QUERY_ELEMENT:
                final String value = endTextRecording();
                if (value.length() > 0) {
                    query.addQueryPart(value);
                }
                current_state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
                break;

            default:
                throwIllegalStateException("Not expecting a stop query element");
        }
    }

    /** &lt;/*&gt; */
    protected void endValueElement()
    throws SAXException {
        switch (current_state) {
            case SQLTransformer.STATE_INSIDE_VALUE_ELEMENT:
                final String name = (String) this.stack.pop();
                final String value = endTextRecording();
                query.setParameter(name, value);
                this.current_state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
                break;

            default:
                throwIllegalStateException("Not expecting an end value element");
        }
    }

    /** &lt;/execute-query&gt; */
    protected void endExecuteQueryElement() throws SAXException {
        switch (current_state) {
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                if (query.parent == null) {
                    executeQuery(query);
                    query = null;
                    current_state = SQLTransformer.STATE_OUTSIDE;
                } else {
                    query.parent.addNestedQuery(query);
                    query = query.parent;
                    current_state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
                }
                break;

            default:
                throwIllegalStateException("Not expecting an end execute query element");
        }
    }

    /** &lt;ancestor-value&gt; */
    protected void startAncestorValueElement(Attributes attributes)
    throws ProcessingException, SAXException {
        switch (current_state) {
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

                current_state = SQLTransformer.STATE_INSIDE_ANCESTOR_VALUE_ELEMENT;
                break;
            default:
                throwIllegalStateException("Not expecting a start ancestor value element");
        }
    }

    /** &lt;/ancestor-value&gt; */
    protected void endAncestorValueElement() {
        current_state = SQLTransformer.STATE_INSIDE_QUERY_ELEMENT;
    }

    /** &lt;substitute-value&gt; */
    protected void startSubstituteValueElement(Attributes attributes)
    throws ProcessingException, SAXException {
        switch (current_state) {
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

                current_state = SQLTransformer.STATE_INSIDE_SUBSTITUTE_VALUE_ELEMENT;
                break;

            default:
                throwIllegalStateException("Not expecting a start substitute value element");
        }
    }

    /** &lt;/substitute-value&gt; */
    protected void endSubstituteValueElement() {
        current_state = SQLTransformer.STATE_INSIDE_QUERY_ELEMENT;
    }

    /** &lt;escape-string&gt; */
    protected void startEscapeStringElement(Attributes attributes)
    throws ProcessingException, SAXException {
        switch (current_state) {
            case SQLTransformer.STATE_INSIDE_QUERY_ELEMENT:
                final String value = endTextRecording();
                if (value.length() > 0) {
                    query.addQueryPart(value);
                }
                startTextRecording();

                current_state = SQLTransformer.STATE_INSIDE_ESCAPE_STRING;
                break;

            default:
                throwIllegalStateException("Not expecting a start escape-string element");
        }
    }

    /** &lt;/escape-string&gt; */
    protected void endEscapeStringElement()
    throws SAXException {
        switch (current_state) {
            case SQLTransformer.STATE_INSIDE_ESCAPE_STRING:
                String value = endTextRecording();
                if (value.length() > 0) {
                    value = StringEscapeUtils.escapeSql(value);
                    value = StringUtils.replace(value, "\\", "\\\\");
                    query.addQueryPart(value);
                }
                startTextRecording();
                current_state = SQLTransformer.STATE_INSIDE_QUERY_ELEMENT;
                break;

            default:
                throwIllegalStateException("Not expecting a end escape-string element");
        }
    }

    /** &lt;in-parameter&gt; */
    protected void startInParameterElement(Attributes attributes) {
        switch (current_state) {
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                String nr = getAttributeValue(attributes, SQLTransformer.MAGIC_IN_PARAMETER_NR_ATTRIBUTE);
                String value = getAttributeValue(attributes, SQLTransformer.MAGIC_IN_PARAMETER_VALUE_ATTRIBUTE);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("IN PARAMETER NR " + nr + "; VALUE " + value);
                }

                int position = Integer.parseInt(nr);
                query.setInParameter(position, value);
                current_state = SQLTransformer.STATE_INSIDE_IN_PARAMETER_ELEMENT;
                break;

            default:
                throwIllegalStateException("Not expecting an in-parameter element");
        }
    }

    /** &lt;/in-parameter&gt; */
    protected void endInParameterElement() {
        current_state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
    }

    /** &lt;out-parameter&gt; */
    protected void startOutParameterElement(Attributes attributes) {
        switch (current_state) {
            case SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                String name = getAttributeValue(attributes, SQLTransformer.MAGIC_OUT_PARAMETER_NAME_ATTRIBUTE);
                String nr = getAttributeValue(attributes, SQLTransformer.MAGIC_OUT_PARAMETER_NR_ATTRIBUTE);
                String type = getAttributeValue(attributes, SQLTransformer.MAGIC_OUT_PARAMETER_TYPE_ATTRIBUTE);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("OUT PARAMETER NAME" + name + ";NR " + nr + "; TYPE " + type);
                }

                int position = Integer.parseInt(nr);
                query.setOutParameter(position, type, name);
                current_state = SQLTransformer.STATE_INSIDE_OUT_PARAMETER_ELEMENT;
                break;

            default:
                throwIllegalStateException("Not expecting an out-parameter element");
        }
    }

    /** &lt;/out-parameter&gt; */
    protected void endOutParameterElement() {
        current_state = SQLTransformer.STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
    }

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

    /**
     * Helper method for generating SAX events
     */
    protected void start(String name, Attributes attr)
    throws SAXException {
        try {
            super.startTransformingElement(outUri, name, nsQualify(name, outPrefix), attr);
        } catch (IOException e) {
            throw new SAXException(e);
        } catch (ProcessingException e) {
            throw new SAXException(e);
        }
    }

    /**
     * Helper method for generating SAX events
     */
    protected void end(String name) throws SAXException {
        try {
            super.endTransformingElement(outUri, name, nsQualify(name, outPrefix));
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
     * Convert object to string represenation
     */
    protected static String getStringValue(Object object) {
        if (object instanceof byte[]) {
            // FIXME Encoding?
            return new String((byte[]) object);
        } else if (object instanceof char[]) {
            return new String((char[]) object);
        } else if (object != null) {
            return object.toString();
        }

        return "";
    }

    /**
     * One of the queries in the query tree formed from nested queries.
     */
    class Query extends AbstractLogEnabled {
        /** Who's your daddy? */
        protected SQLTransformer transformer;

        /** Parent query, or null for top level query */
        protected Query parent;

        /** Nested sub-queries we have. */
        protected List nested = new ArrayList();

        /** SQL configuration information */
        protected Parameters properties;

        /** Dummy static variables for the moment **/
        protected String rowset_name;
        protected String row_name;
        protected String nr_of_rows = "nrofrows";
        protected String name_attribute = "name";

        /** The connection */
        protected Connection conn;

        /** And the statements */
        protected PreparedStatement pst;
        protected CallableStatement cst;

        /** The results, of course */
        protected ResultSet rs;

        /** And the results' metadata */
        protected ResultSetMetaData md;

        /** If this query is actually an update (insert, update, delete) */
        protected boolean isupdate;

        /** If this query is actually a stored procedure */
        protected boolean isstoredprocedure;

        /** Name of the query */
        protected String name;

        /** If it is an update/etc, the return value (num rows modified) */
        protected int rv = -1;

        /** The parts of the query */
        protected List query_parts = new ArrayList();

        /** In parameters */
        protected HashMap inParameters;

        /** Out parameters */
        protected HashMap outParameters;

        /** Mapping out parameters - objectModel */
        protected HashMap outParametersNames;

        /** Handling of case of column names in results */
        protected int columnCase;


        protected Query(SQLTransformer transformer, Query parent) {
            this.transformer = transformer;
            this.parent = parent;
            this.properties = new Parameters();
            this.properties.merge(transformer.parameters);
        }

        /** Return iterator over nested sub-queries. */
        protected Iterator nested() {
            return this.nested.iterator();
        }

        /** Add nested sub-query. */
        protected void addNestedQuery(Query query) {
            this.nested.add(query);
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
            properties.setParameter(name, value);
        }

        protected void setUpdate(boolean flag) {
            isupdate = flag;
        }

        protected void setStoredProcedure(boolean flag) {
            isstoredprocedure = flag;
        }

        protected boolean isStoredProcedure() {
            return isstoredprocedure;
        }

        protected void setInParameter(int pos, String val) {
            if (inParameters == null) {
                inParameters = new HashMap();
            }
            inParameters.put(new Integer(pos), val);
        }

        protected void setOutParameter(int pos, String type, String name) {
            if (outParameters == null) {
                outParameters = new HashMap();
                outParametersNames = new HashMap();
            }
            outParameters.put(new Integer(pos), type);
            outParametersNames.put(new Integer(pos), name);
        }

        protected void setColumnCase(String columnCase) {
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

        private void registerInParameters(PreparedStatement pst) throws SQLException {
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

                String className, fieldName;
                if (index > -1) {
                    className = type.substring(0, index);
                    fieldName = type.substring(index + 1, type.length());
                } else {
                    getLogger().error("Invalid SQLType: " + type, null);
                    throw new SQLException("Invalid SQLType: " + type);
                }
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

        protected void setConnection(Connection conn) {
            this.conn = conn;
        }

        /**
         * Get a Connection. Made this a separate method to separate the logic from the actual execution.
         */
        protected Connection getConnection() throws SQLException {
            Connection result = null;

            final String connection = properties.getParameter(SQLTransformer.MAGIC_CONNECTION, null);
            if (connection != null) {
                // Use datasource components
                if (this.transformer.dbSelector == null) {
                    throw new SQLException("Failed to obtain connection from datasource '" + connection + "'. " +
                                           "No datasources configured in cocoon.xconf.");
                }

                DataSourceComponent datasource = null;
                try {
                    datasource = (DataSourceComponent) this.transformer.dbSelector.select(connection);
                    for (int i = 0; i < transformer.connectAttempts && result == null; i++) {
                        try {
                            result = datasource.getConnection();
                        } catch (Exception e) {
                            final long waittime = transformer.connectWaittime;
                            getLogger().debug("Unable to get connection; waiting " +
                                                 waittime + "ms to try again.");
                            try {
                                Thread.sleep(waittime);
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }
                } catch (ServiceException cme) {
                    getLogger().error("Could not use connection: " + connection, cme);
                } finally {
                    if (datasource != null) {
                        this.transformer.dbSelector.release(datasource);
                    }
                }

                if (result == null) {
                    throw new SQLException("Failed to obtain connection from datasource '" + connection + "'. " +
                                           "Made " + transformer.connectAttempts + " attempts with "
                                           + transformer.connectWaittime + "ms interval");
                }
            } else {
                // Create connection manually
                final String dburl = properties.getParameter(SQLTransformer.MAGIC_DBURL, null);
                final String username = properties.getParameter(SQLTransformer.MAGIC_USERNAME, null);
                final String password = properties.getParameter(SQLTransformer.MAGIC_PASSWORD, null);

                if (username == null || password == null) {
                    result = DriverManager.getConnection(dburl);
                } else {
                    result = DriverManager.getConnection(dburl, username, password);
                }
            }

            return result;
        }

        /**
         * Execute the query. Connection must be set already.
         */
        protected void execute() throws SQLException {
            if (this.conn == null) {
                throw new SQLException("A connection must be set before executing a query");
            }

            this.rowset_name = properties.getParameter(SQLTransformer.MAGIC_DOC_ELEMENT, "rowset");
            this.row_name = properties.getParameter(SQLTransformer.MAGIC_ROW_ELEMENT, "row");
            setColumnCase(properties.getParameter(SQLTransformer.MAGIC_COLUMN_CASE, "lowercase"));

            StringBuffer sb = new StringBuffer();
            for (Iterator i = query_parts.iterator(); i.hasNext();) {
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
            if (!isstoredprocedure && !isupdate) {
                if (query.length() > 6 && !query.substring(0, 6).equalsIgnoreCase("SELECT")) {
                    isupdate = true;
                }
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Executing " + query);
            }

            try {
                if (!isstoredprocedure) {
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

                registerInParameters(pst);
                boolean result = pst.execute();
                if (result) {
                    rs = pst.getResultSet();
                    md = rs.getMetaData();
                } else {
                    rv = pst.getUpdateCount();
                }
            } catch (SQLException e) {
                getLogger().error("Caught a SQLException", e);
                throw e;
            } finally {
                // Connection is not closed here, but later on. See bug #12173.
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

        protected String getColumnValue(int i) throws SQLException {
            int numberOfChar = 1024;
            String retval;

            if (rs.getMetaData().getColumnType(i) == java.sql.Types.DOUBLE) {
                retval = SQLTransformer.getStringValue(rs.getBigDecimal(i));
            } else if (rs.getMetaData().getColumnType(i) == java.sql.Types.CLOB) {
                Clob clob = rs.getClob(i);
                InputStream inputStream = clob.getAsciiStream();
                byte[] readByte = new byte[numberOfChar];
                StringBuffer buffer = new StringBuffer();
                try {
                    while (inputStream.read(readByte) > -1) {
                        String string = new String(readByte, clobEncoding);
                        buffer.append(string);
                    }
                } catch (IOException e) {
                    throw new SQLException("Error reading stream from CLOB");
                }
                retval = buffer.toString();
            } else {
                retval = SQLTransformer.getStringValue(rs.getObject(i));
            }
            return retval;
        }

        // fix not applied here because there is no metadata from Name -> number and coltype
        // for a given "name" versus number.  That being said this shouldn't be an issue
        // as this function is only called for ancestor lookups.
        protected String getColumnValue(String name) throws SQLException {
            String retval = SQLTransformer.getStringValue(rs.getObject(name));
            // if (rs.getMetaData().getColumnType( name ) == 8)
            // retval = transformer.getStringValue( rs.getBigDecimal( name ) );
            return retval;
        }

        protected boolean next() throws SQLException {
            // if rv is not -1, then an SQL insert, update, etc, has
            // happened (see JDBC docs - return codes for executeUpdate)
            if (rv != -1) {
                return false;
            }

            try {
                if (rs == null || !rs.next()) {
                    return false;
                }
            } catch (NullPointerException e) {
                getLogger().debug("NullPointerException, returning false.", e);
                return false;
            }

            return true;
        }

        /**
         * Closes all the resources, ignores (but logs) exceptions.
         */
        protected void close() {
            try {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (NullPointerException e) {
                        getLogger().debug("NullPointer while closing the resultset.", e);
                    } catch (SQLException e) {
                        getLogger().info("SQLException while closing the ResultSet.", e);
                    }
                    // This prevents us from using the resultset again.
                    rs = null;
                }

                if (pst != null && pst != cst) {
                    try {
                        pst.close();
                    } catch (SQLException e) {
                        getLogger().info("SQLException while closing the Statement.", e);
                    }
                }
                // Prevent using pst again.
                pst = null;

                if (cst != null) {
                    try {
                        cst.close();
                    } catch (SQLException e) {
                        getLogger().info("SQLException while closing the Statement.", e);
                    }
                }
                // Prevent using cst again.
                cst = null;
            } finally {
                // Prevent using conn again.
                conn = null;
            }
        }

        protected void addQueryPart(Object value) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Adding query part \"" + value + "\"");
            }
            query_parts.add(value);
        }

        protected void serializeData(ServiceManager manager, String value)
        throws SQLException, SAXException {
            if (value != null) {
                value = value.trim();
                // Could this be XML ?
                if (value.length() > 0 && value.charAt(0) == '<') {
                    try {
                        String stripped = value;

                        // Strip off the XML Declaration if there is one!
                        if (stripped.startsWith("<?xml ")) {
                            stripped = stripped.substring(stripped.indexOf("?>") + 2);
                        }

                        if (transformer.parser == null) {
                            transformer.parser = (SAXParser) manager.lookup(SAXParser.ROLE);
                        }
                        if (transformer.compiler == null) {
                            transformer.compiler = (XMLSerializer) manager.lookup(XMLSerializer.ROLE);
                        }
                        if (transformer.interpreter == null) {
                            transformer.interpreter = (XMLDeserializer) manager.lookup(XMLDeserializer.ROLE);
                        }

                        transformer.parser.parse(new InputSource(new StringReader("<root>" + stripped + "</root>")),
                                                 transformer.compiler);

                        IncludeXMLConsumer filter = new IncludeXMLConsumer(transformer, transformer);
                        filter.setIgnoreRootElement(true);

                        transformer.interpreter.setConsumer(filter);
                        transformer.interpreter.deserialize(transformer.compiler.getSAXFragment());
                    } catch (Exception local) {
                        // FIXME: bad coding "catch(Exception)"
                        // if an exception occured the data was not xml
                        transformer.data(value);
                    } finally {
                        // otherwise serializer won't be reset
                        if (transformer.compiler != null) {
                            manager.release(transformer.compiler);
                            transformer.compiler = null;
                        }
                    }
                } else {
                    transformer.data(value);
                }
            }
        }

        protected void serializeRow(ServiceManager manager)
        throws SQLException, SAXException {
            if (!isupdate && !isstoredprocedure) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    String columnName = getColumnName(md.getColumnName(i));
                    transformer.start(columnName, EMPTY_ATTRIBUTES);
                    serializeData(manager, getColumnValue(i));
                    transformer.end(columnName);
                }
            } else if (isupdate && !isstoredprocedure) {
                transformer.start("returncode", EMPTY_ATTRIBUTES);
                serializeData(manager, String.valueOf(rv));
                transformer.end("returncode");
                rv = -1; // we only want the return code shown once.
            }
        }

        protected void serializeStoredProcedure(ServiceManager manager)
        throws SQLException, SAXException {
            if (outParametersNames == null || cst == null) {
                return;
            }

            // make sure output follows order as parameter order in stored procedure
            Iterator itOutKeys = new TreeMap(outParameters).keySet().iterator();
            while (itOutKeys.hasNext()) {
                Integer counter = (Integer) itOutKeys.next();
                try {
                    Object obj = cst.getObject(counter.intValue());
                    if (!(obj instanceof ResultSet)) {
                        transformer.start((String) outParametersNames.get(counter), EMPTY_ATTRIBUTES);
                        serializeData(manager, SQLTransformer.getStringValue(obj));
                        transformer.end((String) outParametersNames.get(counter));
                    } else {
                        ResultSet rs = (ResultSet) obj;
                        try {
                            transformer.start((String) outParametersNames.get(counter), EMPTY_ATTRIBUTES);
                            ResultSetMetaData md = rs.getMetaData();
                            while (rs.next()) {
                                transformer.start(this.row_name, EMPTY_ATTRIBUTES);
                                for (int i = 1; i <= md.getColumnCount(); i++) {
                                    String columnName = getColumnName(md.getColumnName(i));
                                    transformer.start(columnName, EMPTY_ATTRIBUTES);
                                    if (md.getColumnType(i) == 8) {  // prevent nasty exponent notation
                                        serializeData(manager, SQLTransformer.getStringValue(rs.getBigDecimal(i)));
                                    } else {
                                        serializeData(manager, SQLTransformer.getStringValue(rs.getObject(i)));
                                    }
                                    transformer.end(columnName);
                                }
                                transformer.end(this.row_name);
                            }
                        } finally {
                            try {
                                rs.close();
                            } catch (SQLException ignored) { }
                        }
                        transformer.end((String) outParametersNames.get(counter));
                    }
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
