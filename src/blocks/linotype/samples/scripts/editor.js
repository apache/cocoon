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
// -------------------------- global DOM nodes ------------------------------
/*
Nummer Node Type 
1 Element 
2 Attribute 
3 Text
4 CDATA 
5 Entity-Referenz 
6 Entity 
7 Processing Instruction 
8 Comment 
9 Document 
10 Document Type 
11 Document Fragment 
12 Notation
*/
NodeType={ 
	 ELEMENT_NODE:0x01,
	 ATTRIBUTE_NODE:0x02,
	 TEXT_NODE:0x03,
	 CDATA_NODE:0x04,
	 ENTITY_REFERENZE_NODE:0x05,
	 ENTITY_NODE:0x06,
	 SCRIPT_NODE:0x07,
	 COMMENT_NODE:0x08,
	 DOCUMENT_NODE:0x09,
	 DOCUMENT_FRAGMENT_NODE:0x0a,
	 NOTATION_NODE:0x0b
};

Alternatives= {
	blockquote: [ "quote", "code", "note", "fixme","first" ],
	img: [ "inline", "floating-left", "floating-right" ],
	p: [ "normal", "figure" ]
}

IE=(document.all)? true:false;
PrefixImageSrc=(window.prefixImgSrc)? window.prefixImgSrc:"image-"; 
var iframe;
var editor;
var editor_window;
var path;
var formatblock;
var alternatives;
var alternativesTarget;
var class_selector;
var block_selector;
var lastOrigin;
var image_inputs;
var image_controls;

// -------------------------- global variables ------------------------------

var previousKey;
var modified = false;

var imageCounter = 0;
var imageData = new Array();

var sourceMode = false;

// ----------------------- UI-modifying functions ---------------------------

function start(e) {
    iframe = document.getElementById('edit');
	editor_window = (!IE)? iframe.contentWindow : document.frames.edit;

	path = document.getElementById('path').firstChild;
	formatblock = document.getElementById('formatblock');
	formatblock.onchange = formatblockChange;
	alternatives = document.getElementById('alternatives');
	block_selector = document.getElementById('block_selector');
	class_selector = document.getElementById('class_selector');
	image_inputs = document.getElementById('image_inputs');
	image_controls = document.getElementById('image_controls');
	try {
		editor_window.document.designMode="On";
		editor = (IE) ? frames.edit.document : editor_window.document;
		addEvent(editor,"click",click,true);
		addEvent(editor,"keydown",keypress,true);
		try {
			editor.execCommand("useCSS", false, true); // midas gets it backwards
		} catch(e){}
	} catch (e) {
		alert("I'm sorry, but Linotype doesn't work on this browser: " + e.name + " " + e.message);
		return;
	}

    divs = document.getElementsByTagName('div');
    for (var i = 0; i < divs.length; i++) {
		var nameclass=getClassName(divs[i]);
		if (nameclass == "imagebutton") {
            divs[i].onmousedown = buttonDown;
            divs[i].onmouseup = buttonUp;
            divs[i].onmouseover = buttonOver;
            divs[i].onmouseout = buttonOut;
            divs[i].onclick = buttonClick;
        }
    }

    instrumentImages(editor);

    document.getElementById("wysiwyg-checkbox").checked = true;

    window.status = "Welcome to Linotype";
}

function addEvent(obj,type,func,bol){
	bol=bol||false;
	if(IE) obj.attachEvent("on"+type.replace(/^on/g,""), func);
	else obj.addEventListener(type.replace(/^on/g,""),func,bol);
}
function setClassAttribute(obj,value){
	if(IE){
		obj.className=value;
	} else {
		obj.setAttribute("class",value,false);
	}
}
function getClassName(obj){
	return (IE)?obj.className:obj.getAttribute("class");
}

function stop(e) {

    /*
     * FIXME (SM): the onload() event isn't cancellable. This means that we can't
     * prevent people from unloading the page and loose their changes. In IE
     * there is a non-standard way to do this which is hacky at hell but does
     * the job. Is there an equivalent thing for moz?
     */

    if (modified) {
        // if ( window.confirm("You havn't saved. You changes will be lost.\nDo you want to continue?") ) {
        //     return;
        // } else {
        //    e.stopPropagation();
        //    e.preventDefault();
        //    e.returnValue = false;
        //    return false;
        // }
    }
}

