/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.ojb.odmg.OJB;
import org.odmg.Database;
import org.odmg.Implementation;
import org.odmg.ODMGException;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

/**
 * Implementation of the OdmgImplementation. Create a ODMG Implementation and store it for future use
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id$
 */
public class OdmgImplementationImpl extends AbstractLogEnabled
                                    implements OdmgImplementation, ThreadSafe, Initializable, Disposable {

    private final static String DEFAULT_CONNECTION ="default";
    private final static int DEFAULT_MODE = Database.OPEN_READ_WRITE;
    private Implementation odmg;

    private Hashtable databases = new Hashtable();

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
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        // Get the Implementation
        this.odmg = OJB.getInstance();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.odmg.components.Odmg#getInstance()
     */
    public Implementation getInstance() throws ODMGException {
        Database db = (Database) this.databases.get(DEFAULT_CONNECTION);
        if (null == db) {
            db = this.odmg.newDatabase();
            db.open(DEFAULT_CONNECTION, DEFAULT_MODE);
            synchronized (this.databases) {
                this.databases.put(DEFAULT_CONNECTION + DEFAULT_MODE, db);
            }
        }
        return this.odmg;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.odmg.components.Odmg#getInstance(java.lang.String, int)
     */
    public Implementation getInstance(String connection, int mode) throws ODMGException {
        Database db = (Database) this.databases.get(connection + mode);
        if (null == db) {
            db = this.odmg.newDatabase();
            db.open(connection, mode);
            synchronized (this.databases) {
                this.databases.put(connection + mode, db);
            }
        }
        return this.odmg;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.odmg.components.Odmg#getInstance(java.lang.String)
     */
    public Implementation getInstance(String connection) throws ODMGException {
        Database db = (Database) this.databases.get(connection + DEFAULT_MODE);
        if (null == db) {
            db = this.odmg.newDatabase();
            db.open(connection, DEFAULT_MODE);
            synchronized (this.databases) {
                this.databases.put(connection + DEFAULT_MODE, db);
            }
        }
        return this.odmg;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.ojb.odmg.components.OdmgImplementation#getInstance(int)
     */
    public Implementation getInstance(int mode) throws ODMGException {
        Database db = (Database) this.databases.get(DEFAULT_CONNECTION + mode);
        if (null == db) {
            db = this.odmg.newDatabase();
            db.open(DEFAULT_CONNECTION, mode);
            synchronized (this.databases) {
                this.databases.put(DEFAULT_CONNECTION + mode, db);
            }
        }
        return this.odmg;
    }
}
