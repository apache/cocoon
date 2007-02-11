/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.woody.datatype;

import org.w3c.dom.Element;

/**
 * Interface for classes that can build ValidationRules from an XML description.
 * A ValidationRuleBuilder can be Composable.
 * A ValidationRuleBuilder should be thread safe, only one instance of it
 * will be created.
 * 
 * @version $Id: ValidationRuleBuilder.java,v 1.5 2004/03/09 13:54:04 reinhard Exp $
 */
public interface ValidationRuleBuilder {
    public ValidationRule build(Element validationRuleElement) throws Exception;
}