function updateUI(force) {
    var target = getOrigin();
    if (target != lastOrigin || force) {
        lastOrigin = target;
        
        if (!sourceMode) {
            path.nodeValue = getPath(target);
        } else {
            path.nodeValue = "...";
        }
		if (target && (target.nodeType ==NodeType.TEXT_NODE)) { //remove  target.TEXT_NODE not in IE
            target = target.parentNode;
        }
        
        var format = getBlockFormat(target);
        if (format) {
            formatblock.selectedIndex = format;
            block_selector.style.visibility = "visible";
        } else {
            block_selector.style.visibility = "hidden";
        }
        
        var options = getAlternatives(target);
        if (options) {
            var parent = alternatives.parentNode;
            parent.removeChild(alternatives);
            alternatives = document.createElement('select');
			var type = getClassName(target);
            for (i = 0; i < options.length; i++) {
                var option = document.createElement('option');
                option.setAttribute("value", options[i]);
                if (type == options[i]) {
                    option.setAttribute("selected", "true");
                }
                option.appendChild(document.createTextNode(options[i]));
                alternatives.appendChild(option);
            }
            alternatives.onchange = alternativesChange;
            parent.appendChild(alternatives);
            alternativesTarget = target;
            class_selector.style.visibility = "visible";
        } else {
            class_selector.style.visibility = "hidden";
        }
    }
}

function wysiwyg(enabled) {
    if (enabled) {
        var text = serializeChildren(editor.body);
		var source = editor.createTextNode(text);
        editor.body.innerHTML = "";
        editor.body.appendChild(source);
		setClassAttribute(editor.body, "source");
        sourceMode = true;
        updateUI(true);
    } else {
		if(IE) {
			var source=editor.body.createTextRange();
			editor.body.innerHTML=resume(source.text);
		} else {
	        var source = editor.body.ownerDocument.createRange();
	        source.selectNodeContents(editor.body);
	        editor.body.innerHTML = resume(source.toString());
		}
		setClassAttribute(editor.body,"body");
        reinstrumentImages(editor);
        sourceMode = false;
    }
}

function loadContent(content) {
    editor.body.innerHTML = resume(content);
}

function addImage() {
    editor.execCommand("insertimage", false, "template.jpg");
    var img = getOrigin();
    var input = instrumentImg(img, true);
    activateImgInstrumentation(input, img);
}

function instrumentImages(edit) {
	if(IE &&editor.readyState != "complete"){ 
		setTimeout("instrumentImages()",100);
		return;
	}
    var imgs = detach(editor.getElementsByTagName('img'));
    for (var i = 0; i < imgs.length; i++) {
        instrumentImg(imgs[i], false);
    }
}

function reinstrumentImages(editor) {
	var imgs = detach(editor.getElementsByTagName('img'));
    for (var i = 0; i < imgs.length; i++) {
        reinstrumentImg(imgs[i]);
    }
}

function instrumentImg(img, template) {
	var systemtr=(img.src.indexOf('\\')!= -1)? '\\':"/";
	var id=img.src.substring(img.src.lastIndexOf(systemtr)+1,img.src.lastIndexOf('.'));

	if(template){ id="new";}
    var imgID = "image-" + id;
    var src = img.getAttribute("src");
	img.setAttribute("id",imgID,true);
	if(!IE){ img.setAttribute("href",src,false);}
	img.setAttribute("ihref",src,false);
    setTemplate(img, template);
	addEvent(img.parentNode,"DOMNodeRemoved",imageRemoved);

    imageData[imgID] = [src, src, false];

    var input = document.createElement("input");
	
	input.setAttribute("type","file",false);
	input.setAttribute("id",imgID + "-input",false);
	input.setAttribute("size", "1",false);
	setClassAttribute(input,"image_browser");
	addEvent(input,"click",inputChange);
	if(IE){addEvent(input,"focus",inputChange);} //only for IE 5.1 compatibility
	else{addEvent(input,"change",inputChange);}
    input.style.position = "absolute";
    input.style.visiblility = "hidden";

    image_inputs.appendChild(input);
    
    return input;
}

function reinstrumentImg(img, id) {
    if (!id) {
        var src = img.getAttribute("src");
		var id = "image-"+src.substring(src.lastIndexOf('/')+1,src.lastIndexOf('.'));
    }

    var data = imageData[id];
	img.setAttribute("id",id,false);
	img.setAttribute("src",data[0],false);
	img.setAttribute("ihref",data[1],false);
    setTemplate(img, data[2]);
	//if(!IE){
	//	img.setAttribute("href",data[1],false);
		//addEvent(img,"mousedown",imageDown,true)
	//}
}

