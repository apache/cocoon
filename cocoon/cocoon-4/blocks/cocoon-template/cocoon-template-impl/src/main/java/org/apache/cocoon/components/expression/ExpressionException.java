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
package org.apache.cocoon.components.expression;

import java.io.IOException;

import org.apache.avalon.framework.CascadingThrowable;

/**
 * @version $Id$
 */
public class ExpressionException
    extends IOException implements CascadingThrowable {

    /**
     * The Throwable that caused this exception to be thrown.
     */
    private final Throwable throwable;

    /**
     * Construct a new <code>ExpressionException</code> instance.
     *
     * @param message the detail message for this exception.
     */
    public ExpressionException(final String message) {
        this(message, null);
    }

    /**
     * Construct a new <code>ExpressionException</code> instance.
     *
     * @param message the detail message for this exception.
     * @param throwable the root cause of the exception.
     */
    public ExpressionException(final String message, final Throwable throwable) {
        super(message);
        this.throwable = throwable;
    }
    
    /**
     * Retrieve the cause of the exception.
     *
     * @return the cause.
     */
    public final Throwable getCause() {
        return this.throwable;
    }
}
