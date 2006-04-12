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
AO_FOM_Cocoon.suicide = new Continuation();

AO_FOM_Cocoon.prototype.sendPageAndWait = function(uri, bizData) {
    this.sendPage(uri, bizData, new Continuation());
    AO_FOM_Cocoon.suicide();
}

AO_FOM_Cocoon.prototype.handleContinuation = function(k, wk) {
    k(wk);
}
