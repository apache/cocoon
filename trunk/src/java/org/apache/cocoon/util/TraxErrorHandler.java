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
package org.apache.cocoon.util;

import org.apache.avalon.framework.logger.Logger;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

/**
 * This ErrorListener simply logs the exception and in
 * case of an fatal-error the exception is rethrown.
 * Warnings and errors are ignored.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: TraxErrorHandler.java,v 1.2 2004/03/08 14:03:30 cziegeler Exp $
 */
public class TraxErrorHandler implements ErrorListener {

    private StringBuffer warnings = new StringBuffer("Errors in XSLT transformation:\n");
    private Logger logger = null;

    public TraxErrorHandler( Logger logger ) {
        this.logger = logger;
    }

    public void warning( TransformerException exception )
            throws TransformerException
    {
        final String message = getMessage( exception );
        if ( this.logger != null ) {
            this.logger.warn( message );
        } else {
            System.out.println( "WARNING: " + message );
        }
        warnings.append("Warning: ");
        warnings.append(message);
        warnings.append("\n");
    }

    public void error( TransformerException exception )
            throws TransformerException
    {
        final String message = getMessage( exception );
        if ( this.logger != null ) {
            this.logger.error( message, exception );
        } else {
            System.out.println( "ERROR: " + message );
        }
        warnings.append("Error: ");
        warnings.append(message);
        warnings.append("\n");
    }

    public void fatalError( TransformerException exception )
            throws TransformerException
    {
        final String message = getMessage( exception );
        if ( this.logger != null ) {
            this.logger.fatalError( message, exception );
        } else {
            System.out.println( "FATAL-ERROR: " + message );
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

    private String getMessage( TransformerException exception ) {

        SourceLocator locator = exception.getLocator();
        if ( null != locator ) {
            String id = ( locator.getPublicId() != locator.getPublicId() )
                    ? locator.getPublicId()
                    : ( null != locator.getSystemId() )
                    ? locator.getSystemId() : "SystemId Unknown";
            return "File " + id
                    + "; Line " + locator.getLineNumber()
                    + "; Column " + locator.getColumnNumber()
                    + "; " + exception.getMessage();
        }
        return exception.getMessage();
    }
}
