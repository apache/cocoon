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
package org.apache.cocoon.woody.event.impl;

import org.apache.cocoon.woody.event.ActionListener;
import org.apache.cocoon.woody.event.ValueChangedListener;
import org.apache.cocoon.woody.event.WidgetListener;
import org.apache.cocoon.woody.event.WidgetListenerBuilder;
import org.apache.cocoon.woody.util.JavaScriptHelper;
import org.mozilla.javascript.Script;
import org.w3c.dom.Element;

/**
 * Builds a {@link WidgetListener} based on a JavaScript snippet.
 * <p>
 * The syntax for this listener is as follows :
 * <pre>
 *   &lt;javascript&gt;
 *     var widget = event.sourceWidget;
 *     sourceWidget.setValue("Yeah");
 *   &lt;/javascript&gt;
 * </pre>
 * As shown above, the event that fired this listener is published as the <code>event</code>
 * variable.
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: JavaScriptWidgetListenerBuilder.java,v 1.5 2004/02/11 09:27:55 antonio Exp $
 */
public class JavaScriptWidgetListenerBuilder implements WidgetListenerBuilder {

    public static final JavaScriptWidgetListenerBuilder INSTANCE = new JavaScriptWidgetListenerBuilder();

    public WidgetListener buildListener(Element element, Class listenerClass) throws Exception {

        Script script = JavaScriptHelper.buildScript(element);

        if (listenerClass == ActionListener.class) {
            return new JavaScriptWidgetListener.JSActionListener(script);
        } else if (listenerClass == ValueChangedListener.class) {
            return new JavaScriptWidgetListener.JSValueChangedListener(script);
        } else {
            throw new Exception("Unkonwn event class: " + listenerClass);
        }
    }
}
