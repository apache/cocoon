
importPackage(Packages.org.apache.cocoon.components.slide);
importPackage(Packages.org.apache.cocoon.samples.slide);
importPackage(Packages.org.apache.cocoon.components.source.helpers);
importPackage(Packages.org.apache.excalibur.source);
importPackage(Packages.org.apache.excalibur.xml.dom);

var repository = cocoon.getComponent("org.apache.cocoon.components.repository.SourceRepository");
var resolver = cocoon.getComponent(SourceResolver.ROLE);
var slide = cocoon.getComponent(SlideRepository.ROLE);
var nat = slide.getNamespaceToken("cocoon");

// ---------------------------------------------- utility functions

function getBaseURI() {
  var namespace = cocoon.parameters["namespace"];
  var caller    = cocoon.parameters["caller"];
  return "slide://" + caller + "@" + namespace + "/";
}

// ---------------------------------------------- file management

// make a new collection
function public_mkcol() {
  var baseUri        = getBaseURI();
  var parentPath     = cocoon.request.getParameter("parentPath");
  var collectionName = cocoon.request.getParameter("collectionName");
  var location = baseUri + parentPath + collectionName;
  var status = repository.makeCollection(location);

  cocoon.redirectTo("content/" + parentPath);
}

// upload a file
function public_upload() {
  var baseUri      = getBaseURI();
  var parentPath   = cocoon.request.getParameter("parentPath");
  var resourceName = cocoon.request.getParameter("resourceName");
  var dest = baseUri + parentPath + resourceName;
  var src  = "upload://uploadFile";
  
  var status = repository.save(src,dest);
  cocoon.redirectTo("content/" + parentPath);
}

// delete a resource
function public_delete() {
  var baseUri = getBaseURI();
  var parentPath = cocoon.request.getParameter("parentPath");
  var resourceName = cocoon.request.getParameter("resourceName");
  var location = baseUri + parentPath + resourceName;
  
  var status = repository.remove(location);
  cocoon.redirectTo("content/" + parentPath);
}

// ---------------------------------------------- property management

function public_addproperty() {
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
    cocoon.log.info("setting property " + property + " on source " + location);
    source.setSourceProperty(property);
  }
  finally {
    if (source != null) {
      resolver.release(source);
    }
  }
  cocoon.redirectTo("properties/" + resourcePath);
}

function public_removeproperty() {
  var baseUri = getBaseURI();
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var location = baseUri + resourcePath;
  var source = null;
  try {
    source = resolver.resolveURI(location);
    var name      = cocoon.request.getParameter("name");
    var namespace = cocoon.request.getParameter("namespace");
    cocoon.log.info("removing property " + namespace + "#" + name + " from source " + location);
    source.removeSourceProperty(namespace,name);
  } finally {
    if (source != null) {
      resolver.release(source);
    }
  }
  cocoon.redirectTo("properties/" + resourcePath);
}

// ---------------------------------------------- lock management

function public_removelock() {
  var baseUri = getBaseURI();
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var subject = cocoon.request.getParameter("subject");
  var location = baseUri + resourcePath;
  
  cocoon.log.info("removing lock " + subject + " from source " + location);
  
  // TODO: remove lock
  
  cocoon.redirectTo("locks/" + resourcePath);
}

function public_addlock() {
  var baseUri = getBaseURI();
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var subject      = cocoon.request.getParameter("subject");
  var type         = cocoon.request.getParameter("type");
  var inheritable  = cocoon.request.getParameter("inheritable");
  var exclusive    = cocoon.request.getParameter("exclusive");
  var location = baseUri + resourcePath;
  
  cocoon.log.info("adding lock " + subject + " to source " + location);
  
  // TODO: add lock
  
  cocoon.redirectTo("locks/" + resourcePath);
}

// ---------------------------------------------- user management

function public_adduser() {
  var username = cocoon.request.getParameter("username");
  var password = cocoon.request.getParameter("password");
  var role     = cocoon.request.getParameter("role");
  var caller   = cocoon.parameters["caller"];
  
  AdminHelper.addUser(nat,caller,username,password,role);
  cocoon.redirectTo("users/");
}

function public_removeuser() {
  var username = cocoon.request.getParameter("username");
  var caller   = cocoon.parameters["caller"];
  
  AdminHelper.removeUser(nat,caller,username);
  cocoon.redirectTo("users/");
}

function public_addgroup() {
  var groupname = cocoon.request.getParameter("groupname");
  var caller    = cocoon.parameters["caller"];
  
  AdminHelper.addGroup(nat,caller,groupname);
  cocoon.redirectTo("users/");
}

function public_removegroup() {
  var groupname = cocoon.request.getParameter("groupname");
  var caller    = cocoon.parameters["caller"];
  
  AdminHelper.removeGroup(nat,caller,groupname);
  cocoon.redirectTo("users/");
}

function public_addmember() {
  var username  = cocoon.request.getParameter("username");
  var groupname = cocoon.request.getParameter("groupname");
  var caller    = cocoon.parameters["caller"];
  
  AdminHelper.addGroupMember(nat,caller,groupname,username);
  cocoon.redirectTo("users/");
}

function public_removemember() {
  var username  = cocoon.request.getParameter("username");
  var groupname = cocoon.request.getParameter("groupname");
  var caller    = cocoon.parameters["caller"];
  
  AdminHelper.removeGroupMember(nat,caller,groupname,username);
  cocoon.redirectTo("users/");
}

// ---------------------------------------------- permission management

function public_removePermission() {
  var caller       = cocoon.parameters["caller"];
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var subject      = cocoon.request.getParameter("subject");
  var action       = cocoon.request.getParameter("action");
  
  AdminHelper.removePermission(nat,caller,resourcePath,subject,action);
  cocoon.redirectTo("permissions/" + resourcePath);
}

function public_addPermission() {
  var caller       = cocoon.parameters["caller"];
  var resourcePath = cocoon.request.getParameter("resourcePath");
  var subject      = cocoon.request.getParameter("subject");
  var action       = cocoon.request.getParameter("action");
  var inheritable  = cocoon.request.getParameter("inheritable");
  var negative     = cocoon.request.getParameter("negative");
  
  AdminHelper.addPermission(nat,caller,resourcePath,subject,action,inheritable,negative);
  cocoon.redirectTo("permissions/" + resourcePath);
}

// ---------------------------------------------- screens

function screen_authenticate() {
  var userid = cocoon.request.getParameter("userid");
  var password = cocoon.request.getParameter("password");
  cocoon.sendPage("screens/authentication.jx",{id:userid,role:"root"});
}

function screen_permissions() {
  var caller = cocoon.parameters["caller"];
  var path   = cocoon.parameters["path"];
  
  var permissions = AdminHelper.listPermissions(nat,caller,path);
  cocoon.sendPage("screens/permissions.jx",{permissions:permissions});
}

function screen_actions() {
  var caller = cocoon.parameters["caller"];
  
  var actions = AdminHelper.listActions(nat,caller);
  cocoon.sendPage("screens/actions.jx",{actions:actions});
}

function screen_groups() {
  var caller = cocoon.parameters["caller"];
  
  var groups = AdminHelper.listGroups(nat,caller);
  cocoon.sendPage("screens/groups.jx",{groups:groups});
}

function screen_roles() {
  var caller = cocoon.parameters["caller"];
  
  var roles = AdminHelper.listRoles(nat,caller);
  cocoon.sendPage("screens/roles.jx",{roles:roles});
}

function screen_users() {
  var caller = cocoon.parameters["caller"];
  
  var users = AdminHelper.listUsers(nat,caller);
  cocoon.sendPage("screens/users.jx",{users:users});
}

