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

package org.apache.cocoon.transformation.pagination;

/**
 * Container class for the immutable pagination rules for each page.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: ItemGroup.java,v 1.2 2004/03/08 14:03:31 cziegeler Exp $
 */
public class ItemGroup {
    
    private String name;
    private String elementName;
    private String elementURI;
    
    public ItemGroup (String name, String elementURI, String elementName) {
        this.name = name;
        this.elementURI = elementURI;
        this.elementName = elementName;
    }
    
    public boolean match(String elementName, String elementURI) {
        return (this.elementName.equals(elementName) && this.elementURI.equals(elementURI));
    }

    public boolean match(String elementURI) {
        return this.elementURI.equals(elementURI);
    }

    public String getName() {
        return this.name;
    }

    public String getElementURI() {
        return this.elementURI;
    }
    
    public String getElementName() {
        return this.elementName;
    }
}
