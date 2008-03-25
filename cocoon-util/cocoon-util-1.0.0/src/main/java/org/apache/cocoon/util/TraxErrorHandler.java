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
package org.apache.cocoon.util;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.AvalonLogger;

/**
 * This error handler simply logs all exception and, in case of a fatal error,
 * the exception is rethrown. Warnings and errors are not re-thrown.
 *
 * @version $Id$
 */
public class TraxErrorHandler implements ErrorListener {

    private final Log logger;
    
    private StringBuffer warnings = new StringBuffer("Errors in XSLT transformation:\n");


    public TraxErrorHandler() {
        this.logger = LogFactory.getLog(getClass());
    }

    public TraxErrorHandler(Log logger) {
        this.logger = logger;
    }

    /** @deprecated Use {@link #TraxErrorHandler(Log)}. } */
    public TraxErrorHandler(Logger logger) {
        this.logger = new AvalonLogger(logger);
    }

    public void warning(TransformerException exception)
    throws TransformerException {
        final String message = getMessage(exception);
        if (this.logger != null) {
            this.logger.warn(message);
        } else {
            System.out.println("WARNING: " + message);
        }
        warnings.append("Warning: ");
        warnings.append(message);
        warnings.append("\n");
    }

    public void error(TransformerException exception)
    throws TransformerException {
        final String message = getMessage(exception);
        if (this.logger != null) {
            this.logger.error(message, exception);
        } else {
            System.out.println("ERROR: " + message);
        }
        warnings.append("Error: ");
        warnings.append(message);
        warnings.append("\n");
    }

    public void fatalError(TransformerException exception)
    throws TransformerException {
        final String message = getMessage(exception);
        if (this.logger != null) {
            this.logger.fatal(message, exception);
        } else {
            System.out.println("FATAL-ERROR: " + message);
        }
        warnings.append("Fatal: ");
        warnings.append(message);
        warnings.append("\n");

        try {
            throw new TransformerException(warnings.toString());
        } finally {
            warnings = new StringBuffer();
        }
    }

    private String getMessage(TransformerException exception) {
        SourceLocator locator = exception.getLocator();
        if (locator != null) {
            String id = (!locator.getPublicId().equals(locator.getPublicId()))
                    ? locator.getPublicId()
                    : (null != locator.getSystemId())
                    ? locator.getSystemId() : "SystemId Unknown";
            return "File " + id
                   + "; Line " + locator.getLineNumber()
                   + "; Column " + locator.getColumnNumber()
                   + "; " + exception.getMessage();
        }
        return exception.getMessage();
    }
}
