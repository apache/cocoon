/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.acting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import org.apache.avalon.Component;
import org.apache.avalon.ComponentSelector;
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;
import org.apache.avalon.Parameters;

import org.apache.cocoon.Roles;
import org.apache.cocoon.components.datasource.DataSourceComponent;

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

import org.apache.cocoon.Constants;
/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2001-01-10 17:31:56 $
 */
public class AddEmployeeAction extends ComposerAction {

    DataSourceComponent datasource = null;

    /**
     * Get the <code>Configuration</code> object for this <code>Component</code>
     */
    public void configure( Configuration configuration) throws ConfigurationException {
        try {
            ComponentSelector selector = (ComponentSelector) this.manager.lookup(Roles.DB_CONNECTION);
            this.datasource = (DataSourceComponent) selector.select(configuration.getChild("use-connection").getValue());
        } catch (ComponentManagerException cme) {
            throw new ConfigurationException("Could not get the DataSource Object", cme);
        }
    }

    /**
     * A simple Action that logs if the <code>Session</code> object
     * has been created
     */
    public Map act (EntityResolver resolver, Map objectModel, String src, Parameters par) throws Exception {
        HttpServletRequest req = (HttpServletRequest) objectModel.get(Constants.REQUEST_OBJECT);
        String name = req.getParameter("name");
        String department = req.getParameter("department");

        if (addEmployee(name, department) == true) {
            req.setAttribute("message", "You have added the employee " + name);
        } else {
            req.setAttribute("message", "You did not add the employee " + name);
        }
        return null;
    }

    private boolean addEmployee(String name, String department) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean returnValue = false;

        try {
            conn = datasource.getConnection();
            conn.setAutoCommit(false);
            rs = conn.createStatement().executeQuery("SELECT Max(id) AS maxid FROM employee_table;");
            int maxid = -1;

            if (rs.next() == true) {
                maxid = rs.getInt("maxid");

                ps = conn.prepareStatement("INSERT INTO employee_table (id, name, department_id) VALUES (?, ?, ?)");
                ps.setInt(1, maxid);
                ps.setString(2, name);
                ps.setString(3, department);

                returnValue = ps.execute();
            }
        } catch (SQLException se) {
            // returnValue = false;
        } finally {
            try {
                if (returnValue = false) {
                    conn.rollback();
                } else {
                    conn.commit();
                }

                ps.close();
                rs.close();
                rs.getStatement().close();
                conn.close();
            } catch (Exception e) {
                // we should never be here
            }
        }

        return returnValue;
    }
}
