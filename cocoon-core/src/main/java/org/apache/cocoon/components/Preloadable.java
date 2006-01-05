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
package org.apache.cocoon.components;

/**
 * Marker interface for Avalon-managed components that need to be loaded at startup time.
 * <p>
 * Components implementing this interface will always be initialized when the service manager that
 * holds them is created, even if lazy-loading of components is enabled.
 * <p>
 * Note that there are also other ways to require a component to be loaded at startup:
 * <ul>
 * <li>by implementing the <code>Startable</code> Avalon interface, which defines two additional
 *     <code>start()</code> and <code>stop()</code> methods</li>
 * <li>by adding the <code>preload="true"</code> attribute on a component's configuration</li>
 * </ul>
 * 
 * @since 2.2
 * @version $Id$
 */
public interface Preloadable {
    // nothing, it's just a marker
}
