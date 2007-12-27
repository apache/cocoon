package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.store.Store;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;

import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.ProcessingException;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.collections.map.AbstractReferenceMap;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.util.Map;
import java.net.MalformedURLException;
import java.io.IOException;

/**
 * <grammar>
 *   <define name="input.module.config.contents" combine="choice">
 *     <optional><element name="cacheable"><data type="boolean"/></element></optional>
 *     <optional><element name="reloadable"><data type="boolean"/></element></optional>
 *     <optional>
 *       <ref name="org.apache.cocoon.components.modules.input.XPathXMLFileModule:file">
 *     </optional>
 *     <optional><element name="cache-role"><data type="String"/></element></optional>
 *   </define>
 * <p/>
 *   <define name="input.module.runtime.contents" combine="choice">
 *     <optional>
 *       <ref name="org.apache.cocoon.components.modules.input.XPathXMLFileModule:file">
 *     </optional>
 *   </define>
 * <p/>
 *   <define name="org.apache.cocoon.components.modules.input.XPathXMLFileModule:file">
 *     <element name="file">
 *       <attribute name="src"><data type="anyURI"/></attribute>
 *       <optional><attribute name="cacheable"><data type="boolean"/></attribute></optional>
 *       <optional><attribute name="reloadable"><data type="boolean"/></attribute></optional>
 *     </element>
 *   </define>
 * </grammar>
 * <p/>
 * This module provides an Input Module interface to any XML document, by using
 * XPath expressions as attribute keys.
 * The XML can be obtained from any Cocoon <code>Source</code> (e.g.,
 * <code>cocoon:/...</code>, <code>context://..</code>, and regular URLs).
 * Sources can be cached in memory for better performance and reloaded if
 * changed. The source can also contain references to other input modules to allow the source
 * file name to be determined dynamically.
 * <p/>
 * Caching and reloading can be turned on / off (default: caching on,
 * reloading off) through <code>&lt;reloadable&gt;false&lt;/reloadable&gt;</code>
 * and <code>&lt;cacheable&gt;false&lt;/cacheable&gt;</code>. The file
 * (source) to use is specified through <code>&lt;file
 * src="protocol:path/to/file.xml" reloadable="true" cacheable="true"/&gt;</code>
 * optionally overriding the defaults for caching and/or reloading. When specfied as attributes
 * to the file element the values for cacheable and reloadable may be input module references which
 * will be resolved on every call. These must resolve to 'true' or 'false'.
 * </>
 * The XML documents will be cached using the Store configured via the cache-role configuration
 * element. If not specified the default Store as specified in this classes ROLE attribute will
 * be used.
 * <p/>
 * In addition, xpath expressions can be cached for higher performance.
 * Thus, if an expression has been evaluated for a file, the result
 * is cached and will be reused, the expression is not evaluated
 * a second time. This can be turned off using the <code>cache-expressions</code>
 * configuration option.
 *
 * @version $Id: $
 */
