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
package org.apache.cocoon.components.cprocessor.sitemap;

import org.apache.cocoon.components.cprocessor.NamedNode;


public interface ViewNode extends NamedNode {
    
    public static final String ROLE = ViewNode.class.getName();
    
    /**
     * Pseudo-label for views <code>from-position="first"</code> (i.e. generator).
     */
    public static final String FIRST_POS_LABEL = "!first!";

    /**
     * Pseudo-label for views <code>from-position="last"</code> (i.e. serializer).
     */
    public static final String LAST_POS_LABEL = "!last!";

    /**
     * Return this view's label.
     */
    public String getLabel();
    
}