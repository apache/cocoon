package org.apache.cocoon.processor.sql;

import java.sql.*;
import java.text.*;
import java.util.*;
import javax.servlet.http.*;
import org.w3c.dom.*;

/**
 * A class that can create a SQL query. It's given a query to start with,
 * plus a query_props table that contains parameters from the XML file, and the
 * parameters table from cocoon that notably may contain a HttpServletRequest
 * object keyed from "request".
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:14 $
 */

public class SQLQueryCreator {

    public String getQuery(Connection conn, String query, Element query_element, Properties query_props, Dictionary parameters) throws Exception {
        HttpServletRequest req = (HttpServletRequest)parameters.get("request");
        String ldelim = query_props.getProperty("variable-left-delimiter");
        int llength = ldelim.length();
        String rdelim = query_props.getProperty("variable-right-delimiter");
        int rlength = rdelim.length();
        int offset = 0;
        while (true) {
            int lindex = query.indexOf(ldelim,offset);
            if (lindex < 0) break;
            int rindex = query.indexOf(rdelim,offset+llength);
            if (rindex < 0 || rindex < lindex) break;
            String name = query.substring(lindex+llength,rindex);
            String value = req.getParameter(name);
            if (value == null) break;
            query = query.substring(0,lindex)+value+query.substring(rindex+rlength);
            offset = lindex+value.length();
        }
        return query;
    }

}
