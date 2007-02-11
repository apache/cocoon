/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

// flowscripts for using the Query Bean
// $Id: query.js,v 1.3 2004/10/22 12:14:23 jeremy Exp $

cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js");
cocoon.load("flow/QuerySearcher.js");
cocoon.load("flow/QueryHistory.js");
cocoon.load("flow/QueryFavourites.js");

// display the User's Search History
function showHistory() {
	var history = new QueryHistory(cocoon.parameters["history"]);
	cocoon.sendPage(cocoon.parameters["screen"], {history: history.list()});
}

// erase the User's Search History
function clearHistory() {
	var history = new QueryHistory(cocoon.parameters["history"]);
	history.clear();
	cocoon.sendPage(cocoon.parameters["screen"]);
}

// display the User's Favourite Searches
function showFavourites() {
	var favourites = null;
	try {
		favourites = new QueryFavourites(cocoon.parameters["user-id"]);
		cocoon.sendPage(cocoon.parameters["screen"], {queries: favourites.list()});
	} catch (error) {
		cocoon.sendPage("screen/error", {message: error});
	}
}


// add a history item to the User's Favourite Searches
function addFavourite() {
	var history = new QueryHistory(cocoon.parameters["history"]);
	var favourites = null;
	try {
		favourites = new QueryFavourites(cocoon.parameters["user-id"]);
		var query = history.get(cocoon.parameters["hid"], false);
		if (query != null) {
			favourites.add(query);
		}
		cocoon.sendPage(cocoon.parameters["screen"], {queries: favourites.list()});
	} catch (error) {
		cocoon.log.error(error);
		cocoon.sendPage("screen/error", {message: error});
	}
}

// add an item from the User's Favourite Searches, using it's ID
function removeFavourite() {
	var favourites = null;
	try {
		favourites = new QueryFavourites(cocoon.parameters["user-id"]);
		favourites.remove(cocoon.parameters["fid"]);
		cocoon.sendPage(cocoon.parameters["screen"], {queries: favourites.list()});
	} catch (error) {
		cocoon.log.error(error);
		cocoon.sendPage("screen/error", {message: error});
	}
}

// perform searches
function doSearch() {
	var screen = cocoon.parameters["screen"];
	var searcher = null;
	var favourites = null;
	var history = new QueryHistory(cocoon.parameters["history"]);
	try {
		searcher = new QuerySearcher(cocoon.parameters["lucene-directory"], cocoon.parameters["lucene-analyzer"]);
		favourites = new QueryFavourites();
		var result = null;
		if (!"".equals(cocoon.parameters["page"])) { 					// paging an existing Query
			result = searcher.page(cocoon.parameters["page"], cocoon.parameters["hid"], history);
		} else if (!"".equals(cocoon.parameters["query"])) { 	// running a quick Query
			result = searcher.quicksearch(cocoon.parameters["type"], cocoon.parameters["bool"], cocoon.parameters["match"], cocoon.parameters["field"], cocoon.parameters["query"], cocoon.parameters["size"], history);
		} else if (!"".equals(cocoon.parameters["fid"])) { 		// running a favourite Query
			result = searcher.search(favourites.get(cocoon.parameters["fid"]), history);
		} else if ("".equals(cocoon.parameters["hid"])) { 			// making a new Query to edit
			var query = searcher.newquery(cocoon.parameters["type"], cocoon.parameters["bool"], cocoon.parameters["match"], cocoon.parameters["field"]);
			if (edit(query)) {
				result = searcher.search(query, history);
			} else {
				cocoon.sendPage("screen/cancelled", {message: "cancel.note"});
				return;				
			}
		} else { 																							// editing a Query from history
			var query = history.get(cocoon.parameters["hid"], true);
			if (edit(query)) {
				result = searcher.search(query, history);
			} else {
				cocoon.sendPage("screen/cancelled", {message: "cancel.note"});
				return;	
			}
		}
		cocoon.sendPage(screen, {result: result});
	} catch (error) {
		cocoon.log.error(error);
		cocoon.sendPage("screen/error", {message: error});	
	} finally {
		if (searcher != null) searcher.close();
	}
}


// allow the user to edit the query
function edit(query) {
	var form = new Form(cocoon.parameters["form-definition"]);
	form.createBinding(cocoon.parameters["bindingURI"]);
	form.load(query);
	form.showForm(cocoon.parameters["form"]);
	if ("_submit".equals(form.submitId)) {
		form.save(query);
		cocoon.log.debug("form submitted");
		query.id = null; // this is no longer a favourite, now it has been edited
		return true;
	} else {
		cocoon.log.debug("form cancelled");
		return false;
	}
}
