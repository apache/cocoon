/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
cocoon.load("resource://org/apache/cocoon/portal/tools/ptm.js");

/* component authenticationManager contains the data from the file 'sunrise-user.xml' */

function getContext ()
{
	var contextMan = 
	cocoon.getComponent(Packages.org.apache.cocoon.webapps.session.ContextManager.ROLE);
	var context = contextMan.getContext("authentication");

	var authenticationManager = cocoon.getComponent("org.apache.cocoon.webapps.authentication.AuthenticationManager");
	/* get current user data */
	var context = authenticationManager.getState().getHandler().getContext();

	var grabber = new Packages.org.apache.cocoon.portal.tools.userManagement.ContextGrabber();
	var obj = grabber.grab(context);
	
	var pic = obj.getContextItem ("picture");
	if (pic != null)
		obj.setPicture (pic);
	
	cocoon.releaseComponent(contextMan);
	cocoon.releaseComponent(authenticationManager);
	return obj;
}

/* public function: print the data of an user
 */
function showUser() {
	
	var obj = getContext ();
	
	/* show form */
	var form = new Form("cocoon:/page/model/userData?mode=readonly");
	form.createBinding("cocoon:/page/binding/userData?mode=readonly");
    form.load(obj);
    form.showForm("page/form/userData?mode=readonly");
}

/* public function: print and edit the data of an user
 */
function editUser() {

	var obj = getContext ();
	
	/* show form */
	var form = new Form("cocoon:/page/model/userData?mode=edit");
	form.createBinding("cocoon:/page/binding/userData?mode=edit");
    form.load(obj);
    form.showForm("page/form/userData?mode=edit");
}

/* public function: you can create a new user
 */
function addUser() {
	
	var authenticationManager = cocoon.getComponent("org.apache.cocoon.webapps.authentication.AuthenticationManager");
	
	/* get current user data */
	var obj = new Packages.org.apache.cocoon.portal.tools.userManagement.UserBean();
	var context = authenticationManager.getState().getHandler().getContext();
	createDefaultKeys (context.getXML("/authentication/"),0,obj);
	
	var mf = obj.getContext();
	for(var it = mf.iterator();it.hasNext();) {
		var e = it.next()
		print(e.getValue());
	}
	
	/* show form */
	var form = new Form("cocoon:/page/model/userData?mode=add");
	form.createBinding("cocoon:/page/binding/userData?mode=add");
    form.load(obj);
    form.showForm("page/form/userData?mode=add");

    cocoon.releaseComponent(authenticationManager);
}

/* internal function: creating an empty context of user data from the authenticationManager
 */
function createDefaultKeys (node,lev, obj) {
	
	node = node.firstChild;
	while (node != null)
	{
		if (node.nodeName == '#text')
		{
			createDefaultKeys(node,lev+1,obj);
		}
		else if (node.firstChild == null) 
		{
			obj.addContext (node.nodeName,"");
		}
		else if (node.firstChild.nodeValue == null)
		{
			createDefaultKeys(node,lev+1,obj);
		}
		else
		{
			obj.addContext (node.nodeName,"");
		}
		node = node.nextSibling;
	}
}

/* internal function: creates the context of the user data from the authenticationManager
 */
function printRek (node,lev, obj)
{
	node = node.firstChild;
	while (node != null)
	{
		if (node.nodeName == '#text')
		{
			printRek(node,lev+1,obj);
		}
		else if (node.firstChild == null) 
		{
			obj.addContext (node.nodeName,"");
		}
		else if (node.firstChild.nodeValue == null)
		{
			printRek(node,lev+1,obj);
		}
		else
		{
			obj.addContext (node.nodeName,node.firstChild.nodeValue);
		}
		node = node.nextSibling;
	}

}