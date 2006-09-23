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
package org.apache.cocoon.template.script;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.expression.StringTemplateParser;
import org.apache.cocoon.template.script.event.StartDocument;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.store.Store;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * @version $Id$
 */
public class DefaultScriptManager
  extends AbstractLogEnabled
  implements Serviceable, Disposable, ScriptManager, ThreadSafe {

    private ServiceManager manager;
    private final static String JX_STORE_PREFIX = "jxtg:";
    private Store store;
    private InstructionFactory instructionFactory;
    private ServiceSelector stringTemplateParserSelector;
    private StringTemplateParser stringTemplateParser;
    private String stringTemplateParserName = "jxtg";

    public DefaultScriptManager() {
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.store = (Store) this.manager.lookup(Store.TRANSIENT_STORE);
        this.instructionFactory = (InstructionFactory) this.manager.lookup(InstructionFactory.ROLE);
        this.stringTemplateParserSelector = (ServiceSelector) this.manager.lookup(StringTemplateParser.ROLE
                + "Selector");
        this.stringTemplateParser = (StringTemplateParser) this.stringTemplateParserSelector
                .select(this.stringTemplateParserName);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.store);
            this.manager.release(this.instructionFactory);
            if ( this.stringTemplateParserSelector != null ) {
                this.stringTemplateParserSelector.release(this.stringTemplateParser);
                this.manager.release(this.stringTemplateParserSelector);
                this.stringTemplateParserSelector = null;
                this.stringTemplateParser = null;
            }
            this.store = null;
            this.instructionFactory = null;
            this.manager = null;
        }
    }

    private Store getStore() {
        return store;
    }

    public StartDocument resolveTemplate(String uri) throws SAXParseException, ProcessingException {
        return resolveTemplate(uri, null);
    }

    public StartDocument resolveTemplate(String uri, Locator location) throws SAXParseException, ProcessingException {
        Source input = null;
        StartDocument doc = null;
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            input = resolver.resolveURI(uri);
            SourceValidity validity = null;

            String storeUri = JX_STORE_PREFIX + input.getURI();
            doc = (StartDocument) getStore().get(storeUri);

            // TODO: why was this previously in synchronized( getCache() )?
            if (doc != null) {
                boolean recompile = false;
                if (doc.getSourceValidity() == null) {
                    recompile = true;
                } else {
                    int valid = doc.getSourceValidity().isValid();
                    if (valid == SourceValidity.UNKNOWN) {
                        validity = input.getValidity();
                        valid = doc.getSourceValidity().isValid(validity);
                    }
                    if (valid != SourceValidity.VALID) {
                        recompile = true;
                    }
                }
                if (recompile) {
                    doc = null; // recompile
                }
            }

            if (doc == null) {
                Parser parser = new Parser(new ParsingContext(this.stringTemplateParser, this.instructionFactory));
                // call getValidity before using the stream is faster if
                // the source is a SitemapSource
                if (validity == null) {
                    validity = input.getValidity();
                }
                SourceUtil.parse(manager, input, parser);
                doc = parser.getStartEvent();
                doc.setUri(input.getURI());
                doc.setSourceValidity(validity);

                getStore().store(storeUri, doc);
            }
        } catch (SourceException se) {
            throw SourceUtil.handle("Error during resolving of '" + uri + "'.", se);
        } catch (Exception exc) {
            throw new SAXParseException(exc.getMessage(), location, exc);
        } finally {
            if (input != null)
                resolver.release(input);
            if (resolver != null)
                manager.release(resolver);
        }
        return doc;
    }
}