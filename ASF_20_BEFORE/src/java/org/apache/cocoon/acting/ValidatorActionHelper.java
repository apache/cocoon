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


/**
 * Helper class to pass the result of a validation back along with
 * the validated object itself.
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: ValidatorActionHelper.java,v 1.3 2004/02/15 21:30:00 haul Exp $
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
