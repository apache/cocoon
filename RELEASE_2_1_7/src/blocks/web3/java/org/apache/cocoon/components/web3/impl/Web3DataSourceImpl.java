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
package org.apache.cocoon.components.web3.impl;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.thread.ThreadSafe;

import EDU.oswego.cs.dl.util.concurrent.Mutex;

import com.sap.mw.jco.JCO;

import org.apache.cocoon.components.web3.Web3Client;
import org.apache.cocoon.components.web3.Web3DataSource;

/**
 * The Default implementation for R3DataSources in Web3.  This uses the
 * normal <code>com.sap.mw.jco.JCO</code> classes.
 *
 * @author <a href="mailto:michael.gerzabek@at.efp.cc">Michael Gerzabek</a>
 * @since 2.1
 * @version CVS $Id: Web3DataSourceImpl.java,v 1.6 2004/03/05 13:02:25 bdelacretaz Exp $
 */
public class Web3DataSourceImpl extends AbstractLogEnabled
implements Web3DataSource, ThreadSafe {

    protected Web3Properties properties = null;
    protected int            poolsize = 0;
    protected int            current_clients = 0;
    protected String         mySID = null;

    protected boolean        trace = false;
    protected int            level = 0;

    private static Mutex     lock = new Mutex();
    protected ServiceManager manager;

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /** Configure backend component */
    public void configure(final Configuration configuration)
        throws ConfigurationException {
        if (null != configuration) {
            this.properties     = new Web3Properties ();
            Configuration child = configuration.getChild("pool");
            this.trace          = child.getAttributeAsBoolean("trace", false);
            this.level          = child.getAttributeAsInteger("level", 0);
            this.mySID          = configuration.getAttribute("name");
            this.poolsize       = child.getAttributeAsInteger("size");

            this.properties.put("jco.client.client",
                child.getChild("client").getValue());
            this.properties.put("jco.client.user",
                child.getChild("user").getValue());
            this.properties.put("jco.client.passwd",
                child.getChild("password").getValue());
            this.properties.put("jco.client.ashost",
                child.getChild("route").getValue());
            this.properties.put("jco.client.sysnr",
                child.getChild("system").getValue());
            this.properties.put("sap.gateway",
                child.getChild("gateway").getValue(""));
            this.properties.put("sap.programid",
                child.getChild("program-id").getValue(""));

            if ( getLogger().isDebugEnabled() ) {
                getLogger ().debug ("Configure R3DataSource [mySID="
                    + this.mySID );
            }
        } else {
            getLogger ().error ("Couldn't configure Web3DataSource." +
                " No configuration provided!");
        }
    }

    /** initialize the component */
    public void initialize() throws Exception {
        try {
            Web3DataSourceImpl.lock.acquire();
            JCO.addClientPool( this.mySID, this.poolsize, this.properties );
            JCO.getClientPoolManager().getPool( this.mySID ).setTrace( this.trace );
            JCO.setTraceLevel( this.level );
        } catch (Exception ex) {
            getLogger ().error ("Couldn't initialize Web3DataSource "
                + this.mySID, ex);
            throw new Exception ( ex.getMessage() + this.mySID );
        }
        finally {
            Web3DataSourceImpl.lock.release();
        }
    }

    /** Get the backend client, returns <code>null</code> if there is no more
        client in the pool. */
    public Web3Client getWeb3Client() throws Exception {
        Web3Client theClient = null;
        if ( this.current_clients + 1 < this.poolsize ) {
            this.current_clients++;
            try {
                Web3DataSourceImpl.lock.acquire();
                theClient = (Web3Client) this.manager.lookup( Web3Client.ROLE );
                theClient.initClient (JCO.getClient(this.mySID));

                if ( getLogger().isDebugEnabled() ) {
                    getLogger ().debug ("returning client " + theClient);
                }
            } catch (Exception ex){
                getLogger ().error ( this.mySID, ex);
                throw new Exception ( ex.getMessage() );
            } finally {
                Web3DataSourceImpl.lock.release();
            }
        }
        return theClient;
    }

    public void releaseWeb3Client(Web3Client client) {
        try {
            Web3DataSourceImpl.lock.acquire();
            client.releaseClient();
            this.current_clients--;
            manager.release( client );
        }
        catch (Exception x) {
            getLogger().error( x.getMessage(), x);
        }
        finally {
            Web3DataSourceImpl.lock.release();
        }
    }

    /** Dispose properly of the pool */
    public void dispose() {
        try {
            JCO.removeClientPool(this.mySID);
        } catch (Exception ex) {
            getLogger ().error ("Web3DataSource: couldn't" +
                " return Web3DataSource", ex);
        }
        this.properties = null;
        this.mySID = null;
        getLogger ().debug ("Web3DataSource disposed.");
    }

}
