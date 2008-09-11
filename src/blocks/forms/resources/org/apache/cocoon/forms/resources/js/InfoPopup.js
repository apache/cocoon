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
dojo.provide("cocoon.forms.InfoPopup");
dojo.require("dijit.form.Button");
dojo.require("dijit.layout.ContentPane");

/*
 * A widget displaying an icon which when pressed shows a popup.
 * The content of the popup is the content of the original element declaring the widget.
 */
dojo.declare(
    // widget name and class
    "cocoon.forms.InfoPopup",

    // superclass
    [dijit.form.DropDownButton],

    // properties and methods
    {
				iconClass: "dijitValidationIcon",
				closeIconClass: "dijitDialogCloseIcon",
				label: "",
	
				widgetsInTemplate: true,
	
        isContainer: true,
				templateString: "",
        templatePath: dojo.moduleUrl("cocoon.forms", "templates/InfoPopup.html"),



        containerToggle: "plain", /* plain, explode, wipe, fade */

        containerToggleDuration: 150,
        
        

				startup: function(){
					// the child widget from srcNodeRef is the dropdown widget.  Insert it in the page DOM,
					// make it invisible, and store a reference to pass to the popup code.
					if(!this.dropDown){
						console.debug("dropDownNode: " + this.dropDownContainer);
						//var dropDownNode = dojo.query("[widgetId]", this.dropDownContainer);
						//console.debug("dropDownNode: " + this.dropDownNode);
						//this.dropDown = dijit.byNode(dropDownNode);
						this.dropDown = dijit.byNode(this.dropDownContainer);
						delete this.dropDownContainer;
					}
					dojo.body().appendChild(this.dropDown.domNode);
					this.dropDown.domNode.style.display="none";
				}
        
        
        
        
        
        
/*
        attachTemplateNodes: function(){
            // summary: use attachTemplateNodes to specify containerNode, as fillInTemplate is too late for this
            cocoon.forms.InfoPopup.superclass.attachTemplateNodes.apply(this, arguments);

            this.infoPopup = dojo.widget.createWidget("PopupContainer", {toggle: this.containerToggle, toggleDuration: this.containerToggleDuration});

            this.infoPopupContainerNode = this.infoPopup.domNode;
        },

        fillInTemplate: function(args, frag) {
            cocoon.forms.InfoPopup.superclass.fillInTemplate(this, args, frag);

            this.domNode.appendChild(this.infoPopup.domNode);

            // take over class from original node
            var source = this.getFragNodeRef(frag);
            dojo.html.setClass(this.popupNode, dojo.html.getClass(source));

            // move popupNode (defined in the template) to the actual PopupContainer widget
            this.domNode.removeChild(this.popupNode);
            this.infoPopupContainerNode.appendChild(this.popupNode);

            // copy content from original element to the popup
            this.contentNode.innerHTML = this.getFragNodeRef(frag).innerHTML;

            // make sure popup can be visible
            this.popupNode.style.display = "";
        },

        _onIconClick: function() {
            if(!this.infoPopup.isShowingNow) {
                this.infoPopup.open(this.buttonNode, this, this.buttonNode);
            } else {
                this.infoPopup.close();
            }
        },

        hidePopup: function() {
            this.infoPopup.close();
        }
*/
    }
);