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
import java.util.Properties;

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
import org.apache.cocoon.template.jxtg.environment.ErrorHolder;
import org.apache.cocoon.template.jxtg.environment.JSIntrospector;
import org.apache.cocoon.template.jxtg.environment.JXCacheKey;
import org.apache.cocoon.template.jxtg.environment.JXSourceValidity;
import org.apache.cocoon.template.jxtg.environment.LocatorFacade;
import org.apache.cocoon.template.jxtg.environment.MyVariables;
import org.apache.cocoon.template.jxtg.environment.ValueHelper;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.cocoon.template.jxtg.expression.Literal;
import org.apache.cocoon.template.jxtg.expression.MyJexlContext;
import org.apache.cocoon.template.jxtg.expression.Subst;
import org.apache.cocoon.template.jxtg.script.Parser;
import org.apache.cocoon.template.jxtg.script.event.*;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.util.Introspector;
import org.apache.commons.jexl.util.introspection.Info;
import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.Variables;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.sax.XMLizable;
import org.mozilla.javascript.NativeArray;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
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
 * @version CVS $Id: JXTemplateGenerator.java 106437 2004-11-24 17:35:45Z unico $
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

    private void fillContext(Object contextObject, Map map) {
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
        execute(this.xmlConsumer, globalJexlContext, jxpathContext, null,
                startEvent, null);
    }

    interface CharHandler {
        public void characters(char[] ch, int offset, int length)
                throws SAXException;
    }

    private void characters(JexlContext jexlContext,
            JXPathContext jxpathContext, TextEvent event, CharHandler handler)
            throws SAXException {
        Iterator iter = event.getSubstitutions().iterator();
        while (iter.hasNext()) {
            Object subst = iter.next();
            char[] chars;
            if (subst instanceof char[]) {
                chars = (char[]) subst;
            } else {
                JXTExpression expr = (JXTExpression) subst;
                try {
                    Object val = ValueHelper.getValue(expr, jexlContext,
                            jxpathContext);
                    chars = val != null ? val.toString().toCharArray()
                            : ArrayUtils.EMPTY_CHAR_ARRAY;
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(), event
                            .getLocation(), e);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(), event
                            .getLocation(), new ErrorHolder(err));
                }
            }
            handler.characters(chars, 0, chars.length);
        }
    }

    /**
     * dump a DOM document, using an IncludeXMLConsumer to filter out start/end
     * document events
     */
    private void executeDOM(final XMLConsumer consumer,
            MyJexlContext jexlContext, JXPathContext jxpathContext, Node node)
            throws SAXException {
        IncludeXMLConsumer includer = new IncludeXMLConsumer(consumer);
        DOMStreamer streamer = new DOMStreamer(includer);
        streamer.stream(node);
    }

    private void call(Locator location, StartElement macroCall,
            final XMLConsumer consumer, MyJexlContext jexlContext,
            JXPathContext jxpathContext, Event startEvent, Event endEvent)
            throws SAXException {
        try {
            execute(consumer, jexlContext, jxpathContext, macroCall,
                    startEvent, endEvent);
        } catch (SAXParseException exc) {
            throw new SAXParseException(macroCall.getLocalName() + ": "
                    + exc.getMessage(), location, exc);
        }
    }

    private void execute(final XMLConsumer consumer, MyJexlContext jexlContext,
            JXPathContext jxpathContext, StartElement macroCall,
            Event startEvent, Event endEvent) throws SAXException {
        Event ev = startEvent;
        LocatorFacade loc = new LocatorFacade(ev.getLocation());
        consumer.setDocumentLocator(loc);
        while (ev != endEvent) {
            loc.setDocumentLocator(ev.getLocation());
            if (ev instanceof Characters) {
                TextEvent text = (TextEvent) ev;
                Iterator iter = text.getSubstitutions().iterator();
                while (iter.hasNext()) {
                    Object subst = iter.next();
                    char[] chars;
                    if (subst instanceof char[]) {
                        chars = (char[]) subst;
                    } else {
                        JXTExpression expr = (JXTExpression) subst;
                        try {
                            Object val = ValueHelper.getNode(expr, jexlContext,
                                    jxpathContext);
                            if (val instanceof Node) {
                                executeDOM(consumer, jexlContext,
                                        jxpathContext, (Node) val);
                                continue;
                            } else if (val instanceof NodeList) {
                                NodeList nodeList = (NodeList) val;
                                int len = nodeList.getLength();
                                for (int i = 0; i < len; i++) {
                                    Node n = nodeList.item(i);
                                    executeDOM(consumer, jexlContext,
                                            jxpathContext, n);
                                }
                                continue;
                            } else if (val instanceof Node[]) {
                                Node[] nodeList = (Node[]) val;
                                int len = nodeList.length;
                                for (int i = 0; i < len; i++) {
                                    Node n = nodeList[i];
                                    executeDOM(consumer, jexlContext,
                                            jxpathContext, n);
                                }
                                continue;
                            } else if (val instanceof XMLizable) {
                                ((XMLizable) val).toSAX(new IncludeXMLConsumer(
                                        consumer));
                                continue;
                            }
                            chars = val != null ? val.toString().toCharArray()
                                    : ArrayUtils.EMPTY_CHAR_ARRAY;
                        } catch (Exception e) {
                            throw new SAXParseException(e.getMessage(), ev
                                    .getLocation(), e);
                        } catch (Error err) {
                            throw new SAXParseException(err.getMessage(), ev
                                    .getLocation(), new ErrorHolder(err));
                        }
                    }
                    consumer.characters(chars, 0, chars.length);
                }
            } else if (ev instanceof EndElement) {
                EndElement endElement = (EndElement) ev;
                StartElement startElement = endElement.getStartElement();
                consumer.endElement(startElement.getNamespaceURI(),
                        startElement.getLocalName(), startElement.getRaw());
            } else if (ev instanceof EndPrefixMapping) {
                EndPrefixMapping endPrefixMapping = (EndPrefixMapping) ev;
                consumer.endPrefixMapping(endPrefixMapping.getPrefix());
            } else if (ev instanceof IgnorableWhitespace) {
                TextEvent text = (TextEvent) ev;
                characters(jexlContext, jxpathContext, text, new CharHandler() {
                    public void characters(char[] ch, int offset, int len)
                            throws SAXException {
                        consumer.ignorableWhitespace(ch, offset, len);
                    }
                });
            } else if (ev instanceof SkippedEntity) {
                SkippedEntity skippedEntity = (SkippedEntity) ev;
                consumer.skippedEntity(skippedEntity.getName());
            } else if (ev instanceof StartIf) {
                StartIf startIf = (StartIf) ev;
                Object val;
                try {
                    val = ValueHelper.getValue(startIf.getTest(), jexlContext,
                            jxpathContext, Boolean.TRUE);
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(), ev
                            .getLocation(), e);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(), ev
                            .getLocation(), new ErrorHolder(err));
                }
                boolean result = false;
                if (val instanceof Boolean) {
                    result = ((Boolean) val).booleanValue();
                } else {
                    result = (val != null);
                }
                if (!result) {
                    ev = startIf.getEndInstruction().getNext();
                    continue;
                }
            } else if (ev instanceof StartForEach) {
                StartForEach startForEach = (StartForEach) ev;
                final Object items = startForEach.getItems();
                Iterator iter = null;
                int begin, end, step;
                String var, varStatus;
                try {
                    if (items != null) {
                        JXTExpression expr = (JXTExpression) items;
                        if (expr.getCompiledExpression() instanceof CompiledExpression) {
                            CompiledExpression compiledExpression = (CompiledExpression) expr
                                    .getCompiledExpression();
                            Object val = compiledExpression.getPointer(
                                    jxpathContext, expr.getRaw()).getNode();
                            // FIXME: workaround for JXPath bug
                            iter = val instanceof NativeArray ? new JSIntrospector.NativeArrayIterator(
                                    (NativeArray) val)
                                    : compiledExpression
                                            .iteratePointers(jxpathContext);
                        } else if (expr.getCompiledExpression() instanceof Expression) {
                            Expression e = (Expression) expr
                                    .getCompiledExpression();
                            Object result = e.evaluate(jexlContext);
                            if (result != null) {
                                iter = Introspector.getUberspect().getIterator(
                                        result,
                                        new Info(
                                                ev.getLocation().getSystemId(),
                                                ev.getLocation()
                                                        .getLineNumber(), ev
                                                        .getLocation()
                                                        .getColumnNumber()));
                            }
                            if (iter == null) {
                                iter = EMPTY_ITER;
                            }
                        } else {
                            // literal value
                            iter = new Iterator() {
                                Object val = items;

                                public boolean hasNext() {
                                    return val != null;
                                }

                                public Object next() {
                                    Object res = val;
                                    val = null;
                                    return res;
                                }

                                public void remove() {
                                    // EMPTY
                                }
                            };
                        }
                    } else {
                        iter = NULL_ITER;
                    }
                    begin = startForEach.getBegin() == null ? 0 : ValueHelper
                            .getIntValue(startForEach.getBegin(), jexlContext,
                                    jxpathContext);
                    end = startForEach.getEnd() == null ? Integer.MAX_VALUE
                            : ValueHelper.getIntValue(startForEach.getEnd(),
                                    jexlContext, jxpathContext);
                    step = startForEach.getStep() == null ? 1 : ValueHelper
                            .getIntValue(startForEach.getStep(), jexlContext,
                                    jxpathContext);
                    var = ValueHelper.getStringValue(startForEach.getVar(),
                            jexlContext, jxpathContext);
                    varStatus = ValueHelper.getStringValue(startForEach
                            .getVarStatus(), jexlContext, jxpathContext);
                } catch (Exception exc) {
                    throw new SAXParseException(exc.getMessage(), ev
                            .getLocation(), exc);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(), ev
                            .getLocation(), new ErrorHolder(err));
                }
                MyJexlContext localJexlContext = new MyJexlContext(jexlContext);
                MyVariables localJXPathVariables = new MyVariables(
                        (MyVariables) jxpathContext.getVariables());
                int i = 0;
                // Move to the begin row
                while (i < begin && iter.hasNext()) {
                    iter.next();
                    i++;
                }
                LoopTagStatus status = null;
                if (varStatus != null) {
                    status = new LoopTagStatus();
                    status.setBegin(begin);
                    status.setEnd(end);
                    status.setStep(step);
                    status.setFirst(true);
                    localJexlContext.put(varStatus, status);
                    localJXPathVariables.declareVariable(varStatus, status);
                }
                int skipCounter, count = 1;
                JXPathContext localJXPathContext = null;
                while (i <= end && iter.hasNext()) {
                    Object value = iter.next();
                    if (value instanceof Pointer) {
                        Pointer ptr = (Pointer) value;
                        localJXPathContext = jxpathContext
                                .getRelativeContext(ptr);
                        try {
                            value = ptr.getNode();
                        } catch (Exception exc) {
                            throw new SAXParseException(exc.getMessage(), ev
                                    .getLocation(), null);
                        }
                    } else {
                        localJXPathContext = jxpathContextFactory.newContext(
                                jxpathContext, value);
                    }
                    localJXPathContext.setVariables(localJXPathVariables);
                    if (var != null) {
                        localJexlContext.put(var, value);
                    }
                    if (status != null) {
                        status.setIndex(i);
                        status.setCount(count);
                        status.setFirst(i == begin);
                        status.setCurrent(value);
                        status.setLast((i == end || !iter.hasNext()));
                    }
                    execute(consumer, localJexlContext, localJXPathContext,
                            macroCall, startForEach.getNext(), startForEach
                                    .getEndInstruction());
                    // Skip rows
                    skipCounter = step;
                    while (--skipCounter > 0 && iter.hasNext()) {
                        iter.next();
                    }
                    // Increase index
                    i += step;
                    count++;
                }
                ev = startForEach.getEndInstruction().getNext();
                continue;
            } else if (ev instanceof StartChoose) {
                StartChoose startChoose = (StartChoose) ev;
                StartWhen startWhen = startChoose.getFirstChoice();
                while (startWhen != null) {
                    Object val;
                    try {
                        val = ValueHelper.getValue(startWhen.getTest(), jexlContext,
                                jxpathContext, Boolean.TRUE);
                    } catch (Exception e) {
                        throw new SAXParseException(e.getMessage(), ev
                                .getLocation(), e);
                    }
                    boolean result;
                    if (val instanceof Boolean) {
                        result = ((Boolean) val).booleanValue();
                    } else {
                        result = (val != null);
                    }
                    if (result) {
                        execute(consumer, jexlContext, jxpathContext,
                                macroCall, startWhen.getNext(), startWhen
                                        .getEndInstruction());
                        break;
                    }
                    startWhen = startWhen.getNextChoice();
                }
                if (startWhen == null && startChoose.getOtherwise() != null) {
                    execute(consumer, jexlContext, jxpathContext, macroCall,
                            startChoose.getOtherwise().getNext(), startChoose
                                    .getOtherwise().getEndInstruction());
                }
                ev = startChoose.getEndInstruction().getNext();
                continue;
            } else if (ev instanceof StartSet) {
                StartSet startSet = (StartSet) ev;
                Object value = null;
                String var = null;
                try {
                    if (startSet.getVar() != null) {
                        var = ValueHelper.getStringValue(startSet.getVar(),
                                jexlContext, jxpathContext);
                    }
                    if (startSet.getValue() != null) {
                        value = ValueHelper.getNode(startSet.getValue(),
                                jexlContext, jxpathContext);
                    }
                } catch (Exception exc) {
                    throw new SAXParseException(exc.getMessage(), ev
                            .getLocation(), exc);
                }
                if (value == null) {
                    NodeList nodeList = toDOMNodeList("set", startSet,
                            jexlContext, macroCall);
                    // JXPath doesn't handle NodeList, so convert it to an array
                    int len = nodeList.getLength();
                    Node[] nodeArr = new Node[len];
                    for (int i = 0; i < len; i++) {
                        nodeArr[i] = nodeList.item(i);
                    }
                    value = nodeArr;
                }
                if (var != null) {
                    jxpathContext.getVariables().declareVariable(var, value);
                    jexlContext.put(var, value);
                }
                ev = startSet.getEndInstruction().getNext();
                continue;
            } else if (ev instanceof StartElement) {
                StartElement startElement = (StartElement) ev;
                StartDefine def = (StartDefine) definitions.get(startElement
                        .getQname());
                if (def != null) {
                    Map attributeMap = new HashMap();
                    Iterator i = startElement.getAttributeEvents().iterator();
                    while (i.hasNext()) {
                        String attributeName;
                        Object attributeValue;
                        AttributeEvent attrEvent = (AttributeEvent) i.next();
                        attributeName = attrEvent.getLocalName();
                        if (attrEvent instanceof CopyAttribute) {
                            CopyAttribute copy = (CopyAttribute) attrEvent;
                            attributeValue = copy.getValue();
                        } else if (attrEvent instanceof SubstituteAttribute) {
                            SubstituteAttribute substEvent = (SubstituteAttribute) attrEvent;
                            if (substEvent.getSubstitutions().size() == 1
                                    && substEvent.getSubstitutions().get(0) instanceof JXTExpression) {
                                JXTExpression expr = (JXTExpression) substEvent
                                        .getSubstitutions().get(0);
                                Object val;
                                try {
                                    val = ValueHelper.getNode(expr,
                                            jexlContext, jxpathContext);
                                } catch (Exception e) {
                                    throw new SAXParseException(e.getMessage(),
                                            ev.getLocation(), e);
                                } catch (Error err) {
                                    throw new SAXParseException(err
                                            .getMessage(), ev.getLocation(),
                                            new ErrorHolder(err));
                                }
                                attributeValue = val != null ? val : "";
                            } else {
                                StringBuffer buf = new StringBuffer();
                                Iterator iterSubst = substEvent
                                        .getSubstitutions().iterator();
                                while (iterSubst.hasNext()) {
                                    Subst subst = (Subst) iterSubst.next();
                                    if (subst instanceof Literal) {
                                        Literal lit = (Literal) subst;
                                        buf.append(lit.getValue());
                                    } else if (subst instanceof JXTExpression) {
                                        JXTExpression expr = (JXTExpression) subst;
                                        Object val;
                                        try {
                                            val = ValueHelper.getValue(expr,
                                                    jexlContext, jxpathContext);
                                        } catch (Exception e) {
                                            throw new SAXParseException(e
                                                    .getMessage(), ev
                                                    .getLocation(), e);
                                        } catch (Error err) {
                                            throw new SAXParseException(err
                                                    .getMessage(), ev
                                                    .getLocation(),
                                                    new ErrorHolder(err));
                                        }
                                        buf.append(val != null ? val.toString()
                                                : "");
                                    }
                                }
                                attributeValue = buf.toString();
                            }
                        } else {
                            throw new Error("this shouldn't have happened");
                        }
                        attributeMap.put(attributeName, attributeValue);
                    }
                    MyVariables parent = (MyVariables) jxpathContext
                            .getVariables();
                    MyVariables vars = new MyVariables(parent);
                    MyJexlContext localJexlContext = new MyJexlContext(
                            jexlContext);
                    HashMap macro = new HashMap();
                    macro.put("body", startElement);
                    macro.put("arguments", attributeMap);
                    localJexlContext.put("macro", macro);
                    vars.declareVariable("macro", macro);
                    Iterator iter = def.getParameters().entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry e = (Map.Entry) iter.next();
                        String key = (String) e.getKey();
                        StartParameter startParam = (StartParameter) e
                                .getValue();
                        Object default_ = startParam.getDefaultValue();
                        Object val = attributeMap.get(key);
                        if (val == null) {
                            val = default_;
                        }
                        localJexlContext.put(key, val);
                        vars.declareVariable(key, val);
                    }
                    JXPathContext localJXPathContext = jxpathContextFactory
                            .newContext(null, jxpathContext.getContextBean());
                    localJXPathContext.setVariables(vars);
                    call(ev.getLocation(), startElement, consumer,
                            localJexlContext, localJXPathContext,
                            def.getBody(), def.getEndInstruction());
                    ev = startElement.getEndElement().getNext();
                    continue;
                }
                Iterator i = startElement.getAttributeEvents().iterator();
                AttributesImpl attrs = new AttributesImpl();
                while (i.hasNext()) {
                    AttributeEvent attrEvent = (AttributeEvent) i.next();
                    if (attrEvent instanceof CopyAttribute) {
                        CopyAttribute copy = (CopyAttribute) attrEvent;
                        attrs.addAttribute(copy.getNamespaceURI(), copy
                                .getLocalName(), copy.getRaw(), copy.getType(),
                                copy.getValue());
                    } else if (attrEvent instanceof SubstituteAttribute) {
                        StringBuffer buf = new StringBuffer();
                        SubstituteAttribute substEvent = (SubstituteAttribute) attrEvent;
                        Iterator iterSubst = substEvent.getSubstitutions()
                                .iterator();
                        while (iterSubst.hasNext()) {
                            Subst subst = (Subst) iterSubst.next();
                            if (subst instanceof Literal) {
                                Literal lit = (Literal) subst;
                                buf.append(lit.getValue());
                            } else if (subst instanceof JXTExpression) {
                                JXTExpression expr = (JXTExpression) subst;
                                Object val;
                                try {
                                    val = ValueHelper.getValue(expr,
                                            jexlContext, jxpathContext);
                                } catch (Exception e) {
                                    throw new SAXParseException(e.getMessage(),
                                            ev.getLocation(), e);
                                } catch (Error err) {
                                    throw new SAXParseException(err
                                            .getMessage(), ev.getLocation(),
                                            new ErrorHolder(err));
                                }
                                buf.append(val != null ? val.toString() : "");
                            }
                        }
                        attrs.addAttribute(attrEvent.getNamespaceURI(),
                                attrEvent.getLocalName(), attrEvent.getRaw(),
                                attrEvent.getType(), buf.toString());
                    }
                }
                consumer.startElement(startElement.getNamespaceURI(),
                        startElement.getLocalName(), startElement.getRaw(),
                        attrs);
            } else if (ev instanceof StartFormatNumber) {
                StartFormatNumber startFormatNumber = (StartFormatNumber) ev;
                try {
                    String result = startFormatNumber.format(jexlContext,
                            jxpathContext);
                    if (result != null) {
                        char[] chars = result.toCharArray();
                        consumer.characters(chars, 0, chars.length);
                    }
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(), ev
                            .getLocation(), e);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(), ev
                            .getLocation(), new ErrorHolder(err));
                }
            } else if (ev instanceof StartFormatDate) {
                StartFormatDate startFormatDate = (StartFormatDate) ev;
                try {
                    String result = startFormatDate.format(jexlContext,
                            jxpathContext);
                    if (result != null) {
                        char[] chars = result.toCharArray();
                        consumer.characters(chars, 0, chars.length);
                    }
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(), ev
                            .getLocation(), e);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(), ev
                            .getLocation(), new ErrorHolder(err));
                }
            } else if (ev instanceof StartPrefixMapping) {
                StartPrefixMapping startPrefixMapping = (StartPrefixMapping) ev;
                consumer.startPrefixMapping(startPrefixMapping.getPrefix(),
                        startPrefixMapping.getUri());
            } else if (ev instanceof StartComment) {
                StartComment startJXComment = (StartComment) ev;
                // Parse the body of the comment
                NodeList nodeList = toDOMNodeList("comment", startJXComment,
                        jexlContext, macroCall);
                // JXPath doesn't handle NodeList, so convert it to an array
                int len = nodeList.getLength();
                final StringBuffer buf = new StringBuffer();
                Properties omit = XMLUtils.createPropertiesForXML(true);
                for (int i = 0; i < len; i++) {
                    try {
                        String str = XMLUtils.serializeNode(nodeList.item(i),
                                omit);
                        buf.append(StringUtils.substringAfter(str, ">")); // cut
                        // the
                        // XML
                        // header
                    } catch (ProcessingException e) {
                        throw new SAXParseException(e.getMessage(),
                                startJXComment.getLocation(), e);
                    }
                }
                char[] chars = new char[buf.length()];
                buf.getChars(0, chars.length, chars, 0);
                consumer.comment(chars, 0, chars.length);
                ev = startJXComment.getEndInstruction().getNext();
                continue;
            } else if (ev instanceof EndCDATA) {
                consumer.endCDATA();
            } else if (ev instanceof EndDTD) {
                consumer.endDTD();
            } else if (ev instanceof EndEntity) {
                consumer.endEntity(((EndEntity) ev).getName());
            } else if (ev instanceof StartCDATA) {
                consumer.startCDATA();
            } else if (ev instanceof StartDTD) {
                StartDTD startDTD = (StartDTD) ev;
                consumer.startDTD(startDTD.getName(), startDTD.getPublicId(),
                        startDTD.getSystemId());
            } else if (ev instanceof StartEntity) {
                consumer.startEntity(((StartEntity) ev).getName());
            } else if (ev instanceof StartOut) {
                StartOut startOut = (StartOut) ev;
                Object val;
                try {
                    val = ValueHelper.getNode(startOut.getCompiledExpression(),
                            jexlContext, jxpathContext, startOut.getLenient());
                    if (val instanceof Node) {
                        executeDOM(consumer, jexlContext, jxpathContext,
                                (Node) val);
                    } else if (val instanceof NodeList) {
                        NodeList nodeList = (NodeList) val;
                        int len = nodeList.getLength();
                        for (int i = 0; i < len; i++) {
                            Node n = nodeList.item(i);
                            executeDOM(consumer, jexlContext, jxpathContext, n);
                        }
                    } else if (val instanceof Node[]) {
                        Node[] nodeList = (Node[]) val;
                        int len = nodeList.length;
                        for (int i = 0; i < len; i++) {
                            Node n = nodeList[i];
                            executeDOM(consumer, jexlContext, jxpathContext, n);
                        }
                    } else if (val instanceof XMLizable) {
                        ((XMLizable) val)
                                .toSAX(new IncludeXMLConsumer(consumer));
                    } else {
                        char[] ch = val == null ? ArrayUtils.EMPTY_CHAR_ARRAY
                                : val.toString().toCharArray();
                        consumer.characters(ch, 0, ch.length);
                    }
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(), ev
                            .getLocation(), e);
                }
            } else if (ev instanceof StartTemplate) {
                // EMPTY
            } else if (ev instanceof StartEval) {
                StartEval startEval = (StartEval) ev;
                JXTExpression expr = startEval.getValue();
                try {
                    Object val = ValueHelper.getNode(expr, jexlContext,
                            jxpathContext);
                    if (!(val instanceof StartElement)) {
                        throw new Exception(
                                "macro invocation required instead of: " + val);
                    }
                    StartElement call = (StartElement) val;
                    execute(consumer, jexlContext, jxpathContext, call, call
                            .getNext(), call.getEndElement());
                } catch (Exception exc) {
                    throw new SAXParseException(exc.getMessage(), ev
                            .getLocation(), exc);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(), ev
                            .getLocation(), new ErrorHolder(err));
                }
                ev = startEval.getEndInstruction().getNext();
                continue;
            } else if (ev instanceof StartEvalBody) {
                StartEvalBody startEval = (StartEvalBody) ev;
                try {
                    execute(consumer, jexlContext, jxpathContext, null,
                            macroCall.getNext(), macroCall.getEndElement());
                } catch (Exception exc) {
                    throw new SAXParseException(exc.getMessage(), ev
                            .getLocation(), exc);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(), ev
                            .getLocation(), new ErrorHolder(err));
                }
                ev = startEval.getEndInstruction().getNext();
                continue;
            } else if (ev instanceof StartDefine) {
                StartDefine startDefine = (StartDefine) ev;
                definitions.put(startDefine.getQname(), startDefine);
                ev = startDefine.getEndInstruction().getNext();
                continue;
            } else if (ev instanceof StartImport) {
                StartImport startImport = (StartImport) ev;
                String uri;
                AttributeEvent e = startImport.getUri();
                if (e instanceof CopyAttribute) {
                    CopyAttribute copy = (CopyAttribute) e;
                    uri = copy.getValue();
                } else {
                    StringBuffer buf = new StringBuffer();
                    SubstituteAttribute substAttr = (SubstituteAttribute) e;
                    Iterator i = substAttr.getSubstitutions().iterator();
                    while (i.hasNext()) {
                        Subst subst = (Subst) i.next();
                        if (subst instanceof Literal) {
                            Literal lit = (Literal) subst;
                            buf.append(lit.getValue());
                        } else if (subst instanceof JXTExpression) {
                            JXTExpression expr = (JXTExpression) subst;
                            Object val;
                            try {
                                val = ValueHelper.getValue(expr, jexlContext,
                                        jxpathContext);
                            } catch (Exception exc) {
                                throw new SAXParseException(exc.getMessage(),
                                        ev.getLocation(), exc);
                            } catch (Error err) {
                                throw new SAXParseException(err.getMessage(),
                                        ev.getLocation(), new ErrorHolder(err));
                            }
                            buf.append(val != null ? val.toString() : "");
                        }
                    }
                    uri = buf.toString();
                }
                Source input = null;
                StartDocument doc;
                try {
                    input = resolver.resolveURI(uri);
                    SourceValidity validity = null;
                    synchronized (cache) {
                        doc = (StartDocument) cache.get(input.getURI());
                        if (doc != null) {
                            boolean recompile = false;
                            if (doc.getCompileTime() == null) {
                                recompile = true;
                            } else {
                                int valid = doc.getCompileTime().isValid();
                                if (valid == SourceValidity.UNKNOWN) {
                                    validity = input.getValidity();
                                    valid = doc.getCompileTime().isValid(
                                            validity);
                                }
                                if (valid != SourceValidity.VALID) {
                                    recompile = true;
                                }
                            }
                            if (recompile) {
                                doc = null; // recompile
                            }
                        }
                    }
                    if (doc == null) {
                        Parser parser = new Parser();
                        // call getValidity before using the stream is faster if
                        // the source is a SitemapSource
                        if (validity == null) {
                            validity = input.getValidity();
                        }
                        SourceUtil.parse(this.manager, input, parser);
                        doc = parser.getStartEvent();
                        doc.setCompileTime(validity);
                        synchronized (cache) {
                            cache.put(input.getURI(), doc);
                        }
                    }
                } catch (Exception exc) {
                    throw new SAXParseException(exc.getMessage(), ev
                            .getLocation(), exc);
                } finally {
                    resolver.release(input);
                }
                JXPathContext selectJXPath = jxpathContext;
                MyJexlContext selectJexl = jexlContext;
                if (startImport.getSelect() != null) {
                    try {
                        Object obj = ValueHelper.getValue(startImport
                                .getSelect(), jexlContext, jxpathContext);
                        selectJXPath = jxpathContextFactory.newContext(null,
                                obj);
                        selectJXPath.setVariables(variables);
                        selectJexl = new MyJexlContext(jexlContext);
                        fillContext(obj, selectJexl);
                    } catch (Exception exc) {
                        throw new SAXParseException(exc.getMessage(), ev
                                .getLocation(), exc);
                    } catch (Error err) {
                        throw new SAXParseException(err.getMessage(), ev
                                .getLocation(), new ErrorHolder(err));
                    }
                }
                try {
                    execute(consumer, selectJexl, selectJXPath, macroCall, doc
                            .getNext(), doc.getEndDocument());
                } catch (Exception exc) {
                    throw new SAXParseException(
                            "Exception occurred in imported template " + uri
                                    + ": " + exc.getMessage(),
                            ev.getLocation(), exc);
                }
                ev = startImport.getEndInstruction().getNext();
                continue;
            } else if (ev instanceof StartDocument) {
                if (((StartDocument) ev).getEndDocument() != null) {
                    // if this isn't a document fragment
                    consumer.startDocument();
                }
            } else if (ev instanceof EndDocument) {
                consumer.endDocument();
            } else if (ev instanceof ProcessingInstruction) {
                ProcessingInstruction pi = (ProcessingInstruction) ev;
                consumer.processingInstruction(pi.getTarget(), pi.getData());
            }
            ev = ev.getNext();
        }
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

    private NodeList toDOMNodeList(String elementName, StartInstruction si,
            MyJexlContext jexlContext, StartElement macroCall)
            throws SAXException {
        DOMBuilder builder = new DOMBuilder();
        builder.startDocument();
        builder.startElement(NS, elementName, elementName, EMPTY_ATTRS);
        execute(builder, jexlContext, jxpathContext, macroCall, si.getNext(),
                si.getEndInstruction());
        builder.endElement(NS, elementName, elementName);
        builder.endDocument();
        Node node = builder.getDocument().getDocumentElement();
        return node.getChildNodes();
    }

}