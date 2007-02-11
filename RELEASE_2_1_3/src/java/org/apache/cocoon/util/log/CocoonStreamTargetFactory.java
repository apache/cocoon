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
 * @version CVS $Id: CocoonStreamTargetFactory.java,v 1.2 2003/08/12 01:06:47 vgritsenko Exp $
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

