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
package org.apache.cocoon.environment;

import org.apache.cocoon.ProcessingException;
import java.io.IOException;

/**
 * Interface for a permanent redirector abstraction
 *
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: PermanentRedirector.java,v 1.2 2004/03/05 13:02:54 bdelacretaz Exp $
 */

public interface PermanentRedirector {

    /**
     * Redirect to the given URL
     */
    void permanentRedirect(boolean sessionmode, String url) throws IOException, ProcessingException;
 }

