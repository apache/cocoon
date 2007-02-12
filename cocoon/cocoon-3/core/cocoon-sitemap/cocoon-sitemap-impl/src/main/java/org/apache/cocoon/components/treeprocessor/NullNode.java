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
package org.apache.cocoon.components.treeprocessor;

import org.apache.cocoon.environment.Environment;

/**
 * A no-op node to stub not yet implemented features.
 *
 * @version $Id$
 */
public class NullNode extends AbstractProcessingNode {

    public NullNode() {
        super(null);
    }
    
    public final boolean invoke(Environment env, InvokeContext context) throws Exception {

        getLogger().warn("Invoke on NullNode at " + getLocation());
        return false;

    }
}
