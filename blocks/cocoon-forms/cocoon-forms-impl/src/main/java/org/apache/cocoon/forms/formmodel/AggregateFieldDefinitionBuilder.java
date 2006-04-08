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
package org.apache.cocoon.forms.formmodel;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.excalibur.xml.sax.XMLizable;
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

    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        AggregateFieldDefinition definition = new AggregateFieldDefinition();
        setupDefinition(widgetElement, definition);
        definition.makeImmutable();
        return definition;
    }
    
    protected void setupDefinition(Element widgetElement, AggregateFieldDefinition definition) throws Exception {
        
        // parse the field definition
        super.setupDefinition(widgetElement, definition);

        // make children fields
        Element widgetsElement = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, "widgets", true);
        Element[] fieldElements = DomHelper.getChildElements(widgetsElement, FormsConstants.DEFINITION_NS, "field");
        for (int i = 0; i < fieldElements.length; i++) {
            FieldDefinition fieldDefinition = (FieldDefinition)buildAnotherWidgetDefinition(fieldElements[i]);
            definition.addWidgetDefinition(fieldDefinition);
        }

        // compile splitpattern
        Element splitElement = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, "split", true);
        if(splitElement!=null) {
	        String patternString = DomHelper.getAttribute(splitElement, "pattern");
	        Perl5Compiler compiler = new Perl5Compiler();
	        Pattern pattern = null;
	        try {
	            pattern = compiler.compile(patternString, Perl5Compiler.READ_ONLY_MASK);
	        } catch (MalformedPatternException e) {
	            throw new Exception("Invalid regular expression at " + DomHelper.getLocation(splitElement) + ": " + e.getMessage());
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
                throw new Exception("Unknown widget id \"" + field + "\", at " +
                                    DomHelper.getLocation(mapElements[i]));
            }
            
            try {
            	definition.addSplitMapping(group, field);
            	System.out.println("Aggregate: addSplitMapping("+group+","+field+")");
            } catch(RuntimeException e) {
            	throw new Exception("Two groups are mapped to the same widget id \"" + field + "\", at " +
                        DomHelper.getLocation(mapElements[i]));
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
            Expression combineExpr = null;
            try {
                combineExpr = expressionManager.parse(combineExprString);
            } catch (Exception e) {
                throw new Exception("Problem with combine expression defined at " +
                                    DomHelper.getLocation(combineElement) + ": " + e.getMessage());
            }
            Class clazz = definition.getDatatype().getTypeClass();
            if (combineExpr.getResultType() != null && !clazz.isAssignableFrom(combineExpr.getResultType())) {
                throw new Exception("The result of the combine expression should be " + clazz.getName() + ", at " +
                                    DomHelper.getLocation(combineElement));
            }
            definition.setCombineExpression(combineExpr);
        }
    }
}
