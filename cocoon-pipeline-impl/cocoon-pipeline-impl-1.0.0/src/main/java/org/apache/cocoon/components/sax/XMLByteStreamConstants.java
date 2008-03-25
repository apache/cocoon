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
package org.apache.cocoon.components.sax;

/**
 * The common constants for SAX byte streaming
 *
 * @version $Id$
  */
public interface XMLByteStreamConstants {

    int START_DOCUMENT         = 0;
    int END_DOCUMENT           = 1;
    int START_PREFIX_MAPPING   = 2;
    int END_PREFIX_MAPPING     = 3;
    int START_ELEMENT          = 4;
    int END_ELEMENT            = 5;
    int CHARACTERS             = 6;
    int IGNORABLE_WHITESPACE   = 7;
    int PROCESSING_INSTRUCTION = 8;
    int COMMENT                = 9;
    int LOCATOR                = 10;
    int START_DTD              = 11;
    int END_DTD                = 12;
    int START_CDATA            = 13;
    int END_CDATA              = 14;
    int SKIPPED_ENTITY         = 15;
    int START_ENTITY           = 16;
    int END_ENTITY             = 17;
}
