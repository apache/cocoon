/*
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
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
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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

package org.apache.cocoon.processor.sql;

import java.net.URLEncoder;
import java.sql.*;
import java.text.*;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.w3c.dom.*;
import org.apache.cocoon.framework.*;
import org.apache.cocoon.processor.*;

/**
 * A processor that performs SQL database queries.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version $Revision: 1.6 $ $Date: 1999-12-07 09:20:59 $
 */

public class SQLProcessor extends AbstractActor implements Processor, Status {

    /**
     * A table of already instantiated drivers to avoid memory
     * leak on stupid JVM implementations
     */
    protected static Hashtable drivers = new Hashtable();

    /**
     * A table of already instantiated creators for the same rason
     */
    protected static Hashtable query_creators = new Hashtable();

    /**
     * The default query creator, does {@...} substitution
     */
    protected static QueryCreator default_query_creator = new DefaultQueryCreator();

    /**
     * Omit null columns
     */
    protected static final int OMIT_NULLS = 0;

    /**
     * Print null columns with NULL attribute
     */
    protected static final int ATTRIBUTE_NULLS = 1;

    /**
     * Process the DOM tree.
     */
    public Document process(Document document, Dictionary parameters) throws Exception {
        HttpServletRequest request = (HttpServletRequest)parameters.get("request");
        try {
            ConnectionDefs cdefs = new ConnectionDefs(document);
            NodeList query_nodes = document.getElementsByTagName("query");
            Node query_nodes_ary[] = new Node[query_nodes.getLength()];

            for (int i=0; i<query_nodes.getLength(); i++) {
                query_nodes_ary[i] = query_nodes.item(i);
            }

            for (int i=0; i<query_nodes_ary.length; i++) {
                Node query_node = query_nodes_ary[i];
                if (query_node.getNodeType() != Node.ELEMENT_NODE) continue;
                Element query_element = (Element)query_node;
                String defs = query_element.getAttribute("defs");
                Properties query_props = cdefs.getQueryProperties(defs);
                NamedNodeMap query_attributes = query_element.getAttributes();

                for (int j=0; j<query_attributes.getLength(); j++) {
                    Node query_attribute = query_attributes.item(j);
                    query_props.put(query_attribute.getNodeName(),query_attribute.getNodeValue());
                }
                NodeList child_list = query_element.getChildNodes();
                for (int j=0; j<child_list.getLength(); j++) {
                    Node child_node = child_list.item(j);
                    if (child_node.getNodeName().equals("request") && child_node.getNodeType() == Node.ELEMENT_NODE) {
                        Element request_element = (Element)child_node;
                        NamedNodeMap request_attributes = request_element.getAttributes();
                        for (int k=0; k<request_attributes.getLength(); k++) {
                            Node request_attribute = request_attributes.item(k);
                            String attr_name = request_attribute.getNodeName();
                            String req_name = request_attribute.getNodeValue();
                            if (req_name.equals("")) req_name = attr_name;
                            String value = request.getParameter(req_name);
                            if (value == null) value = "";
                            query_props.put(attr_name,value);
                        }
                    }
                }
                Connection conn = cdefs.getConnection(query_props.getProperty("connection"));
                processQuery(document,parameters,query_element,query_props,conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return document;
    }

    /**
     * Process a single query node
     */
    protected void processQuery(Document document, Dictionary parameters, Element query_element, Properties query_props, Connection conn) throws Exception {
        HttpServletRequest req = (HttpServletRequest)parameters.get("request");
        String doc_element_name = query_props.getProperty("doc-element");
        String row_element_name = query_props.getProperty("row-element");
        boolean create_row_elements = true;
        if (row_element_name.equals("")) create_row_elements = false;
        int max_rows = getIntProperty(query_props,"max-rows",-1);
        int skip_rows = getIntProperty(query_props,"skip-rows",0);
        String id_attribute = query_props.getProperty("id-attribute");
        boolean create_id_attribute = true;
        if (id_attribute.equals("")) create_id_attribute = false;
        String id_attribute_column = query_props.getProperty("id-attribute-column");
        String null_indicator = query_props.getProperty("null-indicator");
        String tag_case = query_props.getProperty("tag-case");

        String query_creator_name = query_props.getProperty("creator");
        String count_attribute = query_props.getProperty("count-attribute");
        String query_attribute = query_props.getProperty("query-attribute");
        String skip_rows_attribute = query_props.getProperty("skip-rows-attribute");
        String max_rows_attribute = query_props.getProperty("max-rows-attribute");
        String update_rows_attribute = query_props.getProperty("update-rows-attribute");
        String query = query_props.getProperty("query");
        String namespace = query_props.getProperty("namespace");
		DateFormat timestamp_format = null;
		String timestamp_format_string = query_props.getProperty("timestamp_format");
		if (timestamp_format_string != null)
			timestamp_format = new SimpleDateFormat(timestamp_format_string);
        try {
            if (query == null || query.equals("")) {
                QueryCreator query_creator;
                if (query_creator_name != null) {
                    if (query_creators.containsKey(query_creator_name)) {
                        query_creator = (QueryCreator)query_creators.get(query_creator_name);
                    } else {
                        query_creator = (QueryCreator)Class.forName(query_creator_name).newInstance();
                        query_creators.put(query_creator_name,query_creator);
                    }
                } else {
                    query_creator = default_query_creator;
                }
                NodeList query_text_nodes = query_element.getChildNodes();
                StringBuffer query_buffer = new StringBuffer();
                for (int i=0; i<query_text_nodes.getLength(); i++) {
                    Node query_text_node = query_text_nodes.item(i);
                    if (query_text_node.getNodeType() == Node.TEXT_NODE) {
                        query_buffer.append(query_text_node.getNodeValue());
                    }
                }
                query = query_creator.getQuery(conn,query_buffer.toString(),query_element,query_props,parameters);
            }
            Statement st = conn.createStatement();
            ResultSet rs;
            Node results_node;
            Element results_element = null;
            if (doc_element_name.equals("")) {
                results_node = document.createDocumentFragment();
            } else {
                results_element = Utils.createElement(document,namespace,doc_element_name);
                results_node = results_element;
                if (!count_attribute.equals("")) {
                    String count_query = getCountQuery(query);
                    if (count_query != null) {
                        rs = st.executeQuery(count_query);
                        if (rs.next())
                            results_element.setAttribute(count_attribute,rs.getString(1));
                        rs.close();
                    }
                }
                if (!query_attribute.equals(""))
                    results_element.setAttribute(query_attribute,URLEncoder.encode(query));
                if (!skip_rows_attribute.equals(""))
                    results_element.setAttribute(skip_rows_attribute,""+skip_rows);
                if (!max_rows_attribute.equals(""))
                    results_element.setAttribute(max_rows_attribute,""+max_rows);
            }
            if (!update_rows_attribute.equals("")) {
                int update_rows = st.executeUpdate(query);
                if (results_element != null)
                    results_element.setAttribute(update_rows_attribute,""+update_rows);
            } else {
                rs = st.executeQuery(query);
                ResultSetMetaData md = rs.getMetaData();
                Column columns[] = getColumns(md,tag_case);
                int id_attribute_column_index = -1;
                if (create_id_attribute) {
                    for (int i=0; i<columns.length; i++) {
                        if (columns[i].name.equals(id_attribute_column)) {
                            id_attribute_column_index = i;
                        }
                    }
                }
                int null_mode = OMIT_NULLS;
                if (null_indicator.equals("y")) {
                    null_mode = ATTRIBUTE_NULLS;
                } else if (null_indicator.equals("yes")) {
                    null_mode = ATTRIBUTE_NULLS;
                }

                Element column_element;
                Node row_node = results_node;
                Element row_element = null;
                String value;
                int count = 0;
                if (skip_rows > 0) {
                    while (rs.next()) {
                        count++;
                        if (count == skip_rows) break;
                    }
                }
				ColumnFormatter formatter = new ColumnFormatter(query_element);
                while (rs.next()) {
                    if (create_row_elements) {
                        row_element = Utils.createElement(document,namespace,row_element_name);
                        row_node = row_element;
                        if (create_id_attribute && id_attribute_column_index == -1) {
                            row_element.setAttribute(id_attribute,"" + count);
                        }
                    }

                    for (int i=0; i<columns.length; i++) {
						value = formatter.formatColumn(columns[i],rs,i+1);
						/*
						switch(columns[i].type) {
							case Types.TIMESTAMP:
								if (timestamp_format != null)
									value = timestamp_format.format(rs.getDate(i+1));
								else
                        			value = rs.getString(i+1);
								break;
							default:
								value = rs.getString(i+1);
						}
						*/
                        if (create_row_elements && create_id_attribute && id_attribute_column_index == i) {
                            row_element.setAttribute(id_attribute,value);
                            continue;
                        }
                        if (value == null && null_mode == OMIT_NULLS) continue;
                        column_element = Utils.createElement(document,namespace,columns[i].name);
                        if (value == null && null_mode == ATTRIBUTE_NULLS) {
                            column_element.setAttribute("NULL","YES");
                            column_element.appendChild(document.createTextNode(""));
                        } else {
                            column_element.appendChild(document.createTextNode(value));
                        }
                        row_node.appendChild(column_element);
                    }
                    if (create_row_elements) results_node.appendChild(row_node);
                    if (count-skip_rows == max_rows-1) break;
                    count++;
                }
                rs.close();
            }
            st.close(); conn.commit();
            query_element.getParentNode().replaceChild(results_node,query_element);
        } catch (Exception e) {
            Element error_element = Utils.createErrorElement(document,namespace,query_props,e);
            query_element.getParentNode().replaceChild(error_element,query_element);
            conn.rollback();
        } finally {
          conn.close();
        }
    }

    /**
     * Right now, always return true. How else do we handle this?
     */
    public boolean hasChanged(Object context) {
        return true;
    }

    public String getStatus() {
        return "SQL Processor";
    }

    protected Column[] getColumns(ResultSetMetaData md, String tag_case) throws SQLException {
        Column columns[] = new Column[md.getColumnCount()];
        if (tag_case.equals("preserve")) {
            for (int i=0; i<columns.length; i++) {
                columns[i] = new Column(md.getColumnName(i+1),md.getColumnTypeName(i+1));
            }
        } else if (tag_case.equals("lower")) {
            for (int i=0; i<columns.length; i++) {
                columns[i] = new Column(md.getColumnName(i+1).toLowerCase(),md.getColumnTypeName(i+1));
            }
        } else if (tag_case.equals("upper")) {
            for (int i=0; i<columns.length; i++) {
                columns[i] = new Column(md.getColumnName(i+1).toUpperCase(),md.getColumnTypeName(i+1));
            }
        }
        return columns;
    }

    protected static int getIntProperty(Properties props, String name, int def) {
        String value = props.getProperty(name);
        if (value == null) return def;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static String getCountQuery(String query) {
        String lowercase_query = query.toLowerCase();
        int select_index = lowercase_query.indexOf("select ");
        int from_index = lowercase_query.indexOf(" from ");
        if (select_index < 0 || from_index < 0) return null;
        String columns = query.substring(select_index+7,from_index);
        int comma_index = columns.indexOf(',');
        String column;
        if (comma_index < 0) column = columns;
        else column = columns.substring(0,comma_index);
        return "SELECT count("+column+") FROM "+ query.substring(from_index+6);
    }

}
