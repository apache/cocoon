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

import java.util.StringTokenizer;

import org.apache.cocoon.forms.formmodel.CalculatedFieldAlgorithmBuilder;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Abstract builder for {@link org.apache.cocoon.forms.formmodel.algorithms.AbstractBaseAlgorithm}
 * subclasses.
 * 
 * <p>
 * This class parses the default triggers attribute, containing a comma separated list of widget paths
 * as defined in {@link org.apache.cocoon.forms.util.WidgetFinder}. It also calls the LifecycleHelper
 * so that algorithms gets their logger and context.
 * </p>
 * @version $Id$
 */
public abstract class AbstractBaseAlgorithmBuilder implements CalculatedFieldAlgorithmBuilder {

    protected void setup(Element algorithmElement, AbstractBaseAlgorithm algorithm) throws Exception {
        setupTriggers(algorithmElement, algorithm);
    }

    protected void setupTriggers(Element algorithmElement, AbstractBaseAlgorithm algorithm) throws Exception {
        String fields = DomHelper.getAttribute(algorithmElement, "triggers", null);
        if (fields != null) setupTriggers(fields, algorithm);        
    }
    
    protected void setupTriggers(String fields, AbstractBaseAlgorithm algorithm) {
        algorithm.clearTriggers();
        StringTokenizer stok = new StringTokenizer(fields, ", ");
        while (stok.hasMoreTokens()) {
            String fname = stok.nextToken();
            algorithm.addTrigger(fname);
        }        
    }
}
