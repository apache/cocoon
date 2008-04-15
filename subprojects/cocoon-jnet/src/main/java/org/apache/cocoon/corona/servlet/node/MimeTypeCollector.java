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
package org.apache.cocoon.corona.servlet.node;

import org.apache.cocoon.corona.pipeline.Pipeline;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class MimeTypeCollector {

    private static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<String>();

    @Around("execution(* org.apache.cocoon.corona.pipeline.Pipeline.execute(..))")
    public Object interceptInvoke(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        System.out.println("Executing MimeTypeCollector");
        Object result = proceedingJoinPoint.proceed();

        Pipeline pipeline = (Pipeline) proceedingJoinPoint.getTarget();
        THREAD_LOCAL.set(pipeline.getContentType());

        return result;
    }

    public static String getMimeType() {
        String mimeType = THREAD_LOCAL.get();

        return mimeType;
    }

    public static void setMimeType(String mimeType) {
        THREAD_LOCAL.set(mimeType);
    }
}
