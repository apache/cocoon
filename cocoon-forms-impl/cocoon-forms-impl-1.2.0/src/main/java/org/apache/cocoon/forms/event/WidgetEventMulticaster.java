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
package org.apache.cocoon.forms.event;

import java.awt.AWTEventMulticaster;
import java.util.EventListener;

import org.apache.cocoon.forms.formmodel.tree.TreeSelectionEvent;
import org.apache.cocoon.forms.formmodel.tree.TreeSelectionListener;

/**
 * Convenience class to handle all widget event listeners. See
 * <code>java.awt.AWTEventMulticaster</code> for more information on its use.
 * 
 * @version $Id$
 */
public class WidgetEventMulticaster extends AWTEventMulticaster implements
    ActionListener, ValueChangedListener, ProcessingPhaseListener, RepeaterListener {

    protected WidgetEventMulticaster(EventListener a, EventListener b) {
        super(a, b);
    }
    
    //-- Create ---------------------------------------------------------------
    
    public static CreateListener add(CreateListener a, CreateListener b) {
        return (CreateListener)addInternal(a, b);
    }
    
    public static CreateListener remove(CreateListener l, CreateListener oldl) {
        return (CreateListener)removeInternal(l, oldl);
    }
    
    public void widgetCreated(CreateEvent e) {
        ((CreateListener)a).widgetCreated(e);
        ((CreateListener)b).widgetCreated(e);
    }

    //-- Action ---------------------------------------------------------------

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

    //-- ValueChanged ---------------------------------------------------------

    public static ValueChangedListener add(ValueChangedListener a, ValueChangedListener b) {
        return (ValueChangedListener)addInternal(a, b);
    }
    
    public static ValueChangedListener remove(ValueChangedListener l, ValueChangedListener oldl) {
        return (ValueChangedListener)removeInternal(l, oldl);
    }
    
    public void valueChanged(ValueChangedEvent e) {
        ((ValueChangedListener)a).valueChanged(e);
        ((ValueChangedListener)b).valueChanged(e);
    }
    
    //-- ProcessingPhase ------------------------------------------------------

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
    
    //-- TreeSelectionChanged ---------------------------------------------------------

    public static TreeSelectionListener add(TreeSelectionListener a, TreeSelectionListener b) {
        return (TreeSelectionListener)addInternal(a, b);
    }
    
    public static TreeSelectionListener remove(TreeSelectionListener l, TreeSelectionListener oldl) {
        return (TreeSelectionListener)removeInternal(l, oldl);
    }
    
    public void selectionChanged(TreeSelectionEvent e) {
        ((TreeSelectionListener)a).selectionChanged(e);
        ((TreeSelectionListener)b).selectionChanged(e);
    }

    /**
     * Can't use the superclass method since it creates an AWTEventMulticaster
     */
    protected static EventListener addInternal(EventListener a, EventListener b) {
        if (a == null)  return b;
        if (b == null)  return a;
        return new WidgetEventMulticaster(a, b);
    }

    // -- RepeaterListener -------------------------------------- 
    
    public void repeaterModified(RepeaterEvent e) {
        ((RepeaterListener)a).repeaterModified(e);
        ((RepeaterListener)b).repeaterModified(e);
    }
    
    public static RepeaterListener add(RepeaterListener a, RepeaterListener b) {
        return (RepeaterListener)addInternal(a, b);
    }
    
    public static RepeaterListener remove(RepeaterListener l, RepeaterListener oldl) {
        return (RepeaterListener)removeInternal(l, oldl);
    }
    
    protected EventListener remove(EventListener oldl) {
        if (oldl == a)  return b;
        if (oldl == b)  return a;
        EventListener a2 = removeInternal(a, oldl);
        EventListener b2 = removeInternal(b, oldl);
        if (a2 == a && b2 == b) {
            return this;        // it's not here
        }
        return addInternal(a2, b2);
    }
    
}
