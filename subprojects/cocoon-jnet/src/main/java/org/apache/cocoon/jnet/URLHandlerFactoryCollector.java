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
package org.apache.cocoon.jnet;

import java.net.URLStreamHandlerFactory;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;

public class URLHandlerFactoryCollector {

    private Map<String, URLStreamHandlerFactory> urlHandlerFactories = Collections.emptyMap();

    private boolean urlStreamHandlerInstalled;

    public Object installURLHandlers(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            this.pushUrlHandlerFactories();
            return proceedingJoinPoint.proceed();
        } finally {
            this.popUrlHandlerFactories();
        }
    }

    public void pushUrlHandlerFactories() throws Exception {
        if (!this.urlStreamHandlerInstalled) {
            URLStreamHandlerFactoryInstaller.setURLStreamHandlerFactory(new DynamicURLStreamHandlerFactory());
            this.urlStreamHandlerInstalled = true;
        }

        for (Iterator<URLStreamHandlerFactory> i = this.urlHandlerFactories.values().iterator(); i.hasNext();) {
            URLStreamHandlerFactory streamHandlerFactory = i.next();
            DynamicURLStreamHandlerFactory.push(streamHandlerFactory);
        }
    }

    public void popUrlHandlerFactories() {
        for (Iterator<URLStreamHandlerFactory> i = this.urlHandlerFactories.values().iterator(); i.hasNext(); i
                .next()) {
            DynamicURLStreamHandlerFactory.pop();
        }
    }

    public void setUrlHandlerFactories(Map<String, URLStreamHandlerFactory> urlHandlerFactories) {
        this.urlHandlerFactories = urlHandlerFactories;
    }
}
