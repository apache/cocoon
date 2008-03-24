/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.excalibur.sourceresolve.jnet.source;

import java.net.URLStreamHandler;
import java.util.Map;

import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.sourceresolve.jnet.ParentAwareURLStreamHandlerFactory;

public class SourceURLStreamHandlerFactory
    extends ParentAwareURLStreamHandlerFactory {

    /**
     * @see org.apache.excalibur.sourceresolve.jnet.ParentAwareURLStreamHandlerFactory#create(java.lang.String)
     */
    protected URLStreamHandler create(String protocol) {
        final Map factories = SourceFactoriesManager.getCurrentFactories();
        final SourceFactory factory = (SourceFactory)factories.get(protocol);
        if ( factory != null ) {
            return new SourceURLStreamHandler(factory);
        }
        return null;
    }

}
