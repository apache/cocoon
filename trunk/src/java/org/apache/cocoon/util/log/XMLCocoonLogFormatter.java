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
package org.apache.cocoon.util.log;

import org.apache.avalon.framework.CascadingThrowable;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.log.ContextMap;
import org.apache.log.LogEvent;
import org.apache.log.format.Formatter;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A refactoring of <code>org.apache.log.format.PatternFormatter</code>
 * and <code>org.apache.cocoon.util.log.CocoonLogFormatter</code> for
 * producing XML format.
 * This formater formats the LogEntries according to given input types. Each
 * log entry is inside a &lt;log-entry&gt; element and each information is
 * inside an own element.
 *
 * <ul>
 * <li><code>class</code> : outputs the name of the class that has logged the
 *     message. The optional <code>short</code> subformat removes the
 *     package name. Warning : this pattern works only if formatting occurs in
 *     the same thread as the call to Logger, i.e. it won't work with
 *     <code>AsyncLogTarget</code>. The class name is embeded by a &lt;class&gt;
 *     element.</li>
 * <li><code>thread</code> : outputs the name of the current thread (first element
 *     on the context stack). The thread name is surrounded by a &lt;thread&gt;
 *     element.</li>
 * <li><code>uri</code> : outputs the request URI (&lt;uri&gt;).<li>
 * <li><code>category</code> : outputs the log category (&lt;category&gt;).<li>
 * <li><code>message</code> : outputs the message (&lt;message&gt;).<li>
 * <li><code>time</code> : outputs the time (&lt;time&gt;).<li>
 * <li><code>rtime</code> : outputs the relative time (&lt;relative-time&gt;).<li>
 * <li><code>throwable</code> : outputs the exception (&lt;throwable&gt;).<li>
 * <li><code>priority</code> : outputs the priority (&lt;priority&gt;).<li>
 * </ul>
 *
 * @author <a href="mailto:donaldp@apache.org">Peter Donald</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: XMLCocoonLogFormatter.java,v 1.1 2003/03/09 00:09:44 pier Exp $
 */
