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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.javascript.fom.FOM_JavaScriptFlowHelper;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Variables;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.garbage.Processor;
import org.apache.garbage.parser.Parser;
import org.apache.garbage.tree.Tree;
import org.apache.garbage.tree.TreeException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class GarbageGenerator extends ServiceableGenerator {

    // FIXME - We should not use a static variable here: use a component instead
    protected static Map cache = new HashMap();

    protected JXPathContext jxpathContext;
    protected Source source;

    /**
     * Recyclable
     */
    public void recycle() {
        if ( this.source != null ) {
            this.resolver.release( this.source );
            this.source = null;
        }
        super.recycle();
        this.jxpathContext = null;
    }

    protected static class CacheEntry {
        Tree tree;
        // FIXME use SourceValidity!
        long compileTime;
    }

    public void setup(SourceResolver resolver, Map objectModel,
                      String src, Parameters parameters)
        throws ProcessingException, SAXException, IOException {

        super.setup(resolver, objectModel, src, parameters);
        if (src != null) {
            try {
                this.source = resolver.resolveURI(src);
            } catch (SourceException se) {
                throw SourceUtil.handle("Error during resolving of '" + src + "'.", se);
            }
            long lastMod = source.getLastModified();
            String uri = source.getURI();
            synchronized (cache) {
                CacheEntry t = (CacheEntry)cache.get(uri);
                if (t != null &&
                    lastMod > t.compileTime) {
                    cache.remove(uri);
                }
            }
        }
        Object bean = FlowHelper.getContextObject(objectModel);
        Object kont = FOM_JavaScriptFlowHelper.getFOM_WebContinuation(objectModel);
        setContext(bean, kont,
                   FOM_JavaScriptFlowHelper.getFOM_Request(objectModel),
                   FOM_JavaScriptFlowHelper.getFOM_Response(objectModel),
                   FOM_JavaScriptFlowHelper.getFOM_Session(objectModel),
                   FOM_JavaScriptFlowHelper.getFOM_Context(objectModel),
                   parameters);
    }
    
    protected void setContext(Object contextObject,
			      Object kont,
			      Object request,
			      Object response,
			      Object session,
			      Object context,
			      Parameters parameters) {
        jxpathContext = JXPathContext.newContext(contextObject);
        Variables varScope = jxpathContext.getVariables();
        varScope.declareVariable("flowContext", contextObject);
        varScope.declareVariable("continuation", kont);
        varScope.declareVariable("request", request);
        varScope.declareVariable("response", response);
        varScope.declareVariable("session", session);
        varScope.declareVariable("context", context);
        varScope.declareVariable("parameters", parameters);
    }

    public void generate() 
    throws IOException, SAXException, ProcessingException {
        try {
            CacheEntry t;
            synchronized (cache) {
                t = (CacheEntry)cache.get(source.getURI());
            }
            if (t == null) {
                t = new CacheEntry();
                t.compileTime = source.getLastModified();
                Parser parser = new Parser();
                InputSource is = new InputSource(source.getInputStream());
                is.setSystemId(source.getURI());
                t.tree = parser.parse(is);
                synchronized (cache) {
                    cache.put(source.getURI(), t);
                }
            }
            new Processor(this.xmlConsumer, this.xmlConsumer).process(t.tree, jxpathContext);
        } catch (TreeException exc) {
            throw new SAXParseException(exc.getMessage(), exc, exc);
        }
    }
}
