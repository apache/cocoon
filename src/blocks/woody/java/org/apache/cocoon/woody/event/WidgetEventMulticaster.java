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
package org.apache.cocoon.woody.event;

import java.awt.AWTEventMulticaster;
import java.util.EventListener;

/**
 * Convenience class to handle all widget event listeners. See
 * <code>java.awt.AWTEventMulticaster</code> for more information on its use.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: WidgetEventMulticaster.java,v 1.1 2003/09/24 20:47:05 sylvain Exp $
 */
public class WidgetEventMulticaster extends AWTEventMulticaster implements
    ActionListener, ValueChangedListener, ProcessingPhaseListener {

    protected WidgetEventMulticaster(EventListener a, EventListener b) {
        super(a, b);
    }
    
    public static ActionListener add(ActionListener a, ActionListener b) {
        return (ActionListener)addInternal(a, b);
    }
    
    public static ActionListener remove(ActionListener l, ActionListener oldl) {
        return (ActionListener)removeInternal(l, oldl);
    }
    
    public void actionPerformed(ActionEvent e) {
        ((ActionListener)a).actionPerformed(e);
        ((ActionListener)b).actionPerformed(e);
    }

    public static ValueChangedListener add(ValueChangedListener a, ValueChangedListener b) {
        return (ValueChangedListener)addInternal(a, b);
    }
    
    public static ValueChangedListener remove(ValueChangedListener l, ValueChangedListener oldl) {
        return (ValueChangedListener)removeInternal(l, oldl);
    }
    
    public void phaseEnded(ProcessingPhaseEvent e) {
        ((ProcessingPhaseListener)a).phaseEnded(e);
        ((ProcessingPhaseListener)b).phaseEnded(e);
    }
    
    public static ProcessingPhaseListener add(ProcessingPhaseListener a, ProcessingPhaseListener b) {
        return (ProcessingPhaseListener)addInternal(a, b);
    }
    
    public static ProcessingPhaseListener remove(ProcessingPhaseListener l, ProcessingPhaseListener oldl) {
        return (ProcessingPhaseListener)removeInternal(l, oldl);
    }
    
    public void valueChanged(ValueChangedEvent e) {
        ((ValueChangedListener)a).valueChanged(e);
        ((ValueChangedListener)b).valueChanged(e);
    }
    
    /**
     * Can't use the superclass method since it creates an AWTEventMulticaster
     */
    protected static EventListener addInternal(EventListener a, EventListener b) {
        if (a == null)  return b;
        if (b == null)  return a;
        return new WidgetEventMulticaster(a, b);
    }
}
