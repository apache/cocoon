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

import org.outerj.expression.Expression;
import org.apache.oro.text.regex.Pattern;
import org.apache.cocoon.woody.datatype.ValidationRule;
import org.apache.excalibur.xml.sax.XMLizable;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * The {@link WidgetDefinition} part of a AggregateField widget, see {@link AggregateField} for more information.
 * 
 * @version $Id: AggregateFieldDefinition.java,v 1.6 2004/02/11 10:43:30 antonio Exp $
 */
public class AggregateFieldDefinition extends AbstractWidgetDefinition {
    private Expression combineExpr;
    /**
     * Compiled split pattern.
     */
    private Pattern splitPattern;
    /**
     * The original regexp expression from which the {@link #splitPattern} was compiled,
     * used purely for informational purposes.
     */
    private String splitRegexp;
    /**
     * Message to be displayed when the {@link #splitPattern} does not match what the
     * user entered. Optional.
     */
    protected XMLizable splitFailMessage;
    /**
     * List containing instances of {@link #splitMappings}, i.e. the mapping between
     * a group (paren) from the regular expression and corresponding field id.
     */
    private List splitMappings = new ArrayList();
    private ContainerDefinitionDelegate container = new ContainerDefinitionDelegate(this);
    /**
     * Validation rules to be applied to the not-splitted value.
     */
    private List validationRules = new ArrayList();
    protected boolean required = false;

    public void addWidgetDefinition(WidgetDefinition widgetDefinition) throws DuplicateIdException {
        container.addWidgetDefinition(widgetDefinition);
    }

    public boolean hasWidget(String id) {
        return container.hasWidget(id);
    }

    protected void addValidationRule(ValidationRule validationRule) {
        validationRules.add(validationRule);
    }

    public Iterator getValidationRuleIterator() {
        return validationRules.iterator();
    }

    protected void setCombineExpression(Expression expression) {
        combineExpr = expression;
    }

    public Expression getCombineExpression() {
        return combineExpr;
    }

    protected void setSplitPattern(Pattern pattern, String regexp) {
        this.splitPattern = pattern;
        this.splitRegexp = regexp;
    }

    public Pattern getSplitPattern() {
        return splitPattern;
    }

    public String getSplitRegexp() {
        return splitRegexp;
    }

    public XMLizable getSplitFailMessage() {
        return splitFailMessage;
    }

    protected void setSplitFailMessage(XMLizable splitFailMessage) {
        this.splitFailMessage = splitFailMessage;
    }

    protected void addSplitMapping(int group, String fieldId) {
        splitMappings.add(new SplitMapping(group, fieldId));
    }

    public Iterator getSplitMappingsIterator() {
        return splitMappings.iterator();
    }

    public Widget createInstance() {
        AggregateField aggregateField = new AggregateField(this);

        Iterator fieldDefinitionIt = container.getWidgetDefinitions().iterator();
        while (fieldDefinitionIt.hasNext()) {
            FieldDefinition fieldDefinition = (FieldDefinition)fieldDefinitionIt.next();
            aggregateField.addField((Field)fieldDefinition.createInstance());
        }

        return aggregateField;
    }

    public boolean isRequired() {
        return required;
    }

    protected void setRequired(boolean required) {
        this.required = required;
    }

    public static class SplitMapping
    {
        int group;
        String fieldId;

        public SplitMapping(int group, String fieldId) {
            this.group = group;
            this.fieldId = fieldId;
        }

        public int getGroup() {
            return group;
        }

        public String getFieldId() {
            return fieldId;
        }
    }
}
