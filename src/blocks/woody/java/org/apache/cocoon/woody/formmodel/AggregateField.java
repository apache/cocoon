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

import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.util.I18nMessage;
import org.apache.cocoon.woody.datatype.ValidationError;
import org.apache.cocoon.woody.datatype.ValidationRule;
import org.apache.cocoon.woody.formmodel.AggregateFieldDefinition.SplitMapping;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.MatchResult;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.outerj.expression.ExpressionException;

import java.util.*;

/**
 * An aggregatedfield allows to edit the content of multiple fields through 1 textbox.
 * Hence this widget is a container widget, though its individual widgets are never rendered.
 *
 * <p>Upon submit, the value from the textbox will be split over multiple field widgets
 * using a regular expression. If this fails, this will simple give a validation error.
 *
 * <p>To validate this widget, both the validation rules of the containing widgets are
 * checked, and those of the aggregated field themselve. The validation rules of the aggregated
 * field can perform checks on the string as entered by the user (e.g. check its total length).
 *
 * <p>When getting the value from this widget (e.g. when generating its XML representation),
 * the values of the individual child widgets are combined again into one string. This is done
 * using an expression.
 *
 * <p>Currently the child widgets should always be field widgets whose datatype is string.
 *
 */
public class AggregateField extends AbstractWidget {
    private AggregateFieldDefinition definition;
    private String enteredValue;
    private List fields = new ArrayList();
    private Map fieldsById = new HashMap();
    private ValidationError validationError;

    protected AggregateField(AggregateFieldDefinition definition) {
        this.definition = definition;
        setLocation(definition.getLocation());
    }

    protected void addField(Field field) {
        fields.add(field);
        field.setParent(this);
        fieldsById.put(field.getId(), field);
    }

    public String getId() {
        return definition.getId();
    }

    public void readFromRequest(FormContext formContext) {
        String newEnteredValue = formContext.getRequest().getParameter(getFullyQualifiedId());
        
        // whitespace & empty field handling
        if (newEnteredValue != null) {
            // TODO make whitespace behaviour configurable !!
            newEnteredValue.trim();

            if (newEnteredValue.length() == 0) {
                newEnteredValue = null;
            }
        }

        // Only convert if the text value actually changed. Otherwise, keep the old value
        // and/or the old validation error (allows to keep errors when clicking on actions)
        if (!(newEnteredValue == null ? "" : newEnteredValue).equals((enteredValue == null ? "" : enteredValue))) {
            
            enteredValue = newEnteredValue;
            validationError = null;
    
            if (enteredValue != null) {
                // try to split it
                PatternMatcher matcher = new Perl5Matcher();
                if (matcher.matches(enteredValue, definition.getSplitPattern())) {
                    MatchResult matchResult = matcher.getMatch();
                    Iterator iterator = definition.getSplitMappingsIterator();
                    while (iterator.hasNext()) {
                        SplitMapping splitMapping = (SplitMapping)iterator.next();
                        String result = matchResult.group(splitMapping.getGroup());
                        // Since we know the fields are guaranteed to have a string datatype, we
                        // can set the value immediately, instead of going to the readFromRequest
                        // (which would also require us to create wrapper FormContext and Request
                        // objects)
                        ((Field)fieldsById.get(splitMapping.getFieldId())).setValue(result);
                    }
                } else {
                    // set values of the fields to null
                    Iterator fieldsIt = fields.iterator();
                    while (fieldsIt.hasNext()) {
                        Field field = (Field)fieldsIt.next();
                        field.setValue(null);
                    }
                }
            }
        }
    }

    /**
     * Always returns a String for this widget (or null).
     */
    public Object getValue() {
        if (fieldsHaveValues()) {
            String value;
            try {
                value = (String)definition.getCombineExpression().evaluate(new ExpressionContextImpl(this, true));
            } catch (ExpressionException e) {
                return "#ERROR evaluating combine expression: " + e.getMessage();
            } catch (ClassCastException e) {
                return "#ERROR evaluating combine expression: result was not a string";
            }
            return value;
        } else {
            return enteredValue;
        }
    }

