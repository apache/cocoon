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
package org.apache.cocoon.portal.tools.userManagement;

import java.util.Collection;
import java.util.HashMap;

/**
 * Object storing information of an user context. 
 * 
 * @version CVS $Id$
 */
public class UserBean {
	
	private HashMap context = new HashMap ();
	private String picture = "";
	
	
    public UserBean () {
    }
    
    /**
     * Add a single context information
     * 
     * @param key name of the context
     * @param value value of the context
     */
    public void addContext (String key, String value){
    	this.context.put (key, new ContextItem (key, value));
    }
	
	/**
	 * Get the whole context of the current user
	 * 
	 * @return Collection of the whole context
	 */
	public Collection getContext () {
		return this.context.values();
	}
	
	/**
	 * return specified context value
	 * 
	 * @param key 
	 */
	public String getContextItem (String key) {
		if (this.context.get(key) != null) {
			return ((ContextItem) this.context.get(key)).getValue();
        } else {
			return "";
        }
	}
	
	/**
	 * Special Attribute for the cocoon portal tool example:
	 * you can store even extra attributes in the bean
	 * 
	 * @return name of the picture file
	 */
	public String getPicture() {
		return picture;
	}
	
	/**
	 * Special Attribute for the cocoon portal tool example:
	 * you can store even extra attributes in the bean
	 * 
	 * @param string name of the picture file
	 */
	public void setPicture(String string) {
		picture = string;
	}
}
