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

public class GarbageGenerator extends ComposerGenerator {

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
