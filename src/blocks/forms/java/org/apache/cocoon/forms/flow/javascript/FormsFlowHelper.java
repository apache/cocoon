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
package org.apache.cocoon.forms.flow.javascript;

import java.util.Locale;

import org.apache.cocoon.components.flow.javascript.fom.FOM_Cocoon;
import org.apache.cocoon.forms.FormContext;

/**
 * A helper class for the flowscript/woody integration.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: FormsFlowHelper.java,v 1.1 2004/03/09 10:34:13 reinhard Exp $
 */
// FOM is really a PITA when the flow has to be integrated with Java classes that use the real
// environment objects...
public class FormsFlowHelper {
    
    /** Only static methods in this class */
    private FormsFlowHelper() {}
    
    public static final FormContext getFormContext(FOM_Cocoon cocoon, Locale locale) {
        return new FormContext(cocoon.getRequest(), locale);
    }


}
