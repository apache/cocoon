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
 * @version $Id: AggregateFieldDefinitionBuilder.java,v 1.9 2004/02/29 06:07:37 vgritsenko Exp $
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
