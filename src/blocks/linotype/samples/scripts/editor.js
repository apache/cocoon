
// -------------------------- global variables ------------------------------

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

var previousKey;
var modified = false;
var initialized = false;

var imagePrefix = "image";
var imageCounter = 0;
var imageData = new Array();

var sourceMode = false;

// ----------------------- UI-modifying functions ---------------------------

function start(e) {

	if (initialized) {
		alert("already initialized");
		return;
	}
	
	// First of all, setup the editing canvas
	try {
		iframe = document.getElementById('edit');
		editor_window = iframe.contentWindow;
		editor = editor_window.document;
		editor.designMode = "On";
		addEvent(editor,"click",click,true);
		addEvent(editor,"keydown",keypress,true);
		try {
			editor.execCommand("useCSS", false, true);
		} catch(e) {}
	} catch (e) {
		alert("I'm sorry, but Linotype doesn't work on this browser: " + e.name + " " + e.message);
		return;
	}
	
	// If we get here, the browser supports "designMode" so we
	// instrument the active parts of the page
	path = document.getElementById('editpath').firstChild;
	formatblock = document.getElementById('formatblock');
	formatblock.onchange = formatblockChange;
	alternatives = document.getElementById('alternatives');
	block_selector = document.getElementById('block_selector');
	class_selector = document.getElementById('class_selector');
	image_inputs = document.getElementById('image_inputs');
	image_controls = document.getElementById('image_controls');
    document.getElementById("wysiwyg-checkbox").checked = true;

	// Then we instrument the toolbar <div> and turn them into buttons
    divs = document.getElementsByTagName('div');
    for (var i = 0; i < divs.length; i++) {
		var nameclass = getClass(divs[i]);
		if (nameclass == "imagebutton") {
            divs[i].onmousedown = buttonDown;
            divs[i].onmouseup = buttonUp;
            divs[i].onmouseover = buttonOver;
            divs[i].onmouseout = buttonOut;
            divs[i].onclick = buttonClick;
        }
    }

	// Then we instrument the images that might be already contained
	// in the editing canvas.
	instrumentImages(editor);
	
	// set the initialization flag to avoid onload event loops
	initialized = true;
	
	// and here we go!
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
        
		if (target && (target.nodeType == NodeType.TEXT_NODE)) {
            target = target.parentNode;
        }
        
        var format = getBlockFormat(target);
        if (format != -1) {
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
			var type = getClass(target);
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
    
    return target;
}

function wysiwyg(enabled) {
    if (enabled) {
        var text = serializeChildren(editor.body);
		var source = editor.createTextNode(text);
        editor.body.innerHTML = "";
        editor.body.appendChild(source);
		setClass(editor.body, "source");
        sourceMode = true;
        updateUI(true);
    } else {
    	var source = getContentSource();
    	editor.body.innerHTML = source;
		setClass(editor.body,"body");
        reinstrumentImages(editor);
        sourceMode = false;
    }
}

function loadContent(content) {
    editor.body.innerHTML = content;
}

function addImage() {
    editor.execCommand("insertimage", false, "template.jpg");
    var img = getOrigin();
    var input = instrumentImg(img, true);
    activateImgInstrumentation(input, img);
}

function instrumentImages(edit) {
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
    var id = imageCounter++;
    var imgID = imagePrefix + "-" + id;
    var src = img.getAttribute("src");

    img.setAttribute("id", imgID);
    img.setAttribute("ihref", src);
    setTemplate(img, template);

    addEvent(img.parentNode,"DOMNodeRemoved",imageRemoved);
    
    imageData[imgID] = [src, src, template];

    var input = document.createElement("input");
    input.setAttribute("type", "file");
    input.setAttribute("id", imgID + "-input");
    input.setAttribute("size", "1");
    setClass(input, "image_browser");
    addEvent(input,"click", inputChange);
    addEvent(input,"change", inputChange);
    input.style.position = "absolute";
    input.style.visiblility = "hidden";

    image_inputs.appendChild(input);
    
    return input;
}

function reinstrumentImg(img, id) {
    if (!id) {
        var src = img.getAttribute("src");
        var id = src.substring(src.lastIndexOf('/'), src.lastIndexOf('.'));
    }

    var data = imageData[id];
	
	img.setAttribute("id",id);
	img.setAttribute("src",data[0]);
	img.setAttribute("ihref",data[1]);
	setTemplate(img, data[2]);
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

function block() {
	editor.execCommand("formatblock", false, getCommandFormat("P"));
}

function unblock() {
	var block = getParentBlock(getOrigin());
	var parent = block.parentNode;
    var children = block.childNodes;
	for (var i = children.length - 1; i >= 0; i--) {
    	parent.insertBefore(children.item(i),block);
	}
	parent.removeChild(block);
}

// --------------------- Content-related functions -------------------

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

// ----------------------- Event functions ---------------------------

function keypress(e) {
	var key, ch = 0
    modified = true;
	if (!e) e = edit.event;
	key = e.keyCode;

	//var target = getPreviousMeaningfulNode(updateUI());

    //window.status = "[" + key + "," + e.shiftKey + "," + e.ctrlKey + "," + e.altKey + "," + e.metaKey + "]";
	
    try {
        // Disable "Back" / "Forward" buttons (Alt|Apple+LeftArrow / Alt|Apple+RightArrow)
        if (e.altKey || e.metaKey) {
            if (key > 0) {
                switch (key) {
                	// FIXME(SM): is seems that these events aren't canceled by
                	// the event cancellation model in Mozilla and Firebird
                	// but if we trigger an alert() box they are stopped
                	// it's hacky, I know, but I can't find a better way and
                	// it is vital that I stop these since it's muscle memory
                	// for MacOS users to use Apple+arrows to move to star/end
                	// of line and if done here we loose all the content without
                	// a warning!!
                    case 37: // left arrow
                    	alert("blocking Alt|Apple+LeftArrow");
                        e.stopPropagation();
                        e.preventDefault();
                        e.returnValue = false;
                        return false;
                    case 39: // right arrow
                    	alert("blocking Alt|Apple+RightArrow");
                        e.stopPropagation();
                        e.preventDefault();
                        e.returnValue = false;
                        return false;
                }
            }
        
        // FIXME(SM): Here is the place to look for advanced keyboard interaction with the
        // schema, for example when doing two returns in a particular location
        // will generate a particular element, but since this is a hell of
        // state control and I'm a lazy ass, we'll do it in the future ;-)
        
        //} else if (key == 13) {
        //	if (previousKey == 13) {
        //   	editor.execCommand("insertparagraph", false, null);
        //	} else if (target.parentNode.nodeName == "H1") {
        //    	//editor.execCommand("insertparagraph", false, null);
        //  }
        
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
    var format = formatblock.options[selection].value;
	var commandFormat = getCommandFormat(format);
	editor.execCommand("formatblock", false, commandFormat);
    updateUI(true);
    editor_window.focus();
    modified = true;
}

function alternativesChange() {
    var selection = alternatives.selectedIndex;
    var selectionClass = alternatives.options[selection].value;
	setClass(alternativesTarget,selectionClass);
}

function buttonClick() {
    if (this.id == "createlink") {
        var href = prompt("Enter a URL:", "");
        editor.execCommand(this.id, false, href);
    } else if (this.id == "insertimage") {
        addImage();
    } else if (this.id == "block") {
    	block();
    } else if (this.id == "unblock") {
    	unblock();
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

function imageRemoved(e) {
	var target = getTarget(e,this);
	if (target) {
		var id = target.getAttribute("id");
	    var input = document.getElementById(id + "-input");
	    if (input) deactivateImgInstrumentation(input);
	}
}

function inputChange(e) {
	var input = getTarget(e,this);
    var inputID = input.getAttribute("id");
    var imgID = inputID.substring(0, inputID.indexOf("-input"));
    var img = editor.getElementById(imgID);
    if (input.value != "") {
        var src = "file:///" + input.value;
        var href = imgID + input.value.substring(input.value.lastIndexOf('.'));
        var newImg = document.createElement("img");
        img.parentNode.replaceChild(newImg, img);
        imageData[imgID] = [src, href, false];
        reinstrumentImg(newImg, imgID);
        deactivateImgInstrumentation(input);
        input.setAttribute("name", "save:" + href);
    } else {
        img.parentNode.removeChild(img);
        // this will cause a "node removed" event that will cause
        // the imageRemoved() function to be called, cleaning up
        // the instrumented input field
    }
}

// ------------------------------ end of file --------------------------
