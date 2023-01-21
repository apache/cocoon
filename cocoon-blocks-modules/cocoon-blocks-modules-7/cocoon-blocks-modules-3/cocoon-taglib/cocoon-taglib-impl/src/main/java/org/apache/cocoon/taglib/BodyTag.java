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
package org.apache.cocoon.taglib;

import org.xml.sax.SAXException;

/**
 * The BodyTag interface extends IterationTag.
 *
 * @version $Id$
 */
public interface BodyTag extends IterationTag {

    /**
     * Evaluate and buffer body content.
     * Valid return value for doStartTag.
     */
    int EVAL_BODY_BUFFERED = 2;

    /**
     * Invoked only when doStartTag returns EVAL_BODY_BUFFERED and tag has content
     */
    void setBodyContent(BodyContent bodyContent) throws SAXException;

    /**
     * Invoked after setBodyContent only when doStartTag returns EVAL_BODY_BUFFERED
     * and tag has content
     */
    void doInitBody() throws SAXException;
}
