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

import org.apache.avalon.excalibur.logger.factory.StreamTargetFactory;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.log.format.Formatter;

/**
 * TargetFactory for {@link org.apache.log.output.io.StreamTarget}.
 *
 * This factory is able to create different StreamTargets according to the following
 * configuration syntax:
 * <pre>
 * &lt;stream id="foo"&gt;
 *  &lt;stream&gt;<i>stream-context-name</i>&lt;/stream&gt;
 *  &lt;format type="<i>raw|pattern|extended|xml|cocoon</i>"&gt;<i>pattern to be used if needed</i>&lt;/format&gt;
 * &lt;/stream&gt;
 * </pre>
 *
 * <p>The "stream-context-name" is the name of an <code>java.io.OutputStream</code> that
 * is fetched in the context. This context contains two predefined streams :
 * <li>"<code>System.out</code>" for the system output stream,</li>
 * <li>"<code>System.err</code>" for the system error stream.</li>
 * </p>
 *
 * <p>The syntax of "format" is the same as in <code>CocoonTargetFactory</code>.</p>
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: CocoonStreamTargetFactory.java,v 1.3 2004/03/05 13:03:01 bdelacretaz Exp $
 */
public class CocoonStreamTargetFactory
    extends StreamTargetFactory {
        
    //Format of default Cocoon formatter
    private static final String CFORMAT =
        "%7.7{priority} %{time}   [%8.8{category}] (%{uri}) %{thread}/%{class:short}: %{message}\\n%{throwable}";

    //Format of default Cocoon XML formatter
    private static final String XFORMAT =
        "priority time category uri thread class message throwable";

    protected Formatter getFormatter(final Configuration conf) {
        final String type = conf.getAttribute("type", "unknown");

        if ("cocoon".equals(type)) {
            int depth = conf.getAttributeAsInteger( "depth", 0 );
            final CocoonLogFormatter formatter = new CocoonLogFormatter( depth );
            final String format = conf.getValue(CFORMAT);
            formatter.setFormat(format);
            return formatter;
        } else if ("xml".equals(type)) {
            final XMLCocoonLogFormatter formatter = new XMLCocoonLogFormatter();
            final String format = conf.getValue(XFORMAT);
            formatter.setTypes(format);
            return formatter;
        }

        // default formatter
        return super.getFormatter(conf);
    }
}

