
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

function getISODate() {
    // taken from http://321webliftoff.net/isodatetime.php, many thanks!
	var today = new Date();
	var year  = today.getYear();
	if (year < 2000)    // Y2K Fix, Isaac Powell
	year = year + 1900; // http://onyx.idbsu.edu/~ipowell
	var month = today.getMonth() + 1;
	var day  = today.getDate();
	var hour = today.getHours();
	var hourUTC = today.getUTCHours();
	var diff = hour - hourUTC;
	var hourdifference = Math.abs(diff);
	var minute = today.getMinutes();
	var minuteUTC = today.getUTCMinutes();
	var minutedifference;
	var second = today.getSeconds();
	var timezone;
	if (minute != minuteUTC && minuteUTC < 30 && diff < 0) { hourdifference--; }
	if (minute != minuteUTC && minuteUTC > 30 && diff > 0) { hourdifference--; }
	if (minute != minuteUTC) {
		minutedifference = ":30";
	} else {
		minutedifference = ":00";
	}
	if (hourdifference < 10) { 
		timezone = "0" + hourdifference + minutedifference;
	} else {
		timezone = "" + hourdifference + minutedifference;
	}
	if (diff < 0) {
		timezone = "-" + timezone;
	} else {
		timezone = "+" + timezone;
	}
	if (month <= 9) month = "0" + month;
	if (day <= 9) day = "0" + day;
	if (hour <= 9) hour = "0" + hour;
	if (minute <= 9) minute = "0" + minute;
	if (second <= 9) second = "0" + second;
	return year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":" + second + timezone;
}

var trimRE = /(^\s+)|(\s+$)/g;

function trimWhitespace(str) {
    if (!str) return '';
    return str.replace(trimRE, '');
}

var saneRE = /\n+/g;

function trimNewlines(str) {
    if (!str) return '';
    return str.replace(saneRE, '');
}

function isWhitespace(str) {
    return trimWhitespace(str).length == 0;
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
        } else {
        	return node;
        }
    } else {
    	return null;
    }
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
        } else {
        	return node;
        }
    } else {
    	return null;
    }
}


function getPath(node) {
    if (node) {
        var name = node.nodeName;
        if (node.parentNode) {
            return getPath(node.parentNode) + "/" + name;
        } else if (name != "#document") {
            return name;
        }
    } 
    return "";
}

/*
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

NodeType = { 
	 ELEMENT_NODE: 0x01,
	 ATTRIBUTE_NODE: 0x02,
	 TEXT_NODE: 0x03,
	 CDATA_NODE: 0x04,
	 ENTITY_REFERENZE_NODE: 0x05,
	 ENTITY_NODE: 0x06,
	 SCRIPT_NODE: 0x07,
	 COMMENT_NODE: 0x08,
	 DOCUMENT_NODE: 0x09,
	 DOCUMENT_FRAGMENT_NODE: 0x0a,
	 NOTATION_NODE: 0x0b
};

// ------------------------------ end of file --------------------------
