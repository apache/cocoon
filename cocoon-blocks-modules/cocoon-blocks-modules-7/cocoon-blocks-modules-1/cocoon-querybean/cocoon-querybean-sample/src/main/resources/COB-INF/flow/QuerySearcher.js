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

importClass(Packages.org.apache.cocoon.components.search.LuceneCocoonSearcher);
importClass(Packages.org.apache.cocoon.components.search.LuceneCocoonHelper);
importClass(Packages.org.apache.cocoon.Constants);
importClass(Packages.org.apache.cocoon.bean.query.SimpleLuceneQuery)
importClass(Packages.org.apache.cocoon.bean.query.SimpleLuceneQueryBean)
importClass(Packages.org.apache.cocoon.bean.query.SimpleLuceneCriterion)
importClass(Packages.org.apache.cocoon.bean.query.SimpleLuceneCriterionBean)
importClass(Packages.org.apache.cocoon.bean.query.ContextAccess)

cocoon.load("flow/pager.js");

// QuerySearcher constructor
function QuerySearcher(directory, analyzer) {
	var contextAccess;
	this._low = SimpleLuceneQueryBean.DEFAULT_PAGE_SIZE / 2;
	this._high = SimpleLuceneQueryBean.DEFAULT_PAGE_SIZE * 5;
	try {
		contextAccess = cocoon.createObject(ContextAccess);
		var index = new java.io.File(directory);
		if (!index.isAbsolute()) {
			var workDir = contextAccess.getAvalonContext().get(Constants.CONTEXT_WORK_DIR);
			index = new java.io.File(workDir, directory);
		}
		if (!index.exists()) throw ("search.error.noindex");
		this._searcher = cocoon.getComponent(LuceneCocoonSearcher.ROLE);
		this._searcher.setDirectory(LuceneCocoonHelper.getDirectory(index, false));
		if (this._searcher.getAnalyzer() == null) {
			this._searcher.setAnalyzer(LuceneCocoonHelper.getAnalyzer(analyzer));
		}
	} catch (error) {
		cocoon.log.error(error);
		throw (error);
	} finally {
		cocoon.disposeObject(contextAccess);		
	}
}

// cleanup
QuerySearcher.prototype.close = function() {
	cocoon.releaseComponent(this._searcher);
}

// perform a search using a Query
QuerySearcher.prototype.search = function(query, history) {
	if (query != null) {
		var results = query.search(this._searcher);
		history.add(query);
		var historyid = new java.lang.Long(history.size() -1);
		var nav = pagerNavigation(query.total, query.page, query.size);
		return { results: results, nav: nav, query: query, id: historyid, tip: this.getTip(query) };
	} else {
		throw("search.error.nohistory");
	}
}

// perform a page using a Query from history
QuerySearcher.prototype.page = function(page, id, history) {
	var p;
	var query = history.get(id, true);
	if (query != null) {
		try {
			p = new java.lang.Long(page);
		} catch (error) {
			p = SimpleLuceneQueryBean.DEFAULT_PAGE;
		}
		query.setPage(p);
		var results = query.search(this._searcher);
		//history.promote(query); // this was causing addition to history while paging (why?), did not want this ......
		var historyid = new java.lang.Long(history.size() -1);
		var nav = pagerNavigation(query.total, query.page, query.size);
		return { results: results, nav: nav, query: query, id: historyid };
	} else {
		throw("search.error.nohistory");
	}
}

// perform a quick search using params
QuerySearcher.prototype.quicksearch = function(type, bool, match, field, value, size, history) {
	var s;
	if ("".equals(match) || match == undefined) match = SimpleLuceneCriterion.ANY_MATCH;
	if ("".equals(field) || field == undefined) field = SimpleLuceneCriterion.ANY_FIELD;
	if ("".equals(bool) || bool == undefined) bool = null;
	try {
		s = new java.lang.Long(size);
	} catch (error) {
		s = SimpleLuceneQueryBean.DEFAULT_PAGE_SIZE;
	}
	var query = new SimpleLuceneQueryBean(type, bool, match, field, value);
	query.setSize(s);
	return this.search(query, history);
}

// make a new query
QuerySearcher.prototype.newquery = function(type, bool, match, field) {
	if ("".equals(match) || match == undefined) match = SimpleLuceneCriterion.ANY_MATCH;
	if ("".equals(field) || field == undefined) field = SimpleLuceneCriterion.ANY_FIELD;
	if ("".equals(bool) || bool == undefined) bool = SimpleLuceneQueryBean.OR_BOOL;
	return new SimpleLuceneQueryBean(type, bool, match, field, "");
}

QuerySearcher.prototype.getTip = function(query) {
	if (query.total > this._high) {
		return ("query.tip.high");
	} else if (query.total < this._low) {
		var allProhibited = true;
		var criteria = query.getCriteria();
		for (var i = 0; i < criteria.size(); i++) {
			if (!criteria.get(i).isProhibited()) {
				allProhibited = false;
				break;
			}
		}
		if (allProhibited) {
			return ("query.tip.prohibited");
		} else {
			return ("query.tip.low");
		}
	} else {
		return null;
	}
}



