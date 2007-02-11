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
package org.apache.cocoon.util.log;

import java.util.Map;

import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.util.location.LocatedException;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log.ContextMap;
import org.apache.log.LogEvent;
import org.apache.log.Logger;

/**
 * An extended pattern formatter. New patterns defined by this class are:
 * <ul>
 * <li><code>class</code>: Outputs the name of the class that has logged the
 *     message. The optional <code>short</code> subformat removes the
 *     package name. Warning: This pattern works only if formatting occurs in
 *     the same thread as the call to Logger, i.e. it won't work with
 *     <code>AsyncLogTarget</code>.</li>
 * <li><code>uri</code>: Outputs the request URI.</li>
 * <li><code>query</code>: Outputs the request query string</li>
 * <li><code>thread</code>: Outputs the name of the current thread (first element
 *     on the context stack).</li>
 * <li><code>host</code>: Outputs the request host header.<li>
 * <li><code>rootThrowable</code>: Outputs the root throwable message and
 *     stacktrace.<li>
 * </ul>
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @deprecated This class will be removed in 2.2
 * @version $Id$
 */
public class CocoonLogFormatter extends ExtensiblePatternFormatter
{
    /**
     * The constant defining the default stack depth when
     * none other is specified.
     */
    public static final int DEFAULT_STACK_DEPTH = 8;

    protected final static int     TYPE_CLASS  = MAX_TYPE + 1;
    protected final static int     TYPE_URI    = MAX_TYPE + 2;
    protected final static int     TYPE_THREAD = MAX_TYPE + 3;
    protected final static int     TYPE_HOST   = MAX_TYPE + 4;
    protected final static int     TYPE_QUERY  = MAX_TYPE + 5;
    protected final static int     TYPE_ROOTTHROWABLE = MAX_TYPE + 6;

    protected final static String  TYPE_CLASS_STR       = "class";
    protected final static String  TYPE_CLASS_SHORT_STR = "short";

    protected final static String  TYPE_URI_STR         = "uri";
    protected final static String  TYPE_THREAD_STR      = "thread";
    protected final static String  TYPE_HOST_STR        = "host";
    protected final static String  TYPE_QUERY_STR       = "query";
    protected final static String  TYPE_ROOTTHROWABLE_STR = "rootThrowable";

    private static final String DEFAULT_TIME_PATTERN = "(yyyy-MM-dd) HH:mm.ss:SSS";
    private static final FastDateFormat dateFormatter = FastDateFormat.getInstance(DEFAULT_TIME_PATTERN);

    /**
     * Hack to get the call stack as an array of classes. The
     * SecurityManager class provides it as a protected method, so
     * change it to public through a new method !
     */
    static class CallStack extends SecurityManager {
        /**
         * Returns the current execution stack as an array of classes.
         * The length of the array is the number of methods on the execution
         * stack. The element at index 0 is the class of the currently executing
         * method, the element at index 1 is the class of that method's caller,
         * and so on.
         *
         * @return current execution stack as an array of classes.
         */
        public Class[] get() {
            return getClassContext();
        }
    }

    /**
     * The class that we will search for in the call stack
     * (Avalon logging abstraction)
     */
    private final Class logkitClass = LogKitLogger.class;

    /**
     * The class that we will search for in the call stack
     * (LogKit logger)
     */
    private final Class loggerClass = Logger.class;

    /**
     * The SecurityManager implementation which gives us access to
     * the stack frame
     */
    private CallStack callStack;

    /**
     * The depth to which stacktraces are printed out
     */
    //private final int m_stackDepth;


    public CocoonLogFormatter() {
        this(DEFAULT_STACK_DEPTH);
    }

    public CocoonLogFormatter(int stackDepth) {
        try {
            this.callStack = new CallStack();
        } catch (SecurityException e) {
            // Ignore security exception
        }
        //this.m_stackDepth = stackDepth;
    }

    protected int getTypeIdFor(String type) {
        // Search for new patterns defined here, or else delegate
        // to the parent class
        if (type.equalsIgnoreCase(TYPE_CLASS_STR)) {
            return TYPE_CLASS;
        } else if (type.equalsIgnoreCase(TYPE_URI_STR)) {
            return TYPE_URI;
        } else if (type.equalsIgnoreCase(TYPE_THREAD_STR)) {
            return TYPE_THREAD;
        } else if (type.equalsIgnoreCase(TYPE_HOST_STR)) {
            return TYPE_HOST;
        } else if (type.equalsIgnoreCase(TYPE_QUERY_STR)) {
            return TYPE_QUERY;
        } else if (type.equalsIgnoreCase(TYPE_ROOTTHROWABLE_STR)) {
            return TYPE_ROOTTHROWABLE;
        } else {
            return super.getTypeIdFor(type);
        }
    }

