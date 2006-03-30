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

function showString(parameter) {
    var replaceme = "@REPLACEME@";
    print("parameter = " + parameter, " replaceme = " + replaceme);
    cocoon.sendPage("page/showString", { "parameter" : parameter, "replaceme" : replaceme });
}


var counter;
var result;

function factorial() {
    // Init
    var topmost;
    if (counter == undefined) {
        topmost = "yep";
        counter = 0;
        result = 1;
    }

    print("Factorial '" + counter + "', '" + result + "'");

    if (counter < cocoon.request.getParameter("n")) {
        result = result * ++counter;
        // Recurse
        cocoon.processPipelineTo("factorial", {}, new org.apache.cocoon.util.NullOutputStream());
    }

    cocoon.sendPage("page/showString",
                    { "replaceme" : "Factorial of " + counter + " is ...",
                      "parameter" : result });

    // Clear
    if (topmost != undefined) {
        result = undefined;
        counter = undefined;
    }
}
