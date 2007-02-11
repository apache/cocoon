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

package org.apache.garbage.parser;

public interface ParserConstants {

  int EOF = 0;
  int XML_BaseChar = 1;
  int XML_Ideographic = 2;
  int XML_CombinChar = 3;
  int XML_Digit = 4;
  int XML_Extender = 5;
  int XML_Letter = 6;
  int XML_NameChar = 7;
  int PubIdChar = 8;
  int S = 9;
  int Name = 10;
  int T_CHARACTERS = 11;
  int T_DOCTYPE = 12;
  int T_DOCTYPE_END = 13;
  int T_DOCTYPE_NAME = 14;
  int T_DOCTYPE_SYSTEM = 15;
  int T_DOCTYPE_PUBLIC = 16;
  int T_DOCTYPE_S = 17;
  int T_DOCTYPE_QUOT = 18;
  int T_DOCTYPE_APOS = 19;
  int T_DOCTYPE_SYSTEM_DATA = 20;
  int T_DOCTYPE_PUBLIC_DATA = 21;
  int T_ELEMENT_OPEN = 22;
  int T_ELEMENT_CLOSE = 23;
  int T_ELEMENT_END = 24;
  int T_ELEMENT_SINGLE = 25;
  int T_ATTRIBUTE = 26;
  int T_ATTRIBUTE_EQUALS = 27;
  int T_ATTRIBUTE_QUOT = 28;
  int T_ATTRIBUTE_APOS = 29;
  int T_ATTRIBUTE_EXPR = 30;
  int T_ATTRIBUTE_EREF = 31;
  int T_ATTRIBUTE_DATA = 32;
  int T_ENTITYREF = 33;
  int T_ENTITYREF_END = 34;
  int T_ENTITYREF_NUM = 35;
  int T_ENTITYREF_HEX = 36;
  int T_ENTITYREF_NAME = 37;
  int T_COMMENT = 38;
  int T_COMMENT_END = 39;
  int T_COMMENT_DATA = 40;
  int T_CDATA = 41;
  int T_CDATA_END = 42;
  int T_CDATA_DATA = 43;
  int T_PROCINSTR = 44;
  int T_PROCINSTR_END = 45;
  int T_PROCINSTR_SEP = 46;
  int T_PROCINSTR_TARGET = 47;
  int T_PROCINSTR_DATA = 48;
  int T_EXPRESSION = 49;
  int T_EXPRESSION_END = 50;
  int T_EXPRESSION_DATA = 51;
  int T_TEMPLATE_IF = 52;
  int T_TEMPLATE_ELIF = 53;
  int T_TEMPLATE_ELSE = 54;
  int T_TEMPLATE_FOREACH = 55;
  int T_TEMPLATE_END = 56;
  int T_TEMPLATE_VARIABLE = 57;

  int DEFAULT = 0;
  int DOCTYPE = 1;
  int DOCTYPE_SYSTEM = 2;
  int DOCTYPE_PUBLIC = 3;
  int ELEMENT = 4;
  int ATTRIBUTE = 5;
  int ATTRIBUTE_DATA = 6;
  int ENTITYREF = 7;
  int COMMENT = 8;
  int CDATA = 9;
  int PROCINSTR = 10;
  int PROCINSTR_DATA = 11;
  int EXPRESSION = 12;

  String[] tokenImage = {
    "<EOF>",
    "<XML_BaseChar>",
    "<XML_Ideographic>",
    "<XML_CombinChar>",
    "<XML_Digit>",
    "<XML_Extender>",
    "<XML_Letter>",
    "<XML_NameChar>",
    "<PubIdChar>",
    "<S>",
    "<Name>",
    "<T_CHARACTERS>",
    "<T_DOCTYPE>",
    "<T_DOCTYPE_END>",
    "<T_DOCTYPE_NAME>",
    "<T_DOCTYPE_SYSTEM>",
    "<T_DOCTYPE_PUBLIC>",
    "<T_DOCTYPE_S>",
    "\"\\\"\"",
    "\"\\\'\"",
    "<T_DOCTYPE_SYSTEM_DATA>",
    "<T_DOCTYPE_PUBLIC_DATA>",
    "<T_ELEMENT_OPEN>",
    "<T_ELEMENT_CLOSE>",
    "<T_ELEMENT_END>",
    "<T_ELEMENT_SINGLE>",
    "<T_ATTRIBUTE>",
    "<T_ATTRIBUTE_EQUALS>",
    "\"\\\"\"",
    "\"\\\'\"",
    "\"{\"",
    "\"&\"",
    "<T_ATTRIBUTE_DATA>",
    "\"&\"",
    "\";\"",
    "<T_ENTITYREF_NUM>",
    "<T_ENTITYREF_HEX>",
    "<T_ENTITYREF_NAME>",
    "\"<!--\"",
    "\"-->\"",
    "<T_COMMENT_DATA>",
    "\"<[CDATA[\"",
    "\"]]>\"",
    "<T_CDATA_DATA>",
    "\"<?\"",
    "\"?>\"",
    "<T_PROCINSTR_SEP>",
    "<T_PROCINSTR_TARGET>",
    "<T_PROCINSTR_DATA>",
    "\"#{\"",
    "\"}\"",
    "<T_EXPRESSION_DATA>",
    "<T_TEMPLATE_IF>",
    "<T_TEMPLATE_ELIF>",
    "\"#else\"",
    "<T_TEMPLATE_FOREACH>",
    "\"#end\"",
    "<T_TEMPLATE_VARIABLE>",
  };

}
