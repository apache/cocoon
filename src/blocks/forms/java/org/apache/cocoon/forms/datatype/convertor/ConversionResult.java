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
package org.apache.cocoon.forms.datatype.convertor;

import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.util.I18nMessage;
import org.apache.cocoon.forms.Constants;

/**
 * Object returned as result of {@link Convertor#convertFromString(java.lang.String, java.util.Locale, org.apache.cocoon.forms.datatype.convertor.Convertor.FormatCache)}.
 */
public class ConversionResult {
    private ValidationError validationError;
    private boolean successful;
    private Object result;

    /**
     * Constructs a successful ConversionResult.
     */
    public ConversionResult(Object result) {
        this.successful = true;
        this.result = result;
    }

    /**
     * Constructs an unsuccessful ConversionResult.
     */
    public ConversionResult(ValidationError validationError) {
        this.successful = false;
        this.validationError = validationError;
    }

    /**
     * Constructs an unsuccessful ConversionResult. Will create
     * a default ValidationError message using the given
     * datatypeName.
     *
     * <p>Note: this is not done as a constructor because
     * it would conflict with the constructor which takes
     * an Object as argument.
     */
    public static ConversionResult create(String datatypeName) {
        ValidationError validationError = new ValidationError(new I18nMessage(
            "datatype.conversion-failed",
            new String[] {"datatype." + datatypeName},
            new boolean[] { true },
            Constants.I18N_CATALOGUE
        ));
        return new ConversionResult(validationError);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public ValidationError getValidationError() {
        if (successful)
            throw new IllegalStateException("Cannot call getValidationError() if conversion is successful.");

        return validationError;
    }

    public Object getResult() {
        if (!successful)
            throw new IllegalStateException("Cannot call getResult() if conversion is not successful.");

        return result;
    }
}
