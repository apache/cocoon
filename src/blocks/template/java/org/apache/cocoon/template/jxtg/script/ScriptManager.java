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
package org.apache.cocoon.template.jxtg.script;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.template.jxtg.script.event.StartDocument;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public class ScriptManager {
    private ServiceManager serviceManager;
    private final Map cache = new HashMap();

    public ScriptManager() {
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public void setServiceManager(ServiceManager manager) {
        this.serviceManager = manager;
    }

    private Map getCache() {
        return cache;
    }

    public StartDocument resolveTemplate(String uri) throws SAXParseException,
            ProcessingException {
        return resolveTemplate(uri, null);
    }

    public StartDocument resolveTemplate(String uri, Locator location)
            throws SAXParseException, ProcessingException {
        Source input = null;
        StartDocument doc = null;
        ServiceManager manager = getServiceManager();
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) getServiceManager().lookup(
                    SourceResolver.ROLE);
            input = resolver.resolveURI(uri);
            SourceValidity validity = null;
            synchronized (getCache()) {
                doc = (StartDocument) getCache().get(input.getURI());
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
            }

            if (doc == null) {
                Parser parser = new Parser();
                // call getValidity before using the stream is faster if
                // the source is a SitemapSource
                if (validity == null) {
                    validity = input.getValidity();
                }
                SourceUtil.parse(manager, input, parser);
                doc = parser.getStartEvent();
                doc.setUri(input.getURI());
                doc.setSourceValidity(validity);
                synchronized (getCache()) {
                    getCache().put(input.getURI(), doc);
                }
            }
        } catch (SourceException se) {
            throw SourceUtil.handle("Error during resolving of '" + uri + "'.",
                    se);
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