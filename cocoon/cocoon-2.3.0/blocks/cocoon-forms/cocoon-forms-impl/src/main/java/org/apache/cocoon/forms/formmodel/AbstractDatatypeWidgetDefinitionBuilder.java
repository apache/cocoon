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
package org.apache.cocoon.forms.formmodel;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.FormsException;
import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.datatype.SelectionList;
import org.apache.cocoon.forms.datatype.SelectionListBuilder;
import org.apache.cocoon.forms.datatype.convertor.ConversionResult;
import org.apache.cocoon.forms.event.ValueChangedListener;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.i18n.I18nUtils;
import org.apache.cocoon.processing.ProcessInfoProvider;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xmlizer.XMLizer;

import org.w3c.dom.Element;

/**
 * Abstract base class for WidgetDefinitionBuilders that build widgets that have datatypes/selection lists.
 *
 * @version $Id$
 */
public abstract class AbstractDatatypeWidgetDefinitionBuilder extends AbstractWidgetDefinitionBuilder {

    private Map selectionListBuilders;
    private String defaultSelectionListBuilder;
    private XMLizer xmlizer;
    private SourceResolver sourceResolver;
    private ProcessInfoProvider processInfoProvider; 

    protected void setupDefinition(Element widgetElement,
                                   AbstractDatatypeWidgetDefinition definition,
                                   WidgetDefinitionBuilderContext context)
    throws Exception {
        setupDefinition(widgetElement, definition, context, false);
    }

    protected void setupDefinition(Element widgetElement,
                                   AbstractDatatypeWidgetDefinition definition,
                                   WidgetDefinitionBuilderContext context,
                                   boolean isArrayType)
    throws Exception {
        definition.setProcessInfoProvider( processInfoProvider );
        definition.setSourceResolver( sourceResolver );
        definition.setXmlizer( xmlizer );
        super.setupDefinition(widgetElement, definition, context);
        // parse "label", "hint", etc.
        setDisplayData(widgetElement, definition);

        // parse "on-value-changed"
        Iterator i = buildEventListeners(widgetElement, "on-value-changed", ValueChangedListener.class).iterator();
        while (i.hasNext()) {
            definition.addValueChangedListener((ValueChangedListener)i.next());
        }

        //---- parse "datatype"
        Element datatypeElement = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, "datatype");
        if (datatypeElement != null) {
            Datatype datatype = datatypeManager.createDatatype(datatypeElement, isArrayType);

            // ---- parse "initial-value"
            Object initialValue = null;
            Element initialValueElement = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, "initial-value", false);
            if (initialValueElement != null) {
                String localeValue = DomHelper.getAttribute(initialValueElement, "locale", null);
                Locale locale = localeValue == null ? Locale.getDefault() : I18nUtils.parseLocale(localeValue);
                String value = DomHelper.getElementText(initialValueElement);
                ConversionResult result = datatype.convertFromString(value, locale);
                if (!result.isSuccessful()) {
                    throw new FormsException("Cannot parse initial value '" + value + "'.",
                                             DomHelper.getLocationObject(initialValueElement));
                }
                initialValue = result.getResult();
            }

            definition.setDatatype(datatype, initialValue);
        }


        //---- parse "selection-list"
        // FIXME: pass the manager to the definition as a side effect. Should be removed
        // when definition are managed like components.
        definition.checkMutable();

        SelectionList list = buildSelectionList(widgetElement, definition, "selection-list");
        if (list != null) {
            definition.setSelectionList(list);
        }
    }

    protected SelectionList buildSelectionList(Element widgetElement,
                                               AbstractDatatypeWidgetDefinition definition,
                                               String name)
    throws Exception {
        Element selectionListElement = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, name);

        if (selectionListElement != null && definition.getDatatype() == null) {
            throw new FormsException("A widget with a selection list requires a datatype.",
                                     DomHelper.getLocationObject(widgetElement));
        }

        if (selectionListElement == null) {
            return null;
        }

        // listType can be null, meaning we will use the default selection list
        String listType = selectionListElement.getAttribute("type");
        if ("".equals(listType)) {
            listType = defaultSelectionListBuilder;
        }

        // Get an appropriate list builder
        SelectionListBuilder builder = (SelectionListBuilder)selectionListBuilders.get(listType);
        if( builder == null) {
            throw new FormsException("Unknown selection list builder: " + listType);
        }
        return builder.build(selectionListElement, definition.getDatatype());
    }

    public void setSelectionListBuilders( Map selectionListBuilders )
    {
        this.selectionListBuilders = selectionListBuilders;
    }

    public void setXmlizer( XMLizer xmlizer )
    {
        this.xmlizer = xmlizer;
    }

    public void setSourceResolver( SourceResolver sourceResolver )
    {
        this.sourceResolver = sourceResolver;
    }

    public void setProcessInfoProvider( ProcessInfoProvider processInfoProvider )
    {
        this.processInfoProvider = processInfoProvider;
    }

    protected ProcessInfoProvider getProcessInfoProvider()
    {
        return processInfoProvider;
    }

    public void setDefaultSelectionListBuilder( String defaultSelectionListBuilder )
    {
        this.defaultSelectionListBuilder = defaultSelectionListBuilder;
    }
}
