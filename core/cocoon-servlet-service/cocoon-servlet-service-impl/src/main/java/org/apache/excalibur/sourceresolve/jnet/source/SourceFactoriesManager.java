/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.excalibur.sourceresolve.jnet.source;

import java.util.Collections;
import java.util.Map;

public abstract class SourceFactoriesManager {

    protected static final ThreadLocal FACTORIES = new InheritableThreadLocal();

    protected static Map GLOBAL_FACTORIES;

    public static synchronized void setGlobalFactories(Map factories) {
        GLOBAL_FACTORIES = factories;
    }

    public static void pushFactories(Map factories) {
        // no need to synchronize as we use a thread local
        CompositeMap factoryMap = (CompositeMap)FACTORIES.get();
        if ( factoryMap == null ) {
            factoryMap = new CompositeMap();
            FACTORIES.set(factoryMap);
        }
        factoryMap.pushMap(factories);
    }

    public static void popFactories() {
        // no need to synchronize as we use a thread local
        CompositeMap factoryMap = (CompositeMap)FACTORIES.get();
        if ( factoryMap != null ) {
            factoryMap.popMap();
            if ( factoryMap.getMapCount() == 0 ) {
                FACTORIES.set(null);
            }
        } else {
            throw new IllegalStateException("The factories stack is already empty.");
        }
    }

    public static synchronized Map getCurrentFactories() {
        Map factories = (Map)FACTORIES.get();
        if ( factories == null ) {
            factories = GLOBAL_FACTORIES;
            if ( factories == null ) {
                factories = Collections.EMPTY_MAP;
            }
        } else if (GLOBAL_FACTORIES != null ) {
            CompositeMap cm = new CompositeMap();
            cm.pushMap(GLOBAL_FACTORIES);
            cm.pushMap(factories);
            factories = cm;
        }
        return factories;
    }
}
