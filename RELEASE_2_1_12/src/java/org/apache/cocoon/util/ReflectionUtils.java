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
package org.apache.cocoon.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @version $Id$
 *
 */
public final class ReflectionUtils {

	public interface Matcher {
        boolean matches(final String pName);
    }
	    
    public interface Indexer {
        void put(final Map pMap, final String pKey, final Object pObject);
    }
	    
    private static DefaultIndexer defaultIndexer = new DefaultIndexer();
    private static DefaultMatcher defaultMatcher = new DefaultMatcher();
	    
    private static class DefaultMatcher implements Matcher {
        public boolean matches(final String pName) {
            return pName.startsWith("do");
        }
    }
	    
    private static class DefaultIndexer implements Indexer {
        public void put(final Map pMap, final String pKey, final Object pObject) {

            // doAction -> action
            final String name = Character.toLowerCase(pKey.charAt(2)) + pKey.substring(3);

            System.out.println("reflecting " + name);
            pMap.put(name, pObject);
        }
    }
	    
    public static Map discoverFields(
            final Class pClazz,
            final Matcher pMatcher
            ) {
        
        return discoverFields(pClazz, pMatcher, defaultIndexer);
    }

    public static Map discoverFields(
            final Class pClazz
            ) {
        
        return discoverFields(pClazz, defaultMatcher, defaultIndexer);
    }
    
    public static Map discoverFields(
            final Class pClazz,
            final Matcher pMatcher,
            final Indexer pIndexer
            ) {
        
        System.out.println("discovering fields on " + pClazz.getName());
        
        final Map result = new HashMap();

        Class current = pClazz;
        do {
            final Field[] fields = current.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                final String fname = fields[i].getName();
                if (pMatcher.matches(fname)) {
                    pIndexer.put(result, fname, fields[i]);
                }
            }
            current = current.getSuperclass();
        } while(current != null);
     
        return result;
    }    

    
    public static Map discoverMethods(
            final Class pClazz,
            final Matcher pMatcher
            ) {
        
        return discoverMethods(pClazz, pMatcher, defaultIndexer);
    }

    public static Map discoverMethods(
            final Class pClazz
            ) {
        
        return discoverMethods(pClazz, defaultMatcher, defaultIndexer);
    }
    
    public static Map discoverMethods(
            final Class pClazz,
            final Matcher pMatcher,
            final Indexer pIndexer
            ) {
        
        System.out.println("discovering methods on " + pClazz.getName());
        
        final Map result = new HashMap();

        Class current = pClazz;
        do {
            final Method[] methods = current.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                final String mname = methods[i].getName();
                if (pMatcher.matches(mname)) {
                    pIndexer.put(result, mname, methods[i]);
                }
            }
            current = current.getSuperclass();
        } while(current != null);
     
        return result;
    }    

}
