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

import org.apache.cocoon.forms.formmodel.CalculatedFieldAlgorithm;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;


/**
 * Builder for user custom {@link org.apache.cocoon.forms.formmodel.CalculatedFieldAlgorithm}s.
 * If the specified class is a {@link org.apache.cocoon.forms.formmodel.algorithms.AbstractBaseAlgorithm}
 * subclass, the build process will be delegated to
 *  {@link org.apache.cocoon.forms.formmodel.algorithms.AbstractBaseAlgorithmBuilder}.
 *
 * @version $Id$
 */
public class JavaAlgorithmBuilder extends AbstractBaseAlgorithmBuilder {

    public CalculatedFieldAlgorithm build(Element algorithmElement) throws Exception {
        String clazzname = DomHelper.getAttribute(algorithmElement, "class");
        Class clazz = Class.forName(clazzname);
        if (AbstractBaseAlgorithm.class.isAssignableFrom(clazz)) {
            AbstractBaseAlgorithm algorithm = (AbstractBaseAlgorithm) clazz.newInstance();
            super.setup(algorithmElement, algorithm);
            return algorithm;
        } else {
            CalculatedFieldAlgorithm algorithm = (CalculatedFieldAlgorithm) clazz.newInstance();
            super.setupComponent(algorithm);
            return algorithm;
        }
    }

}
