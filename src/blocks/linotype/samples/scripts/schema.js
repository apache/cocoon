
// ----------------------- Schema-related Functions -------------------------

Alternatives = {
	blockquote: [ "none", "quote", "code", "note", "fixme" ],
	img: [ "inline", "floating-left", "floating-right" ],
	p: [ "normal", "figure" ]
};

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
        str += "<" + name + escape(getAttributesFiltered(node));
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
        }
    }
    return false;
}

function getBlockFormat(node) {
    if (isBlock(node)) {
        switch (node.nodeName.toLowerCase()) {
            case "p": return 0;
            case "h1": return 1;
            case "h2": return 2;
            case "h3": return 3;
            case "h4": return 4;
        }
    }
	return -1;
}

function getParentBlock(node) {
	var parent = node.parentNode;
	if (isBlock(parent)) {
		return parent;
	} else {
		return getParentBlock(parent);
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
        }
    }
    return false;
}

function preserveWhitespace(node) {
	if (node && (node.nodeType == NodeType.ELEMENT_NODE)) {
        switch (node.nodeName.toLowerCase()) {
            case "blockquote":
            	return (getClass(node) == "code");
        }
    }
    return false;
}

function isMeaningfulWhitespace(node) {
	if (node && (node.nodeType == NodeType.TEXT_NODE)) {
        return (isInline(node.nextSibling) || isInline(node.previousSibling));
    } else {
        return false;
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
		if (Alternatives[name]) {
			return Alternatives[name];
		} else {
			return null;
        }
    }
}

function getAttributesFiltered(node) {
    var attr = node.attributes;
    if (node && attr) {
        var str = "";
        for (var i = 0; i < attr.length; i++) {
            str += getAttributeFiltered(node, attr.item(i));
        }
        return str += getHiddenAttributes(node);
    } else {
        return "";
    }
}

function getAttributeFiltered(node, at) {
    var nodeName = node.nodeName.toLowerCase();
	var atName = (at.name) ? at.name.toLowerCase() : "";

	if (((atName == "class") && (at.value != "")) || (atName == "href")) {
        return " " + atName + '="' + at.value + '"';
    } else {
        return "";
    }
}

function getHiddenAttributes(node) {
    var nodeName = node.nodeName.toLowerCase();
    if (nodeName == "img") {
		var href = node.getAttribute("ihref");
		var src = href.substring(href.lastIndexOf('/') + 1);
		return ' src="' + src + '" width="' + node.width + '" height="' + node.height + '"';
    } else {
        return '';
    }
} 
    
function whitespaceBefore(currentNode, precedingNode) {
    var current = currentNode.nodeName.toLowerCase();
    var preceding = (precedingNode) ? precedingNode.nodeName.toLowerCase() : "#none";

    if (current == "#text") {
        if (!isWhitespace(currentNode.nodeValue) && isBlock(precedingNode)) {
            return "\n";
        }
    } else if (isBlock(currentNode)) {
        if (isBlock(precedingNode) || (preceding == "li") || (preceding == "br")) {
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

function structurize(str) {
    str = str.replace(/\*((\w|\s)+)\*/g, "<b>$1</b>");
    str = str.replace(/_((\w|\s)+)_/g,   "<i>$1</i>");
    str = str.replace(/\/((\w|\s)+)\//g, "<i>$1</i>");
    return str;
}

function escape(str) {
    str = str.replace(/&/g, "&amp;");
    str = str.replace(/</g, "&lt;");
    str = str.replace(/>/g, "&gt;");
    str = str.replace(/\u00A0/g, "&#160;");
    return str;
}

function resume(str) {
    str = str.replace(/&amp;/g, "&");
    str = str.replace(/&lt;/g, "<");
    str = str.replace(/&gt;/g, ">");
    //str = str.replace(/&#160;/g, \u00A0);
    return str;
}

// ------------------------------ end of file --------------------------
