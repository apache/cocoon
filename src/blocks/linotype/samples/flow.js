var repo = new Packages.org.apache.cocoon.components.Repository.getInstance();

var users;
var home;
var userid = "";
var username = "";

/*
 * Main entry point for the flow. This is where user authorization takes place.
 */
function main() {
    var action = cocoon.parameters["action"];
    home = cocoon.parameters["home"];
    var args = new Array(3);
    args[0] = cocoon.parameters["id"];
    args[1] = cocoon.parameters["type"];
    args[2] = cocoon.parameters["subpage"];

    if ((userid == undefined) || (userid == "")) {
        login(action, args);
    }
                
    invoke(action, args);
}

/*
 * If the user is not yet authorized, then authentication takes place
 */
function login(action, args) {
    var name = "";
    var password = "";
    var userError = "";
    var passError = "";

    while (true) {
        cocoon.sendPageAndWait("screen/login", { username : name, userError : userError, passError : passError});

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
        
    // cocoon.createSession();
}

/*
 * Now that the user has been authenticated and authorized, execute what 
 * he's asking for. This method checks the flowscript to see if the
 * called action exists as a flowscript function. If so, it's called with
 * the given arguments. If not, the appropriate admin screen is sent
 * to the user.
 */
function invoke(action, args) {
    func = this[action];
    if (func != undefined) {
        func.apply(this,args);
    } else {
        cocoon.sendPage("screen/" + action, {"user" : username});
    }
}

// ----------------------------- actions ----------------------------------

/*
 * The logout action clears the userid from the session thus signaling
 * that the user has logged out and should be further considered authenticated. 
 */
function logout() {
    userid = "";
    cocoon.sendPage("screen/logout");
}
   
/*
 * The edit action performs the editing subflow.
 */
function edit(id,type,subpage) {
    var repository = home + "repository/" + type + "/";
    
    if (id == "template") {
        id = repo.getID(repository);
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
                cocoon.processPipelineTo("/samples/linotype/action/save-" + type,{},output);
                output.close();
                repo.fomSave(cocoon, document);
                if (action == "finish") break;
            }                   
        }

        cocoon.redirectTo("../../../" + type);
    }
}

