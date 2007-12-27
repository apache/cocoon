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
package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.w3c.dom.Document;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**

 <grammar>
    <define name="input.module.config.contents" combine="choice">
       <optional><element name="reloadable"><data type="boolean"/></element></optional>
       <optional><element name="cacheable"><data type="boolean"/></element></optional>
       <optional>
          <ref name="org.apache.cocoon.components.modules.input.XMLFileModule:file">
       </optional>
    </define>

    <define name="input.module.runtime.contents" combine="choice">
       <optional>
          <ref name="org.apache.cocoon.components.modules.input.XMLFileModule:file">
       </optional>
    </define>

    <define name="org.apache.cocoon.components.modules.input.XMLFileModule:file">
       <element name="file">
          <attribute name="src"><data type="anyURI"/></attribute>
          <optional><attribute name="reloadable"><data type="boolean"/></attribute></optional>
          <optional><attribute name="cacheable"><data type="boolean"/></attribute></optional>
       </element>
    </define>
 </grammar>

 * This module provides an Input Module interface to any XML document, by using
 * XPath expressions as attribute keys.
 * The XML can be obtained from any Cocoon <code>Source</code> (e.g.,
 * <code>cocoon:/...</code>, <code>context://..</code>, and regular URLs).
 * Sources can be held in memory for better performance and reloaded if
 * changed.
 *
 * <p>Caching and reloading can be turned on / off (default: caching on,
 * reloading off) through <code>&lt;reloadable&gt;false&lt;/reloadable&gt;</code>
 * and <code>&lt;cacheable&gt;false&lt;/cacheable&gt;</code>. The file
 * (source) to use is specified through <code>&lt;file
 * src="protocol:path/to/file.xml" reloadable="true"
 * cacheable="true"/&gt;</code> optionally overriding defaults for
 * caching and/or reloading.</p>
 *
 * <p>In addition, xpath expressions are cached for higher performance.
 * Thus, if an expression has been evaluated for a file, the result
 * is cached and will be reused, the expression is not evaluated
 * a second time. This can be turned off using the <code>cache-expressions</code>
 * configuration option.</p>
 *
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version $Id$
 */
public class XMLFileModule extends AbstractJXPathModule
                           implements Composable, ThreadSafe {

    /** Static (cocoon.xconf) configuration location, for error reporting */
    String staticConfLocation;

    /** Cached documents */
    Map documents;

    /** Default value for reloadability of sources. Defaults to false. */
    boolean reloadAll;

    /** Default value for cacheability of sources. Defaults to true. */
    boolean cacheAll;

    /** Default value for cacheability of xpath expressions. Defaults to true. */
    boolean cacheExpressions;

    /** Default src */
    String src;

    SourceResolver resolver;
    ComponentManager manager;

    //
    // need two caches for Object and Object[]
    //

    /** XPath expression cache for single attribute values. */
    private Map expressionCache;

    /** XPath expression cache for multiple attribute values. */
    private Map expressionValuesCache;


    /**
     * Takes care of (re-)loading and caching of sources.
     */
    protected static class DocumentHelper {
        private boolean reloadable;
        private boolean cacheable;

        /** Source location */
        private String uri;

        /** Source validity */
        private SourceValidity validity;

        /** Source content cached as DOM Document */
        private Document document;

        /** Remember who created us (and who's caching us) */
        private XMLFileModule instance;

        /**
         * Creates a new <code>DocumentHelper</code> instance.
         *
         * @param reload a <code>boolean</code> value, whether this source should be reloaded if changed.
         * @param cache a <code>boolean</code> value, whether this source should be kept in memory.
         * @param src a <code>String</code> value containing the URI
         */
        public DocumentHelper(boolean reload, boolean cache, String src, XMLFileModule instance) {
            this.reloadable = reload;
            this.cacheable = cache;
            this.uri = src;
            this.instance = instance;
            // defer loading of the document
        }

        /**
         * Returns the Document belonging to the configured
         * source. Transparently handles reloading and caching.
         *
         * @param manager a <code>ComponentManager</code> value
         * @param resolver a <code>SourceResolver</code> value
         * @return a <code>Document</code> value
         * @exception Exception if an error occurs
         */
        public synchronized Document getDocument(ComponentManager manager, SourceResolver resolver, Logger logger)
        throws Exception {
            Source src = null;
            Document dom = null;
            try {
                if (this.document == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Document not cached... Loading uri " + this.uri);
                    }
                    src = resolver.resolveURI(this.uri);
                    this.validity = src.getValidity();
                    this.document = SourceUtil.toDOM(src);
                } else if (this.reloadable) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Document cached... checking validity of uri " + this.uri);
                    }

                    int valid = this.validity == null? SourceValidity.INVALID : this.validity.isValid();
                    if (valid != SourceValidity.VALID) {
                        // Get new source and validity
                        src = resolver.resolveURI(this.uri);
                        SourceValidity newValidity = src.getValidity();
                        // If already invalid, or invalid after validities comparison, reload
                        if (valid == SourceValidity.INVALID || this.validity.isValid(newValidity) != SourceValidity.VALID) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Reloading document... uri " + this.uri);
                            }
                            this.validity = newValidity;
                            this.document = SourceUtil.toDOM(src);

                            /*
                             * Clear the cache, otherwise reloads won't do much.
                             *
                             * FIXME (pf): caches should be held in the DocumentHelper
                             *             instance itself, clearing global cache will
                             *             clear everything for each configured document.
                             *             (this is a quick fix, no time to do the whole)
                             */
                            this.instance.flushCache();
                        }
                    }
                }
                dom = this.document;
            } finally {
                if (src != null) {
                    resolver.release(src);
                }
                if (!this.cacheable) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Not caching document cached... uri " + this.uri);
                    }
                    this.validity = null;
                    this.document = null;
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Done with document... uri " + this.uri);
            }
            return dom;
        }
    }

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composable</code>.
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
        this.resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }

    /**
     * Static (cocoon.xconf) configuration.
     * Configuration is expected to be of the form:
     * &lt;...&gt;
     *   &lt;reloadable&gt;true|<b>false</b>&lt;/reloadable&gt;
     *   &lt;cacheable&gt;<b>true</b>|false&lt;/cacheable&gt;
     *   &lt;file src="<i>src1</i>" reloadable="true|<b>false</b>" cacheable="<b>true</b>|false"/&gt;
     *   &lt;file src="<i>src2</i>" reloadable="true|<b>false</b>" cacheable="<b>true</b>|false"/&gt;
     *   ...
     * &lt;/...&gt;
     *
     * Each &lt;file/&gt; element pre-loads an XML DOM for querying. Typically only one
     * &lt;file&gt; is specified, and its <i>src</i> is used as a default if not
     * overridden in the {@link #getContextObject(Configuration, Map)}
     *
     * @param config a <code>Configuration</code> value, as described above.
     * @exception ConfigurationException if an error occurs
     */
    public void configure(Configuration config)
    throws ConfigurationException {
        super.configure(config);
        this.staticConfLocation = config.getLocation();
        this.reloadAll = config.getChild("reloadable").getValueAsBoolean(false);

        if (config.getChild("cachable", false) != null) {
            throw new ConfigurationException("Bzzt! Wrong spelling at " +
                                             config.getChild("cachable").getLocation() +
                                             ": please use 'cacheable', not 'cachable'");
        }
        this.cacheAll = config.getChild("cacheable").getValueAsBoolean(true);

        this.documents = Collections.synchronizedMap(new ReferenceMap());
        Configuration[] files = config.getChildren("file");
        for (int i = 0; i < files.length; i++) {
            boolean reload = files[i].getAttributeAsBoolean("reloadable", this.reloadAll);
            boolean cache = files[i].getAttributeAsBoolean("cacheable", this.cacheAll);
            this.src = files[i].getAttribute("src");
            // by assigning the source uri to this.src the last one will be the default
            // OTOH caching / reload parameters can be specified in one central place
            // if multiple file tags are used.
            this.documents.put(this.src, new DocumentHelper(reload, cache, this.src, this));
        }

        // init caches
        this.cacheExpressions = config.getChild("cache-expressions").getValueAsBoolean(true);
        if (this.cacheExpressions) {
            this.expressionCache = new ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.SOFT);
            this.expressionValuesCache = new ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.SOFT);
        }
    }

    /**
     * Dispose this component
     */
    public void dispose() {
        super.dispose();
        if (this.manager != null) {
            this.manager.release((Component)this.resolver);
            this.resolver = null;
            this.manager = null;
        }

        this.documents = null;
        this.expressionCache = null;
        this.expressionValuesCache = null;
    }

    /**
     * Retrieve document helper
     */
    private DocumentHelper getDocumentHelper(Configuration modeConf)
    throws ConfigurationException {
        boolean hasDynamicConf = false; // whether we have a <file src="..."> dynamic configuration
        Configuration fileConf = null;  // the nested <file>, if any

        if (modeConf != null && modeConf.getChildren().length > 0) {
            fileConf = modeConf.getChild("file", false);
            if (fileConf == null) {
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("Missing 'file' child element at " + modeConf.getLocation());
                }
            } else {
                hasDynamicConf = true;
            }
        }

        String src = this.src;
        if (hasDynamicConf) {
            src = fileConf.getAttribute("src");
        }

        if (src == null) {
            throw new ConfigurationException(
                "No source specified"
                    + (modeConf != null ? ", either dynamically in " + modeConf.getLocation() + ", or " : "")
                    + " statically in "
                    + this.staticConfLocation);
        }
        if (!this.documents.containsKey(src)) {
            boolean reload = this.reloadAll;
            boolean cache = this.cacheAll;
            if (hasDynamicConf) {
                reload = fileConf.getAttributeAsBoolean("reloadable", reload);
                cache = fileConf.getAttributeAsBoolean("cacheable", cache);
                if (fileConf.getAttribute("cachable", null) != null) {
                    throw new ConfigurationException(
                        "Bzzt! Wrong spelling at "
                            + fileConf.getLocation()
                            + ": please use 'cacheable', not 'cachable'");
                }
            }
            this.documents.put(src, new DocumentHelper(reload, cache, src, this));
        }
        return (DocumentHelper)this.documents.get(src);
    }

    /**
     * Get the DOM object that JXPath will operate on when evaluating
     * attributes.  This DOM is loaded from a Source, specified in the
     * modeConf, or (if modeConf is null) from the
     * {@link #configure(Configuration)}.
     * @param modeConf The dynamic configuration for the current operation. May
     * be <code>null</code>, in which case static (cocoon.xconf) configuration
     * is used.  Configuration is expected to have a &lt;file> child node, and
     * be of the form:
     * &lt;...&gt;
     *   &lt;file src="..." reloadable="true|false"/&gt;
     * &lt;/...&gt;
     * @param objectModel Object Model for the current module operation.
     */
    protected Object getContextObject(Configuration modeConf, Map objectModel)
    throws ConfigurationException {
        DocumentHelper helper = this.getDocumentHelper(modeConf);

        try {
            return helper.getDocument(this.manager, this.resolver, this.getLogger());
        } catch (Exception e) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Error using source " + this.src + "\n" + e.getMessage(), e);
            }
            throw new ConfigurationException("Error using source " + this.src, e);
        }
    }

    public Object getAttribute(String name, Configuration modeConf, Map objectModel)
    throws ConfigurationException {
        return this.getAttribute(name, modeConf, objectModel, false);
    }

    public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel)
    throws ConfigurationException {
        Object result = this.getAttribute(name, modeConf, objectModel, true);
        return (result != null ? (Object[]) result : null);
    }

    private Object getAttribute(String name, Configuration modeConf, Map objectModel, boolean getValues)
    throws ConfigurationException {
        Object contextObj = this.getContextObject(modeConf, objectModel);
        if (modeConf != null) {
            name = modeConf.getChild("parameter").getValue(this.parameter != null ? this.parameter : name);
        }

        Object result = null;
        Map cache = null;
        boolean hasBeenCached = false;
        if (this.cacheExpressions) {
            cache = this.getExpressionCache(getValues? this.expressionValuesCache: this.expressionCache, contextObj);
            hasBeenCached = cache.containsKey(name);
            if (hasBeenCached) {
                result = cache.get(name);
            }
        }

        if (!hasBeenCached) {
            if (getValues){
                result = JXPathHelper.getAttributeValues(name, modeConf, this.configuration, contextObj);
            } else {
                result = JXPathHelper.getAttributeValue(name, modeConf, this.configuration, contextObj);
            }
            if (this.cacheExpressions) {
                cache.put(name, result);
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("for " + name + " newly caching result " + result);
                }
            } else {
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("for " + name + " result is " + result);
                }
            }
        } else {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("for " + name + " using cached result " + result);
            }
        }

        return result;
    }

    protected void flushCache() {
        if (this.cacheExpressions) {
            synchronized(this.expressionCache) {
                this.expressionCache.clear();
            }
            synchronized(this.expressionValuesCache) {
                this.expressionValuesCache.clear();
            }
        }
    }

    private Map getExpressionCache(Map cache, Object key) {
        synchronized (cache) {
            Map map = (Map) cache.get(key);
            if (map == null) {
                map = Collections.synchronizedMap(new HashMap());
                cache.put(key, map);
            }
            return map;
        }
    }
}
