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
package org.apache.cocoon.environment.commandline;

import org.apache.avalon.framework.logger.Logger;

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.ObjectModelHelper;

import java.io.File;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.List;

/**
 * This environment is used to save the requested file to disk.
 *
 * @version $Id$
 */
public class FileSavingEnvironment extends AbstractCommandLineEnvironment {

    protected boolean modified = true;
    protected long sourceLastModified = 0L;

    public FileSavingEnvironment(String uri,
                                 long lastModified,
                                 File context,
                                 Map attributes,
                                 Map parameters,
                                 Map headers,
                                 Map links,
                                 List gatheredLinks,
                                 CommandLineContext cliContext,
                                 OutputStream stream,
                                 Logger log)
    throws MalformedURLException {
        super(uri, null, context, stream, log);
        this.objectModel.put(ObjectModelHelper.REQUEST_OBJECT,
                             new CommandLineRequest(this, null, uri, null, attributes, parameters, headers));
        this.objectModel.put(ObjectModelHelper.RESPONSE_OBJECT,
                             new CommandLineResponse());
        this.objectModel.put(ObjectModelHelper.CONTEXT_OBJECT,
                             cliContext);
        this.sourceLastModified = lastModified;
        if (links != null) {
            this.objectModel.put(Constants.LINK_OBJECT, links);
        }
        if (gatheredLinks != null) {
            this.objectModel.put(Constants.LINK_COLLECTION_OBJECT, gatheredLinks);
        }
    }
    
    public FileSavingEnvironment(String uri,
                                 File context,
                                 Map attributes,
                                 Map parameters,
                                 Map headers,
                                 Map links,
                                 List gatheredLinks,
                                 CommandLineContext cliContext,
                                 OutputStream stream,
                                 Logger log)
    throws MalformedURLException {
        this(uri, 0L, context, attributes, parameters, headers, links, gatheredLinks, cliContext, stream, log);
    }

    /**
     * Check if the response has been modified since the same
     * "resource" was requested.
     * The caller has to test if it is really the same "resource"
     * which is requested.
     * @return true if the response is modified or if the
     *         environment is not able to test it
     */
    public boolean isResponseModified(long cacheLastModified) {
        if (cacheLastModified != 0) {
            return cacheLastModified / 1000 > sourceLastModified / 1000;
        }
        return true;
    }

    /**
     * Mark the response as not modified.
     */
    public void setResponseIsNotModified() {
       this.modified = false;
    }

    public boolean isModified() {
        return this.modified;
    }
}
