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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.avalon.framework.ExceptionUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.log.ContextMap;
import org.apache.log.LogEvent;

/**
 * An extended pattern formatter. New patterns are defined by this class are :
 * <ul>
 * <li><code>class</code> : outputs the name of the class that has logged the
 *     message. The optional <code>short</code> subformat removes the
 *     package name. Warning : this pattern works only if formatting occurs in
 *     the same thread as the call to Logger, i.e. it won't work with
 *     <code>AsyncLogTarget</code>.</li>
 * <li><code>thread</code> : outputs the name of the current thread (first element
 *     on the context stack).</li>
 * <li><code>uri</code> : outputs the request URI.<li>
 * </ul>
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: CocoonLogFormatter.java,v 1.1 2003/03/09 00:09:44 pier Exp $
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

    protected final static String  TYPE_CLASS_STR       = "class";
    protected final static String  TYPE_CLASS_SHORT_STR = "short";

    protected final static String  TYPE_URI_STR         = "uri";
    protected final static String  TYPE_THREAD_STR      = "thread";

    protected final SimpleDateFormat dateFormatter = new SimpleDateFormat("(yyyy-MM-dd) HH:mm.ss:SSS");

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

    /**
     * The class that we will search for in the call stack
     * (Avalon logging abstraction)
     */
    private Class logkitClass = org.apache.avalon.framework.logger.LogKitLogger.class;

    /**
     * The class that we will search for in the call stack
     * (LogKit logger)
     */
    private Class loggerClass = org.apache.log.Logger.class;

    private CallStack callStack = new CallStack();

    //The depth to which stacktraces are printed out
    private final int m_stackDepth;

    public CocoonLogFormatter()
    {
        this( DEFAULT_STACK_DEPTH );
    }

    public CocoonLogFormatter( int stackDepth )
    {
        m_stackDepth = stackDepth;
    }

    protected int getTypeIdFor(String type) {

        // Search for new patterns defined here, or else delegate
        // to the parent class
        if (type.equalsIgnoreCase(TYPE_CLASS_STR))
            return TYPE_CLASS;
        else if (type.equalsIgnoreCase(TYPE_URI_STR))
            return TYPE_URI;
        else if (type.equalsIgnoreCase(TYPE_THREAD_STR))
            return TYPE_THREAD;
        else
            return super.getTypeIdFor( type );
    }

    protected String formatPatternRun(LogEvent event, PatternRun run) {

        // Format new patterns defined here, or else delegate to
        // the parent class
        switch (run.m_type) {
            case TYPE_CLASS :
                return getClass(run.m_format);

            case TYPE_URI :
                return getURI(event.getContextMap());

            case TYPE_THREAD :
                return getThread(event.getContextMap());
        }

        return super.formatPatternRun(event, run);
    }

    /**
     * Finds the class that has called Logger.
     */
    private String getClass(String format) {

        Class[] stack = this.callStack.get();

        // Traverse the call stack in reverse order until we find a Logger
        for (int i = stack.length-1; i >= 0; i--) {
            if (this.logkitClass.isAssignableFrom(stack[i]) ||
                this.loggerClass.isAssignableFrom(stack[i]))
            {
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
     * Find the URI that is being processed.
     */
    private String getURI(ContextMap ctxMap) {
        String result = "Unknown-URI";

        // Get URI from the the object model.
        if (ctxMap != null) {
            Object context = ctxMap.get("objectModel");
            if (context != null && context instanceof Map) {
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
     * Find the thread that is logged this event.
     */
    private String getThread(ContextMap ctxMap) {
        if (ctxMap != null && ctxMap.get("threadName") != null)
            return (String)ctxMap.get("threadName");
        else
            return "Unknown-thread";
    }

    /**
     * Utility method to format stack trace so that CascadingExceptions are
     * formatted with all nested exceptions.
     *
     * FIXME : copied from AvalonFormatter, to be removed if ExtensiblePatternFormatter
     * replaces PatternFormatter.
     *
     * @param throwable the throwable instance
     * @param format ancilliary format parameter - allowed to be null
     * @return the formatted string
     */
    protected String getStackTrace( final Throwable throwable, final String format )
    {
        return throwable == null ? null : ExceptionUtil.printStackTrace(throwable,m_stackDepth);
    }

    /**
     * Utility method to format time.
     *
     * @param time the time
     * @param format ancilliary format parameter - allowed to be null
     * @return the formatted string
     */
    protected String getTime( final long time, final String format )
    {
        return this.dateFormatter.format(new Date());
    }
}
