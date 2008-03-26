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
package org.apache.cocoon.template;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.excalibur.source.SourceValidity;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.core.xml.SAXParser;
import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.el.parsing.Subst;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.objectmodel.helper.ParametersMap;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.environment.JXCacheKey;
import org.apache.cocoon.template.environment.JXSourceValidity;
import org.apache.cocoon.template.script.Invoker;
import org.apache.cocoon.template.script.ScriptManager;
import org.apache.cocoon.template.script.event.Event;
import org.apache.cocoon.template.script.event.StartDocument;
import org.apache.cocoon.template.xml.AttributeAwareXMLConsumerImpl;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.xml.RedundantNamespacesFilter;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.util.NamespacesTable;

import org.xml.sax.SAXException;

/**
 * Provides a generic page template with embedded JSTL and
 * XPath expression substitution to access data sent by
 * Cocoon Flowscripts.
 *
 * @cocoon.sitemap.component.documentation
 * Provides a generic page template with embedded JSTL and
 * XPath expression substitution to access data sent by
 * Cocoon Flowscripts.
 * @cocoon.sitemap.component.name jx
 * @cocoon.sitemap.component.label content
 * @cocoon.sitemap.component.documentation.caching Supported.
 * Caching key and validity should be supplied by jx template.
 * @cocoon.sitemap.component.pooling.max 16
 *
 * @version $Id$
 */
public class JXTemplateGenerator extends AbstractLogEnabled
                                 implements Generator, CacheableProcessingComponent {

    /** The namespace used by this generator */
    public final static String NS = "http://apache.org/cocoon/templates/jx/1.0";

    public final static String CACHE_KEY = "cache-key";
    public final static String VALIDITY = "cache-validity";

    protected ObjectModel objectModel;
    protected NamespacesTable namespaces;
    protected ScriptManager scriptManager;

    protected StartDocument startDocument;
    protected Map definitions;
    protected SAXParser saxParser;

    protected XMLConsumer consumer;

    protected Parameters parameters;

    protected String src;


    public ScriptManager getScriptManager() {
        return scriptManager;
    }

    public void setScriptManager(ScriptManager scriptManager) {
        this.scriptManager = scriptManager;
    }

    public SAXParser getSaxParser() {
        return saxParser;
    }

    public void setSaxParser(SAXParser saxParser) {
        this.saxParser = saxParser;
    }

    public ObjectModel getObjectModel() {
        return objectModel;
    }

    public void setObjectModel(ObjectModel objectModel) {
        this.objectModel = objectModel;
    }

    public void setConsumer(XMLConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * @see org.apache.cocoon.generation.AbstractGenerator#setup(SourceResolver, Map, String, Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        this.parameters = parameters;
        this.src = src;

        // src can be null if this generator is triggered by the jxt transformer (through the
        // TransformerAdapter)
        if (src != null) {
            this.startDocument = scriptManager.resolveTemplate(src);
        }

        this.namespaces = new NamespacesTable();
        this.definitions = new HashMap();
    }

    /**
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public void generate() throws IOException, SAXException, ProcessingException {
        performGeneration(this.startDocument, null);

        // no need to reference compiled script anymore
        this.startDocument = null;
    }

    public void performGeneration(Event startEvent, Event endEvent) throws SAXException {
        objectModel.markLocalContext();

        objectModel.putAt(ObjectModel.PARAMETERS_PATH, new ParametersMap(parameters));
        objectModel.put(ObjectModel.NAMESPACE, namespaces);
        XMLConsumer consumer = new AttributeAwareXMLConsumerImpl(new RedundantNamespacesFilter(this.consumer));
        objectModel.putAt("cocoon/consumer", consumer);

        Invoker.execute(consumer, this.objectModel, new ExecutionContext(this.definitions, this.scriptManager,
                this.saxParser), null, namespaces, startEvent, null);

        objectModel.cleanupLocalContext();
    }

    /**
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getKey()
     */
    public Serializable getKey() {
        Subst cacheKeyExpr = (Subst) this.startDocument.getTemplateProperty(JXTemplateGenerator.CACHE_KEY);
        if (cacheKeyExpr == null) {
            return null;
        }

        try {
            final Serializable templateKey = (Serializable) cacheKeyExpr.getValue(this.objectModel);
            if (templateKey != null) {
                return new JXCacheKey(this.startDocument.getUri(), templateKey);
            }
        } catch (Exception e) {
            getLogger().error("error evaluating cache key", e);
        }

        return null;
    }

    /**
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getValidity()
     */
    public SourceValidity getValidity() {
        Subst validityExpr = (Subst) this.startDocument.getTemplateProperty(JXTemplateGenerator.VALIDITY);
        if (validityExpr == null) {
            return null;
        }

        try {
            final SourceValidity sourceValidity = this.startDocument.getSourceValidity();
            final SourceValidity templateValidity = (SourceValidity) validityExpr.getValue(this.objectModel);
            if (sourceValidity != null && templateValidity != null) {
                return new JXSourceValidity(sourceValidity, templateValidity);
            }
        } catch (Exception e) {
            getLogger().error("error evaluating cache validity", e);
        }

        return null;
    }
}
