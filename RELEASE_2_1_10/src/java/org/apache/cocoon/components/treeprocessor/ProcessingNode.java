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

import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.util.location.Locatable;
import org.apache.cocoon.util.location.Location;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id$
 */

public interface ProcessingNode extends ThreadSafe, Locatable {

    /**
     * The key of the <code>SourceResolver</code> in the object model.
     */
    String OBJECT_SOURCE_RESOLVER = "source-resolver";

    /**
     * Process environment.
     */
    boolean invoke(Environment env, InvokeContext context) throws Exception;

    /**
     * Get the location of this node.
     */
    Location getLocation();
}
