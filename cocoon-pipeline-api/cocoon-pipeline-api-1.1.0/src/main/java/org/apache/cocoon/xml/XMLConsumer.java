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
package org.apache.cocoon.xml;

/**
 * This interfaces identifies classes that consume XML data, receiving
 * notification of SAX events.
 * <p>
 * An XMLConsumer is also a SAX ContentHandler and a SAX LexicalHandler.  That
 * means the XMLConsumer has to respect all the contracts with the SAX
 * interfaces.  SAX stands for Serialized API for XML.  A document start, and
 * each element start must be matched by the corresponding element end or
 * document end.  So why does Cocoon use SAX instead of manipulating a DOM?
 * For two main reasons: performance and scalability.  A DOM tree is much more
 * heavy on system memory than successive calls to an API.  SAX events can be
 * sent as soon as they are read from the originating XML, the parsing and
 * processing can happen essentially at the same time.
 * </p>
 * <p>
 * Most people's needs will be handled just fine with the ContentHandler
 * interface, as that declares your namespaces.  However if you need lexical
 * support to resolve entity names and such, you need the LexicalHandler
 * interface.  The AbstractXMLConsumer base class can make implementing this
 * interface easier so that you only need to override the events you intend to
 * do anything with.
 * </p>
 *
 * @version $Id$
 */
public interface XMLConsumer extends org.apache.excalibur.xml.sax.XMLConsumer {
}
