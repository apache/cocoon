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


// QueryFavourites constructor
function QueryFavourites(user) {
	this._user = user;
	// to be implemented using Apache OJB
}

// add a Query to the QueryFavourites
QueryFavourites.prototype.add = function(query) {
	// to be implemented using Apache OJB
}

// remove a Query from the QueryFavourites
QueryFavourites.prototype.remove = function(id) {
	// to be implemented using Apache OJB
}

// get a Query from the QueryFavourites using it's ID
QueryFavourites.prototype.get = function(id) {
	// to be implemented using Apache OJB
	throw("error.no.favourite");
}

// get a list of Queries from the QueryFavourites
QueryFavourites.prototype.list = function() {
	// to be implemented using Apache OJB
	return new java.util.ArrayList(1);	
}

// close the QueryFavourites
QueryFavourites.prototype.close = function() {
	// to be implemented using Apache OJB
}
