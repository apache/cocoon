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
package org.apache.cocoon.woody.validation.impl;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.context.Context;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.cocoon.woody.util.JavaScriptHelper;
import org.apache.cocoon.woody.validation.WidgetValidator;
import org.mozilla.javascript.Function;

/**
 * A {@link org.apache.cocoon.woody.validation.WidgetValidator} implemented as a JavaScript snippet.
 * This snippet must return a boolean value. If it returns <code>false</code>, it <strong>must</strong> have
 * set a validation error on the validated widget or one of its children.
 * <p>
 * The JavaScript snippet has the "this" variable set to the validated widget, and, if the form is used in a
 * flowscript, can use the flow's global values and fonctions and the <code>cocoon</code> object.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: JavaScriptValidator.java,v 1.1 2004/02/04 17:25:58 sylvain Exp $
 */
public class JavaScriptValidator implements WidgetValidator {
    
    private final Function function;
    private final Context avalonContext;
    
    public JavaScriptValidator(Context context, Function function) {
        this.function = function;
        this.avalonContext = context;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.validation.Validator#validate(org.apache.cocoon.woody.formmodel.Widget, org.apache.cocoon.woody.FormContext)
     */
    public final boolean validate(Widget widget, FormContext context) {

        Request request = ContextHelper.getRequest(this.avalonContext);

        Object result;
            
        try {
            result = JavaScriptHelper.callFunction(this.function, widget, new Object[] {widget}, request);
        } catch(RuntimeException re) {
            throw re; // rethrow
        } catch(Exception e) {
            throw new CascadingRuntimeException("Error invoking JavaScript event handler", e);
        }

        if (result == null) {
            throw new RuntimeException("Validation script did not return a value");
            
        } else if (result instanceof Boolean) {
            return ((Boolean)result).booleanValue();
            
        } else {
            throw new RuntimeException("Validation script returned an unexpected value of type " + result.getClass());
        }
    }
}
