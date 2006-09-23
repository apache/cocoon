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
cocoon.load("resource://org/apache/cocoon/portal/tools/ptm.js");

function getContext () {
	var service = 
	cocoon.getComponent(Packages.org.apache.cocoon.portal.PortalService.ROLE);

	var grabber = new Packages.org.apache.cocoon.portal.tools.userManagement.ContextGrabber();
	var obj = grabber.grab(service);
	
	var pic = obj.getContextItem ("picture");
	if (pic != null)
		obj.setPicture (pic);
	
	cocoon.releaseComponent(service);
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
	
	/* get current user data */
	var obj = new Packages.org.apache.cocoon.portal.tools.userManagement.UserBean();
	
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
}
