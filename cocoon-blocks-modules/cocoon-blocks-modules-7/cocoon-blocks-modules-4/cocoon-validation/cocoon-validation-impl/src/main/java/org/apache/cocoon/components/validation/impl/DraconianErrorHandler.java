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
package org.apache.cocoon.components.validation.impl;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>An implementation of the {@link ErrorHandler} interface re-throwing
 * all exceptions passed to it.</p>
 *
 */
public final class DraconianErrorHandler implements ErrorHandler {
    
    /** <p>The singleton instance of the {@link DraconianErrorHandler}.</p> */
    public static final DraconianErrorHandler INSTANCE = new DraconianErrorHandler();

    /** <p>Deny normal construction of instances of this class.</p> */
    private DraconianErrorHandler() { }

    /**
     * <p>Simply re-throw the specified {@link SAXParseException}.</p>
     */
    public void warning(SAXParseException exception)
    throws SAXException {
        throw exception;
    }

    /**
     * <p>Simply re-throw the specified {@link SAXParseException}.</p>
     */
    public void error(SAXParseException exception)
    throws SAXException {
        throw exception;
    }

    /**
     * <p>Simply re-throw the specified {@link SAXParseException}.</p>
     */
    public void fatalError(SAXParseException exception)
    throws SAXException {
        throw exception;
    }
}
