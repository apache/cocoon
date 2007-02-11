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
package org.apache.cocoon.portal.tools.model;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * @version CVS $Id$
 */
public class User {
	
    private String name;
    private ArrayList roles = new ArrayList();
	
    public User(String name, String role) {
        this.name = name;
        this.roles.add(role);
    }
    
    public User(String name, ArrayList roles) {
        this.name = name;
        this.roles = roles;
    }

    public User(String name, String[] roles) {
        this.name = name;
        for(int i = 0; i < roles.length; i++) {
            this.roles.add(roles[i]);
        }
    }

    
    public String getName() {
	    return this.name;
	}
    
	public void setName(String name) {
	    this.name = name;
	}
	
	public ArrayList getRoles() {
	    return this.roles;
	}
	
    public boolean hasRole(String role) {
        for(Iterator it = roles.iterator(); it.hasNext();) {
            if(((String) it.next()).equals(role)) {
                return true;
            }
        }
		return false;
	}

}
