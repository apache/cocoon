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
package org.apache.cocoon.components.notification;

import org.apache.avalon.framework.CascadingThrowable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.SourceLocator;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.Map;

/**
 *  Generates an Notifying representation of widely used objects.
 *
 * @author <a href="mailto:nicolaken@apache.org">Nicola Ken Barozzi</a>
 * @author Marc Liyanage (futureLAB AG)
 * @version CVS $Id: DefaultNotifyingBuilder.java,v 1.7 2004/06/09 12:11:36 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=NotifyingBuilder
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=notifying-builder
 */
public class DefaultNotifyingBuilder implements NotifyingBuilder, ThreadSafe {

    /**
     * Builds a Notifying object (SimpleNotifyingBean in this case)
     * that tries to explain what the Object o can reveal.
     * @param sender who sent this Object.
     * @param o the object to use when building the SimpleNotifyingBean
     * @return the  Notifying Object that was build
     * @see org.apache.cocoon.components.notification.Notifying
     */
    public Notifying build (Object sender, Object o) {
        if (o instanceof Notifying) {
            return (Notifying) o;
        } else if (o instanceof Throwable) {
            Throwable t = (Throwable) o;
            SimpleNotifyingBean n = new SimpleNotifyingBean(sender);
            n.setType(Notifying.ERROR_NOTIFICATION);
            n.setTitle("An Error Occurred");
            if (t != null) {
                n.setSource(t.getClass().getName());

                Throwable rootCauseThrowable = getRootCause(t);
                n.addExtraDescription(Notifying.EXTRA_CAUSE, rootCauseThrowable.toString());

                if (rootCauseThrowable instanceof SAXParseException) {
                    SAXParseException saxParseException = (SAXParseException) rootCauseThrowable;
                    n.setMessage         (                           saxParseException.getMessage()      );
                    n.addExtraDescription(Notifying.EXTRA_LOCATION,
                                          String.valueOf(saxParseException.getSystemId()));
                    n.addExtraDescription(Notifying.EXTRA_LINE,
                                          String.valueOf(saxParseException.getLineNumber()));
                    n.addExtraDescription(Notifying.EXTRA_COLUMN,
                                          String.valueOf(saxParseException.getColumnNumber()));
                } else if (rootCauseThrowable instanceof TransformerException) {
                    TransformerException transformerException = (TransformerException) rootCauseThrowable;
                    SourceLocator sourceLocator = transformerException.getLocator();
                    n.setMessage         (                           transformerException.getMessage()   );

                    if (null != sourceLocator) {
                        n.addExtraDescription(Notifying.EXTRA_LOCATION,
                                              String.valueOf(sourceLocator.getSystemId()));
                        n.addExtraDescription(Notifying.EXTRA_LINE,
                                              String.valueOf(sourceLocator.getLineNumber()));
                        n.addExtraDescription(Notifying.EXTRA_COLUMN,
                                              String.valueOf(sourceLocator.getColumnNumber()));
                    }
                } else {
                    n.setMessage(t.getMessage());
                }

                n.setDescription(t.toString());

                // Get the stacktrace: if the exception is a SAXException,
                // the stacktrace of the embedded exception is used as the
                // SAXException does not append it automatically
                Throwable stackTraceException;
                if (t instanceof SAXException && ((SAXException) t).getException() != null) {
                    stackTraceException = ((SAXException) t).getException();
                } else {
                    stackTraceException = t;
                }
                // org.apache.avalon.framework.ExceptionUtil.captureStackTrace();
                StringWriter sw = new StringWriter();
                stackTraceException.printStackTrace(new PrintWriter(sw));
                n.addExtraDescription(Notifying.EXTRA_STACKTRACE, sw.toString());
                // Add nested throwables description
                sw = new StringWriter();
                appendCauses(new PrintWriter(sw), stackTraceException);
                String causes = sw.toString();
                if (causes != null && causes.length() != 0) {
                    n.addExtraDescription(Notifying.EXTRA_FULLTRACE, causes);
                }
            }
            return n;
        } else {
            SimpleNotifyingBean n = new SimpleNotifyingBean(sender);
            n.setType(Notifying.UNKNOWN_NOTIFICATION);
            n.setTitle("Object Notification");
            n.setMessage(String.valueOf(o));
            n.setDescription("No details available.");
            return n;
        }
    }

    /**
     * Builds a Notifying object (SimpleNotifyingBean in this case)
     * that explains a notification.
     * @param sender who sent this Object.
     * @param o the object to use when building the SimpleNotifyingBean
     * @param type see the Notifying apidocs
     * @param title see the Notifying apidocs
     * @param source see the Notifying apidocs
     * @param message see the Notifying apidocs
     * @param description see the Notifying apidocs
     * @param extra see the Notifying apidocs
     * @return the  Notifying Object that was build
     * @see org.apache.cocoon.components.notification.Notifying
     */
    public Notifying build(Object sender, Object o, String type, String title,
                           String source, String message, String description, Map extra) {
        // NKB Cast here is secure, the method is of this class
        SimpleNotifyingBean n = (SimpleNotifyingBean) build (sender, o);

        if (type != null)
            n.setType(type);
        if (title != null)
            n.setTitle(title);
        if (source != null)
            n.setSource(source);
        if (message != null)
            n.setMessage(message);
        if (description != null)
            n.setDescription(description);
        if (extra != null)
            n.addExtraDescriptions(extra);

        return n;
    }


    /**
     * Print recursively all nested causes of a Throwable in a PrintWriter.
     */
    private static void appendCauses (PrintWriter out, Throwable t) {
        Throwable cause = null;
        if (t instanceof CascadingThrowable) {
            cause = ((CascadingThrowable) t).getCause();
        } else if (t instanceof SAXException) {
            cause = ((SAXException) t).getException();
        } else if (t instanceof java.sql.SQLException) {
            cause = ((java.sql.SQLException) t).getNextException();
        }
        if (cause != null) {
            out.print("Original Exception: ");
            cause.printStackTrace(out);
            out.println();
            // Recurse
            appendCauses(out, cause);
        }
    }

    /**
     * Get root Exception.
     */
    public static Throwable getRootCause (Throwable t) {
        Throwable cause = null;
        if (t instanceof CascadingThrowable) {
            cause = ((CascadingThrowable) t).getCause();
        } else if (t instanceof SAXException) {
            cause = ((SAXException) t).getException();
        } else if (t instanceof java.sql.SQLException) {
            cause = ((java.sql.SQLException) t).getNextException();
        }
        if (cause == null) {
            return t;
        } else {
            // Recurse
            return getRootCause(cause);
        }
    }
}
