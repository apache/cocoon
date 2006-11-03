/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.classloader.reloading;

import java.util.List;

/**
 * Allow JCI ResourceStore implementations to handle lists of include/exclude patterns.
 * The ReloadingClassLoaderFactory calls these interface methods
 * 
 * @author Maurizio Pillitu
 *
 */
public interface PatternMatcherResourceStore {
    
    public void setExcludes(final List excludePatterns);
    
    public void setIncludes(final List includePatterns);
}