function activateImgInstrumentation(input, img) {
	var ibutton= document.getElementById("insertimage");
	var left=ibutton.offsetLeft;
	var parent=ibutton.offsetParent;
	while(parent){
		left+=parent.offsetLeft;
		parent=parent.offsetParent;
	}
	input.style.left=left+"px";
	input.style.top=ibutton.offsetTop+"px";
    input.style.visibility = "visible";
}

function deactivateImgInstrumentation(input) {
    input.style.visibility = "hidden";
    input.name = "";
}

// ----------------------- Event functions ---------------------------

function keypress(e) {
	var key,ch =0
    modified = true;
	if(!e){e=edit.event;}
    updateUI();
	key = e.keyCode;
	if(!IE){ch = e.charCode;}

    //window.status = "[" + key + "," + ch + "," + e.shiftKey + "," + e.ctrlKey + "," + e.altKey + "," + e.metaKey + "]";

    try {
        // Disable "Back" / "Forward" buttons (Alt|Apple+LeftArrow / Alt|Apple+RightArrow)
        if (e.altKey || e.metaKey) {
            if (key > 0) {
                switch (key) {
                    case 37: // left arrow
                    case 39: // right arrow
                        e.stopPropagation();
                        e.preventDefault();
                        e.returnValue = false;
                        return false;
                }
            //} else if (ch > 0) {
            //    switch (ch) {
            //        case 91: // [
            //        case 93: // ]
            //            e.stopPropagation();
            //            e.preventDefault();
            //            e.returnValue = false;
            //            return false;
            //    }
            }
        //} else if (e.shiftKey && (key == e.DOM_VK_TAB)) {
        //    editor.execCommand("outdent", false, null);
        //} else if (key == e.DOM_VK_TAB) {
        //    editor.execCommand("indent", false, null);
        //} else if ((key == e.DOM_VK_ENTER) ||  (key == e.DOM_VK_RETURN)) {
        //    if ((previousKey == e.DOM_VK_ENTER) ||  (previousKey == e.DOM_VK_RETURN)) {
        //       editor.execCommand("insertparagraph", false, null);
        //    }
        }
    } catch (e) { }

    previousKey = key;
}

function click() {
    updateUI();
    previousKey = null;
}

function formatblockChange() {
    var selection = formatblock.selectedIndex;
    var blockFormat = formatblock.options[selection].value;
	var commandFormat=(IE)?"<"+blockFormat+">":blockFormat;
    if (selection == 0) {
        //var block = getOrigin().parentNode;
        //block.parentNode.replaceChild(block, block.firstChild);
        
        // <----------------- FIXME -----------------------
        
    } else {
		editor.execCommand("formatblock", false, commandFormat);
    }
    updateUI();
    editor_window.focus();
    modified = true;
}

function alternativesChange() {
    var selection = alternatives.selectedIndex;
    var selectionClass = alternatives.options[selection].value;
	setClassAttribute(alternativesTarget,selectionClass);
}

function buttonClick() {
    if (this.id == "createlink") {
        var href = prompt("Enter a URL:", "");
        editor.execCommand(this.id, false, href);
    } else if (this.id == "insertimage") {
        addImage();
    } else if (this.id == "quote") {
        editor.execCommand("formatblock", false, "q");
    } else {
        editor.execCommand(this.id, false, null);
    }
    updateUI(true);
    editor_window.focus();
    modified = true;
}

function buttonDown() {
    this.firstChild.style.left = "1px";
    this.firstChild.style.top = "1px";
    this.style.border="inset 1px";
}

function buttonUp() {
    this.firstChild.style.left = "0px";
    this.firstChild.style.top = "0px";
    this.style.border="outset 1px";
}

function buttonOver() {
    this.style.border="outset 1px";
}

function buttonOut() {
    this.style.border="solid 1px #eee";
}

var originalX;
var originalWidth;
var activeImage;
var activeRatio;

function imageDown(e) {
    originalX = e.clientX;
	addEvent(editor_window,"mousemove",doDrag,true);
	addEvent(editor_window,"mouseup",endDrag,true);
    activeImage = this;
    activeRatio = activeImage.height / activeImage.width;
    originalWidth = activeImage.width;
    e.stopPropagation();
    e.preventDefault();
    e.returnValue = false;
    return false;         
}

