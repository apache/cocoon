/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
dojo.require("dojo.lfx.html");


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
        dojo.require("dojo.lang.timing.Timer");
        var timer = new dojo.lang.timing.Timer(delay);
        timer.onTick = function() {
            cocoon.ajax.update(href, target, insertion);
        };

        timer.onStart = timer.onTick;
        timer.start();
        return timer;
    },

    // Update effects. These function can be used to set cocoon.ajax.BUHandler.highlight
    effects: {
        // highlight effects - transition the background colour
        highlight: { // these are intended to look like a semi-opaque layer of colour over white
            yellow: function(node) {
                dojo.lfx.html.highlight(node, [240, 238, 133], 1000).play(0);
            },
            blue: function(node) {
                dojo.lfx.html.highlight(node, [141, 133, 252], 1000).play(0);
            },
            red: function(node) {
                dojo.lfx.html.highlight(node, [220, 133, 133], 1000).play(0);
            },
            green: function(node) {
                dojo.lfx.html.highlight(node, [159, 223, 133], 1000).play(0);
            },
            grey: function(node) {
                dojo.lfx.html.highlight(node, [128, 128, 128], 1000).play(0);
            },
            purple: function(node) {
                dojo.lfx.html.highlight(node, [197, 133, 220], 1000).play(0);
            },
            orange: function(node) {
                dojo.lfx.html.highlight(node, [252, 202, 133], 1000).play(0);
            }
        },
        blink: function(node) { // hide then show the node
            var opacity = dojo.html.getOpacity(node);
            dojo.html.setOpacity(node, 0.2);
            setTimeout(function() {dojo.html.setOpacity(node, opacity);}, 600);
        }
        // add more effects?
    }
});