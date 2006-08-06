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

import java.util.Iterator;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.layout.impl.LinkLayout;


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
		while ( item != null && !item.getParent().isStatic() ) {
			if ( item.getParent() == null ) {
				item = null;
			} else {
			    item = item.getParent().getParent();
			}
		}
		return item;
	}

    /** 
     * The layout traverser gets notified by each layout object in the tree when
     * the layout is traversed using {@link LayoutFeatures#traverseLayout}.
     */
    public interface LayoutTraverser {

        /**
         * Get notified of a layout found during the tree traversal.
         * @param layout The current layout.
         * @return true if the traversal should continue, false otherwise
         */
        boolean processLayout(Layout layout);
    }

    /**
     * Traverse the whole layout tree including links.
     * @param service The portal service.
     * @param rootLayout The root layout to start the traversal.
     * @param traverser The traverser which gets notified
     * @return If the traverser stops the traversing process, the layout will be returned.
     *         Otherwise null wil be returned.
     */
    public static Layout traverseLayout(PortalService   service,
                                        Layout          rootLayout,
                                        LayoutTraverser traverser) {
        return traverseLayout(service, rootLayout, traverser, true);
    }
    
    /**
     * Traverse the whole layout tree. Depending on the parameters, link layouts are
     * either followed or not.
     * @param service The portal service.
     * @param rootLayout The root layout to start the traversal.
     * @param traverser The traverser which gets notified
     * @param followLinks Should LinkLayouts be followed or not.
     * @return If the traverser stops the traversing process, the layout will be returned.
     *         Otherwise null wil be returned.
     */
    public static Layout traverseLayout(PortalService   service,
                                        Layout          rootLayout,
                                        LayoutTraverser traverser,
                                        boolean         followLinks) {
        if ( rootLayout != null ) {
            if ( !traverser.processLayout(rootLayout) ) {
                return rootLayout;
            }
            if ( rootLayout instanceof CompositeLayout ) {
                final Iterator i = ((CompositeLayout)rootLayout).getItems().iterator();
                while ( i.hasNext() ) {
                    final Item item = (Item)i.next();
                    if ( item.getLayout() != null ) {
                        final Layout result = traverseLayout(service, item.getLayout(), traverser);
                        if ( result != null ) {
                            return result;
                        }
                    }
                }
            } else if ( rootLayout instanceof LinkLayout && followLinks) {
                final LinkLayout linkLayout = (LinkLayout)rootLayout;
                return traverseLayout(service, service.getProfileManager().getPortalLayout(linkLayout.getLayoutKey(), linkLayout.getLayoutId()), traverser);
            }
        }
        return null;
    }

    /**
     * Search for a layout containing the coplet instance data.
     */
    public static CopletLayout searchLayout(final PortalService service,
                                            final String copletId,
                                            final Layout rootLayout) {
        if ( copletId == null ) {
            return null;
        }
        return (CopletLayout)traverseLayout(service, rootLayout, new LayoutTraverser() {
           public boolean processLayout(Layout layout) {
               if ( layout instanceof CopletLayout ) {
                   if ( copletId.equals(((CopletLayout)layout).getCopletInstanceId())) {
                       return false;
                   }
               }
               return true;
           }
        });
    }
}
