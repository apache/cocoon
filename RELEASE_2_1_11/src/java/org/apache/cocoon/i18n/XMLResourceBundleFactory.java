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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.store.Store;

import org.apache.cocoon.util.NetUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This is the XMLResourceBundleFactory, the method for getting and creating
 * XMLResourceBundles.
 *
 * @author <a href="mailto:mengelhart@earthtrip.com">Mike Engelhart</a>
 * @author <a href="mailto:neeme@one.lv">Neeme Praks</a>
 * @author <a href="mailto:oleg@one.lv">Oleg Podolsky</a>
 * @author <a href="mailto:kpiroumian@apache.org">Konstantin Piroumian</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version $Id$
 */
public class XMLResourceBundleFactory extends AbstractLogEnabled
                                      implements BundleFactory, Serviceable, Configurable,
                                                 Disposable, ThreadSafe {

    /**
     * Root directory to all bundle names
     */
    private String directory;

    /**
     * Reload check interval in milliseconds.
     * Defaults to 60000 (1 minute), use <code>-1</code> to
     * disable reloads and <code>0</code> to check for modifications
     * on each catalogue request.
     */
    private long interval;

    /**
     * Service Manager
     */
    protected ServiceManager manager;

    /**
     * Source resolver
     */
    protected SourceResolver resolver;

    /**
     * Store of the loaded bundles
     */
    protected Store cache;


    //
    // Lifecycle
    //

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
    }

    /**
     * Configure the component.
     *
     * @param configuration the configuration
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        this.directory = configuration.getChild(ConfigurationKeys.ROOT_DIRECTORY).getValue("");

        String cacheRole = configuration.getChild(ConfigurationKeys.STORE_ROLE).getValue(Store.TRANSIENT_STORE);
        try {
            this.cache = (Store) this.manager.lookup(cacheRole);
        } catch (ServiceException e) {
            throw new ConfigurationException("Unable to lookup store '" + cacheRole + "'");
        }

        this.interval = configuration.getChild(ConfigurationKeys.RELOAD_INTERVAL).getValueAsLong(60000L);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Bundle directory '" + this.directory + "'");
            getLogger().debug("Store role '" + cacheRole + "'");
        }
    }

    /**
     * Disposes this component.
     */
    public void dispose() {
        this.manager.release(this.resolver);
        this.manager.release(this.cache);
        this.resolver = null;
        this.cache = null;
        this.manager = null;
    }

    //
    // BundleFactory Interface
    //

    /**
     * Returns the root directory to all bundles.
     *
     * @return the directory path
     */
    protected String getDirectory() {
        return this.directory;
    }

    /**
     * Select a bundle based on the bundle name and the locale name.
     *
     * @param name        bundle name
     * @param locale      locale name
     * @return            the bundle
     * @exception         ComponentException if a bundle is not found
     */
    public Bundle select(String name, String locale) throws ComponentException {
        return select(getDirectory(), name, locale);
    }

    /**
     * Select a bundle based on the bundle name and the locale.
     *
     * @param name        bundle name
     * @param locale      locale
     * @return            the bundle
     * @exception         ComponentException if a bundle is not found
     */
    public Bundle select(String name, Locale locale) throws ComponentException {
        return select(getDirectory(), name, locale);
    }

    /**
     * Select a bundle based on the catalogue base location, bundle name,
     * and the locale name.
     *
     * @param directory   catalogue base location (URI)
     * @param name        bundle name
     * @param localeName  locale name
     * @return            the bundle
     * @exception         ComponentException if a bundle is not found
     */
    public Bundle select(String directory, String name, String localeName)
    throws ComponentException {
        return select(directory, name, new Locale(localeName, localeName));
    }

    /**
     * Select a bundle based on the catalogue base location, bundle name,
     * and the locale.
     *
     * @param directory   catalogue base location (URI)
     * @param name        bundle name
     * @param locale      locale
     * @return            the bundle
     * @exception         ComponentException if a bundle is not found
     */
    public Bundle select(String directory, String name, Locale locale)
    throws ComponentException {
        return select(new String[] { directory }, name, locale);
    }

    /**
     * Select a bundle based on the catalogue base location, bundle name,
     * and the locale.
     *
     * @param directories catalogue base location (URI)
     * @param name        bundle name
     * @param locale      locale
     * @return            the bundle
     * @exception         ComponentException if a bundle is not found
     */
    public Bundle select(String[] directories, String name, Locale locale)
    throws ComponentException {
        Bundle bundle = _select(directories, 0, name, locale);
        if (bundle == null) {
            throw new ComponentException(name, "Unable to locate resource: " + name);
        }
        return bundle;
    }

    public void release(Bundle bundle) {
        // Do nothing
    }

    //
    // Implementation
    //

    /**
     * Select a bundle based on bundle name and locale.
     *
     * @param directories       catalogue location(s)
     * @param name              bundle name
     * @param locale            locale
     * @return                  the bundle
     */
    private XMLResourceBundle _select(String[] directories, int index, String name,
                                      Locale locale)
    throws ComponentException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Selecting from: " + name + ", locale: " + locale +
                              ", directory: " + directories[index]);
        }

        final String cacheKey = "XRB" + getCacheKey(directories, index, name, locale);

        XMLResourceBundle bundle = selectCached(cacheKey);
        if (bundle == null) {
            synchronized (this) {
                bundle = selectCached(cacheKey);
                if (bundle == null) {
                    boolean localeAvailable = (locale != null && !locale.getLanguage().equals(""));
                    index++;

                    // Find parent bundle first
                    XMLResourceBundle parent = null;
                    if (localeAvailable && index == directories.length) {
                        // all directories have been searched with this locale,
                        // now start again with the first directory and the parent locale
                        parent = _select(directories, 0, name, getParentLocale(locale));
                    } else if (index < directories.length) {
                        // there are directories left to search for with this locale
                        parent = _select(directories, index, name, locale);
                    }

                    // Create this bundle (if source exists) and pass parent to it.
                    final String sourceURI = getSourceURI(directories[index - 1], name, locale);
                    bundle = _create(sourceURI, locale, parent);
                    updateCache(cacheKey, bundle);
                }
            }
        }
        return bundle;
    }

    /**
     * Constructs new bundle.
     *
     * <p>
     * If there is a problem loading the bundle, created bundle will be empty.
     *
     * @param sourceURI   source URI of the XML resource bundle
     * @param locale      locale of the bundle
     * @param parent      parent bundle, if any
     * @return            the bundle
     */
    private XMLResourceBundle _create(String sourceURI,
                                      Locale locale,
                                      XMLResourceBundle parent) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Creating bundle <" + sourceURI + ">");
        }

        XMLResourceBundle bundle = new XMLResourceBundle(sourceURI, locale, parent);
        bundle.enableLogging(getLogger());
        bundle.reload(this.resolver, this.interval);
        return bundle;
    }

    /**
     * Returns the next locale up the parent hierarchy.
     * E.g. the parent of new Locale("en","us","mac") would be
     * new Locale("en", "us", "").
     *
     * @param locale      the locale
     * @return            the parent locale
     */
    protected Locale getParentLocale(Locale locale) {
        Locale newloc;
        if (locale.getVariant().length() == 0) {
            if (locale.getCountry().length() == 0) {
                newloc = new Locale("", "", "");
            } else {
                newloc = new Locale(locale.getLanguage(), "", "");
            }
        } else {
            newloc = new Locale(locale.getLanguage(), locale.getCountry(), "");
        }
        return newloc;
    }

    /**
     * Creates a cache key for the bundle.
     * @return the cache key
     */
    protected String getCacheKey(String[] directories, int index, String name, Locale locale)
    throws ComponentException {
        StringBuffer cacheKey = new StringBuffer();
        if (index < directories.length) {
            cacheKey.append(":");
            cacheKey.append(getSourceURI(directories[index], name, locale));
            index++;
            cacheKey.append(getCacheKey(directories, index, name, locale));
        } else if ((locale != null && !locale.getLanguage().equals(""))) {
            cacheKey.append(getCacheKey(directories, 0, name, getParentLocale(locale)));
        }
        return cacheKey.toString();
    }

    /**
     * Maps a bundle name and locale to a bundle source URI.
     * If you need a different mapping, then just override this method.
     *
     * @param base    the base URI for the catalogues
     * @param name    the name of the catalogue
     * @param locale  the locale of the bundle
     * @return        the source URI for the bundle
     */
    protected String getSourceURI(String base, String name, Locale locale)
    throws ComponentException {
        // If base is null default to the current location
        if (base == null) {
            base = "";
        }

        // Resolve base URI
        Source src = null;
        Map parameters = Collections.EMPTY_MAP;
        StringBuffer sb = new StringBuffer();
        try {
            src = this.resolver.resolveURI(base);

            // Deparameterize base URL before adding catalogue name
            String uri = NetUtils.deparameterize(src.getURI(),
                                                 parameters = new HashMap(7));

            // Append trailing slash
            sb.append(uri);
            if (!uri.endsWith("/")) {
                sb.append('/');
            }

        } catch (IOException e) {
            throw new ComponentException("Cannot resolve catalogue base URI <" + base + ">", name, e);
        } finally {
            this.resolver.release(src);
        }

        // Append catalogue name
        sb.append(name);

        // Append catalogue locale
        if (locale != null) {
            if (!locale.getLanguage().equals("")) {
                sb.append("_");
                sb.append(locale.getLanguage());
            }
            if (!locale.getCountry().equals("")) {
                sb.append("_");
                sb.append(locale.getCountry());
            }
            if (!locale.getVariant().equals("")) {
                sb.append("_");
                sb.append(locale.getVariant());
            }
        }
        sb.append(".xml");

        // Reconstruct complete bundle URI with parameters
        String uri = NetUtils.parameterize(sb.toString(), parameters);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Resolved name: " + name +
                              ", locale: " + locale + " --> " + uri);
        }
        return uri;
    }

    /**
     * Selects a bundle from the cache, and reloads it if needed.
     *
     * @param cacheKey    caching key of the bundle
     * @return            the cached bundle; null, if not found
     */
    protected XMLResourceBundle selectCached(String cacheKey) {
        XMLResourceBundle bundle = (XMLResourceBundle) this.cache.get(cacheKey);

        if (bundle != null && this.interval != -1) {
            // Reload this bundle and all parent bundles, as necessary
            for (XMLResourceBundle b = bundle; b != null; b = (XMLResourceBundle) b.parent) {
                b.reload(this.resolver, this.interval);
            }
        }

        return bundle;
    }

    /**
     * Stores bundle in the cache.
     *
     * @param cacheKey    caching key of the bundle
     * @param bundle      bundle to be placed in the cache
     */
    protected void updateCache(String cacheKey, XMLResourceBundle bundle) {
        try {
            this.cache.store(cacheKey, bundle);
        } catch (IOException e) {
            getLogger().error("Bundle <" + bundle.getSourceURI() + ">: unable to store.", e);
        }
    }
}
