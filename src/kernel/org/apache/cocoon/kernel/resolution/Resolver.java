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
package org.apache.cocoon.kernel.resolution;

/**
 * <p>The {@link Resolver} interface defines a component able to resolve
 * names into {@link Resource}s.</p>
 *
 * <p>This class does not specify how a specific {@link Resource} is associated
 * with a name; it is left to extensions and implementations of this interface
 * to outline this contract.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.3 $)
 */
public interface Resolver {

    /**
     * <p>Resolve a specified name into a {@link Resource}.</p>
     *
     * @param name a non null {@link String} identifying the resource name.
     * @return a {@link Resource} instance or <b>null</b> if not found.
     */
    public Resource resolve(String name);

}
