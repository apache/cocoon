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
package org.apache.cocoon.forms.event;

import java.awt.AWTEventMulticaster;
import java.util.EventListener;

/**
 * Convenience class to handle all widget event listeners. See
 * <code>java.awt.AWTEventMulticaster</code> for more information on its use.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: WidgetEventMulticaster.java,v 1.1 2004/03/09 10:33:45 reinhard Exp $
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
