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
package org.apache.cocoon.servletservice;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Interface for making the absolute path available from a servlet service relative path.
 * The interface is mainly introduced to make the absoluteizeURI method available when
 * the ServletServiceContext is proxied.
 * 
 * @version $Id$
 */
public interface Absolutizable {

    /**
     * Takes the scheme specific part of a servlet service URI (the scheme is the
     * responsibilty of the ServletSource) and resolve it with respect to the
     * servlets mount point.
     * 
     * @param uri
     * @return
     * @throws URISyntaxException
     */
    public URI absolutizeURI(URI uri) throws URISyntaxException;
}