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
package org.apache.cocoon.generation;

import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.UberspectImpl;
import org.apache.velocity.util.introspection.VelMethod;
import org.apache.velocity.util.introspection.VelPropertyGet;
import org.apache.velocity.util.introspection.VelPropertySet;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>Cocoon {@link Generator} that produces dynamic XML SAX events
 * from a Velocity template file.</p>
 * If called from a Flowscript, the immediate properties of the context object from the Flowscript are available in the Velocity context.
 * In that case, the current Web Continuation from the Flowscript 
 * is also available as a variable named <code>continuation</code>. You would 
 * typically access its <code>id</code>:
 * <p><pre>
 *    &lt;form action="$continuation.id"&gt;
 * </pre></p>
 * <p>You can also reach previous continuations by using the <code>getContinuation()</code> function:</p>
 * <p><pre>
 *     &lt;form action="$continuation.getContinuation(1).id}" >
 * </pre></p>
 * 
 * In addition the following implicit objects are always available in
 * the Velocity context:
 * <p>
 * <dl>
 * <dt><code>request</code> (<code>org.apache.cocoon.environment.Request</code>)</dt>
 * <dd>The Cocoon current request</dd>
 *
 * <dt><code>response</code> (<code>org.apache.cocoon.environment.Response</code>)</dt>
 * <dd>The Cocoon response associated with the current request</dd>
 *
 * <dt><code>session</code> (<code>org.apache.cocoon.environment.Session</code>)</dt>
 * <dd>The Cocoon session associated with the current request</dd>
 *
 * <dt><code>context</code> (<code>org.apache.cocoon.environment.Context</code>)</dt>
 * <dd>The Cocoon context associated with the current request</dd>
 *
 * <dt><code>parameters</code> (<code>org.apache.avalon.framework.parameters.Parameters</code>)</dt>
 * <dd>Any parameters passed to the generator in the pipeline</dd>
 * </dl>
 * </p>
 *
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
 * @version CVS $Id: VelocityGenerator.java,v 1.13 2004/03/05 13:02:25 bdelacretaz Exp $
 */
