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
package org.apache.cocoon.woody.formmodel;

import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;

import org.outerj.expression.Expression;
import org.w3c.dom.Element;

import java.util.HashSet;

/**
 * Builds {@link AggregateFieldDefinition}s.
 *
 * @version $Id: AggregateFieldDefinitionBuilder.java,v 1.12 2004/03/09 13:53:56 reinhard Exp $
 */
public class AggregateFieldDefinitionBuilder extends FieldDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        AggregateFieldDefinition aggregateDefinition = new AggregateFieldDefinition();
        buildWidgetDefinition(aggregateDefinition, widgetElement);

        // make children fields
        Element widgetsElement = DomHelper.getChildElement(widgetElement, Constants.WD_NS, "widgets", true);
        Element[] fieldElements = DomHelper.getChildElements(widgetsElement, Constants.WD_NS, "field");
        for (int i = 0; i < fieldElements.length; i++) {
            FieldDefinition fieldDefinition = (FieldDefinition)buildAnotherWidgetDefinition(fieldElements[i]);
            aggregateDefinition.addWidgetDefinition(fieldDefinition);
        }

        // compile splitpattern
        Element splitElement = DomHelper.getChildElement(widgetElement, Constants.WD_NS, "split", true);
        String patternString = DomHelper.getAttribute(splitElement, "pattern");
        Perl5Compiler compiler = new Perl5Compiler();
        Pattern pattern = null;
        try {
            pattern = compiler.compile(patternString, Perl5Compiler.READ_ONLY_MASK);
        } catch (MalformedPatternException e) {
            throw new Exception("Invalid regular expression at " + DomHelper.getLocation(splitElement) + ": " + e.getMessage());
        }
        aggregateDefinition.setSplitPattern(pattern, patternString);

        // read split mappings
        Element[] mapElements = DomHelper.getChildElements(splitElement, Constants.WD_NS, "map");
        HashSet encounteredFieldMappings = new HashSet();
        for (int i = 0; i < mapElements.length; i++) {
            int group = DomHelper.getAttributeAsInteger(mapElements[i], "group");
            String field = DomHelper.getAttribute(mapElements[i], "field");
            // check that this field exists
            if (!aggregateDefinition.hasWidget(field)) {
                throw new Exception("Unkwon widget id \"" + field + "\", at " +
                                    DomHelper.getLocation(mapElements[i]));
            }
            if (encounteredFieldMappings.contains(field)) {
                throw new Exception("Two groups are mapped to the same widget id \"" + field + "\", at " +
                                    DomHelper.getLocation(mapElements[i]));
            }
            encounteredFieldMappings.add(field);
            aggregateDefinition.addSplitMapping(group, field);
        }

        // read split fail message (if any)
        Element failMessageElement = DomHelper.getChildElement(splitElement, Constants.WD_NS, "failmessage");
        if (failMessageElement != null) {
            XMLizable failMessage = DomHelper.compileElementContent(failMessageElement);
            aggregateDefinition.setSplitFailMessage(failMessage);
        }

        // compile combine expression
        Element combineElement = DomHelper.getChildElement(widgetElement, Constants.WD_NS, "combine", true);
        String combineExprString = DomHelper.getAttribute(combineElement, "expression");
        Expression combineExpr = null;
        try {
            combineExpr = expressionManager.parse(combineExprString);
        } catch (Exception e) {
            throw new Exception("Problem with combine expression defined at " +
                                DomHelper.getLocation(combineElement) + ": " + e.getMessage());
        }
        Class clazz = aggregateDefinition.getDatatype().getTypeClass();
        if (combineExpr.getResultType() != null && !clazz.isAssignableFrom(combineExpr.getResultType())) {
            throw new Exception("The result of the combine expression should be " + clazz.getName() + ", at " +
                                DomHelper.getLocation(combineElement));
        }
        aggregateDefinition.setCombineExpression(combineExpr);

        return aggregateDefinition;
    }
}
