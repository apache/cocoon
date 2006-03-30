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
package org.apache.cocoon.util;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * A helper class for matching element names, and attribute names.
 * <p/>
 * For given include-name, exclude-name decide if element-attribute pair
 * matches. This class defines the precedence and matching algorithm.
 * </p>
 * This was originally part of the EncodeURLTransformer, moved
 * here to make it more reusable.
 *
 * @version $Id$
 */
public class ElementAttributeMatching {
    /**
     * Regular expression of including patterns
     */
    protected RE includeNameRE;
    /**
     * Regular expression of excluding patterns
     */
    protected RE excludeNameRE;


    /**
     * Constructor for the ElementAttributeMatching object
     *
     * @param includeName Description of Parameter
     * @param excludeName Description of Parameter
     * @throws org.apache.regexp.RESyntaxException
     *          Description of Exception
     */
    public ElementAttributeMatching(String includeName, String excludeName) throws RESyntaxException {
        includeNameRE = new RE(includeName, RE.MATCH_CASEINDEPENDENT);
        excludeNameRE = new RE(excludeName, RE.MATCH_CASEINDEPENDENT);
    }


    /**
     * Return true iff element_name attr_name pair is not matched by exclude-name,
     * but is matched by include-name
     *
     * @param element_name
     * @param attr_name
     * @param value used to canonicalize the elemtn/attribute name
     * @return boolean true iff value of attribute_name should get rewritten, else
     *         false.
     */
    public boolean matchesElementAttribute(String element_name, String attr_name, String value) {
        final String element_attr_name = canonicalizeElementAttribute(element_name, attr_name, value);

        if (excludeNameRE != null && includeNameRE != null) {
            return !matchesExcludesElementAttribute(element_attr_name) &&
                    matchesIncludesElementAttribute(element_attr_name);
        } else {
            return false;
        }
    }

    /**
     * Build a single string from element name, attribute and value, for pattern matching.
     */
    private String canonicalizeElementAttribute(String element_name, String attr_name, String value) {
        return element_name + "/@" + attr_name + "=" + value;
    }

    /**
     * @return true element_attr_name is matched by exclude-name.
     */
    private boolean matchesExcludesElementAttribute(String element_attr_name) {
        return excludeNameRE.match(element_attr_name);
    }

    /**
     * @return true element_attr_name is matched by include-name.
     */
    private boolean matchesIncludesElementAttribute(String element_attr_name) {
        return includeNameRE.match(element_attr_name);
    }
}
