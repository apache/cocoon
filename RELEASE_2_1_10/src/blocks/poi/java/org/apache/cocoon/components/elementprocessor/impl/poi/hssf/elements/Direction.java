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

package org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements;



/**
 * Direction codes
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id$
 */
public class Direction
{
    public static final int DIRECTION_UNKNOWN    = -1;
    public static final int DIRECTION_UP_RIGHT   = 0;
    public static final int DIRECTION_UP_LEFT    = 1;
    public static final int DIRECTION_DOWN_RIGHT = 16;
    public static final int DIRECTION_DOWN_LEFT  = 17;

    private Direction() {
    }

    /**
     * Is this a valid direction?
     *
     * @param val value to be checked
     *
     * @return true if valid, false otherwise
     */

    public static boolean isValid(final int val) {
        switch (val) {
            case DIRECTION_UNKNOWN :
            case DIRECTION_UP_RIGHT :
            case DIRECTION_UP_LEFT :
            case DIRECTION_DOWN_RIGHT :
            case DIRECTION_DOWN_LEFT :
                return true;
            default :
                return false;
        }
    }
}   // end public class Direction
