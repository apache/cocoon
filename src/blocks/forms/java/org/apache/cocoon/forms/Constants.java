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
package org.apache.cocoon.forms;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Various constants used in the Woody form framework.
 * 
 * @version $Id: Constants.java,v 1.2 2004/03/09 11:31:12 joerg Exp $
 */
public final class Constants {
// TODO : see this later
//    /**
//     * The value returned by {@link org.apache.cocoon.forms.formmodel.Widget#getValue()}
//     * when the widget's value is invalid.
//     */
//    public static final Object INVALID_VALUE = new Object() {
//        public String toString() {
//            return "Invalid value";
//        }
//    };
    
    /** Namespace for Woody Template elements */
    public static final String FT_NS = "http://apache.org/cocoon/forms/1.0#template";
    public static final String FT_PREFIX = "ft";
    public static final String FT_PREFIX_COLON = "ft:";

    /** Namespace for Woody Instance elements */
    public static final String FI_NS = "http://apache.org/cocoon/forms/1.0#instance";
    public static final String FI_PREFIX = "fi";
    public static final String FI_PREFIX_COLON = "fi:";

    /** Namespace for Woody Definition elements */
    public static final String FD_NS = "http://apache.org/cocoon/forms/1.0#definition";
    public static final String FD_PREFIX = "fd";
    public static final String FD_PREFIX_COLON = "fd:";

    /** I18n catalogue containing the built-in Woody messages. */
    public static final String I18N_CATALOGUE = "forms";

    public static final Attributes EMPTY_ATTRS = new AttributesImpl();
}
