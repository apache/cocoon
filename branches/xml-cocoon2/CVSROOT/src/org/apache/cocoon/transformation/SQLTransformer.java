/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Vector;
import java.util.Map;
import java.util.Enumeration;

import org.apache.avalon.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;
import org.apache.cocoon.util.ClassUtils;

import org.apache.log.Logger;
import org.apache.log.LogKit;

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.ext.LexicalHandler;

/**
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @author <a href="mailto:giacomo.pati@pwr.ch">Giacomo Pati</a>
 *         (PWR Organisation & Entwicklung)
 * @version CVS $Revision: 1.1.2.13 $ $Date: 2000-11-30 21:42:32 $ $Author: bloritsch $
 */

public class SQLTransformer extends AbstractTransformer {

    private Logger log = LogKit.getLoggerFor("cocoon");

    /** The SQL namespace **/
    public static final String my_uri = "http://apache.org/cocoon/SQL";
    public static final String my_name = "SQLTransformer";

    /** The SQL namespace element names **/
    public static final String MAGIC_EXECUTE_QUERY = "execute-query";
    public static final String MAGIC_DRIVER = "driver";
    public static final String MAGIC_DBURL = "dburl";
    public static final String MAGIC_USERNAME = "username";
    public static final String MAGIC_PASSWORD = "password";
    public static final String MAGIC_QUERY = "query";
    public static final String MAGIC_VALUE = "value";
    public static final String MAGIC_DOC_ELEMENT = "doc-element";
    public static final String MAGIC_ROW_ELEMENT = "row-element";
    public static final String MAGIC_ANCESTOR_VALUE = "ancestor-value";
    public static final String MAGIC_ANCESTOR_VALUE_LEVEL_ATTRIBUTE = "level";
    public static final String MAGIC_ANCESTOR_VALUE_NAME_ATTRIBUTE = "name";
    public static final String MAGIC_UPDATE_ATTRIBUTE = "isupdate";

    /** The states we are allowed to be in **/
    public static final int STATE_OUTSIDE = 0;
    public static final int STATE_INSIDE_EXECUTE_QUERY_ELEMENT = 1;
    public static final int STATE_INSIDE_VALUE_ELEMENT = 2;
    public static final int STATE_INSIDE_QUERY_ELEMENT = 3;
    public static final int STATE_INSIDE_ANCESTOR_VALUE_ELEMENT = 4;

    /** Default parameters that might apply to all queries **/
    protected Properties default_properties = new Properties();

    /** The list of queries that we're currently working on **/
    protected Vector queries = new Vector();

    /** The offset of the current query in the queries list **/
    protected int current_query_index = -1;

    /** The name of the value element we're currently receiving **/
    protected String current_name;

    /** The current state of the event receiving FSM **/
    protected int current_state = STATE_OUTSIDE;

    /** The value of the value element we're currently receiving **/
    protected StringBuffer current_value = new StringBuffer();

    /** SAX producing state information **/
    protected XMLConsumer xml_consumer;
    protected LexicalHandler lexical_handler;

    /** BEGIN SitemapComponent methods **/

    public void setup(EntityResolver resolver, Map objectModel,
                      String source, Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        current_state = STATE_OUTSIDE;

        // Check the driver
        String parameter = parameters.getParameter("driver",null);
        if (parameter != null) {
            log.debug("DRIVER: "+parameter);

            default_properties.setProperty("driver",parameter);
        }

        // Check the dburl
        parameter = parameters.getParameter("dburl",null);
        if (parameter != null) {
            log.debug("DBURL: "+parameter);

            default_properties.setProperty("dburl",parameter);
        }

        // Check the username
        parameter = parameters.getParameter("username",null);
        if (parameter != null) {
            log.debug("USERNAME: "+parameter);

            default_properties.setProperty("username",parameter);
        }

        // Check the password
        parameter = parameters.getParameter("password",null);
        if (parameter != null) {
            default_properties.setProperty("password",parameter);
        }

    }

    /** END SitemapComponent methods **/

    /** BEGIN my very own methods **/

