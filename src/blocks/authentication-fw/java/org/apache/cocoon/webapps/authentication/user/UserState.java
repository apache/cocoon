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

import java.util.HashMap;
import java.util.Map;

/**
 * The state of the user.
 * This object holds all authentication handlers ({@link UserHandler}
 * the user is currently logged-in to.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: UserState.java,v 1.2 2004/03/05 13:01:41 bdelacretaz Exp $
*/
public final class UserState
implements java.io.Serializable {

    /** The handlers */
    private Map handlers = new HashMap(7);

   /**
     * Create a new handler object.
     */
    public UserState() {
    }

    public void addHandler(UserHandler value) {
        this.handlers.put(value.getHandlerName(), value);
    }

    public void removeHandler(String name) {
        this.handlers.remove( name );
    }
    
    public UserHandler getHandler(String name) {
        return (UserHandler) this.handlers.get( name );
    }
    
    public boolean hasHandler() {
        return (this.handlers.size() > 0);
    }
}
