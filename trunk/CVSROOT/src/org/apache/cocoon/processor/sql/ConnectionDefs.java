package org.apache.cocoon.processor.sql;

import org.w3c.dom.*;
import java.sql.*;
import java.util.*;

/**
 * Default connection values.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:14 $
 */

public class ConnectionDefs {

    /**
     * The connection creators table
     */
    protected Hashtable creators = new Hashtable();

    /**
     * The query properties defaults table
     */
    protected Hashtable query_props_table = new Hashtable();

    /**
     * The page-specific default query properties
     */
    protected Properties default_query_props = master_default_query_props;

    /**
     * The overall master default query properties
     */
    protected static Properties master_default_query_props = new Properties();
    static {
        master_default_query_props.put("doc-element","ROWSET");
        master_default_query_props.put("row-element","ROW");
        master_default_query_props.put("tag-case","preserve");
        master_default_query_props.put("null-indicator","omit");
        master_default_query_props.put("id-attribute","ID");
        master_default_query_props.put("id-attribute-column","");
        master_default_query_props.put("count-attribute","");
        master_default_query_props.put("query-attribute","");
        master_default_query_props.put("skip-rows-attribute","");
        master_default_query_props.put("max-rows-attribute","");
        master_default_query_props.put("variable-left-delimiter","{@");
        master_default_query_props.put("variable-right-delimiter","}");
        master_default_query_props.put("session-variable-left-delimiter","{@session.");
        master_default_query_props.put("session-variable-right-delimiter","}");
        master_default_query_props.put(Utils.ERROR_ELEMENT,"sqlerror");
        master_default_query_props.put(Utils.ERROR_MESSAGE_ATTRIBUTE,"message");
        master_default_query_props.put(Utils.ERROR_MESSAGE_ELEMENT,"");
        master_default_query_props.put(Utils.ERROR_STACKTRACE_ATTRIBUTE,"");
        master_default_query_props.put(Utils.ERROR_STACKTRACE_ELEMENT,"");
    }

    public ConnectionDefs(Document document) throws Exception {
        NodeList connectiondefs = document.getElementsByTagName("connectiondefs");
        Node connection_defs_ary[] = new Node[connectiondefs.getLength()];
        for (int i=0; i<connectiondefs.getLength(); i++)
            connection_defs_ary[i] = connectiondefs.item(i);
        for (int i=0; i<connection_defs_ary.length; i++) {
            Node connection_def_node = connection_defs_ary[i];
            NodeList connections = connection_def_node.getChildNodes();
            for (int j=0; j<connections.getLength(); j++) {
                Node node = connections.item(j);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element)node;
                    String name = element.getNodeName();
                    if (name.equals("connection"))
                        processConnectionDef(element);
                    else if (name.equals("querydefs"))
                        processQueryDef(element);
                }
            }
            connection_def_node.getParentNode().removeChild(connection_def_node);
        }
    }

    /**
     * Process a single connection definition node
     */
    protected void processConnectionDef(Element connection) throws Exception {
        String name = connection.getAttribute("name");
        if (name == null || name.equals("querydefs")) return;
        Properties connection_props = new Properties();
        NodeList connection_children = connection.getChildNodes();
        for (int k=0; k<connection_children.getLength(); k++) {
            Node connection_parameter = connection_children.item(k);
            String prop_name = connection_parameter.getNodeName();
            NodeList connection_parameter_values = connection_parameter.getChildNodes();
            StringBuffer value = new StringBuffer();
            for (int l=0; l<connection_parameter_values.getLength(); l++) {
                Node value_node = connection_parameter_values.item(l);
                if (value_node.getNodeType() == Node.TEXT_NODE)
                    value.append(value_node.getNodeValue());
            }
            connection_props.put(prop_name,value.toString());
        }
        if (!connection_props.containsKey("dburl")) return;
        creators.put(name,new ConnectionCreator(connection_props));
        String driver = connection_props.getProperty("driver");
        Hashtable drivers = org.apache.cocoon.processor.sql.SQLProcessor.drivers;
        if (driver != null && !drivers.containsKey(driver))
            drivers.put(driver,Class.forName(driver).newInstance());
    }

    protected void processQueryDef(Element querydef) {
        String name = querydef.getAttribute("name");
        if (name == null) return;
        NamedNodeMap attributes = querydef.getAttributes();
        Properties props = new Properties(master_default_query_props);
        for (int i=0; i<attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            props.put(attribute.getNodeName(),attribute.getNodeValue());
        }
        query_props_table.put(name,props);
        String def = querydef.getAttribute("default");
        if (def != null && (def.equals("y") || def.equals("yes")))
            default_query_props = props;
    }

    public ConnectionCreator getConnectionCreator(String name) {
        return (ConnectionCreator)creators.get(name);
    }

    public Connection getConnection(String name) throws SQLException {
        ConnectionCreator creator = (ConnectionCreator)creators.get(name);
        return creator.getConnection();
    }

    public Properties getQueryProperties() { return default_query_props; }


    public Properties getQueryProperties(String name) {
        if (name == null) return default_query_props;
        return (Properties)query_props_table.get(name);
    }

}
