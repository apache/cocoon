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

import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.oro.text.regex.Pattern;

import org.outerj.expression.Expression;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The {@link WidgetDefinition} part of a AggregateField widget, see {@link AggregateField} for more information.
 *
 * @version $Id: AggregateFieldDefinition.java,v 1.1 2004/03/09 10:33:50 reinhard Exp $
 */
public class AggregateFieldDefinition extends FieldDefinition {

    /**
     * Defines expression which combines values of nested fields into this value
     */
    private Expression combineExpr;

    /**
     * Regular expression which splits this value on the values of the nested fields.
     * It is compiled into the {@link #splitPattern}.
     */
    private String splitRegexp;

    /**
     * Compiled pattern out of the {@link #splitRegexp} regular expression.
     */
    private Pattern splitPattern;

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

    /**
     *
     */
    private ContainerDefinitionDelegate container = new ContainerDefinitionDelegate(this);


    public void addWidgetDefinition(WidgetDefinition widgetDefinition) throws DuplicateIdException {
        container.addWidgetDefinition(widgetDefinition);
    }

    public boolean hasWidget(String id) {
        return container.hasWidget(id);
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

    public static class SplitMapping {
        private int group;
        private String fieldId;

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
