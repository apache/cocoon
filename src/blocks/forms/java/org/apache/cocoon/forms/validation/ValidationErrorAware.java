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
package org.apache.cocoon.forms.validation;

/**
 * Interface implemented by {@link org.apache.cocoon.woody.formmodel.Widget}s that
 * can hold a validation error.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: ValidationErrorAware.java,v 1.1 2004/03/09 10:34:09 reinhard Exp $
 */
public interface ValidationErrorAware {
    
    ValidationError getValidationError();
    
    void setValidationError(ValidationError error);
}
