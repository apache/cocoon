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

import org.apache.excalibur.xml.sax.XMLizable;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.FormsException;
import org.apache.cocoon.forms.util.DomHelper;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.outerj.expression.Expression;
import org.w3c.dom.Element;

/**
 * Builds {@link AggregateFieldDefinition}s.
 *
 * @version $Id$
 */
public class AggregateFieldDefinitionBuilder extends FieldDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element widgetElement, WidgetDefinitionBuilderContext context)
    throws Exception {
        AggregateFieldDefinition definition = new AggregateFieldDefinition();
        setupDefinition(widgetElement, definition, context);

        definition.makeImmutable();
        return definition;
    }

    protected void setupDefinition(Element widgetElement, AggregateFieldDefinition definition, WidgetDefinitionBuilderContext context)
    throws Exception {
        // parse the field definition
        super.setupDefinition(widgetElement, definition, context);

        // make children fields
        Element widgetsElement = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, "widgets", true);
        Element[] fieldElements = DomHelper.getChildElements(widgetsElement, FormsConstants.DEFINITION_NS, "field");
        for (int i = 0; i < fieldElements.length; i++) {
            FieldDefinition fd = (FieldDefinition) buildAnotherWidgetDefinition(fieldElements[i], context);
            definition.addWidgetDefinition(fd);
        }

        // compile splitpattern
        Element splitElement = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, "split", true);
        if (splitElement != null) {
            String patternString = DomHelper.getAttribute(splitElement, "pattern");
            Perl5Compiler compiler = new Perl5Compiler();
            Pattern pattern;
            try {
                pattern = compiler.compile(patternString, Perl5Compiler.READ_ONLY_MASK);
            } catch (MalformedPatternException e) {
                throw new FormsException("Invalid regular expression '" + patternString + "'.",
                                         e, DomHelper.getLocationObject(splitElement));
            }
            definition.setSplitPattern(pattern, patternString);
        }

        // read split mappings
        Element[] mapElements = DomHelper.getChildElements(splitElement, FormsConstants.DEFINITION_NS, "map");
        for (int i = 0; i < mapElements.length; i++) {
            int group = DomHelper.getAttributeAsInteger(mapElements[i], "group");
            String field = DomHelper.getAttribute(mapElements[i], "field");
            // check that this field exists
            if (!definition.hasWidget(field)) {
                throw new FormsException("Unknown widget id '" + field + "' referenced.",
                                         DomHelper.getLocationObject(mapElements[i]));
            }

            try {
                definition.addSplitMapping(group, field);
            } catch(RuntimeException e) {
                throw new FormsException("Two groups are mapped to the same widget id '" + field + "'.",
                                         DomHelper.getLocationObject(mapElements[i]));
            }
        }

        // read split fail message (if any)
        Element failMessageElement = DomHelper.getChildElement(splitElement, FormsConstants.DEFINITION_NS, "failmessage");
        if (failMessageElement != null) {
            XMLizable failMessage = DomHelper.compileElementContent(failMessageElement);
            definition.setSplitFailMessage(failMessage);
        }

        // compile combine expression
        Element combineElement = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, "combine", true);
        if(combineElement!=null) {
            String combineExprString = DomHelper.getAttribute(combineElement, "expression");
            Expression combineExpr;
            try {
                combineExpr = expressionManager.parse(combineExprString);
            } catch (Exception e) {
                throw new FormsException("Invalid combine expression '" + combineExprString + "'.",
                                         e, DomHelper.getLocationObject(combineElement));
            }
            Class clazz = definition.getDatatype().getTypeClass();
            if (combineExpr.getResultType() != null && !clazz.isAssignableFrom(combineExpr.getResultType())) {
                throw new FormsException("The result of the combine expression should be '" + clazz.getName() + "', not '" + combineExpr.getResultType().getName() + "'.",
                                         DomHelper.getLocationObject(combineElement));
            }
            definition.setCombineExpression(combineExpr);
        }
    }
}
