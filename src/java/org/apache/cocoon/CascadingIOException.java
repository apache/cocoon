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
 * @version CVS $Id: CascadingIOException.java,v 1.2 2004/03/05 13:02:42 bdelacretaz Exp $
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

