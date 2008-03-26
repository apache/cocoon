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
package org.apache.cocoon.forms;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.apache.avalon.framework.context.Context;
import org.apache.cocoon.core.xml.SAXParser;
import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.forms.formmodel.FormDefinition;
import org.apache.cocoon.forms.formmodel.FormDefinitionBuilder;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.util.location.LocationImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.util.Map;

/**
 * Component implementing the {@link FormManager} role.
 *
 * @version $Id$
 */
public class DefaultFormManager implements FormManager {
    // NOTE: Component is there to allow this block to run in the 2.1 branch

    private static final String PREFIX = "CocoonForms:";
    private static Log LOG = LogFactory.getLog( DefaultFormManager.class );
    protected Map widgetDefinitionBuilders;
    protected CacheManager cacheManager;
    private Context avalonContext;

    private SourceResolver sourceResolver;
    private SAXParser parser;

    //
    // Business Methods
    //

    public Form createForm(String uri) throws Exception {
        Source source = null;
        try {
            try {
                source = sourceResolver.resolveURI(uri);
            } catch (Exception e) {
                throw new FormsException("Could not resolve form definition URI.",
                                         e, new LocationImpl("[FormManager]", uri));
            }
            return createForm(source);
        } finally {
            if (source != null) {
                sourceResolver.release(source);
            }
        }
    }

    public Form createForm(Source source) throws Exception {
        FormDefinition formDefinition = createFormDefinition(source);
        Form form = (Form) formDefinition.createInstance();
        form.initialize();
        return form;
    }

    public Form createForm(Element formElement) throws Exception {
        Form form = (Form) createFormDefinition(formElement).createInstance();
        form.initialize();
        return form;
    }

    public FormDefinition createFormDefinition(String uri) throws Exception {
        Source source = null;
        try {
            try {
                source = sourceResolver.resolveURI(uri);
            } catch (Exception e) {
                throw new FormsException("Could not resolve form definition.",
                                         e, new LocationImpl("[FormManager]", uri));
            }
            return createFormDefinition(source);
        } finally {
            if (source != null) {
                sourceResolver.release(source);
            }
        }
    }

    public FormDefinition createFormDefinition(Source source) throws Exception {
        FormDefinition formDefinition = (FormDefinition) this.cacheManager.get(source, PREFIX);
        if (formDefinition != null && formDefinition.getLocalLibrary().dependenciesHaveChanged()) {
            formDefinition = null; // invalidate
        }

        if (formDefinition == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Building Form: " + source.getURI());
            }

            Document formDocument;
            try {
                InputSource inputSource = new InputSource(source.getInputStream());
                inputSource.setSystemId(source.getURI());
                formDocument = DomHelper.parse(inputSource, parser);
            } catch (Exception e) {
                throw new FormsException("Could not parse form definition.",
                                         e, new LocationImpl("[FormManager]", source.getURI()));
            }

            Element formElement = formDocument.getDocumentElement();
            formDefinition = createFormDefinition(formElement);
            this.cacheManager.set(formDefinition, source, PREFIX);
        }
        return formDefinition;
    }

    public FormDefinition createFormDefinition(Element formElement) throws Exception {
        // check that the root element is a fd:form element
        if (!FormsConstants.DEFINITION_NS.equals(formElement.getNamespaceURI()) || !formElement.getLocalName().equals("form")) {
            throw new FormsException("Expected forms definition <fd:form> element.",
                                     DomHelper.getLocationObject(formElement));
        }

        FormDefinitionBuilder builder = (FormDefinitionBuilder) widgetDefinitionBuilders.get("form");
        if (builder == null) {
            throw new Exception("Cannot find FormDefinitionBuilder 'form'");
        }
        return (FormDefinition) builder.buildWidgetDefinition(formElement);
    }


    public void setWidgetDefinitionBuilders( Map widgetDefinitionBuilders )
    {
        this.widgetDefinitionBuilders = widgetDefinitionBuilders;
    }

    public void setCacheManager( CacheManager cacheManager )
    {
        this.cacheManager = cacheManager;
    }

    public void setParser( SAXParser parser )
    {
        this.parser = parser;
    }

    public void setSourceResolver( SourceResolver sourceResolver )
    {
        this.sourceResolver = sourceResolver;
    }

    public void setAvalonContext( Context avalonContext )
    {
        this.avalonContext = avalonContext;
    }

    public Context getAvalonContext()
    {
        return avalonContext;
    }
}
