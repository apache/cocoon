/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
// flowscripts util for paging navigation
// $Id$



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
