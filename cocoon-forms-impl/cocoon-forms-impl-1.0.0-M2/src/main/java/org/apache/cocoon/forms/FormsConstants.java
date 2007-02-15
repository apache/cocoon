/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

/**
 * Various constants used in the Cocoon Forms framework.
 * 
 * @version $Id$
 */
public class FormsConstants {
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
    
    /** Namespace for Template elements */
    public static final String TEMPLATE_NS = "http://apache.org/cocoon/forms/1.0#template";
    public static final String TEMPLATE_PREFIX = "ft";
    public static final String TEMPLATE_PREFIX_COLON = "ft:";

    /** Namespace for Instance elements */
    public static final String INSTANCE_NS = "http://apache.org/cocoon/forms/1.0#instance";
    public static final String INSTANCE_PREFIX = "fi";
    public static final String INSTANCE_PREFIX_COLON = "fi:";

    /** Namespace for Definition elements */
    public static final String DEFINITION_NS = "http://apache.org/cocoon/forms/1.0#definition";
    public static final String DEFINITION_PREFIX = "fd";
    public static final String DEFINITION_PREFIX_COLON = "fd:";

    /** I18n catalogue containing the built-in messages. */
    public static final String I18N_CATALOGUE = "forms";
    public static final String I18N_NS = "http://apache.org/cocoon/i18n/2.1";
    public static final String I18N_PREFIX = "i18n";
    public static final String I18N_PREFIX_COLON = "i18n:";

}
