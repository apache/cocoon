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

package org.apache.cocoon.processor.newsql;

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
 * @version $Revision: 1.1 $ $Date: 1999-12-16 08:04:53 $
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

	public static String QUERY = "query";
	public static String DOC_ELEMENT = "doc-element";
	public static String ROW_ELEMENT = "row-element";
	public static String MAX_ROWS = "max-rows";
	public static String SKIP_ROWS = "skip-rows";
	public static String ID_ATTRIBUTE = "id-attribute";
	public static String ID_ATTRIBUTE_COLUMN = "id-attribute-column";
	public static String NULL_INDICATOR = "null-indicator";
	public static String TAG_CASE = "tag-case";
	public static String QUERY_CREATOR_NAME = "creator";
	public static String COUNT_ATTRIBUTE = "count-attribute";
	public static String QUERY_ATTRIBUTE = "query-attribute";
	public static String SKIP_ROWS_ATTRIBUTE = "skip-rows-attribute";
	public static String MAX_ROWS_ATTRIBUTE = "max-rows-attribute";
	public static String UPDATE_ROWS_ATTRIBUTE = "update-rows-attribute";
	public static String NAMESPACE = "namespace";
	public static String REQUEST_VARIABLE_LEFT_DELIMITER = "request-variable-left-delimiter";
	public static String REQUEST_VARIABLE_RIGHT_DELIMITER = "request-variable-right-delimiter";
	public static String SESSION_VARIABLE_LEFT_DELIMITER = "session-variable-left-delimiter";
	public static String SESSION_VARIABLE_RIGHT_DELIMITER = "session-variable-right-delimiter";
	public static String RESULTSET_VARIABLE_LEFT_DELIMITER = "resultset-variable-left-delimiter";
	public static String RESULTSET_VARIABLE_RIGHT_DELIMITER = "resultset-variable-right-delimiter";

	protected static Hashtable default_query_attributes = new Hashtable();
	static {
		default_query_attributes.put(QUERY,"");
		default_query_attributes.put(DOC_ELEMENT,"rowset");
		default_query_attributes.put(ROW_ELEMENT,"row");
		default_query_attributes.put(MAX_ROWS,"");
		default_query_attributes.put(SKIP_ROWS,"");
		default_query_attributes.put(ID_ATTRIBUTE,"id");
		default_query_attributes.put(ID_ATTRIBUTE_COLUMN,"");
		default_query_attributes.put(NULL_INDICATOR,"omit");
		default_query_attributes.put(TAG_CASE,"preserve");
		default_query_attributes.put(QUERY_CREATOR_NAME,"");
		default_query_attributes.put(COUNT_ATTRIBUTE,"");
		default_query_attributes.put(QUERY_ATTRIBUTE,"");
		default_query_attributes.put(SKIP_ROWS_ATTRIBUTE,"");
		default_query_attributes.put(UPDATE_ROWS_ATTRIBUTE,"");
		default_query_attributes.put(NAMESPACE,"");
		default_query_attributes.put(REQUEST_VARIABLE_LEFT_DELIMITER,"{@");
		default_query_attributes.put(REQUEST_VARIABLE_RIGHT_DELIMITER,"}");
		default_query_attributes.put(SESSION_VARIABLE_LEFT_DELIMITER,"{@session.");
		default_query_attributes.put(SESSION_VARIABLE_RIGHT_DELIMITER,"}");
		default_query_attributes.put(RESULTSET_VARIABLE_LEFT_DELIMITER,"{$");
		default_query_attributes.put(RESULTSET_VARIABLE_RIGHT_DELIMITER,"}");
		default_query_attributes.put(Utils.ERROR_ELEMENT,"");
		default_query_attributes.put(Utils.ERROR_MESSAGE_ATTRIBUTE,"");
		default_query_attributes.put(Utils.ERROR_MESSAGE_ELEMENT,"");
		default_query_attributes.put(Utils.ERROR_STACKTRACE_ATTRIBUTE,"");
		default_query_attributes.put(Utils.ERROR_STACKTRACE_ELEMENT,"");
	}

    /**
     * Process the DOM tree.
     */
    public Document process(Document document, Dictionary parameters) throws Exception {
	try {
        HttpServletRequest request = (HttpServletRequest)parameters.get("request");
		IDocument idocument = new IDocument(document);
		IElement ielement = idocument.getIElement("query");
		while (ielement != null) {
			Element request_element = ielement.getChildElement("request");
			if (request_element != null) {
	            NamedNodeMap request_attributes = request_element.getAttributes();
   	         	for (int k=0; k<request_attributes.getLength(); k++) {
   	            	Node request_attribute = request_attributes.item(k);
                	String attr_name = request_attribute.getNodeName();
                	String req_name = request_attribute.getNodeValue();
                	if (req_name.equals("")) req_name = attr_name;
                	String value = request.getParameter(req_name);
                	if (value == null) value = "";
					ielement.setAttribute(attr_name,value);
            	}
			}
			String driver = getAttribute(ielement,"driver");
			if (driver.equals("")) {
				throw new SQLProcessorException("You must supply a driver attribute for each query node");
			}
			if (!drivers.containsKey(driver)) {
				drivers.put(driver,Class.forName(driver).newInstance());
			}
			String dburl = getAttribute(ielement,"dburl");
			if (dburl.equals("")) {
				throw new SQLProcessorException("You must supply a dburl attribute for each query node");
			}
			String username = getAttribute(ielement,"username");
			String password = getAttribute(ielement,"password");
			Connection conn = null;
			try {
				if (username != null && password != null) {
					conn = DriverManager.getConnection(dburl,username,password);
				} else {
					conn = DriverManager.getConnection(dburl);
				}
			} catch (SQLException e) {
				throw new SQLProcessorException("Connection to database failed",e);
			}
			Node results_node = processQuery(document,parameters,request,ielement,conn,null);
			Element query_element = ielement.getElement();
			if (results_node != null) {
				query_element.getParentNode().replaceChild(results_node,query_element);
			}
			ielement = idocument.getIElement("query");
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
        return document;
    }

    /**
     * Process a single query node
     */
    protected Node processQuery(Document document, Dictionary parameters, HttpServletRequest request, IElement ielement, Connection conn, ResultSet super_rs) throws Exception {
        Node results_node = null;
		String doc_element_name = getAttribute(ielement,DOC_ELEMENT);
        String row_element_name = getAttribute(ielement,ROW_ELEMENT);
        boolean create_row_elements = true;
        if (row_element_name.equals("")) create_row_elements = false;
        int max_rows = getIntAttribute(ielement,MAX_ROWS,-1);
        int skip_rows = getIntAttribute(ielement,SKIP_ROWS,0);
        String id_attribute = getAttribute(ielement,ID_ATTRIBUTE);
        boolean create_id_attribute = true;
        if (id_attribute.equals("")) create_id_attribute = false;
        String id_attribute_column = getAttribute(ielement,ID_ATTRIBUTE_COLUMN);
        String null_indicator = getAttribute(ielement,NULL_INDICATOR);
        String tag_case = getAttribute(ielement,TAG_CASE);
        String query_creator_name = getAttribute(ielement,QUERY_CREATOR_NAME);
        String count_attribute = getAttribute(ielement,COUNT_ATTRIBUTE);
        String query_attribute = getAttribute(ielement,QUERY_ATTRIBUTE);
        String skip_rows_attribute = getAttribute(ielement,SKIP_ROWS_ATTRIBUTE);
        String max_rows_attribute = getAttribute(ielement,MAX_ROWS_ATTRIBUTE);
        String update_rows_attribute = getAttribute(ielement,UPDATE_ROWS_ATTRIBUTE);
        String query = getAttribute(ielement,QUERY);
        String namespace = getAttribute(ielement,NAMESPACE);
        try {
            if (query == null || query.equals("")) {
                QueryCreator query_creator;
                if (!query_creator_name.equals("")) {
                    if (query_creators.containsKey(query_creator_name)) {
                        query_creator = (QueryCreator)query_creators.get(query_creator_name);
                    } else {
                        query_creator = (QueryCreator)Class.forName(query_creator_name).newInstance();
                        query_creators.put(query_creator_name,query_creator);
                    }
                } else {
                    query_creator = default_query_creator;
                }
				query = ielement.getTextChildren();
				query = query_creator.getQuery(query,parameters,request,ielement,conn,super_rs);
            }
            Statement st = conn.createStatement();
            ResultSet rs;
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
                        if (rs.next()) {
                            results_element.setAttribute(count_attribute,rs.getString(1));
						}
                        rs.close();
                    }
                }
                if (!query_attribute.equals("")) {
                    results_element.setAttribute(query_attribute,URLEncoder.encode(query));
				}
                if (!skip_rows_attribute.equals("")) {
                    results_element.setAttribute(skip_rows_attribute,""+skip_rows);
				}
                if (max_rows_attribute != null && !max_rows_attribute.equals("")) {
                    results_element.setAttribute(max_rows_attribute,""+max_rows);
				}
            }
            if (!update_rows_attribute.equals("")) {
                int update_rows = st.executeUpdate(query);
                if (results_element != null) {
                    results_element.setAttribute(update_rows_attribute,""+update_rows);
				}
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
                int count = 0;
                if (skip_rows > 0) {
                    while (rs.next()) {
                        count++;
                        if (count == skip_rows) {
							break;
						}
                    }
                }
				ColumnFormatter formatter = new ColumnFormatter(ielement.getElement());
                while (rs.next()) {
                    if (create_row_elements) {
                        row_element = Utils.createElement(document,namespace,row_element_name);
                        row_node = row_element;
                        if (create_id_attribute && id_attribute_column_index == -1) {
                            row_element.setAttribute(id_attribute,"" + count);
                        }
                    }

                    for (int i=0; i<columns.length; i++) {
						String value = rs.getString(i+1);
                        if (create_row_elements && create_id_attribute && id_attribute_column_index == i) {
                            row_element.setAttribute(id_attribute,value);
                            continue;
                        }
                        if (value == null && null_mode == OMIT_NULLS) {
							continue;
						}
                        column_element = Utils.createElement(document,namespace,columns[i].name);
                        if (value == null && null_mode == ATTRIBUTE_NULLS) {
                            column_element.setAttribute("NULL","YES");
                            column_element.appendChild(document.createTextNode(""));
                        } else {
							formatter.addColumnNode(document,column_element,columns[i],rs,i+1);
                        }
                        row_node.appendChild(column_element);
                    }
                    if (create_row_elements) {
						results_node.appendChild(row_node);
					}
                    if (count-skip_rows == max_rows-1) {
						break;
					}
					Element query_child = ielement.getChildElement("query");
					if (query_child != null) {
						Node child_results_node = processQuery(document,parameters,request,new IElement(query_child),conn,rs);
						if (create_row_elements) {
							row_node.appendChild(child_results_node);
						} else {
							results_node.appendChild(child_results_node);
						}
					}
                    count++;
                }
                rs.close();
            }
            st.close(); 
			conn.commit();
        } catch (Exception e) {
            results_node = Utils.createErrorElement(document,namespace,ielement,e);
            conn.rollback();
        } finally {
          conn.close();
        }
		return results_node;
    }

	protected static String getAttribute(IElement ielement, String name) {
		String value = ielement.getAttribute(name);
		if (value == null) {
			value = (String)default_query_attributes.get(name);
		}
		return value;
	}

	protected static int getIntAttribute(IElement ielement, String name, int def) {
		String value = getAttribute(ielement,name);
		if (value == null) {
			return def;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return def;
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

    protected static String getCountQuery(String query) {
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
