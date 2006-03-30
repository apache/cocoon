// flowscripts util for paging navigation
// $Id: pager.js,v 1.4 2004/07/05 14:40:31 savs Exp $



/*
	Utility function to create a 'paging record' for the display of long paged lists of records 
*/

function pagerNavigation(total, page, size) {
	total = parseInt(total); // make sure JS realises they are Numbers, so we can add them without getting string concatenation !!!
	page = parseInt(page);
	size = parseInt(size);
	var pages = Math.ceil(total/size);
	var index = new java.util.ArrayList();
	var off = 5; // half the max # of slots to see
	var start = 0;
	var end = pages;
	if (pages > (off*2)) {
		if (page < off) { // if we are close to the left
			start = 0;
			end = start + (off*2);
		} else if (page > (pages - off)) { // if we are close to the right
			start = pages - (off*2);
			end = pages;
		} else { // we are somewhere in the middle
			start = page - off;
			end = page + off;
		}
	} 
	for (var i = start; i < end; i++) index.add(new java.lang.Integer(i));
	var firstIndex = 0;
	var lastIndex = 0;
	try {
		firstIndex = index.get(0);
		lastIndex = index.get(index.size()-1);
	} catch (e) {}
	var record = { 
			total: new java.lang.Integer( total), 
			next: total > ((page * size) + size) ? new java.lang.Integer(page + 1) : null, 
			prev: page > 0 ? new java.lang.Integer(page - 1) : null, 
			size: new java.lang.Integer(size), 
			page: new java.lang.Integer(page), 
			pages: new java.lang.Integer(pages),
			index: index,
			firstIndex: firstIndex,
			lastIndex: lastIndex
		};
	return record;
}
