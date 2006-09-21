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

import org.xml.sax.Attributes;

/**
 * Immutable attributes
 *
 * @version $Id$
 */
public class ImmutableAttributesImpl extends AttributesImpl {

    public ImmutableAttributesImpl() {
        super();
    }

    public ImmutableAttributesImpl(Attributes attrs) {
        super(attrs);
    }

    public void clear() {
        throw new UnsupportedOperationException("immutable attributes");
    }

    public void removeAttribute(int index) {
        throw new UnsupportedOperationException("immutable attributes");
    }

    public void setLocalName(int index, String localName) {
        throw new UnsupportedOperationException("immutable attributes");
    }

    public void setQName(int index, String qName) {
        throw new UnsupportedOperationException("immutable attributes");
    }

    public void setType(int index, String type) {
        throw new UnsupportedOperationException("immutable attributes");
    }

    public void setURI(int index, String uri) {
        throw new UnsupportedOperationException("immutable attributes");
    }

    public void setValue(int index, String value) {
        throw new UnsupportedOperationException("immutable attributes");
    }

    public void setAttributes(Attributes atts) {
        throw new UnsupportedOperationException("immutable attributes");
    }

    public void setAttribute(int index, String uri, String localName, String qName, String type, String value) {
        throw new UnsupportedOperationException("immutable attributes");
    }

    public void addAttribute(String uri, String localName, String qName, String type, String value) {
        throw new UnsupportedOperationException("immutable attributes");
    }
}
