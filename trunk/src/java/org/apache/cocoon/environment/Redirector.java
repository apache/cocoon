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
 * Interface for an redirector abstraction
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Id: Redirector.java,v 1.3 2004/03/08 14:02:42 cziegeler Exp $
 */

public interface Redirector {

    /**
     * Redirect to the given URL
     */
    void redirect(boolean sessionmode, String url) throws IOException, ProcessingException;
    void globalRedirect(boolean sessionmode, String url) throws IOException, ProcessingException;
    
    /**
     * Was one of the redirection methods called ?
     */
    boolean hasRedirected();
    
    /**
     * Send a content-less response with the given status code.
     * 
     * @param sc  an http status code.
     */
    void sendStatus(int sc);
}

