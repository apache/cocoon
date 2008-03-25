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
 * The XMLPipe is both an XMLProducer and an XMLConsumer.  All the Transformers
 * implement this interface for example.  By having an XMLPipe interface, we
 * can chain more than one pipeline component together.  What this means is
 * that Cocoon will honor all the XMLProducer contracts in a pipeline first.
 * The SAX pipeline will be completely assembled before any SAX calls are
 * issued.  Cocoon does not want any stray calls to get lost.  There can be
 * zero or more XMLPipes in a pipeline, but there must always be at least one
 * XMLProducer and XMLConsumer pair.
 * <p>
 * Because an XMLPipe is both a source and a sink for SAX events, the basic
 * contract that you need to worry about is that you must forward any SAX
 * events on that you are not intercepting and transforming.  As you receive
 * your startDocument event, pass it on to the XMLConsumer you received as part
 * of the XMLProducer side of the contract.  An example ASCII art will help
 * make it a bit more clear:
 * </p>
 * <pre>
 * XMLProducer -&gt; (XMLConsumer)XMLPipe(XMLProducer) -&gt; XMLConsumer
 * </pre>
 * <p>
 * A typical example would be using the FileGenerator (an XMLProducer), sending
 * events to an XSLTTransformer (an XMLPipe), which then sends events to an
 * HTMLSerializer (an XMLConsumer).  The XSLTTransformer acts as an XMLConsumer
 * to the FileGenerator, and also acts as an XMLProducer to the HTMLSerializer.
 * It is still the responsibility of the XMLPipe component to ensure that the
 * XML passed on to the next component is valid--provided the XML received from
 * the previous component is valid.  In layman's terms it means if you don't
 * intend to alter the input, just pass it on.  In most cases we just want to
 * transform a small snippet of XML.  For example, inserting a snippet of XML
 * based on an embedded element in a certain namespace.  Anything that doesn't
 * belong to the namespace you are worried about should be passed on as is.
 * </p>
 *
 * @version $Id$
 */
public interface XMLPipe extends XMLConsumer, XMLProducer {}
