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
package org.apache.cocoon;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.avalon.framework.CascadingThrowable;
import org.xml.sax.SAXParseException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.SourceLocator;

/**
 * This is a wrapping IOException.
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CascadingIOException.java,v 1.1 2003/03/09 00:08:35 pier Exp $
 */

public class CascadingIOException 
extends IOException 
implements CascadingThrowable {

    /**
     * The Throwable that caused this exception to be thrown.
     */
    private final Throwable m_throwable;


    /**
     * Construct a new <code>ProcessingException</code> instance.
     */
    public CascadingIOException(String message) {
        this(message, null);
    }
    
    /**
     * Creates a new <code>ProcessingException</code> instance.
     *
     * @param ex an <code>Exception</code> value
     */
    public CascadingIOException(Exception ex) {
        this(ex.getMessage(), ex);
    }
    
    /**
     * Construct a new <code>ProcessingException</code> that references
     * a parent Exception.
     */
    public CascadingIOException(String message, Throwable t) {
        super( message );
        this.m_throwable = t;
    }
    
    /**
     * Retrieve root cause of the exception.
     *
     * @return the root cause
     */
    public final Throwable getCause()
    {
        return this.m_throwable;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(super.toString());
        final Throwable t = getCause();
        if(t!=null) {
            s.append(": ");
            // be more verbose try to get location info
            s.append( extraInfo(t) );
            s.append(t.toString());
        }
        return s.toString();
    }
    
    /**
     * Examine Throwable and try to figure out location information.
     * <p>
     *   At the moment only SAXParseException, and TransformerException
     *   are considered.
     * </p>
     *
     * @return String containing location information of the format
     *  <code>{file-name}:{line}:{column}:</code>, if no location info is 
     *  available return empty string
     */
    private String extraInfo( Throwable t ) {
        StringBuffer sb = new StringBuffer();
        if (t instanceof SAXParseException) {
            SAXParseException spe = (SAXParseException)t;
            sb.append( String.valueOf(spe.getSystemId()));
            sb.append( ":" );
            sb.append( String.valueOf(spe.getLineNumber()));
            sb.append( ":" );
            sb.append( String.valueOf(spe.getColumnNumber()));
            sb.append( ":" );
        } else if (t instanceof TransformerException) {
            TransformerException transformerException = (TransformerException) t;
            SourceLocator sourceLocator = transformerException.getLocator();
            
            if( null != sourceLocator ) {
                sb.append( String.valueOf(sourceLocator.getSystemId()));
                sb.append( ":" );
                sb.append( String.valueOf(sourceLocator.getLineNumber()));
                sb.append( ":" );
                sb.append( String.valueOf(sourceLocator.getColumnNumber()));
                sb.append( ":" );
            }
        }
        return sb.toString();
    }
    
    public void printStackTrace() {
        super.printStackTrace();
        if(getCause()!=null)
            getCause().printStackTrace();
    }
    
    public void printStackTrace( PrintStream s ) {
        super.printStackTrace(s);
        if(getCause()!=null)
            getCause().printStackTrace(s);
    }
    
    public void printStackTrace( PrintWriter s ) {
        super.printStackTrace(s);
        if(getCause()!=null)
            getCause().printStackTrace(s);
    }

}

