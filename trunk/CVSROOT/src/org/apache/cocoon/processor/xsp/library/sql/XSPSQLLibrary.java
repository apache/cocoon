/*
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.
 
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

package org.apache.cocoon.processor.xsp.library.sql;

import java.net.URLEncoder;
import java.sql.*;
import java.text.*;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.w3c.dom.*;

/**
 * A processor that performs SQL database queries.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version $Revision: 1.2 $ $Date: 2000-04-24 20:39:25 $
 */

public class XSPSQLLibrary {

    public static Node processQuery(
      Document document,
      String driver,
      String dburl,
      String username,
      String password,
      String doc_element_name,
      String row_element_name,
      String tag_case,
      String null_indicator,
      String id_attribute,
      String id_attribute_column,
      Integer max_rows,
      Integer skip_rows,
      String error_element,
      String error_message_attribute,
      String error_stacktrace_attribute,
      String error_message_text,
      String query_creator_name,
      String count_attribute,
      String query_attribute,
      String skip_rows_attribute,
      String max_rows_attribute,
      String update_rows_attribute,
      String namespace,
      String query,
      Hashtable column_formats
	) throws Exception {

        Class.forName(driver).newInstance();
        Connection conn;
        if (username == null) {
            conn = DriverManager.getConnection(dburl);
        } else {
            conn = DriverManager.getConnection(dburl,username,password);
        }
        boolean create_row_elements = true;
        if (row_element_name.equals("")) {
            create_row_elements = false;
        }
        if (max_rows.equals("")) {
            max_rows = new Integer(-1);
        }
        if (skip_rows.equals("")) {
            skip_rows = new Integer(0);
        }
        boolean create_id_attribute = true;
        if (id_attribute.equals("")) {
            create_id_attribute = false;
        }
	    boolean indicate_nulls = false;
        if (null_indicator.equals("y")) {
            indicate_nulls = true;
        } else if (null_indicator.equals("yes")) {
            indicate_nulls = true;
        }
        Statement st = conn.createStatement();
        ResultSet rs;
        Node results_node;
        Element results_element = null;
        if (doc_element_name.equals("")) {
            results_node = document.createDocumentFragment();
        } else {
            results_element = createElement(document,namespace,doc_element_name);
        }
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
        if (!max_rows_attribute.equals("")) {
            results_element.setAttribute(max_rows_attribute,""+max_rows);
        }
        if (!st.execute(query)) {
	    	/** this returns the number of rows we updated, or -1 on error **/
            int update_rows = st.getUpdateCount();
            if (results_element != null) {
                results_element.setAttribute(update_rows_attribute,""+update_rows);
            }
        } else {
	    	/** and this is where we return the rowset instead. **/
            rs = st.getResultSet();
            ResultSetMetaData md = rs.getMetaData();
			if (tag_case.equals("")) {
				tag_case = "preserve";
			}
            Column columns[] = getColumns(md,tag_case);
            int id_attribute_column_index = -1;
            if (create_id_attribute) {
                for (int i=0; i<columns.length; i++) {
                    if (columns[i].name.equals(id_attribute_column)) {
                        id_attribute_column_index = i;
                    }
                }
            }
            Element column_element;
            Node row_node = results_node;
            Element row_element = null;
            int count = 0;
            if (skip_rows.intValue() > 0) {
                while (rs.next()) {
                    count++;
                    if (count == skip_rows.intValue()) break;
                }
            }
            while (rs.next()) {
                if (create_row_elements) {
                    row_element = createElement(document,namespace,row_element_name);
                    row_node = row_element;
                    if (create_id_attribute && id_attribute_column_index == -1) {
                        row_element.setAttribute(id_attribute,"" + count);
                    }
                }
                for (int i=0; i<columns.length; i++) {
					Object value = rs.getObject(i+1);
                    if (create_row_elements && create_id_attribute && id_attribute_column_index == i) {
                        row_element.setAttribute(id_attribute,value.toString());
                        continue;
                    }
                    if (value == null && !indicate_nulls) {
						continue;
					}
                    column_element = createElement(document,namespace,columns[i].name);
                    if (value == null && indicate_nulls) {
                        column_element.setAttribute("NULL","YES");
                        column_element.appendChild(document.createTextNode(""));
                    } else {
						column_element.appendChild(document.createTextNode(value.toString()));
                    }
                    row_node.appendChild(column_element);
                }
                if (create_row_elements) results_node.appendChild(row_node);
                if (count-skip_rows.intValue() == max_rows.intValue()-1) break;
                count++;
            }
            rs.close();
        }
        st.close(); conn.close();
        return results_node;
    }

    protected static Column[] getColumns(ResultSetMetaData md, String tag_case) throws SQLException {
        Column columns[] = new Column[md.getColumnCount()];
        if (tag_case.equals("preserve")) {
            for (int i=0; i<columns.length; i++) {
                columns[i] = new Column(md.getColumnName(i+1),md.getColumnType(i+1),md.getColumnTypeName(i+1));
            }
        } else if (tag_case.equals("lower")) {
            for (int i=0; i<columns.length; i++) {
                columns[i] = new Column(md.getColumnName(i+1).toLowerCase(),md.getColumnType(i+1),md.getColumnTypeName(i+1));
            }
        } else if (tag_case.equals("upper")) {
            for (int i=0; i<columns.length; i++) {
                columns[i] = new Column(md.getColumnName(i+1).toUpperCase(),md.getColumnType(i+1),md.getColumnTypeName(i+1));
            }
        }
        return columns;
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

	public static Element createElement(Document document, String namespace, String name) {
		if (namespace == null || namespace.equals("")) {
			return document.createElement(name);
		}
		return document.createElement(namespace+':'+name);
	}

}
