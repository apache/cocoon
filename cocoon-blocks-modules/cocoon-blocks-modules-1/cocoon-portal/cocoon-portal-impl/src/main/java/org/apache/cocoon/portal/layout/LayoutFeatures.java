/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.layout;

import org.apache.cocoon.portal.layout.impl.CopletLayout;


/**
 * This class contains constants and utility methods for the standard features
 * of a layout object.
 *
 * @version $Id$
 */
public class LayoutFeatures {

    /** This is the name of the temporary layout attribute containing the information
     * about full screen layouts. */
    protected static final String ATTRIBUTE_FULLSCREENINFO = LayoutFeatures.class.getName() + "/fullscreen-info";

    /** This is the name of the temporary layout attribute containing the information
     * about maximized layouts. */
    protected static final String ATTRIBUTE_MAXMIZEDINFO = LayoutFeatures.class.getName() + "/maximized-info";

    /** This is the name of the temporary layout attribute containing the information
     * about the selected tab (as index or name). */
    public static final String ATTRIBUTE_TAB = "tab";

    public static final class RenderInfo {
        public final Layout layout;
        public final Item   item;

        public RenderInfo(Layout l, Item i) {
            this.layout = l;
            this.item = i;
        }
    }

    public static Layout getFullScreenInfo(Layout layout) {
        return (Layout) layout.getTemporaryAttribute(ATTRIBUTE_FULLSCREENINFO);
    }

    public static void setFullScreenInfo(Layout layout, Layout fullScreenLayout) {
        if ( fullScreenLayout == null ) {
            layout.removeTemporaryAttribute(ATTRIBUTE_FULLSCREENINFO);
        } else {
            layout.setTemporaryAttribute(ATTRIBUTE_FULLSCREENINFO, fullScreenLayout);            
        }
    }

    public static RenderInfo getRenderInfo(Layout layout) {
        return (RenderInfo) layout.getTemporaryAttribute(ATTRIBUTE_MAXMIZEDINFO);
    }

    public static void setRenderInfo(Layout layout, RenderInfo info) {
        if ( info == null ) {
            layout.removeTemporaryAttribute(ATTRIBUTE_MAXMIZEDINFO);
        } else {
            layout.setTemporaryAttribute(ATTRIBUTE_MAXMIZEDINFO, info);            
        }
    }

	public static Item searchItemForMaximizedCoplet(CopletLayout layout) {
		Item item = layout.getParent();
		while ( item != null && !item.getParent().isStatic().booleanValue() ) {
			if ( item.getParent() == null ) {
				item = null;
			} else {
			    item = item.getParent().getParent();
			}
		}
		return item;
	}
}
