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
//
// CVS $Id: Database.js,v 1.3 2004/03/06 02:25:37 antonio Exp $
//
// Prototype Database API
//
// TBD: Move this Database stuff to its own library outside of flow
//

defineClass("org.apache.cocoon.components.flow.javascript.ScriptableConnection");
defineClass("org.apache.cocoon.components.flow.javascript.ScriptableResult");

Database.getConnection = function(selectorValue) {
    var selector = cocoon.getComponent(Packages.org.apache.avalon.excalibur.datasource.DataSourceComponent.ROLE + "Selector");
    try {
	var ds = selector.select(selectorValue);
	return new Database(ds.getConnection());
    } finally {
	cocoon.releaseComponent(selector);
    }
}

