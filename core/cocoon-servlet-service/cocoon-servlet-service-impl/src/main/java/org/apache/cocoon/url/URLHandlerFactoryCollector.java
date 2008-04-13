/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.url;

import java.net.URLStreamHandlerFactory;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.excalibur.sourceresolve.jnet.DynamicURLStreamHandlerFactory;
import org.apache.excalibur.sourceresolve.jnet.Installer;
import org.aspectj.lang.ProceedingJoinPoint;

public class URLHandlerFactoryCollector {

    private Map urlHandlerFactories = Collections.EMPTY_MAP;

    private boolean urlStreamHandlerInstalled;

    public Object installURLHandlers(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            if (!this.urlStreamHandlerInstalled) {
                Installer.setURLStreamHandlerFactory(new DynamicURLStreamHandlerFactory());
                this.urlStreamHandlerInstalled = true;
            }

            for (Iterator i = this.urlHandlerFactories.values().iterator(); i.hasNext();) {
                URLStreamHandlerFactory streamHandlerFactory = (URLStreamHandlerFactory) i.next();
                DynamicURLStreamHandlerFactory.push(streamHandlerFactory);
            }

            return proceedingJoinPoint.proceed();
        } finally {
            for (Iterator i = this.urlHandlerFactories.values().iterator(); i.hasNext(); i.next()) {
                DynamicURLStreamHandlerFactory.pop();
            }
        }
    }

    public void setUrlHandlerFactories(Map urlHandlerFactories) {
        this.urlHandlerFactories = urlHandlerFactories;
    }
}
