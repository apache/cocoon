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
package org.apache.cocoon.environment.mock;

import org.apache.cocoon.ProcessingException;
import java.io.IOException;

import org.apache.cocoon.environment.Redirector;

public class MockRedirector implements Redirector {

    protected boolean hasRedirected = false;

    private String redirect;

    public void redirect(boolean sessionmode, String url) throws IOException, ProcessingException {
        this.hasRedirected = true;

        redirect = url;
    }
  
    public void globalRedirect(boolean sessionmode, String url) throws IOException, ProcessingException {
        redirect(sessionmode, url);
    }

    public String getRedirect() {
        return redirect;
    }
    
    public boolean hasRedirected() {
        return this.hasRedirected;
    }

    public void sendStatus(int sc) {
        this.hasRedirected = true;
    }

    public void reset() {
        redirect = null;
        hasRedirected = false;
    }
}

