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
package org.apache.cocoon.components;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * Components acception this marker interface indicate that they want
 * to have a reference to their parent.
 * This is for example used for selectors.
 * Note: For the current implementation to work, the parent aware 
 * component and the parent have to be both ThreadSafe!
 * @deprecated
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ParentAware.java,v 1.3 2004/03/05 13:02:45 bdelacretaz Exp $
 */
public interface ParentAware
    extends ThreadSafe {

    /**
     * Set the parent component manager and the role name
     */
    void setParentLocator(ComponentLocator locator)
    throws ComponentException;
}