public class XMLCocoonLogFormatter
implements Formatter {

    protected final static String  TYPE_CLASS_STR       = "class";
    protected final static String  TYPE_CLASS_SHORT_STR = "short";

    protected final static int         TYPE_REQUEST_URI     = 0;
    protected final static int         TYPE_CATEGORY        = 1;
    protected final static int         TYPE_MESSAGE         = 2;
    protected final static int         TYPE_TIME            = 3;
    protected final static int         TYPE_RELATIVE_TIME   = 4;
    protected final static int         TYPE_THROWABLE       = 5;
    protected final static int         TYPE_PRIORITY        = 6;
    protected final static int         TYPE_CLASS        = 7;
    protected final static int         TYPE_CLASS_SHORT        = 8;
    protected final static int         TYPE_THREAD        = 9;

    public final static String[] typeStrings = new String[] {"uri", // 0
         "category",  // 1
         "message",   // 2
         "time",      // 3
         "rtime",     // 4
         "throwable", // 5
         "priority",  // 6
         "class",    // 7
         "class:short", // 8
         "thread"};  // 9

    protected final static String EOL = System.getProperty("line.separator", "\n");
    protected final SimpleDateFormat dateFormatter = new SimpleDateFormat("(yyyy-MM-dd) HH:mm.ss:SSS");

    protected int[] types;

    /**
     * Format the event according to the pattern.
     *
     * @param event the event
     * @return the formatted output
     */
    public String format( final LogEvent event ) {
        final StringBuffer sb = new StringBuffer();
        sb.append("<log-entry>").append(EOL);
        final String value = this.getRequestId(event.getContextMap());
        if (value != null) {
            sb.append("<request-id>").append(value).append("</request-id>").append(EOL);
        }
        for(int i = 0; i < this.types.length; i++) {

            switch(this.types[i]) {

                case TYPE_REQUEST_URI:
                    sb.append("<uri>");
                    sb.append(this.getURI(event.getContextMap()));
                    sb.append("</uri>").append(EOL);
                    break;

                case TYPE_CLASS:
                    sb.append("<class>");
                    sb.append(this.getClass(TYPE_CLASS_STR));
                    sb.append("</class>").append(EOL);
                    break;
                case TYPE_CLASS_SHORT:
                    sb.append("<class>");
                    sb.append(this.getClass(TYPE_CLASS_SHORT_STR));
                    sb.append("</class>").append(EOL);
                    break;

                case TYPE_THREAD:
                    sb.append("<thread>");
                    sb.append(this.getThread(event.getContextMap()));
                    sb.append("</thread>").append(EOL);
                    break;

                case TYPE_RELATIVE_TIME:
                    sb.append("<relative-time>");
                    sb.append(event.getRelativeTime());
                    sb.append("</relative-time>").append(EOL);
                    break;

                case TYPE_TIME:
                    sb.append("<time>");
                    sb.append(dateFormatter.format(new Date(event.getTime())));
                    sb.append("</time>").append(EOL);
                    break;

                case TYPE_THROWABLE:
                    Throwable throwable = event.getThrowable();
                    if (throwable != null) {
                        sb.append("<throwable><![CDATA[").append(EOL);
                        while (throwable != null) {
                            final StringWriter sw = new StringWriter();
                            throwable.printStackTrace( new java.io.PrintWriter( sw ) );
                            sb.append(sw.toString());
                            if (throwable instanceof CascadingThrowable ) {
                                throwable = ((CascadingThrowable)throwable).getCause();
                            } else {
                                throwable = null;
                            }
                        }
                        sb.append(EOL).append("]]> </throwable>").append(EOL);
                    }
                    break;

                case TYPE_MESSAGE:
                    sb.append("<message><![CDATA[").append(EOL);
                    sb.append(event.getMessage());
                    sb.append(EOL).append("]]> </message>").append(EOL);
                    break;

                case TYPE_CATEGORY:
                    sb.append("<category>");
                    sb.append(event.getCategory());
                    sb.append("</category>").append(EOL);
                    break;

                case TYPE_PRIORITY:
                    sb.append("<priority>");
                    sb.append(event.getPriority().getName());
                    sb.append("</priority>").append(EOL);
                    break;
            }
        }
        sb.append("</log-entry>");
        sb.append(EOL);
        return sb.toString();
    }

    /**
     * Find the URI that is being processed.
     */
    private String getURI(ContextMap ctxMap) {
        String result = "Unknown-URI";

        // Get URI from the the object model.
        if (ctxMap != null) {
            Object context = ctxMap.get("objectModel");
            if (context != null &&context instanceof Map) {
                // Get the request
                Request request = ObjectModelHelper.getRequest((Map)context);
                if (request != null) {
                    result = request.getRequestURI();
                }
            }
        }

        return result;
    }

    /**
     * Find the request id that is being processed.
     */
    private String getRequestId(ContextMap ctxMap) {
        String result = null;

        // Get URI from the the object model.
        if (ctxMap != null) {
            Object context = ctxMap.get("request-id");
            if (context != null) result = context.toString();
        }

        return result;
    }

    /**
     * Finds the class that has called Logger.
     */
    private String getClass(String format) {

        Class[] stack = this.callStack.get();

        // Traverse the call stack in reverse order until we find a Logger
        for (int i = stack.length-1; i >= 0; i--) {
            if (this.loggerClass.isAssignableFrom(stack[i])) {

                // Found : the caller is the previous stack element
                String className = stack[i+1].getName();

                // Handle optional format
                if (TYPE_CLASS_SHORT_STR.equalsIgnoreCase(format))
                {
                    int pos = className.lastIndexOf('.');
                    if (pos >= 0)
                        className = className.substring(pos + 1);
                }

                return className;
            }
        }

        // No Logger found in call stack : can occur with AsyncLogTarget
        // where formatting takes place in a different thread.
        return "Unknown-class";
    }

    /**
     * Find the thread that is logged this event.
     */
    private String getThread(ContextMap ctxMap) {
        if (ctxMap != null && ctxMap.get("threadName") != null) {
            return (String)ctxMap.get("threadName");
        } else {
            return "Unknown-thread";
        }
    }

    /**
     * Retrieve the type-id for a particular string.
     *
     * @param type the string
     * @return the type-id
     */
    protected int getTypeIdFor( final String type ) {
        int index = 0;
        boolean found = false;
        while (!found && index < typeStrings.length) {
            if (type.equalsIgnoreCase(typeStrings[index])) {
                found = true;
            } else {
                index++;
            }
        }
        if (found) return index;
        throw new IllegalArgumentException( "Unknown Type - " + type );
    }

    /**
     * Set the types from an array of strings.
     */
    public void setTypes(String [] typeStrings) {
        if (typeStrings == null) {
            this.types = new int[0];
        } else {
            this.types = new int[typeStrings.length];
            for(int i = 0; i < typeStrings.length; i++) {
                this.types[i] = this.getTypeIdFor(typeStrings[i]);
            }
        }
    }

    /**
     * Set the types from a whitespace separated string
     */
    public void setTypes(String typeString) {
        if (typeString == null) {
            this.types = new int[0];
        } else {
            // this is not the best implementation, but it works...
            StringTokenizer st = new StringTokenizer(typeString);
            this.types = new int[st.countTokens()];
            for (int i = 0; i < this.types.length; i++) {
                this.types[i] = this.getTypeIdFor(st.nextToken());
            }
        }
    }

    /** The class that we will search for in the call stack */
    private Class loggerClass = org.apache.log.Logger.class;
    private CallStack callStack = new CallStack();

    /**
     * Hack to get the call stack as an array of classes. The
     * SecurityManager class provides it as a protected method, so
     * change it to public through a new method !
     */
    static public class CallStack extends SecurityManager
    {
        /**
         * Returns the current execution stack as an array of classes.
         * The length of the array is the number of methods on the execution
         * stack. The element at index 0 is the class of the currently executing
         * method, the element at index 1 is the class of that method's caller,
         * and so on.
         *
         * @return current execution stack as an array of classes.
         */
        public Class[] get()
        {
            return getClassContext();
        }
    }
}