function imageRemoved(e) {
	var id = (IE)? window.event.srcElement.id:e.target.id;
    var input = document.getElementById(id + "-input");
    if (input) deactivateImgInstrumentation(input);
}

function doDrag(e) {
    var dx = e.clientX - originalX;
    var width = originalWidth + dx;
    activeImage.style.width = width + "px";
    activeImage.style.height = (width * activeRatio) + "px";
    window.status = "(" + activeImage.width + "px, " + activeImage.height + "px)";
}

function endDrag(e) {
    editor_window.removeEventListener("mousemove", doDrag, true);
    window.status = "";
}

function inputChange() {
		var event=(IE)? window.event.srcElement:this;
		var inputID = event.getAttribute("id");
		var img=editor.getElementById("image-new");
		var systemtr=(event.value.indexOf('\\')!= -1)? '\\':"/";
		var imgID= "image-"+PrefixImageSrc+event.value.substring(event.value.lastIndexOf(systemtr)+1,event.value.lastIndexOf("."));
		if (event.value != "") {
			var newImg = editor.createElement("img");
			var src = "file:///" + event.value;
			var href = PrefixImageSrc+event.value.substring(event.value.lastIndexOf(systemtr)+1);
			newImg.setAttribute("id",imgID,false);
			newImg.setAttribute("ihref",href,false);
			if(!IE)newImg.setAttribute("href",href,false);
			newImg.setAttribute("src",src,false);
			var doc =img.parentNode;
			doc.replaceChild(newImg,img);
        	imageData[imgID] = [src, href, false];
			var nimgD=new Array();
			imageData["image-new"][2]=false;
			event.setAttribute("id",imgID+"-input",false);
        	reinstrumentImg(newImg, imgID);
			deactivateImgInstrumentation(event);
			event.setAttribute("name","save:" + href,false);
    	} else {
			if(!IE) img.parentNode.removeChild(img);
	        // Note: this will trigger a DOMNodeRemoved event that we'll use
	        // to deactivate the image instrumentation (removing it causes
	        // mozilla to crash!!!).
    	}
}

// ----------------------- DOM functions ---------------------------

function getInnerHTML() {
    return editor.body.innerHTML;
}

function getContent() {
	if (sourceMode){wysiwyg(false);}
    var content = '<html xmlns="http://www.w3.org/1999/xhtml"><body>';
    content += serializeChildren(editor.body);
    content += '</body></html>';
    return content;
}

function serializeChildren(node) {
    var str = "";
    var children = node.childNodes;
    for (var i = 0; i < children.length; i++) {
        str += serialize(children.item(i));
    }
    return str;
}

function serialize(node) {
    var str = "";
    var name = node.nodeName.toLowerCase();

    str += whitespaceBefore(node, getPreviousMeaningfulNode(node));

	if (node.nodeType == NodeType.TEXT_NODE) {
        if (preserveWhitespace(node.parentNode) || isMeaningfulWhitespace(node)) {
            str += escape(trimNewlines(node.nodeValue));
        } else {
            str += structurize(escape(trimWhitespace(node.nodeValue)));
        }
    } else if (!isTemplate(node)) {
        str += "<" + name + escape(getAttributes(node));
        var children = node.childNodes;
        if ((children != null) && (children.length > 0)) {
            str += ">";
            for (var i = 0; i < children.length; i++) {
                str += serialize(children.item(i));
            }
            str += "</" + name + ">";
        } else {
            str += "/>";
        }
    }

    str += whitespaceAfter(node, getNextMeaningfulNode(node));

    return str;
}

function getAttributes(node) {
    var attr = node.attributes;
    if (node && attr) {
        var str = "";
        for (var i = 0; i < attr.length; i++) {
            str += getAttribute(node, attr.item(i));
        }
        return str += getHiddenAttributes(node);
    } else {
        return "";
    }
}

function getPath(node) {
    if (node) {
        var name = node.nodeName;
        if (node.parentNode) {
            return getPath(node.parentNode) + "/" + name;
        } else {
            return name;
        }
    } else {
        return "";
    }
}

