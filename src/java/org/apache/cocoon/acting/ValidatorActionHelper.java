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
package org.apache.cocoon.acting;


/**
 * Helper class to pass the result of a validation back along with
 * the validated object itself.
 *
 * @author <a href="mailto:haul@informatik.tu-darmstadt.de">Christian Haul</a>
 * @version CVS $Id: ValidatorActionHelper.java,v 1.3 2004/03/08 13:57:35 cziegeler Exp $
 */
public class ValidatorActionHelper
{
    protected ValidatorActionResult result = ValidatorActionResult.OK;
    protected Object object = null;

    /**
     * Create a ValidatorActionHelper object that contains just the
     * object. Defaults to <code>OK</code> as validation result.
     *
     * @param validatedObject object that has been validated
     */
    public ValidatorActionHelper ( Object validatedObject ) {
        this.object = validatedObject;
        this.result = ValidatorActionResult.OK;
    }

    /**
     * Create a ValidatorActionHelper object that contains just the
     * object. Defaults to <code>OK</code> as validation result.
     *
     * @param validatedObject object that has been validated
     * @param validationResult result of the validation
     */
    public ValidatorActionHelper ( Object validatedObject, ValidatorActionResult validationResult ) {
        this.object = validatedObject;
        this.result = validationResult;
    }

    /**
     * Tests if the validation result is <code>OK</code>
     *
     */
    public boolean isOK() {
        return (result.equals(ValidatorActionResult.OK));
    }

    /**
     * Tests if the validation result is <code>NOTPRESENT</code>,
     * e.g. when the value is null and is allowed to be null.
     *
     */
    public boolean isNotPresent() {
        return (result.equals(ValidatorActionResult.NOTPRESENT));
    }

    /**
     * Tests if the validation result is <code>ISNULL</code>,
     * e.g. when the value is null but is not supposed to be null.
     *
     */
    public boolean isNull() {
        return (result.equals(ValidatorActionResult.ISNULL));
    }

    /**
     * Tests if the validation result is <code>TOOLARGE</code>,
     * e.g. in case of a double or long the value is too large or in
     * case of a string it is too long.
     *
     */
    public boolean isTooLarge() {
        return (result.equals(ValidatorActionResult.TOOLARGE));
    }

    /**
     * Tests if the validation result is <code>TOOSMALL</code>,
     * e.g. in case of a double or long the value is too small or in
     * case of a string it is too short.
     *
     */
    public boolean isTooSmall() {
        return (result.equals(ValidatorActionResult.TOOSMALL));
    }

    /**
     * Tests if the validation result is <code>NOMATCH</code>, can
     * only occur when
     *
     */
    public boolean doesNotMatch() {
        return (result.equals(ValidatorActionResult.NOMATCH));
    }

    /**
     * Returns the tested object.
     *
     */
    public Object getObject() {
        return object;
    }

    /**
     * Returns the result.
     *
     */
    public ValidatorActionResult getResult() {
        return result;
    }
}
