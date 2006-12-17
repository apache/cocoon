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
 * This interfaces identifies classes that produce XML data, sending SAX
 * events to the configured <code>XMLConsumer</code>.
 * <p>
 * The XMLProducer is comprised of only one method to give the component the
 * next element of the pipeline.  Cocoon calls the <code>setConsumer()</code>
 * method with the reference to the next XMLConsumer in the pipeline.  The
 * approach allows the XMLProducer to call the different SAX related methods on
 * the XMLConsumer without knowing ahead of time what that consumer will be.
 * The design is very simple and very powerful in that it allows Cocoon to
 * daisy chain several components in any order and then execute the pipeline.
 * </p>
 * <p>
 * Any producer can be paired with any consumer and we have a pipeline.  The
 * core design is very powerful and allows the end user to mix and match
 * sitemap components as they see fit.  Cocoon will always call
 * <code>setConsumer()</code> on every XMLProducer in a pipeline or it will
 * throw an exception saying that the pipeline is invalid (i.e. there is no
 * serializer for the pipeline).  The only contract that the XMLProducer has to
 * worry about is that it must always make calls to the XMLConsumer passed in
 * through the <code>setConsumer()</code> method.
 * </p>
 *
 * @version $Id$
 */
public interface XMLProducer {

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     *
     * @param consumer  The XMLConsumer target for SAX events.
     */
    void setConsumer(XMLConsumer consumer);
}
