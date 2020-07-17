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


/* 
	AddressbookManager Object
	
	A collection of static functions to access an LDAP-based Addressbook from FlowScript
	
	NB. This is sample code
	It makes various simplifications and has certain important limitations
		it currently cannot delete individual attributes
		it only reads the first value from attributes
		it contains very little error-checking or handling
		there is no result paging or sorting

*/

importClass(Packages.java.util.HashMap);
importClass(Packages.java.util.ArrayList);


var AddressbookManager = {
	binding: { /* binding between the display model and the LDAP Attributes */
		firstname  : "givenName",
		lastname   : "sn",
		email      : "mail",
		deptName   : "physicalDeliveryOfficeName",
		address1   : "postalAddress",
		address2   : "l",
		address3   : "st",
		postcode   : "postalCode",
		ophone     : "telephoneNumber",
		fphone     : "facsimileTelephoneNumber",
		pager      : "pager",
		mphone     : "mobile",
		hphone     : "homePhone",
		dept       : "ou"
	}
}

/*
	Map a Person-LDAP record to a JavaScript Object to use in the display layer
	This maintains a separation between the structure of the LDAP records and the 
	display code and style.
	
	@param uid the unique ID for this person
	@param person the LDAP record of the Person
	@return the Javascript Object representing the Person
*/
AddressbookManager.bindPerson = function(uid, person) {
	var result = {uid: uid}, val; // add a synthetic UID property for convenience
	for (var key in this.binding) {
		val = person.get(this.binding[key]);
		if (val) result[key] = val.get(0); // simplification, only handling single values
	}
	return result;
}

/*
	Map a JavaScript Object to use in the display layer to a Person-LDAP record
	This performs the reverse of AddressbookManager.bindPerson.
	
	TODO: How do attributes get deleted?
	
	@param person the Javascript Object representing the Person
	@return the LDAP record of the Person
*/
AddressbookManager.unbindPerson = function(person) {
	var attributes = new HashMap(), name, val;
	for (var key in person) {
		name = this.binding[key];
		val = person[key];
		if (name && val) attributes.put(name, this.singleAttribute(val)); // copy populated values
	}
	return attributes;
}

/*
	Create a new CForms Model from the binding data
	
	@return the new model
*/
AddressbookManager.getNewModel = function() {
	var model = {};
	for (var key in this.binding) model[key] = "";
	return model;
}

/* 
	Add a new Person to the Addressbook.
	
	TODO: handle errors like duplicate entries properly.
	
	@param entrymanager the LDAP Component
	@param model the Javascript Object representing the Person
	@param ou the organisationalUnit (department) to add this person to
	@return the UID of the new Person
*/
AddressbookManager.addPerson = function(entrymanager, model, ou) {
	model.dept = ou.substring(3); // strip the identifier ('ou=')
	var uid = "cn=" + model.firstname + " " + model.lastname + "," + ou;
	var attributes = this.unbindPerson(model), classes = new ArrayList();
	classes.add("top");
	classes.add("person");
	classes.add("organizationalPerson");
	classes.add("inetOrgPerson");
	attributes.put("objectClass", classes);
	entrymanager.create(uid, attributes);
	return uid;
}

/*
	Get a list of departments in the addressbook
	
	@param entrymanager the LDAP Component
	@return a JavaScript Array of organisationalUnits
*/
AddressbookManager.getDepartments = function(entrymanager) {
	var matchAttrs = new HashMap(), departments = [];
	matchAttrs.put("objectClass", this.singleAttribute("organizationalUnit"));
	var results = entrymanager.find("", matchAttrs);
	var it = results.keySet().iterator();
	while (it.hasNext()) departments[departments.length] = it.next();
	return departments;
}

/*
	Get a list of people from a department in the addressbook
	
	TODO: Sort and Page results
	
	@param entrymanager the LDAP Component
	@param department the organisationalUnit to look in
	@return a JavaScript Array of JavaScript Person Objects
*/
AddressbookManager.getPeople = function(entrymanager, department) {
	var matchAttrs = new HashMap(), people = [];
	department = department ? department : "";
	matchAttrs.put("objectClass", this.singleAttribute("inetOrgPerson"));
	var results = entrymanager.find(department, matchAttrs);
	var it = results.keySet().iterator(), key;
	while (it.hasNext()) {
		key = it.next();
		people[people.length] = this.bindPerson(key + "," + department, results.get(key));
	}
	return people;
}

/*
	Get a single person from the addressbook
	
	@param entrymanager the LDAP Component
	@param uid the UID of the person
	@return a JavaScript person Object
*/
AddressbookManager.getPerson = function(entrymanager, uid) {
	uid = uid ? uid : "";
	var result = entrymanager.get(uid);
	return this.bindPerson(uid, result);
}

/*
	Update a single person who already exists in the addressbook
	
	@param entrymanager the LDAP Component
	@param model the Javascript Object representing the Person
	@return a JavaScript person Object
*/
AddressbookManager.updatePerson = function(entrymanager, model) {
	var uid = model.uid;
	var attributes = this.unbindPerson(model);
	entrymanager.modify(uid, EntryManager.REPLACE_ATTRIBUTE, attributes);
}

/*
	Remove a single person from the addressbook
	
	@param entrymanager the LDAP Component
	@param uid the UID of the person
	@return the department they were in
*/
AddressbookManager.deletePerson = function(entrymanager, uid) {
	var person = this.getPerson(entrymanager, uid);
	var department = "ou=" + person["dept"];
	entrymanager.remove(uid);
	return department;
}

/*
	convenience function to wrap a value in a List
	the naming API takes Attributes as Lists, as Attributes may have many values
	
	@param value the value to wrap
	@return an ArrayList with a single value
*/
AddressbookManager.singleAttribute = function(value) {
	var attribute = new ArrayList();
	attribute.add(value);
	return attribute;
}
