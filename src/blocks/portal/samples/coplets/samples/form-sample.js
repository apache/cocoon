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
// This is a simple flow script
// that can be used as an example for building forms
//
// The script gets the coplet id as a parameter. This id can be used
// as a unique key for the coplet.
//
// The script below doesn't use any continuations. It just checks:
// - if the user already has given some input (which is stored in
//   a session attribute)
// - if the user has just submitted some input
// - if a form should be displayed 
// - The function clear() clears the user input
//
function form() {
    // get the coplet id
    var cid = cocoon.parameters["copletId"];
    var pname = cid + "/myform";

    if ( cocoon.session.getAttribute(pname) == null ) {
        var name = cocoon.request.getParameter("name");
        if ( name == null ) {
            cocoon.sendPage("page/form", {});
        } else {
            cocoon.session.setAttribute(pname, name);
            cocoon.sendPage("page/received", {"name" : name});         
        }
    } else {
        var name = cocoon.session.getAttribute(pname);
        cocoon.sendPage("page/content", {"name" : name});         
    }
}

function clear() {
    // get the coplet id
    var cid = cocoon.parameters["copletId"];
    var pname = cid + "/myform";

    cocoon.session.removeAttribute(pname);
    form();
}
