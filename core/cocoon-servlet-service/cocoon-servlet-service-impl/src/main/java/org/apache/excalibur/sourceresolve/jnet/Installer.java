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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLStreamHandlerFactory;

/**
 * The installer is a general purpose class to install an own
 * {@link URLStreamHandlerFactory} in any environment.
 */
public class Installer {

    public static void setURLStreamHandlerFactory(URLStreamHandlerFactory factory)
    throws Exception {
        try {
            // if we can set the factory, its the first!
            URL.setURLStreamHandlerFactory(factory);
        } catch (Error err) {
            // let's use reflection to get the field holding the factory
            final Field[] fields = URL.class.getDeclaredFields();
            int index = 0;
            Field factoryField = null;
            while ( factoryField == null && index < fields.length ) {
                final Field current = fields[index];
                if ( Modifier.isStatic( current.getModifiers() ) && current.getType().equals( URLStreamHandlerFactory.class ) ) {
                    factoryField = current;
                    factoryField.setAccessible(true);
                } else {
                    index++;
                }
            }
            if ( factoryField == null ) {
                throw new Exception("Unable to detect static field in the URL class for the URLStreamHandlerFactory. Please report this error together with your exact environment to the Apache Excalibur project.");
            }
            try {
                URLStreamHandlerFactory oldFactory = (URLStreamHandlerFactory)factoryField.get(null);
                if ( factory instanceof ParentAwareURLStreamHandlerFactory ) {
                    ((ParentAwareURLStreamHandlerFactory)factory).setParentFactory(oldFactory);
                }
                factoryField.set(null, factory);
            } catch (IllegalArgumentException e) {
                throw new Exception("Unable to set url stream handler factory " + factory);
            } catch (IllegalAccessException e) {
                throw new Exception("Unable to set url stream handler factory " + factory);
            }
        }
    }

    protected static Field getStaticURLStreamHandlerFactoryField() {
        Field[] fields = URL.class.getDeclaredFields();
        for ( int i = 0; i < fields.length; i++ ) {
            if ( Modifier.isStatic( fields[i].getModifiers() ) && fields[i].getType().equals( URLStreamHandlerFactory.class ) ) {
                fields[i].setAccessible( true );
                return fields[i];
            }
        }
        return null;
    }
}
