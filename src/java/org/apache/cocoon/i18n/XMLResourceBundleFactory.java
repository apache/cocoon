/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.DefaultServiceSelector;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.SourceNotFoundException;
import org.xml.sax.SAXParseException;

/**
 * This is the XMLResourceBundleFactory, the method for getting and creating
 * XMLResourceBundles.
 *
 * @author <a href="mailto:mengelhart@earthtrip.com">Mike Engelhart</a>
 * @author <a href="mailto:neeme@one.lv">Neeme Praks</a>
 * @author <a href="mailto:oleg@one.lv">Oleg Podolsky</a>
 * @author <a href="mailto:kpiroumian@apache.org">Konstantin Piroumian</a>
 * @version CVS $Id: XMLResourceBundleFactory.java,v 1.9 2004/03/08 14:02:47 cziegeler Exp $
 */
public class XMLResourceBundleFactory extends DefaultServiceSelector
        implements BundleFactory, Serviceable, Configurable, Disposable, ThreadSafe, LogEnabled {

    /** Should we load bundles to cache on startup or not? */
    protected boolean cacheAtStartup = false;

    /** Root directory to all bundle names */
    protected String directory;

    /** Cache for the names of the bundles that were not found */
    protected Map cacheNotFound = new HashMap();

    /** The logger */
    private Logger logger;

    /** Service Manager */
    protected ServiceManager manager = null;


    /** Default constructor. */
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

    public void service(ServiceManager manager) {
        this.manager = manager;
    }

    public void dispose() {
        Iterator i = getObjectMap().values().iterator();
        while (i.hasNext()) {
            Object bundle = i.next();
            if (bundle instanceof Disposable) {
                ((Disposable)bundle).dispose();
            }
            i.remove();
        }
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
                              cacheAtStartup + ", directory = '" + directory + "'");
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
     * @exception     ServiceException if a bundle is not found
     */
    public Bundle select(String name, String locale) throws ServiceException {
        return select(getDirectory(), name, locale);
    }

    /**
     * Select a bundle based on the bundle name and the locale.
     *
     * @param name    bundle name
     * @param locale  locale
     * @return        the bundle
     * @exception     ServiceException if a bundle is not found
     */
    public Bundle select(String name, Locale locale) throws ServiceException {
        return select(getDirectory(), name, locale);
    }

    /**
     * Select a bundle based on the catalogue base location, bundle name,
     * and the locale name.
     *
     * @param base    catalogue base location (URI)
     * @param name    bundle name
     * @param locale  locale name
     * @return        the bundle
     * @exception     ServiceException if a bundle is not found
     */
    public Bundle select(String directory, String name, String localeName) throws ServiceException {
        return select(directory, name, new Locale(localeName, localeName));
    }

    /**
     * Select a bundle based on the catalogue base location, bundle name,
     * and the locale.
     *
     * @param base    catalogue base location (URI)
     * @param name    bundle name
     * @param locale  locale
     * @return        the bundle
     * @exception     ServiceException if a bundle is not found
     */
    public Bundle select(String directory, String name, Locale locale) throws ServiceException {
        Bundle bundle = _select(directory, name, locale, this.cacheAtStartup);
        if (bundle == null) {
            throw new ServiceException(name, "Unable to locate resource: " + name);
        }
        return bundle;
    }

    /**
     * Select the parent bundle of the current bundle, based on
     * bundle name and locale.
     *
     * @param name              bundle name
     * @param locale            locale
     * @return                  the bundle
     */
    protected Object selectParent(String name, Locale locale) {
        return _select(getDirectory(), name, getParentLocale(locale), this.cacheAtStartup);
    }

    /**
     * Select a bundle based on bundle name and locale.
     *
     * @param base              catalogue location
     * @param name              bundle name
     * @param locale            locale
     * @param cacheAtStartup    cache all the keys when constructing?
     * @return                  the bundle
     */
    private Bundle _select(String base, String name, Locale locale, boolean cacheAtStartup) {
        String fileName = getFileName(base, name, locale);
        XMLResourceBundle bundle = (XMLResourceBundle)selectCached(fileName);
        if (bundle == null && !isNotFoundBundle(fileName)) {
            synchronized (this) {
                bundle = (XMLResourceBundle)selectCached(fileName);
                if (bundle == null && !isNotFoundBundle(fileName)) {
                    bundle = _loadBundle(name, fileName, locale, cacheAtStartup);

                    while (bundle == null && locale != null && !locale.getLanguage().equals("")) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Bundle '" + fileName + "' not found; trying parent");
                        }
                        locale = getParentLocale(locale);
                        String parentFileName = getFileName(base, name, locale);
                        bundle = _loadBundle(name, parentFileName, locale, cacheAtStartup);
                    }
                    
                    updateCache(fileName, bundle);
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
     * @param cacheAtStartup    cache all the keys when constructing?
     * @return                  the bundle, null if loading failed
     */
    private XMLResourceBundle _loadBundle(String name, String fileName,
                                          Locale locale, boolean cacheAtStartup) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Loading bundle: " + name + ", locale: " + locale +
                              ", uri: " + fileName);
        }

        XMLResourceBundle bundle = null;
        XMLResourceBundle parentBundle = null;
        try {
            if (locale != null && !locale.getLanguage().equals("")) {
                parentBundle = (XMLResourceBundle)selectParent(name, locale);
            }
            bundle = new XMLResourceBundle();
            bundle.enableLogging(logger);
            bundle.service(this.manager);
            bundle.init(name, fileName, locale, parentBundle, cacheAtStartup);
            return bundle;
        } catch (FileNotFoundException fe) {
            getLogger().info("Resource not found: " + name + ", locale: " + locale +
                             ", bundleName: " + fileName + ". Exception: " + fe.getMessage());
        } catch (SourceNotFoundException e) {
            getLogger().info("Resource not found: " + name + ", locale: " + locale +
                             ", bundleName: " + fileName + ". Exception: " + e.getMessage());
        } catch (SAXParseException se) {
            getLogger().error("Incorrect resource format", se);
        } catch (Exception e) {
            getLogger().error("Resource loading failed", e);
        }

        return null;
    }

    public void release(Object component) {
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

    /**
     * Maps a bundle name and locale to a full path in the filesystem.
     * If you need a different mapping, then just override this method.
     *
     * @param locale               the locale
     * @return                  the parent locale
     */
    protected String getFileName(String base, String name, Locale locale) {
        if (base == null) {
            base = "";
        }
        
        StringBuffer sb = new StringBuffer(base);
        if (!base.endsWith("/")) {
            sb.append('/').append(name);
        }
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
     * @param fileName          file name of the bundle
     * @return                  the cached bundle; null, if not found
     */
    protected Object selectCached(String fileName) {
        Object bundle = null;
        try {
            bundle = select(fileName);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Returning from cache: " + fileName);
            }
        } catch (ServiceException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Not found in cache: " + fileName);
            }
        }
        return bundle;
    }

    /**
     * Checks if the bundle is in the &quot;not-found&quot; cache.
     *
     * @param fileName          file name of the bundle
     * @return                  true, if the bundle wasn't found already before;
     *                          otherwise, false.
     */
    protected boolean isNotFoundBundle(String fileName) {
        String result = (String) (cacheNotFound.get(fileName));
        if (result != null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Returning from not_found_cache: " + fileName);
            }
        } else {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Not found in not_found_cache: " + fileName);
            }
        }
        return result != null;
    }

    /**
     * Checks if the bundle is in the &quot;not-found&quot; cache.
     *
     * @param fileName          file name of the bundle
     */
    protected void updateCache(String fileName, XMLResourceBundle bundle) {
        if (bundle == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Updating not_found_cache: " + fileName);
            }
            cacheNotFound.put(fileName, fileName);
        } else {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Updating cache: " + fileName);
            }
            super.put(fileName, bundle);
        }
    }
}
