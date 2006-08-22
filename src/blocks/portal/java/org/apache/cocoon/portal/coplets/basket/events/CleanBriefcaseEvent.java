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
package org.apache.cocoon.portal.coplets.basket.events;

import org.apache.cocoon.portal.coplets.basket.Briefcase;

/**
 * Clean all briefcases or a single one
 *
 * @version CVS $Id: CleanBasketEvent.java 30941 2004-07-29 19:56:58Z vgritsenko $
 */
public class CleanBriefcaseEvent extends ContentStoreEvent {
    
    /**
     * Constructor
     * If this constructor is used all baskets will be cleaned
     */
    public CleanBriefcaseEvent() {
        // nothing to do here
    }
    
    /**
     * Constructor
     * One briefcase will be cleaned
     * @param briefcase The briefcase
     */
    public CleanBriefcaseEvent(Briefcase briefcase) {
        super(briefcase);
    }
    
}
