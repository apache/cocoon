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

//importPackage(Packages.org.apache.cocoon.components.repository);
//var repository = cocoon.getComponent(SourceRepository.ROLE);
var repository = cocoon.getComponent("org.apache.cocoon.components.repository.SourceRepository");

function selectMethod() {
  var page = cocoon.parameters["page"];
  var method = cocoon.request.getMethod();
  cocoon.sendPage(method+"/"+page, null);
}

function sendStatus(sc) {
  cocoon.sendStatus(sc);
}

function put() {
  var src  = cocoon.parameters["src"];
  var dest = cocoon.parameters["dest"];
  try {
    var status = repository.save(src,dest);
    sendStatus(status);
  }
  catch (e) {
    cocoon.log.error(e);
    sendStatus(500);
  }
}

function remove() {
  var location = cocoon.parameters["location"];
  try {
    var status = repository.remove(location);
    sendStatus(status);
  }
  catch (e) {
    cocoon.log.error(e);
    sendStatus(500);
  }
}

function mkcol() {
  var location = cocoon.parameters["location"];
  try {
    var status = repository.makeCollection(location);
    sendStatus(status);
  }
  catch (e) {
    cocoon.log.error(e);
    sendStatus(500);
  }
}

function copy() {
  var from      = cocoon.parameters["from"];
  var to        = cocoon.parameters["to"];
  var recurse   = isRecurse(cocoon.parameters["depth"]);
  var overwrite = isOverwrite(cocoon.parameters["overwrite"]);
  try {
    var status = repository.copy(from,to,recurse,overwrite);
    sendStatus(status);
  } catch (e) {
    sendStatus(500);
  }
}

function move() {
  var from      = cocoon.parameters["from"];
  var to        = cocoon.parameters["to"];
  var recurse   = isRecurse(cocoon.parameters["depth"]);
  var overwrite = isOverwrite(cocoon.parameters["overwrite"]);
  try {
    var status = repository.move(from,to,recurse,overwrite);
    sendStatus(status);
  } catch (e) {
    sendStatus(500);
  }
}

function options() {
  cocoon.response.setHeader("DAV","1");
  var options = "OPTIONS,GET,HEAD,POST,DELETE,TRACE,PUT" 
              + ",MKCOL,PROPFIND,PROPPATCH,COPY,MOVE";
  cocoon.response.setHeader("Allow",options);

  //interoperability with Windows 2000
  var w2kDAVDiscoverAgent = "Microsoft Data Access Internet"
                          + " Publishing Provider Protocol Discovery";
  if (cocoon.request.getHeader("User-Agent") == w2kDAVDiscoverAgent) {
      cocoon.response.setHeader("MS-Author-Via","DAV");
  }

  sendStatus(200);
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
  else if (depth == 'Infinity') {
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
