/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.environment.commandline;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.ObjectModelHelper;

/**
 * This environment is sample the links of the resource.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: LinkSamplingEnvironment.java,v 1.7 2004/03/05 13:02:54 bdelacretaz Exp $
 */

public class LinkSamplingEnvironment extends AbstractCommandLineEnvironment {

    private boolean skip = false;

    public LinkSamplingEnvironment(String uri,
                                   File contextFile,
                                   Map attributes,
                                   Map parameters,
                                   CommandLineContext cliContext,
                                   Logger log)
    throws MalformedURLException, IOException {
        super(uri, Constants.LINK_VIEW, contextFile, new ByteArrayOutputStream(), log);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("uri = " + uri);
        }
        this.objectModel.put(ObjectModelHelper.REQUEST_OBJECT,
                             new CommandLineRequest(this, null, uri, null, attributes, parameters));
        this.objectModel.put(ObjectModelHelper.RESPONSE_OBJECT,
                             new CommandLineResponse());
        this.objectModel.put(ObjectModelHelper.CONTEXT_OBJECT,
                             cliContext);
    }

    /**
     * Set the ContentType
     */
    public void setContentType(String contentType) {
        if (!Constants.LINK_CONTENT_TYPE.equals(contentType)) {
            this.skip = true;
        }
    }

    /**
     * Indicates if other links are present.
     */
    public Collection getLinks() throws IOException {
        HashSet set = new HashSet();
        if (!skip) {
            BufferedReader buffer = null;
            try {
                buffer = new BufferedReader(
                        new InputStreamReader(
                                new ByteArrayInputStream(
                                        ((ByteArrayOutputStream) super.outputStream).toByteArray())));

                String line;
                while ((line = buffer.readLine()) !=null) {
                    set.add(line);
                }
            } finally {
                // explictly close the input
                if (buffer != null) {
                    try {
                        buffer.close();
                        buffer = null;
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        return set;
    }
}
