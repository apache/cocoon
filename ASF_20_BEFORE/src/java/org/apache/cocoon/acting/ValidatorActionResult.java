/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.acting;

import org.apache.cocoon.util.EnumerationFactory;

/**
 * A number of constants to represent the possible outcomes of a
 * validation.
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: ValidatorActionResult.java,v 1.3 2004/02/15 21:30:00 haul Exp $
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
