/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.corona.servlet.node;

import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.corona.sitemap.node.InvocationResult;
import org.apache.cocoon.corona.sitemap.node.SerializeNode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class StatusCodeCollector {

    private static ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>();

    @Around("execution(org.apache.cocoon.corona.sitemap.node.InvocationResult org.apache.cocoon.corona.sitemap.node.SerializeNode.invoke(org.apache.cocoon.corona.sitemap.Invocation))")
    public Object interceptInvoke(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        SerializeNode target = (SerializeNode) proceedingJoinPoint.getTarget();
        String statusCode = target.getParameters().get("status-code");

        InvocationResult invocationResult = (InvocationResult) proceedingJoinPoint.proceed();

        if (invocationResult.isProcessed() && statusCode != null) {
            threadLocal.set(Integer.valueOf(statusCode));
        }

        return invocationResult;
    }

    public static int getStatusCode() {
        Integer integer = threadLocal.get();

        if (integer == null) {
            return HttpServletResponse.SC_OK;
        }

        return integer.intValue();
    }

    public static void setStatusCode(int statusCode) {
        threadLocal.set(statusCode);
    }
}
