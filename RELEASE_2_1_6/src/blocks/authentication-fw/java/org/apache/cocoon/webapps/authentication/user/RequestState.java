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
package org.apache.cocoon.webapps.authentication.user;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.webapps.authentication.configuration.ApplicationConfiguration;
import org.apache.excalibur.source.SourceResolver;


/**
 * The state of the user for the current request.
 * This object holds the information which handler and application
 * is currently used for this request.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: RequestState.java,v 1.7 2004/03/19 13:59:21 cziegeler Exp $
*/
public final class RequestState
implements java.io.Serializable {

    /** The handlers */
    private UserHandler handler;
        
    /** The application */
    private String application;
    
    /**
     * Create a new handler object.
     */
    public RequestState(UserHandler handler, String app) {
        this.handler = handler;
        this.application = app;
    }
    
    /**
     * Initialize
     */
    public void initialize(SourceResolver resolver)
    throws ProcessingException {
        if ( this.application != null && !this.handler.getApplicationsLoaded()) {
            ApplicationConfiguration conf = (ApplicationConfiguration) this.handler.getHandlerConfiguration().getApplications().get(this.application);
            if ( !this.handler.isApplicationLoaded( conf ) ) {
                this.handler.getContext().loadApplicationXML( conf, resolver );
            }
        }
    }

    public String getApplicationName() {
        return this.application;
    }
    
    public UserHandler getHandler() {
        return this.handler;
    }
    
    public String getHandlerName() {
        return this.handler.getHandlerName();
    }
    
    public ApplicationConfiguration getApplicationConfiguration() {
        if ( this.application != null ) {
            return (ApplicationConfiguration)this.handler.getHandlerConfiguration().getApplications().get(this.application);
        }
        return null;
    }
    
    /**
     * Get the configuration if available
     */
    public Configuration getModuleConfiguration(String name) {
        Configuration conf = null;

        if (this.handler != null && this.application != null) {
            conf = this.getApplicationConfiguration().getConfiguration(name);
        }
        if (this.handler != null && conf == null) {
            conf = this.handler.getHandlerConfiguration().getConfiguration(name);
        }

        return conf;
    }    
}
