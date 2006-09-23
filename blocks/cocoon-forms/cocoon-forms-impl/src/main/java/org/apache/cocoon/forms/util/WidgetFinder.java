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
package org.apache.cocoon.forms.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.forms.event.RepeaterEvent;
import org.apache.cocoon.forms.event.RepeaterEventAction;
import org.apache.cocoon.forms.event.RepeaterListener;
import org.apache.cocoon.forms.event.WidgetEventMulticaster;
import org.apache.cocoon.forms.formmodel.Repeater;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.commons.lang.StringUtils;

/**
 * An utility class to manage list of widgets.
 * 
 * <p>
 * The {@link org.apache.cocoon.forms.formmodel.Widget#lookupWidget(String)} method is able
 * to only return one widget, while this class returns a list of widgets. It uses a path syntax containing a /./,
 * <code>repeater/./foo</code>, which repreesents all the instances of the foo widget inside the repeater,
 * one per row. Note that it also supports finding a widgets inside multi level repeaters, something like 
 * invoices/./movements/./amount or courseYears/./exams/./preparatoryCourses/./title . 
 * </p>
 * <p>
 * Class has been designed to offer good performances, since the widget list is built only once and
 * is automatically updated when a repeater row is added or removed.
 * {@link org.apache.cocoon.forms.event.RepeaterListener}s can be attached directly to receive notifications
 * of widget additions or removals. 
 * </p>
 * <p>
 * This class is used in {@link org.apache.cocoon.forms.formmodel.CalculatedField}s and 
 * {@link org.apache.cocoon.forms.formmodel.CalculatedFieldAlgorithm}s. 
 * </p>
 * @version $Id$
 */
public class WidgetFinder {

    private boolean keepUpdated = false;
   
    // Holds all the widgets not child of a repeater.
    private List noRepeaterWidgets = null;
    // Map repeater -> Set of Strings containing paths
    private Map repeaterPaths = null;
    // Map repeater -> Set of Widgets
    private Map repeaterWidgets = null;
    // A List of recently added widgets, will get cleared when getNewAdditions is called.
    private List newAdditions = new ArrayList();
    
    private RefreshingRepeaterListener refreshingListener = new RefreshingRepeaterListener();
    
    private RepeaterListener listener;
    
    /**
     * Searches for widgets. It will iterate on the given paths and find all
     * corresponding widgets. If a path is in the forms repeater/* /widget
     * then all the rows of the repeater will be iterated and subwidgets
     * will be fetched. 
     * @param context The context widget to start from.
     * @param paths An iterator of Strings containing the paths.
     * @param keepUpdated If true, listeners will be installed on repeaters
     * to keep lists updated without polling.
     */
    public WidgetFinder(Widget context, Iterator paths, boolean keepUpdated) {
        this.keepUpdated = keepUpdated;
        while (paths.hasNext()) {
            String path= (String)paths.next();
            path = toAsterisk(path);
            if (path.indexOf('*') == -1) {
                addSimpleWidget(context, path);
            } else {
                recurseRepeaters(context, path, true);
            }
        }
    }
    
    /**
     * Searches for widgets. If path is in the forms repeater/* /widget
     * then all the rows of the repeater will be iterated and subwidgets
     * will be fetched. 
     * @param context The context widget to start from.
     * @param path Path to search for..
     * @param keepUpdated If true, listeners will be installed on repeaters
     * to keep lists updated without polling.
     */
    public WidgetFinder(Widget context, String path, boolean keepUpdated) {
        path = toAsterisk(path);
        this.keepUpdated = keepUpdated;
        if (path.indexOf('*') == -1) {
            addSimpleWidget(context, path);
        } else {
            recurseRepeaters(context, path, true);
        }        
    }

    private String toAsterisk(String path) {
        return StringUtils.replace(path, "/./", "/*/");
    }

