/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.xsp.handler;

/**
 * This class acts like a Factory to instantiate the correct version
 * of the component handler that you need.
 *
 * @version $Id$
 * @since 2.2
 */
public interface ComponentHandler {

    /**
     * Get an instance of the type of component handled by this handler.
     * 
     * @return an instance
     * @exception Exception if an error occurs
     */
    Object get() throws Exception;

    /**
     * Put back an instance of the type of component handled by this handler.
     *
     * @param component a service
     * @exception Exception if an error occurs
     */
    void put( Object component ) 
    throws Exception;
    
    /**
     * Indicates if this handler manages a single object, i.e. all calls to {@link #get()}
     * will return the same object.
     * 
     * @return <code>true</code> if managed object is a singleton
     */
    boolean isSingleton();

    /**
     * Returns <code>true</code> if this component handler can safely be
     * disposed (i.e. none of the components it is handling are still
     * being used).
     *
     * @return <code>true</code> if this component handler can safely be
     *         disposed; <code>false</code> otherwise
     */
    boolean canBeDisposed();

    /**
     * Dispose of the component handler and any associated Pools and Factories.
     */
    public void dispose();
    
    /**
     * Initialize this handler
     */
    void initialize() throws Exception;
}
