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


/**
 * Definition of a {@link CalculatedField}.
 * @version $Id$
 */
public class CalculatedFieldDefinition extends FieldDefinition {

    private CalculatedFieldAlgorithm algorithm = null;
    
    public Widget createInstance() {
        return new CalculatedField(this);
    }

    /**
     * @return Returns the algorithm.
     */
    public CalculatedFieldAlgorithm getAlgorithm() {
        return algorithm;
    }
    /**
     * @param algorithm The algorithm to set.
     */
    public void setAlgorithm(CalculatedFieldAlgorithm algorithm) {
        super.checkMutable();        
        this.algorithm = algorithm;
    }
    
}
