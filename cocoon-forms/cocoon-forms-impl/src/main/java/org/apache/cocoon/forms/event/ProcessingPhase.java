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
 * Type-safe enumeration of the various form processing phases.
 * 
 * @version $Id$
 */
public class ProcessingPhase extends ValuedEnum {

    protected ProcessingPhase(String name, int value) {
        super(name, value);
    }
    public static final int PROCESSING_INITIALIZE_VALUE = 4;
    public static final ProcessingPhase PROCESSING_INITIALIZE = new ProcessingPhase("Processing initialize", PROCESSING_INITIALIZE_VALUE);
    
    public static final int LOAD_MODEL_VALUE = 0;
    public static final ProcessingPhase LOAD_MODEL = new ProcessingPhase("Load model", LOAD_MODEL_VALUE);
    
    public static final int READ_FROM_REQUEST_VALUE = 1;
    public static final ProcessingPhase READ_FROM_REQUEST = new ProcessingPhase("Read from request", READ_FROM_REQUEST_VALUE);
    
    public static final int VALIDATE_VALUE = 2;
    public static final ProcessingPhase VALIDATE = new ProcessingPhase("Validate", VALIDATE_VALUE);
    
    public static final int SAVE_MODEL_VALUE = 3;
    public static final ProcessingPhase SAVE_MODEL = new ProcessingPhase("Save model", SAVE_MODEL_VALUE);
     
    public static ProcessingPhase getEnum(String name) {
      return (ProcessingPhase) getEnum(ProcessingPhase.class, name);
    }
    
    public static ProcessingPhase getEnum(int value) {
      return (ProcessingPhase) getEnum(ProcessingPhase.class, value);
    }

    public static Map getEnumMap() {
      return getEnumMap(ProcessingPhase.class);
    }
 
    public static List getEnumList() {
      return getEnumList(ProcessingPhase.class);
    }
 
    public static Iterator iterator() {
      return iterator(ProcessingPhase.class);
    }
}
