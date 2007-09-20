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
package org.apache.cocoon.forms.event;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.ValuedEnum;

/**
 * Type-safe enumeration of the various repeater actions that triggers events.
 * 
 * @version $Id$
 */
public class RepeaterEventAction extends ValuedEnum {

    protected RepeaterEventAction(String name, int value) {
        super(name, value);
    }
    
    public static final int ROW_ADDED_VALUE = 0;
    /**
     * This event type is triggered after a row has been added.
     */
    public static final RepeaterEventAction ROW_ADDED = new RepeaterEventAction("Row added", ROW_ADDED_VALUE);

    public static final int ROW_DELETING_VALUE = 1;
    /**
     * This event type is triggered before a row get's removed. 
     */
    public static final RepeaterEventAction ROW_DELETING = new RepeaterEventAction("Row deleting", ROW_DELETING_VALUE);
    
    public static final int ROW_DELETED_VALUE = 2;
    /**
     * This event type is triggered after a row has been removed. 
     */
    public static final RepeaterEventAction ROW_DELETED = new RepeaterEventAction("Row deleted", ROW_DELETED_VALUE);
    
    public static final int ROWS_REARRANGED_VALUE = 3;
    /**
     * This event type is triggered after the order of one or more rows has been changed.
     */
    public static final RepeaterEventAction ROWS_REARRANGED = new RepeaterEventAction("Rows rearranged",ROWS_REARRANGED_VALUE);

    public static final int ROWS_CLEARING_VALUE = 4;
    /**
     * This event type is triggered before the repeater is cleared (aka before all rows are removed). 
     */
    public static final RepeaterEventAction ROWS_CLEARING = new RepeaterEventAction("Rows clearing",ROWS_CLEARING_VALUE);
    
    public static final int ROWS_CLEARED_VALUE = 5;
    /**
     * This event type is triggered after the repeater has been cleared (aka after all rows have been removed)
     */
    public static final RepeaterEventAction ROWS_CLEARED = new RepeaterEventAction("Rows cleared",ROWS_CLEARED_VALUE);
    
    public static RepeaterEventAction getEnum(String name) {
      return (RepeaterEventAction) getEnum(RepeaterEventAction.class, name);
    }
    
    public static RepeaterEventAction getEnum(int value) {
      return (RepeaterEventAction) getEnum(RepeaterEventAction.class, value);
    }

    public static Map getEnumMap() {
      return getEnumMap(RepeaterEventAction.class);
    }
 
    public static List getEnumList() {
      return getEnumList(RepeaterEventAction.class);
    }
 
    public static Iterator iterator() {
      return iterator(RepeaterEventAction.class);
    }
}
