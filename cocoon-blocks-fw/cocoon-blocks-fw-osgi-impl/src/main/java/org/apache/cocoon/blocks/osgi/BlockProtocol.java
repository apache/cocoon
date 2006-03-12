/*
 * Copyright 1999-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.blocks.osgi;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.osgi.service.log.LogService;
import org.osgi.service.url.AbstractURLStreamHandlerService;

/**
 * Factory for block protocol
 * 
 * @version $Id$
 */
public class BlockProtocol extends AbstractURLStreamHandlerService {

    private LogService log;
    
    protected void setLog(LogService log) {
        this.log = log;
    }
    /* (non-Javadoc)
     * @see org.osgi.service.url.AbstractURLStreamHandlerService#openConnection(java.net.URL)
     */
    public URLConnection openConnection(URL url) throws IOException {
        return new BlockConnection(url, this.log);
    }

}