    /**
     * Recurses a repeater path with asterisk.
     * @param context The context widget.
     * @param path The path.
     */
    private void recurseRepeaters(Widget context, String path, boolean root) {
        String reppath = path.substring(0, path.indexOf('*') - 1);
        String childpath = path.substring(path.indexOf('*') + 2);
        Widget wdg = context.lookupWidget(reppath);
        if (wdg == null) {
            if (root) {
                throw new IllegalArgumentException("Cannot find a repeater with path " + reppath + " relative to widget " + context.getName());
            } else {
                return;
            }
        }
        if (!(wdg instanceof Repeater)) {
            throw new IllegalArgumentException("The widget with path " + reppath + " relative to widget " + context.getName() + " is not a repeater!");
        }
        Repeater repeater = (Repeater)wdg;
        if (context instanceof Repeater.RepeaterRow) {
            // Add this repeater to the repeater widgets
            addRepeaterWidget((Repeater) context.getParent(), repeater);
        }
        
        addRepeaterPath(repeater, childpath);
        if (childpath.indexOf('*') != -1) {
            for (int i = 0; i < repeater.getSize(); i++) {
                Repeater.RepeaterRow row = repeater.getRow(i);
                recurseRepeaters(row, childpath, false);
            }
        } else {
            for (int i = 0; i < repeater.getSize(); i++) {
                Repeater.RepeaterRow row = repeater.getRow(i);
	            Widget okwdg = row.lookupWidget(childpath);
	            if (okwdg != null) {
	                addRepeaterWidget(repeater, okwdg);
	            }
            }
        }
    }
    
    /**
     * Adds to the list a widget descendant of a repeater.
     * @param repeater The repeater.
     * @param okwdg The widget.
     */
    private void addRepeaterWidget(Repeater repeater, Widget okwdg) {
        if (this.repeaterWidgets == null) this.repeaterWidgets = new HashMap();
        Set widgets = (Set) this.repeaterWidgets.get(repeater);
        if (widgets == null) {
            widgets = new HashSet();
            this.repeaterWidgets.put(repeater, widgets);
        }
        widgets.add(okwdg);
        newAdditions.add(okwdg);
    }

    /**
     * Adds a repeater monitored path.
     * @param repeater The repeater.
     * @param childpath The child part of the path.
     */
    private void addRepeaterPath(Repeater repeater, String childpath) {
        if (this.repeaterPaths == null) this.repeaterPaths = new HashMap();
        Set paths = (Set) this.repeaterPaths.get(repeater);
        if (paths == null) {
            paths = new HashSet();
            this.repeaterPaths.put(repeater, paths); 
            if (keepUpdated) repeater.addRepeaterListener(refreshingListener);
        }
        paths.add(childpath);
    }

    /**
     * Called when a new row addition event is received from a monitored repeater.
     * @param repeater The repeated that generated the event.
     * @param index The new row index.
     */
    protected void refreshForAdd(Repeater repeater, int index) {
        Repeater.RepeaterRow row = repeater.getRow(index);
        if (this.repeaterPaths == null) this.repeaterPaths = new HashMap();
        Set paths = (Set) this.repeaterPaths.get(repeater);
        for (Iterator iter = paths.iterator(); iter.hasNext();) {
            String path = (String) iter.next();
            if (path.indexOf('*') != -1) {
                recurseRepeaters(row, path, false);
            } else {
                Widget wdg = row.lookupWidget(path);
                if (wdg == null) {
                    throw new IllegalStateException("Even after row addition cannot find a widget with path " + path + " in repeater " + repeater.getName());
                }
                addRepeaterWidget(repeater, wdg);
            }
        }
    }
    
    /**
     * Called when a row deletion event is received from a monitored repeater.
     * @param repeater The repeated that generated the event.
     * @param index The deleted row index.
     */
    protected void refreshForDelete(Repeater repeater, int index) {
        Repeater.RepeaterRow row = repeater.getRow(index);
        Set widgets = (Set) this.repeaterWidgets.get(repeater);
        for (Iterator iter = widgets.iterator(); iter.hasNext();) {
            Widget widget = (Widget) iter.next();
            boolean ischild = false;
            Widget parent = widget.getParent();
            while (parent != null) {
                if (parent == row) {
                    ischild = true;
                    break;
                }
                parent = parent.getParent();
            }
            if (ischild) {
                iter.remove();
                if (widget instanceof Repeater) {
                    if (this.repeaterPaths != null) this.repeaterPaths.remove(widget);
                    this.repeaterWidgets.remove(widget);
                }
            }
        }
    }