public class VelocityGenerator extends ServiceableGenerator
        implements Initializable, Configurable, LogSystem {

    /**
     * <p>Velocity context implementation specific to the Servlet environment.</p>
     *
     * <p>It provides the following special features:</p>
     * <ul>
     *   <li>puts the request, response, session, and servlet context objects
     *       into the Velocity context for direct access, and keeps them 
     *       read-only</li>
     *   <li>supports a read-only toolbox of view tools</li>
     *   <li>auto-searches servlet request attributes, session attributes and
     *       servlet context attribues for objects</li>
     * </ul>
     *
     * <p>The {@link #internalGet(String key)} method implements the following search order
     * for objects:</p>
     * <ol>
     *   <li>servlet request, servlet response, servlet session, servlet context</li>
     *   <li>toolbox</li>
     *   <li>local hashtable of objects (traditional use)</li>
     *   <li>servlet request attribues, servlet session attribute, servlet context
     *     attributes</li>
     * </ol> 
     *
     * <p>The purpose of this class is to make it easy for web designer to work 
     * with Java servlet based web applications. They do not need to be concerned 
     * with the concepts of request, session or application attributes and the 
     * live time of objects in these scopes.</p>
     *  
     * <p>Note that the put() method always puts objects into the local hashtable.
     * </p>
     *
     * <p>Acknowledge: the source code is borrowed from the jakarta-velocity-tools
     * project with slight modifications.</p>
     *
     * @author <a href="mailto:albert@charcoalgeneration.com">Albert Kwong</a>
     * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
     * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
     * @author <a href="mailto:albert@charcoalgeneration.com">Albert Kwong</a>
     */
    public static class ChainedContext extends VelocityContext
    {
        
        /**
         * A local reference to the current servlet request.
         */ 
        private Request request;
        
        /**
         * A local reference to the current servlet response.
         */
        private Response response;
        
        /**
         * A local reference to the servlet session.
         */
        private Session session;
        
        /**
         * A local reference to the servlet context.
         */
        private org.apache.cocoon.environment.Context application;
        
        /**
         * A local reference to pipeline parameters.
         */
        private Parameters parameters;
        
        /**
         * Key to the HTTP request object.
         */
        public static final String REQUEST = "request";
        
        /**
         * Key to the HTTP response object.
         */
        public static final String RESPONSE = "response";
        
        /**
         * Key to the HTTP session object.
         */
        public static final String SESSION = "session";
        
        /**
         * Key to the servlet context object.
         */
        public static final String APPLICATION = "context";
        
        /**
         * Key to the servlet context object.
         */
        public static final String PARAMETERS = "parameters";
        
        
        /**
         * Default constructor.
         */
        public ChainedContext(org.apache.velocity.context.Context ctx, 
                              Request request,
                              Response response,
                              org.apache.cocoon.environment.Context application,
                              Parameters parameters)
        {
            super(null, ctx);
            this.request = request;
            this.response = response;
            this.session = request.getSession(false);
            this.application = application;
            this.parameters = parameters;
        }
        
        
        /**
         * <p>Looks up and returns the object with the specified key.</p>
         * 
         * <p>See the class documentation for more details.</p>
         *
         * @param key the key of the object requested
         * 
         * @return the requested object or null if not found
         */
        public Object internalGet( String key )
        {
            // make the four scopes of the Apocalypse Read only
            if ( key.equals( REQUEST ))
                {
                    return request;
                }
            else if( key.equals(RESPONSE) )
                {
                    return response;
                }
            else if ( key.equals(SESSION) )
                {
                    return session;
                }
            else if ( key.equals(APPLICATION))
                {
                    return application;
                }
            else if ( key.equals(PARAMETERS))
                {
                    return parameters;
                }
            
            Object o = null;
            
            // try the local hashtable
            o = super.internalGet( key );
            
            // if not found, wander down the scopes...
            if (o == null)
                {
                    o = request.getAttribute( key );
                    
                    if ( o == null )
                        {
                            if ( session != null )
                                {
                                    o = session.getAttribute( key );
                                }
                            
                            if ( o == null )
                                {
                                    o = application.getAttribute( key );
                                }
                        }
                }
            
            return o;
        }

        
    }  // ChainedContext

    /**
     * Velocity Introspector that supports Rhino JavaScript objects
     * as well as Java Objects
     *
     */
    public static class JSIntrospector extends UberspectImpl {
        
        public static class JSMethod implements VelMethod {
            
            Scriptable scope;
            String name;
            
            public JSMethod(Scriptable scope, String name) {
                this.scope = scope;
                this.name = name;
            }
            
            public Object invoke(Object thisArg, Object[] args)
                throws Exception {
                org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.enter();
                try {
                    Object result; 
                    Scriptable thisObj;
                    if (!(thisArg instanceof Scriptable)) {
                        thisObj = org.mozilla.javascript.Context.toObject(thisArg, scope);
                    } else {
                        thisObj = (Scriptable)thisArg;
                    }
                    result = ScriptableObject.getProperty(thisObj, name);
                    Object[] newArgs = null;
                    if (args != null) {
                        newArgs = new Object[args.length];
                        for (int i = 0; i < args.length; i++) {
                            newArgs[i] = args[i];
                            if (args[i] != null && 
                                !(args[i] instanceof Number) &&
                                !(args[i] instanceof Boolean) &&
                                !(args[i] instanceof String) &&
                                !(args[i] instanceof Scriptable)) {
                                newArgs[i] = org.mozilla.javascript.Context.toObject(args[i], scope);
                            }
                        }
                    }
                    result = ScriptRuntime.call(cx, result, thisObj, 
                                                newArgs, scope);
                    if (result == Undefined.instance ||
                        result == Scriptable.NOT_FOUND) {
                        result = null;
                    } else while (result instanceof Wrapper) {
                        result = ((Wrapper)result).unwrap();
                    }
                    return result;
                } catch (JavaScriptException e) {
                    throw new java.lang.reflect.InvocationTargetException(e);
                } finally {
                    org.mozilla.javascript.Context.exit();
                }
            }
            
            public boolean isCacheable() {
                return false;
            }
            
            public String getMethodName() {
                return name;
            }
            
            public Class getReturnType() {
                return Object.class;
            }
            
        }
        
        public static class JSPropertyGet implements VelPropertyGet {
            
            Scriptable scope;
            String name;
            
            public JSPropertyGet(Scriptable scope, String name) {
                this.scope = scope;
                this.name = name;
            }
            
            public Object invoke(Object thisArg) throws Exception {
                org.mozilla.javascript.Context.enter();
                try {
                    Scriptable thisObj;
                    if (!(thisArg instanceof Scriptable)) {
                        thisObj = org.mozilla.javascript.Context.toObject(thisArg, scope);
                    } else {
                        thisObj = (Scriptable)thisArg;
                    }
                    Object result = ScriptableObject.getProperty(thisObj, name);
                    if (result == Undefined.instance || 
                        result == Scriptable.NOT_FOUND) {
                        result = null;
                    } else while (result instanceof Wrapper) {
                        result = ((Wrapper)result).unwrap();
                    }
                    return result;
                } finally {
                    org.mozilla.javascript.Context.exit();
                }
            }
            
            public boolean isCacheable() {
                return false;
            }
            
            public String getMethodName() {
                return name;
            }
            
        }
        
        public static class JSPropertySet implements VelPropertySet {
            
            Scriptable scope;
            String name;
            
            public JSPropertySet(Scriptable scope, String name) {
                this.scope = scope;
                this.name = name;
            }
            
            public Object invoke(Object thisArg, Object rhs) throws Exception {
                org.mozilla.javascript.Context.enter();
                try {
                    Scriptable thisObj;
                    Object arg = rhs;
                    if (!(thisArg instanceof Scriptable)) {
                        thisObj = org.mozilla.javascript.Context.toObject(thisArg, scope);
                    } else {
                        thisObj = (Scriptable)thisArg;
                    }
                    if (arg != null && 
                        !(arg instanceof Number) &&
                        !(arg instanceof Boolean) &&
                        !(arg instanceof String) &&
                        !(arg instanceof Scriptable)) {
                        arg = org.mozilla.javascript.Context.toObject(arg, scope);
                    }
                    ScriptableObject.putProperty(thisObj, name, arg);
                    return rhs;
                } finally {
                    org.mozilla.javascript.Context.exit();
                }
            }
            
            public boolean isCacheable() {
                return false;
            }
            
            public String getMethodName() {
                return name;        
            }
        }
        
        public static class NativeArrayIterator implements Iterator {
            
            NativeArray arr;
            int index;
            
            public NativeArrayIterator(NativeArray arr) {
                this.arr = arr;
                this.index = 0;
            }
            
            public boolean hasNext() {
                return index < (int)arr.jsGet_length();
            }
            
            public Object next() {
                org.mozilla.javascript.Context.enter();
                try {
                    Object result = arr.get(index++, arr);
                    if (result == Undefined.instance ||
                        result == Scriptable.NOT_FOUND) {
                        result = null;
                    } else while (result instanceof Wrapper) {
                        result = ((Wrapper)result).unwrap();
                    }
                    return result;
                } finally {
                    org.mozilla.javascript.Context.exit();
                }
            }
            
            public void remove() {
                arr.delete(index);
            }
        }
        
        public static class ScriptableIterator implements Iterator {
            
            Scriptable scope;
            Object[] ids;
            int index;
            
            public ScriptableIterator(Scriptable scope) {
                this.scope = scope;
                this.ids = scope.getIds();
                this.index = 0;
            }
            
            public boolean hasNext() {
                return index < ids.length;
            }
            
            public Object next() {
                org.mozilla.javascript.Context.enter();
                try {
                    Object result = 
                        ScriptableObject.getProperty(scope, 
                                                     ids[index++].toString());
                    if (result == Undefined.instance ||
                        result == Scriptable.NOT_FOUND) {
                        result = null;
                    } else while (result instanceof Wrapper) {
                        result = ((Wrapper)result).unwrap();
                    }
                    return result;
                } finally {
                    org.mozilla.javascript.Context.exit();
                }
            }
            
            public void remove() {
                org.mozilla.javascript.Context.enter();
                try {
                    scope.delete(ids[index].toString());
                } finally {
                    org.mozilla.javascript.Context.exit();
                }
            }
        }
        
        public Iterator getIterator(Object obj, Info i)
            throws Exception {
            if (!(obj instanceof Scriptable)) {
                return super.getIterator(obj, i);
            }
            if (obj instanceof NativeArray) {
                return new NativeArrayIterator((NativeArray)obj);
            }
            return new ScriptableIterator((Scriptable)obj);
        }
        
        public VelMethod getMethod(Object obj, String methodName, 
                                   Object[] args, Info i)
            throws Exception {
            if (!(obj instanceof Scriptable)) {
                return super.getMethod(obj, methodName, args, i);
            }
            return new JSMethod((Scriptable)obj, methodName);
        }
        
        public VelPropertyGet getPropertyGet(Object obj, String identifier, 
                                             Info i)
            throws Exception {
            if (!(obj instanceof Scriptable)) {
                return super.getPropertyGet(obj, identifier, i);
            }
            return new JSPropertyGet((Scriptable)obj, identifier);
        }
        
        public VelPropertySet getPropertySet(Object obj, String identifier, 
                                             Object arg, Info i)
            throws Exception {
            if (!(obj instanceof Scriptable)) {
                return super.getPropertySet(obj, identifier, arg, i);
            }
            return new JSPropertySet((Scriptable)obj, identifier);
        }
    }

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

        private org.apache.avalon.framework.context.Context resolverContext;

        /**
         * Initialize this resource loader. The 'context' property is
         * required and must be of type {@link Context}. The context
         * is used to pass the Cocoon SourceResolver for the current
         * pipeline.
         *
         * @param config the properties to configure this resource.
         * @throws IllegalArgumentException thrown if the required
         *         'context' property is not set.
         * @throws ClassCastException if the 'context' property is not
         *         of type {@link org.apache.avalon.framework.context.Context}.
         * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#init(ExtendedProperties)
         */
        public void init(ExtendedProperties config) {
            this.resolverContext = (org.apache.avalon.framework.context.Context) config.get("context");
            if (this.resolverContext == null) {
                throw new IllegalArgumentException("Runtime Cocoon resolver context not specified in resource loader configuration.");
            }
        }

        /**
         * @param systemId the path to the resource
         * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getResourceStream(String)
         */
        public InputStream getResourceStream(String systemId)
                throws org.apache.velocity.exception.ResourceNotFoundException {
            try {
                return resolveSource(systemId).getInputStream();
            } catch (org.apache.velocity.exception.ResourceNotFoundException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new org.apache.velocity.exception.ResourceNotFoundException("Unable to resolve source: " + ex);
            }
        }

        /**
         * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#isSourceModified(Resource)
         */
        public boolean isSourceModified(Resource resource) {
            long lastModified = 0;
            try {
                lastModified = resolveSource(resource.getName()).getLastModified();
            } catch (Exception ex) {
                super.rsvc.warn("Unable to determine last modified for resource: "
                                + resource.getName() + ": " + ex);
            }

            return lastModified > 0 ? lastModified != resource.getLastModified() : true;
        }

        /**
         * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getLastModified(Resource)
         */
        public long getLastModified(Resource resource) {
            long lastModified = 0;
            try {
                lastModified = resolveSource(resource.getName()).getLastModified();
            } catch (Exception ex) {
                super.rsvc.warn("Unable to determine last modified for resource: "
                                + resource.getName() + ": " + ex);
            }

            return lastModified;
        }

        /**
         * Store all the Source objects we lookup via the SourceResolver so that they can be properly
         * recycled later.
         *
         * @param systemId the path to the resource
         */
        private Source resolveSource(String systemId) throws org.apache.velocity.exception.ResourceNotFoundException {
            Map sourceCache;
            try {
                sourceCache = (Map) this.resolverContext.get(CONTEXT_SOURCE_CACHE_KEY);
            } catch (ContextException ignore) {
                throw new org.apache.velocity.exception.ResourceNotFoundException("Runtime Cocoon source cache not specified in resource loader resolver context.");
            }

            Source source = (Source) sourceCache.get(systemId);
            if (source == null) {
                try {
                    SourceResolver resolver = (SourceResolver) this.resolverContext.get(CONTEXT_RESOLVER_KEY);
                    source = resolver.resolveURI(systemId);
                } catch (ContextException ex) {
                    throw new org.apache.velocity.exception.ResourceNotFoundException("No Cocoon source resolver associated with current request.");
                } catch (Exception ex) {
                    throw new org.apache.velocity.exception.ResourceNotFoundException("Unable to resolve source: " + ex);
                }
            }

            sourceCache.put(systemId, source);
            return source;
        }
    }

    /**
     * Key to lookup the {@link SourceResolver} from the context of
     * the resource loader
     */
    final private static String CONTEXT_RESOLVER_KEY = "resolver";

    /**
     * Key to lookup the source cache {@link Map} from the context of
     * the resource loader
     */
    final private static String CONTEXT_SOURCE_CACHE_KEY = "source-cache";

    private VelocityEngine tmplEngine;
    private boolean tmplEngineInitialized;
    private DefaultContext resolverContext;
    private Context velocityContext;
    private boolean activeFlag;

    /**
     * Read any additional objects to export to the Velocity context
     * from the configuration.
     *
     * @param configuration the class configurations.
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        this.resolverContext = new DefaultContext();
        this.tmplEngine = new VelocityEngine();

        // Set up a JavaScript introspector for the Cocoon flow layer
        this.tmplEngine.setProperty(org.apache.velocity.runtime.RuntimeConstants.UBERSPECT_CLASSNAME,
                                    JSIntrospector.class.getName());
        this.tmplEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, this);

        // First set up our default 'cocoon' resource loader
        this.tmplEngine.setProperty("cocoon.resource.loader.class",
                                    TemplateLoader.class.getName());
        this.tmplEngine.setProperty("cocoon.resource.loader.cache",
                                    configuration.getAttribute("usecache", "false"));
        this.tmplEngine.setProperty("cocoon.resource.loader.modificationCheckInterval",
                                    configuration.getAttribute("checkInterval", "0"));
        this.tmplEngine.setProperty("cocoon.resource.loader.context",
                                    this.resolverContext);

        // Read in any additional properties to pass to the VelocityEngine during initialization
        Configuration[] properties = configuration.getChildren("property");
        for (int i = 0; i < properties.length; ++i) {
            Configuration c = properties[i];
            String name = c.getAttribute("name");

            // Disallow setting of certain properties
            if (name.startsWith("runtime.log")
                    || name.indexOf(".resource.loader.") != -1) {
                if (getLogger().isInfoEnabled()) {
                    getLogger().info("ignoring disallowed property '" + name + "'.");
                }
                continue;
            }
            this.tmplEngine.setProperty(name, c.getAttribute("value"));
        }

        // Now read in any additional Velocity resource loaders
        List resourceLoaders = new ArrayList();
        Configuration[] loaders = configuration.getChildren("resource-loader");
        for (int i = 0; i < loaders.length; ++i) {
            Configuration loader = loaders[i];
            String name = loader.getAttribute("name");
            if (name.equals("cocoon")) {
                if (getLogger().isInfoEnabled()) {
                    getLogger().info("'cocoon' resource loader already defined.");
                }
                continue;
            }
            resourceLoaders.add(name);
            String prefix = name + ".resource.loader.";
            String type = loader.getAttribute("class");
            this.tmplEngine.setProperty(prefix + "class", type);
            Configuration[] loaderProperties = loader.getChildren("property");
            for (int j = 0; j < loaderProperties.length; j++) {
                Configuration c = loaderProperties[j];
                String propName = c.getAttribute("name");
                this.tmplEngine.setProperty(prefix + propName, c.getAttribute("value"));
            }
        }

        // Velocity expects resource loaders as CSV list
        //
        StringBuffer buffer = new StringBuffer("cocoon");
        for (Iterator it = resourceLoaders.iterator(); it.hasNext();) {
            buffer.append(',');
            buffer.append((String) it.next());
        }
        tmplEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, buffer.toString());
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        //this.tmplEngine.init();
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(SourceResolver, Map, String, Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters params)
            throws ProcessingException, SAXException, IOException {
        if (activeFlag) {
            throw new IllegalStateException("setup called on recyclable sitemap component before properly recycling previous state");
        }

        super.setup(resolver, objectModel, src, params);

        // Pass along the SourceResolver to the Velocity resource loader
        this.resolverContext.put(CONTEXT_RESOLVER_KEY, resolver);
        this.resolverContext.put(CONTEXT_SOURCE_CACHE_KEY, new HashMap());

        // FIXME: Initialize the Velocity context. Use objectModel to pass these
        final Object bean = FlowHelper.getContextObject(objectModel);
        if (bean != null) {

            final WebContinuation kont = FlowHelper.getWebContinuation(objectModel);

            // Hack? I use JXPath to determine the properties of the bean object
            final JXPathBeanInfo bi = JXPathIntrospector.getBeanInfo(bean.getClass());
            DynamicPropertyHandler h = null;
            final PropertyDescriptor[] props;
            if (bi.isDynamic()) {
                Class cl = bi.getDynamicPropertyHandlerClass();
                try {
                    h = (DynamicPropertyHandler) cl.newInstance();
                } catch (Exception exc) {
                    exc.printStackTrace();
                    h = null;
                }
                props = null;
            } else {
                h = null;
                props = bi.getPropertyDescriptors();
            }
            final DynamicPropertyHandler handler = h;
            
            this.velocityContext = new Context() {
                    public Object put(String key, Object value) {
                        if (key.equals("flowContext") 
                            || key.equals("continuation")) {
                            return value;
                        }
                        if (handler != null) {
                            handler.setProperty(bean, key, value);
                            return value;
                        } else {
                            for (int i = 0; i < props.length; i++) {
                                if (props[i].getName().equals(key)) {
                                    try {
                                        return props[i].getWriteMethod().invoke(bean, new Object[]{value});
                                    } catch (Exception ignored) {
                                        break;
                                    }
                                }
                            }
                            return value;
                        }
                    }
                    
                    public boolean containsKey(Object key) {
                        if (key.equals("flowContext") 
                            || key.equals("continuation")) {
                            return true;
                        }
                        if (handler != null) {
                            String[] result = handler.getPropertyNames(bean);
                            for (int i = 0; i < result.length; i++) {
                                if (key.equals(result[i])) {
                                    return true;
                                }
                            }
                        } else {
                            for (int i = 0; i < props.length; i++) {
                                if (key.equals(props[i].getName())) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                    
                    public Object[] getKeys() {
                        Object[] result = null;
                        if (handler != null) {
                            result = handler.getPropertyNames(bean);
                        } else {
                            result = new Object[props.length];
                            for (int i = 0; i < props.length; i++) {
                                result[i] = props[i].getName();
                            }
                        }
                        Set set = new HashSet();
                        for (int i = 0; i < result.length; i++) {
                            set.add(result[i]);
                        }
                        set.add("flowContext");
                        set.add("continuation");
                        result = new Object[set.size()];
                        set.toArray(result);
                        return result;
                    }
                    
                    public Object get(String key) {
                        if (key.equals("flowContext")) {
                            return bean;
                        }
                        if (key.equals("continuation")) {
                            return kont;
                        }
                        if (handler != null) {
                            return handler.getProperty(bean, key.toString());
                        } else {
                            for (int i = 0; i < props.length; i++) {
                                if (props[i].getName().equals(key)) {
                                    try {
                                        return props[i].getReadMethod().invoke(bean, null);
                                    } catch (Exception ignored) {
                                        break;
                                    }
                                }
                            }
                            return null;
                        }
                    }
                    
                    public Object remove(Object key) {
                        // not implemented
                        return key;
                    }
                };
        }
        this.velocityContext = 
            new ChainedContext (this.velocityContext, 
                                ObjectModelHelper.getRequest(objectModel), 
                                ObjectModelHelper.getResponse(objectModel), 
                                ObjectModelHelper.getContext(objectModel),
                                params);
        this.velocityContext.put("template", src);
        this.activeFlag = true;
    }
    /**
     * Free up the VelocityContext associated with the pipeline, and
     * release any Source objects resolved by the resource loader.
     *
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        this.activeFlag = false;

        // Recycle all the Source objects resolved/used by our resource loader
        try {
            Map sourceCache = (Map) this.resolverContext.get(CONTEXT_SOURCE_CACHE_KEY);
            for (Iterator it = sourceCache.values().iterator(); it.hasNext();) {
                this.resolver.release((Source) it.next());
            }
        } catch (ContextException ignore) {
        }

        this.velocityContext = null;
        super.recycle();
    }

    /**
     * Generate XML data using Velocity template.
     *
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public void generate()
            throws IOException, SAXException, ProcessingException {
        // Guard against calling generate before setup.
        if (!activeFlag) {
            throw new IllegalStateException("generate called on sitemap component before setup.");
        }

        SAXParser parser = null;
        StringWriter w = new StringWriter();
        try {
            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Processing File: " + super.source);
            }
            if (!tmplEngineInitialized) {
                tmplEngine.init();
                tmplEngineInitialized = true;
            }
            /* lets render a template */
            this.tmplEngine.mergeTemplate(super.source, velocityContext, w);

            InputSource xmlInput =
                    new InputSource(new StringReader(w.toString()));
            xmlInput.setSystemId(super.source);
            parser.parse(xmlInput, this.xmlConsumer);
        } catch (IOException e) {
            getLogger().warn("VelocityGenerator.generate()", e);
            throw new ResourceNotFoundException("Could not get Resource for VelocityGenerator", e);
        } catch (SAXParseException e) {
            int line = e.getLineNumber();
            int column = e.getColumnNumber();
            if (line <= 0) {
                line = Integer.MAX_VALUE;
            }
            BufferedReader reader = 
                new BufferedReader(new StringReader(w.toString()));
            String message = e.getMessage() +" In generated document:\n";
            for (int i = 0; i < line; i++) {
                String lineStr = reader.readLine();
                if (lineStr == null) {
                    break;
                }
                message += lineStr + "\n";
            }
            if (column > 0) {
                String columnIndicator = "";
                for (int i = 1; i < column; i++) {
                    columnIndicator += " ";
                }
                columnIndicator += "^" + "\n";
                message += columnIndicator;
            }
            SAXException pe = new SAXParseException(message, 
                                                    e.getPublicId(),
                                                    "(Document generated from template "+e.getSystemId() + ")",
                                                    e.getLineNumber(),
                                                    e.getColumnNumber(),
                                                    null);
            getLogger().error("VelocityGenerator.generate()", pe);
            throw pe;
        } catch (SAXException e) {
            getLogger().error("VelocityGenerator.generate()", e);
            throw e;
        } catch (ServiceException e) {
            getLogger().error("Could not get parser", e);
            throw new ProcessingException("Exception in VelocityGenerator.generate()", e);
        } catch (ProcessingException e) {
            throw e;
        } catch (Exception e) {
            getLogger().error("Could not get parser", e);
            throw new ProcessingException("Exception in VelocityGenerator.generate()", e);
        } finally {
            this.manager.release(parser);
        }
    }


    /**
     * This implementation does nothing.
     *
     * @see org.apache.velocity.runtime.log.LogSystem#init(RuntimeServices)
     */
    public void init(RuntimeServices rs) throws Exception {
    }

    /**
     * Pass along Velocity log messages to our configured logger.
     *
     * @see org.apache.velocity.runtime.log.LogSystem#logVelocityMessage(int, String)
     */
    public void logVelocityMessage(int level, String message) {
        switch (level) {
            case LogSystem.WARN_ID:
                getLogger().warn(message);
                break;
            case LogSystem.INFO_ID:
                getLogger().info(message);
                break;
            case LogSystem.DEBUG_ID:
                getLogger().debug(message);
                break;
            case LogSystem.ERROR_ID:
                getLogger().error(message);
                break;
            default :
                getLogger().info(message);
                break;
        }
    }
}
