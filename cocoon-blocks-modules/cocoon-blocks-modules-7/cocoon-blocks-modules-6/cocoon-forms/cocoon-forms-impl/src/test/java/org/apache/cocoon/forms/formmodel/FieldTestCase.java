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

import org.apache.cocoon.CocoonTestCase;
import org.apache.cocoon.environment.mock.MockRequest;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.event.ValueChangedEvent;
import org.apache.cocoon.forms.event.ValueChangedListener;
import org.w3c.dom.Document;

/**
 * Test case for CForm's Field widget
 *
 * @version $Id$
 */

public class FieldTestCase extends CocoonTestCase {
    
    public static final String VALUE_PATH = "fi:fragment/fi:field/fi:value";
    public static final String VALIDATION_PATH = "fi:fragment/fi:field/fi:validation-message";
    
    
    /**
     * Nominal test where the request data is syntactically correct and validates
     */
    public void testValueDoesParseAndValidate() throws Exception {
        Form form = WidgetTestHelper.loadForm(getManager(), this, "FieldTestCase.model.xml");
        Field field = (Field)form.getChild("intfield");
        Action button = (Action)form.getChild("action");
        MockRequest request;
        
        request = new MockRequest();
        request.addParameter("intfield", "11");
        request.addParameter("action", "pressed");
        form.process(new FormContext(request));
        
        // No parsing nor validation where performed
        Document doc = WidgetTestHelper.getWidgetFragment(field, null);
        WidgetTestHelper.assertXPathEquals("Displayed value", "11", VALUE_PATH, doc);
        WidgetTestHelper.assertXPathNotExists("Validation error", VALIDATION_PATH, doc);
        
        // Now do some parsing.
        assertEquals("Field value", new Integer(11), field.getValue());
        // And still no validation error (do not call getValidationError() as it does validate)
        doc = WidgetTestHelper.getWidgetFragment(field, null);
        WidgetTestHelper.assertXPathNotExists("Validation error", VALIDATION_PATH, doc);
        
        // Now validate
        assertTrue("Field does validate", field.validate());
        assertNull("getValidationError() null after validation", field.getValidationError());
        doc = WidgetTestHelper.getWidgetFragment(field, null);
        WidgetTestHelper.assertXPathNotExists("Validation error", VALIDATION_PATH, doc);        
    }
    
    /**
     * Request data is not syntactically correct
     */
    public void testValueDoesNotParse() throws Exception {
        Form form = WidgetTestHelper.loadForm(getManager(), this, "FieldTestCase.model.xml");
        Field field = (Field)form.getChild("intfield");
        Action button = (Action)form.getChild("action");
        MockRequest request;
        
        request = new MockRequest();
        request.addParameter("intfield", "foo");
        request.addParameter("action", "pressed");
        form.process(new FormContext(request));
        
        // No parsing nor validation where performed
        Document doc = WidgetTestHelper.getWidgetFragment(field, null);
        WidgetTestHelper.assertXPathEquals("Displayed velue", "foo", VALUE_PATH, doc);
        WidgetTestHelper.assertXPathNotExists("Validation error before parse", VALIDATION_PATH, doc);
        
        // Now do some parsing. Will return null as it's not parseable
        assertNull("Field value", field.getValue());
        // But still no validation error
        doc = WidgetTestHelper.getWidgetFragment(field, null);
        WidgetTestHelper.assertXPathEquals("Displayed value", "foo", VALUE_PATH, doc);
        WidgetTestHelper.assertXPathNotExists("Validation error after parse", VALIDATION_PATH, doc);
        
        // Now validate
        assertFalse("Field validation", field.validate());
        doc = WidgetTestHelper.getWidgetFragment(field, null);
        WidgetTestHelper.assertXPathEquals("Displayed velue", "foo", VALUE_PATH, doc);
        WidgetTestHelper.assertXPathExists("Validation not null after parse", VALIDATION_PATH, doc);
        assertNotNull("getValidationError() not null after validation", field.getValidationError());
    }
    
