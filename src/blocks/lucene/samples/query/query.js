/*
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

// flowscripts for using the Query Bean
// $Id: query.js,v 1.2 2004/06/23 10:50:50 jeremy Exp $


cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js");

importClass (Packages.org.apache.cocoon.Constants);
importPackage (Packages.org.apache.cocoon.bean.query);
importClass (Packages.org.apache.cocoon.components.search.LuceneCocoonSearcher);
importClass (Packages.org.apache.cocoon.components.search.LuceneCocoonHelper);


var SESSION_ATTR = "_query_bean_history_"; // the name of the Session Attribute used by this code
var STANDARD_ANALYZER = "org.apache.lucene.analysis.standard.StandardAnalyzer";

// display the User's Search History
function history () {
	var history = getHistory ();
	var count = history.size ();
	var histlist = new java.util.ArrayList (count);
	var index = 0;
	for (var position = 0; position < count; position++) {
		index = count - position - 1; // reverse the order
		histlist.add (position, {id: new java.lang.Long (index), query: history.get (index)});
	}
	cocoon.sendPage (cocoon.parameters["screen"], {history: histlist});
}

// perform Searches
function simpleLuceneQuery () {
	var screen = cocoon.parameters["screen"];
	var type = cocoon.parameters["type"];
	var historyid = cocoon.parameters["id"];
	var directory = cocoon.parameters["lucene-directory"];
	var query = null;
	var results = null;
	var history = getHistory ();
	var searcher = cocoon.getComponent (LuceneCocoonSearcher.ROLE);
	var contextAccess = cocoon.createObject (ContextAccess);
	var avalonContext = contextAccess.getAvalonContext ();
	var page = null;
	var match = "".equals (cocoon.parameters["match"]) ? SimpleLuceneCriterion.ANY_MATCH : cocoon.parameters["match"];
	var field = "".equals (cocoon.parameters["field"]) ? SimpleLuceneCriterion.ANY_FIELD : cocoon.parameters["field"];
	try {
		if ( !"".equals (cocoon.parameters["page"]) ) page = new java.lang.Long (cocoon.parameters["page"]);
		if ( !"".equals (cocoon.parameters["query"]) ) { // test for: quick ?query
			query = new SimpleLuceneQueryBean (type, null, match, field, cocoon.parameters["query"]);
		} else if ( "".equals (historyid) ) {            // test for: new query
			query = new SimpleLuceneQueryBean (type, null, match, field, "");
			edit (query);
		} else {
			try {
				var edition = history.get (historyid);
				if (page == null) {                          // edit a query already in the history
					query = edition.copy ();                   // clone it first so history items are separate
					edit (query);    
				} else {                                     // page a query already in the history
					query = edition;
					query.page = page;
				}		
			} catch (e) {
				cocoon.sendPage("screen/error", {message: "search.error.nohistory"});
				return;
			}
		}
		if (query != null) {
			try {
				var index = getLuceneDirectory (avalonContext, directory)
				if (index == null) {
					cocoon.sendPage("screen/error", {message: "search.error.noindex"});
					return;
				}
				searcher.setDirectory (index);
				if (searcher.getAnalyzer () == null) searcher.setAnalyzer (LuceneCocoonHelper.getAnalyzer(STANDARD_ANALYZER));
				results = query.search (searcher);
			} catch (e) {
				cocoon.log.error (e);
				cocoon.sendPage("screen/error", {message: e});
				return;
			}
			var nav = pagerNavigation (query.total, query.page, query.size);
			if (page == null) {
				history.add (query); 													// add a fresh query to the history
			} else {
				history.remove (query); 											// move move a paged query to the top
				history.add (query);
			}
			historyid = new java.lang.Long (history.size () -1); 
			cocoon.sendPage (screen, {result: { results: results, nav: nav, query: query, id: historyid }});
		} else {
			cocoon.sendPage("screen/error", {message: "search.error.noquery"});
		}
	} catch (e) {
		cocoon.log.error (e);
		cocoon.sendPage("screen/error", {message: e});
	} finally {
		cocoon.releaseComponent (searcher);
		cocoon.disposeObject (contextAccess);
	}
}

// allow the user to edit the query
function edit (query) {
	var form = new Form (cocoon.parameters["form-definition"]);
	form.createBinding (cocoon.parameters["bindingURI"]);
	form.load (query);
	form.showForm (cocoon.parameters["form"]);
	if ("submit".equals (form.submitId)) {
		form.save (query);
	}
}

// get or setup the User's History in the Session
function getHistory () {
	var history = cocoon.session.getAttribute (SESSION_ATTR);
	if (history == null) {
		history = new java.util.ArrayList ();
		cocoon.session.setAttribute (SESSION_ATTR, history);
	}
	return history;
}

// Utility function to work out the directory to use as the Lucene Index 
function getLuceneDirectory (avalonContext, directory) {
	var index = new java.io.File (directory);
	if (!index.isAbsolute ()) {
		var workDir = avalonContext.get (Constants.CONTEXT_WORK_DIR);
		index = new java.io.File(workDir, directory);
	}
	if (!index.exists ()) {
		return null;
	} else {
		return LuceneCocoonHelper.getDirectory (index, false);
	}
}

// Utility function to create a 'paging record' for the display of long paged lists of records 
function pagerNavigation (total, page, size) {
	var pages = Math.ceil (total/size);
	var index = new java.util.ArrayList ();
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
			end = page.intValue () + off;
		}
	} 
	for (var i = start; i < end; i++) index.add (new java.lang.Integer (i));
	var firstIndex = 0;
	var lastIndex = 0;
	try {
		firstIndex = index.get (0);
		lastIndex = index.get (index.size()-1);
	} catch (e) {}
	return (
		{ 
			total: total, 
			next: total > (page.intValue () * size.intValue ()) + size.intValue () ? new java.lang.Integer (page.intValue () + 1) : null, 
			prev: page > 0 ? new java.lang.Integer (page - 1) : null, 
			size: new java.lang.Integer (size), 
			page: new java.lang.Integer (page), 
			pages: new java.lang.Integer (pages),
			index: index,
			firstIndex: firstIndex,
			lastIndex: lastIndex
		}
	);
}
