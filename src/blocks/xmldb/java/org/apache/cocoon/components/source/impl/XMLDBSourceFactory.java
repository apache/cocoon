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

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.components.source.helpers.SourceCredential;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;

/**
 * This class implements the xmldb:// pseudo-protocol and allows to get XML
 * content from an XML:DB enabled XML database.
 *
 * @author <a href="mailto:gianugo@rabellino.it">Gianugo Rabellino</a>
 * @version CVS $Id: XMLDBSourceFactory.java,v 1.1 2003/03/12 09:35:38 cziegeler Exp $
 */

public final class XMLDBSourceFactory
        extends AbstractLogEnabled
        implements SourceFactory, Configurable, Composable {

    /** The driver implementation class */
    protected String driver;

    /** The authentication info */
    protected SourceCredential credential;

    /** The Component Manager class */
    protected ComponentManager m_manager;

    /** A Map containing the driver list */
    protected HashMap driverMap;

    /** A Map containing the authentication credentials */
    protected HashMap credentialMap;

    /**
     * Configure the instance.
     */
    public void configure(final Configuration conf)
            throws ConfigurationException {

        driverMap = new HashMap();
        credentialMap = new HashMap();

        Configuration[] xmldbConfigs = conf.getChildren("driver");

        for (int i = 0; i < xmldbConfigs.length; i++) {
            SourceCredential credential = new SourceCredential(null, null);

            driverMap.put(xmldbConfigs[i].getAttribute("type"),
                          xmldbConfigs[i].getAttribute("class"));

            credential.setPrincipal(xmldbConfigs[i].getAttribute("user", null));
            credential.setPassword(xmldbConfigs[i].getAttribute("password", null));
            credentialMap.put(xmldbConfigs[i].getAttribute("type"), credential);
        }
    }

    /**
     * Compose this Composable object. We need to pass on the
     * ComponentManager to the actual Source.
     */

    public void compose(ComponentManager cm) {
        this.m_manager = cm;
    }

    public Source getSource(String location, Map parameters)
    throws MalformedURLException, IOException {
        int start = location.indexOf(':') + 1;
        int end = location.indexOf(':', start);

        if (start == -1 || end == -1) {
            throw new MalformedURLException("Mispelled XML:DB URL. " +
                                            "The syntax is \"xmldb:databasetype://host/collection/resource\"");
        }

        String type = location.substring(start, end);

        driver = (String)driverMap.get(type);
        credential = (SourceCredential)credentialMap.get(type);

        if (driver == null) {
            throw new IOException("Unable to find a driver for the \"" +
                                          type + " \" database type, please check the configuration");
        }

        return new XMLDBSource(this.getLogger(),
                               driver, credential, location, location.substring(0, start-1), 
                               this.m_manager);
    }

    public void release(org.apache.excalibur.source.Source source) {
        // nothing to do here
        if (null != source ) {
            ((XMLDBSource)source).recycle();
        }
    }

}
