/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.webapps.authentication.user;

import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.webapps.authentication.configuration.ApplicationConfiguration;
import org.apache.excalibur.source.SourceResolver;


/**
 * The state of the user for the current request.
 * This object holds the information which handler and application
 * is currently used for this request.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: RequestState.java,v 1.2 2003/04/27 14:45:03 cziegeler Exp $
*/
public final class RequestState
implements java.io.Serializable {

    private static final String KEY = RequestState.class.getName();
    
    /** The handlers */
    private UserHandler handler;
        
    /** The application */
    private String application;
    
    public static RequestState getState() {
        final Map objectModel = CocoonComponentManager.getCurrentEnvironment().getObjectModel();
        return (RequestState)objectModel.get(KEY);
    }
    
    public static void setState(RequestState status) {
        final Map objectModel = CocoonComponentManager.getCurrentEnvironment().getObjectModel();
        if ( status != null ) {
            objectModel.put( KEY, status);
        } else {
            objectModel.remove( KEY );
        }
    }
    
    /**
     * Create a new handler object.
     */
    public RequestState(UserHandler handler, String app, SourceResolver resolver) 
    throws ProcessingException {
        this.handler = handler;
        this.application = app;
        if ( this.application != null && !this.handler.getApplicationsLoaded()) {
            ApplicationConfiguration conf = (ApplicationConfiguration) this.handler.getHandlerConfiguration().getApplications().get(this.application);
            if ( !this.handler.isApplicationLoaded( conf ) ) {
                this.handler.createContext().loadApplicationXML( conf, resolver );
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
}
