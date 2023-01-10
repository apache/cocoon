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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;

import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * Implementation of a {@link Source} that gets its content from
 * a ZIP archive.
 *
 * <p>
 * A ZIP source can be obtained using the <code>zip:</code> pseudo-protocol.
 * The syntax for protocol is
 * <pre>
 *   zip:[archive-url]!/[file-path]
 * </pre>
 *
 * Where, <code>archive-url</code> can be any supported Cocoon URL, and
 * <code>file-path</code> is the path to the file within archive.
 *
 * @version $Id$
 * @since 2.1.8
 */
public class ZipSourceFactory extends AbstractLogEnabled
                              implements SourceFactory, ThreadSafe, Serviceable {

    private ServiceManager manager;

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    public Source getSource(String location, Map parameters) throws IOException {
        // Checks URL syntax
        int protocolEnd = location.indexOf(":");
        if (protocolEnd == -1) {
            throw new MalformedURLException("Protocol ':' separator is missing in URL: " + location);
        }

        int archiveEnd = location.lastIndexOf("!/");
        if (archiveEnd == -1) {
            throw new MalformedURLException("File path '!/' separator is missing in URL: " + location);
        }

        // Get protocol. Protocol is configurable via cocoon.xconf
        final String protocol = location.substring(0, protocolEnd);

        // Get archive URL
        final String archiveURL = location.substring(protocolEnd + 1, archiveEnd);

        // Get file path
        final String filePath = location.substring(archiveEnd + 2);

        // Resolve archive source
        Source archive;
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            archive = resolver.resolveURI(archiveURL);
        } catch (ServiceException se) {
            throw new SourceException("SourceResolver is not available.", se);
        } finally {
            this.manager.release(resolver);
        }

        return new ZipSource(protocol, archive, filePath);
    }

    public void release(Source source) {
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            ((ZipSource) source).dispose(resolver);
        } catch (ServiceException e) {
            // Ignored
            getLogger().error("ServiceException while looking up SourceResolver in release()", e);
        } finally {
            this.manager.release(resolver);
        }
    }
}
