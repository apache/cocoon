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
package org.apache.cocoon.woody.flow.javascript;

import java.util.Locale;

import org.apache.cocoon.components.flow.javascript.fom.FOM_Cocoon;
import org.apache.cocoon.woody.FormContext;

/**
 * A helper class for the flowscript/woody integration.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: WoodyFlowHelper.java,v 1.4 2004/03/09 13:54:25 reinhard Exp $
 */
// FOM is really a PITA when the flow has to be integrated with Java classes that use the real
// environment objects...
public class WoodyFlowHelper {
    
    /** Only static methods in this class */
    private WoodyFlowHelper() {}
    
    public static final FormContext getFormContext(FOM_Cocoon cocoon, Locale locale) {
        return new FormContext(cocoon.getRequest(), locale);
    }


}
