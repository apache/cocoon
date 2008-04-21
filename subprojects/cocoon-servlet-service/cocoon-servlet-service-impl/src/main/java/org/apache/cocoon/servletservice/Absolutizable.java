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
 * By this interface, a {@link ServletServiceContext} provides absolute information
 * about a mounted servlet service.
 *
 * @version $Id$
 * @since 1.0.0
 */
public interface Absolutizable {

    /**
     * Takes the scheme specific part of a servlet service URI (the scheme is the
     * responsibilty of the ServletSource) and resolve it with respect to the
     * servlets mount point.
     *
     * @param uri relative uri
     * @return absolutized uri
     * @throws URISyntaxException
     */
    URI absolutizeURI(URI uri) throws URISyntaxException;

    /**
     * Get the fully qualified servlet service name of a connected service.
     *
     * @param connectionName
     * @return The fully qualified servlet service name of a connected service.
     */
    String getServiceName(String connectionName);

    /**
     * Get the fully qualifed service name.
     *
     * @return The fully qualified servlet service name.
     */
    String getServiceName();

}
