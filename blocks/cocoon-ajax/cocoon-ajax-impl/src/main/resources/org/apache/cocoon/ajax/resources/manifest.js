/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 /*
  * Mapping of all widget short names to their full package names
  * This is used for widget autoloading - no dojo.require() is necessary.
  * If you use a widget in markup or create one dynamically, then this
  * mapping is used to find and load any dependencies not already loaded.
  * You should use your own namespace for any custom widgets (remember to register it).
  * For extra widgets you use, dojo.require() may still be used to explicitly load them.
  *
  * NOTE: Introduced in 2.1.11, replaces functionality in cocoon.js
  *
  * @version $Id$
  */
  
dojo.provide("cocoon.ajax.manifest");
dojo.require("dojo.ns");

(function(){
	var map = {
		html: {
			"formuploadprogress"        : "cocoon.ajax.FormUploadProgress",
			"partiallink"               : "cocoon.ajax.PartialLink"
            // register new Widgets in the cocoon.ajax namespace here
		},
		svg: {
		    // register svg widgets here
		},
		vml: {
		    // register vml widgets here
		}
	};
	
	function ajaxResolver(name, domain){
		if(!domain){ domain="html"; }
		if(!map[domain]){ return null; }
		return map[domain][name];    
	}
    
    dojo.registerModulePath("cocoon.ajax", "servlet:/resource/external/ajax/js");
	dojo.registerNamespace("ajax", "cocoon.ajax", ajaxResolver);

})();
