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
package org.apache.cocoon.classloader.reloading;

//TODO rcl
//import java.net.URL;
//import java.net.URLStreamHandlerFactory;
//import java.util.List;
//import org.apache.cocoon.classloader.DefaultClassLoader;
// import org.apache.commons.jci.stores.ResourceStore;

/**
 * @version $Id$
 * @since 2.2
 */
public class ReloadingClassLoader { // TODO rcl extends DefaultClassLoader {
//
//    private ResourceStore[] stores = new ResourceStore[0];
//
//    /**
//     * Alternate constructor to define a parent and initial <code>URL</code>
//     * s.
//     */
//    public ReloadingClassLoader(URL[] urls, List includePatterns, List excludePatterns, final ClassLoader parent) {
//        this(urls, includePatterns, excludePatterns, parent, null);
//    }
//
//    /**
//     * Alternate constructor to define a parent, initial <code>URL</code>s,
//     * and a default <code>URLStreamHandlerFactory</code>.
//     */
//    public ReloadingClassLoader(final URL[] urls, List includePatterns, List excludePatterns, ClassLoader parent, URLStreamHandlerFactory factory) {
//        super(urls, includePatterns, excludePatterns, parent, factory);
//    }
//
//    public void addResourceStore(final ResourceStore pStore) {
//        final int n = this.stores.length;
//        final ResourceStore[] newStores = new ResourceStore[n + 1];
//        System.arraycopy(this.stores, 0, newStores, 0, n);
//        newStores[n] = pStore;
//        this.stores = newStores;
//    }
//
//    private Class fastFindClass(final String name) {
//        
//        if (stores != null) {
//            for (int i = 0; i < stores.length; i++) {
//                final ResourceStore store = stores[i];
//                final byte[] clazzBytes = store.read(name);
//                if (clazzBytes != null) {
//                    return defineClass(name, clazzBytes, 0, clazzBytes.length);
//                }            
//            }
//        }
//        
//        return null;            
//    }
//    
//    protected Class getClass(String name) throws ClassNotFoundException {
//        Class clazz = fastFindClass(name);
//        
//        if (clazz == null) {
//            return super.getClass(name);
//        }
//        return clazz;
//    }
}

