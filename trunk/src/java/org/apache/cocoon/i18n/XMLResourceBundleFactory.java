/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.i18n;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.DefaultComponentSelector;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
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
 * @version CVS $Id: XMLResourceBundleFactory.java,v 1.6 2003/12/06 21:22:09 cziegeler Exp $
 */
public class XMLResourceBundleFactory extends DefaultComponentSelector
        implements BundleFactory, Composable, Configurable, Disposable, ThreadSafe, LogEnabled {

    /** Should we load bundles to cache on startup or not? */
    protected boolean cacheAtStartup = false;

    /** Root directory to all bundle names */
    protected String directory;

    /** Cache for the names of the bundles that were not found */
    protected Map cacheNotFound = new HashMap();

    /** The logger */
    private Logger logger;

    /** Component Manager */
    protected ComponentManager manager = null;


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

    public void compose(ComponentManager manager) {
        this.manager = manager;
    }

    public void dispose() {
        Iterator i = getComponentMap().values().iterator();
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
     * @exception     ComponentException if a bundle is not found
     */
    public Component select(String name, String locale) throws ComponentException {
        return select(getDirectory(), name, locale);
    }

    /**
     * Select a bundle based on the bundle name and the locale.
     *
     * @param name    bundle name
     * @param locale  locale
     * @return        the bundle
     * @exception     ComponentException if a bundle is not found
     */
    public Component select(String name, Locale locale) throws ComponentException {
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
     * @exception     ComponentException if a bundle is not found
     */
    public Component select(String directory, String name, String localeName) throws ComponentException {
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
     * @exception     ComponentException if a bundle is not found
     */
    public Component select(String directory, String name, Locale locale) throws ComponentException {
        Component bundle = _select(directory, name, locale, this.cacheAtStartup);
        if (bundle == null) {
            throw new ComponentException(name, "Unable to locate resource: " + name);
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
    protected Component selectParent(String name, Locale locale) {
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
    private Component _select(String base, String name, Locale locale, boolean cacheAtStartup) {
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
            bundle.compose(this.manager);
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

    public void release(Component component) {
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
    protected Component selectCached(String fileName) {
        Component bundle = null;
        try {
            bundle = super.select(fileName);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Returning from cache: " + fileName);
            }
        } catch (ComponentException e) {
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
