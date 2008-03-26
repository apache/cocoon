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
package org.apache.cocoon.forms.formmodel.algorithms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.forms.formmodel.CalculatedFieldAlgorithm;

/**
 * Abstract base class for algorithms. 
 * 
 * <p> This class implements the getTriggerWidgets method and holds the triggers list.
 * </p>
 * 
 * @version $Id$
 */
public abstract class AbstractBaseAlgorithm implements CalculatedFieldAlgorithm {

    protected List triggers = new ArrayList();
    
    public Iterator getTriggerWidgets() {
        return triggers.iterator();
    }
    
    public void addTrigger(String widget) {
        this.triggers.add(widget);
    }

    public void clearTriggers() {
        this.triggers.clear();
    }

}
