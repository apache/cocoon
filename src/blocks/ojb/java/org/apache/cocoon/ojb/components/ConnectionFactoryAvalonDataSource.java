/*

   ============================================================================
                     The Apache Software License, Version 1.1
   ============================================================================
   Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.ojb.components;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.excalibur.datasource.DataSourceComponent;

import org.apache.ojb.broker.accesslayer.ConnectionFactory;
import org.apache.ojb.broker.accesslayer.LookupException;
import org.apache.ojb.broker.metadata.JdbcConnectionDescriptor;


/**
 * OJBConnectionFactory implemenation to bridge into the Avalon DataSource Connection Pooling
 * Component defined in the Cocoon configuration.
 *
 * @author giacomo at apache.org
 * @version $Id: ConnectionFactoryAvalonDataSource.java,v 1.1 2004/02/01 21:37:29 giacomo Exp $
 */
public class ConnectionFactoryAvalonDataSource
    implements ConnectionFactory {
    /** The <code>ServiceManager</code> to be used */
    private static ServiceManager manager;

    /** The <code>ServiceSelector</code> to be used */
    private static ServiceSelector dbselector;

    /**
     * Initializes this helper class with the <code>ServiceManager</code> to be used.  This method
     * should be called from a Avalon Component configured into Cocoon at startup to supply the
     * needed <code>ServiceManager</code>.
     *
     * @param serviceManager The ServiceManager
     *
     * @throws ServiceException In case we cannot obtain a DataSource
     */
    public static void initialize(final ServiceManager serviceManager)
        throws ServiceException {
        ConnectionFactoryAvalonDataSource.manager = serviceManager;
        ConnectionFactoryAvalonDataSource.dbselector =
            (ServiceSelector)ConnectionFactoryAvalonDataSource.manager.lookup(DataSourceComponent.ROLE +
                                                                              "Selector");
    }

    /**
     * Signal disposal to this helper class.
     */
    public static void dispose() {
        ConnectionFactoryAvalonDataSource.manager.release(ConnectionFactoryAvalonDataSource.dbselector);
    }

    /* (non-Javadoc)
     * @see org.apache.ojb.broker.accesslayer.ConnectionFactory#lookupConnection(org.apache.ojb.broker.metadata.JdbcConnectionDescriptor)
     */
    public Connection lookupConnection(final JdbcConnectionDescriptor conDesc)
        throws LookupException {
        if (null == ConnectionFactoryAvalonDataSource.manager) {
            throw new LookupException("ServiceManager was not set!");
        }

        try {
            return ((DataSourceComponent)ConnectionFactoryAvalonDataSource.dbselector.select(conDesc.getJcdAlias())).getConnection();
        } catch (final ServiceException se) {
            throw new LookupException("Cannot lookup DataSources named " + conDesc.getJcdAlias(), se);
        } catch (final SQLException sqle) {
            throw new LookupException("Cannot get Connection from DataSource named " +
                                      conDesc.getDbAlias(), sqle);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ojb.broker.accesslayer.ConnectionFactory#releaseConnection(org.apache.ojb.broker.metadata.JdbcConnectionDescriptor, java.sql.Connection)
     */
    public void releaseConnection(JdbcConnectionDescriptor conDesc, Connection con) {
        try {
            con.close(); // The DataSource itself from where this connection comes from will take care of pooling
        } catch (final SQLException sqle) {
            // This should not happend, but in case 
            throw new CascadingRuntimeException("Cannot eelase SQL Connection to DataSource", sqle);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ojb.broker.accesslayer.ConnectionFactory#releaseAllResources()
     */
    public void releaseAllResources() {
        //Nothing to do here
    }
}
