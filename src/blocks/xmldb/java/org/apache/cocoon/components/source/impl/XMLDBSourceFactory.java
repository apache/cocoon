/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
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
 * @version CVS $Id: XMLDBSourceFactory.java,v 1.6 2004/01/29 03:18:43 vgritsenko Exp $
 */
public final class XMLDBSourceFactory extends AbstractLogEnabled
                                      implements SourceFactory, Configurable, Serviceable, ThreadSafe {

    /** The ServiceManager instance */
    protected ServiceManager m_manager;

    /** A Map containing the authentication credentials */
    protected HashMap credentialMap;

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

            } catch (XMLDBException xde) {
                String error = "Unable to connect to the XMLDB database. Error "
                               + xde.errorCode + ": " + xde.getMessage();
                getLogger().debug(error, xde);
                throw new ConfigurationException(error, xde);

            } catch (Exception e) {
                getLogger().warn("There was a problem setting up the connection. "
                                 + "Make sure that your driver is available");
                throw new ConfigurationException("Problem setting up the connection to XML:DB: "
                                                 + e.getMessage(), e);
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
                               this.m_manager);
    }

    public void release(org.apache.excalibur.source.Source source) {
        // nothing to do here
        if (null != source ) {
            ((XMLDBSource)source).recycle();
        }
    }
}
