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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.components.source.helpers.SourceCredential;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.DatabaseManager;

/**
 * This class implements the xmldb:// pseudo-protocol and allows to get XML
 * content from an XML:DB enabled XML database.
 *
 * @author <a href="mailto:gianugo@rabellino.it">Gianugo Rabellino</a>
 * @version CVS $Id: XMLDBSourceFactory.java,v 1.9 2004/05/17 16:15:18 vgritsenko Exp $
 */
public final class XMLDBSourceFactory extends AbstractLogEnabled
                                      implements SourceFactory, Contextualizable, Configurable, Serviceable, ThreadSafe {

    /** The ServiceManager instance */
    protected ServiceManager m_manager;

    /** A Map containing the authentication credentials */
    protected HashMap credentialMap;

    /** The avalon context */
    protected Context context;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }
    /**
     * Configure the instance and initialize XML:DB connections (load and register the drivers).
     */
    public void configure(final Configuration conf)
    throws ConfigurationException {

        credentialMap = new HashMap();

        Configuration[] drivers = conf.getChildren("driver");
        for (int i = 0; i < drivers.length; i++) {
            String type = drivers[i].getAttribute("type");
            String driver = drivers[i].getAttribute("class");

            SourceCredential credential = new SourceCredential(null, null);
            credential.setPrincipal(drivers[i].getAttribute("user", null));
            credential.setPassword(drivers[i].getAttribute("password", null));
            credentialMap.put(type, credential);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Initializing XML:DB connection, using driver " + driver);
            }

            try {
                Database db = (Database)Class.forName(driver).newInstance();

                Configuration[] params = drivers[i].getChildren();
                for (int j = 0; j < params.length; j++) {
                    db.setProperty(params[j].getName(), params[j].getValue());
                }

                DatabaseManager.registerDatabase(db);

            } catch (XMLDBException e) {
                String msg = "Unable to connect to the XMLDB database '" + type + "'." +
                             " Error " + e.errorCode + ": " + e.getMessage();
                getLogger().debug(msg, e);
                throw new ConfigurationException(msg, e);

            } catch (Exception e) {
                String msg = "Unable to load XMLDB database driver '" + driver + "'." +
                             " Make sure that the driver is available. Error: " + e.getMessage();
                getLogger().debug(msg, e);
                throw new ConfigurationException(msg, e);
            }
        }
    }

    /**
     * Compose this Serviceable object. We need to pass on the
     * ServiceManager to the actual Source.
     */
    public void service(ServiceManager cm) {
        this.m_manager = cm;
    }

    /**
     * Resolve the source
     */
    public Source getSource(String location, Map parameters)
    throws MalformedURLException, IOException {

        int start = location.indexOf(':') + 1;
        int end = location.indexOf(':', start);

        if (start == 0 || end == -1) {
            throw new MalformedURLException("Mispelled XML:DB URL. " +
                                            "The syntax is \"xmldb:databasetype://host/collection/resource\"");
        }

        String type = location.substring(start, end);
        SourceCredential credential = (SourceCredential)credentialMap.get(type);

        return new XMLDBSource(this.getLogger(),
                               credential, location,
                               this.m_manager,
                               this.context);
    }

    public void release(org.apache.excalibur.source.Source source) {
        // nothing to do here
        if (null != source ) {
            ((XMLDBSource)source).recycle();
        }
    }
}
