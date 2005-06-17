/*
 * Copyright 1999-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.SAXParseException;

/**
 * This is the XMLResourceBundleFactory, the method for getting and creating
 * XMLResourceBundles.
 *
 * @author <a href="mailto:mengelhart@earthtrip.com">Mike Engelhart</a>
 * @author <a href="mailto:neeme@one.lv">Neeme Praks</a>
 * @author <a href="mailto:oleg@one.lv">Oleg Podolsky</a>
 * @author <a href="mailto:kpiroumian@apache.org">Konstantin Piroumian</a>
 * @version $Id$
 */
public class XMLResourceBundleFactory
       implements BundleFactory, Serviceable, Configurable, Disposable, ThreadSafe, LogEnabled {

    /**
     * Cache of the bundles by file name
     */
    protected final Map cache = Collections.synchronizedMap(new HashMap());

    /**
     * Cache for the file names of the bundles that were not found
     */
    protected final Map cacheNotFound = new HashMap();

    /**
     * Should we load bundles to cache on startup or not?
     */
    protected boolean cacheAtStartup;

    /**
     * Root directory to all bundle names
     */
    protected String directory;

    /**
     * The logger
     */
    private Logger logger;

    /**
     * Service Manager
     */
    protected ServiceManager manager;

    /**
     * Source resolver
     */
    protected SourceResolver resolver;


    /**
     * Default constructor
     */
    public XMLResourceBundleFactory() {
    }

    /**
     * @see org.apache.avalon.framework.logger.LogEnabled#enableLogging(org.apache.avalon.framework.logger.Logger)
     */
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    public void dispose() {
        Iterator i = this.cache.values().iterator();
        while (i.hasNext()) {
            Object bundle = i.next();
            if (bundle instanceof Disposable) {
                ((Disposable)bundle).dispose();
            }
            i.remove();
        }
        this.manager.release(this.resolver);
        this.manager = null;
    }

    /**
     * Configure the component.
     *
     * @param configuration the configuration
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        this.cacheAtStartup = configuration.getChild(ConfigurationKeys.CACHE_AT_STARTUP).getValueAsBoolean(false);

        try {
            this.directory = configuration.getChild(ConfigurationKeys.ROOT_DIRECTORY, true).getValue();
        } catch (ConfigurationException e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Root directory not provided in configuration, using default (root).");
            }
            this.directory = "";
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Configured with: cacheAtStartup = " +
                              this.cacheAtStartup + ", directory = '" + this.directory + "'");
        }
    }

    /**
     * Returns the root directory to all bundles.
     *
     * @return the directory path
     */
    protected String getDirectory() {
        return directory;
    }

    /**
     * Should we load bundles to cache on startup or not?
     *
     * @return true if pre-loading all resources; false otherwise
     */
    protected boolean cacheAtStartup() {
        return cacheAtStartup;
    }

    /**
     * Select a bundle based on the bundle name and the locale name.
     *
     * @param name    bundle name
     * @param locale  locale name
     * @return        the bundle
     * @exception     Exception if a bundle is not found
     */
    public Bundle select(String name, String locale) throws Exception {
        return select(getDirectory(), name, locale);
    }

    /**
     * Select a bundle based on the bundle name and the locale.
     *
     * @param name    bundle name
     * @param locale  locale
     * @return        the bundle
     * @exception     Exception if a bundle is not found
     */
    public Bundle select(String name, Locale locale) throws Exception {
        return select(getDirectory(), name, locale);
    }

    /**
     * Select a bundle based on the catalogue base location, bundle name,
     * and the locale name.
     *
     * @param directory    catalogue base location (URI)
     * @param name    bundle name
     * @param localeName  locale name
     * @return        the bundle
     * @exception     Exception if a bundle is not found
     */
    public Bundle select(String directory, String name, String localeName)
    throws Exception {
        return select(directory, name, new Locale(localeName, localeName));
    }

    /**
     * Select a bundle based on the catalogue base location, bundle name,
     * and the locale.
     *
     * @param directory    catalogue base location (URI)
     * @param name    bundle name
     * @param locale  locale
     * @return        the bundle
     * @exception     Exception if a bundle is not found
     */
    public Bundle select(String directory, String name, Locale locale)
    throws Exception {
        String []directories = new String[1];
        directories[0] = directory;
        return select(directories, name, locale);
    }

    /**
     * Select a bundle based on the catalogue base location, bundle name,
     * and the locale.
     *
     * @param directories catalogue base location (URI)
     * @param name    bundle name
     * @param locale  locale
     * @return        the bundle
     * @exception     Exception if a bundle is not found
     */
    public Bundle select(String[] directories, String name, Locale locale)
    throws Exception {
        Bundle bundle = _select(directories, 0, name, locale);
        if (bundle == null) {
            throw new Exception("Unable to locate resource: " + name);
        }
        return bundle;
    }

    /**
     * Select a bundle based on bundle name and locale.
     *
     * @param directories       catalogue location(s)
     * @param name              bundle name
     * @param locale            locale
     * @return                  the bundle
     */
    private XMLResourceBundle _select(String[] directories, int index, String name,
                                      Locale locale) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Selecting from: " + name + ", locale: " + locale +
                              ", directory: " + directories[index]);
        }

        final String cacheKey = getCacheKey(directories, index, name, locale);
        final String fileName = getFileName(directories[index], name, locale);

        XMLResourceBundle bundle = selectCached(cacheKey, fileName);
        if (bundle == null) {
            synchronized (this) {
                bundle = selectCached(cacheKey, fileName);
                if (bundle == null) {
                    boolean localeAvailable = (locale != null && !locale.getLanguage().equals(""));
                    index++;

                    XMLResourceBundle parentBundle = null;
                    if (localeAvailable && index == directories.length) {
                        // all directories have been searched with this locale,
                        // now start again with the first directory and the parent locale
                        parentBundle = _select(directories, 0, name, getParentLocale(locale));
                    } else if (index < directories.length) {
                        // there are directories left to search for with this locale
                        parentBundle = _select(directories, index, name, locale);
                    }

                    if (!isNotFoundBundle(cacheKey)) {
                        bundle = _loadBundle(name, fileName, locale, parentBundle);
                        updateCache(cacheKey, bundle);
                    }

                    if (bundle == null) {
                        return parentBundle;
                    }
                }
            }
        }
        return bundle;
    }

    /**
     * Construct a bundle based on bundle name, file name and locale.
     *
     * @param name              bundle name
     * @param fileName          full path to source XML file
     * @param locale            locale
     * @return                  the bundle, null if loading failed
     */
    private XMLResourceBundle _loadBundle(String name, String fileName, Locale locale,
                                          XMLResourceBundle parentBundle) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Loading bundle: " + name + ", locale: " + locale +
                              ", uri: " + fileName);
        }

        XMLResourceBundle bundle = null;
        try {
            bundle = new XMLResourceBundle();
            bundle.enableLogging(this.logger);
            bundle.service(this.manager);
            bundle.init(name, fileName, locale, parentBundle);
            return bundle;
        } catch (ResourceNotFoundException e) {
            getLogger().info("Resource not found: " + name + ", locale: " + locale +
                             ", bundleName: " + fileName + ". Exception: " + e);
        } catch (SourceNotFoundException e) {
            getLogger().info("Resource not found: " + name + ", locale: " + locale +
                             ", bundleName: " + fileName + ". Exception: " + e);
        } catch (SAXParseException e) {
            getLogger().error("Incorrect resource format", e);
        } catch (Exception e) {
            getLogger().error("Resource loading failed", e);
        }

        return null;
    }

    public void release(Bundle bundle) {
        // Do nothing
    }

    /**
     * Returns the next locale up the parent hierarchy.
     * E.g. the parent of new Locale("en","us","mac") would be
     * new Locale("en", "us", "").
     *
     * @param locale            the locale
     * @return                  the parent locale
     */
    protected Locale getParentLocale(Locale locale) {
        Locale newloc;
        if (locale.getVariant().equals("")) {
            if (locale.getCountry().equals("")) {
                newloc = new Locale("", "", "");
            } else {
                newloc = new Locale(locale.getLanguage(), "", "");
            }
        } else {
            newloc = new Locale(locale.getLanguage(), locale.getCountry(), "");
        }
        return newloc;
    }

    protected String getCacheKey(String[] directories, int index, String name, Locale locale) {
        StringBuffer cacheKey = new StringBuffer();
        for (; index < directories.length; index++) {
            cacheKey.append(getFileName(directories[index], name, locale));
            cacheKey.append(":");
        }
        return cacheKey.toString();
    }

    /**
     * Maps a bundle name and locale to a full path in the filesystem.
     * If you need a different mapping, then just override this method.
     *
     * @param locale            the locale
     * @return                  the parent locale
     */
    protected String getFileName(String base, String name, Locale locale) {
        StringBuffer sb = new StringBuffer();
        if (base == null || base.length() == 0) {
            // FIXME (SW): can this happen?
        } else {
            try {
                Source src = this.resolver.resolveURI(base);
                String uri = src.getURI();
                sb.append(uri);
                if (!uri.endsWith("/")) {
                    sb.append('/');
                }
                this.resolver.release(src);
            } catch(IOException ioe) {
                throw new CascadingRuntimeException("Cannot resolve " + base, ioe);
            }
        }

        sb.append(name);

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

        String result = sb.toString();
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Resolved bundle name: " + name +
                              ", locale: " + locale + " --> " + result);
        }
        return result;
    }

    /**
     * Selects a bundle from the cache.
     *
     * @param cacheKey          caching key of the bundle
     * @param fileName          file name of the bundle
     * @return                  the cached bundle; null, if not found
     */
    protected XMLResourceBundle selectCached(String cacheKey, String fileName) {
        XMLResourceBundle bundle = (XMLResourceBundle) this.cache.get(cacheKey);
        if (bundle != null) {
            bundle.update(fileName);
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug((bundle == null? "NOT ":"") + "In cache: " + cacheKey);
        }
        return bundle;
    }

    /**
     * Checks if the bundle is in the &quot;not-found&quot; cache.
     *
     * @param cacheKey          caching key of the bundle
     * @return                  true, if the bundle wasn't found already before;
     *                          otherwise, false.
     */
    protected boolean isNotFoundBundle(String cacheKey) {
        Object result = this.cacheNotFound.get(cacheKey);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug((result == null? "NOT ":"") + "In not_found_cache: " + cacheKey);
        }
        return result != null;
    }

    /**
     * Stores bundle in the cache (or in the &quot;not-found&quot; cache,
     * if bundle is null)
     *
     * @param cacheKey          caching key of the bundle
     * @param bundle            bundle to be placed in the cache
     */
    protected void updateCache(String cacheKey, XMLResourceBundle bundle) {
        if (bundle == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Updating not_found_cache: " + cacheKey);
            }
            this.cacheNotFound.put(cacheKey, cacheKey);
        } else {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Updating cache: " + cacheKey);
            }
            this.cache.put(cacheKey, bundle);
        }
    }
}
