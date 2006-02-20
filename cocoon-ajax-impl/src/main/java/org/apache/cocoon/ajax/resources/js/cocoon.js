/*
 * Copyright 2006 The Apache Software Foundation.
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
/**
 * Bootstrap file to plug Cocoon in the Dojo package system. It declares the
 * "cocoon" root package and hooks in the script loading engine to handle
 * the per-block organization of JS files in Cocoon.
 * <p>
 * A typical usage scenario would be:
 * <pre>
 *   <script src="resources/dojo/dojo.js">
 *   <script src="resources/ajax/js/cocoon.js"/>
 *   <script>
 *      dojo.require("cocoon.forms.Form");
 *      ...
 *    </script>
 * </pre>
 *
 * @version $Id$
 */

dojo.provide("cocoon");

// Setup hooks to load JS files provided by Cocoon blocks
(function() {
    // base path is "resources/dojo". Move to back to "resources"
    var rsrcPath = "../";
  
    // cocoon.foo.bar is loaded from "resources/foo/js/bar.js"
    var orgRequire = dojo.require;
    dojo.require = function() {
        var module = arguments[0];
        var match = module.match(/^(cocoon\.([^\.]*))/);
        if (match) {
            var block = match[1];
            var name = match[2];
            if (dojo.hostenv.getModulePrefix(block) == block) {
                // Register the block's path
                //dojo.debug("Registering Cocoon module = " + arguments[0]);
                var prefix = rsrcPath + name + "/js";
                dojo.hostenv.setModulePrefix(block, prefix);           
                //dojo.debug("calling setModulePrefix("+block+", "+ prefix +")");
            }
        }
        // Continue normal processing
        return orgRequire.apply(this, arguments);
    };

    // cocoon.foo (single JS file for a block) is loaded from "resources/foo/js/common.js"
    var orgLoadUri = dojo.hostenv.loadUri;
    dojo.hostenv.loadUri = function(uri) {
        // match 'xxx/dojo/cocoon/yyy.js
        var match = uri.match(/^(.*)\/dojo\/cocoon\/([^\/]*)\.js$/);
        if (match) {
            arguments[0] = match[1] + "/cocoon/" + match[2] + "/js/common.js";
        }
        return orgLoadUri.apply(this, arguments);
    }
})();
