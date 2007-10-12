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
package org.apache.cocoon.components.notification;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.xml.sax.SAXParseException;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 *  Generates an Notifying representation of widely used objects.
 *
 * @version $Id$
 */
public class DefaultNotifyingBuilder implements NotifyingBuilder {

    /**
     * Builds a Notifying object (SimpleNotifyingBean in this case)
     * that tries to explain what the Object o can reveal.
     *
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
                Throwable rootCause = getRootCause(t);

                n.setSource(t.getClass().getName());

                // NullPointerException usually does not have a message
                if (rootCause.getMessage() != null) {
                    n.setMessage(rootCause.getMessage());
                } else {
                    n.setMessage(t.getMessage());
                }

                n.setDescription(t.toString());
                n.addExtraDescription(Notifying.EXTRA_CAUSE, rootCause.toString());

                if (rootCause instanceof SAXParseException) {
                    SAXParseException saxParseException = (SAXParseException) rootCause;

                    n.addExtraDescription(Notifying.EXTRA_LOCATION,
                                          String.valueOf(saxParseException.getSystemId()));
                    n.addExtraDescription(Notifying.EXTRA_LINE,
                                          String.valueOf(saxParseException.getLineNumber()));
                    n.addExtraDescription(Notifying.EXTRA_COLUMN,
                                          String.valueOf(saxParseException.getColumnNumber()));
                } else if (rootCause instanceof TransformerException) {
                    TransformerException transformerException = (TransformerException) rootCause;
                    SourceLocator sourceLocator = transformerException.getLocator();

                    if (null != sourceLocator) {
                        n.addExtraDescription(Notifying.EXTRA_LOCATION,
                                              String.valueOf(sourceLocator.getSystemId()));
                        n.addExtraDescription(Notifying.EXTRA_LINE,
                                              String.valueOf(sourceLocator.getLineNumber()));
                        n.addExtraDescription(Notifying.EXTRA_COLUMN,
                                              String.valueOf(sourceLocator.getColumnNumber()));
                    }
                }

                // Add root cause exception stacktrace
                StringWriter sw = new StringWriter();
                rootCause.printStackTrace(new PrintWriter(sw));
                n.addExtraDescription(Notifying.EXTRA_STACKTRACE, sw.toString());

                // Add full exception chain
                sw = new StringWriter();
                appendTraceChain(sw, t);
                n.addExtraDescription(Notifying.EXTRA_FULLTRACE, sw.toString());
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
     *
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
     * Print stacktrace of the Throwable and stacktraces of its all nested causes into a Writer.
     */
    private static void appendTraceChain(Writer out, Throwable t) {
        PrintWriter pw = new PrintWriter(out);
        if (SystemUtils.isJavaVersionAtLeast(140)) {
            t.printStackTrace(pw);
        } else {
            for (Throwable cause = t; cause != null; cause = ExceptionUtils.getCause(cause)) {
                if (cause != t) {
                    pw.println();
                }
                cause.printStackTrace(pw);
            }
        }
    }

    /**
     * Get root cause Throwable.
     */
    public static Throwable getRootCause (Throwable t) {
        Throwable rootCause = ExceptionUtils.getRootCause(t);
        return rootCause != null ? rootCause : t;
    }
}
