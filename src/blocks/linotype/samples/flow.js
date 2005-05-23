/*
* Copyright 2002-2004 The Apache Software Foundation
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
var repo = new Packages.org.apache.cocoon.components.Repository.getInstance();

var users;
var home;
var base;
var userid = "";
var username = "";

/*
 * Main entry point for the flow. This is where user authorization takes place.
 */
function main(action,root,baseURL) {
 	var action = cocoon.parameters.page;
 	var root = cocoon.parameters.root;
 	var base = cocoon.parameters.baseURL;

 	home = root + "/";

    if ((userid == undefined) || (userid == "")) {
        login(action);
    }

    invoke(action);
}

/*
 * If the user is not yet authorized, than authentication takes place
 */
function login(action) {
    var name = "";
    var password = "";
    var userError = "";
    var passError = "";

    while (true) {
        cocoon.sendPageAndWait("screen/login", { base : base, username : name, userError : userError, passError : passError});

        name = cocoon.request.getParameter("username");
        password = cocoon.request.getParameter("password");
        
        if (users == undefined) {
			var stream = new java.io.FileInputStream(home + "linotype.users.properties");
			users = new Packages.org.apache.cocoon.components.UserManager.getInstance(stream);
		}
                
        if (users.isValidName(name)) {
            if (users.isValidPassword(name,password)) {
                userid = name;
                username = users.getFullName(name);
                break;
            } else {
                userError = "";
                passError = "Password doesn't match";
            }
        } else {
            userError = "User not found";
            passError = "";
        }
    }
}

/*
 * Now that the user has been authenticated and authorized, execute what 
 * he's asking for. This method checks the flowscript to see if the
 * called action exists as a flowscript function. If so, it's called with
 * the given arguments. If not, the appropriate admin screen is sent
 * to the user.
 */
function invoke(action) {
    var func = this[action];
    if (func != undefined) {
        func.apply(this,new Array());
    } else {
        cocoon.sendPage("screen/" + action, { base : base, user : username});
    }
}

// ----------------------------- actions ----------------------------------

/*
 * The logout action clears the userid from the session thus signaling
 * that the user has logged out and should be further considered authenticated. 
 */
function logout() {
    userid = "";
    cocoon.sendPage("screen/logout", { base : base });
}
   
/*
 * The edit action performs the editing subflow.
 */
function edit() {
    var id = cocoon.parameters.id;
    var type = cocoon.parameters.type;
    var subpage = cocoon.parameters.subpage;

    var repository = home + "repository/" + type + "/";

    if (id == "template") {
        id += "-" + getID(repository);
        repo.copy(repository + "template", repository + id);
        cocoon.redirectTo("../" + id + "/");
    } else if ((subpage != undefined) && (subpage != "")) {
        cocoon.sendPage("edit/" + type + "/" + id + "/" + subpage,{});
    } else {
        var document = repository + id;

        while (true) {
            var versions = repo.getVersions(document);
            cocoon.sendPageAndWait("edit/" + type + "/" + id + "/", { 
                userid : userid, 
                username : username, 
                versions : versions,
                innerHTML : cocoon.request.getParameter("innerHTML") 
            });
            var action = cocoon.request.getParameter("action");
            if (action == "delete") {
                repo.remove(document);
                break;
            } else if (action == "restore") {
                var version = cocoon.request.getParameter("version");
                repo.revertFrom(document,version);
            } else {
                var output = repo.getOutputStream(document);
                cocoon.processPipelineTo("action/save-" + type,{},output);
                output.close();
                repo.fomSave(cocoon, document);
                if (action == "finish") {
                	break;
                }
                if (action == "publish") {
                	if (id.indexOf("template-") > -1) {
                		var realID = id.substring(id.indexOf("-") + 1);
			        	repo.copy(document, repository + realID);
			        	repo.remove(document);
			    }
                	break;
                }
            }                   
        }

        cocoon.redirectTo("../../../" + type);
    }
}

function getID(repository) {

	var dirs = new java.io.File(repository).listFiles();
	var id = 0;

	for (var i = 0; i < dirs.length; i++) {
		if (dirs[i].isDirectory()) {
			var name = dirs[i].getName();
            if (name.indexOf("template-") > -1) {
            	name = name.substring(name.indexOf("-") + 1);
            }
			var localid = parseInt(name);
			if (localid > id) id = localid;
		}
	}
	
	return ++id;
}
