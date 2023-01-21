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
package org.apache.cocoon.forms.formmodel;

import org.apache.cocoon.forms.FormsException;
import org.apache.cocoon.forms.event.RepeaterEvent;
import org.apache.cocoon.forms.event.RepeaterListener;
import org.apache.cocoon.forms.event.WidgetEventMulticaster;

/**
 * The {@link WidgetDefinition} part of a Repeater widget, see {@link Repeater} for more information.
 *
 * @version $Id$
 */
public class RepeaterDefinition extends AbstractContainerDefinition {

    private int initialSize = 0;
    private int minSize;
    private int maxSize;
    private boolean orderable;
    private RepeaterListener listener;

    private boolean enhanced=false;
    private int initialPage=0;
    private int pageSize;
	private String customPageId;


    public RepeaterDefinition(int initialSize, int minSize, int maxSize,
                              boolean selectable, boolean orderable) {
        super();
        this.initialSize = initialSize;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.orderable = orderable;
    }
    
    public RepeaterDefinition(int initialSize, int minSize, int maxSize, boolean selectable,boolean orderable,boolean enhanced, int initialPage, int pageSize, String customPageId) {
		super();
		this.initialSize = initialSize;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.orderable = orderable;
		this.enhanced = enhanced;
		this.initialPage = initialPage;
		this.pageSize = pageSize;
		this.customPageId = customPageId;
	}

    /**
     * initialize this definition with the other, sort of like a copy constructor
     */
    public void initializeFrom(WidgetDefinition definition) throws Exception {
        super.initializeFrom(definition);

        if (!(definition instanceof RepeaterDefinition)) {
            throw new FormsException("Parent definition " + definition.getClass().getName() + " is not a RepeaterDefinition.",
                                     getLocation());
        }

        RepeaterDefinition other = (RepeaterDefinition) definition;
        this.initialSize = other.initialSize;
        this.maxSize = other.maxSize;
        this.minSize = other.minSize;
        this.enhanced = other.enhanced;
        this.orderable = other.orderable;
        this.initialPage = other.initialPage;
        this.pageSize = other.pageSize;
    }

    public Widget createInstance() {
    	if (enhanced) {
    		return new EnhancedRepeater(this);
    	} else {
    		return new Repeater(this);
    	}
    }

    public int getInitialSize() {
        return this.initialSize;
    }

    public int getMaxSize() {
        return this.maxSize;
    }

    public int getMinSize() {
        return this.minSize;
    }

    public boolean getOrderable() {
        return this.orderable;
    }

    public void addRepeaterListener(RepeaterListener listener) {
        checkMutable();
        this.listener = WidgetEventMulticaster.add(this.listener, listener);
    }

    public void fireRepeaterEvent(RepeaterEvent event) {
        if (this.listener != null) {
            this.listener.repeaterModified(event);
        }
    }

    public boolean hasRepeaterListeners() {
        return this.listener != null;
    }

    public RepeaterListener getRepeaterListener() {
        return this.listener;
    }

	public int getInitialPage() {
		return initialPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public boolean isEnhanced() {
		return enhanced;
	}

	public String getCustomPageId() {
		return customPageId;
	}
    
}
