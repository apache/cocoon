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
package org.apache.cocoon.caching;

import java.io.Serializable;

/**
 * A CacheValidity object contains all information for one pipeline component
 * to check if it is still valid.<br>
 * For example, the FileGenerator stores only the timestamp for the read
 * xml file in this container.
 * Although this interface is deprecated it is still used for compatibility!
 *
 * @deprecated Use the Avalon Excalibur SourceValidity implementations instead
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CacheValidity.java,v 1.2 2004/03/05 13:02:45 bdelacretaz Exp $
 */
public interface CacheValidity extends Serializable {

    /**
     * Check if the component is still valid.
     * This is only true, if the incoming CacheValidity is of the same
     * type and has the same values.
     */
    boolean isValid(CacheValidity validity);

    /**
     * Creates text represenation of the validity object.
     * This is used to create fake 'lastModificationDate' for cocoon: sources.
     * <p>Due to changes in source API, this method is no longer needed,
     * starting with Cocoon 2.1.
     */
    String toString();
}
