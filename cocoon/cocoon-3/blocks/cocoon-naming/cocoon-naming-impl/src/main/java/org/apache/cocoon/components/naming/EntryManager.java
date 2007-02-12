/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.components.naming;

import java.util.Map;

import javax.naming.directory.DirContext;

import org.apache.cocoon.ProcessingException;


/**
 *	The <code>EntryManager</code> is an Avalon Component for managing the Entries in a Javax Naming Directory.
 *  This is the interface implemented by {@link org.apache.cocoon.components.naming.LDAPEntryManager LDAPEntryManager}.
 *  @version $Id$
 */
public interface EntryManager {

    String ROLE = EntryManager.class.getName();

    int ADD_ATTRIBUTE = DirContext.ADD_ATTRIBUTE; 
	int REMOVE_ATTRIBUTE = DirContext.REMOVE_ATTRIBUTE;
	int REPLACE_ATTRIBUTE = DirContext.REPLACE_ATTRIBUTE;
	
	void create(String entry_name, Map entity_attributes) throws ProcessingException ;
	
	Map get(String entry_name) throws ProcessingException;

	Map find(Map match_attributes) throws ProcessingException;

	Map find(String context, Map match_attributes) throws ProcessingException;
	
	void modify(String entry_name, int mod_operand, Map mod_attributes) throws ProcessingException;
	
}

