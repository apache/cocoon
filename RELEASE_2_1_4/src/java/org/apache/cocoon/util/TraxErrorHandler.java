/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: TraxErrorHandler.java,v 1.1 2003/03/09 00:09:43 pier Exp $
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
