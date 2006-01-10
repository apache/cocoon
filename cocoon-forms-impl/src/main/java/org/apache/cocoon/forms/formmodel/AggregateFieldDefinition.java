/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The {@link WidgetDefinition} part of a AggregateField widget, see {@link AggregateField} for more information.
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
    	
    	if(definition instanceof AggregateFieldDefinition) {
    		AggregateFieldDefinition other = (AggregateFieldDefinition)definition;
    		
    		this.combineExpr = other.combineExpr;
    		this.splitRegexp = other.splitRegexp;
    		this.splitPattern = other.splitPattern;
    		this.splitFailMessage = other.splitFailMessage;
    		
    		Iterator defs = other.container.getWidgetDefinitions().iterator();
    		while(defs.hasNext()) {
    			container.addWidgetDefinition((WidgetDefinition)defs.next());
    		}
    		
    		Collections.copy(this.splitMappings,other.splitMappings);
    		
    		Iterator fields = other.mappedFields.iterator();
    		while(fields.hasNext()) {
    			this.mappedFields.add(fields.next());
    		}
    		
    	} else {
    		throw new Exception("Definition to inherit from is not of the right type! (at "+getLocation()+")");
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
    	
    	if(this.container.size()==0)
    		throw new IncompletenessException("AggregateField doesn't have any child widgets!",this);
    	
    	if(this.combineExpr==null)
    		throw new IncompletenessException("AggregateField requires a combine expression!",this);
    	
    	if(this.splitPattern==null)
    		throw new IncompletenessException("AggregateField requires a split regular expression!",this);
    	
    	if(this.splitMappings.size()==0)
    		throw new IncompletenessException("AggregateField requires at least one group to field mapping!",this);
    	
    	// now check children's completeness
    	List defs = container.getWidgetDefinitions();
    	Iterator it = defs.iterator();
    	while(it.hasNext()) {
    		((WidgetDefinition)it.next()).checkCompleteness();
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
        
        if(mappedFields.contains(fieldId))
        	throw new RuntimeException("Field '"+fieldId+"' is already mapped to another group!");
        
        mappedFields.add(fieldId);
        
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
