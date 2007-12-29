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
package org.apache.cocoon.acting;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simple Action that logs a given message for a given log level.
 * 
 * <p>Parameters:
 * 
 * <pre>
 * level (optional):   Indicates the log level, defaults to 'info'
 * console (optional): Indicates weather the message is also print to console, defaults to 'false'
 * message (optional): The message to log, defaults to 'No log message given'
 * </pre>
 * 
 * <p>Sitemap definition:
 * 
 * <pre>
 * &lt;map:action name=&quot;log&quot; src=&quot;org.apache.cocoon.acting.LogAction&quot;/&gt;
 * </pre>
 * 
 * <p>Example use:
 * 
 * <pre>
 * &lt;map:match pattern=&quot;some-resource&quot;&gt;
 * 
 *   &lt;!-- do something else --&gt;
 * 
 *   &lt;map:act type=&quot;log&quot;&gt;
 *     &lt;map:parameter name=&quot;level&quot; value=&quot;info&quot;/&gt;
 *     &lt;map:parameter name=&quot;message&quot; value=&quot;Log message from sitemap action&quot;/&gt;
 *     &lt;map:parameter name=&quot;console&quot; value=&quot;true&quot;/&gt;
 *   &lt;/map:act&gt;
 * 
 *   &lt;!-- do something else --&gt;
 * 
 * &lt;/map:match&gt;
 * </pre>
 * 
 * @cocoon.sitemap.component.documentation
 * A simple Action that logs a given message for a given log level.
 *
 * @version $Id$
 */
public class LogAction extends AbstractAction implements ThreadSafe {

    /** The default logger for this class. */
    private Log log = LogFactory.getLog(getClass());

    /** Level's parameter name */
    private final static String PARAM_LEVEL = "level";

    /** Level's default value ('info') */
    private final static String LEVEL_DEFAULT = "info";

    /** Message's parameter name */
    private final static String PARAM_MSG = "message";

    /** Message's default value ('No log message given') */
    private final static String MSG_DEFAULT = "No log message given";

    /** Console's parameter name */
    private final static String PARAM_CONSOLE = "console";

    /** Console's default value (false) */
    private final static boolean CONSOLE_DEFAULT = false;

    /**
     * A simple Action that logs a given message for a given log level.
     */
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters par)
            throws Exception {

        final String logLevel = (par.getParameter(PARAM_LEVEL) != null ? (String) par.getParameter(PARAM_LEVEL)
                : LEVEL_DEFAULT);
        final String logMsg = (par.getParameter(PARAM_MSG) != null ? (String) par.getParameter(PARAM_MSG) : MSG_DEFAULT);

        if (par.getParameterAsBoolean(PARAM_CONSOLE, CONSOLE_DEFAULT)) {
            System.out.println(logMsg);
        }

        if (logLevel.equalsIgnoreCase("info")) {
            if (log.isInfoEnabled()) {
                log.info(logMsg);
            }
        } else {
            if (logLevel.equalsIgnoreCase("warn")) {
                if (log.isWarnEnabled()) {
                    log.warn(logMsg);
                }
            } else {
                if (logLevel.equalsIgnoreCase("error")) {
                    if (log.isErrorEnabled()) {
                        log.error(logMsg);
                    }
                } else {
                    if (logLevel.equalsIgnoreCase("fatal")) {
                        if (log.isFatalEnabled()) {
                            log.fatal(logMsg);
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug(logMsg);
                        }
                    }
                }
            }
        }

        return null;
    }
}
