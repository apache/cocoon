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

import org.apache.cocoon.webapps.authentication.context.AuthenticationContext;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

/**
 * Grabbing the context of an user, which is set in the file sunrise-user.xml
 * and is stored in the class AuthenticationContext.
 * 
 * @version CVS $Id$
 */
public class ContextGrabber {
	
	/**
	 * Grabbing the context of the current user
	 * 
	 * @param context the instantiated class AuthenticationContext
	 * @return Object of context information
	 */
	public UserBean grab (AuthenticationContext context) {
		UserBean ub = new UserBean ();
		DocumentFragment df = null;
		try {
			df = context.getXML ("/authentication/");
		} catch (Exception e) {}
		
		grabAuthContext (df.getFirstChild(),ub);
		
		return ub;
	}
	
	private void grabAuthContext (Node node, UserBean ub) {
		
		while (node != null) {
			
			if (!node.getNodeName().equals("#text")) {
				if (node.getFirstChild() != null) {
					grabAuthContext  (node.getFirstChild () ,ub);
					ub.addContext(node.getNodeName(),node.getFirstChild().getNodeValue());
				} else {
					ub.addContext(node.getNodeName(),"");
                }
			}
			node = node.getNextSibling();
		}
	}
	
}
