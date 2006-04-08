/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents a PortalTool.
 *
 * @version $Id$
 */
public class PortalTool {

    protected final Map functions;

    protected final String toolName;
    protected final String toolId;
    protected final List i18n;

    /**
     * Creates a new Portal Tool
     * @param toolName
     * @param toolId
     * @param functions
     * @param i18n
     */
    public PortalTool(String toolName, String toolId, Map functions, List i18n) {
        this.toolName = toolName;
        this.toolId = toolId;
        this.functions = functions;
        this.i18n = i18n;
    }

    /**
     * Returns a collection of available function.
     */
    public Collection getFunctions() {
        return functions.values();
    }

    /**
     * Returns the function with the id id.
     * @param id
     */
    public PortalToolFunction getFunction(String id) {
        return (PortalToolFunction) functions.get(id); 
    }

    /**
     * not in use!
     */
    public Collection getInternalFunctions() {
        ArrayList internal = new ArrayList();
        Collection funs = functions.values();
        for(Iterator it = funs.iterator(); it.hasNext(); ) {
            PortalToolFunction ptf = (PortalToolFunction) it.next();
            if (ptf.isInternal()) {
                internal.add(ptf);
            }
        }
        return internal;
    }

    /**
     * return all public functions
     */
    public Collection getPublicFunctions() {
        ArrayList publik = new ArrayList();
        Collection funs = functions.values();
        for(Iterator it = funs.iterator(); it.hasNext(); ) {
            PortalToolFunction ptf = (PortalToolFunction) it.next();
            if (!ptf.isInternal()) {
                publik.add(ptf);
            }
        }
        return publik;	    
    }

    /**
     * Returns the id of the tools.
     */
    public String getId() {
        return toolId;
    }

    /**
     * Returns the name of the tool.
     */
    public String getName() {
        return toolName;
    }

    public List getI18n() {
        return i18n;
    }
}
