
//importPackage(Packages.org.apache.cocoon.components.repository);
//var repository = cocoon.getComponent(SourceRepository.ROLE);
var repository = cocoon.getComponent("org.apache.cocoon.components.repository.SourceRepository");

function selectMethod() {
  var page = cocoon.parameters["page"];
  var method = cocoon.request.getMethod();
  cocoon.sendPage(method+"/"+page, null);
}

function sendStatus() {
  var status  = cocoon.parameters["status"];
  var msg = cocoon.parameters["message"];
  cocoon.sendPage("status/" + status, {message:msg});
}

function put() {
  var src  = cocoon.parameters["src"];
  var dest = cocoon.parameters["dest"];
  try {
    var status = repository.save(src,dest);
    cocoon.sendPage("status/" + status, {message:""});
  }
  catch (e) {
    cocoon.log.error(e);
    cocoon.sendPage("status/500",{message:"unknown error"});
  }
}

function remove() {
  var location = cocoon.parameters["location"];
  try {
    var status = repository.remove(location);
    cocoon.sendPage("status/" + status, {message:""});
  }
  catch (e) {
    cocoon.log.error(e);
    cocoon.sendPage("status/500",{message:"unknown error"});
  }
}

function mkcol() {
  var location = cocoon.parameters["location"];
  try {
    var status = repository.makeCollection(location);
    cocoon.sendPage("status/" + status, {message:""});
  }
  catch (e) {
    cocoon.log.error(e);
    cocoon.sendPage("status/500",{message:"unknown error"});
  }
}

function copy() {
  var from = cocoon.parameters["from"];
  var to   = cocoon.parameters["to"];
  try {
    var status = repository.copy(from,to);
    cocoon.sendPage("status/" + status, {message:""});
  } catch (e) {
    cocoon.log.error("status/500",{message:""});
  }
}

function move() {
  var from = cocoon.parameters["from"];
  var to   = cocoon.parameters["to"];
  try {
    var status = repository.move(from,to);
    cocoon.sendPage("status/" + status, {message:""});
  } catch (e) {
    cocoon.log.error("status/500",{message:""});
  }
}

function options() {
  cocoon.response.setHeader("DAV","1");
  var options = "OPTIONS,GET,HEAD,POST,DELETE,TRACE,PUT" 
              + ",MKCOL,PROPFIND,PROPPATCH,COPY,MOVE";
  cocoon.response.setHeader("Allow",options);
  cocoon.sendPage("status/200",{});
}

/*
function getDestination() {
  var destination = cocoon.request.getHeader("Destination");
  var index = destination.indexOf('://');
  if (index != -1) {
    destination = destination.substring(index+3);
    index = destination.indexOf('/');
    if (index != -1) {
      destination = destination.substring(index);
    }
  }
  return destination;
}

function getUserAgent() {
  var userAgent = cocoon.request.getHeader("User-Agent");
  var index = userAgent.indexOf('/');
  if (index != -1) {
    return userAgent.substring(0,index);
  }
  return userAgent;
}
*/