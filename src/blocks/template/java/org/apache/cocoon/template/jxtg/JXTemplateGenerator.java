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
package org.apache.cocoon.template.jxtg;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.flow.javascript.fom.FOM_JavaScriptFlowHelper;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.template.jxtg.environment.ExecutionContext;
import org.apache.cocoon.template.jxtg.environment.JSIntrospector;
import org.apache.cocoon.template.jxtg.environment.JXCacheKey;
import org.apache.cocoon.template.jxtg.environment.JXSourceValidity;
import org.apache.cocoon.template.jxtg.environment.MyVariables;
import org.apache.cocoon.template.jxtg.environment.ValueHelper;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.cocoon.template.jxtg.expression.MyJexlContext;
import org.apache.cocoon.template.jxtg.script.Invoker;
import org.apache.cocoon.template.jxtg.script.Parser;
import org.apache.cocoon.template.jxtg.script.event.*;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.util.Introspector;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.Variables;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;

/**
 * @cocoon.sitemap.component.documentation Provides a generic page template with
 *                                         embedded JSTL and XPath expression
 *                                         substitution to access data sent by
 *                                         Cocoon Flowscripts.
 * 
 * @cocoon.sitemap.component.name jx
 * @cocoon.sitemap.component.label content
 * @cocoon.sitemap.component.logger sitemap.generator.jx
 * 
 * @cocoon.sitemap.component.pooling.min 2
 * @cocoon.sitemap.component.pooling.max 16
 * @cocoon.sitemap.component.pooling.grow 2
 * 
 * 
 * @version CVS $Id$
 */
