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
// the applied scripts are called in the order found at the basescript
cocoon.apply( "aspects1.js" );
cocoon.apply( "aspects2.js" );

function interceptionTest() {
  var uri = "page/info";
  cocoon.log.error ( "interceptionTest - baseScript" );
  var x = callAnotherFunction();
  cocoon.sendPageAndWait(uri, { });
}

function callAnotherFunction() {
  var x = 1;
  cocoon.log.error ( "callAnotherFunction() - baseScript" );     
  if( x == 2 ) {
    return 72;
  }
  return 27;
}

function testSendPageAndWait() { 
  woody.send( "bla" );    
  cocoon.sendPageAndWait("xxx",{});
  cocoon.sendPageAndWait("yyy",{});
  cocoon.sendPageAndWait( "zzz", {} );
}
