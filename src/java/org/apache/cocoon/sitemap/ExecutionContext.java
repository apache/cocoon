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
package org.apache.cocoon.sitemap;


/**
 * This context contains information about the current statement that should
 * be executed like the location in the sitemap etc.
 *
 * TODO - This is not finished yet!
 * 
 * @since 2.2
 * @version CVS $Id: ExecutionContext.java,v 1.1 2004/06/09 11:59:23 cziegeler Exp $
 */
public interface ExecutionContext {
    
    /**
     * Return the location of the statement in the sitemap.
     */
    String getLocation();
    
    /**
     * Return the component type
     */
    String getType();
}
