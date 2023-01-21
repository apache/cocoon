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
package org.apache.cocoon.components.repository.helpers;

import org.apache.cocoon.ProcessingException;

/**
 * A locking helper interface intended to be used by flowscripts or corresponding wrapper components.
 */
public interface RepositoryTransactionHelper {
    
    /**
     * beginning a transaction on the repository
     * 
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean beginTran() throws ProcessingException;

    /**
     * committing a transaction on the repository
     * 
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean commitTran() throws ProcessingException;

    /**
     * rolling back a transaction on the repository
     * 
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean rollbackTran() throws ProcessingException;

    /**
     * lock the resource
     * 
     * @param uri  the uri of the resource.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean lock(String uri) throws ProcessingException;

    /**
     * lock the resource with explicit timeout in seconds
     * 
     * @param uri  the uri of the resource.
     * @param timeout  the lock timeout in seconds.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean lock(String uri, int timeout) throws ProcessingException;

    /**
     * unlock resource
     * 
     * @param uri  the uri of the resource.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean unlock(String uri) throws ProcessingException;

    /**
     * checking wether the repository supports transactions
     * 
     * @return  true if the repository supports transactions.
     */
    boolean supportsTransactions();

    /**
     * checking wether the repository supports locking
     * 
     * @return  true if the repository supports locking.
     */
    boolean supportsLocking();

}