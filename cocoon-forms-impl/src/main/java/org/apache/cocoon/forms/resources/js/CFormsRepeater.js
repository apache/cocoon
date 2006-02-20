/*
 * Copyright 1999-2006 The Apache Software Foundation.
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
dojo.provide("cocoon.forms.CFormsRepeater");
dojo.require("dojo.dnd.HtmlDragAndDrop");
dojo.require("dojo.event");
dojo.require("dojo.widget.DomWidget");

/**
 * Dojo widget for repeaters, that handles drag'n drop reordering and selection
 * by clicking in the rows.
 * <p>
 * The drop indicator can be styled with the "forms-dropIndicator" CSS class.
 *
 * @version $Id$
 */
// Extends the base DomWidget class. We don't need all the HtmlWidget stuff
// but need traversal of the DOM to build child widgets
cocoon.forms.CFormsRepeater = function() {
	dojo.widget.DomWidget.call(this);
};

dojo.inherits(cocoon.forms.CFormsRepeater, dojo.widget.DomWidget);

dojo.lang.extend(cocoon.forms.CFormsRepeater, {
	// Properties
	orderable: false,
	select: "none",
	
	// Widget definition
	widgetType: "CFormsRepeater",
    isContainer: true,
    buildRendering: function(args, parserFragment, parentWidget) {
	    // FIXME: we should destroy all drag sources and drop targets when the widget is destroyed
        // Magical statement to get the dom node, stolen in DomWidget
	    this.domNode = parserFragment["dojo:"+this.widgetType.toLowerCase()].nodeRef;
	    
        this.id = this.domNode.getAttribute("id");
        dojo.debug("Creating repeater " + this.id);
        if (!this.orderable && this.select == "none") {
            dojo.debug("CFormsRepeater '" + this.id + "' is not orderable nor selectable");
        }

        if (this.orderable) {
	        // Get the parent of the first repeater row (may be different from this.domNode)
	        var firstRow = dojo.byId(this.id + ".0");
	        if (!firstRow) return;

            // Check that TR's are in TBODY otherwise it doesn't work
	        if (firstRow.tagName.toLowerCase() == "tr" && firstRow.parentNode.tagName.toLowerCase() != "tbody") {
	            throw "CFormsRepeater requires TR's to be in a TBODY (check '" + this.id + "')";
	        }

			var type = "cforms-" + this.id;
			var dropTarget = new dojo.dnd.HtmlDropTarget(firstRow.parentNode, [type]);

			dropTarget.createDropIndicator = function() {
				this.dropIndicator = document.createElement("div");
				this.dropIndicator.className = "forms-dropIndicator";
				with (this.dropIndicator.style) {
					position = "absolute";
					zIndex = 1;
					width = dojo.style.getInnerWidth(this.domNode) + "px";
					left = dojo.style.getAbsoluteX(this.domNode) + "px";
				}
			};
			dojo.event.connect(dropTarget, "insert", this, "afterInsert");
		
			var row;
			for (var idx = 0; row = dojo.byId(this.id + "." + idx); idx++) {
				var dragSource = new dojo.dnd.HtmlDragSource(row, type);
				row.style.cursor = "move";
            }
        }

        if (true || this.select == "single" || this.select == "multiple") {
	        var row;
	        var widget = this;
	        for (var idx = 0; row = dojo.byId(this.id + "." + idx); idx++) {
		        (function() {
		            var localIdx = idx; // to use it in the closure
		            var localRow = row;
		            dojo.event.connect(row, "onclick", function(e) { widget.selectRow(e, localRow, localIdx) })
			    })()
            }
        }
    },

    /**
     * Called after a dropped node has been inserted at its target position
     * @param e the event (has a "dragObject" property)
     * @param refNode the reference node for the insertion
     * @param the insertion position relative to refNode ("before" or "after")
     */
    afterInsert: function(e, refNode, position) {
	    var parts = e.dragObject.domNode.getAttribute("id").split('.');
	    var source = parseInt(parts[parts.length - 1]);
	    parts = refNode.getAttribute("id").split('.');
	    var before = parseInt(parts[parts.length - 1]);
	    // Compute the row number before which to place the moved row
	    if (position == "after") before++;
	    if (before == source || before == source + 1) return; // no change needed
	
//		dojo.debug("moving row " + source + " before " + before + " (" + position + ")");

		// submit the form to update server-side model
		var form = cocoon.forms.getForm(this.domNode);
		var params = {};
		params[this.id + ".action"] = "move";
		params[this.id + ".from"]   = source;
		params[this.id + ".before"] = before;
		dojo.widget.byId(form.getAttribute("dojoWidgetId")).submit(this.id, params);
    },

    isValidEvent: function(e) {
	    var elt = dojo.html.getEventTarget(e);
	    if (!elt) return true;
	    if (elt.onclick) return false;
	    var name = elt.tagName.toLowerCase();
	    return (name != "input" && name != "a");
    },

    selectRow: function(e, row, idx) {
	    if (this.isValidEvent(e)) {
	        if (row.getAttribute("selected") == "true") {
	            row.setAttribute("selected", "false");
	            row.style.background = "none";
	        } else {
	            row.setAttribute("selected", "true");
	            row.style.background = "green";
	        }
		    dojo.debug("clic " + idx); //+ " from " + elt.tagName + " onclick=" + elt.onclick);
	    }
	}
});

dojo.widget.tags.addParseTreeHandler("dojo:CFormsRepeater");
// Register this module as a widget package
dojo.widget.manager.registerWidgetPackage("cocoon.forms");

