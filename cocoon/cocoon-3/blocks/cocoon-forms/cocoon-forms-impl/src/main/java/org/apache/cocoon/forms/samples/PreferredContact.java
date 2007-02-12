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
package org.apache.cocoon.forms.samples;

import org.apache.commons.lang.enums.Enum;

/**
 * Test apache enum class.
 * 
 * @version $Id$
 */
public class PreferredContact extends Enum {

    public static final PreferredContact EMAIL = new PreferredContact("EMAIL");
    public static final PreferredContact FAX = new PreferredContact("FAX");
    public static final PreferredContact PHONE = new PreferredContact("PHONE");
    public static final PreferredContact PAGER = new PreferredContact("PAGER");
    public static final PreferredContact POSTAL_MAIL = new PreferredContact("POSTAL_MAIL");
    
    protected PreferredContact(String name) {
        super(name);
    }
    
    public String toString() {
        return PreferredContact.class.getName() + "." + getName();
    }

}
