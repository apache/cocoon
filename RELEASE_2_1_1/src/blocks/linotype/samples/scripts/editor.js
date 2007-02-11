// -------------------------- global DOM nodes ------------------------------

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
var imageData = {};

var sourceMode = false;

// ----------------------- UI-modifying functions ---------------------------

function start(e) {
    iframe = document.getElementById('edit');
    editor_window = iframe.contentWindow;
    editor = editor_window.document;

    path = document.getElementById('path').firstChild;
    formatblock = document.getElementById('formatblock');
    formatblock.onchange = formatblockChange;
    alternatives = document.getElementById('alternatives');
    block_selector = document.getElementById('block_selector');
    class_selector = document.getElementById('class_selector');
    image_inputs = document.getElementById('image_inputs');
    image_controls = document.getElementById('image_controls');

    try {
        editor.designMode = "on";
        editor.execCommand("useCSS", false, true); // midas gets it backwards
        editor.addEventListener("click",click,true);
        editor.addEventListener("keypress",keypress,true);
    } catch (e) {
        alert("I'm sorry, but Linotype doesn't work on this browser: " + e);
        return;
    }

    divs = document.getElementsByTagName('div');

    for (var i = 0; i < divs.length; i++) {
        if (divs[i].getAttribute("class") == "imagebutton") {
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

function stop(e) {

    /*
     * FIXME (SM): the onload() event isn't cancellable. This means that we can't
     * prevent people from unloading the page and loose their changes. In IE
     * there is a non-standard way to do this which is hacky at hell but does
     * the job. Is there an equivalent thing for moz?
     */

    if (modified) {
        // if ( window.confirm("You havn't saved. You changes will be lost.\nDo you want to continue?") ) {
        // 	return;
        // } else {
        //	e.stopPropagation();
        //	e.preventDefault();
        //	e.returnValue = false;
        //	return false;
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
        
        if (target && (target.nodeType == target.TEXT_NODE)) {
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
            var type = target.getAttribute("class");
            for (i = 0; i < options.length; i++) {
                var option = document.createElement('option');
                option.setAttribute("value",options[i]);
                if (type == options[i]) {
                    option.setAttribute("selected","true");
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
        var source = document.createTextNode(text);
        editor.body.innerHTML = "";
        editor.body.appendChild(source);
        editor.body.setAttribute("class","source");
        sourceMode = true;
        updateUI(true);
    } else {
        var source = editor.body.ownerDocument.createRange();
        source.selectNodeContents(editor.body);
        editor.body.innerHTML = resume(source.toString());
        editor.body.setAttribute("class","body");
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
    var input = instrumentImg(img,true);
    activateImgInstrumentation(input,img);
}

function instrumentImages(editor) {
    var imgs = detach(editor.getElementsByTagName('img'));
    for (var i = 0; i < imgs.length; i++) {
        instrumentImg(imgs[i],false);
    }
}

function reinstrumentImages(editor) {
    imgs = detach(editor.getElementsByTagName('img'));
    for (var i = 0; i < imgs.length; i++) {
        reinstrumentImg(imgs[i]);
    }
}

function instrumentImg(img,template) {
    var id = imageCounter++;
    var imgID = "image-" + id;
    var src = img.getAttribute("src");

    img.setAttribute("id",imgID);
    img.setAttribute("href",src);
    setTemplate(img,template);

    img.addEventListener("mousedown",imageDown,false);
    
    img.parentNode.addEventListener("DOMNodeRemoved",imageRemoved,false);
    
    imageData[imgID] = [src,src,false];

    var input = document.createElement("input");
    input.setAttribute("type","file");
    input.setAttribute("id",imgID + "-input");
    input.setAttribute("size", "1");
    input.setAttribute("class", "image_browser");
    input.addEventListener("click",inputChange,false);
    input.addEventListener("change",inputChange,false);
    input.style.position = "absolute";
    input.style.visiblility = "hidden";

    image_inputs.appendChild(input);
    
    return input;
}

function reinstrumentImg(img,id) {
	if (!id) {
	    var src = img.getAttribute("src");
	    var id = src.substring(src.lastIndexOf('/'),src.lastIndexOf('.'));
	}

    var data = imageData[id];

    img.setAttribute("id",id);
    img.setAttribute("src",data[0]);
    img.setAttribute("href",data[1]);
    setTemplate(img,data[2]);

    img.addEventListener("mousedown",imageDown,true);
}

function activateImgInstrumentation(input,img) {
    input.style.left = (iframe.offsetLeft + img.x + 5) + "px";
    input.style.top = (iframe.offsetTop + img.y + 5) + "px";
	input.style.visibility = "visible";
}

function deactivateImgInstrumentation(input) {
	input.style.visibility = "hidden";
	input.name = "";
}

// ----------------------- Event functions ---------------------------

function keypress(e) {

    modified = true;

    updateUI();

    var key = e.keyCode;
    var ch = e.charCode;

    //window.status = "[" + key + "," + ch + "," + e.shiftKey + "," + e.ctrlKey + "," + e.altKey + "," + e.metaKey + "]";

    try {
        if (e.ctrlKey || e.metaKey) {
            if (key > 0) {
                switch (key) {
                    case 37: // left arrow
                    case 39: // right arrow
                        e.stopPropagation();
                        e.preventDefault();
                        e.returnValue = false;
                        return false;
                }
            } else if (ch > 0) {
                switch (ch) {
                    case 91: // [
                    case 93: // ]
                        e.stopPropagation();
                        e.preventDefault();
                        e.returnValue = false;
                        return false;
                }
            }
        //} else if (e.shiftKey && (key == e.DOM_VK_TAB)) {
        //    editor.execCommand("outdent",false,null);
        //} else if (key == e.DOM_VK_TAB) {
        //    editor.execCommand("indent",false,null);
        //} else if ((key == e.DOM_VK_ENTER) ||  (key == e.DOM_VK_RETURN)) {
        //    if ((previousKey == e.DOM_VK_ENTER) ||  (previousKey == e.DOM_VK_RETURN)) {
        //       editor.execCommand("insertparagraph",false,null);
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
    if (selection == 0) {
    	//var block = getOrigin().parentNode;
    	//block.parentNode.replaceChild(block,block.firstChild);
    	
    	// <----------------- FIXME -----------------------
    	
    } else {
        editor.execCommand("formatblock", false, blockFormat);
    }
    updateUI();
    editor_window.focus();
    modified = true;
}

function alternativesChange() {
    var selection = alternatives.selectedIndex;
    var selectionClass = alternatives.options[selection].value;
    alternativesTarget.setAttribute("class",selectionClass);
}

function buttonClick() {
    if (this.id == "createlink") {
        var href = prompt("Enter a URL:", "");
        editor.execCommand(this.id,false,href);
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
    editor_window.addEventListener("mousemove", doDrag, true);
    editor_window.addEventListener("mouseup", endDrag, true);
    activeImage = this;
    activeRatio = activeImage.height / activeImage.width;
    originalWidth = activeImage.width;
    e.stopPropagation();
    e.preventDefault();
    e.returnValue = false;
    return false;         
}

function imageRemoved(e) {
	var id = e.target.id;
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
    var inputID = this.getAttribute("id");
    var imgID = inputID.substring(0,inputID.indexOf("-input"));
    var img = editor.getElementById(imgID);
    if (this.value != "") {
        var newImg = document.createElement("img");
        img.parentNode.replaceChild(newImg,img);
        var src = "file:///" + this.value;
        var href = imgID + this.value.substring(this.value.lastIndexOf('.'));
        imageData[imgID] = [src,href,false];
        reinstrumentImg(newImg,imgID);
	    deactivateImgInstrumentation(this);
        this.setAttribute("name","save:" + href);
    } else {
        img.parentNode.removeChild(img);
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
	if (sourceMode) wysiwyg(false);
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

    str += whitespaceBefore(node,getPreviousMeaningfulNode(node));

    if (node.nodeType == node.TEXT_NODE) {
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

    str += whitespaceAfter(node,getNextMeaningfulNode(node));

    return str;
}

function getAttributes(node) {
    var attr = node.attributes;
    if (node && attr) {
        var str = "";
        for (var i = 0; i < attr.length; i++) {
            str += getAttribute(node,attr.item(i));
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

    var selection = editor_window.getSelection();

    //window.status = selection.anchorNode + "," + selection.anchorOffset + " " + selection.focusNode + "," + selection.focusOffset;

    if (selection.anchorNode == selection.focusNode) {
        if (selection.anchorNode.nodeType != selection.anchorNode.TEXT_NODE ) {
            var index = (selection.focusOffset == 0) ? 0 : selection.focusOffset - 1;
            return selection.anchorNode.childNodes[index];
        }
    }

    return selection.anchorNode;
}

function getPreviousMeaningfulNode(node) {
    if (node) {
        var previous = node.previousSibling;
        if (previous) {
            if (previous.nodeType == node.TEXT_NODE) {
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
            if (next.nodeType == node.TEXT_NODE) {
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
    return str.replace(trimRE,'');
}

var saneRE = /\n+/g;

/**
 * Removes all newlines
 */
function trimNewlines(str) {
    if (!str) return '';
    return str.replace(saneRE,'');
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
    if (node && (node.nodeType == node.ELEMENT_NODE)) {
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
    if (node && (node.nodeType == node.ELEMENT_NODE)) {
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
    if (node && (node.nodeType == node.TEXT_NODE)) {
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
        return undefined;
    }
}

function isTemplate(node) {
    if (node && (node.nodeType == node.ELEMENT_NODE)) {
		if (node.getAttribute("template") == "yes") {
			return true;
		} else if (node.nodeName.toLowerCase() == "br") {
			if (!node.nextSibling) {
				return true;
			} else {
				return isTemplate(node.nextSibling);
			}
		}
	}
	return false;
}

function setTemplate(node, status) {
    var value = (status) ? "yes" : "no";
    node.setAttribute("template",value);
}

function getAlternatives(node) {
    if (node) {
        switch (node.nodeName.toLowerCase()) {
            case "blockquote": return [ "quote", "code", "note", "fixme" ];
            case "img": return [ "inline", "floating-left", "floating-right" ];
            case "p": return [ "normal", "figure" ];
            default: return null;
        }
    }
}

function getAttribute(node,at) {
    var nodeName = node.nodeName.toLowerCase();
    var atName = at.name.toLowerCase();

    if ((nodeName == "img") && (atName == "href")) {
        return ' src="' + at.value + '"';
    } else if ((atName == "class") || (atName == "href")) {
        return " " + atName + '="' + at.value + '"';
    } else {
        return "";
    }
}

function getHiddenAttributes(node) {
    var nodeName = node.nodeName.toLowerCase();
    if (nodeName == "img") {
    	return ' width="' + node.width + '" height="' + node.height + '"';
    } else {
    	return '';
    }
} 
	
function preserveWhitespace(node) {
    return ((node.nodeName.toLowerCase() == "blockquote") && (node.getAttribute("class") == "code"));
}

function whitespaceBefore(currentNode,precedingNode) {
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

function whitespaceAfter(currentNode,followingNode) {
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
	str = str.replace(/\*((\w|\s)+)\*/g,"<b>$1</b>");
	str = str.replace(/\"((\w|\s)+)\"/g,"<q>$1</q>");
	return str;
}

/*
 * Escapes special charachters into entities
 */
function escape(str) {
	str = str.replace(/&/g,"&amp;");
	str = str.replace(/</g,"&lt;");
	str = str.replace(/>/g,"&gt;");
	str = str.replace(/\u00A0/g,"&#160;");
	return str;
}

/*
 * Resumes entities into special charachters
 * [this is done because Midas doesn't recognize all entities]
 */
function resume(str) {
	str = str.replace(/&amp;/g,"&");
	str = str.replace(/&lt;/g,"<");
	str = str.replace(/&gt;/g,">");
	//str = str.replace(/&#160;/g,\u00A0);
	return str;
}

// ------------------------------ end of file --------------------------
