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
package org.apache.cocoon.portal.tools;

/**
 * Represents a function the user can call via the ui or the ToolManager
 * 
 * @version CVS $Id$
 */
public class PortalToolFunction implements PortalToolComponent {
	
    // Function name (displayed to the user, maybe an i18n-key)
    private String name ="";
    // id to access the function
	private String id ="";
	// corresponding flow function
	private String function="";
	
	private boolean internal = false;
	/**
	 * Constructor
	 */
	public PortalToolFunction() {}
	
	/**
	 * Constructor
	 * @param name Function-Name
	 * @param id Id
	 * @param function Corresponding flow functino
	 */
	public PortalToolFunction(String name, String id, String function) {
		this.name = name;
		this.id = id;
		this.function = function;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.tools.PortalToolComponent#getId()
	 */
	public String getId() {
		return id;
	}
	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.tools.PortalToolComponent#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
	}
	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.tools.PortalToolComponent#getName()
	 */
	public String getName() {
		return name;
	}
	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.tools.PortalToolComponent#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Returns the Functionname
	 */
	public String getFunction() {
		return function;
	}
	
	/**
	 * Sets the Functionname
	 * @param path
	 */
	public void setFunction(String path) {
		this.function = path;
	}
	
	
	/**
	 * not in use!
	 */
    public boolean isInternal() {
        return internal;
    }
    
    /**
     * not in use!
     * @param internal
     */
    public void setInternal(boolean internal) {
        this.internal = internal;
    }
}
