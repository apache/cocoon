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
package org.apache.cocoon.generation;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>Cocoon {@link Generator} that produces dynamic XML SAX events
 * from a Velocity template file.</p>
 *
 * <h2>Sitemap Configuration</h2>
 *
 * <p>
 * Attributes:
 * <dl>
 * <dt>usecache (optional; default: 'false')</dt>
 * <dd>set to 'true' to enable template caching on the 'cocoon'
 * resource loader</dd>
 *
 * <dt>checkInterval (optional; default: '0')</dt>
 * <dd>This is the number of seconds between modification checks when
 * caching is turned on.  When this is an integer &gt; 0, this represents
 * the number of seconds between checks to see if the template was
 * modified. If the template has been modified since last check, then
 * it is reloaded and reparsed. Otherwise nothing is done. When &lt;= 0,
 * no modification checks will take place, and assuming that the
 * property cache (above) is true, once a template is loaded and
 * parsed the first time it is used, it will not be checked or
 * reloaded after that until the application or servlet engine is
 * restarted.</dd>
 * </dl>
 * </p>
 *
 * <p>
 * Child Elements:
 *
 * <dl>
 * <dt>&lt;property key="propertyKey" value="propertyValue"/&gt; (optional; 0..n)</dt>
 * <dd>An additional property to pass along to the Velocity template
 * engine during initialization</dd>
 *
 * <dt>&lt;resource-loader name="loaderName" class="javaClassName" &gt; (optional; 0..n; children: property*)</dt>
 * <dd>The default configuration uses the 'cocoon' resource loader
 * which resolves resources via the Cocoon SourceResolver. Additional
 * resource loaders can be added with this configuration
 * element. Configuration properties for the resource loader can be
 * specified by adding a child property element of the resource-loader
 * element. The prefix '&lt;name&gt;.resource.loader.' is
 * automatically added to the property name.</dd>
 *
 * <dt>&lt;export-object key="objectMapKey" name="velocityContextName" [scope=request/session/sitemap]/&gt; (optional; 0..n)</dt>
 * <dd>Export the object specified by <em>key</em> from the <em>request, session, sitemap</em> to
 * the Velocity context of the template.  The object can be accessed
 * from the template as <em>name</em>.
 * Be careful to use a valid VTL variable name.</dd>
 * </dl>
 * </p>
 *
 * <p>
 * Default Java objects exported to the Velocity context:
 *
 * <dl>
 * <dt>request (org.apache.cocoon.environment.Request)</dt>
 * <dd>The Cocoon current request</dd>
 *
 * <dt>template (java.lang.String)</dt>
 * <dd>The path of the template file currently being evaluated</dd>
 *
 * <dt>response (org.apache.cocoon.environment.Response)</dt>
 * <dd>The Cocoon response associated with the current request</dd>
 *
 * <dt>context (org.apache.cocoon.environment.Context)</dt>
 * <dd>The Cocoon context associated with the current request</dd>
 *
 * <dt>parameters (org.apache.avalon.framework.parameters.Parameters)</dt>
 * <dd>Any parameters passed to the generator in the pipeline</dd>
 * </dl>
 * </p>
 *
 * <p> Additional Java objects can be exported from the Cocoon object
 * map to the Velocity context by adding one or more &lt;export-object
 * key="objectMapKey" name="velocityContextName"/&gt; child elements
 * to the generator configuration in the sitemap.
 * Additionally a scope=[request, session, sitemap] can be set in which
 * lookup of the object to export is performed.</p>
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @author <a href="mailto:mike@hihat.net">Michael McKibben</a>
 * @author <a href="mailto:michael.homeijer@ordina.nl">Michael Homeijer</a>
 * @version CVS $Id: VelocityGenerator.java,v 1.6 2003/07/03 11:36:10 cziegeler Exp $
 */
