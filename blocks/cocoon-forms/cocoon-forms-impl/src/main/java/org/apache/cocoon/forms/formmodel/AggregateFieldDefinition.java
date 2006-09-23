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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.excalibur.xml.sax.XMLizable;

import org.apache.cocoon.forms.FormsException;
import org.apache.cocoon.forms.FormsRuntimeException;

import org.apache.oro.text.regex.Pattern;
import org.outerj.expression.Expression;

/**
 * The {@link WidgetDefinition} part of a AggregateField widget, see {@link AggregateField}
 * for more information.
 *
 * @version $Id$
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
     * Message to be displayed when the {@link #setSplitPattern(Pattern, String) splitPattern}
     * does not match what the user entered. Optional.
     */
    protected XMLizable splitFailMessage;

    /**
     * List containing instances of {@link SplitMapping}, i.e. the mapping between
     * a group (paren) from the regular expression and corresponding field id.
     */
    private List splitMappings = new ArrayList();

    /**
     * Set containing widgets mapped to groups to find out if a specific widget has been mapped already
     */
    private Set mappedFields = new HashSet();

    /**
     *
     */
    private WidgetDefinitionList container = new WidgetDefinitionList(this);

    /**
     * initialize this definition with the other, sort of like a copy constructor
     */
    public void initializeFrom(WidgetDefinition definition) throws Exception {
        super.initializeFrom(definition);

        if (!(definition instanceof AggregateFieldDefinition)) {
            throw new FormsException("Ancestor definition " + definition.getClass().getName() + " is not an AggregateFieldDefinition.",
                                     getLocation());
        }

        AggregateFieldDefinition other = (AggregateFieldDefinition) definition;

        this.combineExpr = other.combineExpr;
        this.splitRegexp = other.splitRegexp;
        this.splitPattern = other.splitPattern;
        this.splitFailMessage = other.splitFailMessage;

        Iterator defs = other.container.getWidgetDefinitions().iterator();
        while (defs.hasNext()) {
            container.addWidgetDefinition((WidgetDefinition) defs.next());
        }

        Collections.copy(this.splitMappings, other.splitMappings);

        Iterator fields = other.mappedFields.iterator();
        while (fields.hasNext()) {
            this.mappedFields.add(fields.next());
        }
    }

    public void addWidgetDefinition(WidgetDefinition widgetDefinition) throws DuplicateIdException {
        checkMutable();
        container.addWidgetDefinition(widgetDefinition);
    }

    /**
     * checks completeness of this definition
     */
    public void checkCompleteness() throws IncompletenessException {
        super.checkCompleteness();

        if (this.container.size() == 0) {
            throw new IncompletenessException("Aggregate field '" + getId() + "' doesn't have any child widgets.", this);
        }

        if (this.combineExpr == null) {
            throw new IncompletenessException("Aggregate field '" + getId() + "' requires combine expression.", this);
        }

        if (this.splitPattern == null) {
            throw new IncompletenessException("Aggregate field '" + getId() + "' requires split regular expression.", this);
        }

        if (this.splitMappings.size() == 0) {
            throw new IncompletenessException("Aggregate field '" + getId() + "' requires at least one group to field mapping.", this);
        }

        // now check children's completeness
        Iterator i = container.getWidgetDefinitions().iterator();
        while (i.hasNext()) {
            ((WidgetDefinition) i.next()).checkCompleteness();
        }
    }

    public boolean hasWidget(String id) {
        return container.hasWidget(id);
    }

    protected void setCombineExpression(Expression expression) {
        checkMutable();
        combineExpr = expression;
    }

    public Expression getCombineExpression() {
        return combineExpr;
    }

    protected void setSplitPattern(Pattern pattern, String regexp) {
        checkMutable();
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
        checkMutable();
        this.splitFailMessage = splitFailMessage;
    }

    protected void addSplitMapping(int group, String fieldId) {
        checkMutable();

        if (mappedFields.contains(fieldId)) {
            throw new FormsRuntimeException("Field '" + fieldId + "' is already mapped to another group.",
                                            getLocation());
        }

        mappedFields.add(fieldId);

        splitMappings.add(new SplitMapping(group, fieldId));
    }

    public Iterator getSplitMappingsIterator() {
        return splitMappings.iterator();
    }

    public Widget createInstance() {
        AggregateField aggregateField = new AggregateField(this);

        Iterator i = container.getWidgetDefinitions().iterator();
        while (i.hasNext()) {
            FieldDefinition fieldDefinition = (FieldDefinition) i.next();
            aggregateField.addField((Field) fieldDefinition.createInstance());
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
