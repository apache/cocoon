/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.matching.helpers;

import java.util.HashMap;

/**
 * This class is an utility class that perform wilcard-patterns matching and
 * isolation.
 * @deprecated This class has moved to the o.a.c.util package.
 * @version $Id$
 */
public class WildcardHelper {

    /**
     * @see org.apache.cocoon.util.WildcardHelper#compilePattern(String)
     */
    public static int[] compilePattern(String data)
    throws NullPointerException {
        return org.apache.cocoon.util.WildcardHelper.compilePattern(data);
    }

    /**
     * @see org.apache.cocoon.util.WildcardHelper#match(HashMap, String, int[])
     */
    public static boolean match(HashMap map, String data, int[] expr) 
    throws NullPointerException {
        return org.apache.cocoon.util.WildcardHelper.match(map, data, expr);
    }
}
