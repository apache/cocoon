// ------------------------- Browser dependent functions --------------------

var IE = (document.all) ? true : false;

function addEvent(obj,type,func,bol) {
	if (IE) {
		obj.attachEvent(type,func);
	} else {
		obj.addEventListener(type.replace(/^on/g,""),func,bol);
	}
}

function removeEvent(obj,type,func,bol) {
	if (IE) {
		obj.detachEvent(type,func);
	} else {
		obj.removeEventListener(type.replace(/^on/g,""),func,bol);
	}
}

function setClass(obj,value) {
	if (IE) {
		obj.className = value;
	} else {
		obj.setAttribute("class",value,false);
	}
}

function getClass(obj) {
	return (IE) ? obj.className : obj.getAttribute("class");
}

function getTarget(e,obj) {
	if (IE) {
		return window.event.srcElement;
	} else {
		if (e) {
			return e.target;
		} else {
			return obj;
		}
	}
}

function getContentSource() {
	var iframe = document.getElementById('edit');
	var editor_body = iframe.contentWindow.document.body;
	if (IE) {
		return editor_body.createTextRange();
	} else {
        var source = editor_body.ownerDocument.createRange();
        source.selectNodeContents(editor.body);
        return source.toString();
	}
}

function getSelection(iframe) {
	if (IE) {
		return iframe.contentWindow.document.selection.createRange();
	} else {
		return iframe.contentWindow.getSelection();
	}
}

function getCommandFormat(block) {
	return (IE) ? "<"+block+">" : block;
}

// ------------------------ WARNING: HOTSPOT! ---------------------------
// since it's called for every keystroke, keep it as fast as possible!!!
// ----------------------------------------------------------------------

function getOrigin() {

	var iframe = document.getElementById('edit');
	var selection = getSelection(iframe);
	
	if (IE) {
		var event = iframe.contentWindow.event;
		if (selection.type == "Control") {
			 if (selection.length > 1) {
			 	alert("Error too many obj");
			 } else {
			 	return selection(0);
			 }
		} else {
			if ((selection.parentElement().nodeType != NodeType.TEXT_NODE) && event) {
				var nodes = selection.parentElement().childNodes;
				if (nodes.length == 1) return nodes[0];
				for (var co = 0; co < nodes.length; co++) {
					if (!nodes[co].offsetTop && nodes[co].nextSibling) {
						if ((nodes[co].nextSibling.offsetTop <= event.y) 
						     && (nodes[co].nextSibling.offsetTop + nodes[co].nextSibling.offsetHeight >= event.y) 
						     && (nodes[co].nextSibling.offsetLeft <= event.x)) { 
								return nodes[co];
						}
					} else if ((nodes[co].offsetTop <= event.y) 
					            && (nodes[co].offsetTop + nodes[co].offsetHeight >= event.y)) {
						return nodes[co];
					}
				}
				return nodes[nodes.length-1];
			}
		}
		return selection.parentElement();
	} else {
	    if (selection.anchorNode == selection.focusNode) {
	        if (selection.anchorNode.nodeType != selection.anchorNode.TEXT_NODE) {
	            var index = (selection.focusOffset == 0) ? 0 : selection.focusOffset - 1;
	            return selection.anchorNode.childNodes[index];
	        }
	    }
	    return selection.anchorNode;
	}
}