public class XPathXMLFileModule extends AbstractInputModule
    implements Serviceable, ThreadSafe
{
    public static final String ROLE = Store.ROLE + "/XPathXMLFileTransientStore";
    /**
     * Contains all globally registered extension classes and
     * packages. Thus the lookup and loading of globally registered
     * extensions is done only once.
     */
    protected JXPathHelperConfiguration configuration;

    /**
     * Static (cocoon.xconf) configuration location, for error reporting
     */
    String staticConfLocation;

    /**
     * Cached documents
     */
    private Store cache;

    /**
     * Determines whether the configured source document should be cached.
     */
    private String  cacheParm;
    private Boolean cacheSource;

    /**
     * Determines whether the configured source document should be reloaded.
     */
    private String  reloadParm;
    private Boolean reloadSource;

    /**
     * Default value for reloadability of sources. Defaults to false.
     */
    boolean reloadAll;
    /**
     * Default value for cacheability of xpath expressions. Defaults to true.
     */
    private boolean cacheExpressions;

    /**
     *  Whether the source needs to be resolved.
     */
    private boolean needsResolve;

    /**
     * Overrides attribute name
     */
    protected String parameter;

    /**
     * Default src
     */
    private String src;

    protected SourceResolver resolver;
    protected ServiceManager manager;


    /* (non-Javadoc)
    * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
    */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }

    /**
     * Static (cocoon.xconf) configuration.
     * Configuration is expected to be of the form:
     * &lt;...&gt;
     * &lt;reloadable&gt;<b>true</b>|false&lt;/reloadable&gt;
     * &lt;cacheable&gt;<b>true</b>|false&lt;/cacheable&gt;
     * &lt;cache-role&gt;org.apache.excalibur.store.Store/TransientStore&lt;/cache-role&gt;
     * &lt;file src="<i>src</i>"/&gt;
     * ...
     * &lt;/...&gt;
     * <p/>
     * The &lt;file/&gt; element specifies a file pattern. Only one
     * &lt;file&gt; can be specified, however it can contain references to input modules which will be resolved
     * each time the module is used. The configured <i>src</i> is used if not
     * overridden via a file parameter in the sitemap.
     *
     * @param config a <code>Configuration</code> value, as described above.
     * @throws org.apache.avalon.framework.configuration.ConfigurationException
     *          if an error occurs
     */
    public void configure(Configuration config) throws ConfigurationException {
        this.configuration = JXPathHelper.setup(config);
        this.staticConfLocation = config.getLocation();
        Configuration roleConfig = config.getChild("cache-role", true);
        boolean cacheAll = config.getChild("cacheable").getValueAsBoolean(true);
        this.reloadAll = config.getChild("reloadable").getValueAsBoolean(true);
        String cacheRole = roleConfig.getValue(ROLE);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Using cache " + cacheRole);
        }

        try {
            this.cache = (Store) this.manager.lookup(cacheRole);
        } catch (ServiceException ce) {
            throw new ConfigurationException("Unable to lookup cache: " + cacheRole, ce);
        }

        Configuration fileConfig = config.getChild("file");

        this.src = fileConfig.getAttribute("src");
        this.cacheParm = fileConfig.getAttribute("cacheable", null);
        this.reloadParm = fileConfig.getAttribute("reloadable", null);
        if (this.cacheParm == null) {
            this.cacheSource = Boolean.valueOf(cacheAll);
        } else if (VariableResolverFactory.needsResolve(this.cacheParm)) {
            this.cacheSource = null;
        } else {
            this.cacheSource = Boolean.valueOf(this.cacheParm);
        }
        if (this.reloadParm == null) {
            this.reloadSource = Boolean.valueOf(this.reloadAll);
        } else if (VariableResolverFactory.needsResolve(this.reloadParm)) {
            this.reloadSource = null;
        } else {
            this.reloadSource = Boolean.valueOf(this.reloadParm);
        }

        // init caches
        this.cacheExpressions = config.getChild("cache-expressions").getValueAsBoolean(true);
        this.needsResolve = VariableResolverFactory.needsResolve(this.src);
    }

    /**
     * Dispose this component
     */
    public void dispose() {
        super.dispose();
        if (this.manager != null) {
            this.manager.release(this.resolver);
            this.manager.release(this.cache);
            this.resolver = null;
            this.cache = null;
            this.manager = null;
        }
    }

    public Object getAttribute(String name, Configuration modeConf, Map objectModel)
        throws ConfigurationException {
        return getAttribute(name, modeConf, objectModel, false);
    }

    public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel)
        throws ConfigurationException {
        Object result = getAttribute(name, modeConf, objectModel, true);
        return (result != null ? (Object[]) result : null);
    }
    /**
     * Get the DocumentInfo for the DOM object that JXPath will operate on when evaluating
     * attributes.  This DOM is loaded from a Source, specified in the
     * modeConf, or (if modeConf is null) from the
     * {@link #configure(org.apache.avalon.framework.configuration.Configuration)}.
     *
     * @param name The JXPath to retrieve
     * @param modeConf    The dynamic configuration for the current operation. May
     *                    be <code>null</code>, in which case static (cocoon.xconf) configuration
     *                    is used.  Configuration is expected to have a &lt;file> child node, and
     *                    be of the form:
     *                    &lt;...&gt;
     *                    &lt;file src="..." reloadable="true|false"/&gt;
     *                    &lt;/...&gt;
     * @param objectModel Object Model for the current module operation.
     * @param getValues true if multiple values should be retrieve, false otherwise
     * @return the result of the XPath query into the XML document
     * @throws ConfigurationException if an error occurs.
     */
    private Object getAttribute(String name, Configuration modeConf, Map objectModel, boolean getValues)
        throws ConfigurationException {

        if (modeConf != null) {
            name = modeConf.getChild("parameter").getValue(this.parameter != null ? this.parameter : name);
        }

        boolean hasDynamicConf = false; // whether we have a <file src="..."> dynamic configuration
        Configuration fileConf = null;  // the nested <file>, if any

        if (modeConf != null && modeConf.getChildren().length > 0) {
            fileConf = modeConf.getChild("file", false);
            if (fileConf == null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Missing 'file' child element at " + modeConf.getLocation());
                }
            } else {
                hasDynamicConf = true;
            }
        }

        String src = this.src;
        Boolean cacheSource = this.cacheSource;
        Boolean reloadSource = this.cacheSource;
        boolean needsResolve = this.needsResolve;
        String cacheParm = this.cacheParm;
        String reloadParm = this.reloadParm;

        if (hasDynamicConf) {
            src = fileConf.getAttribute("src");
            cacheParm = fileConf.getAttribute("cacheable", this.cacheParm);
            reloadParm = fileConf.getAttribute("reloadable", this.reloadParm);
            if (cacheParm == null) {
                cacheSource = this.cacheSource;
            } else if (VariableResolverFactory.needsResolve(cacheParm)) {
                cacheSource = null;
                if (cacheSource == null) {
                    try {
                        VariableResolver varResolver = VariableResolverFactory.getResolver(cacheParm, this.manager);
                        cacheSource = Boolean.valueOf(varResolver.resolve(objectModel));
                    } catch (PatternException pe) {
                        throw new ConfigurationException("Error resolving " + cacheParm, pe);
                    }
                }
            } else {
                cacheSource = Boolean.valueOf(cacheParm);
            }
            if (reloadParm == null) {
                reloadSource = this.reloadSource;
            } else if (VariableResolverFactory.needsResolve(reloadParm)) {
                reloadSource = null;
            } else {
                reloadSource = Boolean.valueOf(reloadParm);
            }
            needsResolve = true;
        }
        if (cacheSource == null) {
            try {
                VariableResolver varResolver = VariableResolverFactory.getResolver(cacheParm, this.manager);
                cacheSource = Boolean.valueOf(varResolver.resolve(objectModel));
            } catch (PatternException pe) {
                throw new ConfigurationException("Error resolving " + cacheParm, pe);
            }
        }
        if (reloadSource == null) {
            try {
                VariableResolver varResolver =
                    VariableResolverFactory.getResolver(reloadParm, this.manager);
                reloadSource = Boolean.valueOf(varResolver.resolve(objectModel));
            } catch (PatternException pe) {
                throw new ConfigurationException("Error resolving " + reloadParm, pe);
            }
        }

        if (src == null) {
            throw new ConfigurationException(
                "No source specified"
                    + (modeConf != null ? ", either dynamically in " + modeConf.getLocation() + ", or " : "")
                    + " statically in "
                    + staticConfLocation);
        }

        if (needsResolve) {
            try {
                VariableResolver varResolver = VariableResolverFactory.getResolver(src, this.manager);
                src = varResolver.resolve(objectModel);
            } catch (PatternException pe) {
                throw new ConfigurationException("Error resolving variables for " + src, pe);
            }
        }

        Object result;

        if (cacheSource.booleanValue()) {
            DocumentInfo info = (DocumentInfo) this.cache.get(src);
            if (info == null || (reloadSource.booleanValue() && !info.isValid())) {
                Source docSource = null;
                try {
                    docSource = resolver.resolveURI(src);
                    DocumentInfo newInfo =  new DocumentInfo(src, SourceUtil.toDOM(docSource),
                        docSource.getValidity(), this.cacheExpressions, this.resolver);
                    synchronized(this.cache) {
                        DocumentInfo cachedInfo = (DocumentInfo)this.cache.get(src);
                        if (cachedInfo == null || cachedInfo == info) {
                            this.cache.store(src, newInfo);
                            info = newInfo;
                        } else {
                            info = cachedInfo;
                        }
                    }
                } catch (MalformedURLException mue) {
                    throw new ConfigurationException("Unable to resolve " + src, mue);
                } catch (IOException ioe) {
                    throw new ConfigurationException("Unable to access" + src, ioe);
                } catch (ProcessingException pe) {
                    throw new ConfigurationException("Unable to process " + src, pe);
                } catch (SAXException se) {
                    throw new ConfigurationException("Error processing XML document " + src, se);
                } finally {
                    if (docSource != null) {
                        resolver.release(docSource);
                    }
                }
            }
            if (info.cacheExpressions) {
                Map cache = getValues ? info.expressionValuesCache : info.expressionCache;
                synchronized (cache) {
                    if (cache.containsKey(name)) {
                        result = cache.get(name);
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("for " + name + " using cached result " + result);
                        }
                    } else {
                        result = getResult(name, info.document, modeConf, getValues);
                        if (result != null) {
                            cache.put(name, result);
                            if (getLogger().isDebugEnabled()) {
                                getLogger().debug("for " + name + " newly caching result " + result);
                            }
                        } else {
                            if (getLogger().isDebugEnabled()) {
                                getLogger().debug("for " + name + " result is null");
                            }
                        }
                    }
                }
            } else {
                result = getResult(name, info.document, modeConf, getValues);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("for " + name + " result is " + result);
                }
            }
        } else {
            Source docSource = null;
            try {
                docSource = resolver.resolveURI(src);
                result = getResult(name, SourceUtil.toDOM(docSource), modeConf, getValues);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("for " + name + " result is " + result);
                }
            } catch (MalformedURLException mue) {
                throw new ConfigurationException("Unable to resolve " + src, mue);
            } catch (IOException ioe) {
                throw new ConfigurationException("Unable to access" + src, ioe);
            } catch (ProcessingException pe) {
                throw new ConfigurationException("Unable to process " + src, pe);
            } catch (SAXException se) {
                throw new ConfigurationException("Error processing XML document " + src, se);
            } finally {
                if (docSource != null) {
                    resolver.release(docSource);
                }
            }
        }

        return result;
    }

    private Object getResult(String name, Document document, Configuration modeConf, boolean getValues)
        throws ConfigurationException {
        Object result;

        if (getValues) {
            result = JXPathHelper.getAttributeValues(name, modeConf, this.configuration, document);
        } else {
            result = JXPathHelper.getAttributeValue(name, modeConf, this.configuration, document);
        }
        return result;
    }

    /**
     * Used to keep track of the Document, its validity and any cached expressions.
     */
    private static class DocumentInfo
    {
        public DocumentInfo(String uri, Document doc, SourceValidity validity, boolean cacheExpressions,
                            SourceResolver resolver) {
            this.cacheExpressions = cacheExpressions;
            if (cacheExpressions) {
                expressionCache = new ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.SOFT);
                expressionValuesCache = new ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.SOFT);
            }
            this.resolver = resolver;
            this.uri = uri;
            this.document = doc;
            this.validity = validity;
        }

        private boolean cacheExpressions;

        private final String uri;

        private final SourceValidity validity;

        private final SourceResolver resolver;

        /**
         * Source content cached as DOM Document
         */
        private final Document document;

        private Map expressionCache;
        private Map expressionValuesCache;

        /**
         * Returns true if the document is valid, false otherwise.
         * <p/>
         *
         * @return returns true if the document is valid, false otherwise.
         */
        private boolean isValid() {
            Source src = null;
            boolean result = true;

            try {
                int valid = validity == null ? SourceValidity.INVALID : validity.isValid();
                if (valid == SourceValidity.UNKNOWN) {
                    // Get new source and validity
                    src = resolver.resolveURI(this.uri);
                    SourceValidity newValidity = src.getValidity();
                    valid = validity.isValid(newValidity);
                }
                if (valid != SourceValidity.VALID) {
                    result = false;
                }
            }
            catch (Exception ex) {
                result = false;
            }
            finally {
                if (src != null) {
                    resolver.release(src);
                }
            }
            return result;
        }
    }
}