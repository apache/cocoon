
importPackage(Packages.org.apache.cocoon.components.slide);
importPackage(Packages.org.apache.cocoon.components.source.helpers);
importPackage(Packages.org.apache.excalibur.source);

var repository = cocoon.getComponent("org.apache.cocoon.components.repository.SourceRepository");
var provider = cocoon.getComponent(PrincipalProvider.ROLE + "Selector").select("slide");
var resolver = cocoon.getComponent(SourceResolver.ROLE);

// ---------------------------------------------- utility functions

function getBaseURI() {
  var namespace = cocoon.parameters["namespace"];
  var caller    = cocoon.parameters["caller"];
  return "slide://" + caller + "@" + namespace;
}

function getParentPath() {
  var parentPath = cocoon.request.getParameter("parentPath");
  if (!parentPath.lastIndexOf('/') != parentPath.length-1) {
      parentPath = parentPath + "/";
  }
  return parentPath;
}

function getResourcePath() {
  return cocoon.request.getParameter("resourcePath");
  
}

// ---------------------------------------------- authentication

function authenticate() {
  var userid = cocoon.request.getParameter("userid");
  var password = cocoon.request.getParameter("password");
  // TODO: if (password = correct) ..
  cocoon.sendPage("authentication",{id:userid,role:"root"});
}

// ---------------------------------------------- file management

// make a new collection
function public_mkcol() {
  var baseUri        = getBaseURI();
  var parentPath     = getParentPath();
  var collectionName = cocoon.request.getParameter("collectionName");
  var location = baseUri + parentPath + collectionName;
  var status = repository.makeCollection(location);

  cocoon.redirectTo("content" + parentPath);
}

// upload a file
function public_upload() {
  var baseUri      = getBaseURI();
  var parentPath   = getParentPath();
  var resourceName = cocoon.request.getParameter("resourceName");
  var dest = baseUri + parentPath + resourceName;
  var src  = "upload://uploadFile";
  
  var status = repository.save(src,dest);
  cocoon.redirectTo("content" + parentPath);
}

// delete a resource
function public_delete() {
  var baseUri = getBaseURI();
  var parentPath = getParentPath();
  var resourceName = cocoon.request.getParameter("resourceName");
  var location = baseUri + parentPath + resourceName;
  
  var status = repository.remove(location);
  cocoon.redirectTo("content" + parentPath);
}

// ---------------------------------------------- property management

function public_addproperty() {
  var baseUri      = getBaseURI();
  var resourcePath = getResourcePath();
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
  cocoon.redirectTo("properties" + resourcePath);
}

function public_removeproperty() {
  var baseUri = getBaseURI();
  var resourcePath = getResourcePath();
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
  cocoon.redirectTo("properties" + resourcePath);
}

// ---------------------------------------------- lock management

function public_removelock() {
  var baseUri = getBaseURI();
  var resourcePath = getResourcePath();
  var subject = cocoon.request.getParameter("subject");
  var location = baseUri + resourcePath;
  
  cocoon.log.info("removing lock " + subject + " from source " + location);
  
  // TODO: remove lock
  
  cocoon.redirectTo("locks" + resourcePath);
}

function public_addlock() {
  var baseUri = getBaseURI();
  var resourcePath = getResourcePath();
  var subject      = cocoon.request.getParameter("subject");
  var type         = cocoon.request.getParameter("type");
  var inheritable  = cocoon.request.getParameter("inheritable");
  var exclusive    = cocoon.request.getParameter("exclusive");
  var location = baseUri + resourcePath;
  
  cocoon.log.info("adding lock " + subject + " to source " + location);
  
  // TODO: add lock
  
  cocoon.redirectTo("locks" + resourcePath);
}

// ---------------------------------------------- user management

function public_adduser() {
  var username = cocoon.request.getParameter("username");
  var password = cocoon.request.getParameter("password");
  var role     = cocoon.request.getParameter("role");
  var caller   = cocoon.parameters["caller"];

  provider.addPrincipal(new Principal(caller),new Principal(username,role,password));
  cocoon.redirectTo("users/");
}

function public_removeuser() {
  var username = cocoon.request.getParameter("username");
  var caller   = cocoon.parameters["caller"];
  
  provider.removePrincipal(new Principal(caller),new Principal(username));
  cocoon.redirectTo("users/");
}

function public_addgroup() {
  var groupname = cocoon.request.getParameter("groupname");
  var caller    = cocoon.parameters["caller"];

  provider.addPrincipalGroup(new Principal(caller),new PrincipalGroup(groupname));
  cocoon.sendPage("users/",null);
}

function public_removegroup() {
  var groupname = cocoon.request.getParameter("groupname");
  var caller    = cocoon.parameters["caller"];
  
  provider.removePrincipalGroup(new Principal(caller),new PrincipalGroup(groupname));
  cocoon.sendPage("users/",null);
}