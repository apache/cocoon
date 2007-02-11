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
package org.apache.cocoon.components.persistence;


/**
 * A request data store is a component that manages data that is
 * linked to the current request.
 * With the setRequestData() method you can link any object to the
 * current request. This object can be fetched via getRequestData()
 * as long as the request is running. This data is not available
 * in any sub-request (cocoon: protocol calls).
 * If you want to share data between the main request and any sub-request
 * than you have to use the setGlobalRequestData etc. methods.
 * 
 * This component is a replacement for the request lifecycle and
 * global request lifecycle components.
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: RequestDataStore.java,v 1.3 2004/03/05 13:02:50 bdelacretaz Exp $
 * @since 2.1.1
 */
public interface RequestDataStore {
        
    String ROLE = RequestDataStore.class.getName();
    
    Object getRequestData(String key);

    void removeRequestData(String key);

    void setRequestData(String key, Object value);

    Object getGlobalRequestData(String key);

    void removeGlobalRequestData(String key);

    void setGlobalRequestData(String key, Object value);
}
