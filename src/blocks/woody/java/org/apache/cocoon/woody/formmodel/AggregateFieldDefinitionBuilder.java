/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.woody.formmodel;

import org.w3c.dom.Element;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.datatype.ValidationRule;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.excalibur.xml.sax.XMLizable;
import org.outerj.expression.Expression;

import java.util.HashSet;

/**
 * Builds {@link AggregateFieldDefinition}s.
 */
public class AggregateFieldDefinitionBuilder extends AbstractWidgetDefinitionBuilder {
    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        AggregateFieldDefinition definition = new AggregateFieldDefinition();
        setLocation(widgetElement, definition);
        setId(widgetElement, definition);
        setDisplayData(widgetElement, definition);
//FIXME: these are currently type-related validators
//        setValidators(widgetElement, definition);

        // make childfields
        Element widgetsElement = DomHelper.getChildElement(widgetElement, Constants.WD_NS, "widgets", true);
        Element[] fieldElements = DomHelper.getChildElements(widgetsElement, Constants.WD_NS, "field");
        for (int i = 0; i < fieldElements.length; i++) {
            FieldDefinition fieldDefinition = (FieldDefinition)buildAnotherWidgetDefinition(fieldElements[i]);
            if (!String.class.isAssignableFrom(fieldDefinition.getDatatype().getTypeClass()))
                throw new Exception("An aggregatefield can only contain fields with datatype string, at " + DomHelper.getLocation(fieldElements[i]));
            definition.addWidgetDefinition(fieldDefinition);
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
        definition.setSplitPattern(pattern, patternString);

        // read split mappings
        Element[] mapElements = DomHelper.getChildElements(splitElement, Constants.WD_NS, "map");
        HashSet encounteredFieldMappings = new HashSet();
        for (int i = 0; i < mapElements.length; i++) {
            int group = DomHelper.getAttributeAsInteger(mapElements[i], "group");
            String field = DomHelper.getAttribute(mapElements[i], "field");
            // check that this field exists
            if (!definition.hasWidget(field))
                throw new Exception("Unkwon widget id \"" + field + "\"mentioned on mapping at " + DomHelper.getLocation(mapElements[i]));
            if (encounteredFieldMappings.contains(field))
                throw new Exception("It makes no sense to map two groups to the widget with id \"" + field + "\", at " + DomHelper.getLocation(mapElements[i]));
            encounteredFieldMappings.add(field);
            definition.addSplitMapping(group, field);
        }

        // read split fail message (if any)
        Element failMessageElement = DomHelper.getChildElement(splitElement, Constants.WD_NS, "failmessage");
        if (failMessageElement != null) {
            XMLizable failMessage = DomHelper.compileElementContent(failMessageElement);
            definition.setSplitFailMessage(failMessage);
        }

        // compile combine expression
        Element combineElement = DomHelper.getChildElement(widgetElement, Constants.WD_NS, "combine", true);
        String combineExprString = DomHelper.getAttribute(combineElement, "expression");
        Expression combineExpr = null;
        try {
            combineExpr = expressionManager.parse(combineExprString);
        } catch (Exception e) {
            throw new Exception("Problem with combine expression defined at " + DomHelper.getLocation(combineElement) + ": " + e.getMessage());
        }
        if (combineExpr.getResultType() != null && !String.class.isAssignableFrom(combineExpr.getResultType()))
            throw new Exception("The result of the combine expression should be a string, at " + DomHelper.getLocation(combineElement));
        definition.setCombineExpression(combineExpr);

        // add validation rules
        Element validationElement = DomHelper.getChildElement(widgetElement, Constants.WD_NS, "validation", false);
        if (validationElement != null) {
            Element[] validationRuleElements = DomHelper.getChildElements(validationElement, Constants.WD_NS);
            for (int i = 0; i < validationRuleElements.length; i++) {
                Element validationRuleElement = validationRuleElements[i];
                ValidationRule validationRule = datatypeManager.createValidationRule(validationRuleElement);
                if (!validationRule.supportsType(String.class, false))
                    throw new Exception("The validation rule for the aggregatefield " + definition.getId() + " specified at " + DomHelper.getLocation(validationRuleElement) + " does not work with strings.");
                definition.addValidationRule(validationRule);
            }
        }

        // requiredness
        boolean required = DomHelper.getAttributeAsBoolean(widgetElement, "required", false);
        definition.setRequired(required);

        return definition;
    }
}
