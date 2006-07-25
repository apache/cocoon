/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// SVN $ID:$
cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js");
importPackage(Packages.org.apache.cocoon.portal);

function getPTM() {
     return cocoon.getComponent("org.apache.cocoon.portal.tools.PortalToolManager");
}

function relPTM(ptm) {
	cocoon.releaseComponent(ptm);
}