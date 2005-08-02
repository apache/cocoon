/*
 * Copyright 2005 The Apache Software Foundation.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.apache.cocoon.util.location.Locatable;
import org.apache.cocoon.util.location.Location;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.JavaScriptException;
import org.xml.sax.SAXParseException;

/**
 * This class builds on <a href="http://jakarta.apache.org/commons/lang/">Jakarta Commons Lang</a>'s
 * <code><a href="http://jakarta.apache.org/commons/lang/api/org/apache/commons/lang/exception/ExceptionUtils.html">ExceptionUtils</a></code>
 * to handle exception chains, with additional heuristics to unroll exceptions, and other Cocoon-specific stuff
 * such as getting the {@link Location} of an exception.
 * 
 * @version $Id$
 */
public class ExceptionUtils extends org.apache.commons.lang.exception.ExceptionUtils {
    
    private static Method initCauseMethod;

    static {
        // Add the method used by Rhino to access wrapped exception, which is not part
        // of the standard method set of ExceptionUtils.
        org.apache.commons.lang.exception.ExceptionUtils.addCauseMethodName("getWrappedException");
        
        try {
            initCauseMethod = Throwable.class.getMethod("initCause", new Class[] {Throwable.class});
        } catch(Exception e) {
            // Ignore
        }
    }
    /**
     * Get the cause of a <code>Throwable</code>
     * 
     * @param thr the throwable
     * @return <code>thr</code>'s parent, or <code>null</code> if none exists.
     */
    public static final Throwable getCause(Throwable thr) {
        Throwable result;
//        // Specific case of JavaScriptException, which holds the wrapped exception
//        // in its 'value' property, which ExceptionUtils cannot find
//        if (thr instanceof JavaScriptException) {
//            Object obj = ((JavaScriptException)thr).getValue();
//            if (obj instanceof Throwable) {
//                result = (Throwable)obj;
//            } else {
//                result = null;
//            }
//        } else {
            result = org.apache.commons.lang.exception.ExceptionUtils.getCause(thr);
//        }

        // Ensure JDK 1.4's exception chaining is properly set up (this should really be done in Commons-Lang).
        if (result != null && initCauseMethod != null) {
            try {
                initCauseMethod.invoke(thr, new Throwable[]{result});
            } catch (Exception e) {
                // Ignore
            }
        }
        
        return result;
    }

    /**
     * Get the root cause of a <code>Throwable</code>.
     * 
     * @param thr the throwable
     * @return <code>thr</code>'s root parent, or <code>null</code> if none exists.
     */
   public static final Throwable getRootCause(Throwable thr) {
       Throwable parent;
       Throwable current = thr;
       while ((parent = getCause(current)) != null) {
           current = parent;
       }
       
       // Return current only if not the original throwable (happens when there's no cause)
       return current == thr ? null : current;
   }
    
    /**
     * Get the location of a throwable. Checks various ways to get the exception location.
     * 
     * @param thr the throwable
     * @return the location, or <code>null</code> if it could not be determined
     */
    public static Location getLocation(Throwable thr) {

        if (thr instanceof Locatable) {
            return ((Locatable)thr).getLocation();

        } else if (thr instanceof SAXParseException) {
            SAXParseException spe = (SAXParseException)thr;
            if (spe.getSystemId() != null) {
                return new Location(spe.getSystemId(), spe.getLineNumber(), spe.getColumnNumber());
            } else {
                return null;
            }

        } else if (thr instanceof TransformerException) {
            TransformerException ex = (TransformerException)thr;
            SourceLocator locator = ex.getLocator();
            if (locator != null && locator.getSystemId() != null) {
                return new Location(locator.getSystemId(), locator.getLineNumber(), locator.getColumnNumber());
            } else {
                return null;
            }

        } else if (thr instanceof EcmaError) {
            EcmaError ex = (EcmaError)thr;
            if (ex.getSourceName() != null) {
                return new Location(ex.getSourceName(), ex.getLineNumber(), ex.getColumnNumber());
            } else {
                return null;
            }

        } else if (thr instanceof JavaScriptException) {
            JavaScriptException ex = (JavaScriptException)thr;
            if (ex.sourceName() != null) {
                return new Location(ex.sourceName(), ex.lineNumber(), -1);
//            Vector stackTrace = ex.getJSStackTrace();
//            if (stackTrace != null) {
//                // see JavaScriptException.getMessage()
//                int i = stackTrace.size() - 1;
//                String sourceName = (String)stackTrace.elementAt(i-2);
//                int lineNum = ((Integer)stackTrace.elementAt(i)).intValue();
//                return new Location(sourceName, lineNum, -1);
            } else {
                return null;
            }
        }
        
        return null;
    }
}