    /**
     * Called when a repeater clear event is received from a monitored repeater.
     * @param repeater The repeated that generated the event.
     */  
    protected void refreshForClear(Repeater repeater) {
        Set widgets = (Set) this.repeaterWidgets.get(repeater);
        for (Iterator iter = widgets.iterator(); iter.hasNext();) {
            Widget widget = (Widget) iter.next();
            if (widget instanceof Repeater) {
                if (this.repeaterPaths != null) this.repeaterPaths.remove(widget);
                this.repeaterWidgets.remove(widget);
            }
        }
        widgets.clear();
    }
    
    /**
     * Adds a widget not contained in a repeater.
     * @param context
     * @param path
     */
    private void addSimpleWidget(Widget context, String path) {
        Widget widget = context.lookupWidget(path);
        if (widget == null) throw new IllegalArgumentException("Cannot find a widget with path " + path + " relative to widget " + context.getName());
        if (this.noRepeaterWidgets == null) this.noRepeaterWidgets = new ArrayList();
        this.noRepeaterWidgets.add(widget);
        newAdditions.add(widget);        
    }
    
    /**
     * Return all widgets found for the given paths.
     * @return A Collection of {@link Widget}s.
     */
    public Collection getWidgets() {
        List list = new ArrayList();
        if (this.noRepeaterWidgets != null) list.addAll(this.noRepeaterWidgets);
        if (this.repeaterWidgets != null) {
	        for (Iterator iter = this.repeaterWidgets.keySet().iterator(); iter.hasNext();) {
	            Repeater repeater = (Repeater) iter.next();
	            list.addAll((Collection)this.repeaterWidgets.get(repeater));
	        }
        }
        return list;
    }
    
    /**
     * @return true if this finder is mutable (i.e. it's monitoring some repeaters) or false if getWidgets() will always return the same list (i.e. it's not monitoring any widget).
     */
    public boolean isMutable() {
        return (this.repeaterPaths != null) && this.repeaterPaths.size() > 0;
    }
    
    
    class RefreshingRepeaterListener implements RepeaterListener {
        public void repeaterModified(RepeaterEvent event) {
            if (event.getAction() == RepeaterEventAction.ROW_ADDED) {
                refreshForAdd((Repeater)event.getSourceWidget(), event.getRow());
            }
            if (event.getAction() == RepeaterEventAction.ROW_DELETING) {
                refreshForDelete((Repeater)event.getSourceWidget(), event.getRow());
            }
            if (event.getAction() == RepeaterEventAction.ROWS_CLEARING) {
                refreshForClear((Repeater)event.getSourceWidget());
            }
            if (listener != null) {
                listener.repeaterModified(event);
            }
        }
    }
    
    /**
     * @return true if new widgets have been added to this list (i.e. new repeater rows have been created) since last time getNewAdditions() was called. 
     */
    public boolean hasNewAdditions() {
        return this.newAdditions.size() > 0;
    }
    
    /**
     * Gets the new widgets that has been added to the list, as a consequence of new repeater rows additions, since
     * last time this method was called or the finder was initialized. 
     * @return A List of {@link Widget}s.
     */
    public List getNewAdditions() {
        List ret = new ArrayList(newAdditions);
        newAdditions.clear();
        return ret;
    }
    
    /**
     * Adds a repeater listener. New widget additions or deletions will be notified thru this listener (events received
     * from monitored repeaters will be forwarded, use {@link #getNewAdditions()} to retrieve new widgets).
     * @param listener The listener to add.
     */
    public void addRepeaterListener(RepeaterListener listener) {
        this.listener = WidgetEventMulticaster.add(this.listener, listener);
    }

    /**
     * Removes a listener. See {@link #addRepeaterListener(RepeaterListener)}.
     * @param listener The listener to remove.
     */
    public void removeRepeaterListener(RepeaterListener listener) {
        this.listener = WidgetEventMulticaster.remove(this.listener, listener);
    }

    /**
     * @return true if there are listeners registered on this instance. See {@link #addRepeaterListener(RepeaterListener)}.
     */
    public boolean hasRepeaterListeners() {
        return this.listener != null;
    }
}
