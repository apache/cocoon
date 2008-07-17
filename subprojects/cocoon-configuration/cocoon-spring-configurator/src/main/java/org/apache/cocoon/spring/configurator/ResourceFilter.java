/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.spring.configurator;

import java.util.Set;

import org.springframework.core.io.Resource;

/**
 * <p>This is an interface for custom implementations of filtering of resources being 
 * processed by Spring Configurator.</p>
 * 
 * <p>If you want to make some decisions on which resources you want to include at the runtime
 * then you should implement this interface and configure it following way:</p>
 * <pre>
 *   <configurator:settings>
 *     ..
 *     <configurator:filter class="org.package.SomeClass"/>
 *   </configurator:settings>
 * </pre>
 * 
 * <p>The implementations of this interface should be stateless</p>
 *
 */
public interface ResourceFilter {
    
    /**
     * @param resources The set of {@link Resource Spring resources}
     * @return
     */
    Set filter(Set resources);
    
}
