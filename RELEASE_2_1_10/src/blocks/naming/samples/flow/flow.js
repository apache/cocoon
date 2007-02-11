/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

// webapp flow for an LDAP Addressbook

cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js");
cocoon.load("flow/AddressbookManager.js");

importClass(Packages.org.apache.cocoon.components.naming.EntryManager);

/*

External Function
Add a Person to the Addressbook

*/
function addNewPerson(form) {
	var department = cocoon.parameters["dn"];
	var entrymanager = cocoon.getComponent(EntryManager.ROLE);
	var model = AddressbookManager.getNewModel();
	try {
		form.load(model);
		form.showForm(cocoon.parameters["screen"]);
		form.save(model);
		if ("done".equals(form.submitId)) {
			var uid = AddressbookManager.addPerson(entrymanager, model, department);
			cocoon.redirectTo("person.html?dn=" + uid);
		} else if ("cancel".equals(form.submitId)) {
			cocoon.redirectTo("people.html?dn=" + department);
		}
	} catch (e) {
		cocoon.log.error(e);
		throw(e);
	} finally {
		cocoon.releaseComponent(entrymanager);
	}
}

/*

External Function
Update a Person's existing attributes in the Addressbook

*/
function updatePerson(form) {
	var uid = cocoon.parameters["dn"];
	var entrymanager = cocoon.getComponent(EntryManager.ROLE);
	try {
		var model = AddressbookManager.getPerson(entrymanager, uid);
		form.load(model);
		form.showForm(cocoon.parameters["screen"]);
		form.save(model);
		if ("done".equals(form.submitId))
			AddressbookManager.updatePerson(entrymanager, model);
		cocoon.redirectTo("person.html?dn=" + uid);
	} catch (e) {
		cocoon.log.error(e);
		throw(e);
	} finally {
		cocoon.releaseComponent(entrymanager);
	}
}

/*

External Function
Retrieves a Person from the Addressbook using their UID

*/
function getPerson() {
	var uid = cocoon.parameters["dn"];
	var entrymanager = cocoon.getComponent(EntryManager.ROLE);
	try {
		var person = AddressbookManager.getPerson(entrymanager, uid);
		cocoon.sendPage(cocoon.parameters["screen"], {person: person});
	} catch (e) {
		print(e);
		throw(e);
	} finally {
		cocoon.releaseComponent(entrymanager);
	}
}

/*

External Function
Deletes a Person from the Addressbook using their UID
TODO: add a confirmation screen

*/
function deletePerson() {
	var uid = cocoon.parameters["dn"];
	var entrymanager = cocoon.getComponent(EntryManager.ROLE);
	try {
		var department = AddressbookManager.deletePerson(entrymanager, uid);
		cocoon.redirectTo("people.html?dn=" + department);
	} catch (e) {
		print(e);
		throw(e);
	} finally {
		cocoon.releaseComponent(entrymanager);
	}
}

/*

External Function
Retrieves all People in the specified Department from the Addressbook

*/
function getPeople() {
	var department = cocoon.parameters["dn"];
	var entrymanager = cocoon.getComponent(EntryManager.ROLE);
	try {
		var people = AddressbookManager.getPeople(entrymanager, department);
		cocoon.sendPage(cocoon.parameters["screen"], {people: people, department: department});
	} catch (e) {
		print(e);
		throw(e);
	} finally {
		cocoon.releaseComponent(entrymanager);
	}
}

/*

External Function
Retrieves all Departments from the Addressbook

*/
function getDepartments() {
	var entrymanager = cocoon.getComponent(EntryManager.ROLE);
	try {
		var departments = AddressbookManager.getDepartments(entrymanager);
		cocoon.sendPage(cocoon.parameters["screen"], {departments: departments});
	} catch (e) {
		print(e);
		throw(e);
	} finally {
		cocoon.releaseComponent(entrymanager);
	}
}

