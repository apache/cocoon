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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * The installer is a general purpose class to install an own {@link URLStreamHandlerFactory} in any environment.
 */
public class URLStreamHandlerFactoryInstaller {

    public static void setURLStreamHandlerFactory(URLStreamHandlerFactory factory) throws Exception {
        try {
            // if we can set the factory, its the first!
            URL.setURLStreamHandlerFactory(new ParentableURLStreamHandlerFactory(factory, null));
        } catch (Error err) {
            ParentableURLStreamHandlerFactory currentFactory = getCurrentFactory();
            setCurrentFactory(new ParentableURLStreamHandlerFactory(factory, currentFactory));
        }
    }

    private static ParentableURLStreamHandlerFactory getCurrentFactory() throws Exception {
        Field factoryField = getFactoryField();

        URLStreamHandlerFactory currentFactory = (URLStreamHandlerFactory) factoryField.get(null);
        if (currentFactory instanceof ParentableURLStreamHandlerFactory) {
            return (ParentableURLStreamHandlerFactory) currentFactory;
        }

        return new ParentableURLStreamHandlerFactory(currentFactory, null);
    }

    private static Field getFactoryField() throws Exception {
        // let's use reflection to get the field holding the factory
        final Field[] fields = URL.class.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Field current = fields[i];
            if (Modifier.isStatic(current.getModifiers()) && current.getType().equals(URLStreamHandlerFactory.class)) {
                current.setAccessible(true);
                return current;
            }
        }

        throw new Exception("Unable to detect static field in the URL class for the URLStreamHandlerFactory."
                + " Please report this error together with your exact environment to the Apache Excalibur project.");
    }

    private static void setCurrentFactory(ParentableURLStreamHandlerFactory parentableURLStreamHandlerFactory) throws Exception {
        Field factoryField = getFactoryField();
        factoryField.set(null, parentableURLStreamHandlerFactory);
    }

    private static class ParentableURLStreamHandlerFactory implements URLStreamHandlerFactory {

        private final URLStreamHandlerFactory factory;
        private final ParentableURLStreamHandlerFactory parent;

        public ParentableURLStreamHandlerFactory(URLStreamHandlerFactory factory, ParentableURLStreamHandlerFactory parent) {
            super();
            this.parent = parent;
            this.factory = factory;
        }

        public URLStreamHandler createURLStreamHandler(String protocol) {
            URLStreamHandler handler = this.factory.createURLStreamHandler(protocol);

            if (handler == null && this.parent != null) {
                handler = this.parent.createURLStreamHandler(protocol);
            }

            return handler;
        }
    }
}
