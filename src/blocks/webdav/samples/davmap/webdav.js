
//importPackage(Packages.org.apache.cocoon.components.repository);
//var repository = cocoon.getComponent(SourceRepository.ROLE);
var repository = cocoon.getComponent("org.apache.cocoon.components.repository.SourceRepository");

function selectMethod() {
  var page = cocoon.parameters["page"];
  var method = cocoon.request.getMethod();
  cocoon.sendPage(method+"/"+page, null);
}

function sendStatus(status,msg) {
  //cocoon.response.setStatus(status);
  cocoon.sendPage("status/"+status,{message:msg});
}

function put() {
  var src  = cocoon.parameters["src"];
  var dest = cocoon.parameters["dest"];
  try {
    var status = repository.save(src,dest);
    sendStatus(status,"");
  }
  catch (e) {
    cocoon.log.error(e);
    sendStatus(500,"unknown error");
  }
}

function remove() {
  var location = cocoon.parameters["location"];
  try {
    var status = repository.remove(location);
    sendStatus(status,"");
  }
  catch (e) {
    cocoon.log.error(e);
    sendStatus(500,"unknown error");
  }
}

function mkcol() {
  var location = cocoon.parameters["location"];
  try {
    var status = repository.makeCollection(location);
    sendStatus(status,"");
  }
  catch (e) {
    cocoon.log.error(e);
    sendStatus(500,"unknown error");
  }
}

function copy() {
  var from      = cocoon.parameters["from"];
  var to        = cocoon.parameters["to"];
  var recurse   = isRecurse(cocoon.parameters["depth"]);
  var overwrite = isOverwrite(cocoon.parameters["overwrite"]);
  try {
    var status = repository.copy(from,to,recurse,overwrite);
    sendStatus(status,"");
  } catch (e) {
    sendStatus(500,"unknown error");
  }
}

function move() {
  var from      = cocoon.parameters["from"];
  var to        = cocoon.parameters["to"];
  var recurse   = isRecurse(cocoon.parameters["depth"]);
  var overwrite = isOverwrite(cocoon.parameters["overwrite"]);
  try {
    var status = repository.move(from,to,recurse,overwrite);
    sendStatus(status,"");
  } catch (e) {
    sendStatus(500,"unknown error");
  }
}

function options() {
  cocoon.response.setHeader("DAV","1");
  var options = "OPTIONS,GET,HEAD,POST,DELETE,TRACE,PUT" 
              + ",MKCOL,PROPFIND,PROPPATCH,COPY,MOVE";
  cocoon.response.setHeader("Allow",options);
  sendStatus(200,"");
}

/*
 * parse the depth header to find out if recursion
 * take place. (used by MOVE and COPY)
 */
function isRecurse(depth) {
  var recurse;
  if (depth == null || depth == '') {
    recurse = true;
  }
  else if (depth == 'infinity') {
    recurse = true;
  }
  else {
    recurse = false;
  }
  return recurse;
}

/*
 * convert the overwrite header into a boolean type
 */
function isOverwrite(header) {
  var overwrite = true;
  if (header == 'F') {
    overwrite = false;
  }
  return overwrite;
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