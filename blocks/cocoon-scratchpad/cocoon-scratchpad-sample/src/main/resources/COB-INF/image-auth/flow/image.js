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

function main() {
    
    var secret = generateSecret();
   
    while (true) {
        cocoon.sendPageAndWait("main.jxt", {secret:secret});

        if (cocoon.parameters.msg == "image") {
            cocoon.sendPage("auth.jpg", {text:secret});
            return;
        } else {
    
            var input = cocoon.request.get("secret");

            if (input == secret) {
                break;
            }
        }
    }
    
    cocoon.sendPage("success.jxt", {secret:secret});
    
}

function generateSecret() {
    
      var characters = "!@#$%^&*(){}[]<>.,ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    
      var passwordlength = 7;
    
      var password = "";
      var randomnumber = 0;
      
      for (var n = 0; n < passwordlength; n++) {
         randomnumber = Math.floor(characters.length*Math.random());
         password += characters.substring(randomnumber,randomnumber + 1) 
      }
      
      return password;
}
    
