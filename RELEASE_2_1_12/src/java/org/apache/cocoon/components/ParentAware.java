/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * @version CVS $Id$
 */
public interface ParentAware
    extends ThreadSafe {

    /**
     * Set the parent component manager and the role name
     */
    void setParentLocator(ComponentLocator locator)
    throws ComponentException;
}
