/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
function passcode(length) {
  var alphabet = [ 'a', 'a', 'a', 'b', 'c', 'd', 'e', 'e', 'e', 'f', 'g', 'h', 'i',
                   'i', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'o', 'o', 'p', 'q', 'r',
                   's', 't', 'u', 'u', 'u', 'v', 'w', 'x', 'y', 'z' ]; 

  var result = "";
  for (var x = 0; x < length; x++) {
    var offset = Math.random() * alphabet.length;
    result += alphabet[Math.floor(offset)];
  }
  return result;
} 

function simple() {
  var session = cocoon.session;

  session.setAttribute("captcha", passcode(7));
  cocoon.sendPageAndWait("simple.jx");
  var parameters = null;

  while (true) {
    parameters = {
      "supplied": cocoon.request["captcha"],
      "expected": session.getAttribute("captcha"),
    };

    if (parameters['supplied'].equals(parameters['expected'])) break;

    session.setAttribute("captcha", passcode(7));
    cocoon.sendPageAndWait("simple-failure.jx", parameters);
  }

  session.invalidate();
  cocoon.sendPageAndWait("simple-success.jx", parameters);
}
