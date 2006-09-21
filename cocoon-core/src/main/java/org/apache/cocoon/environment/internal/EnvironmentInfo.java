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
package org.apache.cocoon.environment.internal;

import org.apache.cocoon.Processor;
import org.apache.cocoon.environment.Environment;

/**
 * This object holds a set of objects for an environment.
 *
 * This is an internal class, and it might change in an incompatible way over time.
 * For developing your own components/applications based on Cocoon, you shouldn't 
 * really need it.
 *
 * @version $Id$
 * @since 2.2
 */
public class EnvironmentInfo {

    public final Processor      processor;
    public final int            oldStackCount;
    public final Environment    environment;

    public EnvironmentInfo(Processor   processor, 
                           int         oldStackCount,
                           Environment environment) {
        this.processor = processor;
        this.oldStackCount = oldStackCount;
        this.environment = environment;
    }
}

