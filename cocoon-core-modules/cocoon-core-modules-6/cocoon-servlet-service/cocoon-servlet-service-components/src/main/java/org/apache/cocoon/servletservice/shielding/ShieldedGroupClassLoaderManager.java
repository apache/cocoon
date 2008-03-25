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
package org.apache.cocoon.servletservice.shielding;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.cocoon.classloader.ClassLoaderConfiguration;
import org.apache.cocoon.classloader.ClassLoaderFactory;
import org.apache.cocoon.classloader.DefaultClassLoaderFactory;

/**
 * Used by the ShieldingServletService to obtain a shielded classloader. Supports
 * groups of servlets that use the same classloader that is still shielded from
 * the rest of the webapp.
 *
 * @version $Id$
 * @since 1.0.0
 */
public class ShieldedGroupClassLoaderManager {

    // parameters

    public static final String SHIELDED_GROUP_PARAM = "shielded-group";

    public static final String SHIELDED_LIB_PARAM = "shielded-lib";

    public static final String SHIELDED_CLASSES_PARAM = "shielded-classes";

    public static final String CLASSLOADER_FACTORY_PARAM = "classloader-factory";

    // standard directories

    /**
     * Default directory for shielded libs (below COB-INF), must start with "/"
     */
    protected static final String SHIELDED_LIB = "shielded/lib";

    /**
     * Default directory for shielded classes (below COB-INF), must start with
     * "/"
     */
    protected static final String SHIELDED_CLASSES = "shielded/classes";

    /**
     * Maps group names (Strings) to their classloader.
     */
    protected static Map groupClassloaders = new HashMap();

    /**
     * Get the classloader, either an already instantiated classloader for a
     * group or create a completely new one.
     */
    public static synchronized ClassLoader getClassLoader(
            ServletContext servletContext, Map parameters)
            throws ServletException {
        final String classLoaderGroup = (String) parameters
                .get(SHIELDED_GROUP_PARAM);
        if (classLoaderGroup != null) {
            // check if the classloader for this group was already created
            if (groupClassloaders.containsKey(classLoaderGroup)) {
                return (ClassLoader) groupClassloaders.get(classLoaderGroup);
            }
        }

        ClassLoader shieldedClassLoader;

        shieldedClassLoader = createClassLoader(
                ShieldedGroupClassLoaderManager.class.getClassLoader(),
                servletContext, parameters);

        // if this classloader is part of a group, add it to the map
        if (classLoaderGroup != null) {
            groupClassloaders.put(classLoaderGroup, shieldedClassLoader);
        }

        return shieldedClassLoader;
    }

    /**
     * Create the shielded class loader.
     */
    protected static ClassLoader createClassLoader(ClassLoader parent,
            ServletContext servletContext, Map parameters)
            throws ServletException {
        String classesDirectory = ShieldedGroupClassLoaderManager.SHIELDED_CLASSES;
        String jarDirectory = ShieldedGroupClassLoaderManager.SHIELDED_LIB;

        if (parameters.get(SHIELDED_CLASSES_PARAM) != null) {
            classesDirectory = (String) parameters.get(SHIELDED_CLASSES_PARAM);
        }
        if (parameters.get(SHIELDED_LIB_PARAM) != null) {
            jarDirectory = (String) parameters.get(SHIELDED_LIB_PARAM);
        }

        ClassLoaderConfiguration config = new ClassLoaderConfiguration();
        config.addClassDirectory(classesDirectory);
        config.addLibDirectory(jarDirectory);

        String factoryClassName = DefaultClassLoaderFactory.class.getName();
        if (parameters.get(CLASSLOADER_FACTORY_PARAM) != null) {
            factoryClassName = (String) parameters.get(CLASSLOADER_FACTORY_PARAM);
        }

        try {
            final Class classLoaderFactoryClass = Class.forName(factoryClassName);
            ClassLoaderFactory factory = (ClassLoaderFactory) classLoaderFactoryClass.newInstance();
            return factory.createClassLoader(
                    config.getClass().getClassLoader(), config, servletContext);
        } catch (InstantiationException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (SecurityException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (Exception e) {
            throw new ServletException(e.getMessage(), e);
        }
    }
}