function getOrigin() {

    // ------------------------ WARNING: HOTSPOT! ---------------------------
    // since it's called for every keystroke, keep it as fast as possible!!!
    // ----------------------------------------------------------------------
	var selection=(IE)? editor.selection.createRange():editor_window.getSelection();
	if(IE){
		if(editor.selection.type == "Control"){
			 if(selection.length > 1) alert("Error too many obj");
			 else return selection(0);
		} else {
			if(selection.parentElement().nodeType != NodeType.TEXT_NODE
				&& editor_window.event){
				var e=editor_window.event;
				var nodes = selection.parentElement().childNodes;
				if(nodes.length == 1) {return nodes[0];}
				for(var co=0; co < nodes.length;co++){
					if(!nodes[co].offsetTop && nodes[co].nextSibling){
						if(nodes[co].nextSibling.offsetTop <= e.y 
							&& nodes[co].nextSibling.offsetTop+ nodes[co].nextSibling.offsetHeight >= e.y
							&& nodes[co].nextSibling.offsetLeft <= e.x) { 
							return nodes[co];
						}
					} else if(nodes[co].offsetTop <= e.y 
						&& nodes[co].offsetTop+ nodes[co].offsetHeight>= e.y) {
						return nodes[co];
					}
				}
				return nodes[nodes.length-1];
			}
		}
		return selection.parentElement();
	}
	else{
    if (selection.anchorNode == selection.focusNode) {
        if (selection.anchorNode.nodeType != selection.anchorNode.TEXT_NODE) {
            var index = (selection.focusOffset == 0) ? 0 : selection.focusOffset - 1;
            return selection.anchorNode.childNodes[index];
        }
    }

    return selection.anchorNode;
}
}

function getPreviousMeaningfulNode(node) {
    if (node) {
        var previous = node.previousSibling;
        if (previous) {
			if (previous.nodeType == NodeType.TEXT_NODE) {
                if (!isWhitespace(previous.nodeValue)) {
                    return previous;
                } else {
                    return getPreviousMeaningfulNode(previous);
                }
            } else {
                return previous;
            }
        }
    }
    return null;
}

function getNextMeaningfulNode(node) {
    if (node) {
        var next = node.nextSibling;
        if (next) {
			if (next.nodeType == NodeType.TEXT_NODE) {
                if (!isWhitespace(next.nodeValue)) {
                    return next;
                } else {
                    return getNextMeaningfulNode(next);
                }
            } else {
                return next;
            }
        }
    }
    return null;
}

// ----------------------------- Utility Functions -------------------------

function getMonth(number) {
    switch (number) {
        case  0: return "January";
        case  1: return "February";
        case  2: return "March";
        case  3: return "April";
        case  4: return "May";
        case  5: return "June";
        case  6: return "July";
        case  7: return "August";
        case  8: return "September";
        case  9: return "October";
        case 10: return "November";
        case 11: return "December";
    }
}

function getDate() {
    var date = new Date();
    var month = getMonth(date.getMonth());
    var day = date.getDate();
    var year = date.getFullYear();
    return month + " " + day + ", " + year;
}

function getTime() {
    var time = new Date();
    var hour = time.getHours();
    var minute = time.getMinutes();
    return ((hour < 10) ? "0" : "") + hour + ((minute < 10) ? ":0" : ":") + minute;
}

function getFullDate() {
    return (new Date()).toGMTString();
}

var trimRE = /(^\s+)|(\s+$)/g;

/**
 * Removes whitespace from the beginning and end of line.
 */
function trimWhitespace(str) {
    if (!str) return '';
    return str.replace(trimRE, '');
}

var saneRE = /\n+/g;

/**
 * Removes all newlines
 */
function trimNewlines(str) {
    if (!str) return '';
    return str.replace(saneRE, '');
}

function isWhitespace(str) {
    return trimWhitespace(str).length == 0;
}

/**
 * Detach the live array returned by some DOM functions
 * and make it static (means that it doesn't change if you work
 * on the DOM). Very useful to avoid nasty recursion when working
 * on getElementByTagName()
 */
function detach(liveArray) {
    var staticArray = new Array(liveArray.length);
    for (var i = 0; i < liveArray.length; i++) {
        staticArray[i] = liveArray[i];
    }
    return staticArray;
}

// ----------------------------- Semantic Functions -------------------------

function isBlock(node) {
	if (node && (node.nodeType == NodeType.ELEMENT_NODE)) {
        switch (node.nodeName.toLowerCase()) {
            case "p":
            case "blockquote":
            case "h1":
            case "h2":
            case "h3":
            case "h4":
            case "ol":
            case "ul":
            case "body":
                return true;
            default:
                return false;
        }
    } else {
        return false;
    }
}

function isInline(node) {
	if (node && (node.nodeType == NodeType.ELEMENT_NODE)) {
        switch (node.nodeName.toLowerCase()) {
            case "b":
            case "i":
            case "q":
            case "strike":
            case "img":
            case "a":
                return true;
            default:
                return false;
        }
    } else {
        return false;
    }
}

