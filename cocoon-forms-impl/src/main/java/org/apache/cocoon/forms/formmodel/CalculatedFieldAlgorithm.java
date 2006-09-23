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

import org.apache.cocoon.forms.datatype.Datatype;

/**
 * Common interface for an algorithm to calculate the value of a 
 * {@link org.apache.cocoon.forms.formmodel.CalculatedField}.
 * 
 * @version $Id$
 */
public interface CalculatedFieldAlgorithm {

    /**
     * Checks wether this algorithm is able to return the given datatype. For example,
     * an arithmetic algorithm like sum should check that the given datatype is a number.
     * @param dataType The target datatype.
     * @return true if this algorithm can return a compatible value, false otherwise.
     */
    public boolean isSuitableFor(Datatype dataType);
    
    /**
     * Performs the actual calculation.
     * @param form The form.
     * @param parent The parent widget of the {@link CalculatedField} widget (may be the same as form)
     * @param datatype The target datatype.
     * @return the calculated value for the {@link CalculatedField}.
     */
    public Object calculate(Form form, Widget parent, Datatype datatype);
    
    /**
     * Returns an iterator on trigger widget paths. When the value of a trigger widget changes,
     * then the {@link CalculatedField} value must be recalculated.
     * @return An iterator of Strings representing widget paths as interpreted by {@link org.apache.cocoon.forms.util.WidgetFinder}.
     */
    public Iterator getTriggerWidgets();
    
}
