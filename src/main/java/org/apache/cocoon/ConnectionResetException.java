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
package org.apache.cocoon;

/**
 * This Exception is thrown every time a component detects an exception
 * due to a connection reset by peer.
 *
 * @version $Id$
 */
public class ConnectionResetException extends ProcessingException {

    /**
     * Construct a new <code>ConnectionResetException</code> instance.
     *
     * @param message a <code>String</code> value
     */
    public ConnectionResetException(String message) {
        super(message);
    }

    /**
     * Construct a new <code>ConnectionResetException</code> that references
     * a parent Exception.
     *
     * @param message a <code>String</code> value
     * @param t a <code>Throwable</code> value
     */
    public ConnectionResetException(String message, Throwable t) {
        super(message, t);
    }
}
