/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.datasource;

import org.apache.avalon.Component;
import org.apache.avalon.Configurable;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The standard interface for DataSources in Cocoon.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-01-08 20:20:46 $
 */
public interface DataSourceComponent extends Component, Configurable {
    /**
     * Gets the Connection to the database
     */
    Connection getConnection() throws SQLException;
}
