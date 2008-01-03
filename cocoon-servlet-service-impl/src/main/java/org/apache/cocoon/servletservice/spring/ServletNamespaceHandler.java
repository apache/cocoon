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
package org.apache.cocoon.servletservice.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * <p>
 * Spring namespace handler for the Cocoon servlet namespace (<code>http://cocoon.apache.org/schema/servlet</code>).
 * The {@link ServletDecorator} deals with the implementation details.
 *
 * @version $Id: ServletNamespaceHandler.java 562806 2007-08-05 02:26:41Z
 *          vgritsenko $
 * @since 1.0.0
 */
public class ServletNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionDecorator("context", new ServletDecorator());
    }

}
