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
package org.apache.cocoon.components.web3;

import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO.Client;
import com.sap.mw.jco.JCO.Function;

/**
 * The standard interface for R3Clients in Web3.
 *
 * @since 2.1
 * @version $Id$
 */
public interface Web3Client {

    String ROLE = Web3Client.class.getName();

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
