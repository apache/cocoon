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
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2001-01-11 21:01:42 $
 */
public class UpdEmployeeAction extends ComposerAction {

    DataSourceComponent datasource = null;

    /**
     * Get the <code>Configuration</code> object for this <code>Component</code>
     */
    public void configure( Configuration configuration) throws ConfigurationException {
        Configuration connElement = configuration.getChild("use-connection");

        try {
            ComponentSelector selector = (ComponentSelector) this.manager.lookup(Roles.DB_CONNECTION);
            this.datasource = (DataSourceComponent) selector.select(connElement.getValue());
        } catch (ComponentManagerException cme) {
            log.error("Could not get the DataSourceComponent", cme);
            throw new ConfigurationException("Could not get the DataSource Component", cme);
        }
    }

    /**
     * A simple Action that logs if the <code>Session</code> object
     * has been created
     */
    public Map act (EntityResolver resolver, Map objectModel, String src, Parameters par) throws Exception {
        HttpServletRequest req = (HttpServletRequest) objectModel.get(Constants.REQUEST_OBJECT);
        String id = req.getParameter("employee");
        String name = req.getParameter("name");
        String department = req.getParameter("department")

        if (updateEmployee(id, name, department) == true) {
            req.setAttribute("message", "You have updated the employee " + name);
        } else {
            req.setAttribute("message", "You did not update the employee " + name);
        }
        return null;
    }

    private boolean updateEmployee(String id, String name, String department) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean returnValue = true;

        try {
            conn = datasource.getConnection();
            conn.setAutoCommit(false);

            ps = conn.prepareStatement("UPDATE employee_table SET id = ?, name = ?, department_id = ?");
            ps.setString(1, id);
            ps.setString(2, name);
            ps.setString(3, department);

            ps.executeUpdate();
            returnValue = true;
            conn.commit();
        } catch (SQLException se) {
            try {
                conn.rollback();
            } catch (SQLException sse) {
                log.error("Caught an exception trying to roll back transaction", sse);
            }

            log.error("There was a SQL error", se);
        } finally {
            try {
                if (ps != null) ps.close();
                conn.close();
            } catch (Exception e) {
                log.error("We should never be in this clause", e);
            }
        }

        return returnValue;
    }
}