public class VelocityGenerator extends ComposerGenerator
  implements Initializable, Configurable, LogSystem {

    /**
     * Velocity {@link org.apache.velocity.runtime.resource.loader.ResourceLoader}
     * implementation to load template resources using Cocoon's
     *{@link SourceResolver}. This class is created by the Velocity
     * framework via the ResourceLoaderFactory.
     *
     * @see org.apache.velocity.runtime.resource.loader.ResourceLoader
     */
    public static class TemplateLoader
      extends org.apache.velocity.runtime.resource.loader.ResourceLoader {

        private Context resolverContext;

        /**
         * Initialize this resource loader. The 'context' property is
         * required and must be of type {@link Context}. The context
         * is used to pass the Cocoon SourceResolver for the current
         * pipeline.
         *
         * @param config the properties to configure this resource.
         *         of type {@link Context}.
         * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#init
         */
        public void init(ExtendedProperties config) {
            this.resolverContext = (Context) config.get("context");
            if (this.resolverContext==null) {
                throw new IllegalArgumentException("Runtime Cocoon resolver context not specified in resource loader configuration.");
            }
        }

        /**
         * @param systemId the path to the resource
         * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getResourceStream
         *
         */
        public InputStream getResourceStream(String systemId)
          throws org.apache.velocity.exception.ResourceNotFoundException {
            try {
                return resolveSource(systemId).getInputStream();
            } catch (org.apache.velocity.exception.ResourceNotFoundException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new org.apache.velocity.exception.ResourceNotFoundException("Unable to resolve source: "+
                    ex);
            }
        }

        /**
         * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#isSourceModified
         *
         * @param resource   
         *
         */
        public boolean isSourceModified(org.apache.velocity.runtime.resource.Resource resource) {
            long lastModified = 0;

            try {
                lastModified = resolveSource(resource.getName()).getLastModified();
            } catch (Exception ex) {
                super.rsvc.warn("Unable to determine last modified for resource: "+
                                resource.getName()+": "+ex);
            }

            return (lastModified>0)
                   ? lastModified!=resource.getLastModified() : true;
        }

        /**
         * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getLastModified
         *
         * @param resource   
         *
         */
        public long getLastModified(org.apache.velocity.runtime.resource.Resource resource) {
            long lastModified = 0;

            try {
                lastModified = resolveSource(resource.getName()).getLastModified();
            } catch (Exception ex) {
                super.rsvc.warn("Unable to determine last modified for resource: "+
                                resource.getName()+": "+ex);
            }

            return lastModified;
        }

        /**
         * Store all the Source objects we lookup via the SourceResolver so that they can be properly
         * recycled later.
         *
         * @param systemId the path to the resource
         *
         */
        private Source resolveSource(String systemId)
          throws org.apache.velocity.exception.ResourceNotFoundException {
            Map sourceCache;

            try {
                sourceCache = (Map) this.resolverContext.get(CONTEXT_SOURCE_CACHE_KEY);
            } catch (ContextException ignore) {
                throw new org.apache.velocity.exception.ResourceNotFoundException("Runtime Cocoon source cache not specified in resource loader resolver context.");
            }

            Source source = (Source) sourceCache.get(systemId);

            if (source==null) {
                try {
                    SourceResolver resolver = (SourceResolver) this.resolverContext.get(CONTEXT_RESOLVER_KEY);

                    source = resolver.resolveURI(systemId);
                } catch (ContextException ex) {
                    throw new org.apache.velocity.exception.ResourceNotFoundException("No Cocoon source resolver associated with current request.");
                } catch (Exception ex) {
                    throw new org.apache.velocity.exception.ResourceNotFoundException("Unable to resolve source: "+
                        ex);
                }
            }

            sourceCache.put(systemId, source);
            return source;
        }
    }

    /**
     * Holder object for controlling Cocoon objects exported to the
     * Velocity Context.
     */
    private static class ObjectExport {
        private String key;
        private String sourcemap;
        private String name;
    }

    /**
     * Key to lookup the {@link SourceResolver} from the context of
     * the resource loader.
     */
    final private static String CONTEXT_RESOLVER_KEY = "resolver";

    /**
     * Key to lookup the source cache {@link Map} from the context of
     * the resource loader.
     */
    final private static String CONTEXT_SOURCE_CACHE_KEY = "source-cache";

    private static final String VALUE_SITEMAP = "sitemap";
    private static final String VALUE_SESSION = "session";
    private static final String VALUE_REQUEST = "request";

    private List objectExports;
    private VelocityEngine tmplEngine;
    private DefaultContext resolverContext;
    private VelocityContext velocityContext;
    private boolean activeFlag;

    /**
     * Read any additional objects to export to the Velocity context
     * from the configuration.
     *
     * @param configuration the class configurations.
     * @see org.apache.avalon.framework.configuration.Configurable#configure
     */
    public void configure(Configuration configuration)
      throws ConfigurationException {
        this.resolverContext = new DefaultContext();
        this.objectExports = new ArrayList();
        this.tmplEngine = new VelocityEngine();
        this.tmplEngine.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, this);

        // First set up our default 'cocoon' resource loader
        this.tmplEngine.setProperty("cocoon.resource.loader.class",
                                    TemplateLoader.class.getName());
        this.tmplEngine.setProperty("cocoon.resource.loader.cache",
                                    configuration.getAttribute("usecache",
                                        "false"));
        this.tmplEngine.setProperty("cocoon.resource.loader.modificationCheckInterval",
                                    configuration.getAttribute("checkInterval",
                                        "0"));
        this.tmplEngine.setProperty("cocoon.resource.loader.context",
                                    this.resolverContext);

        // Read in any additional properties to pass to the VelocityEngine during initialization
        Configuration[] properties = configuration.getChildren("property");

        for (int i = 0; i<properties.length; ++i) {
            Configuration c = properties[i];
            String name = c.getAttribute("name");

            // Disallow setting of certain properties
            if (name.startsWith("runtime.log") ||
                (name.indexOf(".resource.loader.")!=-1)) {
                if (getLogger().isInfoEnabled()) {
                    getLogger().info("ignoring disallowed property '"+name+
                                     "'.");
                }
                continue;
            }
            this.tmplEngine.setProperty(name, c.getAttribute("value"));
        }

        // Now read in any additional Velocity resource loaders
        List resourceLoaders = new ArrayList();
        Configuration[] loaders = configuration.getChildren("resource-loader");

        for (int i = 0; i<loaders.length; ++i) {
            Configuration loader = loaders[i];
            String name = loader.getAttribute("name");

            if (name.equals("cocoon")) {
                if (getLogger().isInfoEnabled()) {
                    getLogger().info("'cocoon' resource loader already defined.");
                }
                continue;
            }
            resourceLoaders.add(name);
            String prefix = name+".resource.loader.";
            String type = loader.getAttribute("class");

            this.tmplEngine.setProperty(prefix+"class", type);
            Configuration[] loaderProperties = loader.getChildren("property");

            for (int j = 0; j<loaderProperties.length; j++) {
                Configuration c = loaderProperties[j];
                String propName = c.getAttribute("name");

                this.tmplEngine.setProperty(prefix+propName,
                                            c.getAttribute("value"));
            }
        }

        // Velocity expects resource loaders as CSV list
        StringBuffer buffer = new StringBuffer("cocoon");

        for (Iterator it = resourceLoaders.iterator(); it.hasNext(); ) {
            buffer.append(',');
            buffer.append((String) it.next());
        }
        tmplEngine.setProperty(Velocity.RESOURCE_LOADER, buffer.toString());

        // Read in additional objects to export from the object map
        Configuration[] exports = configuration.getChildren("export-object");

        for (int i = 0; i<exports.length; ++i) {
            Configuration c = exports[i];
            ObjectExport export = new ObjectExport();

            export.key = c.getAttribute("key");
            export.name = c.getAttribute("name");
            export.sourcemap = c.getAttribute("scope");
            this.objectExports.add(export);
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize
     */
    public void initialize() throws Exception {
        this.tmplEngine.init();
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup
     */
    public void setup(SourceResolver resolver, Map objectModel, String src,
                      Parameters params)
                        throws ProcessingException, SAXException,
                               IOException {
        if (activeFlag) {
            throw new IllegalStateException("setup called on recyclable sitemap component before properly recycling previous state");
        }

        super.setup(resolver, objectModel, src, params);

        // Pass along the SourceResolver to the Velocity resource loader
        this.resolverContext.put(CONTEXT_RESOLVER_KEY, resolver);
        this.resolverContext.put(CONTEXT_SOURCE_CACHE_KEY, new HashMap());

        // Initialize the Velocity context
        this.velocityContext = new VelocityContext();
        this.velocityContext.put("template", src);
        this.velocityContext.put("request",
                                 ObjectModelHelper.getRequest(objectModel));
        this.velocityContext.put("response",
                                 ObjectModelHelper.getResponse(objectModel));
        this.velocityContext.put("context",
                                 ObjectModelHelper.getContext(objectModel));
        this.velocityContext.put("parameters", params);

        // Export any additional objects to the Velocity context
        for (Iterator it = this.objectExports.iterator(); it.hasNext(); ) {
            ObjectExport export = (ObjectExport) it.next();
            // Object object = objectModel.get(export.key);
            Object object = this.searchBean(objectModel, export.key,
                                            export.sourcemap);

            if (object!=null) {
                this.velocityContext.put(export.name, object);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("exporting object under key '"+
                                      export.key+
                                      "' to velocity context with name '"+
                                      export.name+"'.");
                }
            } else if (getLogger().isInfoEnabled()) {
                getLogger().info("unable to export object under key '"+
                                 export.key+"' to velocity context.");
            }
        }

        this.activeFlag = true;
    }

    /**
     * Free up the VelocityContext associated with the pipeline, and
     * release any Source objects resolved by the resource loader.
     *
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle
     */
    public void recycle() {
        this.activeFlag = false;

        // Recycle all the Source objects resolved/used by our resource loader
        try {
            Map sourceCache = (Map) this.resolverContext.get(CONTEXT_SOURCE_CACHE_KEY);

            for (Iterator it = sourceCache.values().iterator(); it.hasNext(); ) {
                this.resolver.release((Source) it.next());
            }
        } catch (ContextException ignore) {}

        this.velocityContext = null;
        super.recycle();
    }

    /**
     * Generate XML data using Velocity template.
     *
     * @see org.apache.cocoon.generation.Generator#generate
     */
    public void generate()
      throws IOException, SAXException, ProcessingException {
        // Guard against calling generate before setup.
        if ( !activeFlag) {
            throw new IllegalStateException("generate called on sitemap component before setup.");
        }

        SAXParser parser = null;

        try {
            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Processing File: "+super.source);
            }

            /* lets render a template */

            StringWriter w = new StringWriter();

            this.tmplEngine.mergeTemplate(super.source, velocityContext, w);

            InputSource xmlInput = new InputSource(new StringReader(w.toString()));

            parser.parse(xmlInput, this.xmlConsumer);
        } catch (IOException e) {
            getLogger().warn("VelocityGenerator.generate()", e);
            throw new ResourceNotFoundException("Could not get Resource for VelocityGenerator",
                                                e);
        } catch (SAXException e) {
            getLogger().error("VelocityGenerator.generate()", e);
            throw e;
        } catch (ComponentException e) {
            getLogger().error("Could not get parser", e);
            throw new ProcessingException("Exception in VelocityGenerator.generate()",
                                          e);
        } catch (ProcessingException e) {
            throw e;
        } catch (Exception e) {
            getLogger().error("Could not get parser", e);
            throw new ProcessingException("Exception in VelocityGenerator.generate()",
                                          e);
        } finally {
            this.manager.release((Component) parser);
        }
    }

    /**
     * This implementation does nothing.
     *
     * @see org.apache.velocity.runtime.log.LogSystem#init
     *
     * @param rs         
     */
    public void init(RuntimeServices rs) throws Exception {
    }

    private Object searchBean(Map objectModel, String name,
                              String sourcemap) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Object bean;

        // search all maps for the given bean
        if ((sourcemap==null) || VALUE_SITEMAP.equals(sourcemap)) {
            // System.out.println("Searching bean " + name+ " in "+VALUE_SITEMAP);
            bean = objectModel.get(name);

            if (bean!=null) {
                return bean;
            }
        }

        if ((sourcemap==null) || VALUE_REQUEST.equals(sourcemap)) {
            // System.out.println("Searching bean " + name+ " in "+ VALUE_REQUEST);
            bean = request.getAttribute(name);

            if (bean!=null) {
                return bean;
            }
        }

        if ((sourcemap==null) || VALUE_SESSION.equals(sourcemap)) {
            // System.out.println("Searching bean " + name+ " in "+VALUE_SESSION);
            Session session = request.getSession(false);

            if (session!=null) {
                bean = session.getAttribute(name);

                if (bean!=null) {
                    return bean;
                }
            }
        }

        return null;
    }

    /**
     * Pass along Velocity log messages to our configured logger.
     *
     * @see org.apache.velocity.runtime.log.LogSystem#logVelocityMessage
     *
     * @param level      
     * @param message    
     */
    public void logVelocityMessage(int level, String message) {
        switch (level) {
            case LogSystem.WARN_ID :
                getLogger().warn(message);
                break;

            case LogSystem.INFO_ID :
                getLogger().info(message);
                break;

            case LogSystem.DEBUG_ID :
                getLogger().debug(message);
                break;

            case LogSystem.ERROR_ID :
                getLogger().error(message);
                break;

            default :
                getLogger().info(message);
                break;
        }
    }
}
