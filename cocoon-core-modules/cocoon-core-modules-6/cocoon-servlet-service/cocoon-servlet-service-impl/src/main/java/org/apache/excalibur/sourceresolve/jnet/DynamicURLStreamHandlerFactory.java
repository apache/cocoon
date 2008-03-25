/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.excalibur.sourceresolve.jnet;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class DynamicURLStreamHandlerFactory extends ParentAwareURLStreamHandlerFactory {

    protected static final ThreadLocal FACTORY = new InheritableThreadLocal();

    public static void push(URLStreamHandlerFactory factory) {
        // no need to synchronize as we use a thread local
        if ( !(factory instanceof ParentAwareURLStreamHandlerFactory) ) {
            factory = new URLStreamHandlerFactoryWrapper(factory);
        }
        URLStreamHandlerFactory old = (URLStreamHandlerFactory) FACTORY.get();
        ((ParentAwareURLStreamHandlerFactory)factory).setParentFactory(old);
        FACTORY.set(factory);
    }

    public static void pop() {
        ParentAwareURLStreamHandlerFactory factory = (ParentAwareURLStreamHandlerFactory)FACTORY.get();
        if ( factory != null ) {
            FACTORY.set(factory.getParent());
        }
    }

    /**
     * @see org.apache.excalibur.sourceresolve.jnet.ParentAwareURLStreamHandlerFactory#create(java.lang.String)
     */
    protected URLStreamHandler create(String protocol) {
        ParentAwareURLStreamHandlerFactory factory = (ParentAwareURLStreamHandlerFactory)FACTORY.get();
        return factory.createURLStreamHandler(protocol);
    }
}
