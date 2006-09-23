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
package org.apache.cocoon.portal.om;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.CopletInstanceEvent;
import org.apache.cocoon.portal.event.coplet.CopletInstanceSizingEvent;
import org.apache.cocoon.portal.event.coplet.CopletJXPathEvent;

/**
 * This class contains constants and utility methods for the standard features
 * of a coplet instance.
 *
 * @version $Id$
 */
public final class CopletInstanceFeatures {

    protected static final String CHANGED_COPLETS_ATTRIBUTE_NAME = CopletInstanceFeatures.class.getName() + "/ChangedCoplets";

    /**
     * Tests if this is a sizing event for a coplet instance.
     */
    public static boolean isSizingEvent(CopletInstanceEvent cie) {
        if ( cie instanceof CopletInstanceSizingEvent ) {
            return true;
        }
        if ( cie instanceof CopletJXPathEvent ) {
            if ( "size".equals(((CopletJXPathEvent)cie).getPath()) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the new size of the sizing event.
     */
    public static int getSize(CopletInstanceEvent cie) {
        if ( cie instanceof CopletInstanceSizingEvent ) {
            return ((CopletInstanceSizingEvent)cie).getSize();
        }
        if ( cie instanceof CopletJXPathEvent ) {
            CopletJXPathEvent e = (CopletJXPathEvent)cie;
            if ( "size".equals(e.getPath()) && e.getValue() != null ) {
                return Integer.valueOf(e.getValue().toString()).intValue();
            }
        }
        return -1;
    }

    public static List getChangedCopletInstanceDataObjects(PortalService service) {
        List list = (List)service.getUserService().getTemporaryAttribute(CHANGED_COPLETS_ATTRIBUTE_NAME);
        if ( list == null ) {
            return Collections.EMPTY_LIST;
        }
        return list;
    }

    public static void addChangedCopletInstanceData(PortalService service,
                                                    CopletInstance cid) {
        List list = (List)service.getUserService().getTemporaryAttribute(CHANGED_COPLETS_ATTRIBUTE_NAME);
        if ( list == null ) {
            list = new ArrayList();
        }
        if ( !list.contains(cid) ) {
            list.add(cid);
        }
        service.getUserService().setTemporaryAttribute(CHANGED_COPLETS_ATTRIBUTE_NAME, list);
    }

    public static String sizeToString(int value) {
        switch (value) {
            case CopletInstance.SIZE_NORMAL : return "normal";
            case CopletInstance.SIZE_FULLSCREEN : return "fullscreen";
            case CopletInstance.SIZE_MAXIMIZED : return "maximized";
            case CopletInstance.SIZE_MINIMIZED : return "minimized";
            default:
                return "";
        }
    }
}
