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
package org.apache.cocoon.components.serializers.encoding;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: Charset.java,v 1.1 2004/04/21 09:33:22 pier Exp $
 */
public interface Charset extends Verifier {

    /**
     * Return the primary name of this <code>Charset</code>
     */
    public String getName();

    /**
     * Return all alias names for this <code>Charset</code>
     */
    public String[] getAliases();

    /**
     * Compare two <code>Charset</code> instances for equality.
     */
    public boolean equals(Charset charset);
}
