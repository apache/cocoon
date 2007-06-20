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

importClass(Packages.org.apache.ojb.broker.PersistenceBrokerFactory);
importClass(Packages.org.apache.ojb.broker.query.Criteria);
importClass(Packages.org.apache.ojb.broker.query.QueryByCriteria);
importPackage(Packages.org.apache.cocoon.bean.query);


// QueryFavourites constructor
function QueryFavourites(user) {
	this._user = user;
}

// add a Query to the QueryFavourites
QueryFavourites.prototype.add = function(query) {
	query.user = this._user;
	query.date = new java.util.Date();
	var broker = null;
	try {
		broker = PersistenceBrokerFactory.defaultPersistenceBroker();
		broker.beginTransaction();
		broker.store(query);
		broker.commitTransaction();
	} catch (e) {
		cocoon.log.error(e);
		if(broker != null) broker.abortTransaction();
	} finally {
		if (broker != null) broker.close();
	}
}

// remove a Query from the QueryFavourites
QueryFavourites.prototype.remove = function(id) {
	var broker = null;
	var result = null;
	try {
		broker = PersistenceBrokerFactory.defaultPersistenceBroker();
		var criteria = criteria = new Criteria();
		criteria.addEqualTo("id", new java.lang.Long(id));
		//criteria.addEqualTo("user", new String(this._user));
    var query = new QueryByCriteria(SimpleLuceneQueryBean, criteria);
		result = broker.getObjectByQuery(query);
		broker.beginTransaction();
		broker["delete"](result);
		broker.commitTransaction();
	} catch (e) {
		cocoon.log.error(e);
		throw("error.no.favourite");
	} finally {
		if (broker != null) broker.close();
	}
	return result;	
}

// get a Query from the QueryFavourites using it's ID
QueryFavourites.prototype.get = function(id) {
	var broker = null;
	var result = null;
	try {
		broker = PersistenceBrokerFactory.defaultPersistenceBroker();
		var criteria = criteria = new Criteria();
		criteria.addEqualTo("id", new java.lang.Long(id));
		//criteria.addEqualTo("user", new String(this._user));
    var query = new QueryByCriteria(SimpleLuceneQueryBean, criteria);
		result = broker.getObjectByQuery(query);
	} catch (e) {
		cocoon.log.error(e);
		throw("error.no.favourite");
	} finally {
		if (broker != null) broker.close();
	}
	return result;	
}

// get a list of Queries from the QueryFavourites
QueryFavourites.prototype.list = function() {
	var broker = null;
	var result = null;
	try {
		broker = PersistenceBrokerFactory.defaultPersistenceBroker();
		var criteria = criteria = new Criteria();
		criteria.addEqualTo("user", new String(this._user));
    var query = new QueryByCriteria(SimpleLuceneQueryBean, criteria);
    query.addOrderByAscending("date");
		result = broker.getCollectionByQuery(query);
	} finally {
		if (broker != null) broker.close();
	}
	return result;	
}
