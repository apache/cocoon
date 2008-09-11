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


/*
    System Library of static Collection Helper Functions
    
    The useful stuff (so far) :
    
      Turn a JS Object or Array into a suggestion-list you can use from a form
      
      Get a function that can sort JS Objects based on one of their properties 

    @version $Id$

*/

// setup the namespace TODO: dojo supposedly runs in Rhino, maybe we could use some of it's inheritance helpers in FlowScript
var System = System || {};
System.Collections = System.Collections || {};/*  */


System.Collections.doSuggestionList = function(){
  var suggestable = System.Collections.getSuggestionList(cocoon.parameters["id"]);
	var suggestions = [];
	var filter = cocoon.request.getParameter("filter");
	var id = cocoon.request.getParameter("id");
  var locale = Packages.org.apache.cocoon.i18n.I18nUtils.parseLocale(cocoon.request.get("locale"));
	if (filter) {
	  suggestions = suggestable.getSuggested(locale, filter, cocoon.request.getParameter("ignoreCase"), cocoon.request.getParameter("start"), cocoon.request.getParameter("count"));
	} else if (id) {
	  suggestions = suggestable.getById(locale, id);
	} else {
	  suggestions = suggestable.getAll(locale, cocoon.request.getParameter("start"), cocoon.request.getParameter("count"));
	}
	var payload = { // dojo.data style json payload 
	  identifier: System.Collections.getDefaults().getIdKey(),
	  label: System.Collections.getDefaults().getValueKey(),
	  numRows: suggestable.getAll(locale).length, /* not ideal, but it's cached */
	  items: suggestions
	};
	//cocoon.sendPage("send-json", {json: "/* {identifier:'" +  + "',label:'" +  + "',numRows:" + suggestable.getAll(locale).length + ",items:" + suggestions.toSource() + "} */"});
  cocoon.sendPage("send-json", {json: "/* " + payload.toSource() + " */"});
};

/* Encapsulated Defaults for Collections Functions */
System.Collections.getDefaults = function() {
  // private properties
  var SUGGESTABLES_CACHE = "System.Collections.Suggestables.Cache";
  var SUGGESTION_CACHE = "System.Collections.Suggestables.lastSuggestionCache";
  var IDKEY = "value";
  var VALUEKEY = "label";

  return { // public functions
    getSuggestableCacheKey: function(){ return SUGGESTABLES_CACHE; },
    getLastSuggestionCacheKey: function(){ return SUGGESTION_CACHE; },
    getIdKey: function(){ return IDKEY; },
    getValueKey: function(){ return VALUEKEY }
  };
};



System.Collections.registerSuggestionList = function(id, object, valueProperty, labelProperty, i18nCatalogue) {
  var defaults = System.Collections.getDefaults();
  if (cocoon.context.getAttribute(defaults.getSuggestableCacheKey()) == null) cocoon.context.setAttribute(defaults.getSuggestableCacheKey(), {});
  var cache = cocoon.context.getAttribute(defaults.getSuggestableCacheKey());
  var suggestable = cache[id];
  if (!suggestable) { // only register an id once
    print("Registering new Suggestable: " + id);
    cache[id] = suggestable = {};
    if (i18nCatalogue) suggestable.catalog = i18nCatalogue;
    if (typeof object === "function") {
      suggestable.generator = object;
    } else if (object instanceof Array || typeof object === "array") {
      var items = [];
      for (var i = 0; i < object.length; i++) {
        var item = object[i];
        var obj = {};
        var keys = [];
        if (typeof item === 'string') {
          var key = "" + item; // only add JS Strings // what about Numbers etc.?
          if (key != "" && keys.indexOf(key) === -1) { // avoid duplicates
            keys.push(key);
            // TODO: if there is an i18nCatalogue, apply it to the key
            obj[defaults.getIdKey()] = key;
            items.push(obj);
          }
        } else { // hopefully it is an associative Array
          var key = item[valueProperty]; // TODO: do not add Java Objects, convert them first 
          if (key != "" && keys.indexOf(key) === -1) { // avoid duplicates
            obj[defaults.getIdKey()] = key;
            // TODO: if there is an i18nCatalogue, apply it to the label
            obj[defaults.getValueKey()] = "" + item[labelProperty]; // only add JS Strings // what about Numbers etc.?
            items.push(obj);
          }
        }
      }
      suggestable.list = items;
    }// TODO: Add Java Collections etc.
  } 
  return id;
};

