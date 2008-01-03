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
package org.apache.cocoon.servletservice.shielding;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @version $Id$
 * @since 1.0.0
 */
public class ShieldingClassLoaderInterceptor implements MethodInterceptor {

    private final ClassLoader classLoader;

    public ShieldingClassLoaderInterceptor(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.classLoader);

            return invocation.proceed();
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

}
