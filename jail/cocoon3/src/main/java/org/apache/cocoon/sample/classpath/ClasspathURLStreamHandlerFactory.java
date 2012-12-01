/*
 * Copyright (C) 2012 Tirasa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.sample.classpath;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class ClasspathURLStreamHandlerFactory implements URLStreamHandlerFactory {

    /**
     * {@inheritDoc}
     *
     * @see java.net.URLStreamHandlerFactory#createURLStreamHandler(java.lang.String)
     */
    @Override
    public URLStreamHandler createURLStreamHandler(final String protocol) {
        return "classpath".equalsIgnoreCase(protocol)
                ? new ClasspathURLStreamHandler()
                : null;
    }

    public class ClasspathURLStreamHandler extends URLStreamHandler {

        @Override
        protected URLConnection openConnection(final URL url)
                throws IOException {

            final URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(url.getPath());
            if (resourceUrl == null) {
                throw new FileNotFoundException(url.toExternalForm());
            }

            return resourceUrl.openConnection();
        }
    }
}