    /**
     * This will be the meat of SQLTransformer, where the query is run.
     */
    protected void executeQuery(int index) throws SAXException {
        this.contentHandler.startPrefixMapping("",my_uri);
        AttributesImpl attr = new AttributesImpl();
        Query query = (Query) queries.elementAt(index);
        try {
            query.execute();
        } catch (SQLException e) {
            throw new SAXException(e);
        }
        this.start(query.rowset_name, attr);
        try {
            while (query.next()) {
                this.start(query.row_name, attr);
                query.serializeRow();
                if (index + 1 < queries.size()) {
                    executeQuery(index + 1);
                }
                this.end(query.row_name);
            }
        } catch (SQLException e) {
            throw new SAXException(e);
        }
        this.end(query.rowset_name);
        this.contentHandler.endPrefixMapping("");
    }

    protected static void throwIllegalStateException(String message) {
        throw new IllegalStateException(my_name + ": "+message);
    }

    protected void startExecuteQueryElement() {
        switch (current_state) {
            case STATE_OUTSIDE:
            case STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                current_query_index = queries.size();
                Query query = new Query(this, current_query_index);
                queries.addElement(query);
                current_state = STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
                break;
            default:
                throwIllegalStateException("Not expecting a start execute query element");
        }
    }

    protected void startValueElement(String name) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                current_name = name;
                current_value.setLength(0);
                current_state = STATE_INSIDE_VALUE_ELEMENT;
                break;
            default:
                throwIllegalStateException("Not expecting a start value element: "+
                                           name);
        }
    }

    protected void startQueryElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                current_value.setLength(0);
                current_state = STATE_INSIDE_QUERY_ELEMENT;
                String isupdate =
                    attributes.getValue("", MAGIC_UPDATE_ATTRIBUTE);
        if (isupdate != null && !isupdate.equalsIgnoreCase("false"))
                    getCurrentQuery().setUpdate(true);
                break;
            default:
                throwIllegalStateException("Not expecting a start query element");
        }
    }

    protected void endQueryElement() {
        switch (current_state) {
            case STATE_INSIDE_QUERY_ELEMENT:
                if (current_value.length() > 0) {
                    getCurrentQuery().addQueryPart(
                      current_value.toString());
                    log.debug("QUERY IS \""+
                                           current_value.toString() + "\"");

                    current_value.setLength(0);
                }
                current_state = STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
                break;
            default:
                throwIllegalStateException("Not expecting a stop query element");
        }
    }

    protected void endValueElement() {
        switch (current_state) {
            case STATE_INSIDE_VALUE_ELEMENT:
                getCurrentQuery().setParameter(current_name,
                                               current_value.toString());
                log.debug("SETTING VALUE ELEMENT name {"+
                                       current_name + "} value {"+
                                       current_value.toString() + "}");

                current_state = STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
                break;
            default:
                throwIllegalStateException("Not expecting an end value element");
        }
    }

    protected void endExecuteQueryElement() throws SAXException {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY_ELEMENT:
                if (current_query_index == 0) {
                    executeQuery(0);
                    queries.removeAllElements();
                    current_state = STATE_OUTSIDE;
                } else {
                    current_query_index--;
                    current_state = STATE_INSIDE_EXECUTE_QUERY_ELEMENT;
                }
                break;
            default:
                throwIllegalStateException("Not expecting an end execute query element");
        }
    }

    protected void startAncestorValueElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_QUERY_ELEMENT:
                int level = 0;
                try {
                    level = Integer.parseInt( attributes.getValue(my_uri,
                                              MAGIC_ANCESTOR_VALUE_LEVEL_ATTRIBUTE));
                } catch (Exception e) {
                    throwIllegalStateException("Ancestor value elements must have a "+
                                               MAGIC_ANCESTOR_VALUE_LEVEL_ATTRIBUTE + " attribute");
                }
                String name = attributes.getValue(my_uri,
                                                  MAGIC_ANCESTOR_VALUE_NAME_ATTRIBUTE);
                if (name == null) {
                    throwIllegalStateException("Ancestor value elements must have a "+
                                               MAGIC_ANCESTOR_VALUE_NAME_ATTRIBUTE + " attribute");
                }
                AncestorValue av = new AncestorValue(level, name);
                log.debug("ANCESTOR VALUE "+level + " "+name);

                if (current_value.length() > 0) {
                    getCurrentQuery().addQueryPart(
                      current_value.toString());
                    log.debug("QUERY IS \""+
                                           current_value.toString() + "\"");

                    current_value.setLength(0);
                }
                getCurrentQuery().addQueryPart(av);
                current_state = STATE_INSIDE_ANCESTOR_VALUE_ELEMENT;
                break;
            default:
                throwIllegalStateException("Not expecting a start ancestor value element");
        }
    }

    protected void endAncestorValueElement() {
        current_state = STATE_INSIDE_QUERY_ELEMENT;
    }

    protected Query getCurrentQuery() {
        return (Query) queries.elementAt(current_query_index);
    }

    protected Query getQuery(int i) {
        return (Query) queries.elementAt(i);
    }

    /** END my very own methods **/

    /** BEGIN SAX ContentHandler handlers **/

    public void setDocumentLocator(Locator locator) {
        log.info("PUBLIC ID"+locator.getPublicId());
        log.info("SYSTEM ID"+locator.getSystemId());
        if (super.contentHandler != null)
            super.contentHandler.setDocumentLocator(locator);
    }

    public void startElement(String uri, String name, String raw,
                             Attributes attributes) throws SAXException {
        if (!uri.equals(my_uri)) {
            super.startElement(uri, name, raw, attributes);
            return;
        }
        log.debug("RECEIVED START ELEMENT "+name);

        if (name.equals(MAGIC_EXECUTE_QUERY)) {
            startExecuteQueryElement();
        } else if (name.equals(MAGIC_QUERY)) {
            startQueryElement(attributes);
        } else if (name.equals(MAGIC_ANCESTOR_VALUE)) {
            startAncestorValueElement(attributes);
        } else {
            startValueElement(name);
        }
    }

    public void endElement(String uri, String name,
                           String raw) throws SAXException {
        if (!uri.equals(my_uri)) {
            super.endElement(uri, name, raw);
            return;
        }
        log.debug("RECEIVED END ELEMENT "+name + "("+uri +
                               ")");

        if (name.equals(MAGIC_EXECUTE_QUERY)) {
            endExecuteQueryElement();
        } else if (name.equals(MAGIC_QUERY)) {
            endQueryElement();
        } else if (name.equals(MAGIC_ANCESTOR_VALUE)) {
            endAncestorValueElement();
        } else if (name.equals(MAGIC_VALUE) || current_state == STATE_INSIDE_VALUE_ELEMENT) {
            endValueElement();
        } else {
            super.endElement(uri, name, raw);
        }
    }

    public void characters(char ary[], int start,
                           int length) throws SAXException {
        if (current_state != STATE_INSIDE_VALUE_ELEMENT &&
                current_state != STATE_INSIDE_QUERY_ELEMENT) {
            super.characters(ary, start, length);
        }
        log.debug("RECEIVED CHARACTERS: "+
                               new String(ary, start, length));

        current_value.append(ary, start, length);
    }

    private void attribute(AttributesImpl attr, String name, String value) {
        attr.addAttribute("",name, name, "CDATA",value);
    }

    private void start(String name,
                       AttributesImpl attr) throws SAXException {
        super.contentHandler.startElement(my_uri, name, name, attr);
        attr.clear();
    }

    private void end(String name) throws SAXException {
        super.contentHandler.endElement(my_uri, name, name);
    }

    private void data(String data) throws SAXException {
        if (data != null)
            super.contentHandler.characters(data.toCharArray(), 0,
                                            data.length());
    }

    protected static String getStringValue(Object object) {
        if (object instanceof byte[]) {
            return new String((byte[]) object);
        } else if (object instanceof char[]) {
            return new String((char[]) object);
        } else if (object != null) {
            return object.toString();
        } else {
            return "";
        }
    }

    class Query {

        /** Who's your daddy? **/
        protected SQLTransformer transformer;

        /** What index are you in daddy's queries list **/
        protected int query_index;

        /** SQL configuration information **/
        protected Properties properties;

        /** Dummy static variables for the moment **/
        protected String rowset_name = "ROWSET";
        protected String row_name = "ROW";

        /** The connection, once opened **/
        protected Connection conn;

        /** And the statement **/
        protected Statement st;

        /** The results, of course **/
        protected ResultSet rs = null;

        /** And the results' metadata **/
        protected ResultSetMetaData md = null;

    /** If this query is actually an update (insert, update, delete) **/
    protected boolean isupdate = false;

    /** If it is an update/etc, the return value (num rows modified) **/
    protected int rv = -1;

        /** The parts of the query **/
        protected Vector query_parts = new Vector();

        protected Query(SQLTransformer transformer, int query_index) {
            this.transformer = transformer;
            this.query_index = query_index;
            this.properties = new Properties(transformer.default_properties);
        }

        protected void setParameter(String name, String value) {
            properties.setProperty(name, value);
        }

        protected void setUpdate(boolean flag) {
            isupdate = flag;
        }

        protected void execute() throws SQLException {
            String driver = properties.getProperty(transformer.MAGIC_DRIVER);
            try {
                ClassUtils.newInstance(driver);
            } catch (Exception e) {}
            if (null != properties.getProperty(transformer.MAGIC_DOC_ELEMENT)) {
                this.rowset_name = properties.getProperty(transformer.MAGIC_DOC_ELEMENT);
            }
            if (null != properties.getProperty(transformer.MAGIC_ROW_ELEMENT)) {
                this.row_name = properties.getProperty(transformer.MAGIC_ROW_ELEMENT);
            }
            String dburl = properties.getProperty(transformer.MAGIC_DBURL);
            String username = properties.getProperty(transformer.MAGIC_USERNAME);
            String password = properties.getProperty(transformer.MAGIC_PASSWORD);
            Enumeration enum = query_parts.elements();
            StringBuffer sb = new StringBuffer();
            while (enum.hasMoreElements()) {
                Object object = enum.nextElement();
                if (object instanceof String) {
                    sb.append((String) object);
                } else if (object instanceof AncestorValue) {
                    /** Do a lookup into the ancestors' result's values **/
                    AncestorValue av = (AncestorValue) object;
                    Query query = transformer.getQuery(query_index - av.level);
                    try {
                        sb.append(query.getColumnValue(av.name));
                    } catch (SQLException e) {
                        close();
                        throw e;
                    }
                }
            }
            String query = sb.toString();
            try {
                if (username == null || password == null) {
                    conn = DriverManager.getConnection(dburl);
                } else {
                    conn = DriverManager.getConnection(dburl, username,
                                                       password);
                }
                st = conn.createStatement();
                if (isupdate)
                        rv = st.executeUpdate(query);
                else {
                        rs = st.executeQuery(query);
                        md = rs.getMetaData();
                }
            } catch (SQLException e) {
                log.error("Caught a SQLException", e);
                conn.close();
                throw e;
            }
        }

        protected String getColumnValue(int i) throws SQLException {
            try {
                return transformer.getStringValue(rs.getObject(i));
            } catch (SQLException e) {
                close();
                throw e;
            }
        }

        protected String getColumnValue(String name) throws SQLException {
            try {
                return transformer.getStringValue(rs.getObject(name));
            } catch (SQLException e) {
                close();
                throw e;
            }
        }

        protected boolean next() throws SQLException {
            try {
                // if rv is not -1, then an SQL insert, update, etc, has
        // happened (see JDBC docs - return codes for executeUpdate)
            if (rv != -1)
            return true;
                if (rs == null || !rs.next()) {
                    close();
                    return false;
                }
                return true;
            } catch (SQLException e) {
                close();
                throw e;
            }
        }

        protected void close() throws SQLException {
            try {
                if (rs != null)
                    rs.close();
                st.close();
            } finally {
                conn.close();
            }
        }

        protected void addQueryPart(Object object) {
            query_parts.addElement(object);
        }

        protected void serializeRow() throws SQLException, SAXException {
            AttributesImpl attr = new AttributesImpl();

        if (!isupdate) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    transformer.start(md.getColumnName(i).toLowerCase(), attr);
                    try {
                        transformer.data(getColumnValue(i));
                    } catch (SQLException e) {
                        close();
                        throw e;
                    }
                    transformer.end(md.getColumnName(i).toLowerCase());
                }
            } else {
                transformer.start("returncode", attr);
                transformer.data(String.valueOf(rv));
                transformer.end("returncode");
                rv = -1; // we only want the return code shown once.
        }
        }

    }

    class AncestorValue {

        protected int level;
        protected String name;

        protected AncestorValue(int level, String name) {
            this.level = level;
            this.name = name;
        }

    }

}
