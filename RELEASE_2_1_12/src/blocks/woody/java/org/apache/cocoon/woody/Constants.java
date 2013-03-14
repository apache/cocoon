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
package org.apache.cocoon.woody;

import org.apache.cocoon.xml.XMLUtils;

import org.xml.sax.Attributes;

/**
 * Various constants used in the Woody form framework.
 *
 * @version $Id$
 */
public final class Constants {
// TODO : see this later
//    /**
//     * The value returned by {@link org.apache.cocoon.woody.formmodel.Widget#getValue()}
//     * when the widget's value is invalid.
//     */
//    public static final Object INVALID_VALUE = new Object() {
//        public String toString() {
//            return "Invalid value";
//        }
//    };

    /** Namespace for Woody Template elements */
    public static final String WT_NS = "http://apache.org/cocoon/woody/template/1.0";
    public static final String WT_PREFIX = "wt";
    public static final String WT_PREFIX_COLON = "wt:";

    /** Namespace for Woody Instance elements */
    public static final String WI_NS = "http://apache.org/cocoon/woody/instance/1.0";
    public static final String WI_PREFIX = "wi";
    public static final String WI_PREFIX_COLON = "wi:";

    /** Namespace for Woody Definition elements */
    public static final String WD_NS = "http://apache.org/cocoon/woody/definition/1.0";
    public static final String WD_PREFIX = "wd";
    public static final String WD_PREFIX_COLON = "wd:";

    /** I18n catalogue containing the built-in Woody messages. */
    public static final String I18N_CATALOGUE = "woody";

    public static final Attributes EMPTY_ATTRS = XMLUtils.EMPTY_ATTRIBUTES;
}
