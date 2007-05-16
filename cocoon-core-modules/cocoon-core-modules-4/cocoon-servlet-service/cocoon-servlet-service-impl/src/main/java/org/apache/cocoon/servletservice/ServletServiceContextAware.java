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

import javax.servlet.ServletContext;

/**
 * Inteface for making the servlet service context of a servlet service
 * available. It is needed for inter servlet communication in the servlet
 * service framework. It is normaly introduced by an AOP mixin.
 * 
 * @version $Id$
 */
public interface ServletServiceContextAware {
    /**
     * The servlet context used for inter servlet service communication
     * @return
     */
    public ServletContext getServletServiceContext();
}