/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.datasource;

import org.apache.avalon.util.pool.Pool;
import org.apache.avalon.Disposable;
import org.apache.avalon.Poolable;
import org.apache.avalon.Recyclable;
import org.apache.avalon.Loggable;
import org.apache.avalon.Initializable;
import org.apache.log.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Pool implementation for JdbcConnections.  It uses a background
 * thread to manage the number of SQL Connections.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2001-01-22 21:56:34 $
 */
public class JdbcConnectionPool implements Pool, Runnable, Disposable, Loggable, Initializable {
    private final String dburl;
    private final String username;
    private final String password;
    private final int min;
    private final int max;
    private int currentCount = 0;
    private List active = new ArrayList();
    private List ready = new ArrayList();
    private Logger log;
    private boolean monitoring = true;

    public JdbcConnectionPool(String url, String username, String password, int min, int max) {
        this.dburl = url;
        this.username = username;
        this.password = password;

        if (min < 0) {
            log.warn("Minumum number of connections specified is less than 0, using 0");
            this.min = 0;
        } else {
            this.min = min;
        }

        if ((max < min) || (max < 1)) {
            log.warn("Maximum number of connections specified must be at least 1 and must be greater than the minumum number of connections");
            this.max = (min > 1) ? min : 1;
        } else {
            this.max = max;
        }
    }

    public void setLogger (Logger logger) {
        if (this.log == null) {
            this.log = logger;
        }
    }

    public void init() {
        for (int i = 0; i < min; i++) {
            this.ready.add(this.createJdbcConnection());
        }

        new Thread(this);
    }

    private JdbcConnection createJdbcConnection() {
        JdbcConnection conn = null;

        try {
            if (username == null) {
                conn = new JdbcConnection(DriverManager.getConnection(this.dburl), this);
                conn.setLogger(this.log);
                currentCount++;
            } else {
                conn = new JdbcConnection(DriverManager.getConnection(this.dburl, this.username, this.password), this);
                conn.setLogger(this.log);
                currentCount++;
            }
        } catch (SQLException se) {
            log.error("Could not create connection to database", se);
        }

        log.debug("JdbcConnection object created");
        return conn;
    }

    private void recycle(Recyclable obj) {
        log.debug("JdbcConnection object recycled");
        obj.recycle();
    }

    public Poolable get()
    throws Exception {
        Poolable obj = null;
        if (this.ready.size() == 0) {
            if (this.currentCount < this.max) {
                synchronized (this.active) {
                    obj = this.createJdbcConnection();
                    this.active.add(obj);
                }
            } else {
                throw new SQLException("There are no more Connections available");
            }
        } else {
            synchronized (this.active) {
                obj = (Poolable) this.ready.remove(0);
                this.active.add(obj);
            }
        }

        log.debug("JdbcConnection '" + this.dburl + "' has been requested from pool.");

        return obj;
    }

    public synchronized void put(Poolable obj) {
        int location = this.active.indexOf(obj);
        this.active.remove(obj);

        if (this.monitoring) {
            this.ready.add(obj);
        }

        log.debug("JdbcConnection '" + this.dburl + "' has been returned to the pool.");
    }

    public void run() {
        while (this.monitoring) {
            if (this.ready.size() < this.min) {
                log.debug("There are not enough Connections for pool: " + this.dburl);
                while ((this.ready.size() < this.min) && (this.currentCount < this.max)) {
                    this.ready.add(this.createJdbcConnection());
                }
            } else {
                log.debug("Trimming excess fat from pool: " + this.dburl);
                while (this.ready.size() > this.min) {
                    this.recycle((Recyclable) this.ready.remove(0));
                }
            }

            try {
                Thread.sleep(1 * 60 * 1000);
            } catch (InterruptedException ie) {
                log.warn("Caught an InterruptedException", ie);
            }
        }
    }

    public void dispose() {
        this.monitoring = false;

        while (! this.ready.isEmpty()) {
            this.recycle((Recyclable) this.ready.remove(0));
        }
    }
}
