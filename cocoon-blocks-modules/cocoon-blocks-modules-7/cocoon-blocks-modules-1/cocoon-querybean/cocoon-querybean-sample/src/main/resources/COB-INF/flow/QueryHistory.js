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

importClass(Packages.java.util.ArrayList);
importClass(Packages.java.lang.Long);


// QueryHistory Object Constructor
function QueryHistory(attr) {
	try {
		this._history = cocoon.session.getAttribute(attr);
		if (this._history == null) {
			this._history = new ArrayList();
			cocoon.session.setAttribute(attr, this._history);
		}
	} catch (error) {
		cocoon.log.error(error);
	}
}

// add a Query to the QueryHistory
QueryHistory.prototype.add = function(item) {
	this._history.add(item);
}

// empty the QueryHistory
QueryHistory.prototype.clear = function() {
	this._history.clear();
}

// remove a Query from the QueryHistory
QueryHistory.prototype.remove = function(item) {
	this._history.remove(item);
}

// return a list of the Queries in the QueryHistory, in reverse order
QueryHistory.prototype.list = function() {
	var count = this._history.size();
	var history = new ArrayList(count);
	var index = 0;
	for (var position = 0; position < count; position++) {
		index = count - position - 1; // reverse the order
		history.add(position, {id: new Long(index), query: this._history.get(index)});
	}
	return history;
}

// get a Query from the QueryHistory, using it's ID, always returns a copy
QueryHistory.prototype.get = function(id, clone) {
	var item;
	try {
		item = this._history.get(parseInt(id));
	} catch (e1) {
		cocoon.log.error(e1);
		throw("error.no.history");
	}
	try {
		if (clone) {
			return item.clone();
		} else {
			return item;
		}
	} catch (e2) {
		cocoon.log.error(e2);
		throw("items stored in history need to be Cloneable");
	}
}

// move a Query to the top of the QueryHistory list
QueryHistory.prototype.promote = function(query) {
	this._history.remove(query);
	this._history.add(query);
}

// get the size of the QueryHistory list
QueryHistory.prototype.size = function() {
	return this._history.size();
}
