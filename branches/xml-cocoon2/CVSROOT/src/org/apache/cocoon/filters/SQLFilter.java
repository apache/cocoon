package org.apache.cocoon.filters;

import org.apache.cocoon.Parameters;
import org.apache.cocoon.Request;
import org.apache.cocoon.Response;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
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

public class SQLFilter extends AbstractFilter {

	/** The SQL namespace **/
	public static final String my_uri = "http://xml.apache.org/cocoon/SQL";
	public static final String my_prefix = "sql";
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

	/** SAX producing state informatin **/
	protected XMLConsumer xml_consumer;
	protected ContentHandler content_handler;
	protected LexicalHandler lexical_handler;

	/** SQL configuration information **/
	protected String driver;
	protected String dburl;
	protected String username;
	protected String password;
	protected String query;

	/** Dummy static variables for the moment **/
	protected String rowset_name = "ROWSET";
	protected String row_name = "ROW";

	/** BEGIN SitemapComponent methods **/

	/** What does Pier want me to do here??? **/
	public void setup(Request request, Response response, String source, Parameters parameters) throws ProcessingException, SAXException, IOException {
		current_state = STATE_NORMAL;
	}

	/** END SitemapComponent methods **/

	/** BEGIN My very own methods. Well, method. I only have one of my own. **/

	/**
	 * This will be the meat of SQLFilter, where the query is run.
	 */
	protected void executeQuery() throws Exception {
		Class.forName(driver).newInstance();
		Connection conn;
		if (username == null || password == null) {
			conn = DriverManager.getConnection(dburl);
		} else {
			conn = DriverManager.getConnection(dburl,username,password);
		}
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			ResultSetMetaData md = rs.getMetaData();
			int count = md.getColumnCount();
			content_handler.startElement(my_uri,rowset_name,my_prefix+rowset_name,null);
			while (rs.next()) {
				content_handler.startElement(my_uri,row_name,my_prefix+row_name,null);
				for (int i=1; i<=count; i++) {
					String name = md.getColumnName(i);
					content_handler.startElement(my_uri,name,my_prefix+name,null);
					String value = rs.getString(i);
					char ary[] = value.toCharArray();
					content_handler.characters(ary,0,ary.length);
					content_handler.endElement(my_uri,name,my_prefix+name);
				}
				content_handler.endElement(my_uri,row_name,my_prefix+row_name);
			}
			content_handler.endElement(my_uri,rowset_name,my_prefix+rowset_name);
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

	public void startElement(String uri, String name, String raw, Attributes attributes) throws SAXException {
		if (!uri.equals(my_uri)) {
			super.startElement(uri,name,raw,attributes);
			return;
		}
		if (name.equals(MAGIC_EXECUTE_QUERY)) {
			if (current_state != STATE_NORMAL) {
				throwIllegalStateException("You may not nest "+raw+" inside another "+MAGIC_EXECUTE_QUERY+" element.");
			}
			current_state = STATE_AWAITING_ELEMENT;
			return;
		}
		if (current_state != STATE_AWAITING_ELEMENT) {
			throwIllegalStateException("The "+raw+" element is not allowed outside of the "+MAGIC_EXECUTE_QUERY+" element.");
		}
		current_element_name = name;
		current_state = STATE_AWAITING_CHARACTERS;
	}

	public void endElement(String uri, String name, String raw) throws SAXException {
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

	public void characters(char ary[], int start, int length) throws SAXException {
		if (current_state != STATE_AWAITING_CHARACTERS) {
			super.characters(ary,start,length);
		}
		current_element_value.append(ary,start,length);
	}

}
