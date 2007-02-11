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
package org.apache.cocoon.sitemap;

import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.wrapper.EnvironmentWrapper;

import java.io.IOException;

/**
 * Wrapper for sitemap redirection
 *
 * @deprecated This class has been used by the old sitemap engine
 *  
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SitemapRedirector.java,v 1.6 2004/03/05 13:02:58 bdelacretaz Exp $
 */
public class SitemapRedirector implements Redirector {
    private boolean hasRedirected = false;
    private Environment e;

    /**
     * Constructor with environment--so redirection happens as expected
     */
    public SitemapRedirector(Environment e) {
        this.e = e;
    }

    /**
     * Perform actual redirection
     */
    public void redirect(boolean sessionMode, String url) throws IOException {
        e.redirect(sessionMode, url);
        this.hasRedirected = true;
    }
    
    public void globalRedirect(boolean sessionMode, String url) throws IOException {
        if (e instanceof EnvironmentWrapper) {
            ((EnvironmentWrapper)e).globalRedirect(sessionMode,url);
        } else {
            e.redirect(sessionMode, url);
        }
        this.hasRedirected = true;
    }

    public void sendStatus(int sc) {
        e.setStatus(sc);
        this.hasRedirected = true;
    }

    /**
     * Perform check on whether redirection has occured or not
     */
    public boolean hasRedirected() {
        return this.hasRedirected;
    }
}
