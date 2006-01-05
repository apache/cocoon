/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.xpointer.parser;

public interface XPointerFrameworkParserConstants {

  int EOF = 0;
  int Letter = 1;
  int BaseChar = 2;
  int Ideographic = 3;
  int CombiningChar = 4;
  int UnicodeDigit = 5;
  int Extender = 6;
  int NCName = 7;
  int WS = 8;
  int QName = 9;
  int LBRACE = 10;
  int RBRACE = 11;
  int CIRC_LBRACE = 12;
  int CIRC_RBRACE = 13;
  int DOUBLE_CIRC = 14;
  int NormalChar = 15;

  int DEFAULT = 0;
  int IN_SCHEME = 1;

  String[] tokenImage = {
    "<EOF>",
    "<Letter>",
    "<BaseChar>",
    "<Ideographic>",
    "<CombiningChar>",
    "<UnicodeDigit>",
    "<Extender>",
    "<NCName>",
    "<WS>",
    "<QName>",
    "\"(\"",
    "\")\"",
    "\"^(\"",
    "\"^)\"",
    "\"^^\"",
    "<NormalChar>",
  };

}
