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
package org.apache.cocoon.util.log;

import org.apache.avalon.excalibur.logger.factory.FileTargetFactory;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.log.format.Formatter;

/**
 * CocoonTargetFactory class.
 *
 * This factory is able to create different LogTargets specific to Cocoon
 * according to the following configuration syntax:
 *
 * <pre>
 * &lt;file id="foo"&gt;
 *  &lt;filename&gt;${context-key}/real-name/...&lt;/filename&gt;
 *  &lt;format type="raw|pattern|extended|xml|cocoon"&gt;pattern to be used if needed&lt;/format&gt;
 *  &lt;append&gt;true|false&lt;/append&gt;
 *  &lt;rotation type="revolving|unique" init="5" max="10"&gt;
 *   &lt;or&gt;
 *    &lt;size&gt;10000000&lt;/size&gt;
 *    &lt;time&gt;24:00:00&lt;/time&gt;
 *    &lt;time&gt;12:00:00&lt;/time&gt;
 *   &lt;/or&gt;
 *  &lt;/rotate&gt;
 * &lt;/file&gt;
 * </pre>
 *
 * <p>Some explanations about the Elements used in the configuration:</p>
 * <dl>
 *  <dt>&lt;filename&gt;</dt>
 *  <dd>
 *   This denotes the name of the file to log to. It can be constructed
 *   out of entries in the passed Context object as ${context-key}.
 *   This element is required.
 *  </dd>
 *  <dt>&lt;format&gt;</dt>
 *  <dd>
 *   The type attribute of the pattern element denotes the type of
 *   Formatter to be used and according to it the pattern to use for.
 *   This elements defaults to:
 *   <p>
 *    %7.7{priority} %{time}   [%8.8{category}] (%{uri}) %{thread}/%{class:short}: %{message}\\n%{throwable}
 *   </p>
 *  </dd>
 *  <dt>&lt;append&gt;<dt>
 *  <dd>
 *   If the log file should be deleted every time the logger is creates
 *   (normally at the start of the applcation) or not and thus the log
 *   entries will be appended. This elements defaults to false.
 *  </dd>
 *  <dt>&lt;rotation&gt;</dt>
 *  <dd>
 *   This is an optional element.
 *   The type attribute determines which FileStrategy to user
 *   (revolving=RevolvingFileStrategy, unique=UniqueFileStrategy).
 *   The required init and max attribute are used to determine the initial and
 *   maximum rotation to use on a type="revolving" attribute.
 *  </dd>
 *  <dt>&lt;or&gt;</dt>
 *  <dd>uses the OrRotateStrategy to combine the children</dd>
 *  <dt>&lt;size&gt;</dt>
 *  <dd>
 *   The number of bytes if no suffix used or kilo bytes (1024) if suffixed with
 *   'k' or mega bytes (1024k) if suffixed with 'm' when a file rotation should
 *   occur. It doesn't make sense to specify more than one.
 *  </dd>
 *  <dt>&lt;time&gt;</dt>
 *  <dd>
 *   The time as HH:MM:SS when a rotation should occur. If you like to rotate
 *   a logfile more than once a day put an &lt;or&gt; element immediately after the
 *   &lt;rotation&gt; element and specify the times (and one size, too) inside the
 *   &lt;or&gt; element.
 *  </dd>
 * </dl>
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: CocoonTargetFactory.java,v 1.2 2004/03/05 13:03:01 bdelacretaz Exp $
 */
public class CocoonTargetFactory
    extends FileTargetFactory
{
    //Format of default Cocoon formatter
    private static final String CFORMAT =
        "%7.7{priority} %{time}   [%8.8{category}] (%{uri}) %{thread}/%{class:short}: %{message}\\n%{throwable}";

    //Format of default Cocoon XML formatter
    private static final String XFORMAT =
        "priority time category uri thread class message throwable";

    protected Formatter getFormatter(final Configuration conf)
    {
        final String type = conf.getAttribute("type", "unknown");

        if ("cocoon".equals(type))
        {
            int depth = conf.getAttributeAsInteger( "depth", 0 );
            final CocoonLogFormatter formatter = new CocoonLogFormatter( depth );
            final String format = conf.getValue(CFORMAT);
            formatter.setFormat(format);
            return formatter;
        }
        else if ("xml".equals(type))
        {
            final XMLCocoonLogFormatter formatter = new XMLCocoonLogFormatter();
            final String format = conf.getValue(XFORMAT);
            formatter.setTypes(format);
            return formatter;
        }

        // default formatter
        return super.getFormatter(conf);
    }
}

