/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.blocks.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.cocoon.kernel.configuration.ConfigurationException;
import org.apache.cocoon.kernel.configuration.Parameters;
import org.apache.cocoon.kernel.composition.Composer;
import org.apache.cocoon.kernel.composition.Wirings;
import org.apache.cocoon.kernel.composition.WiringException;

/**
 * <p>A {@link ConnectionComposer} is a simple {@link Composer} creating JDBC
 * {@link Connection} instances.</p>
 *
 * <p>This implementation relies on the {@link WirableConnection} class to
 * provide implementations of JDBC {@link Connection}s.</p>
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public class ConnectionComposer implements Composer {

    /** The URL to pass to the driver. */
    private String url = null;

    /** The properties to associate with each {@link Connection}. */
    private Properties properties = null;

    /**
     * <p>Configure this {@link ConnectionComposer}.</p>
     *
     * <p>This implementation requires a single <code>url</code> parameter to
     * operate (the URL of the JDBC connection). All extra parameters will be
     * converted into {@link String}s and passed as properties to the JDBC
     * {@link DriverManager}.</p>
     *
     * @param parameters the {@link Parameters} configuring the instance.
     * @throws ConfigurationException if this instance could not be configured.
     */
    public void configure(Parameters parameters)
    throws ConfigurationException {
        this.url = parameters.getString("url");

        Properties properties = new Properties();
        Iterator iterator = parameters.keySet().iterator();
        while (iterator.hasNext()) {
            String name = (String)iterator.next();
            if (name.equals("url")) continue;
            properties.put(name, parameters.getString(name));
        }
        if (properties.size() > 0) this.properties = properties;
    }

    /**
     * <p>Return a new {@link WirableConnection} instance wrapping a JDBC
     * {@link Connection}.</p>
     *
     * @return a <b>non null</b> {@link WirableConnection} instance.
     * @throws Throwable if the {@link Object} can not be (for example)
     *                   instantiated or returned for whatever reason.
     */
    public Object acquire()
    throws Throwable {
        Connection connection = null;
        if (properties == null) {
            connection = DriverManager.getConnection(this.url);
        } else {
            connection = DriverManager.getConnection(this.url, this.properties);
        }
        return(new WirableConnection(connection));
    }
    
    /**
     * <p>Dispose a previously created {@link WirableConnection}.</p>
     *
     * <p>This method will invoke the {@link #dispose(Object)} method.</p>
     *
     * @param object a <b>non null</b> {@link Object} instance as previously
     *               acquired from this {@link Composer} to release.
     */
    public void release(Object object) {
        this.dispose(object);
    }
    
    /**
     * <p>Dispose a previously created {@link WirableConnection}.</p>
     *
     * <p>This method will attempt to close the underlying {@link Connection}
     * nested in the {@link WirableConnection} if it was not closed.</p>
     *
     * @param object a <b>non null</b> {@link Object} instance as previously
     *               acquired from this {@link Composer} to dispose.
     */
    public void dispose(Object object) {
        Connection connection = ((WirableConnection)object).getConnection();
        try {
            if (!connection.isClosed()) connection.close();
        } catch (SQLException e) {
            /* Swallow exception */
        }
    }

    /**
     * <p>Contextualize this {@link Composer} with the {@link Wirings}
     * associated to the block where it resides.</p>
     *
     * <p>This implementation does not require any wiring.</p>
     *
     * @param wirings the {@link Wirings} instance associated with
     *                this {@link Composer}'s block.
     * @throws WiringException if there was an error performing operations
     *                              on the supplied {@link Wirings} instance.
     */
    public void contextualize(Wirings wirings)
    throws WiringException {
        /* Nothing to do here */
    }
}