    /**
     * Request data is syntactically correct but doesn't validate
     */
    public void testValueDoesNotValidate() throws Exception {
        Form form = WidgetTestHelper.loadForm(getManager(), this, "FieldTestCase.model.xml");
        Field field = (Field)form.getChild("intfield");
        Action button = (Action)form.getChild("action");
        MockRequest request;
        
        request = new MockRequest();
        request.addParameter("intfield", "1");
        request.addParameter("action", "pressed");
        form.process(new FormContext(request));
        
        // No parsing nor validation where performed
        Document doc = WidgetTestHelper.getWidgetFragment(field, null);
        WidgetTestHelper.assertXPathEquals("Displayed value", "1", VALUE_PATH, doc);
        WidgetTestHelper.assertXPathNotExists("Validation error before parse", VALIDATION_PATH, doc);
        
        // Now do some parsing. Will return null although syntactically correct as it's invalid
        assertNull("Field value", field.getValue());
        // But still no validation error
        doc = WidgetTestHelper.getWidgetFragment(field, null);
        WidgetTestHelper.assertXPathNotExists("Validation error after parse", VALIDATION_PATH, doc);
        
        // Now validate
        assertFalse("Field validation", field.validate());
        doc = WidgetTestHelper.getWidgetFragment(field, null);
        WidgetTestHelper.assertXPathExists("Validation error after validation", VALIDATION_PATH, doc);
        assertNotNull("getValidationError() not null after validation", field.getValidationError());
    }
    
    /**
     * Test that a field's value is properly set by a call to setValue("") with an
     * empty string when the field is in unparsed state (there used to be a bug in
     * that case)
     */
    public void testSetEmptyValueWhenValueChangedOnRequest() throws Exception {
        Form form = WidgetTestHelper.loadForm(getManager(), this, "FieldTestCase.model.xml");
        Field field = (Field)form.getChild("stringfield");
        Action button = (Action)form.getChild("action");
        MockRequest request;
        
        // Set a value in stringfield and submit with an action
        // (no validation, thus no call to doParse())
        request = new MockRequest();
        request.addParameter("stringfield", "bar");
        request.addParameter("action", "pressed");
        form.process(new FormContext(request));
        
        // Verify submit widget, just to be sure that validation did not occur
        assertEquals("Form submit widget", button, form.getSubmitWidget());
        
        // Set the value to an empty string. In that case, a faulty test made
        // it actually ignore it when state was VALUE_UNPARSED
        field.setValue("");
        
        // Check value by various means
        Document doc = WidgetTestHelper.getWidgetFragment(field, null);
        WidgetTestHelper.assertXPathEquals("Displayed value", "", VALUE_PATH, doc);
        assertEquals("Datatype string conversion", "", field.getDatatype().convertToString(field.value, null));
        assertEquals("Field value", "", (String)field.getValue());
    }
    
    /**
     * Test that the previous field value is correctly passed to event listeners
     * even if it was not already parsed.
     */
    public void testOldValuePresentInEventEvenIfNotParsed() throws Exception {
        Form form = WidgetTestHelper.loadForm(getManager(), this, "FieldTestCase.model.xml");
        Field field = (Field)form.getChild("stringfield");
        Action button = (Action)form.getChild("action");
        MockRequest request;
        
        // Set a value on "stringfield", and submit using an action so that
        // it stays in unparsed state
        request = new MockRequest();
        request.addParameter("stringfield", "foo");
        request.addParameter("action", "pressed");
        form.process(new FormContext(request));

        // Now add an event listener that will check old an new value
        field.addValueChangedListener(new ValueChangedListener (){
            public void valueChanged(ValueChangedEvent event) {
                assertEquals("Old value", "foo", (String)event.getOldValue());
                assertEquals("New value", "bar", (String)event.getNewValue());
            }
        });
        
        // Change value to "bar", still without explicit validation
        // That will call the event listener
        request = new MockRequest();
        request.addParameter("stringfield", "bar");
        request.addParameter("button", "pressed");
        form.process(new FormContext(request));
    }
    
    /**
     * Request parameters are not read when a field is not in active state
     */
    public void testParameterNotReadWhenDisabled() throws Exception {
        Form form = WidgetTestHelper.loadForm(getManager(), this, "FieldTestCase.model.xml");
        Field field = (Field)form.getChild("stringfield");
        MockRequest request;

        // Disable the form
        form.setState(WidgetState.DISABLED);
        field.setValue("foo");
        
        request = new MockRequest();
        request.addParameter("stringfield", "bar");
        form.process(new FormContext(request));
        
        // Check that "bar" was not read
        assertEquals("foo", field.getValue());
        
        // Switch back to active and resumbit the same request
        form.setState(WidgetState.ACTIVE);
        form.process(new FormContext(request));
        
        // Should have changed now
        assertEquals("bar", field.getValue());
    }
}
