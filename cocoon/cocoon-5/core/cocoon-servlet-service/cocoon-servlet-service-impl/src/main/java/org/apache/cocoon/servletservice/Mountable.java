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

/**
 * Interface for servlet services that are mounted on a specific path.
 * The inteface is not needed for using the {@link DispatcherServlet}
 * as it mounts all servlet beans that has a property named mountPath.
 * The main use for the interface is to b e able to create an AOP mixin
 * with an ordinary Java proxy.
 *
 * @version $Id$
 */
public interface Mountable {

    /**
     * The mount path of the servlet.
     *
     * @return servlet mount path
     */
    public String getMountPath();
}
