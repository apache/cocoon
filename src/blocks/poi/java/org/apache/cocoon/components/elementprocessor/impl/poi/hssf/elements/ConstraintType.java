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

package org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements;



/**
 * Contraint codes
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: ConstraintType.java,v 1.5 2004/03/05 13:02:03 bdelacretaz Exp $
 */
public class ConstraintType
{
    public static final int CONSTRAINT_TYPE_NONE         = 0;
    public static final int CONSTRAINT_TYPE_LESS_THAN    = 1;
    public static final int CONSTRAINT_TYPE_GREATER_THAN = 2;
    public static final int CONSTRAINT_TYPE_EQUALS       = 4;
    public static final int CONSTRAINT_TYPE_INT          = 8;
    public static final int CONSTRAINT_TYPE_BOOL         = 16;

    private ConstraintType() {
    }

    /**
     * Is this a valid constraint?
     *
     * @param val value to be checked
     *
     * @return true if valid, false otherwise
     */

    public static boolean isValid(final int val) {
        switch (val) {
            case CONSTRAINT_TYPE_NONE :
            case CONSTRAINT_TYPE_LESS_THAN :
            case CONSTRAINT_TYPE_GREATER_THAN :
            case CONSTRAINT_TYPE_EQUALS :
            case CONSTRAINT_TYPE_INT :
            case CONSTRAINT_TYPE_BOOL :
                return true;
            default :
                return false;
        }
    }
}   // end public class ConstraintType
