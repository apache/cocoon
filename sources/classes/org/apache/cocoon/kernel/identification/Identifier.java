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

/**
 * <p>The {@link Identifier} interface represents an unique versioned ID as
 * required by this framework.</p>
 *
 * <p>Each identifier <b>always</b> assumes the form of an {@link URL}, and to
 * be properly versioned, the URL must follow this structure:</p>
 *
 * <p><code>protocol://location/path/major.minor(.revision)?</code></p>
 *
 * <p>The <code>protocol://location/path/</code> part is called also
 * <b>base</b> URL of the identifier, therefore the full specified URL without
 * any versioning information.</p>
 *
 * <p>The remaining <code>major.minor(.revision)?</code>part is the version
 * specifier and is divided in three parts: a major and minor version numbers,
 * and an optional revision number all separated by &quot;.&quot; (dot).</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.3 $)
 */
public interface Identifier {

    /**
     * <p>Return a human-readable representation of the version number.</p>
     *
     * @return a <b>non null</b> {@link String}.
     */
    public String version();    

    /**
     * <p>Return the major version number.</p>
     *
     * @return a <b>non negative</b> number.
     */
    public int major();

    /**
     * <p>Return the minor version number.</p>
     *
     * @return a <b>non negative</b> number.
     */
    public int minor();

    /**
     * <p>Return the revision number.</p>
     *
     * @return a <b>non negative</b> number or -1 if this {@link Identifier}
     *         does not specify a revision number.
     */
    public int revision();

    /**
     * <p>Return the base URL of this identifier as an {@link URL}.</p>
     *
     * @return a <b>non null</b> {@link URL}.
     */
    public URL base();

    /**
     * <p>Return the full URL of this identifier as an {@link URL}.</p>
     *
     * @return a <b>non null</b> {@link URL}.
     */
    public URL toURL();

    /**
     * <p>Return a hashed {@link String} representation of this
     * {@link Identifier}.</p>
     *
     * @return a <b>non null</b> {@link String}.
     */
    public String hash();
}
