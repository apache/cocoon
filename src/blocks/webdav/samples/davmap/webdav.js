
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
