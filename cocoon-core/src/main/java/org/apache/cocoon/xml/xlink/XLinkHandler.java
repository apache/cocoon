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
package org.apache.cocoon.xml.xlink;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This interface indicates an XLinkHandler that uses the same
 * event driven design patterns that SAX enforces.
 *
 * @version $Id$
 */
public interface XLinkHandler  {

    void simpleLink(String href, String role, String arcrole, String title, String show, String actuate, String uri, String name, String raw, Attributes attr) throws SAXException;

    void startExtendedLink(String role, String title, String uri, String name, String raw, Attributes attr) throws SAXException;

    void endExtendedLink(String uri, String name, String raw) throws SAXException;

    void startLocator(String href, String role, String title, String label, String uri, String name, String raw, Attributes attr) throws SAXException;

    void endLocator(String uri, String name, String raw) throws SAXException;

    void startArc(String arcrole, String title, String show, String actuate, String from, String to, String uri, String name, String raw, Attributes attr) throws SAXException;

    void endArc(String uri, String name, String raw) throws SAXException;

    void linkResource(String role, String title, String label, String uri, String name, String raw, Attributes attr) throws SAXException;

    void linkTitle(String uri, String name, String raw, Attributes attr) throws SAXException;

}

