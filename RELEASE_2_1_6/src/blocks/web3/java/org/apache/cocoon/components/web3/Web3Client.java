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
package org.apache.cocoon.components.web3;

import org.apache.avalon.framework.component.Component;

import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO.Client;
import com.sap.mw.jco.JCO.Function;

/**
 * The standard interface for R3Clients in Web3.
 *
 * @author <a href="mailto:michael.gerzabek@at.efp.cc">Michael Gerzabek</a>
 * @since 2.1
 * @version CVS $Id: Web3Client.java,v 1.5 2004/03/05 13:02:25 bdelacretaz Exp $
 */
public interface Web3Client extends Component {
    
    String ROLE = "org.apache.cocoon.components.web3.Web3Client";
    
    /**
     * Releases the Connection to the backend
     */
    void releaseClient ();
    
    /**
     * Initialize the client
     */
    void initClient (Client client);
    
    /**
     * Get a Client Repository
     */
    IRepository getRepository ();
    
    /**
     * Execute an Abab function
     */
    void execute(Function function);   
}
