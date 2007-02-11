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

importClass(Packages.org.apache.excalibur.source.SourceResolver);
importClass(Packages.org.apache.excalibur.source.TraversableSource);
importClass(Packages.java.util.ArrayList);



// flowscripts for indexing content for the Query Bean
// $Id: query.js,v 1.3 2004/10/22 12:14:23 jeremy Exp $

function indexItem() {
	var cdir = cocoon.parameters["content-directory"]
	var target = cocoon.parameters["indexer-target"]
	var rdir = cocoon.parameters["result-directory"];
	var rsuffix = cocoon.parameters["result-suffix"];
	var files = new ArrayList();
	var resolver = cocoon.getComponent(SourceResolver.ROLE);
	var base;
	var source;
	try {
		base = resolver.resolveURI(cdir);
		if (!base.isCollection()) throw ("error.invalid.content");
		source = resolver.resolveURI(base.getURI() + target);
		if (source.isCollection()) {
			throw ("error.invalid.content");
		} else {
			files.add(source.getURI());
			cocoon.log.error("reindexing: " + source.getURI());
		}
		cocoon.sendPage(cocoon.parameters["screen"], 
			{
				directory: cocoon.parameters["lucene-directory"],
				analyzer: cocoon.parameters["lucene-analyzer"],
				merge: cocoon.parameters["lucene-merge-factor"],
				create: cocoon.parameters["lucene-create-index"],
				files: files,
				converter: new Converter(base.getURI(), rdir, rsuffix),
				content: cocoon.parameters["lucene-content"]
			}
		);
	} catch (error) {
		cocoon.log.error(error);
		cocoon.sendPage("screen/error", {message: error});	
	} finally {
		resolver.release(base);
		resolver.release(source);
		cocoon.releaseComponent(resolver);
	}

}

function indexCollection () {
	var cdir = cocoon.parameters["content-directory"]
	var rdir = cocoon.parameters["result-directory"];
	var include = cocoon.parameters["include-pattern"];
	var exclude = cocoon.parameters["exclude-pattern"];
	var rsuffix = cocoon.parameters["result-suffix"];
	var files = new ArrayList();
	var resolver = cocoon.getComponent(SourceResolver.ROLE);
	var source;
	try {
		var inRegExp = "undefined".equals(include) || "".equals(include) ? new RegExp(".*") : new RegExp(include);
		var exRegExp = "undefined".equals(exclude) || "".equals(exclude) ? null : new RegExp(exclude);
		source = resolver.resolveURI(cdir);
		if (source instanceof TraversableSource && source.isCollection()) {
			getFiles(source, files, inRegExp, exRegExp);
		} else {
			throw ("error.invalid.content");
		}
		cocoon.sendPage(cocoon.parameters["screen"], 
			{
				directory: cocoon.parameters["lucene-directory"],
				analyzer: cocoon.parameters["lucene-analyzer"],
				merge: cocoon.parameters["lucene-merge-factor"],
				create: cocoon.parameters["lucene-create-index"],
				files: files,
				converter: new Converter(source.getURI(), rdir, rsuffix),
				content: cocoon.parameters["lucene-content"]
			}
		);
	} catch (error) {
		cocoon.log.error(error);
		cocoon.sendPage("screen/error", {message: error});	
	} finally {
		resolver.release(source);
		cocoon.releaseComponent(resolver);
	}
}

function getFiles(dir, files, inRegExp, exRegExp) {
	try {
		var theFiles = dir.getChildren();
		for (var i = 0; i < theFiles.size(); i++ ) {
			var f = theFiles.get(i);
			if (f.isCollection()) {
				getFiles(f, files, inRegExp, exRegExp);
			} else {
				var apath = f.getURI();
				if (inRegExp.test(apath)) {
					if (exRegExp == null || !exRegExp.test(apath)) {
						files.add(apath);
					}						
				}
			}
		}
	} catch (error) {
		cocoon.log.error(error);
	}
}

function Converter (base, rdir, rsuffix) {
	this._base = base;
	this._rdir = rdir;
	this._rsuffix = rsuffix;
	if ("undefined".equals(this._rdir)) this._rdir = "";
	if ("undefined".equals(this._rsuffix)) this._rsuffix = "";
}

Converter.prototype.convert = function(file) {
	var path = file.toString();
	// remove the absolute base path
	path = path.substring(this._base.length());
	// replace the suffix, if a replacement was provided
	if (!"".equals(this._rsuffix)) path = path.substring(0, path.lastIndexOf(".")) + this._rsuffix;
	// prefix with the results path
	path = this._rdir + path;
	// replace windows path delimiters with http ones
	path = path.replace( '\\', '/' );
	return path;
}