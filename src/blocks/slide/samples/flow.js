
importPackage(Packages.org.apache.cocoon.components.slide);
importPackage(Packages.org.apache.cocoon.components.source.helpers);
importPackage(Packages.org.apache.cocoon.samples.slide);
importPackage(Packages.org.apache.excalibur.source);

var repository = cocoon.getComponent("org.apache.cocoon.components.repository.SourceRepository");
var resolver = cocoon.getComponent(SourceResolver.ROLE);
var slide = cocoon.getComponent(SlideRepository.ROLE);
var nat = slide.getNamespaceToken("cocoon");

// these variables need to be available in the sitemap as well
// is/should there be a simple way to share these? an input module?
var principal;
var namespace = "cocoon";
var base = "/samples/slide";

// ---------------------------------------------- utility functions

function getBaseURI() {
  return "slide://" + principal + "@" + namespace + "/";
}

// ---------------------------------------------- authentication

function protect() {
  var path = cocoon.parameters["path"];
  if (principal == undefined){
    login(path);
  }
  else {
    invoke(path);
  }
}

function invoke(path) {
  var func = this["protected_" + path];
  if (func != undefined) {
    func.apply(this);
  }
  else {
    cocoon.sendPage(path,null);
  }
}

function login(path) {
  cocoon.session;
  var userid = "";
  while (principal == undefined) {
    cocoon.sendPageAndWait("screens/login.html",{userid:userid,prefix:"/slide-samples"});
    userid       = cocoon.request.getParameter("userid");
    var password = cocoon.request.getParameter("password");
    if (AdminHelper.login(nat,userid,password)) {
      principal = userid;
      // also put it in the session so it can be accessed 
      // from the sitemap throuhg the session attribute module
      cocoon.session.setAttribute("slide-principal",principal);
    }
  }
  cocoon.redirectTo(base + path);
}

function logout() {
  cocoon.session.invalidate();
  cocoon.redirectTo(base + "content/");
}

// ---------------------------------------------- file management

// make a new collection
function protected_mkcol() {
  var baseUri        = getBaseURI();
  var parentPath     = cocoon.request.getParameter("parentPath");
  var collectionName = cocoon.request.getParameter("collectionName");
  var location = baseUri + parentPath + "/" + collectionName;
  var status = repository.makeCollection(location);

  cocoon.redirectTo(base + "content/" + parentPath);
}

// upload a file
function protected_upload() {
  var baseUri      = getBaseURI();
  var parentPath   = cocoon.request.getParameter("parentPath");
  var resourceName = cocoon.request.getParameter("resourceName");
  var dest = baseUri + parentPath + "/" + resourceName;
  var src  = "upload://uploadFile";
  
  var status = repository.save(src,dest);
  cocoon.redirectTo(base + "content/" + parentPath);
}

// delete a resource
function protected_delete() {
  var baseUri = getBaseURI();
  var parentPath = cocoon.request.getParameter("parentPath");
  var resourceName = cocoon.request.getParameter("resourceName");
  var location = baseUri + parentPath + "/" + resourceName;
  
  var status = repository.remove(location);
  cocoon.redirectTo(base + "content/" + parentPath);
}

// ---------------------------------------------- property management

function protected_addproperty() {
  var baseUri      = getBaseURI();
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var location     = baseUri + resourcePath;
  var source = null;
  try {
    source = resolver.resolveURI(location);
    var name      = cocoon.request.getParameter("name");
    var namespace = cocoon.request.getParameter("namespace");
    var value     = cocoon.request.getParameter("value");
    var property = new SourceProperty(namespace,name,value);
    
    source.setSourceProperty(property);
  }
  finally {
    if (source != null) {
      resolver.release(source);
    }
  }
  cocoon.redirectTo(base + "properties/" + resourcePath);
}

function protected_removeproperty() {
  var baseUri = getBaseURI();
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var location = baseUri + resourcePath;
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
  cocoon.redirectTo(base + "properties/" + resourcePath);
}

