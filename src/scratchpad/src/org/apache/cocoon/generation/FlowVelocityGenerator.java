/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-

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
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.mozilla.javascript.*;
import org.apache.velocity.util.introspection.*;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Cocoon {@link Generator} that produces dynamic XML SAX events
 * from a Velocity template file.</p>
 *
 * <p> This differs from VelocityGenerator only in the objects that
 * populate the velocity context passed to the template. In this
 * generator there are two objects:
 * <ul>
 * <li>this - which represents the bean that was passed to sendPage*() </li>
 * <li>continuation - which represents the current continuation - an instance
 * of org.apache.cocoon.components.flow.WebContinuation</li>
 * </ul>
 * The immediate properties of the bean object are also available in the context
 * </p>
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
 * @version CVS $Id: FlowVelocityGenerator.java,v 1.3 2003/04/06 03:04:27 coliver Exp $
 */
public class FlowVelocityGenerator extends ComposerGenerator
        implements Initializable, Configurable, LogSystem {

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
			result == ScriptableObject.NOT_FOUND) {
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
			result == ScriptableObject.NOT_FOUND) {
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
			result == ScriptableObject.NOT_FOUND) {
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
			result == ScriptableObject.NOT_FOUND) {
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
         * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#init
         */
        public void init(ExtendedProperties config) {
            this.resolverContext = (org.apache.avalon.framework.context.Context) config.get("context");
            if (this.resolverContext == null) {
                throw new IllegalArgumentException("Runtime Cocoon resolver context not specified in resource loader configuration.");
            }
        }

        /**
         * @param systemId the path to the resource
         * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getResourceStream
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
         * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#isSourceModified
         */
        public boolean isSourceModified(org.apache.velocity.runtime.resource.Resource resource) {
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
         * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getLastModified
         */
        public long getLastModified(org.apache.velocity.runtime.resource.Resource resource) {
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
    private DefaultContext resolverContext;
    private Context velocityContext;
    private boolean activeFlag;

    /**
     * Read any additional objects to export to the Velocity context
     * from the configuration.
     *
     * @param configuration the class configurations.
     * @see org.apache.avalon.framework.configuration.Configurable#configure
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        this.resolverContext = new DefaultContext();
        this.tmplEngine = new VelocityEngine();

        // Set up a JavaScript introspector for the Cocoon flow layer
        this.tmplEngine.setProperty(org.apache.velocity.runtime.RuntimeConstants.UBERSPECT_CLASSNAME,
                                    JSIntrospector.class.getName());
        this.tmplEngine.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, this);

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
        tmplEngine.setProperty(Velocity.RESOURCE_LOADER, buffer.toString());
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
        final Object bean = ((Environment) resolver).getAttribute("bean-dict");
        final WebContinuation kont =
                (WebContinuation) ((Environment) resolver).getAttribute("kont");

        // This velocity context provides two variables: "this" which represents the
        // bean object passed to sendPage*() and "continuation" which is the
        // current continuation. The immediate properties of the bean object are
        // also available in the context.
        //
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
                if (key.equals("this") || key.equals("continuation")) {
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
                if (key.equals("this") || key.equals("continuation")) {
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
                set.add("this");
                set.add("continuation");
                result = new Object[set.size()];
                set.toArray(result);
                return result;
            }

            public Object get(String key) {
                if (key.equals("this")) {
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
     * @see org.apache.cocoon.generation.Generator#generate
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
            String columnIndicator = "";
            if (column > 0) {
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
        } catch (ComponentException e) {
            getLogger().error("Could not get parser", e);
            throw new ProcessingException("Exception in VelocityGenerator.generate()", e);
        } catch (ProcessingException e) {
            throw e;
        } catch (Exception e) {
            getLogger().error("Could not get parser", e);
            throw new ProcessingException("Exception in VelocityGenerator.generate()", e);
        } finally {
            this.manager.release((Component) parser);
        }
    }


    /**
     * This implementation does nothing.
     *
     * @see org.apache.velocity.runtime.log.LogSystem#init
     */
    public void init(RuntimeServices rs) throws Exception {
    }

    /**
     * Pass along Velocity log messages to our configured logger.
     *
     * @see org.apache.velocity.runtime.log.LogSystem#logVelocityMessage
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
