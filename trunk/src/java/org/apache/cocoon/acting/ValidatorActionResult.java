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

import org.apache.cocoon.util.EnumerationFactory;

/**
 * A number of constants to represent the possible outcomes of a
 * validation.
 *
 * @author <a href="mailto:haul@informatik.tu-darmstadt.de">Christian Haul</a>
 * @version CVS $Id: ValidatorActionResult.java,v 1.3 2004/03/08 13:57:35 cziegeler Exp $
 */
public class ValidatorActionResult extends EnumerationFactory {

    /**
     * no error occurred, parameter successfully checked.
     */
    public static final ValidatorActionResult
    OK         = new ValidatorActionResult ("OK");          // 0

    /**
     * this is returned when the result of a validation is
     * requested but no such result is found in the request
     * attribute.
     */
    public static final ValidatorActionResult
    NOTPRESENT = new ValidatorActionResult ("NOTPRESENT");  // 1

    /**
     * some error occurred, this is a result that is never set but
     * serves as a comparison target.
     */
    public static final ValidatorActionResult
    ERROR      = new ValidatorActionResult ("ERROR");       // 2

    /**
     * the parameter is null but isn't allowed to.
     */
    public static final ValidatorActionResult
    ISNULL     = new ValidatorActionResult ("ISNULL");      // 3

    /**
     * either value or length in case of a string is less than the
     * specified minimum.
     */
    public static final ValidatorActionResult
    TOOSMALL   = new ValidatorActionResult ("TOOSMALL");    // 4

    /**
     * either value or length in case of a string is greater than
     * the specified maximum.
     */
    public static final ValidatorActionResult
    TOOLARGE   = new ValidatorActionResult ("TOOLARGE");    // 5

    /**
     * a string parameter's value is not matched by the specified
     * regular expression.
     */
    public static final ValidatorActionResult
    NOMATCH    = new ValidatorActionResult ("NOMATCH");     // 6

    /**
     * maximum error, only used for comparisons.
     */
    public static final ValidatorActionResult
    MAXERROR   = new ValidatorActionResult ("MAXERROR");    // 7

    /**
     * Make constructor private to inhibit creation outside.
     */
    private ValidatorActionResult (String image) {
        super (image);
    }
}
