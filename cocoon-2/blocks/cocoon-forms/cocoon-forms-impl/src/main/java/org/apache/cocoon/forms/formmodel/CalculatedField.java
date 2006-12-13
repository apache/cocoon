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

import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.datatype.convertor.ConversionResult;
import org.apache.cocoon.forms.event.RepeaterEvent;
import org.apache.cocoon.forms.event.RepeaterListener;
import org.apache.cocoon.forms.event.ValueChangedEvent;
import org.apache.cocoon.forms.event.ValueChangedListener;
import org.apache.cocoon.forms.event.ValueChangedListenerEnabled;
import org.apache.cocoon.forms.util.WidgetFinder;

import com.ibm.icu.math.BigDecimal;


/**
 * A field which calculates its value.
 * 
 * <p>A calculated field is useful to create fields containing a sum, or a percentage, or any other
 * value derived from other fields in the form.</p>
 * 
 * <p>The way the field calculates its value is determined by its 
 * {@link org.apache.cocoon.forms.formmodel.CalculatedFieldAlgorithm}.
 * The algorithm is also responsible for determining which other form widgets will trigger
 * a value calculation for this field.
 * </p>
 * 
 * @version $Id$
 */
public class CalculatedField extends Field {

//    private CalculatedFieldDefinition definition;
    private CalculatedFieldAlgorithm algorithm = null;
        
    private WidgetFinder finder = null;
    private RecalculateValueListener mockListener = new RecalculateValueListener();
    
    private boolean needRecaulculate = false;
//    private boolean initialized = false;
    private boolean calculating = false;

    
    /**
     * @param definition
     */
    protected CalculatedField(CalculatedFieldDefinition definition) {
        super(definition);
        
//        this.definition = definition;
        this.algorithm = definition.getAlgorithm();
    }
    
    public void initialize() {
        super.initialize();
        Iterator triggers = this.algorithm.getTriggerWidgets();
        this.finder = new WidgetFinder(this.getParent(), triggers, true);
        this.finder.addRepeaterListener(new InstallHandlersListener());
        installHandlers();
        
//        this.initialized = true;
    }
    
    /**
     * Installs handlers on other widgets. This both forces other widget to
     * submit the form when their values change, and also gives this field
     * a good optimization on calls to its algorithm. 
     */
    protected void installHandlers() {
        List adds = this.finder.getNewAdditions();
        for (Iterator iter = adds.iterator(); iter.hasNext();) {
            Widget widget = (Widget) iter.next();
            if (widget instanceof ValueChangedListenerEnabled) {
                ((ValueChangedListenerEnabled)widget).addValueChangedListener(mockListener);
            }
        }
    }
    
    protected void readFromRequest(String newEnteredValue) {
        // Never read a calculated field from request.
    }

    public Object getValue() {
        // Need to calculate if the following is true.
        //  - We are not already calculating (to avoid stack overflow)
        //  - We need to recaulculate.
        if (!calculating && needRecaulculate) {
            calculating = true;
            try {
                super.setValue(recalculate());
            } finally {
                calculating = false;
            }
        }
        return super.getValue();
    }
    
    /**
     * Calls the algorithm to perform a recaulculation.
     * @return The calculated value for this field.
     */
    protected Object recalculate() {
        Object ret = this.algorithm.calculate(this.getForm(), this.getParent(), this.getDatatype());
        needRecaulculate = false;
        try {
            ret = convert(ret, this.getDatatype());
        } catch (Exception e) {
            // FIXME : log the conversion error
        }
        return ret;
    }
    
    /**
     * Tries to convert the return value of the algorithm to the right value for this field datatype.
     * @param ret The return value fo the algorithm.
     * @param datatype The target datatype.
     * @return A converted value, or the given ret value if no conversion was possible.
     */
    protected Object convert(Object ret, Datatype datatype) throws Exception {
        // First try object to object conversion
        Class target = datatype.getTypeClass(); 
        if (ret instanceof Number) {
            // Try to convert the number back to what expected
            Number number = (Number)ret; 
            if (target.equals(BigDecimal.class)) {
                return number;
            } else if (target.equals(Double.class)) {
                ret = new Double(number.doubleValue());
            } else if (target.equals(Float.class)) {
                ret = new Float(number.floatValue());
            } else if (target.equals(Integer.class)) {
                ret = new Integer(number.intValue());
            } else if (target.equals(Long.class)) {
                ret = new Long(number.longValue());
            } else if (target.equals(String.class)) {
                ret = number.toString();
            }
            return ret;
        } else if (ret instanceof String) {
            if (Number.class.isAssignableFrom(target)) {
                // Try to build a new number parsing the string.
                ret = target.getConstructor(new Class[] { String.class }).newInstance(new Object[] { ret });
            }
            return ret;
        }
        // Finally try to use the convertor
        ConversionResult result = this.getDatatype().convertFromString(ret.toString(), getForm().getLocale());
        if (result.isSuccessful()) {
            ret = result.getResult();
        }
        return ret;
    }
    
    
    /**
     * This listener is added to trigger fields, so that we know when they have been modified AND they are
     * automatically submitted.
     */
    class RecalculateValueListener implements ValueChangedListener {
        public void valueChanged(ValueChangedEvent event) {
            needRecaulculate = true;
            getValue();
        }
    }

    /**
     * This listener is installed on the WidgetFinder to know when some repeater
     * involved in our calculations gets modified.
     */
    class InstallHandlersListener implements RepeaterListener {
        public void repeaterModified(RepeaterEvent event) {
            needRecaulculate = true;
            installHandlers();
            getValue();
        }
    }
    
    /**
     * @return Returns the algorithm.
     */
    public CalculatedFieldAlgorithm getAlgorithm() {
        return algorithm;
    }
}
