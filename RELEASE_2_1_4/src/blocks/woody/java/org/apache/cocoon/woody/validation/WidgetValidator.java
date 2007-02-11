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
package org.apache.cocoon.woody.validation;

import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.formmodel.Widget;

/**
 * Validates a widget. Validation can mean lots of different things depending on the
 * actual widget and validator type, e.g. :
 * <li>
 * <ul>on fields, a validator will validate the field's value,</ul>
 * <ul>on repeaters, a validator can perform inter-row validation</ul>
 * </li>
 * <p>
 * A validator returns a boolean result indicating if validation was successful or not.
 * If not successful, the validator <code>must<code> set a {@link org.apache.cocoon.woody.validation.ValidationError}
 * on the validated widget or one of its children.
 * <p>
 * <em>Note:</em> It is important (although it cannot be explicitely forbidden) that a validator
 * does not consider widgets that are not the validated widgets itself or its children, as this
 * may lead to inconsistencies in the form model because of the way form validation occurs (depth-first
 * traversal of the widget tree).
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: WidgetValidator.java,v 1.1 2004/02/04 17:25:58 sylvain Exp $
 */
public interface WidgetValidator {
    
    /**
     * Validate a widget.
     * 
     * @param widget the widget to validate
     * @param context the form context
     * @return <code>true</code> if validation was successful. If not, the validator must have set
     *         a {@link ValidationError} on the widget or one of its children.
     */
    boolean validate(Widget widget, FormContext context);
}
