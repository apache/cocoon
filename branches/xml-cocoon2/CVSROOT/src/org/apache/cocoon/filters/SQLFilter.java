/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.filters;

import org.apache.cocoon.Parameters;
import org.apache.cocoon.Request;
import org.apache.cocoon.Response;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.ext.LexicalHandler;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.IOException;

/**
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @author <a href="mailto:giacomo.pati@pwr.ch">Giacomo Pati</a>
 *         (PWR Organisation & Entwicklung)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-03-22 07:35:31 $ $Author: balld $
 */

public class SQLFilter extends AbstractFilter {

    /** The SQL namespace **/
    public static final String my_uri = "http://xml.apache.org/cocoon/SQL";
//    public static final String my_prefix = "sql";
    public static final String my_name = "SQLFilter";

    /** The SQL namespace element names **/
    public static final String MAGIC_EXECUTE_QUERY = "execute-query";
    public static final String MAGIC_DRIVER = "driver";
    public static final String MAGIC_DBURL = "dburl";
    public static final String MAGIC_USERNAME = "username";
    public static final String MAGIC_PASSWORD = "password";
    public static final String MAGIC_QUERY = "query";

    /** The states we are allowed to be in **/
    public static final int STATE_NORMAL = 0;
    public static final int STATE_AWAITING_ELEMENT = 1;
    public static final int STATE_AWAITING_CHARACTERS = 2;

    /** SAX consuming state information **/
    protected int current_state;
    protected String current_element_name;
    protected StringBuffer current_element_value;

    /** SAX producing state information **/
    protected XMLConsumer xml_consumer;
    //protected ContentHandler content_handler;
    protected LexicalHandler lexical_handler;

    /** SQL configuration information **/
    protected String default_driver;
    protected String default_dburl;
    protected String default_username;
    protected String default_password;
    protected String driver;
    protected String dburl;
    protected String username;
    protected String password;
    protected String query;
	protected boolean indent_output;

    /** Dummy static variables for the moment **/
    protected String rowset_name = "ROWSET";
    protected String row_name = "ROW";


    /** BEGIN SitemapComponent methods **/

    public void setup(Request request, Response response, 
                      String source, Parameters parameters) 
            throws ProcessingException, SAXException, IOException {
        current_state = STATE_NORMAL;

        // Check the driver
        this.default_driver = parameters.getParameter("driver",null);

        // Check the dburl
        this.default_dburl = parameters.getParameter("dburl",null);

        // Check the username
        this.default_username = parameters.getParameter("username",null);

        // Check the password
        this.default_password = parameters.getParameter("password",null);

		// Check the indenting
		String parameter = parameters.getParameter("indent-output",null);
		if (parameter != null && parameter.toLowerCase().equals("yes")) {
			this.indent_output = true;
		} else {
			this.indent_output = false;
		}
    }

    /** END SitemapComponent methods **/

    /** BEGIN My very own methods. Well, method. I only have one of my own. **/

    /**
     * This will be the meat of SQLFilter, where the query is run.
     */
    protected void executeQuery() throws Exception {
        String use_driver = driver == null ? default_driver : driver;
        String use_dburl = dburl == null ? default_dburl : dburl;
        String use_username = username == null ? default_username : username;
        String use_password = password == null ? default_password : password;

        AttributesImpl attr=new AttributesImpl();
        Class.forName(use_driver).newInstance();
        Connection conn;
        if (use_username == null || use_password == null) { 
            conn = DriverManager.getConnection(use_dburl);
        } else {
            conn = DriverManager.getConnection(use_dburl,use_username,use_password);
        }
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            ResultSetMetaData md = rs.getMetaData();
            int count = md.getColumnCount();
            this.contentHandler.startPrefixMapping("",my_uri);
            this.start(rowset_name,attr);
            if (indent_output) this.data("\n");
            while (rs.next()) {
                this.data("  ");
                this.start(row_name,attr);
                this.data("\n");
                for (int i=1; i<=count; i++) {
                    String name = md.getColumnName(i);
                    if (indent_output) this.data("    ");
                    this.start(name,attr);
                    this.data(rs.getString(i));
                    this.end(name);
                    if (indent_output) this.data("\n");
                }
                if (indent_output) this.data("  ");
                this.end(row_name);
                if (indent_output) this.data("\n");
            }
            this.end(rowset_name);
            if (indent_output) this.data("\n");
            this.contentHandler.endPrefixMapping("");
            rs.close();
            st.close();
        } finally {
            conn.close();
        }
    }

    protected static void throwIllegalStateException(String message) {
        throw new IllegalStateException(my_name+": "+message);
    }

    /** END My very own methods. **/

    /** BEGIN SAX ContentHandler handlers **/

    public void startElement(String uri, String name, String raw, Attributes attributes) 
            throws SAXException {
        if (!uri.equals(my_uri)) {
            super.startElement(uri,name,raw,attributes);
            return;
        }
        if (name.equals(MAGIC_EXECUTE_QUERY)) {
            if (current_state != STATE_NORMAL) {
                throwIllegalStateException("You may not nest "
                        +raw+" inside another "
                        +MAGIC_EXECUTE_QUERY+" element.");
            }
            current_state = STATE_AWAITING_ELEMENT;
            return;
        }
        if (current_state != STATE_AWAITING_ELEMENT) {
            throwIllegalStateException("The "
                    +raw+" element is not allowed outside of the "
                    +MAGIC_EXECUTE_QUERY+" element.");
        }
        current_element_name = name;
        current_state = STATE_AWAITING_CHARACTERS;
    }

    public void endElement(String uri, String name, String raw) 
            throws SAXException {
        if (!uri.equals(my_uri)) {
            super.endElement(uri,name,raw);
            return;
        }
        if (name.equals(MAGIC_EXECUTE_QUERY)) {
            try {
                executeQuery();
            } catch (Exception e) {}
            current_state = STATE_NORMAL;
            driver = null;
            dburl = null;
            username = null;
            password = null;
            query = null;
            return;
        }
        if (current_element_name.equals(MAGIC_DRIVER)) {
            driver = current_element_value.toString();
        } else if (current_element_name.equals(MAGIC_DBURL)) {
            dburl = current_element_value.toString();
        } else if (current_element_name.equals(MAGIC_USERNAME)) {
            username = current_element_value.toString();
        } else if (current_element_name.equals(MAGIC_PASSWORD)) {
            password = current_element_value.toString();
        } else if (current_element_name.equals(MAGIC_QUERY)) {
            query = current_element_value.toString();
        }
            current_state = STATE_AWAITING_ELEMENT;
    }

    public void characters(char ary[], int start, int length) 
            throws SAXException {
        if (current_state != STATE_AWAITING_CHARACTERS) {
            super.characters(ary,start,length);
        }
        current_element_value = new StringBuffer (50);
        current_element_value.append(ary,start,length);
    }

    private void attribute(AttributesImpl attr, String name, String value) {
        attr.addAttribute("",name,name,"CDATA",value);
    }

    private void start(String name, AttributesImpl attr)
    throws SAXException {
        super.contentHandler.startElement(my_uri,name,name,attr);
        attr.clear();
    }

    private void end(String name)
    throws SAXException {
        super.contentHandler.endElement(my_uri,name,name);
    }

    private void data(String data)
    throws SAXException {
        if (data != null)
            super.contentHandler.characters(data.toCharArray(),0,data.length());
    }
}
