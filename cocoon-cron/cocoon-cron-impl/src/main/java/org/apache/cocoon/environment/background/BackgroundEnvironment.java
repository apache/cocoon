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
package org.apache.cocoon.environment.background;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.environment.AbstractEnvironment;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.commandline.CommandLineRequest;
import org.apache.cocoon.environment.commandline.CommandLineResponse;
import org.apache.cocoon.util.NullOutputStream;

/**
 * A simple implementation of <code>org.apache.cocoon.environment.Environment</code>
 * for pipeline calls which are not externally triggered.
 *
 * @version $Id$
 *
 * @since 2.1.4
 */
public class BackgroundEnvironment extends AbstractEnvironment {

    public BackgroundEnvironment(Logger logger, Context ctx) {
        super("", null, null);
        enableLogging(logger);
        this.outputStream = new NullOutputStream();

        // TODO Would special Background*-objects have advantages?
        Request request = new CommandLineRequest(
                this,                  // environment
                "",                    // context path
                "",                    // servlet path
                "",                    // path info
                new HashMap(),         // attributes
                Collections.EMPTY_MAP, // parameters
                Collections.EMPTY_MAP  // headers
        );
        this.objectModel.put(ObjectModelHelper.REQUEST_OBJECT, request);
        this.objectModel.put(ObjectModelHelper.RESPONSE_OBJECT,
                             new CommandLineResponse());
        this.objectModel.put(ObjectModelHelper.CONTEXT_OBJECT, ctx);
    }

    /**
     * @see org.apache.cocoon.environment.Environment#redirect(java.lang.String, boolean, boolean)
     */
    public void redirect(String newURL, boolean global, boolean permanent) throws IOException {

    }

    /**
     * @see org.apache.cocoon.environment.Environment#setContentType(java.lang.String)
     */
    public void setContentType(String mimeType) {
    }

    /**
     * @see org.apache.cocoon.environment.Environment#getContentType()
     */
    public String getContentType() {
        return null;
    }

    /**
     * @see org.apache.cocoon.environment.Environment#setContentLength(int)
     */
    public void setContentLength(int length) {
    }

    /**
     * Always return false
     *
     * @see org.apache.cocoon.environment.Environment#isExternal()
     */
    public boolean isExternal() {
        return false;
    }
}
