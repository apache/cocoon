/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

importPackage(Packages.org.apache.cocoon.components.modules.input);
importPackage(Packages.org.apache.cocoon.components.slide);
importPackage(Packages.org.apache.cocoon.components.source);
importPackage(Packages.org.apache.cocoon.components.source.helpers);
importPackage(Packages.org.apache.cocoon.samples.slide);
importPackage(Packages.org.apache.excalibur.source);

var repository = cocoon.getComponent('org.apache.cocoon.components.repository.SourceRepository');
var resolver = cocoon.getComponent(SourceResolver.ROLE);
var global = cocoon.getComponent(InputModule.ROLE + "Selector").select("global");
var namespace = global.getAttribute("namespace",null,null);
var base = global.getAttribute("base",null,null);
var slide = cocoon.getComponent(SlideRepository.ROLE);
var nat = slide.getNamespaceToken(namespace);
var principal;

// ---------------------------------------------- authentication

function login() {
  cocoon.session;
  var userid = "";
  while (principal == undefined) {
    cocoon.sendPageAndWait("screens/login.html",{userid:userid,base:base});
    userid       = cocoon.request.getParameter("userid");
    var password = cocoon.request.getParameter("password");
    if (AdminHelper.login(nat,userid,password)) {
      principal = userid;
      // make the principal accessible from the sitemap as well
      cocoon.session.setAttribute("slide-principal",principal);
    }
  }
}

function logout() {
  cocoon.session.invalidate();
  cocoon.redirectTo(".");
}

// ---------------------------------------------- file management

function viewcontent() {
  var path     = cocoon.request.getParameter("path");

  login();

  var location = "slide://" + principal + "@" + namespace + path;
  var sourceDTO = null;
  var source = null;
  try {
    source = resolver.resolveURI(location);
    sourceDTO = new SourceDTO(source);
  }  finally {
    if (source != null) {
      resolver.release(source);
    }
  }
  cocoon.sendPage("screens/content.html",{source:sourceDTO});
}

// make a new collection
function makecollection() {
  var parentPath     = cocoon.request.getParameter("parentPath");
  var collectionName = cocoon.request.getParameter("collectionName");

  login();

  var baseUri        = "slide://" + principal + "@" + namespace + "/";
  var location = baseUri + parentPath + "/" + collectionName;
  var status = repository.makeCollection(location);

  cocoon.redirectTo("viewcontent.do?path=" + parentPath);
}

// upload a file
function uploadsource() {
  var parentPath   = cocoon.request.getParameter("parentPath");
  var resourceName = cocoon.request.getParameter("resourceName");

  //FIXME: retrieve upload object before login
  login();

  var baseUri      = "slide://" + principal + "@" + namespace + "/";
  var dest = baseUri + parentPath + "/" + resourceName;
  var src  = "upload://uploadFile";
  var status = repository.save(src,dest);

  cocoon.redirectTo("viewcontent.do?path=" + parentPath);
}

// delete a resource
function deletesource() {
  var parentPath = cocoon.request.getParameter("parentPath");
  var resourceName = cocoon.request.getParameter("resourceName");
  
  login();
  
  var baseUri = "slide://" + principal + "@" + namespace + "/";
  var location = baseUri + parentPath + "/" + resourceName;
  var status = repository.remove(location);

  cocoon.redirectTo("viewcontent.do?path=" + parentPath);
}

// ---------------------------------------------- property management

function viewproperties() {
  var path     = cocoon.request.getParameter("path");
                                                                                                                                                             
  login();
                                                                                                                                                             
  var location = "slide://" + principal + "@" + namespace + path;
  var sourceDTO = null;
  var source = null;
  try {
    source = resolver.resolveURI(location);
    sourceDTO = new SourceDTO(source);
  }  finally {
    if (source != null) {
      resolver.release(source);
    }
  }
  cocoon.sendPage("screens/properties.html",{source:sourceDTO});
}

function addproperty() {
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var name         = cocoon.request.getParameter("name");
  var namespace    = cocoon.request.getParameter("namespace");
  var value        = cocoon.request.getParameter("value");

  login();

  var baseUri  = "slide://" + principal + "@" + nat.getName() + "/";
  var location = baseUri + resourcePath;
  var property = new SourceProperty(namespace,name,value);
  var source = null;
  try {
    source = resolver.resolveURI(location);
    source.setSourceProperty(property);
  } finally {
    if (source != null) {
      resolver.release(source);
    }
  }
  cocoon.redirectTo("viewproperties.do?path=" + resourcePath);
}

function removeproperty() {
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var name         = cocoon.request.getParameter("name");
  var namespace    = cocoon.request.getParameter("namespace");

  login();

  var baseUri  = "slide://" + principal + "@" + nat.getName() + "/";
  var location = baseUri + resourcePath;
  if (namespace.equals("DAV:"))
      throw new IllegalArgumentException("Cannot remove webdav property");
  var source = null;
  try {
    source = resolver.resolveURI(location);
    var name      = cocoon.request.getParameter("name");
    var namespace = cocoon.request.getParameter("namespace");
    
    source.removeSourceProperty(namespace,name);
  } finally {
    if (source != null) {
      resolver.release(source);
    }
  }
  cocoon.redirectTo("viewproperties.do?path=" + resourcePath);
}