// ---------------------------------------------- permission management

function protected_removePermission() {
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var subject      = cocoon.request.getParameter("subject");
  var privilege    = cocoon.request.getParameter("privilege");
  
  AdminHelper.removePermission(nat,principal,resourcePath,subject,privilege);
  cocoon.redirectTo(base + "permissions/" + resourcePath);
}

function protected_addPermission() {
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var subject      = cocoon.request.getParameter("subject");
  var action       = cocoon.request.getParameter("action");
  var inheritable  = cocoon.request.getParameter("inheritable");
  var negative     = cocoon.request.getParameter("negative");
  
  AdminHelper.addPermission(nat,principal,resourcePath,subject,action,inheritable,negative);
  cocoon.redirectTo(base + "permissions/" + resourcePath);
}

// ---------------------------------------------- lock management

function protected_removelock() {
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var objectUri    = cocoon.request.getParameter("objectUri");
  var lockId       = cocoon.request.getParameter("lockId");
  
  AdminHelper.removeLock(nat,principal,objectUri,lockId);
  cocoon.redirectTo(base + "locks/" + resourcePath);
}

function protected_addlock() {
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var subject      = cocoon.request.getParameter("subject");
  var type         = cocoon.request.getParameter("type");
  var exclusive    = cocoon.request.getParameter("exclusive");
  var inheritable  = cocoon.request.getParameter("inheritable");
  
  AdminHelper.addLock(nat,principal,resourcePath,subject,type,exclusive,inheritable);
  
  cocoon.redirectTo(base + "locks/" + resourcePath);
}

// ---------------------------------------------- user management

function protected_adduser() {
  var username = cocoon.request.getParameter("username");
  var password = cocoon.request.getParameter("password");
  
  AdminHelper.addUser(nat,principal,username,password);
  cocoon.redirectTo(base + "users");
}

function protected_addgroup() {
  var groupname = cocoon.request.getParameter("groupname");
  
  AdminHelper.addGroup(nat,principal,groupname);
  cocoon.redirectTo("users");
}

function protected_removeobject() {
  var objecturi = cocoon.request.getParameter("objecturi");
  
  AdminHelper.removeObject(nat,principal,objecturi);
  cocoon.redirectTo(base + "users");
}

function protected_addmember() {
  var objecturi  = cocoon.request.getParameter("objecturi");
  var subjecturi = cocoon.request.getParameter("subjecturi");
  
  AdminHelper.addMember(nat,principal,objecturi,subjecturi);
  cocoon.redirectTo(base + "users");
}

function protected_removemember() {
  var objecturi  = cocoon.request.getParameter("objecturi");
  var subjecturi = cocoon.request.getParameter("subjecturi");
  
  AdminHelper.removeMember(nat,principal,objecturi,subjecturi);
  cocoon.redirectTo(base + "users");
}


// ---------------------------------------------- screens

function screen_permissions() {
  var path   = cocoon.parameters["path"];
  var permissions = AdminHelper.listPermissions(nat,principal,path);
  cocoon.sendPage("screens/permissions.jx",{permissions:permissions});
}

function screen_locks() {
  var path   = cocoon.parameters["path"];
  var locks = AdminHelper.listLocks(nat,principal,path);
  cocoon.sendPage("screens/locks.jx",{locks:locks});
}

function screen_privileges() {
  var privileges = AdminHelper.listPrivileges(nat,principal);
  cocoon.sendPage("screens/privileges.jx",{privileges:privileges});
}

function screen_groups() {
  var groups = AdminHelper.listGroups(nat,principal,"/groups");
  cocoon.sendPage("screens/groups.jx",{groups:groups});
}

function screen_roles() {
  var roles = AdminHelper.listGroups(nat,principal,"/roles");
  cocoon.sendPage("screens/roles.jx",{roles:roles});
}

function screen_users() {
  var users = AdminHelper.listUsers(nat,principal);
  cocoon.sendPage("screens/users.jx",{users:users});
}
