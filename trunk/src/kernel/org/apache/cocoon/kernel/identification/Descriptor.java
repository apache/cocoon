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
package org.apache.cocoon.kernel.identification;

import java.net.URL;
import java.util.Iterator;

/**
 * <p>The {@link Descriptor} iterface extends the concept of a block
 * {@link Identifier} adding functionalities to represent a generic block
 * descriptor.</p>
 *
 * <p>Extensions or implementations of this interface provide the actual
 * &quot;functional&quot; specification of the block.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.3 $)
 */
public interface Descriptor extends Identifier {

    /** <p>The XML namespace of all block {@link Descriptor}s.</p> */
    public static final String NAMESPACE = 
                                "http://www.vnunet.com/blocks/descriptor/1.0";

    /** <p>Identifies a library with &quot;<b>private</b>&quot; access.</p> */
    public static final int ACCESS_PRIVATE = 0;
    
    /** <p>Identifies a library with &quot;<b>protected</b>&quot; access.</p> */
    public static final int ACCESS_PROTECTED = 1;
    
    /** <p>Identifies a library with &quot;<b>public</b>&quot; access.</p> */
    public static final int ACCESS_PUBLIC = 2;
    
    /**
     * <p>Return the unique {@link Identifier} of the block this descriptor
     * is claiming to extend.</p>
     *
     * @return an {@link Identifier} or <b>null</b> if the block described by
     *         this {@link Descriptor} doesn't claim to implement any other
     *         block.
     */
    public Identifier extendedBlock();

    /**
     * <p>Return an iterator over all {@link Identifier}s of the blocks this
     * descriptor is claiming to implement.</p>
     *
     * <p>Note that only non-interface blocks can implement interface blocks,
     * and only interface blocks can be implemented.</p>
     *
     * @return an {@link Iterator} over {@link Identifier} instances.
     */
    public Iterator implementedBlocks();

    /**
     * <p>Return an iterator over all {@link Identifier}s of the blocks this
     * descriptor is claiming to require.</p>
     *
     * <p>Note that only non-interface blocks can require external blocks.</p>
     *
     * @return an {@link Iterator} over {@link Identifier} instances.
     */
    public Iterator requiredBlocks();
    
    /**
     * <p>Return an array of {@link URL}s of all libraries declared in this
     * {@link Descriptor} having the specified access level.</p>
     *
     * @param access the access level of the libraries to return (one of
     *               {@link #ACCESS_PRIVATE}, {@link #ACCESS_PROTECTED} or
     *               {@link #ACCESS_PUBLIC}).
     * @return a <b>non null</b> {@link URL} array.
     */
    public URL[] libraries(int access);

    /**
     * <p>Check whether this {@link Descriptor} describes an interface
     * block or not.</p>
     *
     * @return <b>true</b> if this {@link Descriptor} represents an interface
     *         block, <b>false</b> otherwise.
     */
    public boolean isInterface();
}