// ---------------------------------------------- permission management

function viewpermissions() {
  var path     = cocoon.request.getParameter("path");
                                                                                                                                                             
  login();
                                                                                                                                                             
  var location = "slide://" + principal + "@" + namespace + path;
  var sourceDTO = null;
  var source = null;
  try {
    source = resolver.resolveURI(location);
    sourceDTO = new SourceDTO(source);
  }  finally {
    if (source != null) {
      resolver.release(source);
    }
  }
                                                                                                                                                             
  var roles = AdminHelper.listGroups(nat,principal,"/roles");
  var users = AdminHelper.listUsers(nat,principal);
  var privileges = AdminHelper.listPrivileges(nat,principal);
  cocoon.sendPage("screens/permissions.html",{source:sourceDTO, roles:roles, users:users, privileges:privileges});
}

function removePermission() {
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var subject      = cocoon.request.getParameter("subject");
  var privilege    = cocoon.request.getParameter("privilege");

  login();
  
  AdminHelper.removePermission(nat,principal,resourcePath,subject,privilege);
  cocoon.redirectTo("permissions/" + resourcePath);
}

function addPermission() {
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var subject      = cocoon.request.getParameter("subject");
  var action       = cocoon.request.getParameter("action");
  var inheritable  = cocoon.request.getParameter("inheritable");
  var negative     = cocoon.request.getParameter("negative");

  login();
  
  AdminHelper.addPermission(nat,principal,resourcePath,subject,action,inheritable,negative);
  cocoon.redirectTo("permissions/" + resourcePath);
}

// ---------------------------------------------- lock management

function viewlocks() {
  var path     = cocoon.request.getParameter("path");
                                                                                                                                                             
  login();
                                                                                                                                                             
  var location = "slide://" + principal + "@" + namespace + path;
  var sourceDTO = null;
  var source = null;
  try {
    source = resolver.resolveURI(location);
    sourceDTO = new SourceDTO(source);
  }  finally {
    if (source != null) {
      resolver.release(source);
    }
  }

  var roles = AdminHelper.listGroups(nat,principal,"/roles");
  var users = AdminHelper.listUsers(nat,principal);
  var privileges = AdminHelper.listPrivileges(nat,principal);
  cocoon.sendPage("screens/locks.html",{source:sourceDTO, roles:roles, users:users, privileges:privileges});
}

function removelock() {
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var objectUri    = cocoon.request.getParameter("objectUri");
  var lockId       = cocoon.request.getParameter("lockId");

  login();
  
  AdminHelper.removeLock(nat,principal,objectUri,lockId);

  cocoon.redirectTo("viewlocks.do?path=" + resourcePath);
}

function addlock() {
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var subject      = cocoon.request.getParameter("subject");
  var type         = cocoon.request.getParameter("type");
  var exclusive    = cocoon.request.getParameter("exclusive");
  var expiration   = cocoon.request.getParameter("expiration");
  var inheritable  = cocoon.request.getParameter("inheritable");

  login();
  
  AdminHelper.addLock(nat,principal,resourcePath,subject,type,expiration,exclusive,inheritable);
  
  cocoon.redirectTo("viewlocks.do?path=" + resourcePath);
}

// ---------------------------------------------- user management

function viewusers() {
                                                                                                                                                             
  login();
                                                                                                                                                             
  var roles = AdminHelper.listGroups(nat,principal,"/roles");
  var users = AdminHelper.listUsers(nat,principal);
  cocoon.sendPage("screens/users.html",{roles:roles, users:users});
}

function adduser() {
  var username = cocoon.request.getParameter("username");
  var password = cocoon.request.getParameter("password");

  login();
  
  AdminHelper.addUser(nat,principal,username,password);
  cocoon.redirectTo("users");
}

function addrole () {
  var rolename = cocoon.request.getParameter("rolename");
  
  AdminHelper.addRole(nat,principal,rolename);
  cocoon.redirectTo("users");
}

function addgroup () {
  var groupname = cocoon.request.getParameter("groupname");

  login();
  
  AdminHelper.addGroup(nat,principal,groupname);
  cocoon.redirectTo("users");
}

function removeobject() {
  var objecturi = cocoon.request.getParameter("objecturi");

  login();
  
  AdminHelper.removeObject(nat,principal,objecturi);
  cocoon.redirectTo("users");
}

function addmember() {
  var objecturi  = cocoon.request.getParameter("objecturi");
  var subjecturi = cocoon.request.getParameter("subjecturi");

  login();
  
  AdminHelper.addMember(nat,principal,objecturi,subjecturi);
  cocoon.redirectTo("users");
}

function removemember() {
  var objecturi  = cocoon.request.getParameter("objecturi");
  var subjecturi = cocoon.request.getParameter("subjecturi");

  login();
  
  AdminHelper.removeMember(nat,principal,objecturi,subjecturi);
  cocoon.redirectTo("users");
}

function changepwd() {
  var username = cocoon.request.getParameter("username");
  var password = cocoon.request.getParameter("password");
  
  AdminHelper.changePassword(nat, principal, username, password);
  cocoon.redirectTo("users");
}

