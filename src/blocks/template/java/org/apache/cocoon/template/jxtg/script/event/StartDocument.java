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
package org.apache.cocoon.template.jxtg.script.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.Locator;

public class StartDocument extends Event {
    public StartDocument(Locator location) {
        super(location);
        templateProperties = new HashMap();
    }

    private SourceValidity sourceValidity;
    private String uri;
    private EndDocument endDocument; // null if document fragment
    private Map templateProperties;

    public EndDocument getEndDocument() {
        return endDocument;
    }

    public void setEndDocument(EndDocument endDoc) {
        this.endDocument = endDoc;
    }

    public Map getTemplateProperties() {
        return templateProperties;
    }

    public String getUri() {
        return this.uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @param sourceValidity
     *            The compileTime to set.
     */
    public void setSourceValidity(SourceValidity sourceValidity) {
        this.sourceValidity = sourceValidity;
    }

    /**
     * @return Returns the compileTime.
     */
    public SourceValidity getSourceValidity() {
        return sourceValidity;
    }

    /**
     * @param validity
     * @return
     */
    public Object getTemplateProperty(String name) {
        return getTemplateProperties().get(name);
    }
}