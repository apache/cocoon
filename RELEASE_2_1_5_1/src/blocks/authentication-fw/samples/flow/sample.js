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
cocoon.load("resource://org/apache/cocoon/webapps/authentication/flow/javascript/auth.js");

function isLoggedIn() {
  var handler = cocoon.parameters["handler"];

  if (auth_isAuthenticated(handler)) {
    success();
  } else {
    failure();
  }
}

function protect() {
  var handler = cocoon.parameters["handler"];

  if (auth_checkAuthentication(handler,"")) {
    success();
  } else {
    // already redirected by auth_checkAuthentication
  }
}

function login() {
  var handler = cocoon.parameters["handler"];

  if (auth_isAuthenticated(handler)) {
    success();
  } else if (auth_login(handler, null, cocoon.parameters)) {
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
  } else if (redirect != null) {
    cocoon.redirectTo(redirect);
  } else {
    throw new Error("No protected redirection parameter given");
  }
}

function failure() {

  var internal = cocoon.parameters["failure-internal"];
  var redirect = cocoon.parameters["failure-redirect"];  

  if (internal != null) {
    cocoon.sendPage(internal);
  } else if (redirect != null) {
    cocoon.redirectTo(redirect);
  } else {
    // Why does this throw cause an error?
    throw new Error("No failure redirection parameter given");
  }
}
