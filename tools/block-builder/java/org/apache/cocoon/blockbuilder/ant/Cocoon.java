/*
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.cocoon.blockbuilder.ant;

/**
 * @since 0.1
 */
public class Cocoon {
    
    private String jar;
    
	/**
	 * @return Returns the dir.
	 */
	public String getJar() {
		return jar;
	}
	/**
	 * @param dir The dir to set.
	 */
	public void setJar(String jar) {
		this.jar = jar;
	}
}
