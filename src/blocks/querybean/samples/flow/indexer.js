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
importClass(Packages.java.net.URL);
importClass(Packages.java.io.File);
importClass(Packages.java.util.ArrayList);



// flowscripts for indexing content for the Query Bean
// $Id: query.js,v 1.3 2004/10/22 12:14:23 jeremy Exp $

function createIndex () {
	var cdir = cocoon.parameters["content-directory"]
	var rdir = cocoon.parameters["result-directory"];
	var include = cocoon.parameters["include-pattern"];
	var exclude = cocoon.parameters["exclude-pattern"];
	var rsuffix = cocoon.parameters["result-suffix"];
	var files = new ArrayList();
	try {
		var inRegExp = "undefined".equals(include) ? new RegExp(".*") : new RegExp(include);
		var exRegExp = "undefined".equals(exclude) ? null : new RegExp(exclude);
		var base = new File(new URL(resolve(cdir).getURI()).getFile());
		if (base.isDirectory()) {
			getFiles(base, files, inRegExp, exRegExp);
		} else {
			throw("error.invalid.content");
		}
		cocoon.sendPage(cocoon.parameters["screen"], 
			{
				directory: cocoon.parameters["lucene-directory"],
				analyzer: cocoon.parameters["lucene-analyzer"],
				merge: cocoon.parameters["lucene-merge-factor"],
				create: cocoon.parameters["lucene-create-index"],
				files: files,
				converter: new Converter(base, rdir, rsuffix),
				content: cocoon.parameters["lucene-content"]
			}
		);
	} catch (error) {
		cocoon.log.error(error);
		cocoon.sendPage("screen/error", {message: error});	
	}
}

/**
 * Utility function - resolve a URI to a Source
 *
 */
function resolve(uri) {
   try {
      var resolver = cocoon.getComponent(SourceResolver.ROLE);
      return resolver.resolveURI(uri);
    } catch (error) {
      cocoon.log.error("Unable to resolve source", error);
      throw (error);
    } finally {
      cocoon.releaseComponent(resolver);
   } 
}

function getFiles(dir, files, inRegExp, exRegExp) {
	try {
		var theFiles = dir.listFiles();
		for (var i = 0; i < theFiles.length; i++ ) {
			var f = theFiles[i];
			if (f.isDirectory()) {
				getFiles(f, files, inRegExp, exRegExp);
			} else if (f.isFile()) {
				if (f.canRead()) {
					var apath = f.getAbsolutePath();
					if (inRegExp.test(apath)) {
						if (exRegExp == null || !exRegExp.test(apath)) {
							files.add(apath);
						}						
					}
				}
			}
		}
	} catch (error) {
		cocoon.log.error(error);
	}
}

function Converter (base, rdir, rsuffix) {
	this._base = base.getAbsolutePath();
	this._rdir = rdir;
	this._rsuffix = rsuffix;
	if ("undefined".equals(this._rdir)) this._rdir = "";
	if ("undefined".equals(this._rsuffix)) this._rsuffix = "";
}

Converter.prototype.convert = function(file) {
	var path = file.toString();
	// remove the absolute base path
	path = path.substring(this._base.length() +1);
	// replace the suffix, if a replacement was provided
	if (!"".equals(this._rsuffix)) path = path.substring(0, path.lastIndexOf(".")) + this._rsuffix;
	// prefix with the results path
	path = this._rdir + path;
	// replace windows path delimiters with http ones
	path = path.replace( '\\', '/' );
	return path;
}