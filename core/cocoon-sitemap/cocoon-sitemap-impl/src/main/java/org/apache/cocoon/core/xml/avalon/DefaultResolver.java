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
package org.apache.cocoon.core.xml.avalon;

import java.io.IOException;

import org.apache.excalibur.xml.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * A wrapper implementation to support the Excalibur XML interfaces.
 * 
 * @version $Id$
 * @since 2.2
 */
public class DefaultResolver
    implements EntityResolver {

    protected org.xml.sax.EntityResolver resolver;

    public void setResolver(org.xml.sax.EntityResolver resolver) {
        this.resolver = resolver;
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        return this.resolver.resolveEntity(publicId, systemId);
    }
}
