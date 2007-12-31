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
package org.apache.cocoon.i18n;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;

import java.util.Locale;

/**
 * Bundle Factory implementations are responsible for loading and providing
 * particular types of resource bundles, implementors of Bundle interface.
 *
 * @author <a href="mailto:kpiroumian@apache.org">Konstantin Piroumian</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version $Id$
 */
public interface BundleFactory extends Component {

    /**
     * Bundle factory ROLE name
     */
    String ROLE = BundleFactory.class.getName();

    /**
     * Constants for bundle factory configuration keys
     */
    static class ConfigurationKeys {
        /**
         * Configuration element specifying default location of the
         * resource bundles.
         *
         * @see BundleFactory#select(String, String)
         * @see BundleFactory#select(String, java.util.Locale)
         */
        public static final String ROOT_DIRECTORY = "catalogue-location";

        /**
         * Configuration element specifying role of the Store instance to use
         * for storing cached bundles
         * @since 2.1.8
         */
        public static final String STORE_ROLE = "store-role";

        /**
         * Configuration element specifying delay (in ms) between
         * reload checks.
         * @since 2.1.8
         */
        public static final String RELOAD_INTERVAL = "reload-interval";
    }

    /**
     * Select a bundle based on the catalogue base location, bundle name,
     * and the locale name.
     *
     * @param base    catalogue base location (URI)
     * @param bundleName    bundle name
     * @param locale  locale name
     * @return        the bundle
     * @exception     ComponentException if a bundle is not found
     */
    Bundle select(String base, String bundleName, String locale) throws ComponentException;

    /**
     * Select a bundle based on the catalogue base location, bundle name,
     * and the locale.
     *
     * @param base    catalogue base location (URI)
     * @param bundleName    bundle name
     * @param locale  locale
     * @return        the bundle
     * @exception     ComponentException if a bundle is not found
     */
    Bundle select(String base, String bundleName, Locale locale) throws ComponentException;

    /**
     * Select a bundle based on the catalogue base location, bundle name,
     * and the locale.
     *
     * @param directories    catalogue base location (URI)
     * @param bundleName    bundle name
     * @param locale  locale
     * @return        the bundle
     * @exception     ComponentException if a bundle is not found
     */
    Bundle select(String[] directories, String bundleName, Locale locale) throws ComponentException;

    /**
     * Select a bundle based on the bundle name and the locale name from
     * the default catalogue.
     *
     * @param bundleName    bundle name
     * @param locale  locale name
     * @return        the bundle
     * @exception     ComponentException if a bundle is not found
     */
    Bundle select(String bundleName, String locale) throws ComponentException;

    /**
     * Select a bundle based on the bundle name and the locale from
     * the default catalogue.
     *
     * @param bundleName    bundle name
     * @param locale  locale
     * @return        the bundle
     * @exception     ComponentException if a bundle is not found
     */
    Bundle select(String bundleName, Locale locale) throws ComponentException;

    /**
     * Releases a bundle back to the bundle factory when it's not needed
     * anymore.
     * @param bundle the bundle
     */
    void release(Bundle bundle);
}