function isMeaningfulWhitespace(node) {
	if (node && (node.nodeType == NodeType.TEXT_NODE)) {
        return (isInline(node.nextSibling) || isInline(node.previousSibling));
    } else {
        return false;
    }
}

function getBlockFormat(node) {
    if (isBlock(node)) {
        switch (node.nodeName.toLowerCase()) {
            case "p": return 1;
            case "h1": return 2;
            case "h2": return 3;
            case "h3": return 4;
            case "h4": return 5;
            default: return 0;
        }
    } else {
		return "";
    }
}

function isTemplate(node) {
	if (node && (node.nodeType == NodeType.ELEMENT_NODE)) {
        if (node.getAttribute("template") == "yes") {
            return true;
		} else {
			var name = node.nodeName.toLowerCase();
			if (name == "br") {
            if (!node.nextSibling) {
                return true;
            } else {
                return isTemplate(node.nextSibling);
            }
        }
    }
	}
    return false;
}

function setTemplate(node, status) {
    var value = (status) ? "yes" : "no";
	node.setAttribute("template",value,false);
}

function getAlternatives(node) {
    if (node) {
		var name = node.nodeName.toLowerCase();
		if(Alternatives[name]) return Alternatives[name];
		return null;
        }
    }

function getAttribute(node, at) {
    var nodeName = node.nodeName.toLowerCase();
	var atName = (at.name)? at.name.toLowerCase():"";

	if (((nodeName == "img") && (atName == "ihref")) ) {
		//return ' src="' + at.value + '"';
		return "";
	} else if (((atName == "class") && at.value!="") || (atName == "href")) {
        return " " + atName + '="' + at.value + '"';
    } else {
        return "";
    }
}

function getHiddenAttributes(node) {
    var nodeName = node.nodeName.toLowerCase();
    if (nodeName == "img") {
		var ihref=node.getAttribute("ihref");
		var src=ihref.substring(ihref.lastIndexOf('/')+1);
		return ' src="'+src+'" width="' + node.width + '" height="' + node.height + '"';
		//return ' src="'+src+'" id="'+node.id+'" width="' + node.width + '" height="' + node.height + '"';
    } else {
        return '';
    }
} 
    
function preserveWhitespace(node) {
	return ((node.nodeName.toLowerCase() == "blockquote") && (getClassName(node) == "code"));
}

function whitespaceBefore(currentNode, precedingNode) {
    var current = currentNode.nodeName.toLowerCase();
    var preceding = (precedingNode) ? precedingNode.nodeName.toLowerCase() : "#none";

    if (current == "#text") {
        if (!isWhitespace(currentNode.nodeValue) && isBlock(precedingNode)) {
            return "\n";
        }
    } else if (isBlock(currentNode)) {
        if (isBlock(precedingNode) || (preceding == "li")) {
            return "\n";
        }
    }

    return "";
}

function whitespaceAfter(currentNode, followingNode) {
    var current = currentNode.nodeName.toLowerCase();
    var following = (followingNode) ? followingNode.nodeName.toLowerCase() : "#none";

    if (isBlock(currentNode)) {
        if (isBlock(followingNode) || (following == "#text") || (following == "#none")) {
            return "\n";
        }
    } else if (current == "br") {
        if (following != "#none") {
            return "\n";
        }
    } else if (current == "li") {
        return "\n";
    }

    return "";
}

/*
 * Change special text structures into markup
 */
function structurize(str) {
    str = str.replace(/\*((\w|\s)+)\*/g, "<b>$1</b>");
    str = str.replace(/\"((\w|\s)+)\"/g, "<q>$1</q>");
    return str;
}

/*
 * Escapes special charachters into entities
 */
function escape(str) {
    str = str.replace(/&/g, "&amp;");
    str = str.replace(/</g, "&lt;");
    str = str.replace(/>/g, "&gt;");
    str = str.replace(/\u00A0/g, "&#160;");
    return str;
}

/*
 * Resumes entities into special charachters
 * [this is done because Midas doesn't recognize all entities]
 */
function resume(str) {
    str = str.replace(/&amp;/g, "&");
    str = str.replace(/&lt;/g, "<");
    str = str.replace(/&gt;/g, ">");
    //str = str.replace(/&#160;/g, \u00A0);
    return str;
}

// ------------------------------ end of file --------------------------
