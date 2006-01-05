/*
 * Copyright 2005 The Apache Software Foundation.
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

import org.apache.cocoon.ProcessingException;


/**
 * TODO WORK IN PROGRESS!!
 *
 * This interface is the connection between the Cocoon core components
 * and an optional application/sitemap container.
 *
 * @since 2.2
 * @version $Id$
 */
public interface ComponentLocator {

    Object getComponent(String key) throws ProcessingException;

    void release(Object component);

    boolean hasComponent(String key);
}
