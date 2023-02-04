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
dojo.provide("cocoon.forms.CFormsDragAndDropRepeater");
dojo.require("cocoon.forms.CFormsRepeater");

/**
 * Dojo widget for repeaters, that handles drag'n drop reordering and selection
 * by clicking in the rows.
 * <p>
 * The drop indicator can be styled with the "forms-dropIndicator" CSS class.
 *
 * @version $Id: CFormsRepeater.js 393543 2006-04-12 17:32:25Z cziegeler $
 */

// Extends the base CFormsRepeater class.

dojo.widget.defineWidget(
    "cocoon.forms.CFormsDragAndDropRepeater",
    cocoon.forms.CFormsRepeater, {
    // Properties
    // Widget definition
    ns: "forms",
    widgetType: "CFormsDragAndDropRepeater",
    isContainer: true,
    preventClobber: true, // don't clobber our node
    
    /**
     * Returns the action name to be called on the server for model update.
     */
    getDndAction: function() {
      var addAction = this.domNode.getAttribute("dnd-action");
        dojo.debug("getDndAction: action=" + addAction);
        return addAction;
    },
    /**
     * Returns the drag & drop id used to share row between repeaters.
     */
    getType: function() {
       var type = this.domNode.getAttribute("dnd-id");
       if (type == null) {
                type = this.id;
             }
             return "cforms-" + type;
    },
    
    // widget interface
    buildRendering: function(args, frag) {
        // FIXME: we should destroy all drag sources and drop targets when the widget is destroyed

		cocoon.forms.CFormsRepeater.superclass.buildRendering.call(this, args, frag);
        this.id = this.domNode.getAttribute("id");
        if (!this.orderable && this.select == "none") {
            dojo.debug(this.widgetType + " '" + this.id + "' is not orderable nor selectable");
        }

        if (this.orderable) {
            // Get the parent of the first repeater row (may be different from this.domNode)
            var firstRow = dojo.byId(this.id + ".0");
            if (!firstRow) return;

            // Check that TR's are in TBODY otherwise it doesn't work
            if (firstRow.tagName.toLowerCase() == "tr" && firstRow.parentNode.tagName.toLowerCase() != "tbody") {
                throw this.widgetType + " requires TR's to be in a TBODY (check '" + this.id + "')";
            }

           var type = this.getType();
           var dropTarget = new dojo.dnd.HtmlDropTarget(firstRow.parentNode, [type]);

            dropTarget.createDropIndicator = function() {
                this.dropIndicator = document.createElement("div");
                this.dropIndicator.className = "forms-dropIndicator";
                with (this.dropIndicator.style) {
                    position = "absolute";
                    zIndex = 1;
                    width = dojo.html.getBorderBox(this.domNode).width + "px";
                    left = dojo.html.getAbsolutePosition(this.domNode).x + "px";
                }
            };
            dojo.event.connect("before", dropTarget, "insert", this, "beforeInsert");
            dojo.event.connect(dropTarget, "insert", this, "afterInsert");

            var row;
            for (var idx = 0; row = dojo.byId(this.id + "." + idx); idx++) {
                row.setAttribute("dndType", 'repeaterRow');
                row.setAttribute("dndRepeaterId", this.id);
                row.setAttribute("dndRowIndex", idx);
                var dragSource = new dojo.dnd.HtmlDragSource(row, type);
                row.style.cursor = "move";
            }
        }

        if (this.select != "$no$") {
            var row;
            var widget = this;
            for (var idx = 0; row = dojo.byId(this.id + "." + idx); idx++) {
                var selectId = row.getAttribute("id") + "." + this.select + ":input";
                var selectInput = dojo.byId(selectId);
                if (!selectInput) {
                    throw "No select input found for row '" + row.getAttribute("id") + "'";
                }

                if (selectInput.checked) {
                    dojo.html.prependClass(row, "forms-row-selected");
                }
                (function() {
                    var localIdx = idx; // to use it in the closure
                    var localRow = row;
                    dojo.event.connect(row, "onclick", function(e) { widget.selectRow(e, localRow, localIdx) });
                    dojo.event.connect(row, "onmouseover", function(e) { dojo.html.prependClass(localRow, "forms-row-hover") });
                    dojo.event.connect(row, "onmouseout", function(e) { dojo.html.removeClass(localRow,  "forms-row-hover") });
                })()
            }
        }
    },

    beforeInsert: function(e, refNode, position) {
        if (this.keepSourceInPlace(e, refNode, position)) {
            e.dragObject.domNode = e.dragObject.domNode.cloneNode(true);
          }
    },

    /**
     * The dragged item should be copied instead of moved.
     *
     * return true - the item will be copied.
    */
    keepSourceInPlace: function(e, refNode, position) {
        var parts = e.dragObject.domNode.getAttribute("id").split('.');
        var sourceRepeaterId = parts[0];
        return sourceRepeaterId != this.id;
    },

    /**
     * Return an object with predefined method to retrieve information
     * about the source object to be inserted into this repeater
     * @param e the event (has a "dragObject" property)
     */
    makeDragSource: function(e) {
        var repeater = this;
        var result = {
            _init: function(e) {
                this.e = e;
                this.sourceRowIndex = e.dragObject.domNode.getAttribute("dndRowIndex");
                this.sourceRepeaterId = e.dragObject.domNode.getAttribute("dndRepeaterId");
            },
            getRowIdx: function() { return this.sourceRowIndex;},
            getRepeaterId: function() { return this.sourceRepeaterId;},
            isRepeater: function() { return e.dragObject.domNode.getAttribute("dndType") == 'repeaterRow';},
            makeParameters: function() {
                var res = {};
                res[repeater.id + ".sourceRowIndex"] = this.sourceRowIndex;
                res[repeater.id + ".sourceRepeaterId"] = this.sourceRepeaterId;
                return res;
            }
        };
        result._init(e);
        return result;
    },

    /**
     * Called after a dropped node has been inserted at its target position
     * @param e the event (has a "dragObject" property)
     * @param refNode the reference node for the insertion
     * @param position the insertion position relative to refNode ("before" or "after")
     */
    afterInsert: function(e, refNode, position) {
        // Compute the row number before which to place the moved row
        var targetRowIndex = refNode.getAttribute("dndRowIndex");
        if (position == "after") {
            targetRowIndex++;
        }

        var dragSource = this.makeDragSource(e);
        if (dragSource.isRepeater(e)) {
            var sourceRowIndex = dragSource.getRowIdx();
            var sourceRepeaterId = dragSource.getRepeaterId();
            dojo.debug("afterInsert: sourceRepeaterId=" + sourceRepeaterId);
            dojo.debug("afterInsert: sourceRowIndex=" + sourceRowIndex);
        }

        dojo.debug("afterInsert: targetRepeaterId=" + this.id);
        dojo.debug("afterInsert: targetRowIndex=" + targetRowIndex);

        var params = dragSource.makeParameters();
        params[this.id + ".before"] = targetRowIndex;
        params["dndTarget.id"] = this.id;

        // submit the form to update server-side model
        if (sourceRepeaterId == this.id) {
            // move
            if (targetRowIndex == sourceRowIndex || targetRowIndex == sourceRowIndex + 1) {
                return; // no change needed
            }
            var form = cocoon.forms.getForm(this.domNode);
            params[this.id + ".action"] = "move";
            params[this.id + ".from"]   = sourceRowIndex;
            dojo.widget.byId(form.getAttribute("dojoWidgetId")).submit(this.id, params);
        } else if (this.getDndAction()) {
            // Run dndAction
            var form = cocoon.forms.getForm(this.domNode);
            var dojoForm = dojo.widget.byId(form.getAttribute("dojoWidgetId"));
            this.dndAction(dojoForm, dragSource, targetRowIndex, params);
        }
    },
   /**
     * Calls the server-side event to update the form model
     * @param dojoForm the dojoForm
     * @param dragSource the customized source reference node to be inserted into this repeater
     * @param targeRowtIndex the row insertion index into this repeater
     * @param params the parameter of the server-side event
     */
        dndAction : function(dojoForm, dragSource, targetRowIndex, params) {
                    dojoForm.submit(this.getDndAction(), params);
        }
});
