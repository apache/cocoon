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
 * ObjectFill codes
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: ObjectFill.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class ObjectFill {
    public static final int OBJECT_FILL_LINE = 1;
    public static final int OBJECT_FILL_ARROW = 2;
    public static final int OBJECT_FILL_BOX = 101;
    public static final int OBJECT_FILL_OVAL = 102;

    private ObjectFill() { /*VOID */}

    /**
     * Is this a valid direction?
     * @param val value to be checked
     * @return true if valid, false otherwise
     */
    public static boolean isValid(final int val) {
        switch (val) {
            case OBJECT_FILL_LINE :
            case OBJECT_FILL_ARROW :
            case OBJECT_FILL_BOX :
            case OBJECT_FILL_OVAL :
                return true;
            default :
                return false;
        }
    }
} // end public class ObjectFill
