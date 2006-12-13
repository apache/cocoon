/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * A JavaScript wrapper for the JDBI library (http://jdbi.codehaus.org)
 *
 * @version $Id$
 */

function JDBI(datasource) {
    var selector = cocoon.getComponent(Packages.org.apache.avalon.excalibur.datasource.DataSourceComponent.ROLE + "Selector");
    var datasource = selector.select(datasource);
    // Wrap datasource as a JDBI ConnectionFactory.
    this.dbi = new Packages.org.skife.jdbi.DBI(
        new JavaAdapter(Packages.org.skife.jdbi.ConnectionFactory, {
            getConnection: function() { return datasource.getConnection() }
        })
    );
    // note: Rhino 1.6 allows to write
    // this.dbi = new Packages.org.skife.jdbi.DBI(function() { return datasource.getConnection() });

    // Plug a statement locator that will use Cocoon's source resolver
    var loadFunc = function(name) {
            var resolver = cocoon.getComponent(Packages.org.apache.excalibur.source.SourceResolver.ROLE);
            var src = resolver.resolveURI(name);
            try {
                if (!src.exists()) return null;
                var result = Packages.org.apache.commons.io.IOUtils.toString(src.getInputStream());
            } finally {
                resolver.release(src);
            }
            return result;
        };
    
    this.dbi.setStatementLocator(new JavaAdapter(Packages.org.skife.jdbi.tweak.StatementLocator, { load: loadFunc } ));
    // note: Rhino 1.6 allows to write
    // this.dbi.setStatementLocator(loadFunc);
}

/**
 * Convert a variable number of arguments to a Map. If there are several arguments, they
 * are individually converted to a Map and combined using CombiningMap.
 * FIXME: should be moved to a more efficient helper class
 */
JDBI.combine = function() {
    if (arguments.length == 0) {
        return java.util.Collections.EMPTY_MAP;
    } else if (arguments.length == 1) {
        return JDBI._toMap(arguments[1]);
    } else {
        var result = new Packages.org.apache.cocoon.forms.util.CombiningMap();
        for (var i = 0; i < arguments.length; i++) {
            result.add(JDBI._toMap(arguments[i]))
        }
        return result;
    }
}

JDBI._toMap = function(obj) {
    // FIXME: is there a better way to test "instanceof" a java class?
    var mapClass = java.lang.Class.forName("java.util.Map");
    if (mapClass.isInstance(obj)) {
        return obj;
    }
    var result = new java.util.HashMap();
    for (var prop in obj) {
        result.put(prop, obj[prop]);
    }
    return result;
}

JDBI.prototype.open = function() {
	return this.dbi.open();
}

/**
 * Registers one or more named statements. If <code>statement</code> is given,
 * then a single statement is registered. Otherwise, <code>name</code> is considered
 * as an object of which each property defines a statement.
 */
JDBI.prototype.name = function(name, statement) {
    if (statement) {
        this.dbi.name(name, statement);
    } else {
        // Assume it's a mapping
        for (var i in name) {
            this.dbi.name(i, name[i]);
        }
    }
}

JDBI.prototype.__do = function(name, statement, args) {
	var handle = this.dbi.open();
	try {
		var results = args ? handle[name](statement, args) : handle[name](statement);
	} finally {
		handle.close();
	}
	return results;
}

JDBI.prototype.query = function(statement, args) {
	return this.__do("query", statement, args);
}

JDBI.prototype.first = function(statement, args) {
	return this.__do("first", statement, args);
}

JDBI.prototype.execute = function(statement, args) {
	return this.__do("execute", statement, args);
}

JDBI.prototype.update = function(statement, args) {
	return this.__do("update", statement, args);
}
