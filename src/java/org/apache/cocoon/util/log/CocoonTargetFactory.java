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
 * @version CVS $Id: CocoonTargetFactory.java,v 1.1 2003/03/09 00:09:44 pier Exp $
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