    protected String formatPatternRun(LogEvent event, PatternRun run) {
        // Format new patterns defined here, or else delegate to
        // the parent class
        switch (run.m_type) {
            case TYPE_CLASS:
                return getClass(run.m_format);
            case TYPE_URI:
                return getURI(event.getContextMap());
            case TYPE_THREAD:
                return getThread(event.getContextMap());
            case TYPE_HOST:
                return getHost(event.getContextMap());
            case TYPE_QUERY:
                return getQueryString(event.getContextMap());
            case TYPE_ROOTTHROWABLE:
                Throwable thr = event.getThrowable();
                Throwable root = ExceptionUtils.getRootCause(thr); // Can be null if no cause
                return getStackTrace(root == null ? thr : root, run.m_format);
        }
        return super.formatPatternRun(event, run);
    }

    /**
     * Finds the class that has called Logger.
     */
    private String getClass(String format) {
        if (this.callStack != null) {
            Class[] stack = this.callStack.get();

            // Traverse the call stack in reverse order until we find a Logger
            for (int i = stack.length - 1; i >= 0; i--) {
                if (this.logkitClass.isAssignableFrom(stack[i]) ||
                    this.loggerClass.isAssignableFrom(stack[i])) {
                    // Found: the caller is the previous stack element
                    String className = stack[i + 1].getName();
                    // Handle optional format
                    if (TYPE_CLASS_SHORT_STR.equalsIgnoreCase(format)) {
                        className = ClassUtils.getShortClassName(className);
                    }
                    return className;
                }
            }
        }

        // No callStack: can occur when running under SecurityManager, or
        // no logger found in call stack: can occur with AsyncLogTarget
        // where formatting takes place in a different thread.
        return "Unknown-Class";
    }

    /**
     * Find the URI that is being processed.
     */
    private String getURI(ContextMap ctxMap) {
        // Get URI from the the object model.
        if (ctxMap != null) {
            final Object context = ctxMap.get("objectModel");
            if (context != null && context instanceof Map) {
                // Get the request
                final Request request = ObjectModelHelper.getRequest((Map) context);
                if (request != null) {
                    return request.getRequestURI();
                }
            }
        }

        return "Unknown-URI";
    }

    /**
     * Find request query string
     */
    private String getQueryString(ContextMap ctxMap) {
        if (ctxMap != null) {
            final Object context = ctxMap.get("objectModel");
            if (context != null && context instanceof Map) {
                // Get the request
                final Request request = ObjectModelHelper.getRequest((Map) context);
                if (request != null) {
                    final String queryString = request.getQueryString();
                    if (queryString != null) {
                        return "?" + queryString;
                    }
                }
            }
        }
        return "";
    }

    /**
     * Find the host header of the request that is being processed.
     */
    private String getHost(ContextMap ctxMap) {
        // Get URI from the the object model.
        if (ctxMap != null) {
            final Object context = ctxMap.get("objectModel");
            if (context != null && context instanceof Map) {
                // Get the request
                final Request request = ObjectModelHelper.getRequest((Map) context);
                if (request != null) {
                    return request.getHeader("host");
                }
            }
        }

        return "Unknown-Host";
    }

    /**
     * Find the thread that is logged this event.
     */
    private String getThread(ContextMap ctxMap) {
        // Get thread name from the context.
        if (ctxMap != null) {
            final String threadName = (String) ctxMap.get("threadName");
            if (threadName != null) {
                return threadName;
            }
        }

        return "Unknown-Thread";
    }

    /**
     * Utility method to format stack trace so that CascadingExceptions are
     * formatted with all nested exceptions.
     *
     * <p>FIXME: copied from AvalonFormatter, to be removed if ExtensiblePatternFormatter
     * replaces PatternFormatter.</p>
     *
     * @param throwable the throwable instance
     * @param format ancilliary format parameter - allowed to be null
     * @return the formatted string
     */
    protected String getStackTrace(final Throwable throwable, final String format) {
        if (throwable != null) {
            LocatedException.ensureCauseChainIsSet(throwable);
            return ExceptionUtils.getStackTrace(throwable);
            //return ExceptionUtil.printStackTrace(throwable, m_stackDepth);
        }

        return null;
    }

    /**
     * Utility method to format time.
     *
     * @param time the time
     * @param pattern ancilliary pattern parameter - allowed to be null
     * @return the formatted string
     */
    protected String getTime(final long time, String pattern) {
        if (pattern == null || DEFAULT_TIME_PATTERN.equals(pattern)) {
            return dateFormatter.format(time);
        }
        return FastDateFormat.getInstance(pattern).format(time);
    }
}
