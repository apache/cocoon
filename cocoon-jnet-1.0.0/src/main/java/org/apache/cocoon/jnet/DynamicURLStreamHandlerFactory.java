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
package org.apache.cocoon.jnet;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DynamicURLStreamHandlerFactory implements URLStreamHandlerFactory {

    private static final ThreadLocal FACTORIES = new InheritableThreadLocal();

    public static void pop() {
        getList().remove(0);
    }

    public static void push(URLStreamHandlerFactory factory) {
        getList().add(0, factory);
    }

    private static List getList() {
        List list = (List) FACTORIES.get();

        if (list == null) {
            list = new LinkedList();
            FACTORIES.set(list);
        }

        return list;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.net.URLStreamHandlerFactory#createURLStreamHandler(java.lang.String)
     */
    public URLStreamHandler createURLStreamHandler(String protocol) {
        List list = getList();

        for (Iterator i = list.iterator(); i.hasNext();) {
            URLStreamHandler handler = ((URLStreamHandlerFactory)i.next()).createURLStreamHandler(protocol);

            if (handler != null) {
                return handler;
            }
        }

        return null;
    }
}
