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

import java.util.Collection;

import org.apache.cocoon.components.cprocessor.Node;

/**
 * Represents a component declaration within the map:components section.
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a> 
 */
public interface ComponentNode extends Node {

    public static final String ROLE = ComponentNode.class.getName();

    /**
     * Return the labels associated with this sitemap 
     * component declaration statement. Only relevant if
     * this represents a generator, transformer or serializer.
     */
    public Collection getLabels();
    
    /**
     * Return the hint of the sitemap component this node represents.
     */
    public String getComponentHint();
    
    /**
     * Return the mime-type attribute of this sitemap
     * component declaration statement. Only relevant if
     * this represents a reader or serializer.
     */
    public String getMimeType();
}