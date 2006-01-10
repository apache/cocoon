/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.components.validation;

import org.xml.sax.SAXException;

/**
 * <p>An exception representing that a {@link Validator} was not able to detect
 * or did not support a specified schema grammar language.</p>
 *
 */
public class ValidatorException extends SAXException {

    /**
     * <p>Create a new {@link ValidatorException} instance.</p>
     */
    public ValidatorException(String message) {
        super(message);
    }

    /**
     * <p>Create a new {@link ValidatorException} instance.</p>
     */
    public ValidatorException(String message, Exception exception) {
        super(message, exception);
    }

}