System.Collections.getSuggestionList = function(id) {
  var defaults = System.Collections.getDefaults();
  var cache = cocoon.context.getAttribute(defaults.getSuggestableCacheKey());
  if (!cache) throw "System.Collections.getSuggestionList - Nothing has been registered as a Suggestion List";
  var suggestable = cache[id];
  if (!suggestable) {throw "System.Collections.getSuggestionList - '" + id + "' has not been registered as a Suggestion-List";}

  // private - get everything
  var _getAll = function(locale, start, count) {
    var results = [];
    if (suggestable[locale.toString()]) {
      results = suggestable[locale.toString()];
    } else { // this locale has no list yet
      if (typeof suggestable.generator === "function") {
        results = suggestable.generator(locale, suggestable.catalog, defaults.getIdKey(), defaults.getValueKey());
        suggestable[locale.toString()] = results; // cache it
      } else if (suggestable.catalog) {
        // TODO: Process the list, applying i18n catalog to the labels
        
        suggestable[locale.toString()] = results; // cache it
      } else { // the list is not localisable
        results = suggestable.list;
      }
    }
    if (start > 0 || count < results.length) {
      return results.slice(start, count === 0 ? results.length : (count > 0 ? start + count : count));
    } else {
      return results;
    }
  };
  
  // private - get suggested
  var _getSuggested = function(locale, filter, ignoreCase, start, count) {
    if (cocoon.session.getAttribute(defaults.getLastSuggestionCacheKey()) == null) cocoon.session.setAttribute(defaults.getLastSuggestionCacheKey(), {});
    var suggestions = [], lastSuggestion = cocoon.session.getAttribute(defaults.getLastSuggestionCacheKey());
    filter = "" + filter;
    if (lastSuggestion.filter === filter) {
      suggestions = lastSuggestion.suggestions;
    } else {
      filter = ignoreCase ? filter.toUpperCase() : filter; // TODO: the ignoreCase flag on the RegExp does not seem to be working in FlowScript, but is working on Browsers
      // convert dojo.data query to regex (START: copied from dojo.dijit.ComboBox v.1.1.1)
      var query = "^" + filter
          .replace(/([\\\|\(\)\[\{\^\$\+\?\.\<\>])/g, "\\$1")
          .replace("*", ".*") + "$";
      var matcher = new RegExp(query, ignoreCase ? "i" : "");
      // END: copied from dojo.dijit.ComboBox v.1.1.1
      var results = _getAll(locale);
      for (var i = 0; i < results.length; i++) {
        var name = ignoreCase ? results[i][defaults.getValueKey()].toUpperCase() : results[i][defaults.getValueKey()];
        if (name.match(query)) {
          suggestions.push(results[i]);
        }
        lastSuggestion.filter = filter
        lastSuggestion.suggestions = suggestions;
      }
		}
    if (start > 0 || count < suggestions.length) {
      return suggestions.slice(start, count === 0 ? suggestions.length : (count > 0 ? start + count : count));
    } else {    
      return suggestions;
    }
  };

  return { // SuggestionList Interface
    // public - get all suggestion-list items, displayed in this Locale
    getAll: function(locale, start, count) {
      start = start == null || isNaN(start) ? 0 : new Number(start);
      count = count == null || isNaN(count) ? Infinity : new Number(count);
      var results = _getAll(locale, start, count);
      return results;
    },
    // public - get suggestion-list items by filtering on the label
    getSuggested: function(locale, filter, ignoreCase, start, count) {
      var suggestions = [];
      start = start == null || isNaN(start) ? 0 : new Number(start);
      count = count == null || isNaN(count) ? Infinity : new Number(count);
      if (filter) {
        ignoreCase = new String(ignoreCase) === "false" ? false : true; // ignore case while searching: default true
        suggestions = _getSuggested(locale, filter, ignoreCase, start, count);
      } else {
        suggestions = _getAll(locale);
      }
      return suggestions;
    },
    // public - get a suggestion-list item by it's id 
    getById: function(locale, code) {
      if (code != null && typeof code !== "undefined" && code !== "") {
        var all = _getAll(locale);
        code = "" + code; // convert to JS String for comparison
        for ( var i = 0; i < all.length; i++ ) {
          if (all[i].value === code) return [all[i]];
        }
      }
      return []; // not matched
    }
  };
};

// A suggestion-list Generator that makes a list of Countries, for which we have Locale info (very incomplete list)
System.Collections.getCountryGenerator = function(){
  var _locales = Packages.com.ibm.icu.util.ULocale.getAvailableLocales() ;
  
  // returns a function that can output a list of countries in the language of the specified Locale
  return function(locale, catalog, idKey, valueKey) {
    locale = Packages.com.ibm.icu.util.ULocale.forLocale(locale);
    var countries = [], countryCodes = [];
    for (var i = 0; i < _locales.length; i++) {
      var l = _locales[i];
      var countryCode = "" + l.getCountry(); // NB. convert to JS Strings or toSource() won't work
      if (countryCode !== "" && countryCodes.indexOf(countryCode) === -1) { // avoid duplicates
        countryCodes.push(countryCode); // keep track of what we have done 
        var country = {}, name = "" + l.getDisplayCountry(locale);
        if (name) {
          country[idKey] = countryCode;
          country[valueKey] = name;
          countries.push(country);
        }
      }
    }
    countries.sort(System.Collections.getPropertyComparator(countries[0], valueKey));
    return countries;
  };
};

/*

  TODO:
  System.Collections.getLanguageGenerator - makes a l10n list of language-codes/language-names
  System.Collections.getCurrencyGenerator - makes a l10n list of currency-codes/currency-names
  
  day names?
  month names?
  days by month?
  time-zones?
  measurement-units?
  etc.

*/


// return a function to compare Objects by a named property - sort interface
System.Collections.getPropertyComparator = function(sample, property) {
  if (!sample || typeof sample[property] === "undefined") {
    print("System.Collections.getPropertyComparator mal-configured");
    return function(a, b){return 0;}
  }
  if (typeof sample[property] === "number") {
    return function(a, b){ 
      var aproperty = a[property], bproperty = b[property];
      if (aproperty === bproperty) return 0;
      return aproperty < bproperty ? -1 : 1;
    };
  }
  return function(a, b){ 
    var aproperty = a[property], bproperty = b[property];
    if (aproperty === bproperty) return 0;
    var sorted = [aproperty, bproperty].sort();
    return aproperty === sorted[0] ? -1 : 1;
  };
};
