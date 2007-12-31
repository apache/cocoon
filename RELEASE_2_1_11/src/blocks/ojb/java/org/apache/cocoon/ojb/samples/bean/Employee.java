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

package org.apache.cocoon.ojb.samples.bean;

import java.io.Serializable;

/**
 *  Employee's Bean
 *
 * @author <a href="mailto:antonio@apache.org">Antonio Gallardo</a>
 * @version CVS $Id$
*/
public class Employee implements Serializable {

    private int id;
    protected int departmentId;
    protected String name;

    public Employee(){
        this.id = 1;
        this.departmentId = 1;
        this.name = "My Name";
    }

    public int getId() {
        return this.id;
    }

    public int getDepartmentId() {
        return this.departmentId;
    }

    public String getName() {
        return this.name;
    }

    public void setId(int newId) {
        this.id = newId;
    }

    public void setDepartmentId(int newDepartmentId) {
        this.departmentId = newDepartmentId;
    }

    public void setName(String newName) {
        this.name = newName;
    }
} 
