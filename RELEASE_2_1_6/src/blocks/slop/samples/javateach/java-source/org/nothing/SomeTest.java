/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
// (currently lpstart comments at the very start do not work)
package org.nothing;

//lpstart:
//  <h1>What's this?</h1>
//  Based on the Slop parser, Javateach creates a nice HTML page from the source code of a Java class.
//  The idea is to write explanations of the code inline, allowing explanations and code to stay together,
//  and keeping line numbers accurate.
//
//  <h1>Teaching comments</h1>
//  Comments like this one, surrounded by lpstart/lpend will be extracted from the source
//  code to create an HTML presentation which mixes teaching comments and code.
//lpend:

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

//lpstart:
//  Here we could explain what class comments are about.
//lpend:

/** Simple example of java code parsing with Slop.
 *  The aim is to create a minimal "literate programming" system for teaching,
 *  where java code is decorated with narrative comments. */

//lpstart:
//  <h2>Here's the class declaration</h2>
//  This class does nothing useful, it does not even compile, it is only used to
//  test the javateach formatting.
//  <br/>
//  Code indentation is preserved, this is set by SlopGenerator parameters
//  in the sitemap.
//lpend:

public class SomeTest implements SlopParser,SlopConstants {
    private ContentHandler contentHandler;

    /** chars that can be part of a field name (other than letters) */
    private final static String DEFAULT_TAGNAME_CHARS = "-_";
    private String tagnameChars = DEFAULT_TAGNAME_CHARS;

//lpstart:
// lp markers have to start in column 1.
// <br/>
// HTML constructs are <b>allowed</b> in lp comments:
// <ul>
// <li>You like bullet points, I'm sure...</li>
// <li>Here's the second one</li>
// </ul>
// Links also work, like <a href="http://www.perdu.com" target="_new">this</a>.
//lpend:

    /** optionally preserve whitespace in input */
    private boolean preserveSpace = false;

    /** result of parsing a line */
    static class ParsedLine {
        final String name;
        final String contents;

        ParsedLine(String elementName, String elementContents) {
            name = elementName;
            contents = elementContents;
        }
    }

//lpstart:
//    SetValidTagname() is used to define a list of valid character for XML element
//    names.
//lpend:

    /** set the list of valid chars for tag names (in addition to letters) */
    public void setValidTagnameChars(String str) {
        tagnameChars = str;
    }

}