    /**
     * Returns false if their is at least one field which has no value.
     */
    private boolean fieldsHaveValues() {
        Iterator fieldsIt = fields.iterator();
        while (fieldsIt.hasNext()) {
            Field field = (Field)fieldsIt.next();
            if (field.getValue() == null)
                return false;
        }
        return true;
    }

    public boolean validate(FormContext formContext) {
        // valid unless proven otherwise
        validationError = null;

        if (enteredValue == null && isRequired()) {
            validationError = new ValidationError(new I18nMessage("general.field-required", Constants.I18N_CATALOGUE));
            return false;
        } else if (enteredValue == null)
            return true;
        else if (!fieldsHaveValues()) {
            XMLizable splitFailMessage = definition.getSplitFailMessage();
            if (splitFailMessage != null)
                validationError = new ValidationError(splitFailMessage);
            else
                validationError = new ValidationError(new I18nMessage("aggregatedfield.split-failed", new String[] { definition.getSplitRegexp()}, Constants.I18N_CATALOGUE));
            return false;
        } else {
            // validate my child fields
            Iterator fieldsIt = fields.iterator();
            while (fieldsIt.hasNext()) {
                Field field = (Field)fieldsIt.next();
                if (!field.validate(formContext)) {
                    validationError = field.getValidationError();
                    return false;
                }
            }
            // validate against my own validation rules
            Iterator validationRuleIt = definition.getValidationRuleIterator();
            ExpressionContextImpl exprCtx = new ExpressionContextImpl(this, true);
            while (validationRuleIt.hasNext()) {
                ValidationRule validationRule = (ValidationRule)validationRuleIt.next();
                validationError = validationRule.validate(enteredValue, exprCtx);
                if (validationError != null)
                    return false;
            }
        }
        return validationError == null;
    }

    public boolean isRequired() {
        return definition.isRequired();
    }

    private static final String AGGREGATEFIELD_EL = "aggregatefield";
    private static final String VALUE_EL = "value";
    private static final String VALIDATION_MSG_EL = "validation-message";


    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        AttributesImpl aggregatedFieldAttrs = new AttributesImpl();
        aggregatedFieldAttrs.addCDATAAttribute("id", getFullyQualifiedId());
        aggregatedFieldAttrs.addCDATAAttribute("required", String.valueOf(definition.isRequired()));

        contentHandler.startElement(Constants.WI_NS, AGGREGATEFIELD_EL, Constants.WI_PREFIX_COLON + AGGREGATEFIELD_EL, aggregatedFieldAttrs);

        String value = (String)getValue();
        if (value != null) {
            contentHandler.startElement(Constants.WI_NS, VALUE_EL, Constants.WI_PREFIX_COLON + VALUE_EL, Constants.EMPTY_ATTRS);
            String stringValue; stringValue = value;
            contentHandler.characters(stringValue.toCharArray(), 0, stringValue.length());
            contentHandler.endElement(Constants.WI_NS, VALUE_EL, Constants.WI_PREFIX_COLON + VALUE_EL);
        }

        // validation message element: only present if the value is not valid
        if (validationError != null) {
            contentHandler.startElement(Constants.WI_NS, VALIDATION_MSG_EL, Constants.WI_PREFIX_COLON + VALIDATION_MSG_EL, Constants.EMPTY_ATTRS);
            validationError.generateSaxFragment(contentHandler);
            contentHandler.endElement(Constants.WI_NS, VALIDATION_MSG_EL, Constants.WI_PREFIX_COLON + VALIDATION_MSG_EL);
        }

        // generate label, help, hint, etc.
        definition.generateDisplayData(contentHandler);

        contentHandler.endElement(Constants.WI_NS, AGGREGATEFIELD_EL, Constants.WI_PREFIX_COLON + AGGREGATEFIELD_EL);
    }

    public void generateLabel(ContentHandler contentHandler) throws SAXException {
        definition.generateLabel(contentHandler);
    }

    public Widget getWidget(String id) {
        return (Widget)fieldsById.get(id);
    }
}
