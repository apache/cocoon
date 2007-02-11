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
package org.apache.cocoon.webservices.memory;

/**
 * Class which provides JVM memory related SOAP services.
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @version CVS $Id: Memory.java,v 1.2 2004/03/05 13:01:43 bdelacretaz Exp $
 */
public class Memory {
	
    // static reference to the runtime object.
    private static final Runtime runtime = Runtime.getRuntime();

    /**
     * <code>getFreeMemory</code> returns the amount of free memory
     * in the system.
     *
     * @return the amount of free memory in the system
     */
    public static long getFreeMemory() {
        return runtime.freeMemory();
    }

    /**
     * <code>getTotalMemory</code> returns the total amount of memory
     * in the JVM.
     *
     * @return the total amount of memory in the JVM
     */
    public static long getTotalMemory() {
        return runtime.totalMemory();
    }

    /**
     * <code>invokeGC</code> calls upon the JVM Garbage Collector to
     * recycle unused objects.
     */
    public static void invokeGC() {
        runtime.gc();
    }
}
