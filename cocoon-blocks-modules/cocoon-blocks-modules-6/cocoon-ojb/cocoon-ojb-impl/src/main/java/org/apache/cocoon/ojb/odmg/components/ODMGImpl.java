/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.ojb.odmg.components;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.ojb.odmg.OJB;

import org.apache.cocoon.util.AbstractLogEnabled;

import org.odmg.Database;
import org.odmg.Implementation;
import org.odmg.ODMGException;

/**
 * OJB backed implementation of the ODMG component. Creates a ODMG Implementation
 * Object and stores it for the future use.
 *
 * @version $Id$
 */
public class ODMGImpl extends AbstractLogEnabled
                      implements ODMG, ThreadSafe, Initializable, Disposable {

    private static final String DEFAULT_CONNECTION ="default";
    private static final int DEFAULT_MODE = Database.OPEN_READ_WRITE;

    private Implementation odmg;
    private HashMap databases = new HashMap();

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        // Get the Implementation
        this.odmg = OJB.getInstance();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        final Set keys = this.databases.keySet();
        for (Iterator i = keys.iterator(); i.hasNext();) {
            final Database db = (Database) i.next();
            try {
                db.close();
            } catch (ODMGException e) {
                getLogger().error("OJB-ODMG: Cannot close Database", e);
            }
            i.remove();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.odmg.components.ODMG#getInstance()
     */
    public Implementation getInstance() throws ODMGException {
        return getInstance(DEFAULT_CONNECTION, DEFAULT_MODE);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.odmg.components.ODMG#getInstance(java.lang.String)
     */
    public Implementation getInstance(String connection) throws ODMGException {
        return getInstance(connection, DEFAULT_MODE);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.odmg.components.ODMG#getInstance(int)
     */
    public Implementation getInstance(int mode) throws ODMGException {
        return getInstance(DEFAULT_CONNECTION, mode);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.odmg.components.ODMG#getInstance(java.lang.String, int)
     */
    public Implementation getInstance(String connection, int mode) throws ODMGException {
        synchronized (this.databases) {
            Database db = (Database) this.databases.get(connection + ":" + mode);
            if (null == db) {
                db = this.odmg.newDatabase();
                db.open(connection, mode);
                this.databases.put(connection + ":" + mode, db);
            }
        }
        return this.odmg;
    }
}
