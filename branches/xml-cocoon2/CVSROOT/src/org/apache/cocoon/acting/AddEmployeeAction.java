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
import org.apache.avalon.util.datasource.DataSourceComponent;

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

import org.apache.cocoon.Constants;
/**
 * A simple action that Adds an employee the Employee Table used in
 * the demonstration SQL code.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.9 $ $Date: 2001-02-12 05:56:49 $
 */
public class AddEmployeeAction extends ComposerAction {

    DataSourceComponent datasource = null;

    /**
     * Get the <code>Configuration</code> object for this <code>Component</code>
     */
    public void configure( Configuration configuration) throws ConfigurationException {
        super.configure(configuration);

        Configuration connElement = configuration.getChild("use-connection");

        try {
            ComponentSelector selector = (ComponentSelector) this.manager.lookup(Roles.DB_CONNECTION);
            this.datasource = (DataSourceComponent) selector.select(connElement.getValue());
        } catch (ComponentManagerException cme) {
            getLogger().error("Could not get the DataSourceComponent", cme);
            throw new ConfigurationException("Could not get the DataSource Component", cme);
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
        boolean returnValue = true;

        try {
            conn = datasource.getConnection();
            conn.setAutoCommit(false);
            rs = conn.createStatement().executeQuery("SELECT Max(id) AS maxid FROM employee_table");
            int maxid = -1;

            if (rs.next() == true) {
                maxid = rs.getInt("maxid") + 1;

                ps = conn.prepareStatement("INSERT INTO employee_table (id, name, department_id) VALUES (?, ?, ?)");
                ps.setInt(1, maxid);
                ps.setString(2, name);
                ps.setString(3, department);

                ps.executeUpdate();
                returnValue = true;
                conn.commit();
            } else {
                returnValue = false;
                conn.rollback();
            }
        } catch (SQLException se) {
            try {
                conn.rollback();
            } catch (SQLException sse) {
                getLogger().error("Caught an exception trying to roll back transaction", sse);
            }

            getLogger().error("There was a SQL error", se);
        } finally {
            try {
                if (ps != null) ps.close();
                if (rs != null) rs.close();
                if (rs.getStatement() != null) rs.getStatement().close();
                conn.close();
            } catch (Exception e) {
                getLogger().error("We should never be in this clause", e);
            }
        }

        return returnValue;
    }
}
