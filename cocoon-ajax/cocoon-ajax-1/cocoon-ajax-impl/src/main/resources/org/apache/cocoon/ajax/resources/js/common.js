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
 * Frequently used Ajax functions
 */

dojo.provide("cocoon.ajax");
dojo.provide("cocoon.ajax.common");
dojo.require("cocoon.ajax.insertion");

dojo.lang.mixin(cocoon.ajax, {
    /**
     * Update the current page with some remote content.
     * @param href the URL of the remote content
     * @param target the update target. It can be either an element (DOM element or id), or
     *        a string in the "{insertion}#{target-id}" format (e.g. "bottom#event-list").
     * @param insertion the insertion method (see cocoon.ajax.insertion), either a sa function
     *        or as a string. If the insertion is specified in target, that last one has
     *        precedence. If no insertion is speficied, it default to "inside".
     */
    update: function(href, target, insertion) {
        
        // If target is a string, parse it. Otherwise, assume it's an element
        if (dojo.lang.isString(target)) {
            var split = target.split("#");
            if (split.length == 2) {
                insertion = split[0];
                target = dojo.byId(split[1]);
            } else {
                target = dojo.byId(target);
            }
        }
        
        if (dojo.lang.isString(insertion)) {
            insertion = cocoon.ajax.insertion[insertion];
        }

        insertion = insertion || cocoon.ajax.insertion.inside;
        
        dojo.io.bind({
		    url: href,
		    load: function(type, data, evt){
		        insertion(target, data);
		    },
		    mimetype: "text/plain"
		    // TODO: add an error-handling function
		});
        
    },

    /**
     */
    periodicalUpdate: function(delay, href, target, insertion) {
        dojo.require("dojo.animation.Timer");
        var timer = new dojo.animation.Timer(delay);
        timer.onTick = function() {
            cocoon.ajax.update(href, target, insertion);
        };
        
        timer.onStart = timer.onTick;
        timer.start();
        return timer;
    }
});