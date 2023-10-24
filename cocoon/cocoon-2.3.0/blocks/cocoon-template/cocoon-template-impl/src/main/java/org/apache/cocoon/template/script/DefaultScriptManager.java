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

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.core.xml.SAXParser;
import org.apache.cocoon.el.parsing.StringTemplateParser;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.script.event.StartDocument;
import org.apache.cocoon.util.location.LocationUtils;
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
public class DefaultScriptManager implements ScriptManager {
    private static final String JX_STORE_PREFIX = "jxtg:";

    private Store store;
    private InstructionFactory instructionFactory;
    private StringTemplateParser stringTemplateParser;
    private SourceResolver sourceResolver;
    private SAXParser saxParser;

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public InstructionFactory getInstructionFactory() {
        return instructionFactory;
    }

    public void setInstructionFactory(InstructionFactory instructionFactory) {
        this.instructionFactory = instructionFactory;
    }

    public StringTemplateParser getStringTemplateParser() {
        return stringTemplateParser;
    }

    public void setStringTemplateParser(StringTemplateParser stringTemplateParser) {
        this.stringTemplateParser = stringTemplateParser;
    }

    public SourceResolver getSourceResolver() {
        return sourceResolver;
    }

    public void setSourceResolver(SourceResolver sourceResolver) {
        this.sourceResolver = sourceResolver;
    }

    public SAXParser getSaxParser() {
        return saxParser;
    }

    public void setSaxParser(SAXParser saxParser) {
        this.saxParser = saxParser;
    }

    public DefaultScriptManager() {
    }

    public StartDocument resolveTemplate(String uri) throws SAXParseException, ProcessingException {
        return resolveTemplate(uri, null);
    }

    public StartDocument resolveTemplate(String uri, Locator location) throws SAXParseException, ProcessingException {
        Source input = null;
        StartDocument doc = null;
        try {
            input = sourceResolver.resolveURI(uri);
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
                SourceUtil.parse(saxParser, input, parser);
                doc = parser.getStartEvent();
                doc.setUri(input.getURI());
                doc.setSourceValidity(validity);

                getStore().store(storeUri, doc);
            }
        } catch (SourceException se) {
            throw SourceUtil.handle("Error during resolving of '" + uri + "'.", se);
        } catch (ProcessingException e) {
            throw ProcessingException.throwLocated(null, e, LocationUtils.getLocation(location));
        } catch (Exception e) {
            throw new SAXParseException(e.getMessage(), location, e);
        } finally {
            if (input != null) {
                sourceResolver.release(input);
            }
        }

        return doc;
    }
}