public class JXTemplateGenerator extends ServiceableGenerator implements
        CacheableProcessingComponent {
    /** The namespace used by this generator */
    public final static String NS = "http://apache.org/cocoon/templates/jx/1.0";

    private static final JXPathContextFactory jxpathContextFactory = JXPathContextFactory
            .newInstance();
    private static final Attributes EMPTY_ATTRS = new AttributesImpl();

    private static final Iterator EMPTY_ITER = new Iterator() {
        public boolean hasNext() {
            return false;
        }

        public Object next() {
            return null;
        }

        public void remove() {
            // EMPTY
        }
    };

    private static final Iterator NULL_ITER = new Iterator() {
        public boolean hasNext() {
            return true;
        }

        public Object next() {
            return null;
        }

        public void remove() {
            // EMPTY
        }
    };

    public static final Locator NULL_LOCATOR = new LocatorImpl();

    public final static String CACHE_KEY = "cache-key";
    public final static String VALIDITY = "cache-validity";

    private JXPathContext jxpathContext;
    private MyJexlContext globalJexlContext;
    private Variables variables;
    private static Map cache = new HashMap();
    private Source inputSource;
    private Map definitions;
    private Map cocoon;

    static {
        // Hack: there's no _nice_ way to add my introspector to Jexl right now
        try {
            Field field = Introspector.class.getDeclaredField("uberSpect");
            field.setAccessible(true);
            field.set(null, new JSIntrospector());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public XMLConsumer getConsumer() {
        return this.xmlConsumer;
    }

    public JXPathContext getJXPathContext() {
        return jxpathContext;
    }

    public MyJexlContext getJexlContext() {
        return globalJexlContext;
    }

    public void recycle() {
        if (this.resolver != null) {
            this.resolver.release(this.inputSource);
        }
        this.inputSource = null;
        this.jxpathContext = null;
        this.globalJexlContext = null;
        this.variables = null;
        this.definitions = null;
        this.cocoon = null;
        super.recycle();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver,
     *      java.util.Map, java.lang.String,
     *      org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src,
            Parameters parameters) throws ProcessingException, SAXException,
            IOException {

        super.setup(resolver, objectModel, src, parameters);
        if (src != null) {
            try {
                this.inputSource = resolver.resolveURI(src);
            } catch (SourceException se) {
                throw SourceUtil.handle("Error during resolving of '" + src
                        + "'.", se);
            }
            final String uri = inputSource.getURI();
            boolean regenerate = false;
            StartDocument startEvent = null;
            synchronized (cache) {
                startEvent = (StartDocument) cache.get(uri);
                if (startEvent != null) {
                    int valid = SourceValidity.UNKNOWN;
                    if (startEvent.getCompileTime() != null) {
                        valid = startEvent.getCompileTime().isValid();
                    }
                    if (valid == SourceValidity.UNKNOWN
                            && startEvent.getCompileTime() != null) {
                        SourceValidity validity = inputSource.getValidity();
                        valid = startEvent.getCompileTime().isValid(validity);
                    }
                    if (valid != SourceValidity.VALID) {
                        cache.remove(uri);
                        regenerate = true;
                    }
                } else {
                    regenerate = true;
                }
            }
            if (regenerate) {
                Parser parser = new Parser();
                SourceUtil.parse(this.manager, this.inputSource, parser);
                startEvent = parser.getStartEvent();
                startEvent.setCompileTime(this.inputSource.getValidity());
                synchronized (cache) {
                    cache.put(uri, startEvent);
                }
            }
        }
        Object bean = FlowHelper.getContextObject(objectModel);
        WebContinuation kont = FlowHelper.getWebContinuation(objectModel);
        setContexts(bean, kont, parameters, objectModel);
        this.definitions = new HashMap();
    }

    public static void fillContext(Object contextObject, Map map) {
        if (contextObject != null) {
            // Hack: I use jxpath to populate the context object's properties
            // in the jexl context
            final JXPathBeanInfo bi = JXPathIntrospector
                    .getBeanInfo(contextObject.getClass());
            if (bi.isDynamic()) {
                Class cl = bi.getDynamicPropertyHandlerClass();
                try {
                    DynamicPropertyHandler h = (DynamicPropertyHandler) cl
                            .newInstance();
                    String[] result = h.getPropertyNames(contextObject);
                    int len = result.length;
                    for (int i = 0; i < len; i++) {
                        try {
                            map.put(result[i], h.getProperty(contextObject,
                                    result[i]));
                        } catch (Exception exc) {
                            exc.printStackTrace();
                        }
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            } else {
                PropertyDescriptor[] props = bi.getPropertyDescriptors();
                int len = props.length;
                for (int i = 0; i < len; i++) {
                    try {
                        Method read = props[i].getReadMethod();
                        if (read != null) {
                            map.put(props[i].getName(), read.invoke(
                                    contextObject, null));
                        }
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
        }
    }

    private void setContexts(Object contextObject, WebContinuation kont,
            Parameters parameters, Map objectModel) {
        final Request request = ObjectModelHelper.getRequest(objectModel);
        final Object session = request.getSession(false);
        final Object app = ObjectModelHelper.getContext(objectModel);
        cocoon = new HashMap();
        cocoon.put("request", FOM_JavaScriptFlowHelper
                .getFOM_Request(objectModel));
        if (session != null) {
            cocoon.put("session", FOM_JavaScriptFlowHelper
                    .getFOM_Session(objectModel));
        }
        cocoon.put("context", FOM_JavaScriptFlowHelper
                .getFOM_Context(objectModel));
        cocoon.put("continuation", FOM_JavaScriptFlowHelper
                .getFOM_WebContinuation(objectModel));
        cocoon.put("parameters", Parameters.toProperties(parameters));
        this.variables = new MyVariables(cocoon, contextObject, kont, request,
                session, app, parameters);
        Map map;
        if (contextObject instanceof Map) {
            map = (Map) contextObject;
        } else {
            map = new HashMap();
            fillContext(contextObject, map);
        }
        jxpathContext = jxpathContextFactory.newContext(null, contextObject);
        jxpathContext.setVariables(variables);
        jxpathContext.setLenient(parameters.getParameterAsBoolean(
                "lenient-xpath", false));
        globalJexlContext = new MyJexlContext();
        globalJexlContext.setVars(map);
        map = globalJexlContext.getVars();
        map.put("cocoon", cocoon);
        if (contextObject != null) {
            map.put("flowContext", contextObject);
            // FIXME (VG): Is this required (what it's used for - examples)?
            // Here I use Rhino's live-connect objects to allow Jexl to call
            // java constructors
            Object javaPkg = FOM_JavaScriptFlowHelper
                    .getJavaPackage(objectModel);
            Object pkgs = FOM_JavaScriptFlowHelper.getPackages(objectModel);
            map.put("java", javaPkg);
            map.put("Packages", pkgs);
        }
        if (kont != null) {
            map.put("continuation", kont);
        }
        map.put("request", request);
        map.put("context", app);
        map.put("parameters", parameters);
        if (session != null) {
            map.put("session", session);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public void generate() throws IOException, SAXException,
            ProcessingException {
        final String cacheKey = this.inputSource.getURI();

        StartDocument startEvent;
        synchronized (cache) {
            startEvent = (StartDocument) cache.get(cacheKey);
        }
        performGeneration(this.xmlConsumer, globalJexlContext, jxpathContext,
                null, startEvent, null);
    }

    public void performGeneration(final XMLConsumer consumer,
            MyJexlContext jexlContext, JXPathContext jxpathContext,
            StartElement macroCall, Event startEvent, Event endEvent)
            throws SAXException {
        cocoon.put("consumer", consumer);
        Invoker.execute(this.xmlConsumer,
                        new ExecutionContext(jexlContext, jxpathContext,
                                             this.variables, this.definitions, this.cache,
                                             this.manager),
                        null, startEvent, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getKey()
     */
    public Serializable getKey() {
        JXTExpression cacheKeyExpr = (JXTExpression) getCurrentTemplateProperty(JXTemplateGenerator.CACHE_KEY);
        try {
            final Serializable templateKey = (Serializable) ValueHelper
                    .getValue(cacheKeyExpr, globalJexlContext, jxpathContext);
            if (templateKey != null) {
                return new JXCacheKey(this.inputSource.getURI(), templateKey);
            }
        } catch (Exception e) {
            getLogger().error("error evaluating cache key", e);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getValidity()
     */
    public SourceValidity getValidity() {
        JXTExpression validityExpr = (JXTExpression) getCurrentTemplateProperty(JXTemplateGenerator.VALIDITY);
        try {
            final SourceValidity sourceValidity = this.inputSource
                    .getValidity();
            final SourceValidity templateValidity = (SourceValidity) ValueHelper
                    .getValue(validityExpr, globalJexlContext, jxpathContext);
            if (sourceValidity != null && templateValidity != null) {
                return new JXSourceValidity(sourceValidity, templateValidity);
            }
        } catch (Exception e) {
            getLogger().error("error evaluating cache validity", e);
        }
        return null;
    }

    private Object getCurrentTemplateProperty(String propertyName) {
        final String uri = this.inputSource.getURI();
        StartDocument startEvent;
        synchronized (cache) {
            startEvent = (StartDocument) cache.get(uri);
        }
        return (startEvent != null) ? startEvent.getTemplateProperties().get(
                propertyName) : null;
    }
}
