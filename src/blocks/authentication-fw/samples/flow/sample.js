cocoon.load("resource://org/apache/cocoon/webapps/authentication/flow/javascript/auth.js");

// @TODO@ Get parameter_ removal working in auth.js
// @TODO@ Get flow redirects to be session aware
// @TODO@ Sort out error on clicking login when already logged in

function protect() {
  var handler = cocoon.parameters["handler"];

  if (auth_isAuthenticated(handler)) {
    success();
  } else {
    failure();
  }
}

function login() {
  var handler = cocoon.parameters["handler"];

  if (auth_isAuthenticated(handler)) {
    success();
  }
  if (auth_login(handler, null, cocoon.parameters)) {
    success();
  } else {
    failure();
  }
}

function logout() {
  var handler = cocoon.parameters["handler"];

  auth_logout(handler);
  failure();
}

function success() {
  var internal = cocoon.parameters["protected-internal"];
  var redirect = cocoon.parameters["protected-redirect"];
  
  if (internal != null) {
    cocoon.sendPage(internal);
  } else if (redirect+"" != "undefined") {
    cocoon.redirectTo(redirect); //THIS NEEDS TO BE A SESSION AWARE REDIRECT
  } else {
    throw new Error("No protected redirection parameter given");
  }
}

function failure() {

  var internal = cocoon.parameters["failure-internal"];
  var redirect = cocoon.parameters["failure-redirect"];  

  if (internal != null) {
    cocoon.sendPage(internal);
  } else if (typeof redirect+"" != "undefined") {
    cocoon.redirectTo(redirect); //THIS NEEDS TO BE A SESSION AWARE REDIRECT
  } else {
    // Why does this throw cause an error?
    throw new Error("No failure redirection parameter given");
  }
}
