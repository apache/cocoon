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
package org.apache.cocoon.kernel.archival;

import java.util.Set;
import org.apache.cocoon.kernel.identification.Descriptor;
import org.apache.cocoon.kernel.identification.Identifier;

/**
 * <p>The {@link Library} interface defines an object storing
 * {@link Descriptor}s.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.2 $)
 */
public interface Library extends Set {

    /**
     * <p>Archive a new {@link Descriptor} in this {@link Library}.</p>
     *
     * <p>The returned {@link Descriptor} might not be the one specified,
     * as the archiving operation might cache {@link Descriptor} instances
     * and modify them for the local deployment environment (for example,
     * modifying library paths from the original URLs to local files).</p>
     *
     * @param descriptor the {@link Descriptor} to archive.
     * @return a {@link Descriptor} instance.
     * @throws UnsupportedOperationException if this {@link Library} is
     *                                       immutable.
     */
    public Descriptor add(Descriptor descriptor);
    
    /**
     * <p>Retrieve a previously archived {@link Descriptor}.</p>
     *
     * @param identifier the {@link Identifier} of the {@link Descriptor}.
     * @return a {@link Descriptor} instance or <b>null</b> if not found.
     */
    public Descriptor get(Identifier identifier);

    /**
     * <p>Retrieve a previously archived {@link Descriptor}.</p>
     *
     * @param identifier the {@link String} id of the {@link Descriptor}.
     * @return a {@link Descriptor} instance or <b>null</b> if not found.
     */
    public Descriptor get(String identifier);
}
